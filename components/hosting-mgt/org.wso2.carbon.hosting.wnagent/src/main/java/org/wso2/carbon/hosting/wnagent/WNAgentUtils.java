package org.wso2.carbon.hosting.wnagent;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.File;

public class WNAgentUtils {
	// private static final String wso2wsasHome =
	// System.getProperty("wso2wsas.home");
	private static final String wso2wsasHome = "/opt/as/wso2as-4.1.2";

	public static WSRegistryServiceClient initializeRegistry() throws Exception {
		WSRegistryServiceClient registry;

		/*
		 * ConfigurationContext configContext =
		 * MessageContext.getCurrentMessageContext().getConfigurationContext();
		 * String adminUser = (String)
		 * MessageContext.getCurrentMessageContext().getServiceContext().
		 * getAxisService().getParameterValue(ResourcesConstants.ADMIN_USER);
		 * String adminUserPassword = (String)
		 * MessageContext.getCurrentMessageContext().getServiceContext().
		 * getAxisService().getParameterValue(ResourcesConstants.ADMIN_USER_PASSWORD
		 * );
		 */
		// String serverURL = (String)
		// MessageContext.getCurrentMessageContext().getServiceContext().
		// getAxisService().getParameterValue(ResourcesConstants.SERVER_URL);

		// String backendURL = (String)
		// MessageContext.getCurrentMessageContext().getServiceContext().
		// getAxisService().getParameterValue(ResourcesConstants.SERVER_URL);
		// String policyPath = (String)
		// MessageContext.getCurrentMessageContext().getServiceContext().
		// getAxisService().getParameterValue(ResourcesConstants.POLICY_PATH);

		System.setProperty("javax.net.ssl.trustStore", wso2wsasHome + File.separator +
		                                               "repository" + File.separator + "resources" +
		                                               File.separator + "security" +
		                                               File.separator + "wso2carbon.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
		System.setProperty("javax.net.ssl.trustStoreType", "JKS");

		String serverURL = "http://localhost:9763/services/";
		String backendURL = "https://localhost:9443/services/";
		String axis2Repo = wso2wsasHome + "/repository/deployment/client";
		String axis2Conf = wso2wsasHome + "/repository/conf/axis2_client.xml";
		ConfigurationContext configContext =
		                                     ConfigurationContextFactory.createConfigurationContextFromFileSystem(axis2Repo,
		                                                                                                          axis2Conf);
		registry =
		           new WSRegistryServiceClient(backendURL, "admin", "admin", serverURL,
		                                       configContext);

		return registry;
	}

}
