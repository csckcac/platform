/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.automation.common.test.greg.governance;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlFilter;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import javax.xml.namespace.QName;
import java.util.List;

import static junit.framework.Assert.assertTrue;

public class WSDLTestCaseClient {

    private Registry governance;
    private static final Log log = LogFactory.getLog(WSDLTestCaseClient.class);

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        int userId = new GregUserIDEvaluator().getTenantID();
        WSRegistryServiceClient registryWS = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProvider().getGovernance(registryWS, userId);
    }

    @Test(groups = {"wso2.greg"})
    public void testAddWSDL() throws Exception {
        log.info("############## testAddWSDL started ...###################");
        WsdlManager wsdlManager = new WsdlManager(governance);

        Wsdl wsdl = wsdlManager.newWsdl("http://svn.wso2.org/repos/wso2/trunk/graphite/components/" +
                "governance/org.wso2.carbon.governance.api/src/test/resources/" +
                "test-resources/wsdl/BizService.wsdl");
        wsdl.addAttribute("creator", "it is me");
        wsdl.addAttribute("version", "0.01");
        wsdlManager.addWsdl(wsdl);
        doSleep();
        Wsdl newWsdl = wsdlManager.getWsdl(wsdl.getId());
        Assert.assertEquals(newWsdl.getWsdlElement().toString(), wsdl.getWsdlElement().toString());
        Assert.assertEquals(newWsdl.getAttribute("creator"), "it is me");
        Assert.assertEquals(newWsdl.getAttribute("version"), "0.01");

        // change the target namespace and check
        String oldWSDLPath = newWsdl.getPath();
        Assert.assertEquals(oldWSDLPath, "/trunk/wsdls/com/foo/BizService.wsdl");
        Assert.assertTrue(governance.resourceExists("/trunk/wsdls/com/foo/BizService.wsdl"));

        OMElement wsdlElement = newWsdl.getWsdlElement();
        wsdlElement.addAttribute("targetNamespace", "http://ww2.wso2.org/test", null);
        wsdlElement.declareNamespace("http://ww2.wso2.org/test", "tns");
        wsdlManager.updateWsdl(newWsdl);
        doSleep();

        Assert.assertEquals(newWsdl.getPath(), "/trunk/wsdls/org/wso2/ww2/test/BizService.wsdl");
        //assertFalse(registry.resourceExists("/wsdls/http/foo/com/BizService.wsdl"));

        // doing an update without changing anything.
        wsdlManager.updateWsdl(newWsdl);
        doSleep();

        Assert.assertEquals(newWsdl.getPath(), "/trunk/wsdls/org/wso2/ww2/test/BizService.wsdl");
        Assert.assertEquals(newWsdl.getAttribute("version"), "0.01");

        newWsdl = wsdlManager.getWsdl(wsdl.getId());
        Assert.assertEquals(newWsdl.getAttribute("creator"), "it is me");
        Assert.assertEquals(newWsdl.getAttribute("version"), "0.01");

        // adding a new schema to the wsdl.
        wsdlElement = newWsdl.getWsdlElement();
        OMElement schemaElement = evaluateXPathToElement("//xsd:schema", wsdlElement);

        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMElement importElement = factory.createOMElement(
                new QName("http://www.w3.org/2001/XMLSchema", "import"));
        importElement.addAttribute("schemaLocation",
                "http://svn.wso2.org/repos/wso2/trunk/graphite/components/governance" +
                        "/org.wso2.carbon.governance.api/src/test/resources/test-resources/" +
                        "xsd/purchasing_dup.xsd", null);
        schemaElement.addChild(importElement);
        importElement.addAttribute("namespace", "http://bar.org/purchasing_dup", null);

        wsdlManager.updateWsdl(newWsdl);
        doSleep();

        Schema[] schemas = newWsdl.getAttachedSchemas();

        //test log
        log.info("####Schemas#####");
        for (Schema schema : schemas) {
            log.info("#####Schema:" + schemas[0].getId() + " schemaName" + schema.getQName().toString());
        }

        Assert.assertEquals(schemas[schemas.length - 1].getPath(), "/trunk/schemas/org/bar/purchasing_dup/purchasing_dup.xsd");


        Wsdl[] wsdls = wsdlManager.findWsdls(new WsdlFilter() {
            public boolean matches(Wsdl wsdl) throws GovernanceException {
                Schema[] schemas = wsdl.getAttachedSchemas();
                for (Schema schema : schemas) {
                    if (schema.getPath().equals("/trunk/schemas/org/bar/purchasing_dup/purchasing_dup.xsd")) {
                        log.info("###### Matching Schemas name" + schema.getQName().toString() + "  schemaID:" + schema.getId());
                        return true;
                    }
                }
                return false;
            }
        });
        log.info("WSDL len:" + wsdls.length);
        Assert.assertEquals(wsdls.length, 1);
        Assert.assertEquals(newWsdl.getId(), wsdls[0].getId());

        // deleting the wsdl
        wsdlManager.removeWsdl(newWsdl.getId());
        Wsdl deletedWsdl = wsdlManager.getWsdl(newWsdl.getId());
        doSleep();
        Assert.assertNull(deletedWsdl);

        // add again
        Wsdl anotherWsdl = wsdlManager.newWsdl("http://svn.wso2.org/repos/wso2/trunk/graphite/components" +
                "/governance/org.wso2.carbon.governance.api/src/test/resources" +
                "/test-resources/wsdl/BizService.wsdl");
        anotherWsdl.addAttribute("creator", "it is not me");
        anotherWsdl.addAttribute("version", "0.02");
        wsdlManager.addWsdl(anotherWsdl);

        // and delete the wsdl
        wsdlManager.removeWsdl(anotherWsdl.getId());
        Assert.assertNull(wsdlManager.getWsdl(anotherWsdl.getId()));

    }

    @Test(groups = {"wso2.greg"}, dependsOnMethods = {"testAddWSDL"})
    public void testEditWSDL() throws Exception {
        WsdlManager wsdlManager = new WsdlManager(governance);

        Wsdl wsdl = wsdlManager.newWsdl("http://svn.wso2.org/repos/wso2/trunk/graphite/components" +
                "/governance/org.wso2.carbon.governance.api/src/test/resources" +
                "/test-resources/wsdl/BizService.wsdl");
        wsdl.addAttribute("creator2", "it is me");
        wsdl.addAttribute("version2", "0.01");
        wsdlManager.addWsdl(wsdl);

        // now edit the wsdl
        OMElement contentElement = wsdl.getWsdlElement();
        OMElement addressElement = evaluateXPathToElement("//soap:address", contentElement);
        addressElement.addAttribute("location", "http://my-custom-endpoint/hoooo", null);
        wsdl.setWsdlElement(contentElement);

        // now do an update.
        wsdlManager.updateWsdl(wsdl);
        doSleep();
        // now get the wsdl and check the update is there.
        Wsdl wsdl2 = wsdlManager.getWsdl(wsdl.getId());
        Assert.assertEquals(wsdl2.getAttribute("creator2"), "it is me");
        Assert.assertEquals(wsdl2.getAttribute("version2"), "0.01");
        OMElement contentElement2 = wsdl.getWsdlElement();
        OMElement addressElement2 = evaluateXPathToElement("//soap:address", contentElement2);

        Assert.assertEquals(addressElement2.getAttributeValue(new QName("location")), "http://my-custom-endpoint/hoooo");
    }

    private static OMElement evaluateXPathToElement(String expression,
                                                    OMElement root) throws Exception {
        List<OMElement> nodes = GovernanceUtils.evaluateXPathToElements(expression, root);
        if (nodes == null || nodes.size() == 0) {
            return null;
        }
        return nodes.get(0);
    }

    private void doSleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {

        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test adding same wsdl twice")
    public void testAddWSDLTwice() throws GovernanceException {
        WsdlManager wsdlManager = new WsdlManager(governance);
        String sampleWsdlURL = "http://ws.strikeiron.com/donotcall2_5?WSDL";
        String wsdlName = "donotcall2_5.wsdl";
        boolean isWsdlFound = false;

        Wsdl[] wsdlList = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().equalsIgnoreCase(wsdlName)) {
                wsdlManager.removeWsdl(w.getId());
            }
        }

        Wsdl wsdl = wsdlManager.newWsdl(sampleWsdlURL);
        wsdlManager.addWsdl(wsdl);
        try {
            wsdlManager.addWsdl(wsdl);
        } catch (GovernanceException e) {
            throw new GovernanceException("Error found while adding same wsdl twice : " + e.getMessage());
        }

        wsdlList = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().equalsIgnoreCase(wsdlName)) {
                if (!isWsdlFound) {
                    isWsdlFound = true;
                } else {
                    assertTrue("Same wsdl added twice", isWsdlFound);
                }

            }
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test adding wsdl which has policy import")
    public void testAddWsdWithPolicyImport() throws GovernanceException {
        String wsdlLocation = "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/" +
                "system-test-framework/core/org.wso2.automation.platform.core/src/main/resources/artifacts/" +
                "GREG/wsdl/wsdl_with_SigEncr.wsdl";
        boolean isWsdlFound = false;
        WsdlManager wsdlManager = new WsdlManager(governance);
        try {
            Wsdl wsdl = wsdlManager.newWsdl(wsdlLocation);
            wsdlManager.addWsdl(wsdl);
        } catch (GovernanceException e) {
            throw new GovernanceException("Exception thrown while adding wsdl which has policy import : " + e.getMessage());
        }

        Wsdl[] wsdlList = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().equalsIgnoreCase("wsdl_with_SigEncr.wsdl")) {
                isWsdlFound = true;
            }
        }
        assertTrue("Wsdl not get added which has policy import", isWsdlFound);
    }

    @Test(groups = {"wso2.greg"}, description = "Test adding wsdl which has inline policy and schema")
    public void testWsdlWithInlinePolicyAndSchema() throws GovernanceException {
        WsdlManager wsdlManager = new WsdlManager(governance);
        boolean isWsdlFound = false;
        Wsdl wsdl;
        try {
            wsdl = wsdlManager.newWsdl("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/" +
                    "system-test-framework/core/org.wso2.automation.platform.core/src/main/resources/artifacts/" +
                    "GREG/wsdl/WithInlinePolicyAndSchema.wsdl");
            wsdlManager.addWsdl(wsdl);
        } catch (GovernanceException e) {
            throw new GovernanceException("Exception thrown while adding wsdl which has inline policy " +
                    "amd schema : " + e.getMessage());
        }
        Wsdl[] wsdlList = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().equalsIgnoreCase("WithInlinePolicyAndSchema.wsdl")) {
                isWsdlFound = true;
            }
        }
        assertTrue("Wsdl not get added which has inline policy and schema", isWsdlFound);
    }

    @Test(groups = {"wso2.greg"}, description = "Test adding multiple wsdl")
    public void testMultipleWsdl() throws GovernanceException {
        Wsdl wsdl;
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl[] wsdlList = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().contains("Automated")) {
                wsdlManager.removeWsdl(w.getId());
            }
        }
        String wsdlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wsdl:definitions xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" xmlns:ns1=\"http://org.apache.axis2/xsd\" xmlns:ns=\"http://echo.services.core.carbon.wso2.org\" xmlns:wsaw=\"http://www.w3.org/2006/05/addressing/wsdl\" xmlns:http1=\"http://schemas.xmlsoap.org/wsdl/http/\" xmlns:ax21=\"http://echo.services.core.carbon.wso2.org/xsd\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:mime=\"http://schemas.xmlsoap.org/wsdl/mime/\" xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:soap12=\"http://schemas.xmlsoap.org/wsdl/soap12/\" targetNamespace=\"http://echo.services.core.carbon.wso2.org\">\n" +
                "    <wsdl:documentation>echo</wsdl:documentation>\n" +
                "    <wsdl:types>\n" +
                "        <xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" targetNamespace=\"http://echo.services.core.carbon.wso2.org/xsd\">\n" +
                "            <xs:complexType name=\"SimpleBean\">\n" +
                "                <xs:sequence>\n" +
                "                    <xs:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"a_r\" nillable=\"true\" type=\"xs:int\"/>\n" +
                "                    <xs:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"b_r\" nillable=\"true\" type=\"xs:int\"/>\n" +
                "                    <xs:element minOccurs=\"0\" name=\"c\" type=\"xs:int\"/>\n" +
                "                </xs:sequence>\n" +
                "            </xs:complexType>\n" +
                "        </xs:schema>\n" +
                "        <xs:schema xmlns:ax22=\"http://echo.services.core.carbon.wso2.org/xsd\"   targetNamespace=\"http://echo.services.core.carbon.wso2.org\">\n" +
                "            <xs:import namespace=\"http://echo.services.core.carbon.wso2.org/xsd\"/>\n" +
                "            <xs:complexType name=\"Exception\">\n" +
                "                <xs:sequence>\n" +
                "                    <xs:element minOccurs=\"0\" name=\"Exception\" nillable=\"true\" type=\"xs:string\"/>\n" +
                "                </xs:sequence>\n" +
                "            </xs:complexType>\n" +
                "            <xs:element name=\"throwAxisFaultResponse\">\n" +
                "                <xs:complexType>\n" +
                "                    <xs:sequence>\n" +
                "                        <xs:element minOccurs=\"0\" name=\"return\" nillable=\"true\" type=\"xs:string\"/>\n" +
                "                    </xs:sequence>\n" +
                "                </xs:complexType>\n" +
                "            </xs:element>\n" +
                "            <xs:element name=\"echoStringArrays\">\n" +
                "                <xs:complexType>\n" +
                "                    <xs:sequence>\n" +
                "                        <xs:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"a\" nillable=\"true\" type=\"xs:string\"/>\n" +
                "                        <xs:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"b\" nillable=\"true\" type=\"xs:string\"/>\n" +
                "                        <xs:element minOccurs=\"0\" name=\"c\" type=\"xs:int\"/>\n" +
                "                    </xs:sequence>\n" +
                "                </xs:complexType>\n" +
                "            </xs:element>\n" +
                "            <xs:element name=\"echoStringArraysResponse\">\n" +
                "                <xs:complexType>\n" +
                "                    <xs:sequence>\n" +
                "                        <xs:element minOccurs=\"0\" name=\"return\" nillable=\"true\" type=\"ax21:SimpleBean\"/>\n" +
                "                    </xs:sequence>\n" +
                "                </xs:complexType>\n" +
                "            </xs:element>\n" +
                "            <xs:element name=\"echoString\">\n" +
                "                <xs:complexType>\n" +
                "                    <xs:sequence>\n" +
                "                        <xs:element minOccurs=\"0\" name=\"in\" nillable=\"true\" type=\"xs:string\"/>\n" +
                "                    </xs:sequence>\n" +
                "                </xs:complexType>\n" +
                "            </xs:element>\n" +
                "            <xs:element name=\"echoStringResponse\">\n" +
                "                <xs:complexType>\n" +
                "                    <xs:sequence>\n" +
                "                        <xs:element minOccurs=\"0\" name=\"return\" nillable=\"true\" type=\"xs:string\"/>\n" +
                "                    </xs:sequence>\n" +
                "                </xs:complexType>\n" +
                "            </xs:element>\n" +
                "            <xs:element name=\"echoOMElement\">\n" +
                "                <xs:complexType>\n" +
                "                    <xs:sequence>\n" +
                "                        <xs:element minOccurs=\"0\" name=\"omEle\" nillable=\"true\" type=\"xs:anyType\"/>\n" +
                "                    </xs:sequence>\n" +
                "                </xs:complexType>\n" +
                "            </xs:element>\n" +
                "            <xs:element name=\"echoOMElementResponse\">\n" +
                "                <xs:complexType>\n" +
                "                    <xs:sequence>\n" +
                "                        <xs:element minOccurs=\"0\" name=\"return\" nillable=\"true\" type=\"xs:anyType\"/>\n" +
                "                    </xs:sequence>\n" +
                "                </xs:complexType>\n" +
                "            </xs:element>\n" +
                "            <xs:element name=\"echoInt\">\n" +
                "                <xs:complexType>\n" +
                "                    <xs:sequence>\n" +
                "                        <xs:element minOccurs=\"0\" name=\"in\" type=\"xs:int\"/>\n" +
                "                    </xs:sequence>\n" +
                "                </xs:complexType>\n" +
                "            </xs:element>\n" +
                "            <xs:element name=\"echoIntResponse\">\n" +
                "                <xs:complexType>\n" +
                "                    <xs:sequence>\n" +
                "                        <xs:element minOccurs=\"0\" name=\"return\" type=\"xs:int\"/>\n" +
                "                    </xs:sequence>\n" +
                "                </xs:complexType>\n" +
                "            </xs:element>\n" +
                "        </xs:schema>\n" +
                "    </wsdl:types>\n" +
                "    <wsdl:message name=\"echoStringArraysRequest\">\n" +
                "        <wsdl:part name=\"parameters\" element=\"ns:echoStringArrays\"/>\n" +
                "    </wsdl:message>\n" +
                "    <wsdl:message name=\"echoStringArraysResponse\">\n" +
                "        <wsdl:part name=\"parameters\" element=\"ns:echoStringArraysResponse\"/>\n" +
                "    </wsdl:message>\n" +
                "    <wsdl:message name=\"echoOMElementRequest\">\n" +
                "        <wsdl:part name=\"parameters\" element=\"ns:echoOMElement\"/>\n" +
                "    </wsdl:message>\n" +
                "    <wsdl:message name=\"echoOMElementResponse\">\n" +
                "        <wsdl:part name=\"parameters\" element=\"ns:echoOMElementResponse\"/>\n" +
                "    </wsdl:message>\n" +
                "    <wsdl:message name=\"echoIntRequest\">\n" +
                "        <wsdl:part name=\"parameters\" element=\"ns:echoInt\"/>\n" +
                "    </wsdl:message>\n" +
                "    <wsdl:message name=\"echoIntResponse\">\n" +
                "        <wsdl:part name=\"parameters\" element=\"ns:echoIntResponse\"/>\n" +
                "    </wsdl:message>\n" +
                "    <wsdl:message name=\"throwAxisFaultRequest\"/>\n" +
                "    <wsdl:message name=\"throwAxisFaultResponse\">\n" +
                "        <wsdl:part name=\"parameters\" element=\"ns:throwAxisFaultResponse\"/>\n" +
                "    </wsdl:message>\n" +
                "    <wsdl:message name=\"echoStringRequest\">\n" +
                "        <wsdl:part name=\"parameters\" element=\"ns:echoString\"/>\n" +
                "    </wsdl:message>\n" +
                "    <wsdl:message name=\"echoStringResponse\">\n" +
                "        <wsdl:part name=\"parameters\" element=\"ns:echoStringResponse\"/>\n" +
                "    </wsdl:message>\n" +
                "    <wsdl:portType name=\"echoPortType\">\n" +
                "        <wsdl:operation name=\"Echo-StringArrays\">\n" +
                "            <wsdl:input message=\"ns:echoStringArraysRequest\" wsaw:Action=\"urn:echoStringArrays\"/>\n" +
                "            <wsdl:output message=\"ns:echoStringArraysResponse\" wsaw:Action=\"urn:echoStringArraysResponse\"/>\n" +
                "        </wsdl:operation>\n" +
                "        <wsdl:operation name=\"echo$OMElement\">\n" +
                "            <wsdl:input message=\"ns:echoOMElementRequest\" wsaw:Action=\"urn:echoOMElement\"/>\n" +
                "            <wsdl:output message=\"ns:echoOMElementResponse\" wsaw:Action=\"urn:echoOMElementResponse\"/>\n" +
                "        </wsdl:operation>\n" +
                "        <wsdl:operation name=\"echoInt\">\n" +
                "            <wsdl:input message=\"ns:echoIntRequest\" wsaw:Action=\"urn:echoInt\"/>\n" +
                "            <wsdl:output message=\"ns:echoIntResponse\" wsaw:Action=\"urn:echoIntResponse\"/>\n" +
                "        </wsdl:operation>\n" +
                "        <wsdl:operation name=\"throwAxisFault\">\n" +
                "            <wsdl:input message=\"ns:throwAxisFaultRequest\" wsaw:Action=\"urn:throwAxisFault\"/>\n" +
                "            <wsdl:output message=\"ns:throwAxisFaultResponse\" wsaw:Action=\"urn:throwAxisFaultResponse\"/>\n" +
                "        </wsdl:operation>\n" +
                "        <wsdl:operation name=\"echoString\">\n" +
                "            <wsdl:input message=\"ns:echoStringRequest\" wsaw:Action=\"urn:echoString\"/>\n" +
                "            <wsdl:output message=\"ns:echoStringResponse\" wsaw:Action=\"urn:echoStringResponse\"/>\n" +
                "        </wsdl:operation>\n" +
                "    </wsdl:portType>\n" +
                "    <wsdl:binding name=\"echoSoap11Binding\" type=\"ns:echoPortType\">\n" +
                "        <soap:binding transport=\"http://schemas.xmlsoap.org/soap/http\" style=\"document\"/>\n" +
                "        <wsdl:operation name=\"EchoStringArrays\">\n" +
                "            <soap:operation soapAction=\"urn:EchoStringArrays\" style=\"document\"/>\n" +
                "            <wsdl:input>\n" +
                "                <soap:body use=\"literal\"/>\n" +
                "            </wsdl:input>\n" +
                "            <wsdl:output>\n" +
                "                <soap:body use=\"literal\"/>\n" +
                "            </wsdl:output>\n" +
                "        </wsdl:operation>\n" +
                "        <wsdl:operation name=\"echoOMElement\">\n" +
                "            <soap:operation soapAction=\"urn:echoOMElement\" style=\"document\"/>\n" +
                "            <wsdl:input>\n" +
                "                <soap:body use=\"literal\"/>\n" +
                "            </wsdl:input>\n" +
                "            <wsdl:output>\n" +
                "                <soap:body use=\"literal\"/>\n" +
                "            </wsdl:output>\n" +
                "        </wsdl:operation>\n" +
                "        <wsdl:operation name=\"echoInt\">\n" +
                "            <soap:operation soapAction=\"urn:echoInt\" style=\"document\"/>\n" +
                "            <wsdl:input>\n" +
                "                <soap:body use=\"literal\"/>\n" +
                "            </wsdl:input>\n" +
                "            <wsdl:output>\n" +
                "                <soap:body use=\"literal\"/>\n" +
                "            </wsdl:output>\n" +
                "        </wsdl:operation>\n" +
                "        <wsdl:operation name=\"throwAxisFault\">\n" +
                "            <soap:operation soapAction=\"urn:throwAxisFault\" style=\"document\"/>\n" +
                "            <wsdl:input>\n" +
                "                <soap:body use=\"literal\"/>\n" +
                "            </wsdl:input>\n" +
                "            <wsdl:output>\n" +
                "                <soap:body use=\"literal\"/>\n" +
                "            </wsdl:output>\n" +
                "        </wsdl:operation>\n" +
                "        <wsdl:operation name=\"echoString\">\n" +
                "            <soap:operation soapAction=\"urn:echoString\" style=\"document\"/>\n" +
                "            <wsdl:input>\n" +
                "                <soap:body use=\"literal\"/>\n" +
                "            </wsdl:input>\n" +
                "            <wsdl:output>\n" +
                "                <soap:body use=\"literal\"/>\n" +
                "            </wsdl:output>\n" +
                "        </wsdl:operation>\n" +
                "    </wsdl:binding>\n" +
                "    <wsdl:binding name=\"echoSoap12Binding\" type=\"ns:echoPortType\">\n" +
                "        <soap12:binding transport=\"http://schemas.xmlsoap.org/soap/http\" style=\"document\"/>\n" +
                "        <wsdl:operation name=\"echoStringArrays\">\n" +
                "            <soap12:operation soapAction=\"urn:echoStringArrays\" style=\"document\"/>\n" +
                "            <wsdl:input>\n" +
                "                <soap12:body use=\"literal\"/>\n" +
                "            </wsdl:input>\n" +
                "            <wsdl:output>\n" +
                "                <soap12:body use=\"literal\"/>\n" +
                "            </wsdl:output>\n" +
                "        </wsdl:operation>\n" +
                "        <wsdl:operation name=\"echoOMElement\">\n" +
                "            <soap12:operation soapAction=\"urn:echoOMElement\" style=\"document\"/>\n" +
                "            <wsdl:input>\n" +
                "                <soap12:body use=\"literal\"/>\n" +
                "            </wsdl:input>\n" +
                "            <wsdl:output>\n" +
                "                <soap12:body use=\"literal\"/>\n" +
                "            </wsdl:output>\n" +
                "        </wsdl:operation>\n" +
                "        <wsdl:operation name=\"echoInt\">\n" +
                "            <soap12:operation soapAction=\"urn:echoInt\" style=\"document\"/>\n" +
                "            <wsdl:input>\n" +
                "                <soap12:body use=\"literal\"/>\n" +
                "            </wsdl:input>\n" +
                "            <wsdl:output>\n" +
                "                <soap12:body use=\"literal\"/>\n" +
                "            </wsdl:output>\n" +
                "        </wsdl:operation>\n" +
                "        <wsdl:operation name=\"throwAxisFault\">\n" +
                "            <soap12:operation soapAction=\"urn:throwAxisFault\" style=\"document\"/>\n" +
                "            <wsdl:input>\n" +
                "                <soap12:body use=\"literal\"/>\n" +
                "            </wsdl:input>\n" +
                "            <wsdl:output>\n" +
                "                <soap12:body use=\"literal\"/>\n" +
                "            </wsdl:output>\n" +
                "        </wsdl:operation>\n" +
                "        <wsdl:operation name=\"echoString\">\n" +
                "            <soap12:operation soapAction=\"urn:echoString\" style=\"document\"/>\n" +
                "            <wsdl:input>\n" +
                "                <soap12:body use=\"literal\"/>\n" +
                "            </wsdl:input>\n" +
                "            <wsdl:output>\n" +
                "                <soap12:body use=\"literal\"/>\n" +
                "            </wsdl:output>\n" +
                "        </wsdl:operation>\n" +
                "    </wsdl:binding>\n" +
                "    <wsdl:binding name=\"echoHttpBinding\" type=\"ns:echoPortType\">\n" +
                "        <http1:binding verb=\"POST\"/>\n" +
                "        <wsdl:operation name=\"echoStringArrays\">\n" +
                "            <http1:operation location=\"echoStringArrays\"/>\n" +
                "            <wsdl:input>\n" +
                "                <mime:content type=\"text/xml\" part=\"echoStringArrays\"/>\n" +
                "            </wsdl:input>\n" +
                "            <wsdl:output>\n" +
                "                <mime:content type=\"text/xml\" part=\"echoStringArrays\"/>\n" +
                "            </wsdl:output>\n" +
                "        </wsdl:operation>\n" +
                "        <wsdl:operation name=\"echoOMElement\">\n" +
                "            <http1:operation location=\"echoOMElement\"/>\n" +
                "            <wsdl:input>\n" +
                "                <mime:content type=\"text/xml\" part=\"echoOMElement\"/>\n" +
                "            </wsdl:input>\n" +
                "            <wsdl:output>\n" +
                "                <mime:content type=\"text/xml\" part=\"echoOMElement\"/>\n" +
                "            </wsdl:output>\n" +
                "        </wsdl:operation>\n" +
                "        <wsdl:operation name=\"echoInt\">\n" +
                "            <http1:operation location=\"echoInt\"/>\n" +
                "            <wsdl:input>\n" +
                "                <mime:content type=\"text/xml\" part=\"echoInt\"/>\n" +
                "            </wsdl:input>\n" +
                "            <wsdl:output>\n" +
                "                <mime:content type=\"text/xml\" part=\"echoInt\"/>\n" +
                "            </wsdl:output>\n" +
                "        </wsdl:operation>\n" +
                "        <wsdl:operation name=\"throwAxisFault\">\n" +
                "            <http1:operation location=\"throwAxisFault\"/>\n" +
                "            <wsdl:input>\n" +
                "                <mime:content type=\"text/xml\" part=\"throwAxisFault\"/>\n" +
                "            </wsdl:input>\n" +
                "            <wsdl:output>\n" +
                "                <mime:content type=\"text/xml\" part=\"throwAxisFault\"/>\n" +
                "            </wsdl:output>\n" +
                "        </wsdl:operation>\n" +
                "        <wsdl:operation name=\"echoString\">\n" +
                "            <http1:operation location=\"echoString\"/>\n" +
                "            <wsdl:input>\n" +
                "                <mime:content type=\"text/xml\" part=\"echoString\"/>\n" +
                "            </wsdl:input>\n" +
                "            <wsdl:output>\n" +
                "                <mime:content type=\"text/xml\" part=\"echoString\"/>\n" +
                "            </wsdl:output>\n" +
                "        </wsdl:operation>\n" +
                "    </wsdl:binding>\n" +
                "    <wsdl:service name=\"echoyuSer1\">\n" +
                "        <wsdl:port name=\"echoHttpsSoap11Endpoint\" binding=\"ns:echoSoap11Binding\">\n" +
                "            <soap:address location=\"https://localhost:8243/services/echo-yu.echoHttpsSoap11Endpoint\"/>\n" +
                "        </wsdl:port>\n" +
                "        <wsdl:port name=\"echoHttpSoap11Endpoint\" binding=\"ns:echoSoap11Binding\">\n" +
                "            <soap:address location=\"http://localhost:8280/services/echo-yu.echoHttpSoap11Endpoint\"/>\n" +
                "        </wsdl:port>\n" +
                "        <wsdl:port name=\"echoHttpSoap12Endpoint\" binding=\"ns:echoSoap12Binding\">\n" +
                "            <soap12:address location=\"http://localhost:8280/services/echo-yu.echoHttpSoap12Endpoint\"/>\n" +
                "        </wsdl:port>\n" +
                "        <wsdl:port name=\"echoHttpsSoap12Endpoint\" binding=\"ns:echoSoap12Binding\">\n" +
                "            <soap12:address location=\"https://localhost:8243/services/echo-yu.echoHttpsSoap12Endpoint\"/>\n" +
                "        </wsdl:port>\n" +
                "        <wsdl:port name=\"echoHttpEndpoint\" binding=\"ns:echoHttpBinding\">\n" +
                "            <http1:address location=\"http://localhost:8280/services/echo-yu.echoHttpEndpoint\"/>\n" +
                "        </wsdl:port>\n" +
                "        <wsdl:port name=\"echoHttpsEndpoint\" binding=\"ns:echoHttpBinding\">\n" +
                "            <http1:address location=\"https://localhost:8243/services/echo-yu.echoHttpsEndpoint\"/>\n" +
                "        </wsdl:port>\n" +
                "    </wsdl:service>\n" +
                "</wsdl:definitions>\n";
        try {
            for (int i = 0; i <= 10000; i++) {
                wsdl = wsdlManager.newWsdl(wsdlContent.getBytes(), "AutomatedWsdl" + i + ".wsdl");
                wsdlManager.addWsdl(wsdl);
                System.out.println("Adding : AutomatedWsdl" + i + ".wsdl");
                if (!wsdlManager.getWsdl(wsdl.getId()).getQName().getLocalPart().equalsIgnoreCase("AutomatedWsdl" + i + ".wsdl")) {
                    assertTrue("Wsdl not added..", false);
                }
            }
        } catch (GovernanceException e) {
            throw new GovernanceException("Error found while adding multiple Wsdl : " + e.getMessage());
        }
    }
}
