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

import static org.testng.Assert.assertTrue;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.RelationAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationBean;
import org.wso2.carbon.registry.relations.stub.beans.xsd.DependenciesBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;

public class WsdlDependencyVerificationTestCase {

	private ManageEnvironment environment;
	private int userId = 1;
	private UserInfo userInfo;
	private ResourceAdminServiceClient resourceAdminServiceClient;

	@BeforeClass()
	public void initialize() throws LoginAuthenticationExceptionException,
			RemoteException {
		EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
		environment = builder.build();
	}

	@Test(groups = "wso2.greg", description = "wsdl addition for association Verification")
	public void testAddResourcesToVerifyAssociation() throws RemoteException,
			MalformedURLException, ResourceAdminServiceExceptionException {
		userInfo = UserListCsvReader.getUserInfo(userId);
		resourceAdminServiceClient = new ResourceAdminServiceClient(environment
				.getGreg().getProductVariables().getBackendUrl(),
				userInfo.getUserName(), userInfo.getPassword());

		resourceAdminServiceClient
				.addWSDL(
						"AmazonWebServices",
						"for the dependency verification",
						"https://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
								+ "platform-integration/clarity-tests/org.wso2.carbon.automation.test.repo/"
								+ "src/main/resources/artifacts/GREG/wsdl/AmazonWebServices.wsdl");

		resourceAdminServiceClient
				.addWSDL(
						"Automated",
						"for the dependency verification",
						"https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/"
								+ "clarity-tests/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts"
								+ "/GREG/wsdl/Automated.wsdl");

	}

	@Test(groups = "wso2.greg", description = "Association Verification")
	public void testVerifyAssociation() throws RemoteException,
			MalformedURLException, ResourceAdminServiceExceptionException,
			AddAssociationRegistryExceptionException {

		RelationAdminServiceClient relationAdminServiceClient = new RelationAdminServiceClient(
				environment.getGreg().getProductVariables().getBackendUrl(),
				userInfo.getUserName(), userInfo.getPassword());

		relationAdminServiceClient
				.addAssociation(
						"/_system/governance/trunk/wsdls/com/amazon/soap/AmazonWebServices.wsdl",
						"depends",
						"/_system/governance/trunk/wsdls/com/strikeiron/www/Automated.wsdl",
						"add");

		DependenciesBean dependenciesBean = relationAdminServiceClient
				.getDependencies("/_system/governance/trunk/wsdls/com/amazon/soap/AmazonWebServices.wsdl");

		AssociationBean[] associationBean = dependenciesBean
				.getAssociationBeans();

		boolean status = false;

		for (AssociationBean tmpAssociationBean : associationBean) {
			if (tmpAssociationBean
					.getDestinationPath()
					.contentEquals(
							"/_system/governance/trunk/wsdls/com/strikeiron/www/Automated.wsdl")) {
				status = true;

			}
		}

		assertTrue(status, "verifies the dependency");

	}
}
