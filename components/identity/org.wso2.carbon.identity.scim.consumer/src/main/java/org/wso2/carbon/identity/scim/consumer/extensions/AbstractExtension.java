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
import org.wso2.carbon.identity.scim.consumer.config.SCIMConsumersConfig;
import org.wso2.carbon.identity.scim.consumer.utils.IdentitySCIMException;
import org.wso2.carbon.identity.scim.consumer.utils.SCIMConsumerConstants;
import org.wso2.carbon.server.admin.privilegedaction.PrivilegedAction;
import org.wso2.carbon.server.admin.privilegedaction.PrivilegedActionException;
import org.wso2.charon.core.schema.SCIMConstants;

public abstract class AbstractExtension implements PrivilegedAction {

    public boolean doesHandle(MessageContext messageContext) {
        String method;
        AxisOperation operation = messageContext.getOperationContext().getAxisOperation();
        if ((operation.getName() != null) && ((method =
                JavaUtils.xmlNameToJavaIdentifier(operation.getName().getLocalPart())) != null)) {
            if (method.equals(getSOAPAction())) {
                return true;
            }
        }
        return false;
    }

    public abstract String getSOAPAction();

    public String getSCIMEndpoint(String tenantDomain, String endpoint)
            throws IdentitySCIMException {
        SCIMConsumersConfig.SCIMConsumerConfig consumerConfig =
                SCIMConsumersConfig.getScimConsumers().get(tenantDomain);
        return consumerConfig.getProperty(endpoint);
        //TODO:if not present, throw exception
    }

    public String getUserName(String tenantDomain) throws IdentitySCIMException {
        SCIMConsumersConfig.SCIMConsumerConfig consumerConfig =
                SCIMConsumersConfig.getScimConsumers().get(tenantDomain);
        return consumerConfig.getProperty(SCIMConsumerConstants.USER_NAME);
        //TODO:if not present, throw exception
    }

    public String getPassword(String tenantDomain) throws IdentitySCIMException {
        SCIMConsumersConfig.SCIMConsumerConfig consumerConfig =
                SCIMConsumersConfig.getScimConsumers().get(tenantDomain);
        return consumerConfig.getProperty(SCIMConsumerConstants.PASSWORD);
        //TODO:if not present, throw exception
    }

    public boolean isSCIMConsumerEnabled(String tenantDomain) throws IdentitySCIMException {
        if (SCIMConsumersConfig.getScimConsumers().containsKey(tenantDomain)) {
            SCIMConsumersConfig.SCIMConsumerConfig consumerConfig =
                    SCIMConsumersConfig.getScimConsumers().get(tenantDomain);
            return Boolean.parseBoolean(consumerConfig.getProperty(SCIMConsumerConstants.ENABLE));
        } else {
            return false;
        }
    }
}
