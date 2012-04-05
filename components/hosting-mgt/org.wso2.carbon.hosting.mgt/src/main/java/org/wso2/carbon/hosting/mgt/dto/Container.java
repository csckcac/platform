package org.wso2.carbon.hosting.mgt.dto;

public class Container {

    private String containerName;

    private String tenant;

	private String jailKeysFile;
	
	private String template;
    
    private String ip;

    private String bridge;


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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getBridge() {
        return bridge;
    }

    public void setBridge(String bridge) {
        this.bridge = bridge;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }
    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
}
