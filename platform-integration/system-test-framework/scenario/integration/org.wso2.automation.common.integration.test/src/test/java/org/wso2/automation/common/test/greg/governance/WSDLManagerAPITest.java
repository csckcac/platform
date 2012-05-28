package org.wso2.automation.common.test.greg.governance;/*
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

import org.apache.axis2.AxisFault;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlFilter;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import static org.testng.Assert.assertTrue;

/**
 * Class will test all API methods of WSDL manager
 */
public class WSDLManagerAPITest {

    public static WsdlManager wsdlManager;
    private static Wsdl wsdlObj;
    private static Wsdl[] wsdlArray;
    private String wsdlName = "donotcall2_5.wsdl";

    @BeforeClass(alwaysRun = true)
    public void initializeAPIObject() throws RegistryException, AxisFault {
        int userId = new GregUserIDEvaluator().getTenantID();
        WSRegistryServiceClient registryWS = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        Registry governance = new RegistryProvider().getGovernance(registryWS, userId);
        wsdlManager = new WsdlManager(governance);
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing newWsdl API method", priority = 1)
    public void testNewWsdl() throws GovernanceException {
        try {
            String sampleWsdlURL = "http://ws.strikeiron.com/donotcall2_5?WSDL";
            wsdlObj = wsdlManager.newWsdl(sampleWsdlURL);
            cleanWSDL();
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:newWsdl method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testNewWsdl"}, description = "Testing " +
            "addWsdl API method", priority = 2)
    public void testAddWsdl() throws GovernanceException {
        try {

            wsdlManager.addWsdl(wsdlObj);
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:addWsdl method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testAddWsdl"}, description = "Testing " +
            "getAllWsdls API method", priority = 3)
    public void testGetAllWsdl() throws GovernanceException {
        boolean isWsdlFound = false;
        try {
            wsdlArray = wsdlManager.getAllWsdls();
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:addWsdl method" + e);
        }
        for (Wsdl w : wsdlArray) {
            if (w.getQName().getLocalPart().equalsIgnoreCase(wsdlName)) {
                isWsdlFound = true;
            }
        }
        assertTrue(isWsdlFound, "Return object of getAllWsdls" +
                " method doesn't have all information ");
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testAddWsdl"}, description = "Testing " +
            "getWsdl API method", priority = 4)
    public void testGetWsdl() throws GovernanceException {
        Wsdl localWsdlObj = null;
        for (Wsdl w : wsdlArray) {
            if (w.getQName().getLocalPart().equalsIgnoreCase(wsdlName)) {
                try {
                    localWsdlObj = wsdlManager.getWsdl(w.getId());

                } catch (GovernanceException e) {
                    throw new GovernanceException("Error occurred while executing WsdlManager:getWsdl method" + e);
                }
            }
        }
        if (localWsdlObj != null) {
            assertTrue(localWsdlObj.getQName().getLocalPart().equalsIgnoreCase(wsdlName), "getWsdl method doesn't work");
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testAddWsdl"}, description = "Testing " +
            "getWsdl API method", priority = 5)
    public void testUpdateWsdl() throws GovernanceException {
        String lcName = "ServiceLifeCycle";
        boolean isLCFound = false;
        try {
            wsdlObj.attachLifecycle(lcName);
            wsdlManager.updateWsdl(wsdlObj);
            wsdlArray = wsdlManager.getAllWsdls();
            for (Wsdl w : wsdlArray) {
                if (w.getLifecycleName().equalsIgnoreCase(lcName)) {
                    isLCFound = true;
                }
            }
            assertTrue(isLCFound, "Error occurred while executing WsdlManager:updateWsdl method");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:updateWsdl method" + e);
        }

    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing FindWSDL", priority = 6)
    public void testFindService() throws GovernanceException {
        try {
            Wsdl[] wsdlArray = wsdlManager.findWsdls(new WsdlFilter() {
                public boolean matches(Wsdl wsdl) throws GovernanceException {
                    String name = wsdl.getQName().getLocalPart();
                    assertTrue(name.contains(wsdlName), "Error occured while executing findWSDL API method");
                    return name.contains(wsdlName);
                }
            }
            );
            assertTrue(wsdlArray.length > 0, "Error occured while executing findWSDL API method");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:findWsdls method" + e);
        }

    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing AddWSDL with Inline content", priority = 7)
    public void testAddWSDLContentWithName() throws GovernanceException {

        String wsdlContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<wsdl:definitions xmlns:http=\"http://schemas.xmlsoap.org/wsdl/http/\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:mime=\"http://schemas.xmlsoap.org/wsdl/mime/\" xmlns:si=\"http://www.strikeiron.com\" xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:s=\"http://www.w3.org/2001/XMLSchema\" xmlns:tm=\"http://microsoft.com/wsdl/mime/textMatching/\" targetNamespace=\"http://www.strikeiron.com\" xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\">\n" +
                "  <wsdl:types>\n" +
                "    <s:schema elementFormDefault=\"qualified\" targetNamespace=\"http://www.strikeiron.com\">\n" +
                "      <s:element name=\"CheckNumberBatch\">\n" +
                "        <s:complexType>\n" +
                "          <s:sequence>\n" +
                "            <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"phoneNumbers\" type=\"si:ArrayOfString\" />\n" +
                "          </s:sequence>\n" +
                "        </s:complexType>\n" +
                "      </s:element>\n" +
                "      <s:complexType name=\"ArrayOfString\">\n" +
                "        <s:sequence>\n" +
                "          <s:element minOccurs=\"0\" maxOccurs=\"unbounded\" name=\"phoneNumber\" nillable=\"true\" type=\"s:string\" />\n" +
                "        </s:sequence>\n" +
                "      </s:complexType>\n" +
                "      <s:element name=\"CheckNumberBatchResponse\">\n" +
                "        <s:complexType>\n" +
                "          <s:sequence>\n" +
                "            <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"CheckNumberBatchResult\" type=\"si:ArrayOfNumberData\" />\n" +
                "          </s:sequence>\n" +
                "        </s:complexType>\n" +
                "      </s:element>\n" +
                "      <s:complexType name=\"ArrayOfNumberData\">\n" +
                "        <s:sequence>\n" +
                "          <s:element minOccurs=\"0\" maxOccurs=\"unbounded\" name=\"NumberData\" nillable=\"true\" type=\"si:NumberData\" />\n" +
                "        </s:sequence>\n" +
                "      </s:complexType>\n" +
                "      <s:complexType name=\"NumberData\">\n" +
                "        <s:sequence>\n" +
                "          <s:element minOccurs=\"1\" maxOccurs=\"1\" name=\"NumberCanBeCalled\" type=\"s:boolean\" />\n" +
                "          <s:element minOccurs=\"1\" maxOccurs=\"1\" name=\"ResultCode\" type=\"s:int\" />\n" +
                "          <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"Result\" type=\"s:string\" />\n" +
                "          <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"Reason\" type=\"s:string\" />\n" +
                "        </s:sequence>\n" +
                "      </s:complexType>\n" +
                "      <s:element name=\"ResponseInfo\" type=\"si:ResponseInfo\" />\n" +
                "      <s:complexType name=\"ResponseInfo\">\n" +
                "        <s:sequence>\n" +
                "          <s:element minOccurs=\"1\" maxOccurs=\"1\" name=\"ResponseCode\" type=\"s:int\" />\n" +
                "          <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"Response\" type=\"s:string\" />\n" +
                "        </s:sequence>\n" +
                "      </s:complexType>\n" +
                "      <s:element name=\"CheckNumber\">\n" +
                "        <s:complexType>\n" +
                "          <s:sequence>\n" +
                "            <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"phoneNumber\" type=\"s:string\" />\n" +
                "          </s:sequence>\n" +
                "        </s:complexType>\n" +
                "      </s:element>\n" +
                "      <s:element name=\"CheckNumberResponse\">\n" +
                "        <s:complexType>\n" +
                "          <s:sequence>\n" +
                "            <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"CheckNumberResult\" type=\"si:NumberData\" />\n" +
                "          </s:sequence>\n" +
                "        </s:complexType>\n" +
                "      </s:element>\n" +
                "      <s:element name=\"AddDNCNumber\">\n" +
                "        <s:complexType>\n" +
                "          <s:sequence>\n" +
                "            <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"phoneNumber\" type=\"s:string\" />\n" +
                "          </s:sequence>\n" +
                "        </s:complexType>\n" +
                "      </s:element>\n" +
                "      <s:element name=\"AddDNCNumberResponse\">\n" +
                "        <s:complexType>\n" +
                "          <s:sequence>\n" +
                "            <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"AddDNCNumberResult\" type=\"si:DNCResultData\" />\n" +
                "          </s:sequence>\n" +
                "        </s:complexType>\n" +
                "      </s:element>\n" +
                "      <s:complexType name=\"DNCResultData\">\n" +
                "        <s:sequence>\n" +
                "          <s:element minOccurs=\"1\" maxOccurs=\"1\" name=\"ResultCode\" type=\"s:int\" />\n" +
                "          <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"Result\" type=\"s:string\" />\n" +
                "          <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"Reason\" type=\"s:string\" />\n" +
                "        </s:sequence>\n" +
                "      </s:complexType>\n" +
                "      <s:element name=\"AddEBRNumber\">\n" +
                "        <s:complexType>\n" +
                "          <s:sequence>\n" +
                "            <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"phoneNumber\" type=\"s:string\" />\n" +
                "            <s:element minOccurs=\"1\" maxOccurs=\"1\" name=\"dateOfLastContact\" type=\"s:dateTime\" />\n" +
                "            <s:element minOccurs=\"1\" maxOccurs=\"1\" name=\"type\" type=\"si:EBRType\" />\n" +
                "          </s:sequence>\n" +
                "        </s:complexType>\n" +
                "      </s:element>\n" +
                "      <s:simpleType name=\"EBRType\">\n" +
                "        <s:restriction base=\"s:string\">\n" +
                "          <s:enumeration value=\"Sale\" />\n" +
                "          <s:enumeration value=\"Inquiry\" />\n" +
                "          <s:enumeration value=\"Permission\" />\n" +
                "        </s:restriction>\n" +
                "      </s:simpleType>\n" +
                "      <s:element name=\"AddEBRNumberResponse\">\n" +
                "        <s:complexType>\n" +
                "          <s:sequence>\n" +
                "            <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"AddEBRNumberResult\" type=\"si:DNCResultData\" />\n" +
                "          </s:sequence>\n" +
                "        </s:complexType>\n" +
                "      </s:element>\n" +
                "      <s:element name=\"UpdateOrgIdOrSAN\">\n" +
                "        <s:complexType>\n" +
                "          <s:sequence>\n" +
                "            <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"OrgIdOrSAN\" type=\"s:string\" />\n" +
                "          </s:sequence>\n" +
                "        </s:complexType>\n" +
                "      </s:element>\n" +
                "      <s:element name=\"UpdateOrgIdOrSANResponse\">\n" +
                "        <s:complexType>\n" +
                "          <s:sequence>\n" +
                "            <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"UpdateOrgIdOrSANResult\" type=\"si:UpdateProjectStatus\" />\n" +
                "          </s:sequence>\n" +
                "        </s:complexType>\n" +
                "      </s:element>\n" +
                "      <s:complexType name=\"UpdateProjectStatus\">\n" +
                "        <s:sequence>\n" +
                "          <s:element minOccurs=\"1\" maxOccurs=\"1\" name=\"CallStatus\" type=\"s:boolean\" />\n" +
                "          <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"ProjectOrgIdOrSAN\" type=\"s:string\" />\n" +
                "        </s:sequence>\n" +
                "      </s:complexType>\n" +
                "    </s:schema>\n" +
                "    <xs:schema xmlns:tns1=\"http://ws.strikeiron.com\" attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" targetNamespace=\"http://ws.strikeiron.com\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "      <xs:element name=\"LicenseInfo\" type=\"tns1:LicenseInfo\" />\n" +
                "      <xs:complexType name=\"LicenseInfo\">\n" +
                "        <xs:sequence>\n" +
                "          <xs:element minOccurs=\"0\" name=\"RegisteredUser\" type=\"tns1:RegisteredUser\" />\n" +
                "        </xs:sequence>\n" +
                "      </xs:complexType>\n" +
                "      <xs:complexType name=\"RegisteredUser\">\n" +
                "        <xs:sequence>\n" +
                "          <xs:element minOccurs=\"0\" name=\"UserID\" type=\"xs:string\" />\n" +
                "          <xs:element minOccurs=\"0\" name=\"Password\" type=\"xs:string\" />\n" +
                "        </xs:sequence>\n" +
                "      </xs:complexType>\n" +
                "      <xs:element name=\"SubscriptionInfo\" type=\"tns1:SubscriptionInfo\" />\n" +
                "      <xs:complexType name=\"SubscriptionInfo\">\n" +
                "        <xs:sequence>\n" +
                "          <xs:element name=\"LicenseStatusCode\" type=\"xs:int\" />\n" +
                "          <xs:element minOccurs=\"0\" name=\"LicenseStatus\" type=\"xs:string\" />\n" +
                "          <xs:element name=\"LicenseActionCode\" type=\"xs:int\" />\n" +
                "          <xs:element minOccurs=\"0\" name=\"LicenseAction\" type=\"xs:string\" />\n" +
                "          <xs:element name=\"RemainingHits\" type=\"xs:int\" />\n" +
                "          <xs:element name=\"Amount\" type=\"xs:decimal\" />\n" +
                "        </xs:sequence>\n" +
                "      </xs:complexType>\n" +
                "      <xs:element name=\"GetRemainingHits\">\n" +
                "        <xs:complexType />\n" +
                "      </xs:element>\n" +
                "      <xs:element name=\"GetRemainingHitsResponse\">\n" +
                "        <xs:complexType />\n" +
                "      </xs:element>\n" +
                "    </xs:schema>\n" +
                "  </wsdl:types>\n" +
                "  <wsdl:message name=\"CheckNumberBatchSoapIn\">\n" +
                "    <wsdl:part name=\"parameters\" element=\"si:CheckNumberBatch\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"CheckNumberBatchSoapOut\">\n" +
                "    <wsdl:part name=\"parameters\" element=\"si:CheckNumberBatchResponse\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"CheckNumberBatchResponseInfo\">\n" +
                "    <wsdl:part name=\"ResponseInfo\" element=\"si:ResponseInfo\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"CheckNumberSoapIn\">\n" +
                "    <wsdl:part name=\"parameters\" element=\"si:CheckNumber\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"CheckNumberSoapOut\">\n" +
                "    <wsdl:part name=\"parameters\" element=\"si:CheckNumberResponse\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"CheckNumberResponseInfo\">\n" +
                "    <wsdl:part name=\"ResponseInfo\" element=\"si:ResponseInfo\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"AddDNCNumberSoapIn\">\n" +
                "    <wsdl:part name=\"parameters\" element=\"si:AddDNCNumber\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"AddDNCNumberSoapOut\">\n" +
                "    <wsdl:part name=\"parameters\" element=\"si:AddDNCNumberResponse\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"AddDNCNumberResponseInfo\">\n" +
                "    <wsdl:part name=\"ResponseInfo\" element=\"si:ResponseInfo\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"AddEBRNumberSoapIn\">\n" +
                "    <wsdl:part name=\"parameters\" element=\"si:AddEBRNumber\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"AddEBRNumberSoapOut\">\n" +
                "    <wsdl:part name=\"parameters\" element=\"si:AddEBRNumberResponse\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"AddEBRNumberResponseInfo\">\n" +
                "    <wsdl:part name=\"ResponseInfo\" element=\"si:ResponseInfo\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"UpdateOrgIdOrSANSoapIn\">\n" +
                "    <wsdl:part name=\"parameters\" element=\"si:UpdateOrgIdOrSAN\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"UpdateOrgIdOrSANSoapOut\">\n" +
                "    <wsdl:part name=\"parameters\" element=\"si:UpdateOrgIdOrSANResponse\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"UpdateOrgIdOrSANResponseInfo\">\n" +
                "    <wsdl:part name=\"ResponseInfo\" element=\"si:ResponseInfo\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"GetRemainingHitsSoapIn\">\n" +
                "    <wsdl:part name=\"parameters\" xmlns:q1=\"http://ws.strikeiron.com\" element=\"q1:GetRemainingHits\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"GetRemainingHitsSoapOut\">\n" +
                "    <wsdl:part name=\"parameters\" xmlns:q2=\"http://ws.strikeiron.com\" element=\"q2:GetRemainingHitsResponse\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"LicenseInfoMessage\">\n" +
                "    <wsdl:part name=\"LicenseInfo\" xmlns:q3=\"http://ws.strikeiron.com\" element=\"q3:LicenseInfo\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"SubscriptionInfoMessage\">\n" +
                "    <wsdl:part name=\"SubscriptionInfo\" xmlns:q4=\"http://ws.strikeiron.com\" element=\"q4:SubscriptionInfo\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:portType name=\"DoNotCallRegistrySoap\">\n" +
                "    <wsdl:operation name=\"CheckNumberBatch\">\n" +
                "      <documentation xmlns=\"http://schemas.xmlsoap.org/wsdl/\">Look up a registered phone number in a do not call list</documentation>\n" +
                "      <wsdl:input message=\"si:CheckNumberBatchSoapIn\" />\n" +
                "      <wsdl:output message=\"si:CheckNumberBatchSoapOut\" />\n" +
                "    </wsdl:operation>\n" +
                "    <wsdl:operation name=\"CheckNumber\">\n" +
                "      <documentation xmlns=\"http://schemas.xmlsoap.org/wsdl/\">Look up a registered phone number in a do not call list</documentation>\n" +
                "      <wsdl:input message=\"si:CheckNumberSoapIn\" />\n" +
                "      <wsdl:output message=\"si:CheckNumberSoapOut\" />\n" +
                "    </wsdl:operation>\n" +
                "    <wsdl:operation name=\"AddDNCNumber\">\n" +
                "      <wsdl:input message=\"si:AddDNCNumberSoapIn\" />\n" +
                "      <wsdl:output message=\"si:AddDNCNumberSoapOut\" />\n" +
                "    </wsdl:operation>\n" +
                "    <wsdl:operation name=\"AddEBRNumber\">\n" +
                "      <wsdl:input message=\"si:AddEBRNumberSoapIn\" />\n" +
                "      <wsdl:output message=\"si:AddEBRNumberSoapOut\" />\n" +
                "    </wsdl:operation>\n" +
                "    <wsdl:operation name=\"UpdateOrgIdOrSAN\">\n" +
                "      <documentation xmlns=\"http://schemas.xmlsoap.org/wsdl/\">Update your Orginazation Id or SAN</documentation>\n" +
                "      <wsdl:input message=\"si:UpdateOrgIdOrSANSoapIn\" />\n" +
                "      <wsdl:output message=\"si:UpdateOrgIdOrSANSoapOut\" />\n" +
                "    </wsdl:operation>\n" +
                "    <wsdl:operation name=\"GetRemainingHits\">\n" +
                "      <wsdl:input message=\"si:GetRemainingHitsSoapIn\" />\n" +
                "      <wsdl:output message=\"si:GetRemainingHitsSoapOut\" />\n" +
                "    </wsdl:operation>\n" +
                "  </wsdl:portType>\n" +
                "  <wsdl:binding name=\"DoNotCallRegistrySoap\" type=\"si:DoNotCallRegistrySoap\">\n" +
                "    <soap:binding transport=\"http://schemas.xmlsoap.org/soap/http\" />\n" +
                "    <wsdl:operation name=\"CheckNumberBatch\">\n" +
                "      <soap:operation soapAction=\"http://www.strikeiron.com/CheckNumberBatch\" style=\"document\" />\n" +
                "      <wsdl:input>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <tns:validation xmlns:tns=\"http://www.strikeiron.com\">\n" +
                "          <tns:assertions />\n" +
                "        </tns:validation>\n" +
                "        <soap:header message=\"si:LicenseInfoMessage\" part=\"LicenseInfo\" use=\"literal\" />\n" +
                "      </wsdl:input>\n" +
                "      <wsdl:output>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <soap:header message=\"si:CheckNumberBatchResponseInfo\" part=\"ResponseInfo\" use=\"literal\" />\n" +
                "        <soap:header message=\"si:SubscriptionInfoMessage\" part=\"SubscriptionInfo\" use=\"literal\" />\n" +
                "      </wsdl:output>\n" +
                "    </wsdl:operation>\n" +
                "    <wsdl:operation name=\"CheckNumber\">\n" +
                "      <soap:operation soapAction=\"http://www.strikeiron.com/CheckNumber\" style=\"document\" />\n" +
                "      <wsdl:input>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <tns:validation xmlns:tns=\"http://www.strikeiron.com\">\n" +
                "          <tns:assertions>\n" +
                "            <tns:assertion>\n" +
                "              <tns:expression>string-length(//tns:phoneNumber) &gt; 0</tns:expression>\n" +
                "              <tns:description>Phone number is required</tns:description>\n" +
                "            </tns:assertion>\n" +
                "          </tns:assertions>\n" +
                "        </tns:validation>\n" +
                "        <soap:header message=\"si:LicenseInfoMessage\" part=\"LicenseInfo\" use=\"literal\" />\n" +
                "      </wsdl:input>\n" +
                "      <wsdl:output>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <soap:header message=\"si:CheckNumberResponseInfo\" part=\"ResponseInfo\" use=\"literal\" />\n" +
                "        <soap:header message=\"si:SubscriptionInfoMessage\" part=\"SubscriptionInfo\" use=\"literal\" />\n" +
                "      </wsdl:output>\n" +
                "    </wsdl:operation>\n" +
                "    <wsdl:operation name=\"AddDNCNumber\">\n" +
                "      <soap:operation soapAction=\"http://www.strikeiron.com/AddDNCNumber\" style=\"document\" />\n" +
                "      <wsdl:input>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <tns:validation xmlns:tns=\"http://www.strikeiron.com\">\n" +
                "          <tns:assertions>\n" +
                "            <tns:assertion>\n" +
                "              <tns:expression>string-length(//tns:phoneNumber) &gt; 0</tns:expression>\n" +
                "              <tns:description>Phone number is required</tns:description>\n" +
                "            </tns:assertion>\n" +
                "          </tns:assertions>\n" +
                "        </tns:validation>\n" +
                "        <soap:header message=\"si:LicenseInfoMessage\" part=\"LicenseInfo\" use=\"literal\" />\n" +
                "      </wsdl:input>\n" +
                "      <wsdl:output>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <soap:header message=\"si:AddDNCNumberResponseInfo\" part=\"ResponseInfo\" use=\"literal\" />\n" +
                "        <soap:header message=\"si:SubscriptionInfoMessage\" part=\"SubscriptionInfo\" use=\"literal\" />\n" +
                "      </wsdl:output>\n" +
                "    </wsdl:operation>\n" +
                "    <wsdl:operation name=\"AddEBRNumber\">\n" +
                "      <soap:operation soapAction=\"http://www.strikeiron.com/AddEBRNumber\" style=\"document\" />\n" +
                "      <wsdl:input>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <tns:validation xmlns:tns=\"http://www.strikeiron.com\">\n" +
                "          <tns:assertions>\n" +
                "            <tns:assertion>\n" +
                "              <tns:expression>string-length(//tns:phoneNumber) &gt; 0</tns:expression>\n" +
                "              <tns:description>Phone number is required</tns:description>\n" +
                "            </tns:assertion>\n" +
                "          </tns:assertions>\n" +
                "        </tns:validation>\n" +
                "        <soap:header message=\"si:LicenseInfoMessage\" part=\"LicenseInfo\" use=\"literal\" />\n" +
                "      </wsdl:input>\n" +
                "      <wsdl:output>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <soap:header message=\"si:AddEBRNumberResponseInfo\" part=\"ResponseInfo\" use=\"literal\" />\n" +
                "        <soap:header message=\"si:SubscriptionInfoMessage\" part=\"SubscriptionInfo\" use=\"literal\" />\n" +
                "      </wsdl:output>\n" +
                "    </wsdl:operation>\n" +
                "    <wsdl:operation name=\"UpdateOrgIdOrSAN\">\n" +
                "      <soap:operation soapAction=\"http://www.strikeiron.com/UpdateOrgIdOrSAN\" style=\"document\" />\n" +
                "      <wsdl:input>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <tns:validation xmlns:tns=\"http://www.strikeiron.com\">\n" +
                "          <tns:assertions>\n" +
                "            <tns:assertion>\n" +
                "              <tns:expression>string-length(//tns:OrgIdOrSAN) &gt; 0</tns:expression>\n" +
                "              <tns:description>Organization Id or SAN is required</tns:description>\n" +
                "            </tns:assertion>\n" +
                "          </tns:assertions>\n" +
                "        </tns:validation>\n" +
                "        <soap:header message=\"si:LicenseInfoMessage\" part=\"LicenseInfo\" use=\"literal\" />\n" +
                "      </wsdl:input>\n" +
                "      <wsdl:output>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <soap:header message=\"si:UpdateOrgIdOrSANResponseInfo\" part=\"ResponseInfo\" use=\"literal\" />\n" +
                "        <soap:header message=\"si:SubscriptionInfoMessage\" part=\"SubscriptionInfo\" use=\"literal\" />\n" +
                "      </wsdl:output>\n" +
                "    </wsdl:operation>\n" +
                "    <wsdl:operation name=\"GetRemainingHits\">\n" +
                "      <soap:operation soapAction=\"http://ws.strikeiron.com/StrikeIron/donotcall2_5/DoNotCallRegistry/GetRemainingHits\" />\n" +
                "      <wsdl:input>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <soap:header message=\"si:LicenseInfoMessage\" part=\"LicenseInfo\" use=\"literal\" />\n" +
                "      </wsdl:input>\n" +
                "      <wsdl:output>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <soap:header message=\"si:SubscriptionInfoMessage\" part=\"SubscriptionInfo\" use=\"literal\" />\n" +
                "      </wsdl:output>\n" +
                "    </wsdl:operation>\n" +
                "  </wsdl:binding>\n" +
                "  <wsdl:service name=\"DoNotCallRegistry\">\n" +
                "    <documentation xmlns=\"http://schemas.xmlsoap.org/wsdl/\">Do Not Call List Service</documentation>\n" +
                "    <wsdl:port name=\"DoNotCallRegistrySoap\" binding=\"si:DoNotCallRegistrySoap\">\n" +
                "      <soap:address location=\"http://ws.strikeiron.com/StrikeIron/donotcall2_5/DoNotCallRegistry\" />\n" +
                "    </wsdl:port>\n" +
                "  </wsdl:service>\n" +
                "</wsdl:definitions>";
        try {
            Wsdl wsdl = wsdlManager.newWsdl(wsdlContent.getBytes(), "AutomatedSample.wsdl");
            wsdlManager.addWsdl(wsdl);
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:newWsdl method " +
                    "which have Inline wsdl content and wsdl Name" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing AddWSDL with Inline content", priority = 8)
    public void testAddWSDLContentWithoutName() throws GovernanceException {

        String wsdlContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<wsdl:definitions xmlns:http=\"http://schemas.xmlsoap.org/wsdl/http/\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:mime=\"http://schemas.xmlsoap.org/wsdl/mime/\" xmlns:si=\"http://www.strikeiron.com\" xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:s=\"http://www.w3.org/2001/XMLSchema\" xmlns:tm=\"http://microsoft.com/wsdl/mime/textMatching/\" targetNamespace=\"http://www.strikeiron.com\" xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\">\n" +
                "  <wsdl:types>\n" +
                "    <s:schema elementFormDefault=\"qualified\" targetNamespace=\"http://www.strikeiron.com\">\n" +
                "      <s:element name=\"CheckNumberBatch\">\n" +
                "        <s:complexType>\n" +
                "          <s:sequence>\n" +
                "            <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"phoneNumbers\" type=\"si:ArrayOfString\" />\n" +
                "          </s:sequence>\n" +
                "        </s:complexType>\n" +
                "      </s:element>\n" +
                "      <s:complexType name=\"ArrayOfString\">\n" +
                "        <s:sequence>\n" +
                "          <s:element minOccurs=\"0\" maxOccurs=\"unbounded\" name=\"phoneNumber\" nillable=\"true\" type=\"s:string\" />\n" +
                "        </s:sequence>\n" +
                "      </s:complexType>\n" +
                "      <s:element name=\"CheckNumberBatchResponse\">\n" +
                "        <s:complexType>\n" +
                "          <s:sequence>\n" +
                "            <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"CheckNumberBatchResult\" type=\"si:ArrayOfNumberData\" />\n" +
                "          </s:sequence>\n" +
                "        </s:complexType>\n" +
                "      </s:element>\n" +
                "      <s:complexType name=\"ArrayOfNumberData\">\n" +
                "        <s:sequence>\n" +
                "          <s:element minOccurs=\"0\" maxOccurs=\"unbounded\" name=\"NumberData\" nillable=\"true\" type=\"si:NumberData\" />\n" +
                "        </s:sequence>\n" +
                "      </s:complexType>\n" +
                "      <s:complexType name=\"NumberData\">\n" +
                "        <s:sequence>\n" +
                "          <s:element minOccurs=\"1\" maxOccurs=\"1\" name=\"NumberCanBeCalled\" type=\"s:boolean\" />\n" +
                "          <s:element minOccurs=\"1\" maxOccurs=\"1\" name=\"ResultCode\" type=\"s:int\" />\n" +
                "          <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"Result\" type=\"s:string\" />\n" +
                "          <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"Reason\" type=\"s:string\" />\n" +
                "        </s:sequence>\n" +
                "      </s:complexType>\n" +
                "      <s:element name=\"ResponseInfo\" type=\"si:ResponseInfo\" />\n" +
                "      <s:complexType name=\"ResponseInfo\">\n" +
                "        <s:sequence>\n" +
                "          <s:element minOccurs=\"1\" maxOccurs=\"1\" name=\"ResponseCode\" type=\"s:int\" />\n" +
                "          <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"Response\" type=\"s:string\" />\n" +
                "        </s:sequence>\n" +
                "      </s:complexType>\n" +
                "      <s:element name=\"CheckNumber\">\n" +
                "        <s:complexType>\n" +
                "          <s:sequence>\n" +
                "            <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"phoneNumber\" type=\"s:string\" />\n" +
                "          </s:sequence>\n" +
                "        </s:complexType>\n" +
                "      </s:element>\n" +
                "      <s:element name=\"CheckNumberResponse\">\n" +
                "        <s:complexType>\n" +
                "          <s:sequence>\n" +
                "            <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"CheckNumberResult\" type=\"si:NumberData\" />\n" +
                "          </s:sequence>\n" +
                "        </s:complexType>\n" +
                "      </s:element>\n" +
                "      <s:element name=\"AddDNCNumber\">\n" +
                "        <s:complexType>\n" +
                "          <s:sequence>\n" +
                "            <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"phoneNumber\" type=\"s:string\" />\n" +
                "          </s:sequence>\n" +
                "        </s:complexType>\n" +
                "      </s:element>\n" +
                "      <s:element name=\"AddDNCNumberResponse\">\n" +
                "        <s:complexType>\n" +
                "          <s:sequence>\n" +
                "            <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"AddDNCNumberResult\" type=\"si:DNCResultData\" />\n" +
                "          </s:sequence>\n" +
                "        </s:complexType>\n" +
                "      </s:element>\n" +
                "      <s:complexType name=\"DNCResultData\">\n" +
                "        <s:sequence>\n" +
                "          <s:element minOccurs=\"1\" maxOccurs=\"1\" name=\"ResultCode\" type=\"s:int\" />\n" +
                "          <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"Result\" type=\"s:string\" />\n" +
                "          <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"Reason\" type=\"s:string\" />\n" +
                "        </s:sequence>\n" +
                "      </s:complexType>\n" +
                "      <s:element name=\"AddEBRNumber\">\n" +
                "        <s:complexType>\n" +
                "          <s:sequence>\n" +
                "            <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"phoneNumber\" type=\"s:string\" />\n" +
                "            <s:element minOccurs=\"1\" maxOccurs=\"1\" name=\"dateOfLastContact\" type=\"s:dateTime\" />\n" +
                "            <s:element minOccurs=\"1\" maxOccurs=\"1\" name=\"type\" type=\"si:EBRType\" />\n" +
                "          </s:sequence>\n" +
                "        </s:complexType>\n" +
                "      </s:element>\n" +
                "      <s:simpleType name=\"EBRType\">\n" +
                "        <s:restriction base=\"s:string\">\n" +
                "          <s:enumeration value=\"Sale\" />\n" +
                "          <s:enumeration value=\"Inquiry\" />\n" +
                "          <s:enumeration value=\"Permission\" />\n" +
                "        </s:restriction>\n" +
                "      </s:simpleType>\n" +
                "      <s:element name=\"AddEBRNumberResponse\">\n" +
                "        <s:complexType>\n" +
                "          <s:sequence>\n" +
                "            <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"AddEBRNumberResult\" type=\"si:DNCResultData\" />\n" +
                "          </s:sequence>\n" +
                "        </s:complexType>\n" +
                "      </s:element>\n" +
                "      <s:element name=\"UpdateOrgIdOrSAN\">\n" +
                "        <s:complexType>\n" +
                "          <s:sequence>\n" +
                "            <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"OrgIdOrSAN\" type=\"s:string\" />\n" +
                "          </s:sequence>\n" +
                "        </s:complexType>\n" +
                "      </s:element>\n" +
                "      <s:element name=\"UpdateOrgIdOrSANResponse\">\n" +
                "        <s:complexType>\n" +
                "          <s:sequence>\n" +
                "            <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"UpdateOrgIdOrSANResult\" type=\"si:UpdateProjectStatus\" />\n" +
                "          </s:sequence>\n" +
                "        </s:complexType>\n" +
                "      </s:element>\n" +
                "      <s:complexType name=\"UpdateProjectStatus\">\n" +
                "        <s:sequence>\n" +
                "          <s:element minOccurs=\"1\" maxOccurs=\"1\" name=\"CallStatus\" type=\"s:boolean\" />\n" +
                "          <s:element minOccurs=\"0\" maxOccurs=\"1\" name=\"ProjectOrgIdOrSAN\" type=\"s:string\" />\n" +
                "        </s:sequence>\n" +
                "      </s:complexType>\n" +
                "    </s:schema>\n" +
                "    <xs:schema xmlns:tns1=\"http://ws.strikeiron.com\" attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" targetNamespace=\"http://ws.strikeiron.com\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "      <xs:element name=\"LicenseInfo\" type=\"tns1:LicenseInfo\" />\n" +
                "      <xs:complexType name=\"LicenseInfo\">\n" +
                "        <xs:sequence>\n" +
                "          <xs:element minOccurs=\"0\" name=\"RegisteredUser\" type=\"tns1:RegisteredUser\" />\n" +
                "        </xs:sequence>\n" +
                "      </xs:complexType>\n" +
                "      <xs:complexType name=\"RegisteredUser\">\n" +
                "        <xs:sequence>\n" +
                "          <xs:element minOccurs=\"0\" name=\"UserID\" type=\"xs:string\" />\n" +
                "          <xs:element minOccurs=\"0\" name=\"Password\" type=\"xs:string\" />\n" +
                "        </xs:sequence>\n" +
                "      </xs:complexType>\n" +
                "      <xs:element name=\"SubscriptionInfo\" type=\"tns1:SubscriptionInfo\" />\n" +
                "      <xs:complexType name=\"SubscriptionInfo\">\n" +
                "        <xs:sequence>\n" +
                "          <xs:element name=\"LicenseStatusCode\" type=\"xs:int\" />\n" +
                "          <xs:element minOccurs=\"0\" name=\"LicenseStatus\" type=\"xs:string\" />\n" +
                "          <xs:element name=\"LicenseActionCode\" type=\"xs:int\" />\n" +
                "          <xs:element minOccurs=\"0\" name=\"LicenseAction\" type=\"xs:string\" />\n" +
                "          <xs:element name=\"RemainingHits\" type=\"xs:int\" />\n" +
                "          <xs:element name=\"Amount\" type=\"xs:decimal\" />\n" +
                "        </xs:sequence>\n" +
                "      </xs:complexType>\n" +
                "      <xs:element name=\"GetRemainingHits\">\n" +
                "        <xs:complexType />\n" +
                "      </xs:element>\n" +
                "      <xs:element name=\"GetRemainingHitsResponse\">\n" +
                "        <xs:complexType />\n" +
                "      </xs:element>\n" +
                "    </xs:schema>\n" +
                "  </wsdl:types>\n" +
                "  <wsdl:message name=\"CheckNumberBatchSoapIn\">\n" +
                "    <wsdl:part name=\"parameters\" element=\"si:CheckNumberBatch\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"CheckNumberBatchSoapOut\">\n" +
                "    <wsdl:part name=\"parameters\" element=\"si:CheckNumberBatchResponse\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"CheckNumberBatchResponseInfo\">\n" +
                "    <wsdl:part name=\"ResponseInfo\" element=\"si:ResponseInfo\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"CheckNumberSoapIn\">\n" +
                "    <wsdl:part name=\"parameters\" element=\"si:CheckNumber\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"CheckNumberSoapOut\">\n" +
                "    <wsdl:part name=\"parameters\" element=\"si:CheckNumberResponse\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"CheckNumberResponseInfo\">\n" +
                "    <wsdl:part name=\"ResponseInfo\" element=\"si:ResponseInfo\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"AddDNCNumberSoapIn\">\n" +
                "    <wsdl:part name=\"parameters\" element=\"si:AddDNCNumber\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"AddDNCNumberSoapOut\">\n" +
                "    <wsdl:part name=\"parameters\" element=\"si:AddDNCNumberResponse\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"AddDNCNumberResponseInfo\">\n" +
                "    <wsdl:part name=\"ResponseInfo\" element=\"si:ResponseInfo\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"AddEBRNumberSoapIn\">\n" +
                "    <wsdl:part name=\"parameters\" element=\"si:AddEBRNumber\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"AddEBRNumberSoapOut\">\n" +
                "    <wsdl:part name=\"parameters\" element=\"si:AddEBRNumberResponse\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"AddEBRNumberResponseInfo\">\n" +
                "    <wsdl:part name=\"ResponseInfo\" element=\"si:ResponseInfo\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"UpdateOrgIdOrSANSoapIn\">\n" +
                "    <wsdl:part name=\"parameters\" element=\"si:UpdateOrgIdOrSAN\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"UpdateOrgIdOrSANSoapOut\">\n" +
                "    <wsdl:part name=\"parameters\" element=\"si:UpdateOrgIdOrSANResponse\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"UpdateOrgIdOrSANResponseInfo\">\n" +
                "    <wsdl:part name=\"ResponseInfo\" element=\"si:ResponseInfo\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"GetRemainingHitsSoapIn\">\n" +
                "    <wsdl:part name=\"parameters\" xmlns:q1=\"http://ws.strikeiron.com\" element=\"q1:GetRemainingHits\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"GetRemainingHitsSoapOut\">\n" +
                "    <wsdl:part name=\"parameters\" xmlns:q2=\"http://ws.strikeiron.com\" element=\"q2:GetRemainingHitsResponse\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"LicenseInfoMessage\">\n" +
                "    <wsdl:part name=\"LicenseInfo\" xmlns:q3=\"http://ws.strikeiron.com\" element=\"q3:LicenseInfo\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:message name=\"SubscriptionInfoMessage\">\n" +
                "    <wsdl:part name=\"SubscriptionInfo\" xmlns:q4=\"http://ws.strikeiron.com\" element=\"q4:SubscriptionInfo\" />\n" +
                "  </wsdl:message>\n" +
                "  <wsdl:portType name=\"DoNotCallRegistrySoap\">\n" +
                "    <wsdl:operation name=\"CheckNumberBatch\">\n" +
                "      <documentation xmlns=\"http://schemas.xmlsoap.org/wsdl/\">Look up a registered phone number in a do not call list</documentation>\n" +
                "      <wsdl:input message=\"si:CheckNumberBatchSoapIn\" />\n" +
                "      <wsdl:output message=\"si:CheckNumberBatchSoapOut\" />\n" +
                "    </wsdl:operation>\n" +
                "    <wsdl:operation name=\"CheckNumber\">\n" +
                "      <documentation xmlns=\"http://schemas.xmlsoap.org/wsdl/\">Look up a registered phone number in a do not call list</documentation>\n" +
                "      <wsdl:input message=\"si:CheckNumberSoapIn\" />\n" +
                "      <wsdl:output message=\"si:CheckNumberSoapOut\" />\n" +
                "    </wsdl:operation>\n" +
                "    <wsdl:operation name=\"AddDNCNumber\">\n" +
                "      <wsdl:input message=\"si:AddDNCNumberSoapIn\" />\n" +
                "      <wsdl:output message=\"si:AddDNCNumberSoapOut\" />\n" +
                "    </wsdl:operation>\n" +
                "    <wsdl:operation name=\"AddEBRNumber\">\n" +
                "      <wsdl:input message=\"si:AddEBRNumberSoapIn\" />\n" +
                "      <wsdl:output message=\"si:AddEBRNumberSoapOut\" />\n" +
                "    </wsdl:operation>\n" +
                "    <wsdl:operation name=\"UpdateOrgIdOrSAN\">\n" +
                "      <documentation xmlns=\"http://schemas.xmlsoap.org/wsdl/\">Update your Orginazation Id or SAN</documentation>\n" +
                "      <wsdl:input message=\"si:UpdateOrgIdOrSANSoapIn\" />\n" +
                "      <wsdl:output message=\"si:UpdateOrgIdOrSANSoapOut\" />\n" +
                "    </wsdl:operation>\n" +
                "    <wsdl:operation name=\"GetRemainingHits\">\n" +
                "      <wsdl:input message=\"si:GetRemainingHitsSoapIn\" />\n" +
                "      <wsdl:output message=\"si:GetRemainingHitsSoapOut\" />\n" +
                "    </wsdl:operation>\n" +
                "  </wsdl:portType>\n" +
                "  <wsdl:binding name=\"DoNotCallRegistrySoap\" type=\"si:DoNotCallRegistrySoap\">\n" +
                "    <soap:binding transport=\"http://schemas.xmlsoap.org/soap/http\" />\n" +
                "    <wsdl:operation name=\"CheckNumberBatch\">\n" +
                "      <soap:operation soapAction=\"http://www.strikeiron.com/CheckNumberBatch\" style=\"document\" />\n" +
                "      <wsdl:input>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <tns:validation xmlns:tns=\"http://www.strikeiron.com\">\n" +
                "          <tns:assertions />\n" +
                "        </tns:validation>\n" +
                "        <soap:header message=\"si:LicenseInfoMessage\" part=\"LicenseInfo\" use=\"literal\" />\n" +
                "      </wsdl:input>\n" +
                "      <wsdl:output>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <soap:header message=\"si:CheckNumberBatchResponseInfo\" part=\"ResponseInfo\" use=\"literal\" />\n" +
                "        <soap:header message=\"si:SubscriptionInfoMessage\" part=\"SubscriptionInfo\" use=\"literal\" />\n" +
                "      </wsdl:output>\n" +
                "    </wsdl:operation>\n" +
                "    <wsdl:operation name=\"CheckNumber\">\n" +
                "      <soap:operation soapAction=\"http://www.strikeiron.com/CheckNumber\" style=\"document\" />\n" +
                "      <wsdl:input>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <tns:validation xmlns:tns=\"http://www.strikeiron.com\">\n" +
                "          <tns:assertions>\n" +
                "            <tns:assertion>\n" +
                "              <tns:expression>string-length(//tns:phoneNumber) &gt; 0</tns:expression>\n" +
                "              <tns:description>Phone number is required</tns:description>\n" +
                "            </tns:assertion>\n" +
                "          </tns:assertions>\n" +
                "        </tns:validation>\n" +
                "        <soap:header message=\"si:LicenseInfoMessage\" part=\"LicenseInfo\" use=\"literal\" />\n" +
                "      </wsdl:input>\n" +
                "      <wsdl:output>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <soap:header message=\"si:CheckNumberResponseInfo\" part=\"ResponseInfo\" use=\"literal\" />\n" +
                "        <soap:header message=\"si:SubscriptionInfoMessage\" part=\"SubscriptionInfo\" use=\"literal\" />\n" +
                "      </wsdl:output>\n" +
                "    </wsdl:operation>\n" +
                "    <wsdl:operation name=\"AddDNCNumber\">\n" +
                "      <soap:operation soapAction=\"http://www.strikeiron.com/AddDNCNumber\" style=\"document\" />\n" +
                "      <wsdl:input>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <tns:validation xmlns:tns=\"http://www.strikeiron.com\">\n" +
                "          <tns:assertions>\n" +
                "            <tns:assertion>\n" +
                "              <tns:expression>string-length(//tns:phoneNumber) &gt; 0</tns:expression>\n" +
                "              <tns:description>Phone number is required</tns:description>\n" +
                "            </tns:assertion>\n" +
                "          </tns:assertions>\n" +
                "        </tns:validation>\n" +
                "        <soap:header message=\"si:LicenseInfoMessage\" part=\"LicenseInfo\" use=\"literal\" />\n" +
                "      </wsdl:input>\n" +
                "      <wsdl:output>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <soap:header message=\"si:AddDNCNumberResponseInfo\" part=\"ResponseInfo\" use=\"literal\" />\n" +
                "        <soap:header message=\"si:SubscriptionInfoMessage\" part=\"SubscriptionInfo\" use=\"literal\" />\n" +
                "      </wsdl:output>\n" +
                "    </wsdl:operation>\n" +
                "    <wsdl:operation name=\"AddEBRNumber\">\n" +
                "      <soap:operation soapAction=\"http://www.strikeiron.com/AddEBRNumber\" style=\"document\" />\n" +
                "      <wsdl:input>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <tns:validation xmlns:tns=\"http://www.strikeiron.com\">\n" +
                "          <tns:assertions>\n" +
                "            <tns:assertion>\n" +
                "              <tns:expression>string-length(//tns:phoneNumber) &gt; 0</tns:expression>\n" +
                "              <tns:description>Phone number is required</tns:description>\n" +
                "            </tns:assertion>\n" +
                "          </tns:assertions>\n" +
                "        </tns:validation>\n" +
                "        <soap:header message=\"si:LicenseInfoMessage\" part=\"LicenseInfo\" use=\"literal\" />\n" +
                "      </wsdl:input>\n" +
                "      <wsdl:output>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <soap:header message=\"si:AddEBRNumberResponseInfo\" part=\"ResponseInfo\" use=\"literal\" />\n" +
                "        <soap:header message=\"si:SubscriptionInfoMessage\" part=\"SubscriptionInfo\" use=\"literal\" />\n" +
                "      </wsdl:output>\n" +
                "    </wsdl:operation>\n" +
                "    <wsdl:operation name=\"UpdateOrgIdOrSAN\">\n" +
                "      <soap:operation soapAction=\"http://www.strikeiron.com/UpdateOrgIdOrSAN\" style=\"document\" />\n" +
                "      <wsdl:input>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <tns:validation xmlns:tns=\"http://www.strikeiron.com\">\n" +
                "          <tns:assertions>\n" +
                "            <tns:assertion>\n" +
                "              <tns:expression>string-length(//tns:OrgIdOrSAN) &gt; 0</tns:expression>\n" +
                "              <tns:description>Organization Id or SAN is required</tns:description>\n" +
                "            </tns:assertion>\n" +
                "          </tns:assertions>\n" +
                "        </tns:validation>\n" +
                "        <soap:header message=\"si:LicenseInfoMessage\" part=\"LicenseInfo\" use=\"literal\" />\n" +
                "      </wsdl:input>\n" +
                "      <wsdl:output>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <soap:header message=\"si:UpdateOrgIdOrSANResponseInfo\" part=\"ResponseInfo\" use=\"literal\" />\n" +
                "        <soap:header message=\"si:SubscriptionInfoMessage\" part=\"SubscriptionInfo\" use=\"literal\" />\n" +
                "      </wsdl:output>\n" +
                "    </wsdl:operation>\n" +
                "    <wsdl:operation name=\"GetRemainingHits\">\n" +
                "      <soap:operation soapAction=\"http://ws.strikeiron.com/StrikeIron/donotcall2_5/DoNotCallRegistry/GetRemainingHits\" />\n" +
                "      <wsdl:input>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <soap:header message=\"si:LicenseInfoMessage\" part=\"LicenseInfo\" use=\"literal\" />\n" +
                "      </wsdl:input>\n" +
                "      <wsdl:output>\n" +
                "        <soap:body use=\"literal\" />\n" +
                "        <soap:header message=\"si:SubscriptionInfoMessage\" part=\"SubscriptionInfo\" use=\"literal\" />\n" +
                "      </wsdl:output>\n" +
                "    </wsdl:operation>\n" +
                "  </wsdl:binding>\n" +
                "  <wsdl:service name=\"DoNotCallRegistry\">\n" +
                "    <documentation xmlns=\"http://schemas.xmlsoap.org/wsdl/\">Do Not Call List Service</documentation>\n" +
                "    <wsdl:port name=\"DoNotCallRegistrySoap\" binding=\"si:DoNotCallRegistrySoap\">\n" +
                "      <soap:address location=\"http://ws.strikeiron.com/StrikeIron/donotcall2_5/DoNotCallRegistry\" />\n" +
                "    </wsdl:port>\n" +
                "  </wsdl:service>\n" +
                "</wsdl:definitions>";
        try {
            Wsdl wsdl = wsdlManager.newWsdl(wsdlContent.getBytes());
            wsdlManager.addWsdl(wsdl);
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:newWsdl method " +
                    "which have Inline wsdl content" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing RemoveWSDL", priority = 9)
    public void testRemoveWSDL() throws GovernanceException {
        try {
            cleanWSDL();
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:removeWsdl method " +
                    ":" + e);
        }
    }


    private void cleanWSDL() throws GovernanceException {
        wsdlArray = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlArray) {
            if (w.getQName().getNamespaceURI().contains("www.strikeiron.com")) {
                wsdlManager.removeWsdl(w.getId());
            }
        }
    }

}
