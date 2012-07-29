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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.ADBException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.RelationAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.RegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.beans.xsd.VersionPath;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

public class ServicesResourceInformationManagementTestCase {

    int userId = 1;
    Service newService, serviceForInformationVerification, serviceForCommentVerification, serviceForCheckpointVerification;
    Service serviceForEndPointDeleting, serviceForDependencyVerification2, serviceForDependencyVerification;
    private final static String WSDL_URL =
            "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
            + "platform-integration/clarity-tests/org.wso2.carbon.automation.test.repo/"
            + "src/main/resources/artifacts/GREG/wsdl/info.wsdl";
    private final static String POLICY_URL =
            "http://svn.wso2.org/repos/wso2/carbon/platform/trunk/"
            + "platform-integration/system-test-framework/core/org.wso2.automation.platform.core/"
            + "src/main/resources/artifacts/GREG/policy/UTPolicy.xml";
    private final static String SCHEMA_URL =
            "http://svn.wso2.org/repos/wso2/carbon/platform/"
            + "trunk/products/greg/modules/integration/registry/"
            + "tests/src/test/java/resources/schema/calculator.xsd";
    private final static String DEPENDENCY_PATH = "/_system/governance/trunk/";
    private final static String ROOT = "/_system/governance";
    private ServiceManager serviceManager;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private InfoServiceAdminClient infoServiceAdminClient;
    private RelationAdminServiceClient relationServiceClient;

    private EndpointManager endpointManager;
    private PolicyManager policyManager;
    private WsdlManager wsdlManager;
    private SchemaManager schemaManager;
    private ManageEnvironment environment;
    private Wsdl wsdl;
    private Policy policy;
    private Schema schema;
    private Endpoint endpoint;

    @BeforeClass()
    public void initialize() throws RemoteException, LoginAuthenticationExceptionException,
                                    RegistryException {
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);
        WSRegistryServiceClient wsRegistry =
                new RegistryProviderUtil().getWSRegistry(userId,
                                                         ProductConstant.GREG_SERVER_NAME);

        Registry governance = new RegistryProviderUtil().getGovernanceRegistry(wsRegistry, userId);
        serviceManager = new ServiceManager(governance);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg()
                                                       .getBackEndUrl(),
                                               userInfo.getUserName(),
                                               userInfo.getPassword());
        infoServiceAdminClient =
                new InfoServiceAdminClient(environment.getGreg()
                                                   .getProductVariables()
                                                   .getBackendUrl(),
                                           userInfo.getUserName(),
                                           userInfo.getPassword());
        relationServiceClient =
                new RelationAdminServiceClient(environment.getGreg()
                                                       .getBackEndUrl(),
                                               userInfo.getUserName(),
                                               userInfo.getPassword());
        endpointManager = new EndpointManager(governance);
        policyManager = new PolicyManager(governance);
        wsdlManager = new WsdlManager(governance);
        schemaManager = new SchemaManager(governance);
    }

    /**
     * All information added at service creation should be available (service
     * content)
     *
     * @throws Exception
     */
    @Test(groups = {"wso2.greg"}, description = "All information added at service creation should be available", priority = 1)
    public void addServiceInformation() throws Exception {

        serviceForInformationVerification =
                serviceManager.newService(new QName(
                        "http://service.for.informationverification/mnm/",
                        "serviceForInformationVerification"));
        serviceForInformationVerification.addAttribute("overview_version", "2.0.0");
        serviceForInformationVerification.addAttribute("overview_description", "Test");
        serviceForInformationVerification.addAttribute("interface_wsdlUrl", WSDL_URL);
        serviceForInformationVerification.addAttribute("docLinks_documentType", "test");
        serviceForInformationVerification.addAttribute("interface_messageFormats", "SOAP 1.2");
        serviceForInformationVerification.addAttribute("interface_messageExchangePatterns",
                                                       "Request Response");
        serviceForInformationVerification.addAttribute("security_authenticationPlatform",
                                                       "XTS-WS TRUST");
        serviceForInformationVerification.addAttribute("security_authenticationMechanism",
                                                       "InfoCard");
        serviceForInformationVerification.addAttribute("security_messageIntegrity", "WS-Security");
        serviceForInformationVerification.addAttribute("security_messageEncryption", "WS-Security");

        serviceManager.addService(serviceForInformationVerification);
        String serviceId = serviceForInformationVerification.getId();
        newService = serviceManager.getService(serviceId);

        Assert.assertEquals(serviceForInformationVerification.getAttribute("overview_version"),
                            "2.0.0");
        Assert.assertEquals(serviceForInformationVerification.getAttribute("overview_description"),
                            "Test");
        Assert.assertEquals(serviceForInformationVerification.getAttribute("interface_wsdlUrl"),
                            WSDL_URL);
        Assert.assertEquals(serviceForInformationVerification.getAttribute("docLinks_documentType"),
                            "test");
        Assert.assertEquals(serviceForInformationVerification.getAttribute("interface_messageFormats"),
                            "SOAP 1.2");
        Assert.assertEquals(serviceForInformationVerification.getAttribute("interface_messageExchangePatterns"),
                            "Request Response");
        Assert.assertEquals(serviceForInformationVerification.getAttribute("security_authenticationMechanism"),
                            "InfoCard");
        Assert.assertEquals(serviceForInformationVerification.getAttribute("security_authenticationPlatform"),
                            "XTS-WS TRUST");
        Assert.assertEquals(serviceForInformationVerification.getAttribute("security_messageIntegrity"),
                            "WS-Security");
        Assert.assertEquals(serviceForInformationVerification.getAttribute("security_messageEncryption"),
                            "WS-Security");
    }

    /**
     * Verify whether feeds of comments and service resource contains correct information
     *
     * @throws RegistryException
     * @throws AxisFault
     * @throws RegistryExceptionException
     */
    @Test(groups = "wso2.greg", description = "Comments Verification", dependsOnMethods = "addServiceInformation")
    public void commentVerificationTestCase()
            throws AxisFault, RegistryException, RegistryExceptionException {
        serviceForCommentVerification =
                serviceManager.newService(new QName(
                        "http://service.for.commentverification2/mnm/",
                        "serviceForCommentVerification"));
        serviceManager.addService(serviceForCommentVerification);
        String comment = "Test Comment";
        String path = ROOT + serviceForCommentVerification.getPath();
        String sessionId = environment.getGreg().getSessionCookie();
        infoServiceAdminClient.addComment(comment, path, sessionId);
        Assert.assertEquals(infoServiceAdminClient.getComments(path, sessionId).getComments()[0].getContent(), comment);

    }

    /**
     * Create checkpoints and verify whether the created versions contain
     * correct information
     *
     * @throws GovernanceException
     * @throws ResourceAdminServiceExceptionException
     *
     * @throws RemoteException
     * @throws ADBException
     */
    @Test(groups = {"wso2.greg"}, description = "Checkpoint Service Verification", dependsOnMethods = "commentVerificationTestCase")
    public void checkpointServiceVerificationTestCase() throws GovernanceException,
                                                               RemoteException,
                                                               ResourceAdminServiceExceptionException,
                                                               ADBException {
        serviceForCheckpointVerification =
                serviceManager.newService(new QName(
                        "http://service.for.checkpointverification/mnm/",
                        "serviceForCheckpointVerification"));
        serviceForCheckpointVerification.addAttribute("test-att", "test-val");
        serviceManager.addService(serviceForCheckpointVerification);
        String destinationPath = ROOT + serviceForCheckpointVerification.getPath();
        resourceAdminServiceClient.createVersion(destinationPath);
        serviceForCheckpointVerification.addAttribute("test-att2", "test-val2");
        serviceManager.updateService(serviceForCheckpointVerification);
        resourceAdminServiceClient.createVersion(destinationPath);

        VersionPath[] versionPaths = resourceAdminServiceClient.getVersionPaths(destinationPath);
        resourceAdminServiceClient.getTextContent(versionPaths[0].getCompleteVersionPath());
        resourceAdminServiceClient.getTextContent(versionPaths[1].getCompleteVersionPath());

        Assert.assertTrue(resourceAdminServiceClient.getTextContent(versionPaths[1].getCompleteVersionPath())
                                  .contains("test-att"));
        Assert.assertFalse(resourceAdminServiceClient.getTextContent(versionPaths[1].getCompleteVersionPath())
                                   .contains("test-att2"));
        Assert.assertTrue(resourceAdminServiceClient.getTextContent(versionPaths[0].getCompleteVersionPath())
                                  .contains("test-att"));
        Assert.assertTrue(resourceAdminServiceClient.getTextContent(versionPaths[0].getCompleteVersionPath())
                                  .contains("test-att2"));

    }

    /**
     * Verify whether deleted endpoints appear back after saving the service
     *
     * @throws GovernanceException
     */
    @Test(groups = {"wso2.greg"}, description = "deleted endpoints", dependsOnMethods = "checkpointServiceVerificationTestCase")
    public void deletingEndPoints() throws GovernanceException {
        serviceForEndPointDeleting =
                serviceManager.newService(new QName(
                        "http://service.for.EndPointDeleting/mnm/",
                        "serviceForEndPointDeleting"));
        Endpoint endpoint = endpointManager.newEndpoint("http://service.for.EndPointDeleting");
        endpointManager.addEndpoint(endpoint);
        serviceManager.addService(serviceForEndPointDeleting);
        serviceForEndPointDeleting.attachEndpoint(endpoint);
        Endpoint endpoints[] = serviceForEndPointDeleting.getAttachedEndpoints();

        Assert.assertEquals(endpoints.length, 1);
        serviceForEndPointDeleting.detachEndpoint(endpoint.getId());
        endpoints = serviceForEndPointDeleting.getAttachedEndpoints();
        Assert.assertEquals(endpoints.length, 0);
    }


    /**
     * Attach/detach resources other than metadata to the service as
     * dependencies and verify whether they appear with check boxes to be
     * promoted
     *
     * @throws ResourceAdminServiceExceptionException
     *
     * @throws RemoteException
     * @throws MalformedURLException
     * @throws AddAssociationRegistryExceptionException
     *
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException
     */
    @Test(groups = "wso2.greg", description = "Dependency Verification", dependsOnMethods = {"deletingEndPoints"})
    public void dependencyVerificationTest2() throws GovernanceException, RemoteException,
                                                     ResourceAdminServiceExceptionException,
                                                     MalformedURLException,
                                                     AddAssociationRegistryExceptionException {
        serviceForDependencyVerification2 =
                serviceManager.newService(new QName(
                        "http://service.for.dependencyverification2/mnm/",
                        "serviceForDependencyVerification2"));
        serviceManager.addService(serviceForDependencyVerification2);

        String path =
                ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator +
                "GREG" + File.separator + "testresource.txt";
        DataHandler dataHandler = new DataHandler(new URL("file:///" + path));

        resourceAdminServiceClient.addResource(DEPENDENCY_PATH + "testresource.txt", "text/plain",
                                               "desc", dataHandler);

        String dependencyType = "depends";
        String todo = "add";
        relationServiceClient.addAssociation(ROOT +
                                             serviceForDependencyVerification2.getPath(),
                                             dependencyType, DEPENDENCY_PATH + "testresource.txt",
                                             todo);

        Assert.assertEquals(relationServiceClient.getDependencies(ROOT +
                                                                  serviceForDependencyVerification2.getPath())
                                    .getAssociationBeans()[0].getDestinationPath(),
                            "/_system/governance/trunk/testresource.txt");
    }


    /**
     * Attach/detach a policy/schema/wsdl/endpoint to the service as
     * dependencies and verify whether they appear with check boxes to be
     * promoted
     * <p/>
     * https://wso2.org/jira/browse/REGISTRY-1179
     *
     * @throws GovernanceException
     */
    @Test(groups = "wso2.greg", description = "Dependency Verification", dependsOnMethods = {"dependencyVerificationTest2"})
    public void dependencyVerificationTest() throws GovernanceException {
        serviceForDependencyVerification =
                serviceManager.newService(new QName(
                        "http://service.for.dependencyverification/mnm/",
                        "serviceForDependencyVerification"));
        endpoint =
                endpointManager.newEndpoint("http://service.for.EndPointDeleting");
        wsdl = wsdlManager.newWsdl(WSDL_URL);
        policy = policyManager.newPolicy(POLICY_URL);
        schema = schemaManager.newSchema(SCHEMA_URL);
        wsdlManager.addWsdl(wsdl);
        policyManager.addPolicy(policy);
        schemaManager.addSchema(schema);
        endpointManager.addEndpoint(endpoint);
        serviceManager.addService(serviceForDependencyVerification);
        serviceForDependencyVerification.attachEndpoint(endpoint);
        serviceForDependencyVerification.attachPolicy(policy);
        serviceForDependencyVerification.attachSchema(schema);
        serviceForDependencyVerification.attachWSDL(wsdl);

        Assert.assertEquals(serviceForDependencyVerification.getDependencies().length, 3);

    }

    @AfterClass()
    public void cleanup() throws GovernanceException {
        serviceManager.removeService(serviceForCheckpointVerification.getId());
        serviceManager.removeService(serviceForCommentVerification.getId());
        serviceManager.removeService(serviceForDependencyVerification.getId());
        serviceManager.removeService(serviceForDependencyVerification2.getId());
        serviceManager.removeService(serviceForEndPointDeleting.getId());
        serviceManager.removeService(serviceForInformationVerification.getId());
        serviceManager.removeService(
                serviceManager.findServices(new ServiceFilter() {
                    public boolean matches(Service service) throws GovernanceException {
                        String attributeVal = service.getAttribute("overview_name");
                        if (attributeVal != null && attributeVal.startsWith("Info")) {
                            return true;
                        }
                        return false;
                    }
                })[0].getId());
        wsdlManager.removeWsdl(wsdl.getId());
        policyManager.removePolicy(policy.getId());
        schemaManager.removeSchema(schema.getId());
        endpointManager.removeEndpoint(endpoint.getId());
    }

}
