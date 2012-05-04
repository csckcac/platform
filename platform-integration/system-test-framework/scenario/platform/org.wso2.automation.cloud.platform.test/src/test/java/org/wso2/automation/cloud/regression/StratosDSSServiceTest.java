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

package org.wso2.automation.cloud.regression;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.automation.cloud.regression.hectorclient.HectorExample;

import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;


public class StratosDSSServiceTest {

    private static final Log log = LogFactory.getLog(StratosDSSServiceTest.class);

    private static String httpDataStratosUrl;
    private UserInfo userInfo;

    @BeforeClass
    public void init() throws LoginAuthenticationExceptionException, RemoteException {
        EnvironmentBuilder builder = new EnvironmentBuilder().dss(5);
        EnvironmentVariables dssServer = builder.build().getDss();
        userInfo = UserListCsvReader.getUserInfo(5);
        httpDataStratosUrl = "http://" + dssServer.getProductVariables().getHostName()
                                + "/services/t/" + userInfo.getDomain();
    }

    @Test(invocationCount = 5)
    public void googleSpreadSheerDataServiceTest() throws AxisFault {
        googleSpreadsheetService();
    }

    @Test(invocationCount = 5)
    public void csvDataServiceTest() throws AxisFault, XMLStreamException {
        csvDataService();
    }

    @Test(invocationCount = 5)
    public void excelDataServiceTest() throws AxisFault, XMLStreamException {
        excelDataService();
    }

    @Test(invocationCount = 5)
    public void sqlDataServiceTest() throws AxisFault {
        rssDataService();
    }

    @Test(invocationCount = 5)
    public void restFullDataServiceTest() throws AxisFault {
        restDataService();
    }

    @Test()
    public void hectorTest() throws AxisFault, InterruptedException {
        Assert.assertTrue(HectorExample.executeKeySpaceSample(userInfo), "Cassandra test fail");
    }

    private void
    rssDataService() throws AxisFault {
        OMElement result;
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://test.org", "ns1");
        OMElement payload = fac.createOMElement("getEmployeeDepartments", omNs);
        OMElement value = fac.createOMElement("Dep_Name", omNs);
        value.addChild(fac.createOMText(value, "Dep1"));
        payload.addChild(value);

        ServiceClient serviceclient = new ServiceClient();
        Options opts = new Options();

        opts.setTo(new EndpointReference(httpDataStratosUrl + "/CompanySampleDS/"));
        opts.getTo();
        opts.setAction("getEmployeeDepartments");
        //bypass http protocol exception
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);

        serviceclient.setOptions(opts);

        result = serviceclient.sendReceive(payload);
        Assert.assertNotNull(result, "Response Message is null");
        log.info(result);
        Assert.assertTrue(result.toString().contains("Jayasuriya"), "RSS data service invocation failed");

    }

    private void csvDataService() throws XMLStreamException, AxisFault {

        String payload = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                         " <soapenv:Header/>\n" +
                         " <soapenv:Body/>\n" +
                         "</soapenv:Envelope>";

        String action = "getProducts";

        OMElement result;
        result = sendRequest(payload, action, new EndpointReference(httpDataStratosUrl
                                                                    + "/CSVSampleService"));
        Assert.assertNotNull(result, "Response Message is null");
        log.info(result);
        Assert.assertTrue(result.toString().contains("1969 Harley Davidson Ultimate Chopper"),
                          "CSV data service invocation failed");


    }

    private void excelDataService() throws XMLStreamException, AxisFault {

        String payload = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                         " <soapenv:Header/>\n" +
                         " <soapenv:Body/>\n" +
                         "</soapenv:Envelope>";

        String action = "getProducts";


        OMElement result;
        result = sendRequest(payload, action, new EndpointReference(httpDataStratosUrl + "/ExcelSampleService"));
        Assert.assertNotNull(result, "Response Message is null");
        log.info(result);
        Assert.assertTrue(result.toString().contains("1952 Alpine Renault 1300"), "EXCEL DataService invocation failed");

    }

    private void restDataService() throws AxisFault {

        OMElement result;
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://product.abc.com", "p");
        OMElement payload = fac.createOMElement("_getproduct_productcode", omNs);
        OMElement value = fac.createOMElement("productCode", omNs);
        value.addChild(fac.createOMText(value, "S12_1108"));
        payload.addChild(value);

        ServiceClient serviceclient;

        serviceclient = new ServiceClient();
        Options opts = new Options();

        opts.setTo(new EndpointReference(httpDataStratosUrl + "/Rest_Sample/"));
        opts.getTo();
        opts.setAction("urn:_getproduct_productcode");
        //bypass http protocol exception
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);

        serviceclient.setOptions(opts);

        result = serviceclient.sendReceive(payload);
        Assert.assertNotNull(result);
        log.info(result);
        Assert.assertTrue(result.toString().contains("2001 Ferrari Enzo"), "REST DataService invocation failed");

    }

    private void googleSpreadsheetService() throws AxisFault {
        OMElement result;
        OMElement payload = createPayLoad();

        ServiceClient serviceclient = new ServiceClient();
        Options opts = new Options();
        opts.setTo(new EndpointReference(httpDataStratosUrl + "/GSpreadSample/"));
        opts.setAction("http://ws.wso2.org/dataservice/getCustomers");
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        serviceclient.setOptions(opts);

        result = serviceclient.sendReceive(payload);
        log.info(result);
        Assert.assertNotNull(result, "Response Message null");
        Assert.assertTrue(result.toString().contains("Signal Gift Stores"), "GSSample DataService invocation failed");

    }

    private static OMElement sendRequest(String payloadStr, String action,
                                         EndpointReference targetEPR)
            throws XMLStreamException, AxisFault {
        OMElement payload = AXIOMUtil.stringToOM(payloadStr);
        Options options = new Options();
        options.setTo(targetEPR);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        options.setAction("urn:" + action); //since soapAction = ""
        ServiceClient sender = new ServiceClient();
        sender.setOptions(options);
        OMElement result = sender.sendReceive(payload);

        return result;
    }

    private static OMElement createPayLoad() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice", "ns1");
        return fac.createOMElement("getCustomers", omNs);
    }
}
