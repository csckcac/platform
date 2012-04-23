package org.wso2.carbon.lb.common.dto;

/**
 * 
 * 
 */
public class ContainerInformation {
    private String containerId;

	private String containerRoot;

	private String netGateway;

	private String ip;

	private String netMask;

	private String bridge;

	private String type;

	private String containerKeysFile;


    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getContainerRoot() {
        return containerRoot;
    }

    public void setContainerRoot(String containerRoot) {
        this.containerRoot = containerRoot;
    }

    public String getNetGateway() {
        return netGateway;
    }

    public void setNetGateway(String netGateway) {
        this.netGateway = netGateway;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContainerKeysFile() {
        return containerKeysFile;
    }

    public void setContainerKeysFile(String containerKeysFile) {
        this.containerKeysFile = containerKeysFile;
    }
}
