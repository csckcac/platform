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
package org.wso2.carbon.automation.api.clients.rssmanager;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.Base64;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;

import org.wso2.carbon.rssmanager.ui.stub.RSSAdminRSSManagerExceptionException;
import org.wso2.carbon.rssmanager.ui.stub.RSSAdminStub;
import org.wso2.carbon.rssmanager.ui.stub.types.*;

import java.rmi.RemoteException;

public class RSSManagerAdminServiceClient {
    private static final Log log = LogFactory.getLog(RSSManagerAdminServiceClient.class);

    private final String serviceName = "RSSManagerAdminService";
    private RSSAdminStub rssAdminStub;

    private static final String ADMIN_CONSOLE_EXTENSION_NS = "http://www.wso2.org/products/wso2commons/adminconsole";
    private static final OMNamespace ADMIN_CONSOLE_OM_NAMESPACE = OMAbstractFactory.getOMFactory().createOMNamespace(ADMIN_CONSOLE_EXTENSION_NS, "instance");
    private static final OMFactory omFactory = OMAbstractFactory.getOMFactory();
    private static final String NULL_NAMESPACE = "";
    private static final OMNamespace NULL_OMNS = omFactory.createOMNamespace(NULL_NAMESPACE, "");

    public RSSManagerAdminServiceClient(String backEndUrl, String sessionCookie) throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        rssAdminStub = new RSSAdminStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, rssAdminStub);
    }

    public RSSManagerAdminServiceClient(String backEndUrl, String userName, String password)
            throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        rssAdminStub = new RSSAdminStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, rssAdminStub);
    }

    public void createDatabase(Database database)
            throws RemoteException {

        if (log.isDebugEnabled()) {
            log.debug("Database Name :" + database.getName());
            log.debug("RSSInstance Name :" + database.getRssInstanceName());
        }
        try {
            rssAdminStub.createDatabase(database);
            log.info("Database Created");
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while creating database '" + database.getName() + "'";
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }
    }

    public void dropDatabase(String rssInstanceName, String databaseName) throws  RemoteException {
        if (log.isDebugEnabled()) {
            log.debug("DatabaseName :" + databaseName);
        }
        try {
            rssAdminStub.dropDatabase(rssInstanceName, databaseName);
            log.info("Database Dropped");
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while dropping the database '" + databaseName + "'";
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }
    }

    public DatabaseMetaData[] getDatabaseInstanceList()
            throws  RemoteException {
        DatabaseMetaData[] databaseList = new DatabaseMetaData[0];
        try {
            databaseList = rssAdminStub.getDatabases();
        } catch (RSSAdminRSSManagerExceptionException e) {
            log.error("Error occurred while retrieving database list", e);
        }
        return databaseList;
    }

    public DatabaseMetaData getDatabaseInstance(String databaseName)
            throws  RemoteException {
        DatabaseMetaData[] databaseList = getDatabaseInstanceList();
        DatabaseMetaData dbInstance = null;
        if (databaseList == null) {
            return null;
        }
        for (DatabaseMetaData dbEntry : databaseList) {
            if (dbEntry.getName().equals(databaseName)) {
                dbInstance = dbEntry;
                break;
            }
        }
        return dbInstance;

    }

    public void createPrivilegeGroup(String privilegeGroupName)
            throws  RemoteException {
        DatabasePrivilegeTemplate privilegeGroup = new DatabasePrivilegeTemplate();

        privilegeGroup.setName(privilegeGroupName);
        privilegeGroup.setPrivileges(getAllDatabasePermission());

        if (log.isDebugEnabled()) {
            log.debug("Privilege Group Name: " + privilegeGroupName);
        }

        try {
            rssAdminStub.createDatabasePrivilegesTemplate(privilegeGroup);
            log.info("Privilege Group Added");
        } catch (RSSAdminRSSManagerExceptionException e) {
            throw new RemoteException("");
        }
    }

    public DatabasePrivilegeTemplate getPrivilegeGroup(String privilegeGroupName)
            throws  RemoteException {
        DatabasePrivilegeTemplate[] privilegeGroups = getUserPrivilegeGroups();
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

    public void dropPrivilegeGroup(String templateName)
            throws  RemoteException {
        if (log.isDebugEnabled()) {
            log.debug("privilege group id :" + templateName);
        }
        try {
            rssAdminStub.dropDatabasePrivilegesTemplate(templateName);
            log.info("privilege group removed");
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred dropping the database privilege template '" +
                    templateName + "'";
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }
    }

    public DatabasePrivilegeTemplate[] getUserPrivilegeGroups()
            throws  RemoteException {
        DatabasePrivilegeTemplate[] template;
        try {
            template = rssAdminStub.getDatabasePrivilegesTemplates();
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while retrieving database privilege template list";
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }
        return template;
    }


    public DatabaseUserMetaData getDatabaseUser(String rssInstanceName, String username)
            throws  RemoteException {
        DatabaseUserMetaData user;
        try {
            user = rssAdminStub.getDatabaseUser(rssInstanceName, username);
            log.info("Database user data received");
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while retrieving information related to the database " +
                    "user '" + username + "'";
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }
        return user;
    }

    public DatabaseMetaData getDatabase(String rssInstanceName, String databaseName)
            throws  RemoteException {
        DatabaseMetaData database;
        try {
            database = rssAdminStub.getDatabase(rssInstanceName, databaseName);
            log.info("Database configuration received");
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while retrieving the configuration of the database '" +
                    databaseName + "'";
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }
        return database;
    }

    public RSSInstanceMetaData[] getRSSInstanceList()
            throws  RemoteException {
        RSSInstanceMetaData[] rssInstance = new RSSInstanceMetaData[0];
        try {
            rssInstance = rssAdminStub.getRSSInstances();
            log.info("RSS instance list retrieved");
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while retrieving the RSS instance list";
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }

        return rssInstance;
    }

    public RSSInstanceMetaData getRSSInstance(String rssInstanceName)
            throws  RemoteException {
        RSSInstanceMetaData rssInstance;
        try {
            rssInstance = rssAdminStub.getRSSInstance(rssInstanceName);
            log.info("RSS instance configuration retrieved");
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while retrieving the configuration of RSS instance '" +
                    rssInstanceName + "'";
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }
        return rssInstance;
    }

    public void createDatabaseUser(String userName, String password, String rssInstanceName)
            throws  RemoteException {
        DatabaseUser user = new DatabaseUser();
        user.setUsername(userName);
        user.setPassword(password);
        if (log.isDebugEnabled()) {
            log.debug("userName : " + userName);
            log.debug("rssInstanceName : " + rssInstanceName);
        }
        try {
            rssAdminStub.createDatabaseUser(user);
            log.info("Database user " + userName + " created");
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while creating database user '" + userName + "'";
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }
    }

    public void dropDatabaseUser(String rssInstanceName, String username) throws  RemoteException {
        if (log.isDebugEnabled()) {
            log.debug("Username : " + username);
        }
        try {
            rssAdminStub.dropDatabaseUser(rssInstanceName, username);
            log.info("User Deleted");
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while dropping the database user '" + username + "'";
            log.error(msg, e);
            throw new RemoteException(msg, e);
        }
    }

    public String[] getUsersAttachedToDatabase(
            String rssInstanceName, String databaseName) throws  RemoteException {
        String[] userList = new String[0];
        if (log.isDebugEnabled()) {
            log.debug("RSS Instance Name : " + rssInstanceName);
            log.debug("Database Name : " + databaseName);
        }

        try {
            userList = rssAdminStub.getUsersAttachedToDatabase(rssInstanceName, databaseName);
        } catch (RSSAdminRSSManagerExceptionException e) {
            String msg = "Error occurred while retrieving the database users attached to the " +
                    "database '" + databaseName + "' on RSS instance '" + rssInstanceName + "'";
        }

        return userList;
    }

//    public String createCarbonDSFromDatabaseUserEntry(int databaseInstanceId,
//                                                      int dbUserId)
//            throws  RemoteException {
//        String carbonDataSource;
//        if (log.isDebugEnabled()) {
//            log.debug("databaseInstanceId " + databaseInstanceId);
//        }
//
//        carbonDataSource = rssAdminStub.createCarbonDSFromDatabaseUserEntry(databaseInstanceId, dbUserId);
//        log.debug(carbonDataSource);
//        carbonDataSource = carbonDataSource.substring((carbonDataSource.indexOf(" '") + 2), carbonDataSource.indexOf("' "));
//        if (log.isDebugEnabled()) {
//            log.debug("Data Source Name : " + carbonDataSource);
//        }
//        log.info("Data Source Created");
//
//        return carbonDataSource;
//    }

    private static DatabasePrivilege[] getAllDatabasePermission() {

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
