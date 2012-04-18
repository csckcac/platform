/**
 * 
 */
package org.wso2.carbon.hosting.wnagent.dto;

import java.util.List;
import java.util.Map;


/**
 * @author wso2
 *
 */

// TODO this is to be removed. Replace with the stubs
public class WorkerNodeConfig {

	private boolean available;
    
    private String name;

    private String containerRoot;

    private String ip;
    
    // remove this
    private Map<String, String> templateMap;

    private String zone;
  
    private List<BridgeConfig> bridges;    

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getContainerRoot() {
        return containerRoot;
    }

    public void setContainerRoot(String containerRoot) {
        this.containerRoot = containerRoot;
    }

	public List<BridgeConfig> getBridges() {
    	return bridges;
    }

	public void setBridges(List<BridgeConfig> bridges) {
    	this.bridges = bridges;
    }

	public Map<String, String> getTemplateMap() {
    	return templateMap;
    }

	public void setTemplateMap(Map<String, String> templateMap) {
    	this.templateMap = templateMap;
    }

	public String getIp() {
    	return ip;
    }

	public void setIp(String ip) {
    	this.ip = ip;
    }   	
}
