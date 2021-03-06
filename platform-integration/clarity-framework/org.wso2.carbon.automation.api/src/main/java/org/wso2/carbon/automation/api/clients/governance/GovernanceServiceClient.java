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
package org.wso2.carbon.automation.api.clients.governance;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.governance.services.stub.AddServicesServiceRegistryExceptionException;
import org.wso2.carbon.governance.services.stub.AddServicesServiceStub;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.rmi.RemoteException;

public class GovernanceServiceClient {
    private static final Log log = LogFactory.getLog(GovernanceServiceClient.class);

    private final String serviceName = "AddServicesService";
    private AddServicesServiceStub addServicesServiceStub;
    private String endPoint;

    public GovernanceServiceClient(String backEndUrl, String sessionCookie) throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        addServicesServiceStub = new AddServicesServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, addServicesServiceStub);
    }

    public GovernanceServiceClient(String backEndUrl, String userName, String password)
            throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        addServicesServiceStub = new AddServicesServiceStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, addServicesServiceStub);

    }

    public String addService(OMElement service)
            throws IOException, XMLStreamException, AddServicesServiceRegistryExceptionException {
        return addServicesServiceStub.addService(service.toString());
    }

    public String getServicePath() throws Exception{
        String servicePath = null;
        try {
            return addServicesServiceStub.getServicePath();
        } catch (Exception e) {
           log.info("Error on getting service paths");
        }
        return servicePath;
    }

}
