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
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.governance.LifeCycleUtils;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

public class MetaDataServicesTestCase {

    private Registry governance;
    int userId = 1;
    private final static String WSDL_URL =
            "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
            + "platform-integration/clarity-tests/org.wso2.carbon.automation.test.repo/"
            + "src/main/resources/artifacts/GREG/wsdl/info.wsdl";
    ServiceManager serviceManager;
    private LifeCycleManagementClient lifeCycleManagerAdminService;
    private LifeCycleAdminServiceClient lifeCycleAdminService;
    private final String ACTION_PROMOTE = "Promote";
    private Wsdl wsdl;
    private Service serviceForDependencyVerification;
    private Service infoService;
    private Service infoServiceTesting;
    private Service serviceForTrunkDeleteTestPromoted;
    private Service serviceForBranchDeleteTest;
    private Service serviceForTickedListItemsTest;
    private Service serviceForTickedListItemsTestPromoted;
    private Service serviceForDetailVerificationTestCase;
    private Service serviceForLCPromoteTests;
    private Service serviceForLCPromoteTestsPromoted, serviceForLCPromoteTestsPromoted2,
            newService;

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
    }

    /**
     * Add a service without the defaultServiceVersion property so that the
     * service is saved as version
     * 1.0.0-SNAPSHOT
     *
     * @throws Exception
     */
    @Test(groups = {"wso2.greg"}, description = "service without the defaultServiceVersion property", priority = 1)
    public void addServiceWithoutVersion() throws Exception {

        Service service = serviceManager.newService(new QName("http://bang.boom.com/mnm/beep", "MyService"));
        serviceManager.addService(service);
        String serviceId = service.getId();
        newService = serviceManager.getService(serviceId);
        Assert.assertEquals(newService.getAttribute("overview_version"), "1.0.0-SNAPSHOT");
    }

    /**
     * Open an existing service, do changes to the service content and save.
     * Verify whether the changes get persisted
     *
     * @throws Exception
     */
    @Test(groups = {"wso2.greg"}, description = "service without the defaultServiceVersion property", dependsOnMethods = "addServiceWithoutVersion", priority = 2)
    public void serviceDetailUpdateTest() throws Exception {
        newService.setQName(new QName("http://bang.boom.com/renamed", "MyServiceRenamed"));
        WsdlManager manager = new WsdlManager(governance);
        wsdl = manager.newWsdl(WSDL_URL);
        manager.addWsdl(wsdl);
        newService.attachWSDL(wsdl);
        serviceManager.updateService(newService);
        String serviceId = newService.getId();
        Assert.assertEquals(serviceManager.getService(serviceId).getQName().getLocalPart(),
                            "MyServiceRenamed");
        Assert.assertEquals(serviceManager.getService(serviceId).getQName().getNamespaceURI(),
                            "http://bang.boom.com/renamed");
        Assert.assertEquals(serviceManager.getService(serviceId).getAttachedWsdls()[0].getQName()
                                    .getNamespaceURI(),
                            "http://footballpool.dataaccess.eu");

    }

    /**
     * Change the default location where you want to add services and verify
     * whether the service gets created at the correct location
     *
     * @throws GovernanceException
     */
    @Test(groups = {"wso2.greg"}, description = "Change the default location of a service", dependsOnMethods = "addServiceWithoutVersion", priority = 3)
    public void changeLocationTest() throws GovernanceException {
        // String servicePath = newService.getPath();
        // servicePath = servicePath+"/test";

    }

    /**
     * Update a service that is at branch level to verify whether the changes
     * done do not get persisted to the trunk level service
     * <p/>
     * Set an LC to a service, then promote it to the next LC level. Then do
     * changes to the service content and update the service and make sure that
     * the LC state is not set back to it's initial state
     *
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RemoteException
     * @throws RegistryException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     */
    @Test(groups = {"wso2.greg"}, description = "Update a service that is at trunk level", dependsOnMethods = "serviceDetailUpdateTest", priority = 4)
    public void changesAtBranchTest() throws RemoteException,
                                             LifeCycleManagementServiceExceptionException,
                                             RegistryException,
                                             CustomLifecyclesChecklistAdminServiceExceptionException {
        ArrayOfString[] parameters = new ArrayOfString[2];

        infoService = serviceManager.findServices(new ServiceFilter() {

            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                if (attributeVal != null && attributeVal.startsWith("Info")) {
                    return true;
                }
                return false;
            }
        })[0];
        infoService.attachLifecycle(lifeCycleManagerAdminService.getLifecycleList()[0]);

        String servicePathDev = "/_system/governance" + infoService.getPath();
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDev, "2.0.0"});
        lifeCycleAdminService.invokeAspectWithParams(servicePathDev, "ServiceLifeCycle",
                                                     ACTION_PROMOTE, null, parameters);

        infoServiceTesting = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                String attributeVal2 = service.getAttribute("overview_version");
                if (attributeVal != null && attributeVal.startsWith("Info") &&
                    attributeVal2.startsWith("2.0.0")) {
                    return true;
                }
                return false;
            }
        })[0];
        infoServiceTesting.setAttribute("test-att", "test-value");
        Assert.assertEquals(infoService.getAttribute("test-att"), null);
        Assert.assertEquals(infoServiceTesting.getAttribute("test-att"), "test-value");
        Assert.assertEquals(infoServiceTesting.getLifecycleState(), "Testing");
    }

    /**
     * Update a service that is at trunk level to verify whether the changes
     * done do not get persisted to the branch level services, that were
     * promoted from the updated service
     *
     * @throws GovernanceException
     */
    @Test(groups = {"wso2.greg"}, description = "Update a service that is at trunk level", dependsOnMethods = "changesAtBranchTest", priority = 4)
    public void changesAtTrunkTest() throws GovernanceException {
        infoService.setAttribute("test-att2", "test-value");
        Assert.assertEquals(infoServiceTesting.getAttribute("test-att2"), null);
        Assert.assertEquals(infoService.getAttribute("test-att2"), "test-value");
    }

    /**
     * Create a service without a WSDL. Then add the WSDL later on and verify
     * whether the dependencies get resolved
     *
     * @throws GovernanceException
     */
    @Test(groups = {"wso2.greg"}, description = "Create a service without a WSDL and verify dependencies", dependsOnMethods = "changesAtTrunkTest")
    public void verifyDependenciesTest() throws GovernanceException {
        serviceForDependencyVerification =
                serviceManager.newService(new QName(
                        "http://service.dependency.varification/mnm/beep",
                        "serviceForDependencyVarification"));
        serviceManager.addService(serviceForDependencyVerification);
        serviceForDependencyVerification.attachWSDL(wsdl);
        Assert.assertEquals(serviceForDependencyVerification.getDependencies()[0].getQName()
                                    .getLocalPart(),
                            "info.wsdl");
    }

    /**
     * Delete a service that is in the trunk level and verify whether there is
     * no effect on other services promoted from the deleted service
     *
     * @throws GovernanceException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws RemoteException
     * @throws LifeCycleManagementServiceExceptionException
     *
     */
    @Test(groups = {"wso2.greg"}, description = "delete a service at trunk level", dependsOnMethods = "changesAtTrunkTest")
    public void deleteServiceAtTrunkTest() throws GovernanceException, RemoteException,
                                                  CustomLifecyclesChecklistAdminServiceExceptionException,
                                                  LifeCycleManagementServiceExceptionException {
        Service serviceForTrunkDeleteTest = serviceManager.newService(new QName(
                "http://service.delete.trunk/mnm/beep",
                "serviceForTrunkDeleteTest"));
        serviceManager.addService(serviceForTrunkDeleteTest);
        String servicePathDev = "/_system/governance" + serviceForTrunkDeleteTest.getPath();
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDev, "2.0.0"});
        serviceForTrunkDeleteTest.attachLifecycle(lifeCycleManagerAdminService.getLifecycleList()[0]);
        lifeCycleAdminService.invokeAspectWithParams(servicePathDev, "ServiceLifeCycle",
                                                     ACTION_PROMOTE, null, parameters);
        serviceForTrunkDeleteTestPromoted = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                String attributeVal2 = service.getAttribute("overview_version");
                if (attributeVal != null && attributeVal.startsWith("serviceForTrunkDeleteTest") &&
                    attributeVal2.startsWith("2.0.0")) {
                    return true;
                }
                return false;
            }
        })[0];
        serviceManager.removeService(serviceForTrunkDeleteTest.getId());
        Assert.assertEquals(serviceForTrunkDeleteTestPromoted.getPath(),
                            "/branches/testing/services/trunk/delete/service/mnm/beep/2.0.0/serviceForTrunkDeleteTest");
    }

    /**
     * Delete a service that is in the branch level and verify whether it has no
     * impact to the service in the trunk level
     *
     * @throws GovernanceException
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     * @throws LifeCycleManagementServiceExceptionException
     *
     */
    @Test(groups = {"wso2.greg"}, description = "delete a service at trunk level", dependsOnMethods = "changesAtTrunkTest")
    public void deleteServiceAtBranchTest() throws GovernanceException, RemoteException,
                                                   CustomLifecyclesChecklistAdminServiceExceptionException,
                                                   LifeCycleManagementServiceExceptionException {
        serviceForBranchDeleteTest =
                serviceManager.newService(new QName(
                        "http://service.delete.branch/mnm/beep",
                        "serviceForBranchDeleteTest"));
        serviceManager.addService(serviceForBranchDeleteTest);
        String servicePathDev = "/_system/governance" + serviceForBranchDeleteTest.getPath();
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDev, "2.0.0"});
        serviceForBranchDeleteTest.attachLifecycle(lifeCycleManagerAdminService.getLifecycleList()[0]);
        lifeCycleAdminService.invokeAspectWithParams(servicePathDev, "ServiceLifeCycle",
                                                     ACTION_PROMOTE, null, parameters);
        Service serviceForBranchDeleteTestPromoted = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                String attributeVal2 = service.getAttribute("overview_version");
                return attributeVal != null &&
                       attributeVal.startsWith("serviceForBranchDeleteTest") &&
                       attributeVal2.startsWith("2.0.0");
            }
        })[0];
        serviceManager.removeService(serviceForBranchDeleteTestPromoted.getId());
        Assert.assertEquals(serviceForBranchDeleteTest.getPath(),
                            "/trunk/services/branch/delete/service/mnm/beep/serviceForBranchDeleteTest");
    }

    /**
     * Verify whether the ticked check list items get unticked when updating the
     * service content which is at,
     * - trunk level
     * - branch level
     *
     * @throws GovernanceException
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     */
    @Test(groups = {"wso2.greg"}, description = "Checking the persistance with ticked check list items", dependsOnMethods = "changesAtTrunkTest")
    public void tickedListItemsTest() throws GovernanceException, RemoteException,
                                             LifeCycleManagementServiceExceptionException,
                                             CustomLifecyclesChecklistAdminServiceExceptionException {
        serviceForTickedListItemsTest =
                serviceManager.newService(new QName(
                        "http://service.ticked.items/mnm/beep",
                        "serviceForTickedListItemsTest"));
        serviceManager.addService(serviceForTickedListItemsTest);
        serviceForTickedListItemsTest.attachLifecycle(lifeCycleManagerAdminService.getLifecycleList()[0]);
        String servicePathDev = "/_system/governance" + serviceForTickedListItemsTest.getPath();
        String ACTION_ITEM_CLICK = "itemClick";
        lifeCycleAdminService.invokeAspect(servicePathDev, "ServiceLifeCycle", ACTION_ITEM_CLICK,
                                           new String[]{"false", "true", "true"});

        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathDev);
        String optionOneValueBefore =
                LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                                                    "registry.custom_lifecycle.checklist.option.0.item")[2];
        String optionTwoValueBefore =
                LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                                                    "registry.custom_lifecycle.checklist.option.1.item")[3];
        String optionThreeValueBefore =
                LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                                                    "registry.custom_lifecycle.checklist.option.2.item")[3];
        serviceForTickedListItemsTest.addAttribute("test-att", "test-val");

        Assert.assertEquals(optionOneValueBefore,
                            LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                                                                "registry.custom_lifecycle.checklist.option.0.item")[2]);
        Assert.assertEquals(optionTwoValueBefore,
                            LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                                                                "registry.custom_lifecycle.checklist.option.1.item")[3]);
        Assert.assertEquals(optionThreeValueBefore,
                            LifeCycleUtils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(),
                                                                "registry.custom_lifecycle.checklist.option.2.item")[3]);

        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDev, "2.0.0"});
        lifeCycleAdminService.invokeAspectWithParams(servicePathDev, "ServiceLifeCycle",
                                                     ACTION_PROMOTE, null, parameters);

        serviceForTickedListItemsTestPromoted = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                String attributeVal2 = service.getAttribute("overview_version");
                if (attributeVal != null &&
                    attributeVal.startsWith("serviceForTickedListItemsTest") &&
                    attributeVal2.startsWith("2.0.0")) {
                    return true;
                }
                return false;
            }
        })[0];
        String promotedServicePathDev =
                "/_system/governance" +
                serviceForTickedListItemsTestPromoted.getPath();
        lifeCycleAdminService.invokeAspect(promotedServicePathDev, "ServiceLifeCycle",
                                           ACTION_ITEM_CLICK, new String[]{"false", "true",
                                                                           "true"});

        LifecycleBean lifeCyclePromoted =
                lifeCycleAdminService.getLifecycleBean(promotedServicePathDev);
        optionOneValueBefore =
                LifeCycleUtils.getLifeCycleProperty(lifeCyclePromoted.getLifecycleProperties(),
                                                    "registry.custom_lifecycle.checklist.option.0.item")[2];
        optionTwoValueBefore =
                LifeCycleUtils.getLifeCycleProperty(lifeCyclePromoted.getLifecycleProperties(),
                                                    "registry.custom_lifecycle.checklist.option.1.item")[3];
        optionThreeValueBefore =
                LifeCycleUtils.getLifeCycleProperty(lifeCyclePromoted.getLifecycleProperties(),
                                                    "registry.custom_lifecycle.checklist.option.2.item")[3];
        serviceForTickedListItemsTest.addAttribute("test-att", "test-val");

        Assert.assertEquals(optionOneValueBefore,
                            LifeCycleUtils.getLifeCycleProperty(lifeCyclePromoted.getLifecycleProperties(),
                                                                "registry.custom_lifecycle.checklist.option.0.item")[2]);
        Assert.assertEquals(optionTwoValueBefore,
                            LifeCycleUtils.getLifeCycleProperty(lifeCyclePromoted.getLifecycleProperties(),
                                                                "registry.custom_lifecycle.checklist.option.1.item")[3]);
        Assert.assertEquals(optionThreeValueBefore,
                            LifeCycleUtils.getLifeCycleProperty(lifeCyclePromoted.getLifecycleProperties(),
                                                                "registry.custom_lifecycle.checklist.option.2.item")[3]);

    }

    /**
     * Verify whether the following column shows correct information for the
     * added service
     * - Service Name
     * - Service Version
     * - Service Namespace
     * - Lifecycle Status
     * - Actions
     * <p/>
     * When a LC is assigned to the service, the Lifecycle status should display
     * the first LC state
     *
     * @throws GovernanceException
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RemoteException
     */
    @Test(groups = {"wso2.greg"}, description = "Verify Service Information", dependsOnMethods = "tickedListItemsTest")
    public void serviceDetailVerificationTestCase() throws GovernanceException, RemoteException,
                                                           LifeCycleManagementServiceExceptionException {
        serviceForDetailVerificationTestCase =
                serviceManager.newService(new QName(
                        "http://service.detail.verification/mnm/beep",
                        "serviceForDetailVerificationTestCase"));
        serviceForDetailVerificationTestCase.addAttribute("overview_version", "2.0.0");
        serviceManager.addService(serviceForDetailVerificationTestCase);
        serviceForDetailVerificationTestCase.attachLifecycle(lifeCycleManagerAdminService.getLifecycleList()[0]);
        Assert.assertEquals(serviceForDetailVerificationTestCase.getAttribute("overview_name"),
                            "serviceForDetailVerificationTestCase");
        Assert.assertEquals(serviceForDetailVerificationTestCase.getAttribute("overview_namespace"),
                            "http://service.detail.verification/mnm/beep");
        Assert.assertEquals(serviceForDetailVerificationTestCase.getAttribute("overview_version"),
                            "2.0.0");
        Assert.assertEquals(serviceForDetailVerificationTestCase.getLifecycleState(), "Development");

    }

    /**
     * When a service is deleted, the service should be removed from the list
     *
     * @throws GovernanceException
     */
    @Test(groups = {"wso2.greg"}, description = "Deleting a service", dependsOnMethods = "serviceDetailVerificationTestCase")
    public void deleteService() throws GovernanceException {
        Service serviceForDeleteServiceTestCase = serviceManager.newService(new QName(
                "http://service.delete.verification/mnm/beep",
                "serviceForDeleteServiceTestCase"));
        serviceManager.addService(serviceForDeleteServiceTestCase);
        serviceManager.removeService(serviceForDeleteServiceTestCase.getId());

        Service[] searchResult = serviceManager.findServices(new ServiceFilter() {

            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                if (attributeVal != null &&
                    attributeVal.startsWith("serviceForDeleteServiceTestCase")) {
                    return true;
                }
                return false;
            }
        });

        Assert.assertEquals(searchResult.length, 0);
    }

    /**
     * When services are promoted, the correct LC state should be updated for
     * each service
     *
     * @throws GovernanceException
     * @throws LifeCycleManagementServiceExceptionException
     *
     * @throws RemoteException
     * @throws CustomLifecyclesChecklistAdminServiceExceptionException
     *
     */
    @Test(groups = {"wso2.greg"}, description = "LC promote tests", dependsOnMethods = "deleteService")
    public void lcPromotingTestCase() throws GovernanceException, RemoteException,
                                             LifeCycleManagementServiceExceptionException,
                                             CustomLifecyclesChecklistAdminServiceExceptionException {
        serviceForLCPromoteTests =
                serviceManager.newService(new QName(
                        "http://service.for.lc/promote/test",
                        "serviceForLCPromoteTests"));
        serviceManager.addService(serviceForLCPromoteTests);
        serviceForLCPromoteTests.attachLifecycle(lifeCycleManagerAdminService.getLifecycleList()[0]);
        String servicePathDev = "/_system/governance" + serviceForLCPromoteTests.getPath();
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDev, "2.0.0"});
        serviceForLCPromoteTests.attachLifecycle(lifeCycleManagerAdminService.getLifecycleList()[0]);
        Assert.assertEquals(serviceForLCPromoteTests.getLifecycleState(), "Development");
        lifeCycleAdminService.invokeAspectWithParams(servicePathDev, "ServiceLifeCycle",
                                                     ACTION_PROMOTE, null, parameters);
        serviceForLCPromoteTestsPromoted = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                String attributeVal2 = service.getAttribute("overview_version");
                if (attributeVal != null && attributeVal.startsWith("serviceForLCPromoteTests") &&
                    attributeVal2.startsWith("2.0.0")) {
                    return true;
                }
                return false;
            }
        })[0];
        Assert.assertEquals(serviceForLCPromoteTestsPromoted.getLifecycleState(), "Testing");
        servicePathDev = "/_system/governance" + serviceForLCPromoteTestsPromoted.getPath();
        parameters[0].setArray(new String[]{servicePathDev, "3.0.0"});
        lifeCycleAdminService.invokeAspectWithParams(servicePathDev, "ServiceLifeCycle",
                                                     ACTION_PROMOTE, null, parameters);
        serviceForLCPromoteTestsPromoted2 = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("overview_name");
                String attributeVal2 = service.getAttribute("overview_version");
                if (attributeVal != null && attributeVal.startsWith("serviceForLCPromoteTests") &&
                    attributeVal2.startsWith("3.0.0")) {
                    return true;
                }
                return false;
            }
        })[0];

        Assert.assertEquals(serviceForLCPromoteTestsPromoted2.getLifecycleState(), "Production");

    }

    @AfterClass()
    public void cleanup() throws GovernanceException {

        serviceManager.removeService(serviceForDependencyVerification.getId());
        serviceManager.removeService(infoService.getId());
        serviceManager.removeService(infoServiceTesting.getId());
        serviceManager.removeService(serviceForTrunkDeleteTestPromoted.getId());
        serviceManager.removeService(serviceForBranchDeleteTest.getId());
        serviceManager.removeService(serviceForTickedListItemsTest.getId());
        serviceManager.removeService(serviceForTickedListItemsTestPromoted.getId());
        serviceManager.removeService(serviceForDetailVerificationTestCase.getId());
        serviceManager.removeService(serviceForLCPromoteTests.getId());
        serviceManager.removeService(serviceForLCPromoteTestsPromoted.getId());
        serviceManager.removeService(serviceForLCPromoteTestsPromoted2.getId());
        serviceManager.removeService(newService.getId());
    }

}
