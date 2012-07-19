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
import org.wso2.carbon.rssmanager.ui.stub.RSSAdminRSSManagerExceptionException;
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

    public void createDatabase(String sessionCookie, String databaseName, String rssInstanceName)
            throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        if (log.isDebugEnabled()) {
            log.debug("Database Name :" + databaseName);
            log.debug("RSSInstanceId :" + rssInstanceName);
        }
        Database database = new Database();
        database.setName(databaseName);
        database.setRssInstanceName(rssInstanceName);

        try {
            consoleAdminStub.createDatabase(database);
            log.info("Database Created");
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while creating database '" + databaseName + "'";
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }
    }

    public void dropDatabase(String sessionCookie, String rssInstanceName, String databaseName)
            throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        if (log.isDebugEnabled()) {
            log.debug("RSS Instance Name :" + rssInstanceName);
            log.debug("Database Instance Name :" + databaseName);
        }
        try {
            consoleAdminStub.dropDatabase(rssInstanceName, databaseName);
            log.info("Database Dropped");
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while dropping the database '" + databaseName + "'";
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }
    }

    public DatabaseMetaData[] getDatabaseInstanceList(String sessionCookie)
            throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        DatabaseMetaData[] databaseList;
        try {
            databaseList = consoleAdminStub.getDatabases();
            log.info("Database instance list received");
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while retrieving the database list";
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }
        return databaseList;
    }

    public DatabaseMetaData getDatabaseInstance(String sessionCookie, String rssInstanceName,
                                                String databaseName)
            throws RemoteException {
        DatabaseMetaData[] databaseList = getDatabaseInstanceList(sessionCookie);
        DatabaseMetaData dbInstance = null;
        if (databaseList == null) {
            return null;
        }
        for (DatabaseMetaData dbEntry : databaseList) {
            if (dbEntry.getName().equals(databaseName) &
                    dbEntry.getRssInstanceName().equals(rssInstanceName)) {
                dbInstance = dbEntry;
                break;
            }
        }
        return dbInstance;

    }

    public void createPrivilegeGroup(String sessionCookie, String privilegeGroupName)
            throws RemoteException {
        DatabasePrivilegeTemplate template = new DatabasePrivilegeTemplate();

        template.setName(privilegeGroupName);
        template.setPrivileges(getAllDatabasePermission());

        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        if (log.isDebugEnabled()) {
            log.debug("Privilege Group Name: " + privilegeGroupName);
        }

        try {
            consoleAdminStub.createDatabasePrivilegesTemplate(template);
            log.info("Privilege Group Added");
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while creating the database privilege template '" +
                    template.getName();
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }
    }

    public DatabasePrivilegeTemplate getPrivilegeGroup(String sessionCookie, String privilegeGroupName)
            throws RemoteException {
        DatabasePrivilegeTemplate[] privilegeGroups = getUserPrivilegeGroups(sessionCookie);
        DatabasePrivilegeTemplate userPrivilegeGroup = null;
        if (privilegeGroups == null) {
            return null;
        }
        if (log.isDebugEnabled()) {
            log.debug("privilege group name :" + privilegeGroupName);
        }
        for (DatabasePrivilegeTemplate priGroup : privilegeGroups) {
            if (priGroup.getName().equals(privilegeGroupName)) {
                userPrivilegeGroup = priGroup;
                log.info("Privilege group found");
                break;
            }
        }

        return userPrivilegeGroup;

    }

    public void deletePrivilegeGroup(String sessionCookie, String templateName)
            throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        if (log.isDebugEnabled()) {
            log.debug("privilege group name :" + templateName);
        }
        try {
            consoleAdminStub.dropDatabasePrivilegesTemplate(templateName);
            log.info("privilege group removed");
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while dropping database privilege template '" +
                    templateName + "'";
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }
    }

    public DatabasePrivilegeTemplate[] getUserPrivilegeGroups(String sessionCookie)
            throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        DatabasePrivilegeTemplate[] DatabasePrivilegeTemplate;
        try {
            DatabasePrivilegeTemplate = consoleAdminStub.getDatabasePrivilegesTemplates();
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while retrieving the list of database privilege templates";
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }
        return DatabasePrivilegeTemplate;
    }


    public String getDatabaseUser(String sessionCookie, String rssInstanceName,
                                                String username)
            throws RemoteException {
        String[] databaseUsers =
                getUsersByDatabaseInstanceId(sessionCookie, rssInstanceName, username);
        DatabaseUserMetaData dbUser = null;
        if (databaseUsers == null) {
            return null;
        }
        if (log.isDebugEnabled()) {
            log.debug("Username " + username);
        }
        for (String user : databaseUsers) {
            if (username.equals(user)) {
                log.info("User Found on database");
                return user;
            }
        }
        return null;
    }

    public DatabaseMetaData getDatabase(String sessionCookie, String rssInstanceName,
                                        String databaseName)
            throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        DatabaseMetaData DatabaseMetaData;
        try {
            DatabaseMetaData = consoleAdminStub.getDatabase(rssInstanceName, databaseName);
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while retrieving the configuration of the database '" +
                    databaseName + "'";
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }
        return DatabaseMetaData;
    }

    public RSSInstanceMetaData[] getRSSInstanceList(String sessionCookie)
            throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        RSSInstanceMetaData[] rssInstance;

        try {
            rssInstance = consoleAdminStub.getRSSInstances();
            log.info("RSS Instance retrieved");
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while retrieving the RSS instance list";
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }

        return rssInstance;
    }

    public RSSInstanceMetaData getRSSInstanceById(String sessionCookie, String rssInstanceName)
            throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        RSSInstanceMetaData rssInstance;

        try {
            rssInstance = consoleAdminStub.getRSSInstance(rssInstanceName);
            log.info("RSS Instance found");
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while retrieving the configuration of the RSS instance '" +
                    rssInstanceName + "'";
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }
        return rssInstance;
    }

    public void createUser(String sessionCookie, String userName, String password,
                           String rssInstanceName)
            throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        DatabaseUser user = new DatabaseUser();
        user.setUsername(userName);
        user.setPassword(password);
        user.setRssInstanceName(rssInstanceName);
        if (log.isDebugEnabled()) {
            log.debug("Username " + userName);
            log.debug("RSS Instance Name " + rssInstanceName);
        }
        try {
            consoleAdminStub.createDatabaseUser(user);
            log.info("User Created");
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while creating the database user '" + userName + "'";
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }
    }

    public void deleteUser(String sessionCookie, String rssInstanceName, String username)
            throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        if (log.isDebugEnabled()) {
            log.debug("RSS Instance Name " + rssInstanceName);
            log.debug("Username " + username);
        }
        try {
            consoleAdminStub.dropDatabaseUser(rssInstanceName, username);
            log.info("User Deleted");
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while dropping the database user '" + username + "'";
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }


    }

    public String[] getUsersByDatabaseInstanceId(
            String sessionCookie, String rssInstanceName, String databaseName)
            throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
        String[] userList;
        if (log.isDebugEnabled()) {
            log.debug("RSS Instance Name " + rssInstanceName);
            log.debug("Database Name " + databaseName);
        }

        try {
            userList = consoleAdminStub.getUsersAttachedToDatabase(rssInstanceName, databaseName);
            log.info("User List received");
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while retrieving the database user list attached to " +
                    "the database '" + databaseName + "'";
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }

        return userList;
    }

//    public String createCarbonDSFromDatabaseUserEntry(String sessionCookie, int databaseInstanceId,
//                                                      int dbUserId)
//            throws RemoteException {
//        AuthenticateStub.authenticateStub(sessionCookie, consoleAdminStub);
//        String carbonDataSource;
//        if (log.isDebugEnabled()) {
//            log.debug("databaseInstanceId " + databaseInstanceId);
//        }
//
//        carbonDataSource = consoleAdminStub.createCarbonDSFromDatabaseUserEntry(databaseInstanceId, dbUserId);
//        log.debug(carbonDataSource);
//        carbonDataSource = carbonDataSource.substring((carbonDataSource.indexOf(" '") + 2), carbonDataSource.indexOf("' "));
//        if (log.isDebugEnabled()) {
//            log.debug("Data Source Name : " + carbonDataSource);
//        }
//        log.info("Data Source Created");
//
//        return carbonDataSource;
//    }

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
        databasePrivilegeList[0].setName("Alter_priv");
        databasePrivilegeList[0].setValue("Y");

        databasePrivilegeList[1] = new DatabasePrivilege();
        databasePrivilegeList[1].setName("Alter_routine_priv");
        databasePrivilegeList[1].setValue("Y");

        databasePrivilegeList[2] = new DatabasePrivilege();
        databasePrivilegeList[2].setName("Create_priv");
        databasePrivilegeList[2].setValue("Y");

        databasePrivilegeList[3] = new DatabasePrivilege();
        databasePrivilegeList[3].setName("Create_routine_priv");
        databasePrivilegeList[3].setValue("Y");

        databasePrivilegeList[4] = new DatabasePrivilege();
        databasePrivilegeList[4].setName("Create_tmp_table_priv");
        databasePrivilegeList[4].setValue("Y");

        databasePrivilegeList[5] = new DatabasePrivilege();
        databasePrivilegeList[5].setName("Create_view_priv");
        databasePrivilegeList[5].setValue("Y");

        databasePrivilegeList[6] = new DatabasePrivilege();
        databasePrivilegeList[6].setName("Delete_priv");
        databasePrivilegeList[6].setValue("Y");

        databasePrivilegeList[7] = new DatabasePrivilege();
        databasePrivilegeList[7].setName("Drop_priv");
        databasePrivilegeList[7].setValue("Y");

        databasePrivilegeList[8] = new DatabasePrivilege();
        databasePrivilegeList[8].setName("Event_priv");
        databasePrivilegeList[8].setValue("Y");

        databasePrivilegeList[9] = new DatabasePrivilege();
        databasePrivilegeList[9].setName("Execute_priv");
        databasePrivilegeList[9].setValue("Y");

        databasePrivilegeList[10] = new DatabasePrivilege();
        databasePrivilegeList[10].setName("Grant_priv");
        databasePrivilegeList[10].setValue("Y");

        databasePrivilegeList[11] = new DatabasePrivilege();
        databasePrivilegeList[11].setName("Index_priv");
        databasePrivilegeList[11].setValue("Y");

        databasePrivilegeList[12] = new DatabasePrivilege();
        databasePrivilegeList[12].setName("Insert_priv");
        databasePrivilegeList[12].setValue("Y");

        databasePrivilegeList[13] = new DatabasePrivilege();
        databasePrivilegeList[13].setName("Lock_tables_priv");
        databasePrivilegeList[13].setValue("Y");

        databasePrivilegeList[14] = new DatabasePrivilege();
        databasePrivilegeList[14].setName("References_priv");
        databasePrivilegeList[14].setValue("Y");

        databasePrivilegeList[15] = new DatabasePrivilege();
        databasePrivilegeList[15].setName("Select_priv");
        databasePrivilegeList[15].setValue("Y");

        databasePrivilegeList[16] = new DatabasePrivilege();
        databasePrivilegeList[16].setName("Show_view_priv");
        databasePrivilegeList[16].setValue("Y");

        databasePrivilegeList[17] = new DatabasePrivilege();
        databasePrivilegeList[17].setName("Trigger_priv");
        databasePrivilegeList[17].setValue("Y");

        databasePrivilegeList[18] = new DatabasePrivilege();
        databasePrivilegeList[18].setName("Update_priv");
        databasePrivilegeList[18].setValue("Y");

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
