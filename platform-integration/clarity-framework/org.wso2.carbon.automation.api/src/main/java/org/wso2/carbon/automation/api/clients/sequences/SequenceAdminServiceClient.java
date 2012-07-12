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
package org.wso2.carbon.automation.api.clients.sequences;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.sequences.stub.types.SequenceAdminServiceStub;
import org.wso2.carbon.sequences.stub.types.SequenceEditorException;

import javax.activation.DataHandler;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.rmi.RemoteException;

public class SequenceAdminServiceClient {
    private static final Log log = LogFactory.getLog(SequenceAdminServiceClient.class);

    private final String serviceName = "SequenceAdminService";
    private SequenceAdminServiceStub sequenceAdminServiceStub;

    public SequenceAdminServiceClient(String backEndUrl, String sessionCookie) throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        sequenceAdminServiceStub = new SequenceAdminServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, sequenceAdminServiceStub);
    }

    public SequenceAdminServiceClient(String backEndUrl, String userName, String password)
            throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        sequenceAdminServiceStub = new SequenceAdminServiceStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, sequenceAdminServiceStub);
    }


    public void addSequence(DataHandler dh)
            throws SequenceEditorException, IOException, XMLStreamException {

        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(dh.getInputStream());
        //create the builder
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        OMElement sequenceElem = builder.getDocumentElement();
        sequenceAdminServiceStub.addSequence(sequenceElem);
    }

    public void addDynamicSequence(String key, OMElement omElement)
            throws SequenceEditorException, RemoteException {
        sequenceAdminServiceStub.addDynamicSequence(key, omElement);

    }

    public OMElement getSequence(String sequenceName)
            throws SequenceEditorException, RemoteException {
        return sequenceAdminServiceStub.getSequence(sequenceName);

    }

    public void deleteSequence(String sequenceName)
            throws SequenceEditorException, RemoteException {
        sequenceAdminServiceStub.deleteSequence(sequenceName);

    }

    public void updateDynamicSequence(String key, OMElement OmSequence)
            throws SequenceEditorException, RemoteException {
        sequenceAdminServiceStub.updateDynamicSequence(key, OmSequence);
    }

    public int getDynamicSequenceCount()
            throws SequenceEditorException, RemoteException {
        return sequenceAdminServiceStub.getDynamicSequenceCount();
    }

    public void deleteDynamicSequence(String key)
            throws SequenceEditorException, RemoteException {
        sequenceAdminServiceStub.deleteDynamicSequence(key);
    }
}
