/*
 * Copyright (c) 1986-2015, Serkan OZAL, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tr.com.serkanozal.jemstone.sa.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.BufferOverflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import sun.jvm.hotspot.HotSpotAgent;
import sun.jvm.hotspot.runtime.VM;
import sun.management.VMManagement;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentConfig;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentContext;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentManager;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentParameter;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentPlugin;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentPlugin.JavaVersion;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentResult;
import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentWorker;
import tr.com.serkanozal.jemstone.sa.impl.compressedrefs.HotSpotSACompressedReferencesResult;
import tr.com.serkanozal.jemstone.sa.impl.compressedrefs.HotSpotSACompressedReferencesWorker;
import tr.com.serkanozal.jemstone.sa.impl.stacktracer.HotSpotSAStackTracerParameter;
import tr.com.serkanozal.jemstone.sa.impl.stacktracer.HotSpotSAStackTracerResult;
import tr.com.serkanozal.jemstone.sa.impl.stacktracer.HotSpotSAStackTracerWorker;
import tr.com.serkanozal.jemstone.util.ClasspathUtil;

/**
 * Implementation of {@link HotSpotServiceabilityAgentManager} based on HotSpot SA API.
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
 * @see HotSpotServiceabilityAgentManager
 * 
 * @see HotSpotServiceabilityAgentWorker
 * @see HotSpotServiceabilityAgentResult
 * 
 * @see HotSpotSACompressedReferencesWorker
 * @see HotSpotSACompressedReferencesResult
 *
 * @author Serkan Ozal
 */
public class HotSpotServiceabilityAgentManagerImpl implements HotSpotServiceabilityAgentManager {

    private static final String SKIP_HOTSPOT_SA_INIT_FLAG = "jemstone.hotspotsa.skipHotspotSAInit";
    private static final String SKIP_HOTSPOT_SA_ATTACH_FLAG = "jemstone.hotspotsa.skipHotspotSAAttach";
    private static final String SKIP_CLASSPATH_LOOKUP_FLAG = "jemstone.hotspotsa.skipClasspathLookup";
    private static final String SKIP_JDK_HOME_LOOKUP_FLAG = "jemstone.hotspotsa.skipJdkHomeLookup";
    private static final String SKIP_ALL_JAR_LOOKUP_FLAG = "jemstone.hotspotsa.skipAllJarLookup";
    private static final String PIPELINE_SIZE_PARAMETER = "jemstone.hotspotsa.pipelineSize";
    private static final String MAX_PIPELINE_SIZE_PARAMETER = "jemstone.hotspotsa.maxPipelineSize";
    private static final String DISABLE_EXPANDING_PIPELINE_PARAMETER = "jemstone.hotspotsa.disableExpandingPipeline";
    private static final String TIMEOUT_PARAMETER = "jemstone.hotspotsa.timeout";
    private static final String TRY_WITH_SUDO_FLAG = "jemstone.hotspotsa.tryWithSudo";

    private static final int DEFAULT_TIMEOUT_IN_MSECS = 5000; // 5 seconds
    private static final int VM_CHECK_PERIOD_SENSITIVITY_IN_MSECS = 1000; // 1 seconds
    private static final int DEFAULT_PIPELINE_SIZE_IN_BYTES = 16 * 1024; // 16 KB
    private static final int DEFAULT_MAX_PIPELINE_SIZE_IN_BYTES = 256 * 1024 * 1024; // 256 MB
    private static final int PROCESS_ATTACH_FAILED_EXIT_CODE = 128;
    
    private static final String JAVA_6 = "1.6";
    private static final String JAVA_7 = "1.7";
    private static final String JAVA_8 = "1.8";
    
    private static final String JAVA_SPEC_VERSION = System.getProperty("java.specification.version");
    
    private static final boolean enable;
    private static final int currentProcessId;
    private static final String classpathForAgent;
    private static final int timeout;
    private static final int pipelineSize;
    private static final int maxPipelineSize;
    private static final boolean disableExpandingPipeline;
    private static final boolean sudoRequired;
    private static final Map<String, String> additionalVmArguments = new HashMap<String, String>();
    private static final String errorMessage;

    static {
        final boolean skipInit = Boolean.getBoolean(SKIP_HOTSPOT_SA_INIT_FLAG);

        boolean active = true;
        int currentProcId = -1;
        String classpathForAgentProc = null;
        String errorMsg = null;
        boolean sudoNeeded = false;

        timeout = Integer.getInteger(TIMEOUT_PARAMETER, DEFAULT_TIMEOUT_IN_MSECS);
        pipelineSize = Integer.getInteger(PIPELINE_SIZE_PARAMETER, 
                                          DEFAULT_PIPELINE_SIZE_IN_BYTES);
        maxPipelineSize = Integer.getInteger(MAX_PIPELINE_SIZE_PARAMETER, 
                                             DEFAULT_MAX_PIPELINE_SIZE_IN_BYTES);
        disableExpandingPipeline = Boolean.getBoolean(DISABLE_EXPANDING_PIPELINE_PARAMETER);
        
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
                
                boolean skipLookups = false;
                boolean tryToLookupAtClasspath = !Boolean.getBoolean(SKIP_CLASSPATH_LOOKUP_FLAG);
                boolean tryToLookupAtJdkHome = false;
                boolean tryToLookupAtAllJar = false;

                final String currentClasspath = 
                        normalizePath(ManagementFactory.getRuntimeMXBean().getClassPath());
                
                if (tryToLookupAtClasspath) {
                    try {
                        // Search it at classpath
                        Class.forName(HotSpotServiceabilityAgentUtil.HOTSPOT_AGENT_CLASSNAME);
    
                        // Use current classpath for agent process
                        classpathForAgentProc = currentClasspath;
                        
                        skipLookups = true;
                    } catch (ClassNotFoundException e) {
                        tryToLookupAtJdkHome = !Boolean.getBoolean(SKIP_JDK_HOME_LOOKUP_FLAG);
                    }
                } else {
                    tryToLookupAtJdkHome = !Boolean.getBoolean(SKIP_JDK_HOME_LOOKUP_FLAG);
                }
                
                if (!skipLookups) {
                    if (tryToLookupAtJdkHome) {
                        try {
                            // If it couldn't be found at classpath, try to find it at
                            File hotspotAgentLib = new File(
                                    normalizePath(System.getProperty("java.home")) + "/../lib/sa-jdi.jar");
                            if (hotspotAgentLib.exists()) {
                                // Add JDK home directory based sa-jdi.jar to the head of the classpath 
                                // to override the other possible sa-jdi.jar on the classpath
                                classpathForAgentProc = normalizePath(hotspotAgentLib.getAbsolutePath()) + 
                                                        File.pathSeparator + currentClasspath;
                                skipLookups = true;
                            } else {
                                tryToLookupAtAllJar = !Boolean.getBoolean(SKIP_ALL_JAR_LOOKUP_FLAG);
                            }
                        } catch (Throwable t) {
                            tryToLookupAtAllJar = !Boolean.getBoolean(SKIP_ALL_JAR_LOOKUP_FLAG);
                        }
                    } else {
                        tryToLookupAtAllJar = !Boolean.getBoolean(SKIP_ALL_JAR_LOOKUP_FLAG);
                    }
                }
                
                if (!skipLookups) {
                    if (tryToLookupAtAllJar) {
                        try {
                            StringBuilder cpBuilder = new StringBuilder();
                            for (String cp : currentClasspath.split(File.pathSeparator)) {
                                if ((cp.contains("sa_jdi") || cp.contains("sa-jdi")) 
                                        && !cp.contains("sa-jdi-all")) {
                                    continue;
                                }   
                                cpBuilder.append(cp).append(File.pathSeparator);
                            }
                            classpathForAgentProc = cpBuilder.toString().replace("%20", " ");
                            
                            additionalVmArguments.put("java.system.class.loader", 
                                                      HotSpotJvmAwareSaJdiClassLoader.class.getName());
                            additionalVmArguments.put(HotSpotJvmAwareSaJdiClassLoader.URL_CLASSPATH_VM_ARGUMENT_NAME, 
                                                      ClasspathUtil.getClasspathUrls().toString());
                        } catch (Throwable t) {
                            active = false;
                            errorMsg = "Couldn't find Hotspot SA library (sa-jdi.jar) in both classpath " + 
                                       "and <JAVA_HOME>/../lib/ directory. " + 
                                       "Also using Hotspot SA library from composite library (sa-jdi-all.jar) is disabled. " + 
                                       "So skipping Hotspot SA support ...";
                        }
                    } else {
                        active = false;
                        errorMsg = "Couldn't find Hotspot SA library (sa-jdi.jar) in both classpath " + 
                                   "and <JAVA_HOME>/../lib/ directory. " + 
                                   "Also using Hotspot SA library from composite library (sa-jdi-all.jar) is disabled. " + 
                                   "So skipping Hotspot SA support ...";
                    }
                }    
            }

            if (active) {
                try {
                    // First check attempt for HotSpot agent connection without "sudo" command
                    executeOnHotSpotSAInternal(currentProcId, classpathForAgentProc, false, null, null,
                                               timeout, pipelineSize);
                } catch (ProcessAttachFailedException e1) {
                    // Possibly because of insufficient privilege. So "sudo" is required.
                    // So if "sudo" command is valid on OS and user allows "sudo" usage
                    if (isSudoValidOS() && Boolean.getBoolean(TRY_WITH_SUDO_FLAG)) {
                        try {
                            // Second check attempt for HotSpot agent connection but this time with "sudo" command
                            executeOnHotSpotSAInternal(currentProcId, classpathForAgentProc, true, null, null,
                                                       timeout, pipelineSize);

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
        currentProcessId = currentProcId;
        classpathForAgent = classpathForAgentProc;
        errorMessage = errorMsg;
        sudoRequired = sudoNeeded;
    }
    
    private static final HotSpotServiceabilityAgentManager INSTANCE = new HotSpotServiceabilityAgentManagerImpl();
    
    @SuppressWarnings("rawtypes")
    private final Map<String, HotSpotServiceabilityAgentPlugin> pluginMap = 
            new ConcurrentHashMap<String, HotSpotServiceabilityAgentPlugin>();

    private HotSpotServiceabilityAgentManagerImpl() {

    }
    
    public static HotSpotServiceabilityAgentManager getInstance() {
        return INSTANCE;
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
    
    private static void safelyClose(MappedByteBuffer buffer) {
        if (buffer != null) {
            sun.misc.Cleaner cleaner = ((sun.nio.ch.DirectBuffer) buffer).cleaner();
            cleaner.clean();
        }
    }
    
    private static void safelyClose(FileChannel channel) {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
    
    private static void safelyClose(File file) {
        if (file != null) {
            file.delete();
        }
    }
    
    private static byte[] serializeObject(Object obj) throws IOException {
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.flush();
            return baos.toByteArray();
        } finally {
            safelyClose(oos);
            safelyClose(baos);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T deserializeObject(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            bais = new ByteArrayInputStream(data);
            ois = new ObjectInputStream(bais);
            return (T) ois.readObject();
        } finally {
            safelyClose(ois);
            safelyClose(bais);
        }
    }

    private static 
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R executeOnHotSpotSAInternal(HotSpotServiceabilityAgentWorker<P, R> worker, P param, 
            int timeoutInMsecs, int pipelineSizeInBytes, int processId) {
        checkEnable();

        return executeOnHotSpotSAInternal(processId, classpathForAgent, sudoRequired, worker, param, 
                                          timeoutInMsecs, pipelineSizeInBytes);
    }
    
    private static 
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R executeOnHotSpotSAInternal(int procId, String classpath, boolean sudoRequired,
            HotSpotServiceabilityAgentWorker<P, R> worker, P param, 
            int timeoutInMsecs, int pipelineSizeInBytes) {
        try {
            return doExecuteOnHotSpotSAInternal(procId, classpath, sudoRequired, worker, param, 
                                                timeoutInMsecs, pipelineSizeInBytes);
        } catch (Throwable t) {
            Throwable cause = t.getCause();
            if (t instanceof BufferOverflowException || cause instanceof BufferOverflowException) {
                if (!disableExpandingPipeline) {
                    int nextPipelineSizeInBytes = 2 * pipelineSizeInBytes;
                    if (nextPipelineSizeInBytes <= maxPipelineSize) {
                        return executeOnHotSpotSAInternal(procId, classpath, sudoRequired, worker, param, 
                                                          timeoutInMsecs, nextPipelineSizeInBytes);
                    }
                }
            }
            throw t;
        }
    }

    @SuppressWarnings({ "unchecked", "resource" })
    private static 
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R doExecuteOnHotSpotSAInternal(int procId, String classpath, boolean sudoRequired,
            HotSpotServiceabilityAgentWorker<P, R> worker, P param, 
            int timeoutInMsecs, int pipelineSizeInBytes) {
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
        for (Map.Entry<String, String> vmArg : additionalVmArguments.entrySet()) {
            args.add("-D" + vmArg.getKey() + "=" + vmArg.getValue());
        }
        args.add(HotSpotServiceabilityAgentManagerImpl.class.getName());

        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        BufferedReader err = null;
        Process agentProcess = null;
        File pipelineFile = null;
        FileChannel pipelineChannel = null;
        MappedByteBuffer pipelineBuffer = null;
        
        try {
            // Create an external Java process to connect this process as HotSpot agent
            agentProcess = new ProcessBuilder(args).start();

            // Create a temporary file to be used as pipeline between caller process and HotSpot SA process
            pipelineFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
            pipelineFile.deleteOnExit();
            
            // Open a connection to memory mapped file based pipeline between caller process and HotSpot SA process
            pipelineChannel = new RandomAccessFile(pipelineFile, "rw").getChannel();
            pipelineBuffer = pipelineChannel.map(FileChannel.MapMode.READ_WRITE, 0, pipelineSizeInBytes);
            
            if (procId == ATTACH_TO_CURRENT_PROCESS) {
                procId = currentProcessId;
            }
            HotSpotServiceabilityAgentRequest<P, R> request = 
                    new HotSpotServiceabilityAgentRequest<P, R>(procId, pipelineFile.getAbsolutePath(), 
                                                                worker, param, timeoutInMsecs, 
                                                                pipelineSizeInBytes);

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
                throw new ProcessAttachFailedException("Attaching as Hotspot SA to process " + 
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
                String errStr = errBuilder.toString();
                if (!errStr.startsWith("WARNING")) {
                    throw new RuntimeException(errStr);
                }    
            }

            in = new ObjectInputStream(is);
            // Get response from HotSpot agent process
            HotSpotServiceabilityAgentResponse<R> response = (HotSpotServiceabilityAgentResponse<R>) in.readObject();

            if (response != null) {
                if (response.getError() != null) {
                    Throwable error = response.getError();
                    throw error;
                } else {
                    int pipelineDataSize = response.getPipelineDataSize();
                    HotSpotServiceabilityAgentResultWrapper<R> resultWrapper = null;
                    if (pipelineDataSize == HotSpotServiceabilityAgentResponse.PIPELINE_DATA_NOT_USED) {
                        // If pipeline has not been used, get result directly over response
                        resultWrapper = response.getResultWrapper();
                    } else {
                        // If pipeline has been used, read data as byte[] from pipeline and deserialize it to result
                        byte[] data = null;
                        pipelineBuffer = pipelineBuffer.load();
                        if (pipelineBuffer.hasArray()) {
                            data = pipelineBuffer.array();
                        } else {
                            data = new byte[pipelineDataSize];
                            pipelineBuffer.get(data);
                        }
                        resultWrapper = deserializeObject(data);
                    }
                    
                    // Print standard output content if it is exist
                    if (resultWrapper.getStdOut() != null) {
                        System.out.print(resultWrapper.getStdOut());
                    }
                        
                    // Print standard error content if it is exist
                    if (resultWrapper.getStdErr() != null) {
                        System.err.print(resultWrapper.getStdErr());
                    }
                        
                    return resultWrapper.getResult();
                }
            } else {
                return null;
            }
        } catch (ProcessAttachFailedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage(), t);
        } finally {
            safelyClose(out);
            safelyClose(in);
            safelyClose(err);
            safelyClose(pipelineBuffer);
            safelyClose(pipelineChannel);
            safelyClose(pipelineFile);

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
        private final String pipelineFilePath;
        private final HotSpotServiceabilityAgentWorker<P, R> worker;
        private final P param;
        private final int timeout;
        private final int pipelineSize;

        private HotSpotServiceabilityAgentRequest(int processId, String pipelineFilePath,
                HotSpotServiceabilityAgentWorker<P, R> worker, P param) {
            this.processId = processId;
            this.pipelineFilePath = pipelineFilePath;
            this.worker = worker;
            this.param = param;
            this.timeout = HotSpotServiceabilityAgentManagerImpl.timeout;
            this.pipelineSize = HotSpotServiceabilityAgentManagerImpl.pipelineSize;
        }

        private HotSpotServiceabilityAgentRequest(int processId, String pipelineFilePath,
                HotSpotServiceabilityAgentWorker<P, R> worker, P param, 
                int timeout, int pipelineSize) {
            this.processId = processId;
            this.pipelineFilePath = pipelineFilePath;
            this.worker = worker;
            this.param = param;
            this.timeout = timeout;
            this.pipelineSize = pipelineSize;
        }

        public int getProcessId() {
            return processId;
        }
        
        public String getPipelineFilePath() {
            return pipelineFilePath;
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
        
        public int getPipelineSize() {
            return pipelineSize;
        }

    }

    /**
     * Represents response from HotSpot agent process by holding result and error if occurred.
     */
    @SuppressWarnings("serial")
    private static class HotSpotServiceabilityAgentResponse<R extends HotSpotServiceabilityAgentResult> 
            implements Serializable {

        private static final int PIPELINE_DATA_NOT_USED = -1;
        
        private final HotSpotServiceabilityAgentResultWrapper<R> resultWrapper;
        private final Throwable error;
        private final int pipelineDataSize;

        private HotSpotServiceabilityAgentResponse(HotSpotServiceabilityAgentResultWrapper<R> resultWrapper) {
            this.resultWrapper = resultWrapper;
            this.error = null;
            this.pipelineDataSize = PIPELINE_DATA_NOT_USED;
        }

        private HotSpotServiceabilityAgentResponse(Throwable error) {
            this.resultWrapper = null;
            this.error = error;
            this.pipelineDataSize = PIPELINE_DATA_NOT_USED;
        }
        
        private HotSpotServiceabilityAgentResponse(int pipelineDataSize) {
            this.resultWrapper = null;
            this.error = null;
            this.pipelineDataSize = pipelineDataSize;
        }

        public HotSpotServiceabilityAgentResultWrapper<R> getResultWrapper() {
            return resultWrapper;
        }

        public Throwable getError() {
            return error;
        }
        
        public int getPipelineDataSize() {
            return pipelineDataSize;
        }

    }
    
    /**
     * Represents a wrapper object from HotSpot agent process by holding 
     * result, standard output and standard error contents.
     */
    @SuppressWarnings("serial")
    private static class HotSpotServiceabilityAgentResultWrapper<R extends HotSpotServiceabilityAgentResult> 
            implements Serializable {
        
        private final R result;
        private String stdOut;
        private String stdErr;
        
        private HotSpotServiceabilityAgentResultWrapper(R result) {
            this.result = result;
        }
        
        public R getResult() {
            return result;
        }
        
        public String getStdOut() {
            return stdOut;
        }
        
        public void setStdOut(String stdOut) {
            this.stdOut = stdOut;
        }
        
        public String getStdErr() {
            return stdErr;
        }
        
        public void setStdErr(String stdErr) {
            this.stdErr = stdErr;
        }
        
    }

    @SuppressWarnings({ "unchecked", "resource" })
    public static 
    <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    void main(final String[] args) {  
        PrintStream originalStdOutStream = System.out;
        ByteArrayOutputStream stdOutOutputStream = new ByteArrayOutputStream();
        PrintStream stdOutStream = new PrintStream(stdOutOutputStream);
        System.setOut(stdOutStream);
        
        ByteArrayOutputStream stdErrOutputStream = new ByteArrayOutputStream();
        PrintStream stdErrStream = new PrintStream(stdErrOutputStream);
        System.setErr(stdErrStream);
        
        HotSpotAgent hotSpotAgent = null;
        VM vm = null;
        HotSpotServiceabilityAgentResponse<R> response = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        FileChannel pipelineChannel = null;
        MappedByteBuffer pipelineBuffer = null;

        try {
            // Gets request from caller process over standard input
            in = new ObjectInputStream(System.in);
            out = new ObjectOutputStream(bos);

            System.setProperty("sun.jvm.hotspot.debugger.useProcDebugger", "true");
            System.setProperty("sun.jvm.hotspot.debugger.useWindbgDebugger", "true");
            System.setProperty("sun.jvm.hotspot.runtime.VM.disableVersionCheck", "true");

            final HotSpotServiceabilityAgentRequest<P, R> request = 
                    (HotSpotServiceabilityAgentRequest<P, R>) in.readObject();
            
            // Open a connection to memory mapped file based pipeline between caller process and HotSpot SA process
            pipelineChannel = new RandomAccessFile(request.getPipelineFilePath(), "rw").getChannel();
            pipelineBuffer = pipelineChannel.map(FileChannel.MapMode.READ_WRITE, 0, request.getPipelineSize());

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
                    
                    // Wrap the result
                    final HotSpotServiceabilityAgentResultWrapper<R> resultWrapper = 
                            new HotSpotServiceabilityAgentResultWrapper<R>(result);
                    
                    // Insert standard output content to result wrapper if there is
                    byte[] stdOutData = stdOutOutputStream.toByteArray();
                    if (stdOutData != null && stdOutData.length > 0) {
                        resultWrapper.setStdOut(new String(stdOutData));
                    }
                    
                    // Insert standard error content to result wrapper if there is
                    byte[] stdErrData = stdErrOutputStream.toByteArray();
                    if (stdErrData != null && stdErrData.length > 0) {
                        resultWrapper.setStdErr(new String(stdErrData));
                    }
                    
                    // Serialize result
                    byte[] resultData = serializeObject(resultWrapper);
                    
                    // Create a response with size of result
                    response = new HotSpotServiceabilityAgentResponse<R>(resultData.length);

                    // Write result to pipeline
                    pipelineBuffer.put(resultData);
                    pipelineBuffer.force();
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
                    originalStdOutStream.write(bos.toByteArray());
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
     * {@inheritDoc}
     */
    @Override
    public boolean isEnable() {
        return enable;
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R executeOnHotSpotSA(Class<? extends HotSpotServiceabilityAgentWorker<P, R>> workerClass) {
        return executeOnHotSpotSA((HotSpotServiceabilityAgentWorker<P, R>) createWorkerInstance(workerClass), 
                                  null, timeout, pipelineSize, currentProcessId);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R executeOnHotSpotSA(Class<? extends HotSpotServiceabilityAgentWorker<P, R>> workerClass, P param) {
        return executeOnHotSpotSA((HotSpotServiceabilityAgentWorker<P, R>) createWorkerInstance(workerClass), 
                                  param, timeout, pipelineSize, currentProcessId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R executeOnHotSpotSA(HotSpotServiceabilityAgentWorker<P, R> worker) {
        return executeOnHotSpotSAInternal(worker, null, timeout, pipelineSize, currentProcessId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R executeOnHotSpotSA(HotSpotServiceabilityAgentWorker<P, R> worker, P param) {
        return executeOnHotSpotSAInternal(worker, param, timeout, pipelineSize, currentProcessId);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R executeOnHotspotSA(Class<? extends HotSpotServiceabilityAgentWorker<P, R>> workerClass,
            int timeoutInMsecs, int pipelineSizeInBytes, int processId) {
        return executeOnHotSpotSA((HotSpotServiceabilityAgentWorker<P, R>) createWorkerInstance(workerClass), 
                                  null, timeoutInMsecs, pipelineSizeInBytes, processId);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R executeOnHotspotSA(Class<? extends HotSpotServiceabilityAgentWorker<P, R>> workerClass,
            P param, int timeoutInMsecs, int pipelineSizeInBytes, int processId) {
        return executeOnHotSpotSA((HotSpotServiceabilityAgentWorker<P, R>) createWorkerInstance(workerClass), 
                                  param, timeoutInMsecs, pipelineSizeInBytes, processId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R executeOnHotSpotSA(HotSpotServiceabilityAgentWorker<P, R> worker,  
            int timeoutInMsecs, int pipelineSizeInBytes, int processId) {
        return executeOnHotSpotSAInternal(worker, null, timeoutInMsecs, pipelineSizeInBytes, processId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R executeOnHotSpotSA(HotSpotServiceabilityAgentWorker<P, R> worker, P param, 
            int timeoutInMsecs, int pipelineSizeInBytes, int processId) {
        return executeOnHotSpotSAInternal(worker, param, timeoutInMsecs, pipelineSizeInBytes, processId);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <P extends HotSpotServiceabilityAgentPlugin>
    P getPlugin(String id) {
        return (P) pluginMap.get(id);
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    @Override
    public <P extends HotSpotServiceabilityAgentPlugin>
    void registerPlugin(P plugin) {
        pluginMap.put(plugin.getId(), plugin);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void deregisterPlugin(String id) {
        pluginMap.remove(id);
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <P extends HotSpotServiceabilityAgentPlugin>
    Collection<P> listPlugins() {
        return (Collection<P>) Collections.unmodifiableCollection(pluginMap.values());
    }
    
    @SuppressWarnings("rawtypes")
    private void checkPluginAndJavaVersionCompatibility(HotSpotServiceabilityAgentPlugin plugin) {
        JavaVersion[] supportedJavaVersions = plugin.getSupportedJavaVersions();
        if (supportedJavaVersions != null && supportedJavaVersions.length > 0) {
            int supportedJavaVersionCode = 0;
            for (JavaVersion supportedJavaVersion : supportedJavaVersions) {
                supportedJavaVersionCode |= supportedJavaVersion.getCode();
            }
            boolean versionMismatch = false;
            if (JAVA_6.equals(JAVA_SPEC_VERSION)) {
                if ((supportedJavaVersionCode & JavaVersion.JAVA_6.getCode()) == 0) {
                    versionMismatch = true;
                }
            } else if (JAVA_7.equals(JAVA_SPEC_VERSION)) {
                if ((supportedJavaVersionCode & JavaVersion.JAVA_7.getCode()) == 0) {
                    versionMismatch = true;
                }
            } else if (JAVA_8.equals(JAVA_SPEC_VERSION)) {
                if ((supportedJavaVersionCode & JavaVersion.JAVA_8.getCode()) == 0) {
                    versionMismatch = true;
                }
            } else {
                throw new IllegalStateException("Unsupported Java version: " + JAVA_SPEC_VERSION);
            }
            if (versionMismatch) {
                StringBuilder sb = null;new StringBuilder();
                for (JavaVersion supportedJavaVersion : supportedJavaVersions) {
                    if (sb == null) {
                        sb = new StringBuilder(supportedJavaVersion.name());
                    } else {
                        sb.append(", ");
                        sb.append(supportedJavaVersion.name());
                    }
                }
                throw new IllegalStateException(
                        "This plug-in " + "(" + plugin.getId() + ")" + 
                        " is not supported at Java version " + JAVA_SPEC_VERSION + 
                        " Supported Java versions: " + (sb != null ? sb.toString() : ""));
            }
        }
    }
    
    private <P extends HotSpotServiceabilityAgentParameter, 
             R extends HotSpotServiceabilityAgentResult,
             W extends HotSpotServiceabilityAgentWorker<P, R>> 
    R runPluginInternal(HotSpotServiceabilityAgentPlugin<P, R, W> plugin, P param,
                        HotSpotServiceabilityAgentConfig config) {
        checkPluginAndJavaVersionCompatibility(plugin);

        if (config != null) {
            return executeOnHotSpotSA(plugin.getWorker(), 
                                      param,
                                      config.getTimeoutInMsecs() != HotSpotServiceabilityAgentConfig.CONFIG_NOT_SET ?
                                              config.getTimeoutInMsecs() : timeout,
                                      config.getPipelineSizeInBytes() != HotSpotServiceabilityAgentConfig.CONFIG_NOT_SET ?
                                              config.getPipelineSizeInBytes() : pipelineSize,
                                      config.getProcessId() != HotSpotServiceabilityAgentConfig.CONFIG_NOT_SET ?
                                              config.getProcessId() : currentProcessId);
        } else {
            return executeOnHotSpotSA(plugin.getWorker(), param);
        }
    }
    
    private <P extends HotSpotServiceabilityAgentParameter, 
             R extends HotSpotServiceabilityAgentResult,
             W extends HotSpotServiceabilityAgentWorker<P, R>> 
    R runPluginInternal(HotSpotServiceabilityAgentPlugin<P, R, W> plugin, P param) {
        HotSpotServiceabilityAgentConfig config = plugin.getConfig();
        return runPluginInternal(plugin, param, config);
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <R extends HotSpotServiceabilityAgentResult> 
    R runPlugin(String id) {
        HotSpotServiceabilityAgentPlugin plugin = pluginMap.get(id);
        if (plugin != null) {
            return (R) runPluginInternal(plugin, null);
        } else {
            throw new IllegalArgumentException("No plugin found with id " + id);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <R extends HotSpotServiceabilityAgentResult> 
    R runPlugin(String id, HotSpotServiceabilityAgentConfig config) {
        HotSpotServiceabilityAgentPlugin plugin = pluginMap.get(id);
        if (plugin != null) {
            return (R) runPluginInternal(plugin, null, config);
        } else {
            throw new IllegalArgumentException("No plugin found with id " + id);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R runPlugin(String id, P param) {
        HotSpotServiceabilityAgentPlugin plugin = pluginMap.get(id);
        if (plugin != null) {
            return (R) runPluginInternal(plugin, param);
        } else {
            throw new IllegalArgumentException("No plugin found with id " + id);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <P extends HotSpotServiceabilityAgentParameter, R extends HotSpotServiceabilityAgentResult> 
    R runPlugin(String id, P param, HotSpotServiceabilityAgentConfig config) {
        HotSpotServiceabilityAgentPlugin plugin = pluginMap.get(id);
        if (plugin != null) {
            return (R) runPluginInternal(plugin, param, config);
        } else {
            throw new IllegalArgumentException("No plugin found with id " + id);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <R extends HotSpotServiceabilityAgentResult> 
    R runPlugin(String id, String[] args) {
        HotSpotServiceabilityAgentPlugin plugin = pluginMap.get(id);
        if (plugin != null) {
            return (R) runPluginInternal(plugin, plugin.getParamater(args));
        } else {
            throw new IllegalArgumentException("No plugin found with id " + id);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <P extends HotSpotServiceabilityAgentParameter, 
            R extends HotSpotServiceabilityAgentResult,
            W extends HotSpotServiceabilityAgentWorker<P, R>>
    R runPlugin(HotSpotServiceabilityAgentPlugin<P, R, W> plugin) {
        return runPluginInternal(plugin, null);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <P extends HotSpotServiceabilityAgentParameter, 
            R extends HotSpotServiceabilityAgentResult,
            W extends HotSpotServiceabilityAgentWorker<P, R>>
    R runPlugin(HotSpotServiceabilityAgentPlugin<P, R, W> plugin, P param) {
        return runPluginInternal(plugin, param);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <P extends HotSpotServiceabilityAgentParameter, 
            R extends HotSpotServiceabilityAgentResult,
            W extends HotSpotServiceabilityAgentWorker<P, R>> 
    R runPlugin(HotSpotServiceabilityAgentPlugin<P, R, W> plugin, String[] args) {
        return runPluginInternal(plugin, plugin.getParamater(args));
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public HotSpotSACompressedReferencesResult getCompressedReferences() {
        return (HotSpotSACompressedReferencesResult)
                executeOnHotSpotSA(createWorkerInstance(HotSpotSACompressedReferencesWorker.class), 
                                   HotSpotServiceabilityAgentParameter.VOID);
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public HotSpotSACompressedReferencesResult getCompressedReferences(int processId) {
        return (HotSpotSACompressedReferencesResult)
               executeOnHotSpotSA(createWorkerInstance(HotSpotSACompressedReferencesWorker.class), 
                                  HotSpotServiceabilityAgentParameter.VOID,
                                  timeout, pipelineSize, processId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public HotSpotSAStackTracerResult getStackTracesOfCurrentThread() {
        return getStackTraces(Thread.currentThread().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HotSpotSAStackTracerResult getStackTraces(String ... threadNames) {
        return executeOnHotSpotSA(HotSpotSAStackTracerWorker.class,
                                  new HotSpotSAStackTracerParameter(threadNames));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public HotSpotSAStackTracerResult getStackTraces(int processId, String ... threadNames) {
        return executeOnHotspotSA(HotSpotSAStackTracerWorker.class, 
                                  new HotSpotSAStackTracerParameter(threadNames), 
                                  timeout, pipelineSize, processId);
    }

    public String details() {
        checkEnable();

        return "HotspotServiceabilityAgentSupport [" + 
                "enable=" + enable + 
                ", processId=" + currentProcessId + 
                ", classpathForAgent=" + classpathForAgent + 
                ", errorMessage=" + errorMessage + "]";
    }

}
