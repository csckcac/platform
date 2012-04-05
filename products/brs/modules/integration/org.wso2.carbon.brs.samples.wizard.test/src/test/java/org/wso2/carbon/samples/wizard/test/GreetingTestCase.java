/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.samples.wizard.test;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.rule.service.stub.wizard.RuleServiceAdminStub;
import org.wso2.carbon.rule.service.stub.wizard.RuleServiceManagementException;
import org.wso2.carbon.rulecep.commons.descriptions.AXIOMXPathSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.OMNamespaceFactory;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.service.RuleServiceExtensionDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.service.RuleServiceExtensionSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensionSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.service.OperationDescription;
import org.wso2.carbon.rulecep.commons.descriptions.service.ServiceDescription;
import org.wso2.carbon.rulecep.commons.descriptions.service.ServiceDescriptionSerializer;
import org.wso2.carbon.samples.wizard.test.ns.NameSpacesFactory;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;

public class GreetingTestCase {
    private static String EMPTY_STRING = "";
    private static final ExtensionSerializer CONFIGURATION_EXTENSION_SERIALIZER = new RuleServiceExtensionSerializer();
    private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();
    private static final OMNamespace NULL_NS = OM_FACTORY.createOMNamespace("", "");
    public static final QName ATT_GENERATE_SERVICES_XML = new QName("", "generateServicesXML");
    public static final String RULE_SERVICE_ARCHIVE_EXTENSION = "aar";
    public static final String DEFAULT_WRAPPER_NAME = "result";
    private static final Log log = LogFactory.getLog(GreetingTestCase.class);

    private LoginLogoutUtil util = new LoginLogoutUtil();
    private RuleServiceAdminStub ruleServiceAdminStub;

    @BeforeMethod(groups = {"wso2.brs"})
    public void login() throws java.lang.Exception {
        ClientConnectionUtil.waitForPort(9443);
        String loggedInSessionCookie = util.login();
        ruleServiceAdminStub =
                new RuleServiceAdminStub("https://localhost:9443/services/RuleServiceAdmin");
        ServiceClient client = ruleServiceAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                            loggedInSessionCookie);
    }

    @Test(groups = {"wso2.brs"})
    public void init() {
        System.out.println("Initializing the test");

        // Creating greeting rule service
        ServiceDescription ruleServiceDescription = new ServiceDescription();
        ruleServiceDescription.setName("greetingService");
        ruleServiceDescription.setDescription("This is a test greeting service");
        ruleServiceDescription.setTargetNamespace("http://brs.carbon.wso2.org");


        RuleServiceExtensionDescription extensionDescription = new RuleServiceExtensionDescription();
        RuleSetDescription ruleSetDescription = new RuleSetDescription();
        extensionDescription.setRuleSetDescription(ruleSetDescription);
        ruleServiceDescription.setServiceExtensionDescription(extensionDescription);

        String ruleSource = "package greeting\n" +
                            "\n" +
                            "import samples.greeting.GreetingMessage;\n" +
                            "import samples.greeting.User;\n" +
                            "\n" +
                            "rule \"Is Morning\" no-loop true\n" +
                            "when\n" +
                            "    user : User()\n" +
                            "    eval((6 < user.now()) && (user.now()< 12))\n" +
                            "then\n" +
                            "    GreetingMessage msg = new GreetingMessage();\n" +
                            "    msg.setMessage(\"Good Morning  \" + user.getName() + \" !!! \");\n" +
                            "    insertLogical(msg);\n" +
                            "end\n" +
                            "\n" +
                            "rule \"Is afternoon\" no-loop true\n" +
                            "when\n" +
                            "    user : User()\n" +
                            "    eval((12 <= user.now()) && (user.now() < 18))\n" +
                            "then\n" +
                            "    GreetingMessage msg = new GreetingMessage();\n" +
                            "    msg.setMessage(\"Good Afternoon  \" + user.getName() + \" !!! \");\n" +
                            "    insertLogical(msg);\n" +
                            "end\n" +
                            "\n" +
                            "rule \"Is Night\" no-loop true\n" +
                            "when\n" +
                            "    user : User()\n" +
                            "    eval( (18 <= user.now()) && (user.now() < 24))\n" +
                            "then\n" +
                            "    GreetingMessage msg = new GreetingMessage();\n" +
                            "    msg.setMessage(\"Good Night  \" + user.getName() + \" !!! \");\n" +
                            "    insertLogical(msg);\n" +
                            "end";
        ruleSetDescription.setRuleSource(ruleSource.trim());
        ruleSetDescription.setKey(EMPTY_STRING);
        ruleSetDescription.setPath(EMPTY_STRING);


        String samplesDir = System.getProperty("samples.dir");
        String greetingServiceAAR = samplesDir + File.separator + "Greeting.jar";

        FileDataSource fileDataSource = new FileDataSource(greetingServiceAAR);
        DataHandler dataHandler = new DataHandler(fileDataSource);


        try {
            String[] strings = ruleServiceAdminStub.uploadFacts("greetingService", "Greeting.jar", dataHandler);
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (RuleServiceManagementException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        ruleServiceDescription.setExtension(RULE_SERVICE_ARCHIVE_EXTENSION);


        String operationName = "greetMe";
        OperationDescription description =
                ruleServiceDescription.getRuleServiceOperationDescription(operationName);
        if (description == null) {
            description = new OperationDescription();
            description.setName(new QName(operationName));//TODO
            ruleServiceDescription.addRuleServiceOperationDescription(description);
        } else {
            description.clearFacts();
            description.clearResults();
        }

        NameSpacesFactory nameSpacesFactory = NameSpacesFactory.getInstance();

        String factCountParameter = "1";

        if (factCountParameter != null && !"".equals(factCountParameter)) {
            int factCount = 0;
            try {
                factCount = Integer.parseInt(factCountParameter.trim());

                for (int i = 0; i < factCount; i++) {
                    String name = "";
                    String type = "samples.greeting.User";

                    if (type != null && !"".equals(type)) {
                        ResourceDescription resourceDescription = new ResourceDescription();
                        description.addFactDescription(resourceDescription);
                        resourceDescription.addNameSpaces(
                                nameSpacesFactory.createNameSpaces("factValue" + i,
                                                                   operationName, null));
                        resourceDescription.setType(type.trim());
                        if (name != null && !"".equals(name)) {
                            resourceDescription.setName(name.trim());
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
                ignored.printStackTrace();
            }
        }

        String wrapperName = "result";
        if (wrapperName == null || "".equals(wrapperName)) {
            wrapperName = DEFAULT_WRAPPER_NAME;
        }

        ResourceDescription wrapperDescription = new ResourceDescription();
        wrapperDescription.setType("omelement");
        wrapperDescription.setName(wrapperName);
        String resultCountParameter = "1";

        if (resultCountParameter != null && !"".equals(resultCountParameter)) {
            int resultCount = 0;
            try {
                resultCount = Integer.parseInt(resultCountParameter.trim());

                for (int i = 0; i < resultCount; i++) {
                    String name = "";
                    String type = "samples.greeting.GreetingMessage";

                    if (type != null && !"".equals(type)) {
                        ResourceDescription resourceDescription = new ResourceDescription();
                        wrapperDescription.addChildResource(resourceDescription);
                        resourceDescription.addNameSpaces(nameSpacesFactory.createNameSpaces("resultValue" + i, operationName, null));
                        resourceDescription.setType(type.trim());
                        if (name != null && !"".equals(name)) {
                            resourceDescription.setName(name.trim());
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }
        if (wrapperDescription.hasChildren()) {
            description.addResultDescription(wrapperDescription);
        }


        String serviceName = ruleServiceDescription.getName();

        System.out.println("serviceName = " + serviceName);

        OMElement result =
                ServiceDescriptionSerializer.serializeToRuleServiceConfiguration(
                        ruleServiceDescription,
                        OMNamespaceFactory.getInstance().createOMNamespace(new QName(serviceName)),
                        new AXIOMXPathSerializer(), CONFIGURATION_EXTENSION_SERIALIZER);
        result.addAttribute(
                OM_FACTORY.createOMAttribute(ATT_GENERATE_SERVICES_XML.getLocalPart(),
                                             NULL_NS, String.valueOf(ruleServiceDescription.isContainsServicesXML())));
        if (ruleServiceDescription.isContainsServicesXML()) {
            ruleServiceDescription.setExtension(RULE_SERVICE_ARCHIVE_EXTENSION);
        }


        try {
            ruleServiceAdminStub.addRuleService(ruleServiceDescription.getExtension(),
                                                serviceName, result);
            System.out.println("successfully added the rule service\n\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(groups = {"wso2.brs"}, dependsOnMethods = {"init"}, enabled = false)
    public void invokeService() throws XMLStreamException, AxisFault {
        boolean invocationComplete = false;
        int tries = 0;
        OMElement result = null;
        while (!invocationComplete && tries <= 10) {
            try {
                ServiceClient serviceClient = new ServiceClient();
                Options options = new Options();
                options.setTo(new EndpointReference("http://localhost:9763/services/greetingService"));
                options.setAction("urn:greetMe");
                serviceClient.setOptions(options);
                result = serviceClient.sendReceive(createPayload());
                invocationComplete = true;
            } catch (Exception e) {
                tries++;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }
            }
        }
        assertNotNull(result, "Result cannot be null");
     }

    private OMElement createPayload() throws XMLStreamException {
        String request = "<p:greetMeRequest xmlns:p=\"http://brs.carbon.wso2.org\">" +
                         "  <p:User>" +
                         "      <xs:name xmlns:xs=\"http://greeting.samples/xsd\">shammi</xs:name>" +
                         "   </p:User>\n" +
                         "</p:greetMeRequest>";
        return new StAXOMBuilder(new ByteArrayInputStream(request.getBytes())).getDocumentElement();
    }

    private String computeSourcePath(String fileName) {
        String samplesDir = System.getProperty("samples.dir");
        return samplesDir + File.separator + fileName;
    }
}
