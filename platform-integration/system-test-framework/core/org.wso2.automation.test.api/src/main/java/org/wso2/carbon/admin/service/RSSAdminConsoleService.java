/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.admin.service;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.Base64;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.service.utils.AuthenticateStub;
import org.wso2.carbon.rssmanager.ui.stub.RSSAdminRSSDAOExceptionException;
import org.wso2.carbon.rssmanager.ui.stub.RSSAdminStub;
import org.wso2.carbon.rssmanager.ui.stub.types.*;


import java.rmi.RemoteException;

public class RSSAdminConsoleService {
    private static final Log log = LogFactory.getLog(RSSAdminConsoleService.class);

    private final String serviceName = "RSSManagerAdminService";
    private RSSAdminStub consoleAdminStub;
    private String endPoint;

    private static final String ADMIN_CONSOLE_EXTENSION_NS = "http://www.wso2.org/products/wso2commons/adminconsole";
    private static final OMNamespace ADMIN_CONSOLE_OM_NAMESPACE = OMAbstractFactory.getOMFactory().createOMNamespace(ADMIN_CONSOLE_EXTENSION_NS, "instance");
    private static final OMFactory omFactory = OMAbstractFactory.getOMFactory();
    private static final String NULL_NAMESPACE = "";
    private static final OMNamespace NULL_OMNS = omFactory.createOMNamespace(NULL_NAMESPACE, "");

    public RSSAdminConsoleService(String backEndUrl) throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        consoleAdminStub = new RSSAdminStub(endPoint);
    }

    public void createDatabase(String sessionCookie, String databaseName, int rssInstanceId)
            throws RSSAdminRSSDAOExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        if (log.isDebugEnabled()) {
            log.debug("Database Name :" + databaseName);
            log.debug("RSSInstanceId :" + rssInstanceId);
        }
        consoleAdminStub.createDatabase(serializeDatabaseInstanceData(databaseName, String.valueOf(rssInstanceId), "0").toString());
        log.info("Database Created");


    }

    public void dropDatabase(String sessionCookie, int databaseId)
            throws RSSAdminRSSDAOExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        if (log.isDebugEnabled()) {
            log.debug("Database InstanceId :" + databaseId);
        }
        consoleAdminStub.dropDatabase(databaseId);
        log.info("Database Dropped");


    }

    public DatabaseInstanceEntry[] getDatabaseInstanceList(String sessionCookie)
            throws RSSAdminRSSDAOExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        DatabaseInstanceEntry[] databaseList;
        databaseList = consoleAdminStub.getDatabaseInstanceList();
        if (log.isDebugEnabled()) {
            log.debug("Database Instance list: " + databaseList);
        }
        log.info("Database instance list received");

        return databaseList;
    }

    public DatabaseInstanceEntry getDatabaseInstance(String sessionCookie, String databaseName)
            throws RSSAdminRSSDAOExceptionException, RemoteException {
        DatabaseInstanceEntry[] databaseList = getDatabaseInstanceList(sessionCookie);
        DatabaseInstanceEntry dbInstance = null;
        if (databaseList == null) {
            return null;
        }
        for (DatabaseInstanceEntry dbEntry : databaseList) {
            if (dbEntry.getDbName().equals(databaseName)) {
                dbInstance = dbEntry;
                break;
            }
        }
        return dbInstance;

    }

    public void createPrivilegeGroup(String sessionCookie, String privilegeGroupName)
            throws RSSAdminRSSDAOExceptionException, RemoteException {
        PrivilegeGroup privilegeGroup = new PrivilegeGroup();

        privilegeGroup.setPrivGroupName(privilegeGroupName);
        privilegeGroup.setPrivs(getAllDatabasePermission());

        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        if (log.isDebugEnabled()) {
            log.debug("Privilege Group Name: " + privilegeGroupName);
        }

        consoleAdminStub.createPrivilegeGroup(privilegeGroup);
        log.info("Privilege Group Added");


    }

    public PrivilegeGroup getPrivilegeGroup(String sessionCookie, String privilegeGroupName)
            throws RSSAdminRSSDAOExceptionException, RemoteException {
        PrivilegeGroup[] privilegeGroups = getUserPrivilegeGroups(sessionCookie);
        PrivilegeGroup userPrivilegeGroup = null;
        if (privilegeGroups == null) {
            return null;
        }
        if (log.isDebugEnabled()) {
            log.debug("privilege group name :" + privilegeGroupName);
        }
        for (PrivilegeGroup priGroup : privilegeGroups) {
            if (priGroup.getPrivGroupName().equals(privilegeGroupName)) {
                userPrivilegeGroup = priGroup;
                log.info("Privilege group found");
                break;
            }
        }

        return userPrivilegeGroup;

    }

    public void deletePrivilegeGroup(String sessionCookie, int privilegeGroupId)
            throws RSSAdminRSSDAOExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        if (log.isDebugEnabled()) {
            log.debug("privilege group id :" + privilegeGroupId);
        }

        consoleAdminStub.removePrivilegeGroup(privilegeGroupId);
        log.info("privilege group removed");

    }

    public PrivilegeGroup[] getUserPrivilegeGroups(String sessionCookie)
            throws RSSAdminRSSDAOExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        PrivilegeGroup[] privilegeGroup;
        privilegeGroup = consoleAdminStub.getPrivilegeGroups();

        return privilegeGroup;

    }


    public DatabaseUserEntry getDatabaseUser(String sessionCookie, String userName,
                                             int databaseInstanceId)
            throws RSSAdminRSSDAOExceptionException, RemoteException {
        DatabaseUserEntry[] databaseUsers = getUsersByDatabaseInstanceId(sessionCookie, databaseInstanceId);
        DatabaseUserEntry dbUser = null;
        if (databaseUsers == null) {
            return null;
        }
        if (log.isDebugEnabled()) {
            log.debug("User name " + userName);
        }
        for (DatabaseUserEntry user : databaseUsers) {
            if (userName.equals(user.getUsername())) {
                dbUser = user;
                log.info("User Found on database");
                break;
            }
        }
        return dbUser;
    }

    public DatabaseInstanceEntry getDatabaseInstanceById(String sessionCookie, int rssInstanceId,
                                                         int databaseInstanceId)
            throws RSSAdminRSSDAOExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        DatabaseInstanceEntry databaseInstanceEntry;
        databaseInstanceEntry = consoleAdminStub.getDatabaseInstanceById(databaseInstanceId);
        return databaseInstanceEntry;
    }

    public RSSInstanceEntry[] getRSSInstanceList(String sessionCookie)
            throws RSSAdminRSSDAOExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        RSSInstanceEntry[] rssInstance;

        rssInstance = consoleAdminStub.getRSSInstanceList();
        log.info("RSS Instance found");

        return rssInstance;
    }

    public RSSInstance getRSSInstanceById(String sessionCookie, int rssInstanceId)
            throws RSSAdminRSSDAOExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        RSSInstance rssInstance;

        rssInstance = consoleAdminStub.getRSSInstanceDataById(rssInstanceId);
        log.info("RSS Instance found");

        return rssInstance;
    }

    public RSSInstanceEntry getRoundRobinAssignedRSSInstance(String sessionCookie)
            throws RSSAdminRSSDAOExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        RSSInstanceEntry rssInstance;

        rssInstance = consoleAdminStub.getRoundRobinAssignedRSSInstance();
        log.info("RSS Instance found");

        return rssInstance;
    }

    public void createUser(String sessionCookie, String userName, String password,
                           int databaseInstanceId, int privilegeGroupId)
            throws RSSAdminRSSDAOExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        DatabaseUser user = new DatabaseUser();
        user.setUsername(userName);
        user.setPassword(password);
        if (log.isDebugEnabled()) {
            log.debug("userName " + userName);
            log.debug("databaseInstanceId " + databaseInstanceId);
            log.debug("privilegeGroupId " + privilegeGroupId);
        }
        consoleAdminStub.createUser(user, privilegeGroupId, databaseInstanceId);
        log.info("User Created");
    }

    public void deleteUser(String sessionCookie, int userId, int databaseInstanceId)
            throws RSSAdminRSSDAOExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        if (log.isDebugEnabled()) {
            log.debug("UserId " + userId);
        }
        consoleAdminStub.dropUser(userId, databaseInstanceId);
        log.info("User Deleted");


    }

    public DatabaseUserEntry[] getUsersByDatabaseInstanceId(String sessionCookie,
                                                            int databaseInstanceId)
            throws RSSAdminRSSDAOExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        DatabaseUserEntry[] userList;
        if (log.isDebugEnabled()) {
            log.debug("databaseInstanceId " + databaseInstanceId);
        }

        userList = consoleAdminStub.getUsersByDatabaseInstanceId(databaseInstanceId);
        log.info("User List received");

        return userList;
    }

    public String createCarbonDSFromDatabaseUserEntry(String sessionCookie, int databaseInstanceId,
                                                      int dbUserId)
            throws RSSAdminRSSDAOExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        String carbonDataSource;
        if (log.isDebugEnabled()) {
            log.debug("databaseInstanceId " + databaseInstanceId);
        }

        carbonDataSource = consoleAdminStub.createCarbonDSFromDatabaseUserEntry(databaseInstanceId, dbUserId);
        log.debug(carbonDataSource);
        carbonDataSource = carbonDataSource.substring((carbonDataSource.indexOf(" '") + 2), carbonDataSource.indexOf("' "));
        if (log.isDebugEnabled()) {
            log.debug("Data Source Name : " + carbonDataSource);
        }
        log.info("Data Source Created");

        return carbonDataSource;
    }

    private static OMElement serializeDatabaseInstanceData(String dbName, String rssInstId,
                                                           String dbInstId) {
        OMElement dbEl = omFactory.createOMElement("db", ADMIN_CONSOLE_OM_NAMESPACE);

        if (!"".equals(rssInstId) && rssInstId != null) {
            dbEl.addAttribute("rssInsId", rssInstId, NULL_OMNS);
        }

        if (!"".equals(dbName) && dbName != null) {
            dbEl.addAttribute("name", dbName, NULL_OMNS);
        }

        if (!"".equals(dbInstId) && dbInstId != null) {
            dbEl.addAttribute("dbInsId", dbInstId, NULL_OMNS);
        }
        if (log.isDebugEnabled()) {
            log.debug(dbEl);
        }
        return dbEl;
    }

    private DatabasePrivilege[] getAllDatabasePermission() {

        DatabasePrivilege[] databasePrivilegeList = new DatabasePrivilege[19];

        databasePrivilegeList[0] = new DatabasePrivilege();
        databasePrivilegeList[0].setPrivName("Alter_priv");
        databasePrivilegeList[0].setPrivValue("Y");

        databasePrivilegeList[1] = new DatabasePrivilege();
        databasePrivilegeList[1].setPrivName("Alter_routine_priv");
        databasePrivilegeList[1].setPrivValue("Y");

        databasePrivilegeList[2] = new DatabasePrivilege();
        databasePrivilegeList[2].setPrivName("Create_priv");
        databasePrivilegeList[2].setPrivValue("Y");

        databasePrivilegeList[3] = new DatabasePrivilege();
        databasePrivilegeList[3].setPrivName("Create_routine_priv");
        databasePrivilegeList[3].setPrivValue("Y");

        databasePrivilegeList[4] = new DatabasePrivilege();
        databasePrivilegeList[4].setPrivName("Create_tmp_table_priv");
        databasePrivilegeList[4].setPrivValue("Y");

        databasePrivilegeList[5] = new DatabasePrivilege();
        databasePrivilegeList[5].setPrivName("Create_view_priv");
        databasePrivilegeList[5].setPrivValue("Y");

        databasePrivilegeList[6] = new DatabasePrivilege();
        databasePrivilegeList[6].setPrivName("Delete_priv");
        databasePrivilegeList[6].setPrivValue("Y");

        databasePrivilegeList[7] = new DatabasePrivilege();
        databasePrivilegeList[7].setPrivName("Drop_priv");
        databasePrivilegeList[7].setPrivValue("Y");

        databasePrivilegeList[8] = new DatabasePrivilege();
        databasePrivilegeList[8].setPrivName("Event_priv");
        databasePrivilegeList[8].setPrivValue("Y");

        databasePrivilegeList[9] = new DatabasePrivilege();
        databasePrivilegeList[9].setPrivName("Execute_priv");
        databasePrivilegeList[9].setPrivValue("Y");

        databasePrivilegeList[10] = new DatabasePrivilege();
        databasePrivilegeList[10].setPrivName("Grant_priv");
        databasePrivilegeList[10].setPrivValue("Y");

        databasePrivilegeList[11] = new DatabasePrivilege();
        databasePrivilegeList[11].setPrivName("Index_priv");
        databasePrivilegeList[11].setPrivValue("Y");

        databasePrivilegeList[12] = new DatabasePrivilege();
        databasePrivilegeList[12].setPrivName("Insert_priv");
        databasePrivilegeList[12].setPrivValue("Y");

        databasePrivilegeList[13] = new DatabasePrivilege();
        databasePrivilegeList[13].setPrivName("Lock_tables_priv");
        databasePrivilegeList[13].setPrivValue("Y");

        databasePrivilegeList[14] = new DatabasePrivilege();
        databasePrivilegeList[14].setPrivName("References_priv");
        databasePrivilegeList[14].setPrivValue("Y");

        databasePrivilegeList[15] = new DatabasePrivilege();
        databasePrivilegeList[15].setPrivName("Select_priv");
        databasePrivilegeList[15].setPrivValue("Y");

        databasePrivilegeList[16] = new DatabasePrivilege();
        databasePrivilegeList[16].setPrivName("Show_view_priv");
        databasePrivilegeList[16].setPrivValue("Y");

        databasePrivilegeList[17] = new DatabasePrivilege();
        databasePrivilegeList[17].setPrivName("Trigger_priv");
        databasePrivilegeList[17].setPrivValue("Y");

        databasePrivilegeList[18] = new DatabasePrivilege();
        databasePrivilegeList[18].setPrivName("Update_priv");
        databasePrivilegeList[18].setPrivValue("Y");

        return databasePrivilegeList;

    }

    public String getFullyQualifiedUsername(String username, String tenantDomain) {
        if (tenantDomain != null) {

            /* The maximum number of characters allowed for the username in mysql system tables is
             * 16. Thus, to adhere the aforementioned constraint as well as to give the username
             * an unique identification based on the tenant domain, we append a hash value that is
             * created based on the tenant domain */
            byte[] bytes = intToByteArray(tenantDomain.hashCode());
            return username + "_" + Base64.encode(bytes);
        }
        return username;
    }

    private static byte[] intToByteArray(int value) {
        byte[] b = new byte[6];
        for (int i = 0; i < 6; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }
}
