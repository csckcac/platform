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
package org.wso2.carbon.automation.api.clients.endpoint;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.endpoint.stub.types.EndpointAdminEndpointAdminException;
import org.wso2.carbon.endpoint.stub.types.EndpointAdminStub;

import javax.activation.DataHandler;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.rmi.RemoteException;

public class EndPointAdminClient {
    private static final Log log = LogFactory.getLog(EndPointAdminClient.class);

    private final String serviceName = "EndpointAdmin";
    private EndpointAdminStub endpointAdminStub;
    private String endPoint;

    public EndPointAdminClient(String backEndUrl, String sessionCookie) throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        endpointAdminStub = new EndpointAdminStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, endpointAdminStub);
    }

    public EndPointAdminClient(String backEndURL, String userName, String password)
            throws AxisFault {
        this.endPoint = backEndURL + serviceName;
        endpointAdminStub = new EndpointAdminStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, endpointAdminStub);
    }

    public void addEndPoint(DataHandler dh)
            throws EndpointAdminEndpointAdminException, IOException, XMLStreamException {
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(dh.getInputStream());
        //create the builder
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        OMElement endPointElem = builder.getDocumentElement();
        endpointAdminStub.addEndpoint(endPointElem.toString());
    }

    public void addEndPoint(OMElement endPointElem)
            throws EndpointAdminEndpointAdminException, IOException, XMLStreamException {
        endpointAdminStub.addEndpoint(endPointElem.toString());
    }

    public void deleteEndPoint(String endPointName)
            throws EndpointAdminEndpointAdminException, RemoteException {
        endpointAdminStub.deleteEndpoint(endPointName);
    }

    public int getEndpointCount() throws EndpointAdminEndpointAdminException, RemoteException {
        return endpointAdminStub.getEndpointCount();
    }

    public String[] getEndpointNames() throws EndpointAdminEndpointAdminException, RemoteException {
        return endpointAdminStub.getEndPointsNames();
    }

    public void enableEndpointStatistics(String endpointName)
            throws RemoteException, EndpointAdminEndpointAdminException {
        endpointAdminStub.enableStatistics(endpointName);
        String endpoint = endpointAdminStub.getEndpointConfiguration(endpointName);
        assert (endpoint.contains("statistics=\"enable"));
    }

    public void deleteEndpoint(String endpointName)
            throws RemoteException, EndpointAdminEndpointAdminException {
        endpointAdminStub.deleteEndpoint(endpointName);
    }

    public String getEndpointConfiguration(String endpointName)
            throws RemoteException, EndpointAdminEndpointAdminException {
        return endpointAdminStub.getEndpointConfiguration(endpointName);
    }
}
