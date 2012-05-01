package org.wso2.carbon.hosting.wnagent.beans;

public class BridgeConfig {


	private boolean available;
    
    private String name;

    private String workerNode;

    private int maximumCountIps;

    private int currentCountIps;
    
    private String ip;
    
    private String netMask;
    
    private String gateway;


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


    public String getWorkerNode() {
        return workerNode;
    }

    public void setWorkerNode(String workerNode) {
        this.workerNode = workerNode;
    }

    public int getMaximumCountIps() {
        return maximumCountIps;
    }

    public void setMaximumCountIps(int maximumCountIps) {
        this.maximumCountIps = maximumCountIps;
    }

    public int getCurrentCountIps() {
        return currentCountIps;
    }

    public void setCurrentCountIps(int currentCountIps) {
        this.currentCountIps = currentCountIps;
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

	public String getGateway() {
    	return gateway;
    }

	public void setGateway(String gateway) {
    	this.gateway = gateway;
    }
    
}
