package tr.com.serkanozal.jemstone;

import tr.com.serkanozal.jemstone.sa.HotSpotServiceabilityAgentManager;

public class Jemstone {

	public static void main(String[] args) {
		System.out.println(HotSpotServiceabilityAgentManager.details());
		System.out.println(HotSpotServiceabilityAgentManager.getCompressedReferences());
	}
	
}
