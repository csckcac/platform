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


import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cloud.csg.agent.observer.CSGServiceObserver;
import org.wso2.carbon.cloud.csg.agent.service.CSGAgentAdminService;
import org.wso2.carbon.cloud.csg.common.CSGConstant;
import org.wso2.carbon.cloud.csg.common.CSGException;
import org.wso2.carbon.cloud.csg.common.CSGUtils;
import org.wso2.carbon.core.util.SystemFilter;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private long initialReconnectDuration;

    private double reconnectionProgressionFactor;

    /**
     * Keep track of published services to re-publish
     */
    private List<String> pendingServices = new ArrayList<String>();

    protected void activate(ComponentContext context) {
        if (this.configurationContextService == null) {
            log.error("Cloud not activated the CSGAgentServiceComponent. " +
                    "ConfigurationContextService is null!");
            return;
        }

        initialReconnectDuration = CSGUtils.getLongProperty(CSGConstant.INITIAL_RECONNECT_DURATION, 10000);
        reconnectionProgressionFactor = CSGUtils.getDoubleProperty(CSGConstant.PROGRESSION_FACTOR, 2.0);

        // register observers for automatic published services
        AxisConfiguration axisConfig =
                this.configurationContextService.getServerConfigContext().getAxisConfiguration();
        CSGServiceObserver observer = new CSGServiceObserver();
        axisConfig.addObservers(observer);

        String[] publishOptimizedList = UserCoreUtil.optimizePermissions(
                CSGConstant.CSG_PUBLISH_PERMISSION_LIST);

        String[] unPublishOptimizedList = UserCoreUtil.optimizePermissions(
                CSGConstant.CSG_UNPUBLISH_PERMISSION_LIST);
        try {
            // add the publish and un publish roles
            UserRealm realm = realmService.getBootstrapRealm();
            AuthorizationManager authorizationManager = realm.getAuthorizationManager();
            authorizationManager.clearRoleActionOnAllResources(CSGConstant.CSG_PUBLISH_ROLE_NAME,
                    UserMgtConstants.EXECUTE_ACTION);
            authorizationManager.clearRoleActionOnAllResources(CSGConstant.CSG_UNPUBLISH_ROLE_NAME,
                    UserMgtConstants.EXECUTE_ACTION);
            for (String permission : publishOptimizedList) {
                authorizationManager.authorizeRole(CSGConstant.CSG_PUBLISH_ROLE_NAME, permission,
                        UserMgtConstants.EXECUTE_ACTION);
            }

            for (String permission : unPublishOptimizedList) {
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

            // look for any published service and published them again
            for (Map.Entry<String, AxisService> entry : axisConfig.getServices().entrySet()) {
                AxisService axisService = entry.getValue();
                CSGAgentAdminService service = new CSGAgentAdminService();
                String status = service.getServiceStatus(axisService.getName());

                if (SystemFilter.isAdminService(axisService) || SystemFilter.isHiddenService(axisService) ||
                        axisService.isClientSide() || status.equals(CSGConstant.CSG_SERVICE_STATUS_UNPUBLISHED)) {
                    continue;
                }
                pendingServices.add(axisService.getName());
            }
            if (pendingServices.size() > 0) {
                new Thread(new ServiceRePublishingTask(), "CSG-re-publishing-thread").start();
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

    private class ServiceRePublishingTask implements Runnable {

        public void run() {
            long retryDuration = initialReconnectDuration;
            while (true) {
                if (CSGUtils.isServerAlive("localhost",
                        CarbonUtils.getTransportPort(configurationContextService, "http"))) {
                    CSGAgentAdminService adminService = new CSGAgentAdminService();
                    for (String serviceName : pendingServices) {
                        try {
                            boolean isAutomatic = adminService.getServiceStatus(serviceName).equals(
                                    CSGConstant.CSG_SERVICE_STATUS_AUTO_MATIC);
                            String serverName = adminService.getPublishedServer(serviceName);
                            adminService.unPublishService(serviceName, serverName);
                            adminService.publishService(serviceName, serverName, isAutomatic);
                        } catch (CSGException e) {
                            log.error("Error while re-publishing the previously published service '" + serviceName + "'," +
                                    " you will need to re-publish the service manually!", e);
                        }
                        log.info("Service '" + serviceName + "', re-published successfully");
                    }

                    break;
                } else {
                    // re-try until success
                    retryDuration = (long) (retryDuration * reconnectionProgressionFactor);
                    try {
                        Thread.sleep(retryDuration);
                    } catch (InterruptedException ignore) {
                    }
                }
            }
        }
    }
}
