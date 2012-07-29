/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.governance.api.test;

import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

public class FilterServicesWithAdminTestCase {
	private Registry governance;
	int userId = 1;
	private final static String WSDL_URL =
	                                       "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
	                                               + "platform-integration/clarity-tests/org.wso2.carbon.automation.test.repo/"
	                                               + "src/main/resources/artifacts/GREG/wsdl/info.wsdl";
	ServiceManager serviceManager;
	ResourceAdminServiceClient resourceAdminServiceClient;
	private LifeCycleManagementClient lifeCycleManagerAdminService;
	Service newService, serviceForSearching1, serviceForSearching2, serviceForSearching3,
	        serviceForSearching4, serviceForSearching5, serviceForPromoting, searchResultPromoted;
	private LifeCycleAdminServiceClient lifeCycleAdminService;
	private final String ACTION_PROMOTE = "Promote";

	@BeforeClass()
	public void initialize() throws RemoteException, LoginAuthenticationExceptionException,
	                        RegistryException {
		UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
		WSRegistryServiceClient wsRegistry =
		                                     new RegistryProviderUtil().getWSRegistry(userId,
		                                                                              ProductConstant.GREG_SERVER_NAME);
		governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, userId);
		serviceManager = new ServiceManager(governance);
		EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
		ManageEnvironment environment = builder.build();
		lifeCycleManagerAdminService =
		                               new LifeCycleManagementClient(
		                                                             environment.getGreg()
		                                                                        .getProductVariables()
		                                                                        .getBackendUrl(),
		                                                             userInfo.getUserName(),
		                                                             userInfo.getPassword());
		lifeCycleAdminService =
		                        new LifeCycleAdminServiceClient(environment.getGreg()
		                                                                   .getProductVariables()
		                                                                   .getBackendUrl(),
		                                                        userInfo.getUserName(),
		                                                        userInfo.getPassword());
		resourceAdminServiceClient =
		                             new ResourceAdminServiceClient(environment.getGreg()
		                                                                       .getBackEndUrl(),
		                                                            userInfo.getUserName(),
		                                                            userInfo.getPassword());

		serviceForSearching1 =
		                       serviceManager.newService(new QName(
		                                                           "http://service.for.searching1/mnm/",
		                                                           "serviceForSearching1"));
		serviceForSearching1.addAttribute("overview_version", "3.0.0");
		serviceForSearching1.addAttribute("overview_description", "Test");
		serviceForSearching1.addAttribute("interface_wsdlUrl", WSDL_URL);
		serviceForSearching1.addAttribute("docLinks_documentType", "test");
		serviceForSearching1.addAttribute("interface_messageFormats", "SOAP 1.2");
		serviceForSearching1.addAttribute("interface_messageExchangePatterns", "Request Response");
		serviceForSearching1.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
		serviceForSearching1.addAttribute("security_authenticationMechanism", "InfoCard");
		serviceForSearching1.addAttribute("security_messageIntegrity", "WS-Security");
		serviceForSearching1.addAttribute("security_messageEncryption", "WS-Security");
		serviceManager.addService(serviceForSearching1);

		serviceForSearching2 =
		                       serviceManager.newService(new QName(
		                                                           "http://service.for.searching2/mnm/",
		                                                           "serviceForSearching2"));
		serviceForSearching2.addAttribute("overview_version", "4.0.0");
		serviceForSearching2.addAttribute("overview_description", "Test");
		serviceForSearching2.addAttribute("interface_wsdlUrl", WSDL_URL);
		serviceForSearching2.addAttribute("docLinks_documentType", "test");
		serviceForSearching2.addAttribute("interface_messageFormats", "SOAP 1.2");
		serviceForSearching2.addAttribute("interface_messageExchangePatterns", "Request Response");
		serviceForSearching2.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
		serviceForSearching2.addAttribute("security_authenticationMechanism", "InfoCard");
		serviceForSearching2.addAttribute("security_messageIntegrity", "WS-Security");
		serviceForSearching2.addAttribute("security_messageEncryption", "WS-Security");
		serviceManager.addService(serviceForSearching2);

		serviceForSearching3 =
		                       serviceManager.newService(new QName(
		                                                           "http://service.for.searching3/mnm/",
		                                                           "serviceForSearching3"));
		serviceForSearching3.addAttribute("overview_version", "5.0.0");
		serviceForSearching3.addAttribute("overview_description", "Test");
		serviceForSearching3.addAttribute("interface_wsdlUrl", WSDL_URL);
		serviceForSearching3.addAttribute("docLinks_documentType", "test");
		serviceForSearching3.addAttribute("interface_messageFormats", "SOAP 1.2");
		serviceForSearching3.addAttribute("interface_messageExchangePatterns", "Request Response");
		serviceForSearching3.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
		serviceForSearching3.addAttribute("security_authenticationMechanism", "InfoCard");
		serviceForSearching3.addAttribute("security_messageIntegrity", "WS-Security");
		serviceForSearching3.addAttribute("security_messageEncryption", "WS-Security");
		serviceManager.addService(serviceForSearching3);

		serviceForSearching4 =
		                       serviceManager.newService(new QName(
		                                                           "http://service.for.searching4/mnm/",
		                                                           "serviceForSearching4"));
		serviceForSearching4.addAttribute("overview_version", "5.1.0");
		serviceForSearching4.addAttribute("overview_description", "Test");
		serviceForSearching4.addAttribute("interface_wsdlUrl", WSDL_URL);
		serviceForSearching4.addAttribute("docLinks_documentType", "test");
		serviceForSearching4.addAttribute("interface_messageFormats", "SOAP 1.2");
		serviceForSearching4.addAttribute("interface_messageExchangePatterns", "Request Response");
		serviceForSearching4.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
		serviceForSearching4.addAttribute("security_authenticationMechanism", "InfoCard");
		serviceForSearching4.addAttribute("security_messageIntegrity", "WS-Security");
		serviceForSearching4.addAttribute("security_messageEncryption", "WS-Security");
		serviceManager.addService(serviceForSearching4);

		serviceForSearching5 =
		                       serviceManager.newService(new QName(
		                                                           "http://service.for.searching5/mnm/",
		                                                           "serviceForSearching5"));
		serviceForSearching5.addAttribute("overview_version", "5.1.1");
		serviceForSearching5.addAttribute("overview_description", "Test");
		serviceForSearching5.addAttribute("interface_wsdlUrl", WSDL_URL);
		serviceForSearching5.addAttribute("docLinks_documentType", "test");
		serviceForSearching5.addAttribute("interface_messageFormats", "SOAP 1.2");
		serviceForSearching5.addAttribute("interface_messageExchangePatterns", "Request Response");
		serviceForSearching5.addAttribute("security_authenticationPlatform ", "XTS-WS TRUST");
		serviceForSearching5.addAttribute("security_authenticationMechanism", "InfoCard");
		serviceForSearching5.addAttribute("security_messageIntegrity", "WS-Security");
		serviceForSearching5.addAttribute("security_messageEncryption", "WS-Security");
		serviceManager.addService(serviceForSearching5);
	}

	/**
	 * Do filtering from the basic filter available on the 'Service List' page.
	 * Here I will use the service filter to mimic the process
	 * <p/>
	 * Search by giving single search criteria
	 * <p/>
	 * Add a new service, then search for the services through basic and
	 * advanced search
	 * 
	 * @throws GovernanceException
	 */
	@Test(groups = { "wso2.greg" }, description = "Filter Services")
	public void filterServicesTestCase() throws GovernanceException {
		Service searchResult = serviceManager.findServices(new ServiceFilter() {

			public boolean matches(Service service) throws GovernanceException {
				String attributeVal = service.getAttribute("overview_name");
				if (attributeVal != null && attributeVal.startsWith("serviceForSearching1")) {
					return true;
				}
				return false;
			}
		})[0];

		Assert.assertEquals(searchResult.getAttribute("overview_name"), "serviceForSearching1");
	}

	/**
	 * Filter from all the available fields in the 'Filter Services' advance
	 * search page. Here I will use the service filter to mimic the process
	 * <p/>
	 * Search by giving multiple search criteria
	 * 
	 * @throws GovernanceException
	 */
	@Test(groups = { "wso2.greg" }, description = "Filter Services", dependsOnMethods = "filterServicesTestCase")
	public void advFilterServicesTestCase() throws GovernanceException {
		Service searchResult = serviceManager.findServices(new ServiceFilter() {

			public boolean matches(Service service) throws GovernanceException {
				String attributeVal1 = service.getAttribute("overview_name");
				String attributeVal2 = service.getAttribute("overview_version");
				String attributeVal3 = service.getAttribute("overview_description");
				String attributeVal4 = service.getAttribute("interface_wsdlUrl");
				String attributeVal5 = service.getAttribute("docLinks_documentType");
				String attributeVal6 = service.getAttribute("interface_messageFormats");
				String attributeVal7 = service.getAttribute("interface_messageExchangePatterns");
				String attributeVal8 = service.getAttribute("security_authenticationPlatform");
				String attributeVal9 = service.getAttribute("security_authenticationMechanism");
				String attributeVal10 = service.getAttribute("security_messageIntegrity");
				String attributeVal11 = service.getAttribute("security_messageEncryption");
				return attributeVal1 != null && attributeVal1.startsWith("serviceForSearching4") &&
				       attributeVal2 != null && attributeVal2.startsWith("5.1.0") &&
				       attributeVal3 != null && attributeVal3.startsWith("Test") &&
				       attributeVal4 != null && attributeVal4.startsWith(WSDL_URL) &&
				       attributeVal5 != null && attributeVal5.startsWith("test") &&
				       attributeVal6 != null && attributeVal6.startsWith("SOAP 1.2") &&
				       attributeVal7 != null && attributeVal7.startsWith("Request Response") &&
				       attributeVal8 != null && attributeVal8.startsWith("XTS-WS TRUST") &&
				       attributeVal9 != null && attributeVal9.startsWith("InfoCard") &&
				       attributeVal10 != null && attributeVal10.startsWith("WS-Security") &&
				       attributeVal11 != null && attributeVal11.startsWith("WS-Security");
			}
		})[0];

		Assert.assertEquals(searchResult.getAttribute("overview_name"), "serviceForSearching4");
		Assert.assertEquals(searchResult.getAttribute("overview_version"), "5.1.0");
		Assert.assertEquals(searchResult.getAttribute("overview_description"), "Test");
		Assert.assertEquals(searchResult.getAttribute("interface_wsdlUrl"), WSDL_URL);
		Assert.assertEquals(searchResult.getAttribute("docLinks_documentType"), "test");
		Assert.assertEquals(searchResult.getAttribute("interface_messageFormats"), "SOAP 1.2");
		Assert.assertEquals(searchResult.getAttribute("interface_messageExchangePatterns"),
		                    "Request Response");
		Assert.assertEquals(searchResult.getAttribute("security_authenticationPlatform"),
		                    "XTS-WS TRUST");
		Assert.assertEquals(searchResult.getAttribute("security_authenticationMechanism"),
		                    "InfoCard");
		Assert.assertEquals(searchResult.getAttribute("security_messageIntegrity"), "WS-Security");
		Assert.assertEquals(searchResult.getAttribute("security_messageEncryption"), "WS-Security");
	}

	/**
	 * Edit the content of the service and then search for the services through
	 * basic and advanced search
	 * 
	 * @throws GovernanceException
	 */
	@Test(groups = { "wso2.greg" }, description = "Filter Edited Services", dependsOnMethods = "advFilterServicesTestCase")
	public void filterEditedServices() throws GovernanceException {
		serviceForSearching2.addAttribute("test-att", "test-val");
		serviceManager.updateService(serviceForSearching2);
		Service searchResult = serviceManager.findServices(new ServiceFilter() {

			public boolean matches(Service service) throws GovernanceException {
				String attributeVal = service.getAttribute("test-att");
				if (attributeVal != null && attributeVal.startsWith("test-val")) {
					return true;
				}
				return false;
			}
		})[0];

		Assert.assertEquals(searchResult.getAttribute("overview_name"), "serviceForSearching2");
	}

	/**
	 * Search for a particular service. Then promote that service to the next
	 * LC state and search to see whether both the original service as well as
	 * the promoted service is captured through the search
	 * 
	 * @throws GovernanceException
	 * @throws LifeCycleManagementServiceExceptionException
	 * 
	 * @throws RemoteException
	 * @throws CustomLifecyclesChecklistAdminServiceExceptionException
	 * 
	 */
	@Test(groups = { "wso2.greg" }, description = "Filter Promoted Services", dependsOnMethods = "filterEditedServices")
	public void filterPromotedServices() throws GovernanceException, RemoteException,
	                                    LifeCycleManagementServiceExceptionException,
	                                    CustomLifecyclesChecklistAdminServiceExceptionException {
		serviceForPromoting =
		                      serviceManager.newService(new QName(
		                                                          "http://service.delete.branch/mnm/beep",
		                                                          "serviceForPromoting"));
		serviceManager.addService(serviceForPromoting);
		String servicePathDev = "/_system/governance" + serviceForPromoting.getPath();
		ArrayOfString[] parameters = new ArrayOfString[2];
		parameters[0] = new ArrayOfString();
		parameters[0].setArray(new String[] { servicePathDev, "2.0.0" });
		serviceForPromoting.attachLifecycle(lifeCycleManagerAdminService.getLifecycleList()[0]);
		lifeCycleAdminService.invokeAspectWithParams(servicePathDev, "ServiceLifeCycle",
		                                             ACTION_PROMOTE, null, parameters);
		Service searchResult = serviceManager.findServices(new ServiceFilter() {
			public boolean matches(Service service) throws GovernanceException {
				String attributeVal = service.getAttribute("overview_name");
				String attributeVal2 = service.getAttribute("overview_version");
				return attributeVal != null && attributeVal.startsWith("serviceForPromoting") &&
				       attributeVal2.startsWith("1.0.0-SNAPSHOT");
			}
		})[0];
		searchResultPromoted = serviceManager.findServices(new ServiceFilter() {
			public boolean matches(Service service) throws GovernanceException {
				String attributeVal = service.getAttribute("overview_name");
				String attributeVal2 = service.getAttribute("overview_version");
				return attributeVal != null && attributeVal.startsWith("serviceForPromoting") &&
				       attributeVal2.startsWith("2.0.0");
			}
		})[0];

		Assert.assertEquals(searchResult.getAttribute("overview_version"), "1.0.0-SNAPSHOT");
		Assert.assertEquals(searchResultPromoted.getAttribute("overview_version"), "2.0.0");
	}

	/**
	 * Search for a service with particular information. Then remove that
	 * information from the service and save. Search again using the same search
	 * criteria and the service should not be captured now
	 * 
	 * @throws GovernanceException
	 */
	@Test(groups = { "wso2.greg" }, description = "Filter Changed Services", dependsOnMethods = "filterPromotedServices")
	public void filterChangedServices() throws GovernanceException {
		Service searchResult1 = serviceManager.findServices(new ServiceFilter() {
			public boolean matches(Service service) throws GovernanceException {
				String attributeVal = service.getAttribute("overview_description");
				String attributeVal2 = service.getAttribute("overview_version");
				return attributeVal != null && attributeVal.startsWith("Test") &&
				       attributeVal2.startsWith("5.0.0");
			}
		})[0];
		Assert.assertEquals(searchResult1.getAttribute("overview_name"), "serviceForSearching3");

		serviceForSearching3.removeAttribute("overview_description");
		serviceManager.updateService(serviceForSearching3);
		Service searchResult2[] = serviceManager.findServices(new ServiceFilter() {
			public boolean matches(Service service) throws GovernanceException {
				String attributeVal = service.getAttribute("overview_description");
				String attributeVal2 = service.getAttribute("overview_version");
				return attributeVal != null && attributeVal.startsWith("Test") &&
				       attributeVal2.startsWith("5.0.0");
			}
		});
		Assert.assertEquals(searchResult2.length, 0);
	}

	/**
	 * Delete a service and search for it
	 * 
	 * @throws GovernanceException
	 */
	@Test(groups = { "wso2.greg" }, description = "Seaching for a deleted service", dependsOnMethods = "filterChangedServices")
	public void searchForDeletedService() throws GovernanceException {
		serviceManager.removeService(serviceForSearching5.getId());
		Service searchResult[] = serviceManager.findServices(new ServiceFilter() {
			public boolean matches(Service service) throws GovernanceException {
				String attributeVal = service.getAttribute("overview_name");
				return attributeVal != null && attributeVal.startsWith("serviceForSearching5");
			}
		});
		Assert.assertEquals(searchResult.length, 0);
	}

	@AfterClass()
	public void cleanup() throws GovernanceException {
		serviceManager.removeService(serviceForPromoting.getId());
		serviceManager.removeService(serviceForSearching1.getId());
		serviceManager.removeService(serviceForSearching2.getId());
		serviceManager.removeService(serviceForSearching3.getId());
		serviceManager.removeService(serviceForSearching4.getId());
		serviceManager.removeService(searchResultPromoted.getId());
	}

}
