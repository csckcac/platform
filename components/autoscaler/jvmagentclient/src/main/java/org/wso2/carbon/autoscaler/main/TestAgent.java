package org.wso2.carbon.autoscaler.main;

import org.wso2.carbon.autoscaler.service.agent.clients.AgentServiceClient;


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
