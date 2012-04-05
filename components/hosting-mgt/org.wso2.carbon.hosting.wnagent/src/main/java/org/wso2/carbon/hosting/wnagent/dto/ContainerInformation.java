package org.wso2.carbon.hosting.wnagent.dto;

public class ContainerInformation {

	private String jailKeysFile;

	private String template;

	private String zone;

	private String containerRoot;

	private String netGateway;

	private String ip;

	private String netMask;

	private String bridge;

	private String memory;

	private String swap;

	private String cpuShares;

	private String cpuSetShares;

	private String storage;

	public String getJailKeysFile() {
		return jailKeysFile;
	}

	public void setJailKeysFile(String jailKeysFile) {
		this.jailKeysFile = jailKeysFile;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getCpuShares() {
		return cpuShares;
	}

	public void setCpuShares(String cpuShares) {
		this.cpuShares = cpuShares;
	}

	public String getNetGateway() {
		return netGateway;
	}

	public void setNetGateway(String netGateway) {
		this.netGateway = netGateway;
	}

	public String getContainerRoot() {
		return containerRoot;
	}

	public void setContainerRoot(String containerRoot) {
		this.containerRoot = containerRoot;
	}

	public String getCpuSetShares() {
		return cpuSetShares;
	}

	public void setCpuSetShares(String cpuSetShares) {
		this.cpuSetShares = cpuSetShares;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getNetMask() {
		return netMask;
	}

	public void setNetMask(String netMask) {
		this.netMask = netMask;
	}

	public String getBridge() {
		return bridge;
	}

	public void setBridge(String bridge) {
		this.bridge = bridge;
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

	public String getStorage() {
		return storage;
	}

	public void setStorage(String storage) {
		this.storage = storage;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}
}
