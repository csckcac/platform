package org.wso2.carbon.mediator.test.script;/*
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

import org.apache.axiom.om.OMElement;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.mediator.test.ESBMediatorTest;

import javax.xml.namespace.QName;
import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class UploadJsScriptTest extends ESBMediatorTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironmeant()
            throws Exception, RuntimeException, IOException {
        init();
        loadSampleESBConfiguration(352);
    }

    @Test(groups = "wso2.esb", description = "Tests level log")
    public void accessSynapseMsgAPI() throws Exception {

        OMElement response;
        response= axis2Client.sendSimpleStockQuoteRequest
                (getMainSequenceURL(),
                 "http://localhost:9000/services/SimpleStockQuoteService",
                 "wso2");
        assertNotNull(response, "Response message null");
        assertEquals(response.getQName().getLocalPart().toString(), "getQuoteResponse");
        assertEquals(response.getFirstElement().getLocalName().toString(),"return");
        assertEquals(response.getFirstChildWithName
                (new QName("http://services.samples/xsd","return")).getFirstElement().getLocalName(),
                     "last");
        assertEquals(response.getFirstChildWithName(
                new QName("http://services.samples/xsd", "return")).getFirstElement().getText(),
                     "99.9");
    }

    @AfterTest(alwaysRun = true)
    public void stop(){
        cleanup();
    }

}
