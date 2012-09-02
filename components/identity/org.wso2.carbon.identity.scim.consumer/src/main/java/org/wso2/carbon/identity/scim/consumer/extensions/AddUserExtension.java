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
package org.wso2.carbon.identity.scim.consumer.extensions;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.scim.common.utils.IdentitySCIMException;
import org.wso2.carbon.identity.scim.consumer.utils.SCIMConsumerConstants;
import org.wso2.carbon.server.admin.privilegedaction.PrivilegedAction;
import org.wso2.carbon.server.admin.privilegedaction.PrivilegedActionException;
import org.wso2.charon.core.client.SCIMClient;
import org.wso2.charon.core.exceptions.CharonException;
import org.wso2.charon.core.objects.User;
import org.wso2.charon.core.schema.SCIMConstants;

import javax.xml.namespace.QName;

public class AddUserExtension extends AbstractPrivilegedActionExtension
        implements PrivilegedAction {

    private static Log logger = LogFactory.getLog(AddUserExtension.class.getName());
    private final String EXTENSION_NAME = "addUserExtension";
    private final String SOAP_ACTION = "addUser";
    private final int PRIORITY = 1;

    public AddUserExtension() {
        initConfigManager();
    }

    @Override
    public boolean isSCIMConsumerEnabled(String tenantDomain) {
        return ((provisioningManager.isConsumerRegistered(tenantDomain)) &&
                (provisioningManager.isAppliedToPrivilegedActions(tenantDomain)));
    }

    public void execute(MessageContext inMessageContext, MessageContext outMessageContext)
            throws PrivilegedActionException {
        if (logger.isDebugEnabled()) {
            logger.debug("AddUser SCIM Extension was invoked...");
        }
        try {
            //TODO:hand it over to a separate thread
            //get the tenant domain from message context
            /*String tenantDomain = (String) inMessageContext.getProperty(
                    SCIMConsumerConstants.TENANT_DOMAIN_ELEMENT_NAME);*/
            String tenantDomain = CarbonContext.getCurrentContext().getTenantDomain();

            if (isSCIMConsumerEnabled(tenantDomain)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("AddUser SCIM Extension is being executed for tenant : " + tenantDomain);
                }
                //extract info from SOAP envelope
                String userName = null;
                String password = null;
                OMElement passwordElement = null;

                //extract user name
                OMElement bodyElement = inMessageContext.getEnvelope().getBody().getFirstElement();
                OMElement userNameElement = bodyElement.getFirstChildWithName(
                        new QName(SCIMConsumerConstants.USER_MGT_NS,
                                  SCIMConsumerConstants.USER_NAME_ELEMENT_NAME));
                userName = userNameElement.getText();
                //extract password element
                passwordElement = bodyElement.getFirstChildWithName(
                        new QName(SCIMConsumerConstants.USER_MGT_NS,
                                  SCIMConsumerConstants.PASSWORD_ELEMENT_NAME));
                password = passwordElement.getText();

                //create SCIM user
                SCIMClient scimClient = new SCIMClient();
                User user = scimClient.createUser();
                user.setUserName(userName);
                user.setPassword(password);

                this.provision(tenantDomain, user, SCIMConstants.POST);
            }
        } catch (CharonException e) {
            logger.error("Error in creating SCIM User..", e);
        } catch (IdentitySCIMException e) {
            logger.error(e.getMessage());
        }
    }

    public int getPriority() {
        return PRIORITY;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /*public boolean doesHandle(MessageContext messageContext) {
        String method;
        AxisOperation op = messageContext.getOperationContext().getAxisOperation();
        if ((op.getName() != null) && ((method =
                JavaUtils.xmlNameToJavaIdentifier(op.getName().getLocalPart())) != null)) {
            if (method.equals(SOAP_ACTION)) {
                return true;
            }
        }
        return false;
    }
*/

    @Override
    public String getSOAPAction() {
        return SOAP_ACTION;
    }

    public boolean isDisabled() {
        return false;
    }

    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    public boolean skipServiceInvocation() {
        return false;
    }

    public boolean skipLowerPriorityExtensions() {
        return false;
    }

    public boolean isAfterServiceCall() {
        return true;
    }
}
