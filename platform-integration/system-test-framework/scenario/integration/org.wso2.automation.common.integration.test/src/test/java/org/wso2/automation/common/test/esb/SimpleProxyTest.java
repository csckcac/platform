/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.automation.common.test.esb;

import junit.framework.Assert;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceService;
import org.apache.axis2.addressing.EndpointReference;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminProxyAdminException;
import org.wso2.platform.test.core.RequestSender;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;

public class SimpleProxyTest {
    private ManageEnvironment manageEnvironmentObj;
    private static final Log log = LogFactory.getLog(SimpleProxyTest.class);

    @BeforeTest
    public void init() throws InterruptedException {
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder().esb(1).as(1);
        manageEnvironmentObj = environmentBuilder.build();
        AdminServiceService adminServiceService = new AdminServiceService(manageEnvironmentObj.getAs().getBackEndUrl());
        int loopCount = 0;
        while (true) {
            String serviceName = adminServiceService.getServiceGroup(manageEnvironmentObj.getAs().
                    getSessionCookie(), "SimpleStockQuoteService");
            if (serviceName != null) {
                break;
            } else if (loopCount >= 15) {
                Assert.fail("Unable to deploy SimpleStockQuoteService in App server instance");
                break;
            }
            Thread.sleep(2000);
            loopCount++;
        }
    }

    @Test(alwaysRun = true, description = "Invoking simple proxy service")
    public void testSimpleProxyService() throws XMLStreamException, RemoteException,
                                                InterruptedException,
                                                ProxyServiceAdminProxyAdminException {
        String serviceName = "StockQuoteProxy";
        int timeout = 600;
        AdminServiceService adminServiceService = new AdminServiceService(manageEnvironmentObj.
                getEsb().getBackEndUrl());
        while (timeout <= 60000) {
            if (adminServiceService.getServiceGroup(manageEnvironmentObj.getEsb().
                    getSessionCookie(), serviceName).equalsIgnoreCase(serviceName)) {
                break;
            } else {
                log.info("Waiting for proxy service deployment.");
                Thread.sleep(timeout);
                timeout++;
            }
        }
        EndpointReference epr = new EndpointReference("http://" + manageEnvironmentObj.getEsb()
                .getProductVariables().getHostName() + ":" + manageEnvironmentObj.getEsb()
                .getProductVariables().getNhttpPort() + "/services/" + serviceName + "/");
        OMElement omElement = RequestSender.sendRequest(createPayLoad("IBM").toString(), epr);
        Assert.assertTrue("Expected result not found ", omElement.toString().contains("IBM"));

    }

    private OMElement createPayLoad(String symbol) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://services.samples", "ns");
        OMElement method = fac.createOMElement("getQuote", omNs);
        OMElement value1 = fac.createOMElement("request", omNs);
        OMElement value2 = fac.createOMElement("symbol", omNs);

        value2.addChild(fac.createOMText(value1, symbol));
        value1.addChild(value2);
        method.addChild(value1);

        return method;
    }
}
