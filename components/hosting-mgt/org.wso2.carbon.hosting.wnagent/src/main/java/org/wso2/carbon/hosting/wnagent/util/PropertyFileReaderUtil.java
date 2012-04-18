/**
 * 
 */
package org.wso2.carbon.hosting.wnagent.util;

import java.util.Collection;
import java.util.Map;
import org.wso2.carbon.hosting.wnagent.dto.AgentConfig;
import org.wso2.carbon.hosting.wnagent.dto.PlanConfig;

/**
 * 
 * @author wso2
 * 
 */
public class PropertyFileReaderUtil {

	private static AgentConfig agentConfig;
	
	public static void readAgentConfig() throws Exception {
		agentConfig = AgentConfig.getAgentConfigInstance();
	}
	
	public static String readAgentMgtServiceEpr() {
	    return agentConfig.getServiceHostUrl();
    }

	public static String readTemplateForDomain(String domainName) {
		Map<String, String> templateMap = agentConfig.getWorkerNodeConf().getTemplateMap();
		return templateMap.get(domainName);	    
    }

	public static PlanConfig readPlanForType(String type) {
		Map<String, PlanConfig> planMap = agentConfig.getResourcePlanConf().getResourcePlanMap();
	    return planMap.get(type);
    }

	public static long getMaxMemoryContainer() {
		
		Map<String, PlanConfig> planMap  = agentConfig.getResourcePlanConf().getResourcePlanMap();
		Collection<PlanConfig> collection = planMap.values();
		long mem = 0;
		for (PlanConfig planConfig : collection) {
	        long memory = Long.parseLong(planConfig.getMemory());
	        
	        if(mem==0){
	        	mem = memory;
	        }
	        
	        if(mem < memory){
	        	mem = memory;
	        }	        
        }
		
	    return mem;
    }
	
	public static String readDefaultPassword() {
		return agentConfig.getDefaultPassword();
	}
}
