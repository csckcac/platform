/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cloud.csg.agent.internal;


import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cloud.csg.agent.observer.CSGServiceObserver;
import org.wso2.carbon.cloud.csg.common.CSGConstant;
import org.wso2.carbon.cloud.csg.common.CSGUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.cloud.csg.agent.internal.CSGAgentServiceComponent" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */

@SuppressWarnings({"UnusedDeclaration"})
public class CSGAgentServiceComponent {

    private static Log log = LogFactory.getLog(CSGAgentServiceComponent.class);

    private ConfigurationContextService configurationContextService;

    private RealmService realmService;

    protected void activate(ComponentContext context) {
        if (this.configurationContextService == null) {
            log.error("Cloud not activated the CSGAgentServiceComponent. " +
                    "ConfigurationContextService is null!");
            return;
        }
        // register observers for automatic published services
        AxisConfiguration axisConfig =
                this.configurationContextService.getServerConfigContext().getAxisConfiguration();
        CSGServiceObserver observer = new CSGServiceObserver();
        axisConfig.addObservers(observer);

        String[] optimizedList = UserCoreUtil.optimizePermissions(CSGUtils.getPermissionsList());
        try {
            // add the publish and un publish roles
            UserRealm realm = realmService.getBootstrapRealm();
            AuthorizationManager authorizationManager = realm.getAuthorizationManager();
            authorizationManager.clearRoleActionOnAllResources(CSGConstant.CSG_PUBLISH_ROLE_NAME,
                    UserMgtConstants.EXECUTE_ACTION);
            authorizationManager.clearRoleActionOnAllResources(CSGConstant.CSG_UNPUBLISH_ROLE_NAME,
                    UserMgtConstants.EXECUTE_ACTION);
            for (String permission : optimizedList) {
                authorizationManager.authorizeRole(CSGConstant.CSG_PUBLISH_ROLE_NAME, permission,
                        UserMgtConstants.EXECUTE_ACTION);
                authorizationManager.authorizeRole(CSGConstant.CSG_UNPUBLISH_ROLE_NAME, permission,
                        UserMgtConstants.EXECUTE_ACTION);
            }

            UserStoreManager manager = realm.getUserStoreManager();
            if (!manager.isExistingRole(CSGConstant.CSG_PUBLISH_ROLE_NAME)) {
                manager.addRole(CSGConstant.CSG_PUBLISH_ROLE_NAME, null, null);
            }

            if (!manager.isExistingRole(CSGConstant.CSG_UNPUBLISH_ROLE_NAME)) {
                manager.addRole(CSGConstant.CSG_UNPUBLISH_ROLE_NAME, null, null);
            }

        } catch (Exception e) {
            log.error("Cloud not activated the CSGAgentServiceComponent. ", e);
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Activated the CSGAgentServiceComponent");
        }
    }

    protected void deactivate(ComponentContext context) {

    }

    protected void setConfigurationContextService(ConfigurationContextService configCtxService) {
        this.configurationContextService = configCtxService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        if (this.configurationContextService != null) {
            this.configurationContextService = null;
        }
    }

    protected void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        if (this.realmService != null) {
            this.realmService = null;
        }
    }
}
