package org.wso2.carbon.lb.common.dto;

public class HostMachine {

	private boolean available;
    
    private String epr;

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

        Bridge bridgesCopy[]= new Bridge[bridges.length];
        System.arraycopy(bridges, 0, bridgesCopy, 0, bridges.length);
        return bridgesCopy;
    }

    public void setBridges(Bridge[] bridges) {
        if(bridges != null){
            this.bridges = new Bridge[0];
            this.bridges = bridges;
            this.bridges= new Bridge[bridges.length];
            System.arraycopy(bridges, 0, this.bridges, 0, bridges.length);
        }
    }

    public String getEpr() {
        return epr;
    }

    public void setEpr(String epr) {
        this.epr = epr;
    }

}