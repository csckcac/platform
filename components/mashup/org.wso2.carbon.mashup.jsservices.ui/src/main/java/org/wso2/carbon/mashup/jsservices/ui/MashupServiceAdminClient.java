/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.mashup.jsservices.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.mashup.jsservices.stub.CarbonExceptionException;
import org.wso2.carbon.mashup.jsservices.stub.MashupServiceAdminStub;

import java.rmi.RemoteException;

public class MashupServiceAdminClient {

    private MashupServiceAdminStub stub = null;
    private static Log log = LogFactory.getLog(MashupServiceAdminClient.class);


    public MashupServiceAdminClient(String cookie, String url, ConfigurationContext configContext)
            throws AxisFault {
        String serviceEndPoint = null;
        try {
            serviceEndPoint = url + "MashupServiceAdmin";
            stub = new MashupServiceAdminStub(configContext, serviceEndPoint);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault e) {
            log.error("Error occurred while connecting via stub to :" + serviceEndPoint, e);
            throw e;
        }
    }

    public String[] getMashupServiceContentAsString(String serviceName) throws CarbonException {
        try {
            return stub.getMashupServiceContentAsString(serviceName);
        } catch (RemoteException e) {
            log.error("Error occured while trying to retrieve the contents of the JS Service " +
                      serviceName, e);
            throw new CarbonException(e);
        }
    }

    public boolean saveMashupServiceSource(String serviceName, String contents, String type)
            throws CarbonException {
        try {
            return stub.saveMashupServiceSource(serviceName, type, contents);
        } catch (RemoteException e) {
            log.error("Error occured while trying to save the the JS Service " + serviceName, e);
            throw new CarbonException(e);
        } catch (CarbonExceptionException e) {
            log.error("Error occured while trying to save the the JS Service " + serviceName, e);
            throw new CarbonException(e);
        }
    }

    public boolean doesServiceExists(String serviceName) throws CarbonException {
        try {
            return stub.doesServiceExists(serviceName);
        } catch (RemoteException e) {
            throw new CarbonException(e);
        } catch (CarbonExceptionException e) {
            throw new CarbonException(e);
        }
    }

    public String[] doesServicesExists(String[] serviceNames) throws CarbonException {
        try {
            return stub.doesServicesExists(serviceNames);
        } catch (RemoteException e) {
            throw new CarbonException(e);
        } catch (CarbonExceptionException e) {
            throw new CarbonException(e);
        }
    }

    public String getBackendHttpPort() throws CarbonException {
        try {
            return stub.getBackendHttpPort();
        } catch (RemoteException e) {
            throw new CarbonException(e);
        } catch (CarbonExceptionException e) {
            throw new CarbonException(e);
        }
    }


}
