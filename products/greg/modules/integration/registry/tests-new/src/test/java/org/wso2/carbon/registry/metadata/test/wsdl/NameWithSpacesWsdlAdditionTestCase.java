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
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

public class NameWithSpacesWsdlAdditionTestCase {
	private Registry governanceRegistry;
	private Wsdl wsdl;

	@BeforeClass
	public void initialize() throws RemoteException,
			LoginAuthenticationExceptionException,
			org.wso2.carbon.registry.api.RegistryException {
		int userId = 1;
		RegistryProviderUtil provider = new RegistryProviderUtil();
		WSRegistryServiceClient wsRegistry = provider.getWSRegistry(userId,
				ProductConstant.GREG_SERVER_NAME);
		governanceRegistry = provider.getGovernanceRegistry(wsRegistry, userId);

	}

	/**
	 * adding a wsdl name with spaces
	 */
	@Test(groups = "wso2.greg", description = "Add WSDL name with spaces", expectedExceptions = GovernanceException.class, enabled = false)
	public void testAddNameWithSpacesWSDL() throws RemoteException,
			ResourceAdminServiceExceptionException, GovernanceException,
			MalformedURLException {

		WsdlManager wsdlManager = new WsdlManager(governanceRegistry);
		wsdl = wsdlManager
				.newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/"
						+ "clarity-tests/org.wso2.carbon.automation.test.repo/src/main/resources/"
						+ "artifacts/GREG/wsdl/wsdl with spaces in the name.wsdl");

		wsdl.addAttribute("version", "1.0.0");
		wsdl.addAttribute("description", "added wsdl name with spaces via URL");
		wsdlManager.addWsdl(wsdl);

		assertFalse(wsdl.getId().isEmpty());
		assertNotNull(wsdl);
		assertTrue(wsdl.getAttribute("description").contentEquals(
				"added wsdl name with spaces via URL")); // name with spaces
															// WSDL addition
															// from URL:
															// verification

	}

	/** name with spaces WSDL addition from URL: verification */
	@Test(groups = "wso2.greg", description = "Add Name with spaces WSDL via URL", dependsOnMethods = "testAddNameWithSpacesWSDL", enabled = false)
	public void testAddedLargeWSDL() throws RemoteException,
			ResourceAdminServiceExceptionException, GovernanceException {
		assertTrue(wsdl.getAttribute("description").contentEquals(
				"added wsdl name with spaces via URL"));

	}

}
