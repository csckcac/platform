package org.wso2.carbon.lb.common.dto;

public class WorkerNode {

	private boolean available;

    private String containerRoot;

    private String ip;
    
    private String zone;

    private Bridge bridges[];

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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

    public Bridge[] getBridges() {
        return bridges;
    }

    public void setBridges(Bridge[] bridges) {
        this.bridges = bridges;
    }
}
