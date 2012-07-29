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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.authenticators.AuthenticatorClient;
import org.wso2.carbon.automation.api.clients.registry.PropertiesAdminServiceClient;
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
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.properties.stub.beans.xsd.RetentionBean;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

public class WsdlRetentionVerificationTestCase {
	private ManageEnvironment environment;
	private int userId1 = 1;
	private int userId2 = 2;
	private UserInfo userInfo;
	private Wsdl wsdl;
	private Registry governanceRegistry;;
	private EnvironmentBuilder builder;
	private PropertiesAdminServiceClient propertiesAdminServiceClient;
	private WsdlManager wsdlManager;
	private RegistryProviderUtil registryProviderUtil;
	private WsdlManager wsdlManager2;
	private String path;

	@BeforeClass()
	public void initialize() throws LoginAuthenticationExceptionException,
			RemoteException, RegistryException {

		builder = new EnvironmentBuilder().greg(userId1);
		environment = builder.build();

		registryProviderUtil = new RegistryProviderUtil();
		WSRegistryServiceClient wsRegistryServiceClient = registryProviderUtil
				.getWSRegistry(userId1, ProductConstant.GREG_SERVER_NAME);
		governanceRegistry = registryProviderUtil.getGovernanceRegistry(
				wsRegistryServiceClient, userId1);
	}

	@Test(groups = "wso2.greg", description = "wsdl addition for association Verification")
	public void testAddResourcesToVerifyAssociation() throws RemoteException,
			MalformedURLException, ResourceAdminServiceExceptionException,
			GovernanceException {

		wsdlManager = new WsdlManager(governanceRegistry);

		wsdl = wsdlManager
				.newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/"
						+ "clarity-tests/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/"
						+ "GREG/wsdl/echo.wsdl");

		wsdl.addAttribute("version", "1.0.0");
		wsdl.addAttribute("author", "Aparna");
		wsdl.addAttribute("description", "for retention verification");
		wsdlManager.addWsdl(wsdl);

		path = "/_system/governance" + wsdl.getPath();
	}

	@Test(groups = "wso2.greg", description = "Association Verification", dependsOnMethods = "testAddResourcesToVerifyAssociation")
	public void testFirstUserRetention() throws GovernanceException,
			RemoteException, PropertiesAdminServiceRegistryExceptionException,
			LogoutAuthenticationExceptionException {

		userInfo = UserListCsvReader.getUserInfo(userId1);
		propertiesAdminServiceClient = new PropertiesAdminServiceClient(
				environment.getGreg().getProductVariables().getBackendUrl(),
				userInfo.getUserName(), userInfo.getPassword());

		propertiesAdminServiceClient.setRetentionProperties(
				"/_system/governance" + wsdl.getPath(), "write", "07/21/2012",
				"09/01/2012");

		RetentionBean retentionBean = propertiesAdminServiceClient
				.getRetentionProperties("/_system/governance" + wsdl.getPath());

		assertTrue(retentionBean.getWriteLocked());
		assertTrue(retentionBean.getToDate().contentEquals("09/01/2012"));
		assertFalse(retentionBean.getDeleteLocked());

		/* logout */
		new AuthenticatorClient(environment.getGreg().getBackEndUrl()).logOut();
	}

	@Test(groups = "wso2.greg", description = "Retention verificaiton: second user", dependsOnMethods = "testFirstUserRetention")
	public void testSecondUserRetention() throws RemoteException,
			MalformedURLException, ResourceAdminServiceExceptionException,
			AddAssociationRegistryExceptionException,
			PropertiesAdminServiceRegistryExceptionException,
			LoginAuthenticationExceptionException, RegistryException {

		builder = new EnvironmentBuilder().greg(userId2);
		environment = builder.build();
		userInfo = UserListCsvReader.getUserInfo(userId2);

		propertiesAdminServiceClient = new PropertiesAdminServiceClient(
				environment.getGreg().getProductVariables().getBackendUrl(),
				userInfo.getUserName(), userInfo.getPassword());

		WSRegistryServiceClient wsRegistryServiceClient = registryProviderUtil
				.getWSRegistry(userId2, ProductConstant.GREG_SERVER_NAME);
		governanceRegistry = registryProviderUtil.getGovernanceRegistry(
				wsRegistryServiceClient, userId2);

		wsdlManager2 = new WsdlManager(governanceRegistry);

		RetentionBean retentionBean = propertiesAdminServiceClient
				.getRetentionProperties(path);

		String path1 = retentionBean.getFromDate();
		path1.length();
		assertTrue(retentionBean.getWriteLocked());
		assertFalse(retentionBean.getDeleteLocked());

		assertNotNull(wsdlManager2);

		Wsdl newWsdl = wsdlManager2.getWsdl(path);

		//assertNotNull(newWsdl);
		// wsdlManager2.getWsdl(path).addAttribute("write access", "enabled");

	}

}
