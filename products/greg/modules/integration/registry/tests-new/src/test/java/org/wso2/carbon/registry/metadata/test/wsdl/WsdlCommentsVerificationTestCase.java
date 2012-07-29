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

import org.apache.axis2.AxisFault;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
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
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.info.stub.beans.xsd.CommentBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

public class WsdlCommentsVerificationTestCase {

	private Registry governanceRegistry;
	private Wsdl wsdl;
	private ManageEnvironment environment;
	private int userId = 1;
	private UserInfo userInfo;
	private EnvironmentBuilder builder;
	private InfoServiceAdminClient infoServiceAdminclient;

	@BeforeClass
	public void initialize() throws RemoteException,
			LoginAuthenticationExceptionException,
			org.wso2.carbon.registry.api.RegistryException {

		builder = new EnvironmentBuilder().greg(userId);
		environment = builder.build();
		userInfo = UserListCsvReader.getUserInfo(userId);
		infoServiceAdminclient = new InfoServiceAdminClient(environment
				.getGreg().getProductVariables().getBackendUrl(),
				userInfo.getUserName(), userInfo.getPassword());

		RegistryProviderUtil provider = new RegistryProviderUtil();
		WSRegistryServiceClient wsRegistry = provider.getWSRegistry(userId,
				ProductConstant.GREG_SERVER_NAME);
		governanceRegistry = provider.getGovernanceRegistry(wsRegistry, userId);

	}

	/**
	 * comments verification
	 */
	@Test(groups = "wso2.greg", description = "comments verification")
	public void testAddWSDL() throws RemoteException,
			ResourceAdminServiceExceptionException, GovernanceException,
			MalformedURLException {

		WsdlManager wsdlManager = new WsdlManager(governanceRegistry);
		wsdl = wsdlManager
				.newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/"
						+ "clarity-tests/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/"
						+ "GREG/wsdl/echo.wsdl");

		wsdl.addAttribute("version", "1.0.0");
		wsdl.addAttribute("author", "Aparna");
		wsdl.addAttribute("description", "added large wsdl using url");
		wsdlManager.addWsdl(wsdl);

		assertFalse(wsdl.getId().isEmpty());
		assertNotNull(wsdl);
		assertTrue(wsdl.getAttribute("author").contentEquals("Aparna"));

	}

	@Test(groups = "wso2.greg", description = "Comments Verification", dependsOnMethods = "testAddWSDL")
	public void testFirstUserRetention() throws AxisFault, GovernanceException,
			RegistryException, RegistryExceptionException {

		infoServiceAdminclient.addComment(
				"This wsdl is added to verify the comments",
				"/_system/governance" + wsdl.getPath(), environment.getGreg()

				.getSessionCookie());

		CommentBean commentBean = infoServiceAdminclient.getComments(
				"/_system/governance" + wsdl.getPath(), environment.getGreg()
						.getSessionCookie());

		assertTrue(commentBean.getComments()[0].getContent().contentEquals(
				"This wsdl is added to verify the comments"));

	}

}
