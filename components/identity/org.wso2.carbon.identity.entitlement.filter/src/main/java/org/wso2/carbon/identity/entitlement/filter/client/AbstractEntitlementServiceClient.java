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
package org.wso2.carbon.identity.entitlement.filter.client;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.filter.exception.EntitlementFilterException;

import javax.xml.namespace.QName;
import java.util.Properties;

public abstract class AbstractEntitlementServiceClient {

    private static final Log log = LogFactory.getLog(BasicAuthEntitlementServiceClient.class);

  /**
     * init the class by passing some properties
     *
     * @param properties Properties
     * @throws Exception if any error
     */
    public abstract void init(Properties properties) throws EntitlementFilterException;

  /**
     * Returns the decision as <code>String</code> Object
     *
     * @param userName  user name of the user who wants access
     * @param resource    resource that is accessed
     * @param action        action on resource
     * @param env           environment
     * @return  Permit of Deny
     * @throws Exception if any error
     */
    public abstract String getDecision(String userName, String resource, String action,
                       String[] env) throws EntitlementFilterException;


    /**
     * Extract the decision from PDP response
     *
     * @param xmlString PDP response string
     * @throws EntitlementFilterException if any error
     */
    public String getStatus(String xmlString) throws EntitlementFilterException {
        OMElement decision;
        OMElement result;
        try {
            result = (AXIOMUtil.stringToOM(xmlString)).getFirstChildWithName(new QName("Result"));
            decision = result.getFirstChildWithName(new QName("Decision"));
            return decision.getText();
        } catch (Exception e) {
            throw new EntitlementFilterException("Unable to parse response string " + xmlString, e);
        }
    }
}
