/*
*Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.appserver.integration.tests;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class JSONTestCase {

    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_JSON_BADGERFISH = "application/json/badgerfish";

    private static final String ECHO_STRING = "Hello JSON Service";

    private static final Log log = LogFactory.getLog(JSONTestCase.class);

    @Test(groups = {"wso2.as"})
    public void testJSONRequest() throws AxisFault, XMLStreamException {
        ClientConnectionUtil.waitForPort(9763);
        EndpointReference targetEPR =
                new EndpointReference("http://localhost:9763/services/JSONService");

        Options options = new Options();
        options.setTo(targetEPR);

        File configFile = new File("repository/conf/axis2/axis2_client.xml");
        ConfigurationContext clientConfigurationContext =
                ConfigurationContextFactory
                        .createConfigurationContextFromFileSystem(null,
                                                                  configFile.getAbsolutePath());
        ServiceClient sender = new ServiceClient(clientConfigurationContext, null);
        sender.setOptions(options);
        options.setTo(targetEPR);

        // application/json case
        String contentType = APPLICATION_JSON;
        options.setProperty(Constants.Configuration.MESSAGE_TYPE, contentType);
        OMElement echoPayload = getEchoPayload(contentType);
        OMElement result = sender.sendReceive(echoPayload);
        assert result != null : "Result cannot be null";
        assert echoPayload.toString().equals(result.toString().trim());

        // application/json/badgerfish case
        contentType = APPLICATION_JSON_BADGERFISH;
        options.setProperty(Constants.Configuration.MESSAGE_TYPE, contentType);
        echoPayload = getEchoPayload(contentType);
        result = sender.sendReceive(echoPayload);
        assertNotNull(result, "Result cannot be null");
        assertEquals(echoPayload.toString(), result.toString().trim());
    }

    private static OMElement getEchoPayload(String contentType) throws XMLStreamException {
        String payload = "<echo><value>" + ECHO_STRING + "</value></echo>";

        // If the content type is "application/json/badgerfish", we
        // can have namespaces within our payload
        if (APPLICATION_JSON_BADGERFISH.equals(contentType)) {
            payload = "<echo><ns:value xmlns:ns=\"http://services.wsas.training.wso2.org\">" +
                      ECHO_STRING + "</ns:value></echo>";
        }

        // If you want to send JSON Arrays, use the following payload
        // payload = "<echo><value>Hello1</value><value>Hello2</value><value>Hello3</value></echo>";

        // return an OMElement from the payload..
        return new StAXOMBuilder(new ByteArrayInputStream(payload.getBytes())).getDocumentElement();
    }
}
