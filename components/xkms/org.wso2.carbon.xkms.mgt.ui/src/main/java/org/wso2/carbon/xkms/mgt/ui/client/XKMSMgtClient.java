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
package org.wso2.carbon.xkms.mgt.ui.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.wso2.carbon.xkms.mgt.stub.XKMSAdminServiceStub;
import org.wso2.carbon.xkms.mgt.stub.types.XKMSConfigData;

public class XKMSMgtClient {

    private XKMSAdminServiceStub stub;
    private static final Log log = LogFactory.getLog(XKMSMgtClient.class);

    /**
     * Instantiates XKMSMgtClient
     *
     * @param cookie           For session management
     * @param backendServerURL URL of the back end server where UserRegistrationAdminService is
     *                         running.
     * @param configCtx        ConfigurationContext
     * @throws org.apache.axis2.AxisFault
     */
    public XKMSMgtClient(String cookie, String backendServerURL,
                         ConfigurationContext configCtx) throws AxisFault {
        String serviceURL = backendServerURL + "XKMSAdminService";
        stub = new XKMSAdminServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }


    public void setXKMSConfig(XKMSConfigData configData) throws Exception {
        stub.setXKMSConfig(configData);
    }

    public XKMSConfigData getXKMSConfig() throws Exception {
        return stub.getXKMSConfig();
    }

}
