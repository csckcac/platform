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

public class WsdlPropertiesVerificationTestCase {

	private Registry governanceRegistry;
	private Wsdl wsdl;
	private WsdlManager wsdlManager;

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
	 * verifying property
	 */
	@Test(groups = "wso2.greg", description = "verify properties of wsdl")
	public void testPropertiesWsdl() throws RemoteException,
			ResourceAdminServiceExceptionException, GovernanceException,
			MalformedURLException {

		wsdlManager = new WsdlManager(governanceRegistry);
		wsdl = wsdlManager
				.newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/clarity-tests/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/wsdl/AmazonWebServices.wsdl"); // 65KB

		wsdl.addAttribute("version", "1.0.0");
		wsdl.addAttribute("author", "Aparna");
		wsdl.addAttribute("description", "wsdl added for property checking");
		wsdlManager.addWsdl(wsdl);

		// Properties Verification
		assertFalse(wsdl.getId().isEmpty());
		assertNotNull(wsdl);

		assertTrue(wsdl.getAttribute("author").contentEquals("Aparna"));
		assertTrue(wsdl.getAttribute("version").contentEquals("1.0.0"));
		assertTrue(wsdl.getAttribute("description").contentEquals(
				"wsdl added for property checking"));

		wsdl.setAttribute("author", "Kanarupan");
		wsdl.setAttribute("description", "this is to verify property edition");

		wsdlManager.updateWsdl(wsdl);

		assertTrue(wsdl.getAttribute("author").contentEquals("Kanarupan"));
		assertTrue(wsdl.getAttribute("version").contentEquals("1.0.0"));
		assertTrue(wsdl.getAttribute("description").contentEquals(
				"this is to verify property edition"));

	}

	@Test(groups = "wso2.greg", description = "verify properties of wsdl", enabled = false)
	public void testPropertiesWsdlNegative() throws RemoteException,
			ResourceAdminServiceExceptionException, GovernanceException,
			MalformedURLException {

		wsdl.addAttribute("", "Kanarupan");
		wsdl.addAttribute("description", "");

		wsdlManager.updateWsdl(wsdl);
		assertTrue(wsdl.getAttribute("author").contentEquals("Kanarupan"));

	}

}
