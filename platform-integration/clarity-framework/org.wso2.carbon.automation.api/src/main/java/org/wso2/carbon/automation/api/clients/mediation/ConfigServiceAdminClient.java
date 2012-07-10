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

package org.wso2.carbon.automation.api.clients.mediation;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.localentry.stub.types.LocalEntryAdminException;
import org.wso2.carbon.mediation.configadmin.stub.ConfigServiceAdminStub;

import javax.activation.DataHandler;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;

public class ConfigServiceAdminClient {
    String backendUrl = null;
    String SessionCookie = null;
    ConfigServiceAdminStub configServiceAdminStub;
    private static final Log log = LogFactory.getLog(ConfigServiceAdminClient.class);

    public ConfigServiceAdminClient(String backendUrl, String sessionCookie) {
        this.backendUrl = backendUrl;
        this.SessionCookie = sessionCookie;
    }

    private ConfigServiceAdminStub setMessageStoreStubStub() throws AxisFault {
        final String messageStoreServiceUrl = backendUrl + "MessageStoreAdminService";
        ConfigServiceAdminStub configAdminService = null;
        configAdminService = new ConfigServiceAdminStub(messageStoreServiceUrl);
        AuthenticateStub.authenticateStub(SessionCookie, configAdminService);
        return configAdminService;
    }

    public void addMessageStore(DataHandler dh)
            throws IOException, LocalEntryAdminException, XMLStreamException {
        configServiceAdminStub = this.setMessageStoreStubStub();
        XMLStreamReader parser =
                XMLInputFactory.newInstance().createXMLStreamReader(dh.getInputStream());
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        OMElement configSourceElem = builder.getDocumentElement();
        configServiceAdminStub.addExistingConfiguration(configSourceElem.toString());

    }

    public void updateConfiguration(OMElement omElement)
            throws IOException, LocalEntryAdminException, XMLStreamException {
        configServiceAdminStub = this.setMessageStoreStubStub();
        configServiceAdminStub.updateConfiguration(omElement);

    }
}
