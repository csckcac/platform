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
package org.wso2.carbon.identity.scim.consumer.internal;

import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.scim.consumer.extensions.AddUserExtension;
import org.wso2.carbon.server.admin.privilegedaction.PrivilegedAction;

import org.apache.commons.logging.Log;

/**
 * @scr.component name="identity.scim.consumer" immediate="true"
 */
public class SCIMConsumerComponent {

    private static Log logger = LogFactory.getLog(SCIMConsumerComponent.class.getName());

    protected void activate(ComponentContext cxt) {
        AddUserExtension addUserExtension = new AddUserExtension();
        //register add user extension
        cxt.getBundleContext().registerService(PrivilegedAction.class.getName(), addUserExtension, null);
        
        if(logger.isDebugEnabled()){
            logger.debug("SCIMConsumerComponent activated successfully.");
        }
    }

}
