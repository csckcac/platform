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
import org.wso2.charon.core.objects.Group;
import org.wso2.charon.core.schema.SCIMConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AddGroupExtension extends AbstractPrivilegedActionExtension
        implements PrivilegedAction {
    private static Log logger = LogFactory.getLog(AddGroupExtension.class.getName());
    private final String EXTENSION_NAME = "addGroupExtension";
    private final String SOAP_ACTION = "addRole";
    private final int PRIORITY = 1;

    public AddGroupExtension() {
        initConfigManager();
    }

    public void execute(MessageContext inMessageContext, MessageContext messageContext1)
            throws PrivilegedActionException {
        if (logger.isDebugEnabled()) {
            logger.debug("AddGroup SCIM Extension was invoked...");
        }
        try {
            //TODO:hand it over to a separate thread
            //get the tenant domain from message context
            /*String tenantDomain = (String) inMessageContext.getProperty(
                    SCIMConsumerConstants.TENANT_DOMAIN_ELEMENT_NAME);*/
            String tenantDomain = CarbonContext.getCurrentContext().getTenantDomain();

            if (isSCIMConsumerEnabled(tenantDomain)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("AddGroup SCIM Extension is being executed for tenant : " + tenantDomain);
                }
                //extract group info from SOAP envelope

                //extract role name
                OMElement bodyElement = inMessageContext.getEnvelope().getBody().getFirstElement();
                OMElement roleNameElement = bodyElement.getFirstChildWithName(
                        new QName(SCIMConsumerConstants.USER_MGT_NS,
                                  SCIMConsumerConstants.ROLE_NAME_ELEMENT_NAME));
                String roleName = roleNameElement.getText();

                //extract members list
                Iterator<OMElement> membersList = bodyElement.getChildrenWithName(
                        new QName(SCIMConsumerConstants.USER_MGT_NS, SCIMConsumerConstants.USER_LIST_ELEMENT_NAME));
                if (membersList != null) {
                    List<String> memberIdList = new ArrayList<String>();
                    //Map<String, String> membersMap = new HashMap<String, String>();
                    while (membersList.hasNext()) {
                        OMElement memberElement = membersList.next();
                        String memberName = memberElement.getText();
                        //TODO:Do a SCIM filter request and obtain the user's SP Id
                        //SCIMClient scimClient = new SCIMClient();
                        //add to memberIdList
                        memberIdList.add(memberName);
                    }
                    SCIMClient scimClient = new SCIMClient();
                    Group group = scimClient.createGroup();
                    group.setDisplayName(roleName);
                    for (String member : memberIdList) {
                        group.setMember("", member);
                    }
                    this.provision(tenantDomain, group, SCIMConstants.POST);
                }
            }
        } catch (CharonException e) {
            logger.error("Error in creating SCIM User..", e);
        } catch (IdentitySCIMException e) {
            logger.error(e.getMessage());
        }

    }

    public int getPriority() {
        return PRIORITY;  
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
    }*/

    /*@Override*/
    public String getSOAPAction() {
        return SOAP_ACTION;
    }

    public boolean isDisabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getExtensionName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean skipServiceInvocation() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean skipLowerPriorityExtensions() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isAfterServiceCall() {
        return false;
    }

    @Override
    public boolean isSCIMConsumerEnabled(String tenantDomain) {
        return ((provisioningManager.isConsumerRegistered(tenantDomain)) &&
                (provisioningManager.isAppliedToPrivilegedActions(tenantDomain)));
    }
}
