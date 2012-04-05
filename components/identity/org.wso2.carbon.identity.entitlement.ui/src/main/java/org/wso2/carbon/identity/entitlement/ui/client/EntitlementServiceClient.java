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
package org.wso2.carbon.identity.entitlement.ui.client;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.stub.EntitlementServiceStub;

public class EntitlementServiceClient {

    private EntitlementServiceStub stub;
    private static final Log log = LogFactory.getLog(EntitlementServiceClient.class);

    /**
     * Instantiates EntitlementServiceClient
     * 
     * @param cookie For session management
     * @param backendServerURL URL of the back end server where EntitlementService is running.
     * @param configCtx ConfigurationContext
     * @throws org.apache.axis2.AxisFault
     */
    public EntitlementServiceClient(String cookie, String backendServerURL,
            ConfigurationContext configCtx) throws AxisFault {
        String serviceURL = backendServerURL + "EntitlementService";
        stub = new EntitlementServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     * 
     * @param request
     * @return
     * @throws AxisFault
     */
    public String getDecision(String request) throws AxisFault {
        try {
            if(request != null){
                request = request.trim().replaceAll("&lt;", "<"); //TODO should be properly fixed
                request = request.trim().replaceAll("&gt;", ">");                 
            }
            return getStatus(stub.getDecision(request));
        } catch (Exception e) {
            handleException("Error occurred while policy evaluation", e);
        }
        return null;
    }

    /**
     * Logs and wraps the given exception.
     * 
     * @param msg Error message
     * @param e Exception
     * @throws AxisFault
     */
    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    /**
     * 
     * @param response
     * @return
     * @throws Exception
     */
    private String getStatus(String xmlstring) throws Exception {
        OMElement response = null;
        OMElement result = null;
        OMElement decision = null;
        response = AXIOMUtil.stringToOM(xmlstring);

        result = response.getFirstChildWithName(new QName("Result"));
        if (result != null) {
            decision = result.getFirstChildWithName(new QName("Decision"));
            if (decision != null) {
                return decision.getText();
            }
        }

        return "Invalid Status";
    }
}
