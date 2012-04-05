package org.wso2.carbon.jvm.autoscaler.agent.client;

import org.wso2.carbon.jvm.autoscaler.agent.client.AgentServiceClient;


public class TestAgent {
	
	private final static String AMS_BACKEND_URL ="http://localhost:9788/services/";

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		AgentServiceClient asc = new AgentServiceClient(AMS_BACKEND_URL);
		
		System.out.println(asc.registerInAgentManagementService());
		
		System.out.println(asc.unregisterInAgentManagementService());
		
		System.out.println(asc.unregisterInAgentManagementService());
		
		System.out.println(asc.registerInAgentManagementService());
		
		System.out.println(asc.unregisterInAgentManagementService());
		
		
	}

}
