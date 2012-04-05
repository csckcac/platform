package org.wso2.carbon.hosting.mgt.dto;

public class ZoneResourcePlan {

    private boolean available;

    private String zone;

    private String memory;
    
    private String swap;
    
    private String cpuShares;
    
    private String cpuSetCpus;
    
    private String storage;
    
    private String netMask;
    
    private String netGateway;



    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
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

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getCpuSetCpus() {
        return cpuSetCpus;
    }

    public void setCpuSetCpus(String cpuSetCpus) {
        this.cpuSetCpus = cpuSetCpus;
    }

    public String getNetMask() {
        return netMask;
    }

    public void setNetMask(String netMask) {
        this.netMask = netMask;
    }

    public String getNetGateway() {
        return netGateway;
    }

    public void setNetGateway(String netGateway) {
        this.netGateway = netGateway;
    }
}
