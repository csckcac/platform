package org.wso2.carbon.identity.entitlement.proxy;

import java.util.Map;

public class PDPConfig {

	private String messageFormat;
	private Map<String, String[]> appToPDPMap;
	private String appId;
	private String userName;
	private String password;	

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getMessageFormat() {
		return messageFormat;
	}

	public void setMessageFormat(String messageFormat) {
		this.messageFormat = messageFormat;
	}

	public Map<String, String[]> getAppToPDPMap() {
		return appToPDPMap;
	}

	public void setAppToPDPMap(Map<String, String[]> appToPDPMap) {
		this.appToPDPMap = appToPDPMap;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

}
