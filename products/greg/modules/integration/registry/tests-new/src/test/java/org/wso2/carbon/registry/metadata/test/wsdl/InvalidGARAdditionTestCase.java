/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.carbon.registry.metadata.test.wsdl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.activation.DataHandler;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

public class InvalidGARAdditionTestCase {
	private Registry governanceRegistry;
	private Wsdl wsdl;
	private ManageEnvironment environment;
	private UserInfo userInfo;

	@BeforeClass
	public void initialize() throws RemoteException,
			LoginAuthenticationExceptionException,
			org.wso2.carbon.registry.api.RegistryException {
		int userId = 1;
		EnvironmentBuilder builder = new EnvironmentBuilder().greg(1);
		environment = builder.build();
		userInfo = UserListCsvReader.getUserInfo(1);
		RegistryProviderUtil provider = new RegistryProviderUtil();
		WSRegistryServiceClient wsRegistry = provider.getWSRegistry(userId,
				ProductConstant.GREG_SERVER_NAME);
		governanceRegistry = provider.getGovernanceRegistry(wsRegistry, userId);

	}

	/**
	 * adding an invalid wsdl (a normal .xml)
	 * 
	 * @throws MalformedURLException
	 */
	@Test(groups = "wso2.greg", description = "Add Invalid GAR", enabled = false, expectedExceptions = GovernanceException.class)
	public void testAddInvalidGAR() throws RemoteException,
			ResourceAdminServiceExceptionException, GovernanceException,
			MalformedURLException {

		WsdlManager wsdlManager = new WsdlManager(governanceRegistry);

		wsdl = wsdlManager
				.newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
						+ "platform-integration/clarity-tests/"
						+ "org.wso2.carbon.automation.test.repo/src/main/resources/"
						+ "artifacts/GREG/gar/Invalidopps.gar");

		wsdl.addAttribute("version", "1.0.0");
		wsdl.addAttribute("author", "Aparna");
		wsdl.addAttribute("description", "added invalid wsdl using url");
		wsdlManager.addWsdl(wsdl);

	}

	// invalid .gar form file system
	@Test(groups = "wso2.greg", description = "invalid wsdl form file system using admin services")
	public void testAddInvalidGARFromFileSystem() throws MalformedURLException,
			RemoteException, ResourceAdminServiceExceptionException {

		ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(
				environment.getGreg().getProductVariables().getBackendUrl(),
				userInfo.getUserName(), userInfo.getPassword());

		// clarity automation api for registry
		String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
				+ "artifacts" + File.separator + "GREG" + File.separator
				+ "gar" + File.separator + "Invalidopps.gar"; // contains the
																// path to the
																// wsdl
																// directory

		DataHandler dh = new DataHandler(new URL("file:///" + resourcePath)); // java.net...

		resourceAdminServiceClient.addResource(
				"/_system/governance/trunk/wsdls/Invalidopps",
				"application/vnd.wso2.governance-archive",
				"invalid gar file addition", dh);

	}

}
