package org.wso2.identity.entitlement.pdp.proxy.soap;

import java.util.HashMap;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;

public class Authenticator {

	private String userName;
	private String password;
	private String serverUrl;
	private String cookie;

	public Authenticator(String userName, String password, String serverUrl) throws Exception {
		this.userName = userName;
		this.password = password;
		this.serverUrl = serverUrl;

		if (!authenticate()) {
			throw new Exception("Authentication Failed");
		}
	}

	private boolean authenticate() throws Exception {
		ConfigurationContext configurationContext;
		configurationContext = ConfigurationContextFactory.createDefaultConfigurationContext();
		HashMap<String, TransportOutDescription> transportsOut = configurationContext
				.getAxisConfiguration().getTransportsOut();
		for (TransportOutDescription transportOutDescription : transportsOut.values()) {
			transportOutDescription.getSender().init(configurationContext, transportOutDescription);
		}
		
		AuthenticationAdminStub authAdmin = new AuthenticationAdminStub(configurationContext,
				serverUrl);
		boolean isAuthenticated = authAdmin.login(userName, password, "SAMPLE_PEP");
		cookie = (String) authAdmin._getServiceClient().getServiceContext()
				.getProperty(HTTPConstants.COOKIE_STRING);
		authAdmin._getServiceClient().cleanupTransport();
		return isAuthenticated;
	}

	public String getCookie(boolean isExpired) throws Exception {
		if (isExpired) {
			authenticate();
		}
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

}
