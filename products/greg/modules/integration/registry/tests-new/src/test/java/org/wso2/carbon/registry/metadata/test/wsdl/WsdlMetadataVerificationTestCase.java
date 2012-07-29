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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

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
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

public class WsdlMetadataVerificationTestCase {

	private ManageEnvironment environment;
	private UserInfo userInfo;

	private Registry governanceRegistry;
	private Wsdl wsdl;
	private ResourceAdminServiceClient resourceAdminServiceClient;

	@BeforeClass
	public void initialize() throws RemoteException,
			LoginAuthenticationExceptionException,
			org.wso2.carbon.registry.api.RegistryException {

		int userId = 1;
		RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
		WSRegistryServiceClient wsRegistryServiceClient = registryProviderUtil
				.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
		governanceRegistry = registryProviderUtil.getGovernanceRegistry(
				wsRegistryServiceClient, userId);

	}

	/**
	 * Metadata verification
	 */
	@Test(groups = "wso2.greg", description = "add wsdl")
	public void testAddWSDL() throws RemoteException,
			ResourceAdminServiceExceptionException, GovernanceException,
			MalformedURLException {

		WsdlManager wsdlManager = new WsdlManager(governanceRegistry);

		wsdl = wsdlManager
				.newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/clarity-tests/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/wsdl/AmazonWebServices.wsdl");

		wsdl.addAttribute("version", "1.0.0");
		wsdl.addAttribute("author", "Aparna");
		wsdl.addAttribute("description",
				"added valid wsdl with corrupted Schema via url");
		wsdlManager.addWsdl(wsdl);

		assertNotNull(wsdl);
		assertTrue(wsdl.getAttribute("author").contentEquals("Aparna"));

	}

	@Test(groups = "wso2.greg", description = "metadata verification")
	public void testVerifyMetadata() throws RemoteException,
			LoginAuthenticationExceptionException, GovernanceException,
			ResourceAdminServiceExceptionException {
		EnvironmentBuilder builder = new EnvironmentBuilder().greg(1); // tenant
																		// ID

		environment = builder.build();
		userInfo = UserListCsvReader.getUserInfo(1);

		resourceAdminServiceClient = new ResourceAdminServiceClient(environment
				.getGreg().getProductVariables().getBackendUrl(),
				userInfo.getUserName(), userInfo.getPassword());

		assertTrue(resourceAdminServiceClient
				.getMetadata("/_system/governance" + wsdl.getPath())
				.getMediaType().contentEquals("application/wsdl+xml")); // mediatype
																		// verified
		assertNull(resourceAdminServiceClient.getMetadata(
				"/_system/governance" + wsdl.getPath()).getPermalink()); // verified
																			// permalink:
																			// returning
																			// a
																			// null

		assertNull(resourceAdminServiceClient.getMetadata(
				"/_system/governance" + wsdl.getPath()).getDescription()); // description
																			// verified
		assertTrue(resourceAdminServiceClient
				.getMetadata("/_system/governance" + wsdl.getPath())
				.getContentPath()
				.contentEquals(
						"/_system/governance/trunk/wsdls/com/amazon/soap/AmazonWebServices.wsdl"));// contentPath
																									// verfied

	}

	@Test(groups = "wso2.greg", description = "metadata verisons verification")
	public void testVersionVerification() throws RegistryException,
			RemoteException, ResourceAdminServiceExceptionException {

		// created version twice
		resourceAdminServiceClient.createVersion("/_system/governance"
				+ wsdl.getPath());

		resourceAdminServiceClient.createVersion("/_system/governance"
				+ wsdl.getPath());

		/* for verifying versions: get the active version and verified */
		VersionPath[] versionPath = resourceAdminServiceClient
				.getVersionPaths("/_system/governance" + wsdl.getPath());
		boolean getActiveVersion = false;

		for (VersionPath pathOfResources : versionPath) {

			if (pathOfResources.isActiveResourcePathSpecified()) {
				getActiveVersion = true;
			}

		}

		assertEquals(versionPath.length, 2,
				"the number of versions created should be 2");
		assertTrue(getActiveVersion,
				"verified the version created and it is active");

		/*
		 * for verifying versions:retrieving an older version, assert it, and
		 * after deleting it and asserting the deletion
		 */

		boolean getInactiveVersion = false;
		String tempPath = "/_system/governance" + wsdl.getPath();
		

		for (VersionPath pathOfResources : versionPath) {
			
			if (!pathOfResources.isActiveResourcePathSpecified()) {

				getInactiveVersion = true;

				tempPath = pathOfResources.getCompleteVersionPath();
				resourceAdminServiceClient.deleteResource(tempPath);
				break;
			}

		}

		/*
		 * assertTrue(getInactiveVersion,
		 * "verified the version created and it is active");
		 * assertNull(resourceAdminServiceClient.getResource(tempPath));
		 */

		/* restoring an older version */

		boolean getRestoredVersion = false;
		int counter2 = 0;
		for (VersionPath pathOfResources : versionPath) {
			counter2 += 1;
			if (!pathOfResources.isActiveResourcePathSpecified()) {
				resourceAdminServiceClient.restoreVersion(pathOfResources
						.getCompleteVersionPath());

				getRestoredVersion = pathOfResources
						.isActiveResourcePathSpecified();
			}

		}

		// assertTrue(getRestoredVersion, "verified version restore");

	}
}