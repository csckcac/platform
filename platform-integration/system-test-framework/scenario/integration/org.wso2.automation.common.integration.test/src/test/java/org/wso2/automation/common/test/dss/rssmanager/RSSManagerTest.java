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
package org.wso2.automation.common.test.dss.rssmanager;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.carbon.admin.service.RSSAdminConsoleService;
import org.wso2.carbon.rssmanager.ui.stub.RSSAdminRSSDAOExceptionException;
import org.wso2.carbon.rssmanager.ui.stub.types.DatabaseInstanceEntry;
import org.wso2.carbon.rssmanager.ui.stub.types.DatabaseUserEntry;
import org.wso2.carbon.rssmanager.ui.stub.types.PrivilegeGroup;
import org.wso2.carbon.rssmanager.ui.stub.types.RSSInstanceEntry;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;

import java.rmi.RemoteException;

/*
Multi tenancy of RSS manager test and security of RSS Databases and users of the tenants
 */
public class RSSManagerTest {
    private static final Log log = LogFactory.getLog(RSSManagerTest.class);

    private RSSAdminConsoleService rssManager;

    private String dssBackEndUrl;
    private UserInfo userInfo;

    private final String dbName = "new_db123";
    private final String dbUserName = "user";
    private final String dbPassword = "password";
    private final String priGrpName = "privilegeGroup";

    private String databaseName;
    private DatabaseInstanceEntry dbInstance;
    private PrivilegeGroup privilegeGroup;
    private DatabaseUserEntry userEnrty;

    private String sessionCookie;
    private EnvironmentVariables dssServer;


    @BeforeClass
    public void init() throws AxisFault {
        EnvironmentBuilder builder = new EnvironmentBuilder().dss(3);
        dssServer = builder.build().getDss();

        sessionCookie = dssServer.getSessionCookie();
        dssBackEndUrl = dssServer.getBackEndUrl();
        userInfo = UserListCsvReader.getUserInfo(3);
        rssManager = new RSSAdminConsoleService(dssBackEndUrl);
    }


    @Test(priority = 1)
    public void createDatabaseForTenant()
            throws RSSAdminRSSDAOExceptionException, RemoteException, InterruptedException {
        UserInfo user = UserListCsvReader.getUserInfo(2);
        String sc;

        if (userInfo.getUserName().equals(user.getUserName())) {
            throw new RuntimeException("User Names are identical. please change the user list user names");
        }

        AdminServiceAuthentication authentication = new AdminServiceAuthentication(dssBackEndUrl);
        sc = authentication.login(user.getUserName(), user.getPassword(), "10.100.3.103");


        dropDBIfExist(sc, dbName + "_" + user.getDomain().replace(".", "_"),
                      rssManager.getFullyQualifiedUsername(dbUserName, user.getDomain()), priGrpName);
        Thread.sleep(5000);
        RSSInstanceEntry rssInstance = rssManager.getRoundRobinAssignedRSSInstance(sc);
        rssManager.createDatabase(sc, dbName, rssInstance.getRssInstanceId());
        databaseName = dbName + "_" + user.getDomain().replace(".", "_");
        dbInstance = rssManager.getDatabaseInstance(sc, databaseName);
        Assert.assertNotNull(dbInstance, "Database Instance Not Found");
        rssManager.createPrivilegeGroup(sc, priGrpName);
        privilegeGroup = rssManager.getPrivilegeGroup(sc, priGrpName);
        Assert.assertNotNull(privilegeGroup, "privilege Group Not Found");
        rssManager.createUser(sc, dbUserName, dbPassword, dbInstance.getDbInstanceId(), privilegeGroup.getPrivGroupId());
        userEnrty = rssManager.getDatabaseUser(sc, rssManager.getFullyQualifiedUsername(dbUserName, user.getDomain()), dbInstance.getDbInstanceId());
        Assert.assertNotNull(userEnrty, "User Not Found on Database");
        authentication.logOut();

    }

    @Test(dependsOnMethods = {"createDatabaseForTenant"})
    public void getOtherTenantDatabase() throws RSSAdminRSSDAOExceptionException, RemoteException {
        DatabaseInstanceEntry dbInst = rssManager.getDatabaseInstance(sessionCookie, databaseName);
        Assert.assertNull(dbInst, "Tenant can list down other tenet databases");
    }

    @Test(dependsOnMethods = {"createDatabaseForTenant"})
    public void dropOtherTenantUser() throws RemoteException {

        try {
            rssManager.deleteUser(sessionCookie, userEnrty.getUserId(), dbInstance.getDbInstanceId());
        } catch (RSSAdminRSSDAOExceptionException e) {
            return;
        }
        Assert.fail("No Security for DB Users.Any tenant can drop other tenant's database users");

    }

    @Test(dependsOnMethods = {"createDatabaseForTenant"})
    public void dropOtherTenantPrivilegeGroup() throws RemoteException {
        try {
            rssManager.deletePrivilegeGroup(sessionCookie, privilegeGroup.getPrivGroupId());
        } catch (RSSAdminRSSDAOExceptionException e) {
            return;
        }
        Assert.fail("No Security for Privilege Group. Any tenant can delete other tenant's privilege Group");
    }

    @Test(dependsOnMethods = {"createDatabaseForTenant"})
    public void dropOtherTenantDatabase() throws RemoteException {
        try {
            rssManager.dropDatabase(sessionCookie, dbInstance.getDbInstanceId());
        } catch (RSSAdminRSSDAOExceptionException e) {

            return;
        }
        Assert.fail("No Security for database. Any tenant can drop other tenant databases");
    }


    @Test(dependsOnMethods = {"createDatabaseForTenant"})
    public void crateDatabaseUserOnOtherTenantDatabase()
            throws RemoteException, RSSAdminRSSDAOExceptionException {

        rssManager.createPrivilegeGroup(sessionCookie, "allPrivileges");
        PrivilegeGroup pg = rssManager.getPrivilegeGroup(sessionCookie, priGrpName);
        try {

            rssManager.createUser(sessionCookie, dbUserName, dbPassword, dbInstance.getDbInstanceId(), pg.getPrivGroupId());
        } catch (RSSAdminRSSDAOExceptionException e) {
            return;
        }
        Assert.fail("Tenant can create users on other tenant database.");
    }

    @Test(dependsOnMethods = {"createDatabaseForTenant"})
    public void multiTenancy()
            throws RSSAdminRSSDAOExceptionException, RemoteException, InterruptedException {
        dropDBIfExist(sessionCookie, dbName + "_" + userInfo.getDomain().replace(".", "_"),
                      rssManager.getFullyQualifiedUsername(dbUserName, userInfo.getDomain()), priGrpName);
        Thread.sleep(5000);

        RSSInstanceEntry rssInstance = rssManager.getRoundRobinAssignedRSSInstance(sessionCookie);
        rssManager.createDatabase(sessionCookie, dbName, rssInstance.getRssInstanceId());
        String databaseName = dbName + "_" + userInfo.getDomain().replace(".", "_");
        DatabaseInstanceEntry dbInstance = rssManager.getDatabaseInstance(sessionCookie, databaseName);
        Assert.assertNotNull(dbInstance, "can not have same database name for different tenants. No multi tenancy");
        rssManager.createPrivilegeGroup(sessionCookie, priGrpName);
        PrivilegeGroup privilegeGroup = rssManager.getPrivilegeGroup(sessionCookie, priGrpName);
        Assert.assertNotNull(privilegeGroup, "can not have same privilege group for different tenants. No multi tenancy");
        rssManager.createUser(sessionCookie, dbUserName, dbPassword, dbInstance.getDbInstanceId(), privilegeGroup.getPrivGroupId());
        DatabaseUserEntry userEnrty = rssManager.getDatabaseUser(sessionCookie, rssManager.getFullyQualifiedUsername(dbUserName, userInfo.getDomain()), dbInstance.getDbInstanceId());
        Assert.assertNotNull(userEnrty, "can not have same user name for different tenants. No multi tenancy");
    }

    @AfterClass
    public void destroy() {
        dssBackEndUrl = null;
        dssServer = null;
        sessionCookie = null;
        userInfo = null;
        rssManager = null;
    }

    private void dropDBIfExist(String sessionCookie, String databaseName, String databaseUser,
                               String userPrivilegeGroup)
            throws RSSAdminRSSDAOExceptionException, RemoteException {
        DatabaseInstanceEntry dbInstance;
        DatabaseUserEntry userEntry;
        PrivilegeGroup privGroup;


        log.info("Setting pre conditions");

        dbInstance = rssManager.getDatabaseInstance(sessionCookie, databaseName);
        if (dbInstance != null) {
            log.info("Database name already in server");
            userEntry = rssManager.getDatabaseUser(sessionCookie, databaseUser, dbInstance.getDbInstanceId());
            if (userEntry != null) {

                log.info("User already in Database. deleting user");
                rssManager.deleteUser(sessionCookie, userEntry.getUserId(), dbInstance.getDbInstanceId());
                log.info("User Deleted");
            }
            log.info("Dropping database");
            rssManager.dropDatabase(sessionCookie, dbInstance.getDbInstanceId());
            log.info("database Dropped");
        }

        privGroup = rssManager.getPrivilegeGroup(sessionCookie, userPrivilegeGroup);
        if (privGroup != null) {
            log.info("Privilege Group name already in server");
            rssManager.deletePrivilegeGroup(sessionCookie, privGroup.getPrivGroupId());
            log.info("Privilege Group Deleted");
        }

    }
}
