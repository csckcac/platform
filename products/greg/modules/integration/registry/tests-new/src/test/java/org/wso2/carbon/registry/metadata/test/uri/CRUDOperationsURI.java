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

package org.wso2.carbon.registry.metadata.test.uri;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.jaxen.JaxenException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.authenticators.AuthenticatorClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;

public class CRUDOperationsURI {
    int userId = 1;
    private Registry governance;
    private ManageEnvironment environment;
    private UserInfo userinfo;

    @BeforeClass
    public void initialize()
            throws LoginAuthenticationExceptionException, RemoteException, RegistryException {

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        WSRegistryServiceClient registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(registry, userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        userinfo = UserListCsvReader.getUserInfo(userId);
    }

    @Test(groups = "wso2.greg", description = "Add/get/delete URI Artifact (CRUD)")
    public void testUriArtifact()
            throws XMLStreamException, LoginAuthenticationExceptionException, RemoteException,
                   JaxenException, RegistryException {

        AuthenticatorClient authenticatorClient =
                new AuthenticatorClient(environment.getGreg().getBackEndUrl());

        authenticatorClient.login(userinfo.getUserName(), userinfo.getPassword(),
                                  environment.getGreg().getProductVariables().getHostName());

        AuthenticationAdminStub stub = (AuthenticationAdminStub) authenticatorClient.getAuthenticationAdminStub();

        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);

        boolean result = stub.login("admin", "admin", "127.0.0.1");


        if (result) {

            options.setTo(new EndpointReference("https://localhost:9443/services/URI"));
            options.setAction("urn:addURI");
            options.setManageSession(true);
            OMElement omElementAddWsdl =
                    client.sendReceive(AXIOMUtil.stringToOM("<ser:addURI " +
                                                            "xmlns:ser=\"http://services.add.uri.governance.carbon.wso2.org\">" +
                                                            "<ser:info>&lt;metadata xmlns=\"http://www.wso2.org/governance/metadata\">" +
                                                            "&lt;overview>&lt;type>WSDL&lt;/type>&lt;name>Axis2Service_Wsdl_With_Wsdl_Imports.wsdl&lt;/name>" +
                                                            "&lt;uri>https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/clarity-tests/" +
                                                            "org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/wsdl/Axis2Service_Wsdl_With_Wsdl_Imports.wsdl&lt;" +
                                                            "/uri>&lt;/overview>&lt;/metadata></ser:info></ser:addURI>"));


            AXIOMXPath expression = new AXIOMXPath("//ns:return");
            expression.addNamespace("ns", omElementAddWsdl.getNamespace().getNamespaceURI());
            String artifactId = ((OMElement) expression.selectSingleNode(omElementAddWsdl)).getText();

            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
            GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "uri");
            String[] allUriGenericArtifacts = artifactManager.getAllGenericArtifactIds();

            assertEquals(isGenericArtifactExists(allUriGenericArtifacts, artifactId), true);

            options.setAction("urn:getURI");
            client.sendReceive(AXIOMUtil.stringToOM("<ser:getURI " +
                                                    "xmlns:ser=\"http://services.get.uri.governance.carbon.wso2.org\"><ser:artifactId>" + artifactId + "</ser:artifactId></ser:getURI>"));


            options.setAction("urn:getURIArtifactIDs");
            client.sendReceive(AXIOMUtil.stringToOM("<ser:getURIArtifactIDs " +
                                                    "" +
                                                    "xmlns:ser=\"http://services.get.uri.artifactids.governance.carbon.wso2.org\"/>"));


            options.setAction("urn:getURIDependencies");
            OMElement omElementGetWsdlDependencies = client.sendReceive(AXIOMUtil.stringToOM
                    ("<ser:getURIDependencies" +
                     " " +
                     "xmlns:ser=\"http://services.get.uri.dependencies.governance.carbon.wso2.org\"><ser:artifactId>" + artifactId + "</ser:artifactId></ser:getURIDependencies>"));


            expression = new AXIOMXPath("//ns:return");
            expression.addNamespace("ns", omElementGetWsdlDependencies.getNamespace().getNamespaceURI());


            options.setAction("urn:deleteURI");
            client.setOptions(options);
            OMElement omElementDeleteWsdl = client.sendReceive(AXIOMUtil.stringToOM("<ser:deleteURI " +
                                                                                    "xmlns:ser=\"http://services" +
                                                                                    ".delete.uri.governance.carbon.wso2.org\"><ser:artifactId>" + artifactId + "</ser:artifactId></ser:deleteURI>"));
            assertEquals(omElementDeleteWsdl.toString(), "<ns:deleteURIResponse xmlns:ns=\"http://services.delete.uri.governance.carbon.wso2.org\"><ns:return>true</ns:return></ns:deleteURIResponse>");


            /*CRUD support for Schemas*/

            options.setAction("urn:addURI");
            options.setManageSession(true);
            OMElement omElementAddSchema = client.sendReceive(AXIOMUtil.stringToOM("<ser:addURI " +
                                                                                   "xmlns:ser=\"http://services.add.uri.governance.carbon.wso2.org\"><ser:info>&lt;metadata " +
                                                                                   "xmlns=\"http://www.wso2.org/governance/metadata\">&lt;overview>&lt;type>XSD&lt;/type>&lt;" +
                                                                                   "name>SchemaImportSample.xsd&lt;/name>&lt;uri>https://svn.wso2" +
                                                                                   ".org/repos/wso2/carbon/platform/trunk/platform-integration/clarity-tests/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/schema/SchemaImportSample.xsd&lt;/uri>&lt;/overview>&lt;/metadata></ser:info></ser:addURI>"));


            expression.addNamespace("ns", omElementAddSchema.getNamespace().getNamespaceURI());
            String schemaArtifactId = ((OMElement) expression.selectSingleNode(omElementAddSchema)).getText();


            String[] allUriGenericArtifactsSchema = artifactManager.getAllGenericArtifactIds();
            assertEquals(isGenericArtifactExists(allUriGenericArtifactsSchema, schemaArtifactId), true);


            options.setAction("urn:getURI");
            client.sendReceive(AXIOMUtil.stringToOM("<ser:getURI " +
                                                    "xmlns:ser=\"http://services.get.uri.governance.carbon.wso2.org\"><ser:artifactId>" + schemaArtifactId + "</ser:artifactId></ser:getURI>"));


            options.setAction("urn:getURIArtifactIDs");
            client.sendReceive(AXIOMUtil.stringToOM("<ser:getURIArtifactIDs" +
                                                    " " +
                                                    "" +
                                                    "xmlns:ser=\"http://services.get.uri.artifactids.governance.carbon.wso2.org\"/>"));


            options.setAction("urn:getURIDependencies");
            OMElement omElementGetSchemaDependencies = client.sendReceive(AXIOMUtil.stringToOM
                    ("<ser:getURIDependencies" +
                     " " +
                     "xmlns:ser=\"http://services.get.uri.dependencies.governance.carbon.wso2" +
                     ".org\"><ser:artifactId>" + schemaArtifactId + "</ser:artifactId></ser:getURIDependencies>"));


            expression = new AXIOMXPath("//ns:return");
            expression.addNamespace("ns", omElementGetSchemaDependencies.getNamespace().getNamespaceURI());


            options.setAction("urn:deleteURI");
            client.setOptions(options);
            client.sendReceive(AXIOMUtil.stringToOM("<ser:deleteURI " +
                                                    "xmlns:ser=\"http://services" +
                                                    ".delete.uri.governance.carbon.wso2.org\"><ser:artifactId>" + schemaArtifactId +
                                                    "</ser:artifactId></ser:deleteURI>"));
            assertEquals(omElementDeleteWsdl.toString(), "<ns:deleteURIResponse xmlns:ns=\"http://services.delete.uri.governance.carbon.wso2.org\"><ns:return>true</ns:return></ns:deleteURIResponse>");


            /*CRUD support for Policies*/

            options.setAction("urn:addURI");
            options.setManageSession(true);
            OMElement omElementAddPolicy = client.sendReceive(AXIOMUtil.stringToOM("<ser:addURI " +
                                                                                   "xmlns:ser=\"http://services.add.uri.governance.carbon.wso2.org\"><ser:info>&lt;metadata " +
                                                                                   "xmlns=\"http://www.wso2.org/governance/metadata\">&lt;overview>&lt;type>Policy&lt;/type>&lt;" +
                                                                                   "name>policy.xml&lt;/name>&lt;uri>https://svn.wso2" +
                                                                                   ".org/repos/wso2/carbon/platform/trunk/platform-integration/clarity-tests/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/policy/policy.xml&lt;/uri>&lt;/overview>&lt;/metadata></ser:info></ser:addURI>"));


            expression.addNamespace("ns", omElementAddPolicy.getNamespace().getNamespaceURI());
            String policyArtifactId = ((OMElement) expression.selectSingleNode(omElementAddPolicy)).getText();


            String[] allUriGenericArtifactsPolicy = artifactManager.getAllGenericArtifactIds();
            assertEquals(isGenericArtifactExists(allUriGenericArtifactsPolicy, policyArtifactId), true);


            options.setAction("urn:getURI");
            client.sendReceive(AXIOMUtil.stringToOM("<ser:getURI " +
                                                    "xmlns:ser=\"http://services.get.uri.governance.carbon.wso2.org\"><ser:artifactId>" + policyArtifactId
                                                    + "</ser:artifactId></ser:getURI>"));


            options.setAction("urn:getURIArtifactIDs");
            client.sendReceive(AXIOMUtil.stringToOM("<ser:getURIArtifactIDs" +
                                                    " " +
                                                    "" +
                                                    "xmlns:ser=\"http://services.get.uri.artifactids.governance.carbon.wso2.org\"/>"));


            options.setAction("urn:getURIDependencies");
            client.sendReceive(AXIOMUtil.stringToOM
                    ("<ser:getURIDependencies" +
                     " " +
                     "xmlns:ser=\"http://services.get.uri.dependencies.governance.carbon.wso2" +
                     ".org\"><ser:artifactId>" + policyArtifactId + "</ser:artifactId></ser:getURIDependencies>"));


            expression = new AXIOMXPath("//ns:return");
            expression.addNamespace("ns", omElementGetSchemaDependencies.getNamespace().getNamespaceURI());

            options.setAction("urn:deleteURI");
            client.setOptions(options);
            client.sendReceive(AXIOMUtil.stringToOM("<ser:deleteURI " +
                                                    "xmlns:ser=\"http://services" +
                                                    ".delete.uri.governance.carbon.wso2.org\"><ser:artifactId>" + policyArtifactId +
                                                    "</ser:artifactId></ser:deleteURI>"));
            assertEquals(omElementDeleteWsdl.toString(), "<ns:deleteURIResponse xmlns:ns=\"http://services.delete.uri.governance.carbon.wso2.org\"><ns:return>true</ns:return></ns:deleteURIResponse>");


            /*CRUD support for Generic Artifacts*/

            options.setAction("urn:addURI");
            options.setManageSession(true);
            OMElement omElementAddGeneric = client.sendReceive(AXIOMUtil.stringToOM("<ser:addURI " +
                                                                                    "xmlns:ser=\"http://services.add.uri.governance.carbon.wso2.org\"><ser:info>&lt;metadata " +
                                                                                    "xmlns=\"http://www.wso2.org/governance/metadata\">&lt;overview>&lt;type>Generic&lt;/type>&lt;" +
                                                                                    "name>resource.txtl&lt;/name>&lt;uri>https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/clarity-tests/org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/resource.txt&lt;/uri>&lt;/overview>&lt;/metadata></ser:info></ser:addURI>"));


            expression.addNamespace("ns", omElementAddGeneric.getNamespace().getNamespaceURI());
            String genericArtifactId = ((OMElement) expression.selectSingleNode(omElementAddGeneric)).getText();


            String[] allUriGenericArtifactsGeneric = artifactManager.getAllGenericArtifactIds();
            assertEquals(isGenericArtifactExists(allUriGenericArtifactsGeneric, genericArtifactId), true);


            options.setAction("urn:getURI");
            client.sendReceive(AXIOMUtil.stringToOM("<ser:getURI " +
                                                    "xmlns:ser=\"http://services.get.uri.governance.carbon.wso2.org\"><ser:artifactId>" + genericArtifactId
                                                    + "</ser:artifactId></ser:getURI>"));


            options.setAction("urn:getURIArtifactIDs");
            client.sendReceive(AXIOMUtil.stringToOM
                    ("<ser:getURIArtifactIDs" +
                     " " +
                     "" +
                     "xmlns:ser=\"http://services.get.uri.artifactids.governance.carbon.wso2.org\"/>"));


            options.setAction("urn:getURIDependencies");
            client.sendReceive(AXIOMUtil.stringToOM
                    ("<ser:getURIDependencies" +
                     " " +
                     "xmlns:ser=\"http://services.get.uri.dependencies.governance.carbon.wso2" +
                     ".org\"><ser:artifactId>" + genericArtifactId + "</ser:artifactId></ser:getURIDependencies>"));


            expression = new AXIOMXPath("//ns:return");
            expression.addNamespace("ns", omElementGetSchemaDependencies.getNamespace().getNamespaceURI());


            options.setAction("urn:deleteURI");
            client.setOptions(options);
            client.sendReceive(AXIOMUtil.stringToOM("<ser:deleteURI " +
                                                    "xmlns:ser=\"http://services" +
                                                    ".delete.uri.governance.carbon.wso2.org\"><ser:artifactId>" + genericArtifactId +
                                                    "</ser:artifactId></ser:deleteURI>"));
            assertEquals(omElementDeleteWsdl.toString(), "<ns:deleteURIResponse xmlns:ns=\"http://services.delete.uri.governance.carbon.wso2.org\"><ns:return>true</ns:return></ns:deleteURIResponse>");
            String[] allGenericArtifacts = artifactManager.getAllGenericArtifactIds();
            for (String genericArtifacts : allGenericArtifacts) {
                artifactManager.removeGenericArtifact(genericArtifacts);

            }

        } else {
            System.out.println("Not logged in");
        }
    }

    public boolean isGenericArtifactExists(String[] allUriGenericArtifacts, String artifactId) {

        for (String uriArtifacts : allUriGenericArtifacts) {
            if (uriArtifacts.equals(artifactId)) {
                return true;
            }

        }
        return false;
    }
}
