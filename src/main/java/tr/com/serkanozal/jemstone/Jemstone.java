package tr.com.serkanozal.jemstone;

import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentManager;
import tr.com.serkanozal.jemstone.sa.impl.HotSpotServiceabilityAgentManagerImpl;

public class Jemstone {

	private static HotSpotServiceabilityAgentManager hotSpotServiceabilityAgentManager = 
	        HotSpotServiceabilityAgentManagerImpl.getInstance();
	
	public static HotSpotServiceabilityAgentManager getHotSpotServiceabilityAgentManager() {
        return hotSpotServiceabilityAgentManager;
    }
	
	public static void setHotSpotServiceabilityAgentManager(
            HotSpotServiceabilityAgentManager hotSpotServiceabilityAgentManager) {
        Jemstone.hotSpotServiceabilityAgentManager = hotSpotServiceabilityAgentManager;
    }
    
    public static void main(String[] args) {
        HotSpotServiceabilityAgentManager hotSpotServiceabilityAgentManager = getHotSpotServiceabilityAgentManager();
		System.out.println(hotSpotServiceabilityAgentManager.details());
		System.out.println(hotSpotServiceabilityAgentManager.getCompressedReferences());
	}
	
}
