/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.governance.lcm.listener;

import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.core.services.callback.*;
import org.wso2.carbon.governance.lcm.util.CommonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;

import java.util.List;
import java.util.LinkedList;

public class LifecycleLoader implements LoginListener {
    
    private static Log log = LogFactory.getLog(LifecycleLoader.class);

    private List<Integer> initializedTenants = new LinkedList<Integer>();

    public void onLogin(Registry configRegistry, LoginEvent loginEvent) {
        try {
            if (initializedTenants.contains(loginEvent.getTenantId())) {
                return;
            }
            initializedTenants.add(loginEvent.getTenantId());
            SuperTenantCarbonContext.startTenantFlow();
            try {
                SuperTenantCarbonContext.getCurrentContext().setTenantId(loginEvent.getTenantId());
                SuperTenantCarbonContext.getCurrentContext().setUsername(loginEvent.getUsername());
                CommonUtil.addDefaultLifecyclesIfNotAvailable(configRegistry,
                        CommonUtil.getRootSystemRegistry());
            } finally {
                SuperTenantCarbonContext.endTenantFlow();
            }
        } catch (Exception e) {
            String msg = "Error in adding the default lifecycles";
            log.error(msg, e);
        }
    }
}
