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

public class SameWsdlAgainAdditionTestCase {

	private Registry governance;
	private Wsdl wsdl, wsdlCopy;
	private WsdlManager wsdlManager;

	@BeforeClass
	public void initialize() throws RemoteException,
			LoginAuthenticationExceptionException,
			org.wso2.carbon.registry.api.RegistryException {
		int userId = 1;
		RegistryProviderUtil provider = new RegistryProviderUtil();
		WSRegistryServiceClient registry = provider.getWSRegistry(userId,
				ProductConstant.GREG_SERVER_NAME);
		governance = provider.getGovernanceRegistry(registry, userId);

	}

	/**
	 * WSDL addition from URL
	 */
	@Test(groups = "wso2.greg", description = "Add WSDL via URL: Automated.wsdl")
	public void testAddWSDL() throws RemoteException,
			ResourceAdminServiceExceptionException, GovernanceException {

		wsdlManager = new WsdlManager(governance);
		wsdl = wsdlManager
				.newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/"
						+ "clarity-tests/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/"
						+ "GREG/wsdl/Automated.wsdl");
		wsdl.addAttribute("version", "1.0.0");
		wsdl.addAttribute("author", "kana");
		wsdl.addAttribute("description", "added wsdl via URL");
		wsdlManager.addWsdl(wsdl);
		/** metadata verification of added wsdl */

		assertTrue(wsdl.getAttribute("description").contentEquals(
				"added wsdl via URL")); // WSDL addition from URL: verification
		assertTrue(wsdl.getAttribute("author").contentEquals("kana"));
		assertNotNull(wsdlManager.getWsdl(wsdl.getId()));
	}

	/** adding the same wsdl again
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     * @throws org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException*/
	@Test(groups = "wso2.greg", description = "Add same WSDL via URL", dependsOnMethods = "testAddWSDL")
	public void testAddAlreadyAddedWSDL() throws RemoteException,
			ResourceAdminServiceExceptionException, GovernanceException {

		wsdlManager = new WsdlManager(governance);
		wsdlCopy = wsdlManager
				.newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/"
						+ "clarity-tests/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/G"
						+ "REG/wsdl/Automated.wsdl");

		wsdlCopy.addAttribute("description", "added wsdl via URL");
		wsdlManager.addWsdl(wsdlCopy);

		assertFalse(wsdl.getId().isEmpty());
		assertNotNull(wsdl);
		assertTrue(wsdlCopy.getAttribute("description").contentEquals(
				"added wsdl via URL")); // Second WSDL addition from URL:
										// verification

	}

	/** compare both WSDLs additions from URL: verification
     * @throws java.rmi.RemoteException
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException*/
	@Test(groups = "wso2.greg", description = "Add WSDL via URL", dependsOnMethods = "testAddAlreadyAddedWSDL", enabled = true)
	public void testCompareWSDLs() throws RemoteException,
			ResourceAdminServiceExceptionException, GovernanceException {
		assertTrue(wsdlCopy.getId().matches(wsdl.getId()));

	}

}
