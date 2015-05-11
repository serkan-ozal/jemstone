package tr.com.serkanozal.jemstone;

import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentMan;
import tr.com.serkanozal.jemstone.sa.impl.HotSpotServiceabilityAgentManagerImpl;

public class Jemstone {

	private static HotSpotServiceabilityAgentMan hotSpotServiceabilityAgentManager = 
	        HotSpotServiceabilityAgentManagerImpl.getInstance();
	
	public static HotSpotServiceabilityAgentMan getHotSpotServiceabilityAgentManager() {
        return hotSpotServiceabilityAgentManager;
    }
	
	public static void setHotSpotServiceabilityAgentManager(
            HotSpotServiceabilityAgentMan hotSpotServiceabilityAgentManager) {
        Jemstone.hotSpotServiceabilityAgentManager = hotSpotServiceabilityAgentManager;
    }
    
    public static void main(String[] args) {
        HotSpotServiceabilityAgentMan hotSpotServiceabilityAgentManager = getHotSpotServiceabilityAgentManager();
		System.out.println(hotSpotServiceabilityAgentManager.details());
		System.out.println(hotSpotServiceabilityAgentManager.getCompressedReferences());
	}
	
}
