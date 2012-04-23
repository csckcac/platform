package org.wso2.carbon.lb.common.dto;


public class Bridge {

    private String bridgeIp;

	private boolean available;

    private String hostMachine;

    private int maximumCountIps;

    private int currentCountIps;

    private String netMask;

    private String netGateway;


    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }


    public String getHostMachine() {
        return hostMachine;
    }

    public void setHostMachine(String hostMachine) {
        this.hostMachine = hostMachine;
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

    public String getBridgeIp() {
        return bridgeIp;
    }

    public void setBridgeIp(String bridgeIp) {
        this.bridgeIp = bridgeIp;
    }

    public String getNetGateway() {
        return netGateway;
    }

    public void setNetGateway(String netGateway) {
        this.netGateway = netGateway;
    }

    public String getNetMask() {
        return netMask;
    }

    public void setNetMask(String netMask) {
        this.netMask = netMask;
    }
}