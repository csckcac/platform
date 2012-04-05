/*
 *  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.entitlement.mediator.client;

import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;

import java.lang.Exception;
import java.util.Properties;

/**
 * This is abstract entitlement client  which calls the entitlement service in WSO2 Identity Server
 * There may be different ways of calling that service as an example, one may be authenticate with
 * WSO2 Identity server using default authenticator, other may be authenticate with Basic auth
 * Also there can be implementation on different transport other than HTTP.
 */
public abstract class EntitlementServiceClient{

    /**
     * init the class by passing some properties
     * @param properties Properties
     */
    public abstract void init(Properties properties);

    /**
     * Returns the decision as <code>String</code> Object
     * @param userName  user name of the user who access the resource
     * @param resource resource that is accessed
     * @param action  action on resource
     * @param env  environment
     * @return  Permit of Deny
     * @throws Exception if any error 
     */
    public abstract String getDecision(String userName, String resource, String action, String[] env)
            throws Exception;

    /**
     * Extracts the decision from XACML request
     * @param xmlstring  XACML request as String
     * @return Decision
     * @throws Exception if any error
     */
    protected String getStatus(String xmlstring) throws Exception {
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
