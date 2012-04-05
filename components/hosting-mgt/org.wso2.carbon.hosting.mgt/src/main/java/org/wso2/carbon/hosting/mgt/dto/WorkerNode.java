package org.wso2.carbon.hosting.mgt.dto;

import java.util.ArrayList;

public class WorkerNode {

	private boolean available;
    
    private String name;

    private String containerRoot;

    private String endPoint;
    
    private String zone;

    private Bridge bridges[];

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

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
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
