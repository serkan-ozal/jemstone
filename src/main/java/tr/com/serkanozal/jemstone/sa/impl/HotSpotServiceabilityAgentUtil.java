package tr.com.serkanozal.jemstone.sa.impl;

import sun.jvm.hotspot.HotSpotAgent;
import sun.jvm.hotspot.runtime.VM;

/**
 * Hotspot Serviceability Agent utility. It was designed for doing some utility stuff.
 *
 * @author Serkan Ozal
 */
public final class HotSpotServiceabilityAgentUtil {

    public static final String HOTSPOT_AGENT_CLASSNAME = "sun.jvm.hotspot.HotSpotAgent";
    public static final String VM_CLASSNAME = "sun.jvm.hotspot.runtime.VM";
    public static final String UNIVERSE_CLASSNAME = "sun.jvm.hotspot.memory.Universe";

    private HotSpotServiceabilityAgentUtil() {

    }

    public static VM getVMInstance() {
        return VM.getVM();
    }

    public static HotSpotAgent getHotSpotAgentInstance() {
        return new HotSpotAgent();
    }

    public static Class<?> getVmClass() throws ClassNotFoundException {
        return Class.forName(VM_CLASSNAME);
    }

    public static Class<?> getUniverseClass() throws ClassNotFoundException {
        return Class.forName(UNIVERSE_CLASSNAME);
    }

}
