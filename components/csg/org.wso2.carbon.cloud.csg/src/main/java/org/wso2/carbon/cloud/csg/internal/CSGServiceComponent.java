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
package org.wso2.carbon.cloud.csg.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.transport.base.threads.WorkerPoolFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.cloud.csg.common.CSGConstant;
import org.wso2.carbon.cloud.csg.common.CSGUtils;
import org.wso2.carbon.cloud.csg.transport.server.CSGThriftServer;
import org.wso2.carbon.cloud.csg.transport.server.CSGThriftServerHandler;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.mgt.UserMgtConstants;

import java.net.SocketException;
import java.util.HashMap;

/**
 * @scr.component name="org.wso2.carbon.cloud.csg.internal.CSGServiceComponent" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="server.configuration"
 * interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setServerConfiguration"
 * unbind="unsetServerConfiguration"
 */
public class CSGServiceComponent {
    private static Log log = LogFactory.getLog(CSGServiceComponent.class);

    private ServerConfigurationService serverConfiguration;
    private RealmService realmService;

    protected void activate(ComponentContext ctxt) {
        // CSG needs to know the key store location and also add the csg user etc..
        if (this.serverConfiguration == null || this.realmService == null) {
            log.error("Could not activated the CSGServiceComponent. " +
                    (this.serverConfiguration == null ?
                            "ServerConfigurationService" : "RealmService") + "is null!");
            return;
        }
        try {
            // add the default csguser into the user store
            String csgRoleName = CSGUtils.getStringProperty(CSGConstant.CSG_ROLE_NAME,
                    CSGConstant.DEFAULT_CSG_ROLE_NAME);
            String[] permissionList;
            String permissionString = CSGUtils.getStringProperty(
                    CSGConstant.CSG_USER_PERMISSION_LIST, null);
            if (permissionString == null) {
                permissionList = CSGConstant.CSG_USER_DEFAULT_PERMISSION_LIST;
            } else {
                // permission string can be configured as a system property as
                // csg-user-permission-list=permission1,permission2,permission3 etc..
                permissionList = new String[]{};
                int i = 0;
                for (String permission : permissionString.split(",")) {
                    permissionList[++i] = permission.trim();
                }
            }
            addCSGUser(
                    csgRoleName,
                    permissionList,
                    CSGUtils.getStringProperty(CSGConstant.CSG_USER, CSGConstant.DEFAULT_CSG_USER),
                    CSGUtils.getStringProperty(CSGConstant.CSG_USER_PASSWORD,
                            CSGConstant.DEFAULT_CSG_USER_PASSWORD));

        } catch (UserStoreException e) {
            log.error("Cloud not activated the CSGServiceComponent.", e);
            return;
        }
        String hostName;
        try {
            hostName = CSGUtils.getCSGThriftServerHostName();
        } catch (SocketException e) {
            log.error("Could not activated the CSGServiceComponent.", e);
            return;
        }

        int port = CSGUtils.getCSGThriftServerPort();
        int timeOut = CSGUtils.getIntProperty(CSGConstant.CSG_THRIFT_CLIENT_TIMEOUT,
                CSGConstant.DEFAULT_TIMEOUT);
        String keyStoreURL = CSGUtils.getWSO2KeyStoreFilePath();
        if (keyStoreURL == null) {
            log.error("KeyStore is missing and required for encryption");
            return;
        }

        String keyStorePassWord = CSGUtils.getWSO2KeyStorePassword();
        if (keyStorePassWord == null) {
            log.error("KeyStore password is missing");
            return;
        }

        WorkerPool workerPool =
                WorkerPoolFactory.getWorkerPool(
                        CSGUtils.getIntProperty(
                                CSGConstant.CSG_T_CORE, CSGConstant.WORKERS_CORE_THREADS),
                        CSGUtils.getIntProperty(
                                CSGConstant.CSG_T_MAX, CSGConstant.WORKERS_MAX_THREADS),
                        CSGUtils.getIntProperty(
                                CSGConstant.CSG_T_ALIVE, CSGConstant.WORKER_KEEP_ALIVE),
                        CSGUtils.getIntProperty(
                                CSGConstant.CSG_T_QLEN, CSGConstant.WORKER_BLOCKING_QUEUE_LENGTH),
                        "CSGThriftServerHandler-worker-thread-group",
                        "CSGThriftServerHandler-worker");
        CSGThriftServerHandler csgThriftServerHandler = new CSGThriftServerHandler(workerPool);
        CSGThriftServer server = new CSGThriftServer(csgThriftServerHandler);
        try {
            server.start(hostName, port, timeOut, keyStoreURL, keyStorePassWord,
                    "CSG-ThriftServer-main-thread");
        } catch (AxisFault axisFault) {
            log.error(axisFault);
            return;
        }
        if (log.isDebugEnabled()) {
            log.info("Activated the CSGServiceComponent");
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

    protected void setServerConfiguration(ServerConfigurationService configuration) {
        serverConfiguration = configuration;
    }

    protected void unsetServerConfiguration(ServerConfigurationService configuration) {
        serverConfiguration = null;
    }

    private void addCSGUser(String roleName,
                            String[] permissionList,
                            String csgUserName,
                            String passWord)
            throws UserStoreException {
        // add the required permission to the csg role
        String[] optimizedList = UserCoreUtil.optimizePermissions(permissionList);
        UserRealm realm = realmService.getBootstrapRealm();
        if (realm.getRealmConfiguration().getAdminRoleName().equals(roleName)) {
            throw new UserStoreException("UI permission of admin is not allowed to change!");
        }
        AuthorizationManager authorizationManager = realm.getAuthorizationManager();
        authorizationManager.clearRoleActionOnAllResources(roleName,
                UserMgtConstants.EXECUTE_ACTION);
        for (String permission : optimizedList) {
            authorizationManager.authorizeRole(roleName, permission,
                    UserMgtConstants.EXECUTE_ACTION);
        }

        // set required permission for csguser to put/get/delete WSDLs etc..
        authorizationManager.authorizeRole(roleName, "/", "add");
        authorizationManager.authorizeRole(roleName, "/", "get");
        authorizationManager.authorizeRole(roleName, "/", "delete");

        UserStoreManager manager = realm.getUserStoreManager();
        // register the csg role if not registered already and add the csguser
        if (!manager.isExistingRole(roleName)) {
            manager.addRole(roleName, null, null);
        }

        if (!manager.isExistingUser(csgUserName)) {
            manager.addUser(
                    csgUserName,
                    passWord,
                    new String[]{roleName},
                    new HashMap<String, String>(),
                    null,
                    false);
        }
    }
}
