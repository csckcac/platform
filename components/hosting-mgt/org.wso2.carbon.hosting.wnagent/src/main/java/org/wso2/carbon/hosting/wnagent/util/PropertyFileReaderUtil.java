/**
 * 
 */
package org.wso2.carbon.hosting.wnagent.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.wso2.carbon.hosting.wnagent.beans.AgentConfig;
import org.wso2.carbon.hosting.wnagent.beans.PlanConfig;
import org.wso2.carbon.lb.common.dto.HostMachine;

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
		Map<String, String> templateMap = agentConfig.getDomainToTemplateMap();
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
	        long memory = Long.parseLong(planConfig.getMemory().split("M")[0]);
	        
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
	
	public static HostMachine readHostMachineConfig(){
		return agentConfig.getHostMachine();
	}
	
	public static Map<String, String> readDomainToTemplateMap(){
		return agentConfig.getDomainToTemplateMap();
	}
	
	public static List<String> readDomainList() {
		return agentConfig.getDomainsList();
	}
	
	public static String readHostMachineIp() {
		return readHostMachineConfig().getIp();
	}

	public static String readDefaultContainerUser() {
	    return agentConfig.getDefaultContainerUser();
    }

	public static String readAgentServicePort() {
		return agentConfig.getAgentServicePort();
	}
}
