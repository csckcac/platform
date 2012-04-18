/**
 * 
 */
package org.wso2.carbon.hosting.wnagent.dto;

/**
 * @author wso2
 *
 */
public class PlanConfig {

	private String name;
	
	private String memory;
	
	private String swap;
	
	private String cpuShares;
	
	private String cpuSets;
	
	private String storage;

	public String getName() {
    	return name;
    }

	public void setName(String name) {
    	this.name = name;
    }

	public String getMemory() {
    	return memory;
    }

	public void setMemory(String memory) {
    	this.memory = memory;
    }

	public String getSwap() {
    	return swap;
    }

	public void setSwap(String swap) {
    	this.swap = swap;
    }

	public String getCpuShares() {
    	return cpuShares;
    }

	public void setCpuShares(String cpuShares) {
    	this.cpuShares = cpuShares;
    }

	public String getCpuSets() {
    	return cpuSets;
    }

	public void setCpuSets(String cpuSets) {
    	this.cpuSets = cpuSets;
    }

	public String getStorage() {
    	return storage;
    }

	public void setStorage(String storage) {
    	this.storage = storage;
    }
	
}
