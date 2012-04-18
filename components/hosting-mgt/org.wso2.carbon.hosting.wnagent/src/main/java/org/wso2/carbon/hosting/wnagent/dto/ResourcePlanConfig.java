/**
 * 
 */
package org.wso2.carbon.hosting.wnagent.dto;

import java.util.Map;

/**
 * @author wso2
 *
 */
public class ResourcePlanConfig {

	private Map<String, PlanConfig> resourcePlanMap;

	public Map<String, PlanConfig> getResourcePlanMap() {
    	return resourcePlanMap;
    }

	public void setResourcePlanMap(Map<String, PlanConfig> resourcePlanMap) {
    	this.resourcePlanMap = resourcePlanMap;
    }	
}
