package tr.com.serkanozal.jemstone.sa;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import sun.jvm.hotspot.HotSpotAgent;
import sun.jvm.hotspot.runtime.VM;
import sun.management.VMManagement;
import tr.com.serkanozal.jemstone.sa.impl.HotSpotServiceabilityAgentUtil;
import tr.com.serkanozal.jemstone.sa.impl.compressedrefs.HotSpotSACompressedReferencesResult;
import tr.com.serkanozal.jemstone.sa.impl.compressedrefs.HotSpotSACompressedReferencesWorker;

/**
 * Hotspot Serviceability Agent support.
 * 
 * <pre>
 * <b>IMPORTANT NOTE:</b> 
 *      On some UNIX based operating systems and MacOSX operation system, Hotspot Serviceability Agent (SA) process 
 *      attach may fail due to insufficient privilege. So, on these operating systems, user (runs the application) 
 *      must be super user and must be already authenticated for example with <code>"sudo"</code> 
 *      command (also with password) to <code>"/etc/sudoers"</code> file.
 * 
 *      For more information about <code>"sudo"</code>, please have a look:
 *          <a href="http://en.wikipedia.org/wiki/Sudo">http://en.wikipedia.org/wiki/Sudo</a>
 *          <a href="http://linux.about.com/od/commands/l/blcmdl8_sudo.htm">http://linux.about.com/od/commands/l/blcmdl8_sudo.htm</a>
 * </pre>
 * 
 * @see HotSpotServiceabilityAgentWorker
 * @see HotSpotServiceabilityAgentResult
 * 
 * @see HotSpotSACompressedReferencesWorker
 * @see HotSpotSACompressedReferencesResult
 *
 * @author Serkan Ozal
 */
@SuppressWarnings("restriction")
public class HotSpotServiceabilityAgentManager {

    private static final String SKIP_HOTSPOT_SA_INIT_FLAG = "jemstone.skipHotspotSAInit";
    private static final String SKIP_HOTSPOT_SA_ATTACH_FLAG = "jemstone.skipHotspotSAAttach";
    private static final String TRY_WITH_SUDO_FLAG = "jemstone.tryWithSudo";

    private static final int DEFAULT_TIMEOUT_IN_MSECS = 5000; // 5 seconds
    private static final int VM_CHECK_PERIOD_SENSITIVITY_IN_MSECS = 1000; // 1 seconds
    private static final int PROCESS_ATTACH_FAILED_EXIT_CODE = 128;

    private static final boolean enable;
    private static final int processId;
    private static final String classpathForAgent;
    private static final boolean sudoRequired;
    private static final String errorMessage;

    static {
        final boolean skipInit = Boolean.getBoolean(SKIP_HOTSPOT_SA_INIT_FLAG);

        boolean active = true;
        int currentProcId = -1;
        String classpathForAgentProc = null;
        String errorMsg = null;
        boolean sudoNeeded = false;

        if (!skipInit) {
            if (Boolean.getBoolean(SKIP_HOTSPOT_SA_ATTACH_FLAG)) {
                active = false;
                errorMsg = "Hotspot SA attach skip flag (" + SKIP_HOTSPOT_SA_ATTACH_FLAG + ") is set. " + 
                           "So skipping Hotspot SA support ...";
            } else {
                final String jvmName = System.getProperty("java.vm.name").toLowerCase();
                // HotSpot Serviceability Agent is only supported on HotSpot JVM
                if (!jvmName.contains("hotspot") && !jvmName.contains("openjdk")) {
                    active = false;
                    errorMsg = "Hotspot Serviceabiliy Agent is only supported on Hotspot JVM. " + 
                               "So skipping Hotspot SA support ...";
                } else {
                    try {
                        // Find current process id to connect via HotSpot agent
                        RuntimeMXBean mxbean = ManagementFactory.getRuntimeMXBean();
                        Field jvmField = mxbean.getClass().getDeclaredField("jvm");
                        jvmField.setAccessible(true);
                        VMManagement management = (VMManagement) jvmField.get(mxbean);
                        Method method = management.getClass().getDeclaredMethod("getProcessId");
                        method.setAccessible(true);

                        currentProcId = (Integer) method.invoke(management);
                    } catch (Throwable t) {
                        active = false;
                        errorMsg = "Couldn't find id of current JVM process. " + 
                                   "So skipping Hotspot SA support ...";
                    }
                }

                final String currentClasspath = 
                        normalizePath(ManagementFactory.getRuntimeMXBean().getClassPath());
                try {
                    // Search it at classpath
                    Class.forName(HotSpotServiceabilityAgentUtil.HOTSPOT_AGENT_CLASSNAME);

                    // Use current classpath for agent process
                    classpathForAgentProc = currentClasspath;
                } catch (ClassNotFoundException e1) {
                    try {
                        // If it couldn't be found at classpath, try to find it
                        // at
                        File hotspotAgentLib = new File(
                                normalizePath(System.getProperty("java.home")) + "/../lib/sa-jdi.jar");
                        if (hotspotAgentLib.exists()) {
                            classpathForAgentProc = currentClasspath + File.pathSeparator +
                                                    normalizePath(hotspotAgentLib.getAbsolutePath());
                        } else {
                            active = false;
                            errorMsg = "Couldn't find Hotspot SA library (sa-jdi.jar) in both classpath and <JAVA_HOME>/../lib/ directory. " + 
                                       "So skipping Hotspot SA support ...";
                        }
                    } catch (Throwable t2) {
                        active = false;
                        errorMsg = "Couldn't find Hotspot SA library (sa-jdi.jar) in both classpath and <JAVA_HOME>/../lib/ directory. " +
                                   "So skipping Hotspot SA support ...";
                    }
                }
            }

            if (active) {
                try {
                    // First check attempt for HotSpot agent connection without "sudo" command
                    executeOnHotSpotSAInternal(currentProcId, classpathForAgentProc, false, null, null,
                                               DEFAULT_TIMEOUT_IN_MSECS);
                } catch (ProcessAttachFailedException e1) {
                    // Possibly because of insufficient privilege. So "sudo" is required.
                    // So if "sudo" command is valid on OS and user allows "sudo" usage
                    if (isSudoValidOS() && Boolean.getBoolean(TRY_WITH_SUDO_FLAG)) {
                        try {
                            // Second check attempt for HotSpot agent connection but this time with "sudo" command
                            executeOnHotSpotSAInternal(currentProcId, classpathForAgentProc, true, null, null,
                                                       DEFAULT_TIMEOUT_IN_MSECS);

                            sudoNeeded = true;
                        } catch (Throwable t2) {
                            active = false;
                            errorMsg = "Initial Hotspot SA process attach check failed also with 'sudo' " + "(" + t2.getMessage() + ") " +  
                                      "So skipping Hotspot SA support ...";
                        }
                    } else {
                        active = false;
                        errorMsg = "Initial Hotspot SA process attach check failed " + "(" + e1.getMessage()  + ") " + 
                                   "So skipping Hotspot SA support ...";
                    }
                } catch (Throwable t1) {
                    active = false;
                    errorMsg = "Initial Hotspot SA process attach check failed " + "(" + t1.getMessage() + ") " + 
                               "So skipping Hotspot SA support ...";
                }
            }
        }

        enable = active;
        processId = currentProcId;
        classpathForAgent = classpathForAgentProc;
        errorMessage = errorMsg;
        sudoRequired = sudoNeeded;
    }

    private HotSpotServiceabilityAgentManager() {

    }

    private static String normalizePath(String path) {
        return path.replace('\\', '/');
    }

    private static void checkEnable() {
        if (!enable) {
            throw new IllegalStateException(errorMessage);
        }
    }

    /**
     * <p>
     * Checks and gets the condition about if "sudo" command is required for
     * creating external Java process to connect current process as HotSpot
     * agent.
     * </p>
     * 
     * <p>
     * On some UNIX based and MacOSX based operations systems, HotSpot
     * Serviceability Agent (SA) process attach fails due to insufficient
     * privilege. So these processes must be execute as super user.
     * </p>
     * 
     * <pre>
     * See also JVM Bug reports:
     * 
     *      <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7129704">http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7129704</a>
     *      <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7050524">http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7050524</a>
     *      <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7112802">http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7112802</a>
     *      <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7160774">http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7160774</a>
     *      
     *      <a href="https://bugs.openjdk.java.net/browse/JDK-7129704">https://bugs.openjdk.java.net/browse/JDK-7129704</a>
     *      <a href="https://bugs.openjdk.java.net/browse/JDK-7050524">https://bugs.openjdk.java.net/browse/JDK-7050524</a>
     *      <a href="https://bugs.openjdk.java.net/browse/JDK-7112802">https://bugs.openjdk.java.net/browse/JDK-7112802</a>
     *      <a href="https://bugs.openjdk.java.net/browse/JDK-7160774">https://bugs.openjdk.java.net/browse/JDK-7160774</a>
     * </pre>
     * 
     * @return <code>true</code> if "sudo" command is required, otherwise <code>false</code>
     */
    private static boolean isSudoValidOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) { // UNIX based operation system
            return true;
        } else if (osName.contains("mac")) { // MacOSX based operation system
            return true;
        } else {
            return false;
        }
    }

    private static void safelyClose(OutputStream out) {
        if (out != null) {
            try {
                out.flush();
            } catch (IOException e) {
                // ignore
            }
            try {
                out.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private static void safelyClose(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private static void safelyClose(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private static 
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R executeOnHotSpotSAInternal(HotSpotServiceabilityAgentWorker<P, R> worker, P param, 
            int timeoutInMsecs) {
        checkEnable();

        return executeOnHotSpotSAInternal(processId, classpathForAgent, sudoRequired, 
                                          worker, param, timeoutInMsecs);
    }

    @SuppressWarnings("unchecked")
    private static 
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R executeOnHotSpotSAInternal(int procId, String classpath, boolean sudoRequired,
            HotSpotServiceabilityAgentWorker<P, R> worker, P param, int timeoutInMsecs) {
        // Generate required arguments to create an external Java process
        List<String> args = new ArrayList<String>();
        if (sudoRequired) {
            args.add("sudo");
        }
        args.add(normalizePath(System.getProperty("java.home")) + "/" + "bin" + "/" + "java");
        // For preventing infinite loop if attaching process touches this class
        args.add("-D" + SKIP_HOTSPOT_SA_INIT_FLAG + "=true");
        args.add("-cp");
        args.add(classpath);
        args.add(HotSpotServiceabilityAgentManager.class.getName());

        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        BufferedReader err = null;
        Process agentProcess = null;
        try {
            // Create an external Java process to connect this process as HotSpot agent
            agentProcess = new ProcessBuilder(args).start();

            HotSpotServiceabilityAgentRequest<P, R> request = 
                    new HotSpotServiceabilityAgentRequest<P, R>(procId, worker, param, timeoutInMsecs);

            // Get input, output and error streams
            InputStream is = agentProcess.getInputStream();
            OutputStream os = agentProcess.getOutputStream();
            InputStream es = agentProcess.getErrorStream();

            // Send request HotSpot agent process to execute
            out = new ObjectOutputStream(os);
            out.writeObject(request);
            out.flush();

            // At least, for all cases, wait process to finish
            int exitCode = agentProcess.waitFor();
            // Reset it, it has terminated and no need to destroy at the finally
            // block
            agentProcess = null;

            // If process attach failed,
            if (exitCode == PROCESS_ATTACH_FAILED_EXIT_CODE) {
                throw new ProcessAttachFailedException("Attaching as Hotspot SA to current process " + 
                                                       "(id=" + procId + ") from external process failed");
            }

            // At first, check errors
            err = new BufferedReader(new InputStreamReader(es));

            StringBuilder errBuilder = null;
            for (String line = err.readLine(); line != null; line = err.readLine()) {
                if (errBuilder == null) {
                    errBuilder = new StringBuilder();
                }
                errBuilder.append(line).append("\n");
            }
            if (errBuilder != null) {
                throw new RuntimeException(errBuilder.toString());
            }

            in = new ObjectInputStream(is);
            // Get response from HotSpot agent process
            HotSpotServiceabilityAgentResponse<R> response = (HotSpotServiceabilityAgentResponse<R>) in.readObject();

            if (response != null) {
                if (response.getError() != null) {
                    Throwable error = response.getError();
                    throw new RuntimeException(error.getMessage(), error);
                }
                return response.getResult();
            } else {
                return null;
            }
        } catch (ProcessAttachFailedException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage(), t);
        } finally {
            safelyClose(out);
            safelyClose(in);
            safelyClose(err);

            if (agentProcess != null) {
                // If process is still in use, destroy it.
                // When it has terminated, it is set to "null" after "waitFor".
                agentProcess.destroy();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static HotSpotServiceabilityAgentWorker createWorkerInstance(
            Class<? extends HotSpotServiceabilityAgentWorker> workerClass) {
        try {
            return workerClass.newInstance();
        } catch (Throwable t) {
            throw new IllegalArgumentException("Could not create instance of " + workerClass.getName(), t);
        }
    }

    /**
     * Specific exception type to represent process attach fail cases.
     */
    @SuppressWarnings("serial")
    private static class ProcessAttachFailedException extends RuntimeException {

        private ProcessAttachFailedException(String message) {
            super(message);
        }

    }

    /**
     * Represents request to HotSpot agent process by holding process id,
     * timeout and {@link HotSpotServiceabilityAgentWorker} to execute.
     */
    @SuppressWarnings("serial")
    private static class HotSpotServiceabilityAgentRequest<P extends HotSpotServiceabilityAgentParameter, 
                                                           R extends HotSpotServiceabilityAgentResult> 
            implements Serializable {

        private final int processId;
        private final HotSpotServiceabilityAgentWorker<P, R> worker;
        private final P param;
        private final int timeout;

        private HotSpotServiceabilityAgentRequest(int processId,
                HotSpotServiceabilityAgentWorker<P, R> worker, P param) {
            this.processId = processId;
            this.worker = worker;
            this.param = param;
            this.timeout = DEFAULT_TIMEOUT_IN_MSECS;
        }

        private HotSpotServiceabilityAgentRequest(int processId,
                HotSpotServiceabilityAgentWorker<P, R> worker, P param, int timeout) {
            this.processId = processId;
            this.worker = worker;
            this.param = param;
            this.timeout = timeout;
        }

        public int getProcessId() {
            return processId;
        }

        public HotSpotServiceabilityAgentWorker<P, R> getWorker() {
            return worker;
        }
        
        public P getParameter() {
            return param;
        }

        public int getTimeout() {
            return timeout;
        }

    }

    /**
     * Represents response from HotSpot agent process by holding result and error if occurred.
     */
    @SuppressWarnings("serial")
    private static class HotSpotServiceabilityAgentResponse<R extends HotSpotServiceabilityAgentResult> 
            implements Serializable {

        private final R result;
        private final Throwable error;

        private HotSpotServiceabilityAgentResponse(R result) {
            this.result = result;
            this.error = null;
        }

        private HotSpotServiceabilityAgentResponse(Throwable error) {
            this.result = null;
            this.error = error;
        }

        public R getResult() {
            return result;
        }

        public Throwable getError() {
            return error;
        }

    }

    @SuppressWarnings("unchecked")
    public static 
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    void main(final String[] args) {
        HotSpotAgent hotSpotAgent = null;
        VM vm = null;
        HotSpotServiceabilityAgentResponse<R> response = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectInputStream in = null;
        ObjectOutputStream out = null;

        try {
            // Gets request from caller process over standard input
            in = new ObjectInputStream(System.in);
            out = new ObjectOutputStream(bos);

            System.setProperty("sun.jvm.hotspot.debugger.useProcDebugger", "true");
            System.setProperty("sun.jvm.hotspot.debugger.useWindbgDebugger", "true");

            final HotSpotServiceabilityAgentRequest<P, R> request = 
                    (HotSpotServiceabilityAgentRequest<P, R>) in.readObject();
            hotSpotAgent = HotSpotServiceabilityAgentUtil.getHotSpotAgentInstance();
            final HotSpotAgent agent = hotSpotAgent;
            
            Thread t = new Thread() {
                public void run() {
                    try {
                        // Attach to the caller process as HotSpot agent
                        agent.attach(request.getProcessId());
                    } catch (Throwable t) {
                        System.exit(PROCESS_ATTACH_FAILED_EXIT_CODE);
                    }
                };
            };
            t.start();

            // Check until timeout
            for (int i = 0; i < request.getTimeout(); i += VM_CHECK_PERIOD_SENSITIVITY_IN_MSECS) {
                Thread.sleep(VM_CHECK_PERIOD_SENSITIVITY_IN_MSECS); // Wait a little before an attempt
                try {
                    if ((vm = HotSpotServiceabilityAgentUtil.getVMInstance()) != null) {
                        break;
                    }
                } catch (Throwable err) {
                    // There is nothing to do, try another
                }
            }

            // Check about if VM is initialized and ready to use
            if (vm != null) {
                final HotSpotServiceabilityAgentWorker<P, R> worker = request.getWorker();
                final P param = request.getParameter();
                if (worker != null) {
                    // Execute worker and gets its result
                    final R result = worker.run(new HotSpotServiceabilityAgentContext(hotSpotAgent, vm), param);
                    response = new HotSpotServiceabilityAgentResponse<R>(result);
                }
            } else {
                throw new IllegalStateException("VM couldn't be initialized !");
            }
        } catch (Throwable t) {
            // If there is an error, attach it to response
            response = new HotSpotServiceabilityAgentResponse<R>(t);
        } finally {
            if (out != null) {
                try {
                    // Send response back to caller process over standard output
                    out.writeObject(response);
                    out.flush();
                    System.out.write(bos.toByteArray());
                } catch (IOException e) {
                    // There is nothing to do, so just ignore
                }
            }
            if (hotSpotAgent != null) {
                try {
                    hotSpotAgent.detach();
                } catch (IllegalArgumentException e) {
                    // There is nothing to do, so just ignore
                }
            }
        }
    }

    /**
     * Returns <code>true</code> if HotSpot Serviceability Agent support is enable, 
     * otherwise <code>false</code>.
     * 
     * @return the enable state of HotSpot Serviceability Agent support
     */
    public static boolean isEnable() {
        return enable;
    }
    
    /**
     * Executes given typed {@link HotSpotServiceabilityAgentWorker} on HotSpot
     * agent process and returns a {@link HotSpotServiceabilityAgentResult} instance as result.
     * 
     * @param workerClass the type of {@link HotSpotServiceabilityAgentWorker} instance to execute
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of worker execution
     */
    @SuppressWarnings("unchecked")
    public static 
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R executeOnHotSpotSA(Class<? extends HotSpotServiceabilityAgentWorker<P, R>> workerClass) {
        return executeOnHotSpotSA((HotSpotServiceabilityAgentWorker<P, R>) createWorkerInstance(workerClass), 
                                  null, DEFAULT_TIMEOUT_IN_MSECS);
    }

    /**
     * Executes given typed {@link HotSpotServiceabilityAgentWorker} on HotSpot
     * agent process and returns a {@link HotSpotServiceabilityAgentResult} instance as result.
     * 
     * @param workerClass the type of {@link HotSpotServiceabilityAgentWorker} instance to execute
     * @param param       the {@link HotSpotServiceabilityAgentParameter} instance as parameter to worker
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of worker execution
     */
    @SuppressWarnings("unchecked")
    public static 
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R executeOnHotSpotSA(Class<? extends HotSpotServiceabilityAgentWorker<P, R>> workerClass, P param) {
        return executeOnHotSpotSA((HotSpotServiceabilityAgentWorker<P, R>) createWorkerInstance(workerClass), 
                                  param, DEFAULT_TIMEOUT_IN_MSECS);
    }
    
    /**
     * Executes given {@link HotSpotServiceabilityAgentWorker} on HotSpot agent
     * process and returns a {@link HotSpotServiceabilityAgentResult} instance as result.
     * 
     * @param worker the {@link HotSpotServiceabilityAgentWorker} instance to execute
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of worker execution
     */
    public static 
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R executeOnHotSpotSA(HotSpotServiceabilityAgentWorker<P, R> worker) {
        return executeOnHotSpotSAInternal(worker, null, DEFAULT_TIMEOUT_IN_MSECS);
    }

    /**
     * Executes given {@link HotSpotServiceabilityAgentWorker} on HotSpot agent
     * process and returns a {@link HotSpotServiceabilityAgentResult} instance as result.
     * 
     * @param worker the {@link HotSpotServiceabilityAgentWorker} instance to execute
     * @param param the {@link HotSpotServiceabilityAgentParameter} instance as parameter to worker
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of worker execution
     */
    public static 
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R executeOnHotSpotSA(HotSpotServiceabilityAgentWorker<P, R> worker, P param) {
        return executeOnHotSpotSAInternal(worker, param, DEFAULT_TIMEOUT_IN_MSECS);
    }
    
    /**
     * Executes given typed {@link HotSpotServiceabilityAgentWorker} on HotSpot
     * agent process and returns a {@link HotSpotServiceabilityAgentResult} instance as result.
     *
     * @param workerClass    the type of {@link HotSpotServiceabilityAgentWorker} instance to execute
     * @param timeoutInMsecs the timeout in milliseconds to wait at most for terminating
     *                       connection between current process and HotSpot agent process.
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of worker execution
     */
    @SuppressWarnings("unchecked")
    public static 
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R executeOnHotspotSA(Class<? extends HotSpotServiceabilityAgentWorker<P, R>> workerClass,
            int timeoutInMsecs) {
        return executeOnHotSpotSA((HotSpotServiceabilityAgentWorker<P, R>) createWorkerInstance(workerClass), 
                                  null, timeoutInMsecs);
    }

    /**
     * Executes given typed {@link HotSpotServiceabilityAgentWorker} on HotSpot
     * agent process and returns a {@link HotSpotServiceabilityAgentResult} instance as result.
     *
     * @param workerClass    the type of {@link HotSpotServiceabilityAgentWorker} instance to execute
     * @param param          the {@link HotSpotServiceabilityAgentParameter} instance as parameter to worker
     * @param timeoutInMsecs the timeout in milliseconds to wait at most for terminating
     *                       connection between current process and HotSpot agent process.
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of worker execution
     */
    @SuppressWarnings("unchecked")
    public static 
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R executeOnHotspotSA(Class<? extends HotSpotServiceabilityAgentWorker<P, R>> workerClass,
            P param, int timeoutInMsecs) {
        return executeOnHotSpotSA((HotSpotServiceabilityAgentWorker<P, R>) createWorkerInstance(workerClass), 
                                  param, timeoutInMsecs);
    }

    /**
     * Executes given {@link HotSpotServiceabilityAgentWorker} on HotSpot agent
     * process and returns a {@link HotSpotServiceabilityAgentResult} instance as result.
     * 
     * @param worker         the {@link HotSpotServiceabilityAgentWorker} instance to execute
     * @param timeoutInMsecs the timeout in milliseconds to wait at most for terminating
     *                       connection between current process and HotSpot agent process.
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of worker execution
     */
    public static 
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R executeOnHotSpotSA(HotSpotServiceabilityAgentWorker<P, R> worker,  int timeoutInMsecs) {
        return executeOnHotSpotSAInternal(worker, null, timeoutInMsecs);
    }
    
    /**
     * Executes given {@link HotSpotServiceabilityAgentWorker} on HotSpot agent
     * process and returns a {@link HotSpotServiceabilityAgentResult} instance as result.
     * 
     * @param worker         the {@link HotSpotServiceabilityAgentWorker} instance to execute
     * @param param          the {@link HotSpotServiceabilityAgentParameter} instance as parameter to worker
     * @param timeoutInMsecs the timeout in milliseconds to wait at most for terminating
     *                       connection between current process and HotSpot agent process.
     * @return the {@link HotSpotServiceabilityAgentResult} instance as result of worker execution
     */
    public static 
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R executeOnHotSpotSA(HotSpotServiceabilityAgentWorker<P, R> worker, P param, int timeoutInMsecs) {
        return executeOnHotSpotSAInternal(worker, param, timeoutInMsecs);
    }

    /**
     * Gets the compressed references information as
     * {@link HotSpotSACompressedReferencesResult} instance.
     * 
     * @return the compressed references information as
     *         {@link HotSpotSACompressedReferencesResult} instance
     */
    public static HotSpotSACompressedReferencesResult getCompressedReferences() {
        return executeOnHotSpotSA(HotSpotSACompressedReferencesWorker.class, 
                                  HotSpotServiceabilityAgentParameter.VOID);
    }

    /**
     * Gives details about HotSpot Serviceability Agent support.
     * 
     * @return the string representation of HotSpot Serviceability Agent support
     */
    public static String details() {
        checkEnable();

        return "HotspotServiceabilityAgentSupport [" + 
                "enable=" + enable + 
                ", processId=" + processId + 
                ", classpathForAgent=" + classpathForAgent + 
                ", errorMessage=" + errorMessage + "]";
    }

}
