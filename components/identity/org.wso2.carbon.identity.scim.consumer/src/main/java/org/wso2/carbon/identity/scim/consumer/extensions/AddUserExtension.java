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

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.server.admin.privilegedaction.PrivilegedAction;
import org.wso2.carbon.server.admin.privilegedaction.PrivilegedActionException;

import org.apache.commons.logging.Log;

public class AddUserExtension implements PrivilegedAction {

    private static Log logger = LogFactory.getLog(AddUserExtension.class.getName());
    private final String EXTENSION_NAME = "addUserExtension";
    private final String SOAP_ACTION = "addUser";
    private final int PRIORITY = 1;

    public void execute(MessageContext messageContext, MessageContext messageContext1)
            throws PrivilegedActionException {
        //temporary
        logger.info("AddUser Extension was invoked...");
    }

    public int getPriority() {
        return PRIORITY;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean doesHandle(MessageContext messageContext) {
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

    public boolean isDisabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getExtensionName() {
        return EXTENSION_NAME;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean skipServiceInvocation() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean skipLowerPriorityExtensions() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
