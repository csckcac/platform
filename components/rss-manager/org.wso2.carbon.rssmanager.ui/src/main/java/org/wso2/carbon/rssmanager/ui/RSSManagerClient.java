/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.rssmanager.ui;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.ui.beans.DatabaseInstance;
import org.wso2.carbon.rssmanager.ui.beans.DatabasePermissions;
import org.wso2.carbon.rssmanager.ui.stub.RSSAdminStub;
import org.wso2.carbon.rssmanager.ui.stub.types.*;

import java.rmi.RemoteException;
import java.util.*;

public class RSSManagerClient {

    private RSSAdminStub stub;

    private ResourceBundle bundle;

    private static final String RSS_MANAGER_EXTENSION_NS =
            "http://www.wso2.org/products/wso2commons/rssmanager";

    private static final OMNamespace RSS_MANAGER_OM_NAMESPACE = OMAbstractFactory.getOMFactory().
            createOMNamespace(RSS_MANAGER_EXTENSION_NS, "instance");

    private static final String BUNDLE = "org.wso2.carbon.rssmanager.ui.i18n.Resources";

    private static final Log log = LogFactory.getLog(RSSManagerClient.class);

    public RSSManagerClient(String cookie, String backendServerUrl,
                            ConfigurationContext configurationContext, Locale locale) {
        String serviceEndpoint = backendServerUrl + "RSSManagerAdminService";
        bundle = java.util.ResourceBundle.getBundle(BUNDLE, locale);
        try {
            stub = new RSSAdminStub(configurationContext, serviceEndpoint);
            ServiceClient serviceClient = stub._getServiceClient();
            Options options = serviceClient.getOptions();
            options.setManageSession(true);
            options.setProperty(HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault axisFault) {
            log.error(axisFault);
        }
    }

    public String removeUserPrivilegeGroup(int privGroupId) throws RemoteException {
        try {
            stub.removePrivilegeGroup(privGroupId);
            return bundle.getString("priv.group.successfully.removed");
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.remove.privilege.group"), e);
        }
        return bundle.getString("failed.to.remove.privilege.group");
    }

    public String editUserPrivilegeGroup(PrivilegeGroup priGroup) throws RemoteException {
        try {
            stub.editPrivilegeGroup(priGroup);
            return bundle.getString("priv.group.successfully.edited");
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.create.privilege.group"), e);
        }
        return bundle.getString("failed.to.create.privilege.group");
    }

    public String createUserPrivilegeGroup(PrivilegeGroup privGroup) throws RemoteException {
        try {
            stub.createPrivilegeGroup(privGroup);
            return bundle.getString("priv.group.successfully.created");
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.create.privilege.group") + ": " +
                    privGroup.getPrivGroupName(), e);
        }
        return bundle.getString("failed.to.create.privilege.group") + ": " +
                privGroup.getPrivGroupName();
    }

    public List<PrivilegeGroup> getAllPrivilegeGroups() throws RemoteException {
        try {
            PrivilegeGroup[] privGroup = stub.getPrivilegeGroups();
            if (privGroup != null) {
                return Arrays.asList(stub.getPrivilegeGroups());
            } else {
                return new ArrayList<PrivilegeGroup>();
            }
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.retrieve.privilege.group.list"), e);
        }
        return new ArrayList<PrivilegeGroup>();
    }

    public String editUserPrivileges(DatabasePermissions permissions,
                                     DatabaseUser user, int dbInsId) throws RemoteException {
        try {
            stub.editUserPrivileges(
                    RSSManagerClientUtil.serializePermissionObject(
                            RSS_MANAGER_OM_NAMESPACE, permissions).toString(), user, dbInsId);
            return bundle.getString("user.successfully.edited");
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.edit.user") + ": " + user.getUsername(), e);
        }
        return bundle.getString("failed.to.edit.user") + ": " + user.getUsername();
    }

    public DatabasePermissions getUserDatabasePermissions(int userId, int dbInsId) throws
            RemoteException {
        OMElement permissionEl = null;
        try {
            permissionEl = AXIOMUtil.stringToOM(
                    stub.getUserDatabasePermissions(userId, dbInsId));
        } catch (Exception e) {
            handleException("Unable to retrieve user database permissions", e);
        }
        return RSSManagerClientUtil.getPermissionObject(permissionEl);
    }

    public String createDatabase(DatabaseInstance db) throws RemoteException {
        try {
            stub.createDatabase(RSSManagerClientUtil.serializeDatabaseInstanceData(
                    RSS_MANAGER_OM_NAMESPACE, db).toString());
            return "Database has been successfully created";
        } catch (RemoteException e) {
            handleException(bundle.getString("failed.to.create.database") + ": " + db.getName(), e);
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.create.database") + ": " + db.getName(), e);
        }
        return bundle.getString("failed.to.create.database") + db.getName();
    }

    public DatabaseUserEntry[] getUsersByDatabase(int dbInstId) throws RemoteException {
        DatabaseUserEntry[] users = new DatabaseUserEntry[0];
        try {
            users = stub.getUsersByDatabaseInstanceId(dbInstId);
            if (users != null) {
                return users;
            }
            return users;
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.retrieve.database.users"), e);
        }
        return users;
    }

    public List<DatabaseInstanceEntry> getDatabaseInstanceList() throws RemoteException {
        try {
            DatabaseInstanceEntry[] entries = stub.getDatabaseInstanceList();
            if (entries != null) {
                return Arrays.asList(entries);
            }
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.retrieve.database.instance.list"), e);
        }
        return new ArrayList<DatabaseInstanceEntry>();
    }

    public DatabaseInstanceEntry getDatabaseInstance(int dbInsId) throws RemoteException {
        try {
            return stub.getDatabaseInstanceById(dbInsId);
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.retrieve.database.instance.data"), e);
        }
        return null;
    }

    public void dropDatabase(int dbInsId) throws RemoteException {
        try {
            stub.dropDatabase(dbInsId);
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.drop.database"), e);
        }
    }

    public List<RSSInstanceEntry> getRSSInstanceList(String tenantDomain) throws RemoteException {
        try {
            RSSInstanceEntry[] instances = stub.getRSSInstanceList();
            if (tenantDomain != null && instances != null) {
                return Arrays.asList(instances);
            }
            List<RSSInstanceEntry> localInstances = new ArrayList<RSSInstanceEntry>();
            if (instances != null) {
                for (RSSInstanceEntry rssIns : instances) {
                    if (!RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(
                            rssIns.getInstanceType())) {
                        localInstances.add(rssIns);
                    }
                }
            }
            return localInstances;
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.retrieve.RSS.instance.list"), e);
        }
        return new ArrayList<RSSInstanceEntry>();
    }

    public List<RSSInstanceEntry> getRSSInstanceList() throws RemoteException {
        try {
            RSSInstanceEntry[] instances = stub.getRSSInstanceList();
            if (instances != null) {
                return Arrays.asList(instances);
            }
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.retrieve.RSS.instance.list"), e);
        }
        return new ArrayList<RSSInstanceEntry>();
    }

    public String addRSSInstance(RSSInstance rssIns) throws RemoteException {
        try {
            stub.addRSSInstance(rssIns);
            return bundle.getString("database.server.instance.successfully.added");
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.add.database.server.instance") + ": " +
                    rssIns.getName(), e);
        }
        return bundle.getString("failed.to.add.database.server.instance") + ": " + rssIns.getName();
    }

    public String testJDBCConnection(String driverClass, String jdbcUrl,
                                     String username, String password) throws RemoteException {
        String response;
        try {
            response = stub.testConnection(driverClass, jdbcUrl, username, password);
            return response;
        } catch (Exception e) {
            handleException("Error connecting to " + jdbcUrl, e);
        }
        return "Error connecting to " + jdbcUrl;
    }

    public String removeDatabaseInstance(int rssInsId) throws RemoteException {
        try {
            stub.removeRSSInstance(rssInsId);
            return bundle.getString("database.server.instance.successfully.removed");
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.remove.database.server.instance"), e);
        }
        return null;
    }

    public String editRSSInstanceInfo(RSSInstance rssIns) throws RemoteException {
        try {
            stub.editRSSInstance(rssIns);
            return bundle.getString("database.server.instance.successfully.edited");
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.edit.database.server.instance") + ": " +
                    rssIns.getName(), e);
        }
        return bundle.getString("failed.to.edit.database.server.instance") + ": " + rssIns.getName();
    }

    public DatabaseUser getDatabaseUserById(int userId) throws RemoteException {
        try {
            return stub.getDatabaseUserById(userId);
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.retrieve.database.user.data"), e);
        }
        return null;
    }
    
    public String deleteUser(int userId, int dbInsId) throws RemoteException {
        try {
            stub.dropUser(userId, dbInsId);
            return bundle.getString("database.user.successfully.dropped");
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.drop.database.user"), e);
        }
        return null;
    }

    public String createCarbonDataSource(int dbInsId, int userId) throws RemoteException {
        try {
            stub.createCarbonDSFromDatabaseUserEntry(dbInsId, userId);
            return bundle.getString("carbon.datasource.successfully.created");
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.create.carbon.datasource"), e);
        }
        return bundle.getString("failed.to.create.carbon.datasource");
    }

    public RSSInstanceEntry getRoundRobinAssignedInstance() throws RemoteException {
        try {
            return stub.getRoundRobinAssignedRSSInstance();
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.retrieve.round.robin.assigned.instance"), e);
        }
        return null;
    }

    public String createUser(DatabaseUser user, int privGroupId,
                             int dbInsId) throws RemoteException {
        try {
            stub.createUser(user, privGroupId, dbInsId);
            return bundle.getString("database.user.successfully.created");
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.create.database.user"), e);
        }
        return bundle.getString("failed.to.create.database.user");
    }

    public PrivilegeGroup getPrivilegeGroupById(int privGroupId) throws RemoteException {
        try {
            return stub.getPrivilegeGroupById(privGroupId);
        } catch (Exception e) {
            handleException(bundle.getString("failed.to.retrieve.database.privilege.group.data"), e);
        }
        return null;
    }

    private void handleException(String msg, Exception e) throws RemoteException {
        log.error(msg, e);
        throw new RemoteException(msg, e);
    }

    public RSSInstance getRSSInstanceById(int rssInsId) throws RemoteException {
        try {
            return stub.getRSSInstanceDataById(rssInsId);
        } catch (Exception e) {
           handleException(bundle.getString("failed.to.edit.database.server.instance"), e);
        }
        return null;
    }
    
}
