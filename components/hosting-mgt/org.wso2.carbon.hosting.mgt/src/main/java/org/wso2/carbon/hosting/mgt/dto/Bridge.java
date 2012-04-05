package org.wso2.carbon.hosting.mgt.dto;

import java.util.ArrayList;

public class Bridge {

	private boolean available;
    
    private String name;

    private String workerNode;

    private int maximumCountIps;

    private int currentCountIps;



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
}