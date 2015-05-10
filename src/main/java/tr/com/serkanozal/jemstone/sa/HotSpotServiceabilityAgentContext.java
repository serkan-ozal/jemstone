package tr.com.serkanozal.jemstone.sa;

import sun.jvm.hotspot.HotSpotAgent;
import sun.jvm.hotspot.runtime.VM;

/**
 * Context to hold required some important HotSpot SA instances such as
 * {@link HotSpotAgent}, {@link VM}. etc ...
 * 
 * @author Serkan Ozal
 */
public class HotSpotServiceabilityAgentContext {

    private final HotSpotAgent hotSpotAgent;
    private final VM vm;

    public HotSpotServiceabilityAgentContext(HotSpotAgent hotSpotAgent, VM vm) {
        this.hotSpotAgent = hotSpotAgent;
        this.vm = vm;
    }

    /**
     * Gets the {@link HotSpotAgent} instance.
     * 
     * @return the {@link HotSpotAgent} instance
     */
    public HotSpotAgent getHotSpotAgent() {
        return hotSpotAgent;
    }

    /**
     * Gets the {@link VM} instance.
     * 
     * @return the {@link VM} instance.
     */
    public VM getVM() {
        return vm;
    }

}
