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
package org.wso2.platform.test.core.utils.dssutils;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.wso2.carbon.admin.service.RSSAdminConsoleService;
import org.wso2.carbon.rssmanager.ui.stub.RSSAdminRSSDAOExceptionException;
import org.wso2.carbon.rssmanager.ui.stub.types.DatabaseInstanceEntry;
import org.wso2.carbon.rssmanager.ui.stub.types.DatabaseUserEntry;
import org.wso2.carbon.rssmanager.ui.stub.types.PrivilegeGroup;
import org.wso2.carbon.rssmanager.ui.stub.types.RSSInstanceEntry;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.dbutils.DatabaseFactory;
import org.wso2.platform.test.core.utils.dbutils.DatabaseManager;
import org.wso2.platform.test.core.utils.fileutils.FileManager;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class SqlDataSourceUtil {
    private static final Log log = LogFactory.getLog(SqlDataSourceUtil.class);
    private String dssBackEndUrl;
    private String sessionCookie;
    private FrameworkProperties frameworkProperties;
    private UserInfo userInfo;
    private RSSAdminConsoleService rSSAdminConsoleService;

    private int rssInstanceId = -1;
    private int dbInstanceId = -1;
    private int userPrivilegeGroupId = -1;
    private String jdbcUrl = null;
    private String jdbcDriver = null;
    private int databaseUserId = -1;
    private String databaseName;
    private String databaseUser;
    private String databasePassword;
    private final String userPrivilegeGroup = "automation";


    public SqlDataSourceUtil(String sessionCookie, String backEndUrl,
                             FrameworkProperties frameworkProperties, int userId) {
        this.sessionCookie = sessionCookie;
        this.dssBackEndUrl = backEndUrl;
        this.frameworkProperties = frameworkProperties;
        this.userInfo = UserListCsvReader.getUserInfo(userId);

    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public String getDatabaseUser() {
        return this.databaseUser;
    }

    public String getDatabasePassword() {
        return this.databasePassword;
    }

    public int getDatabaseUserId() {
        return this.databaseUserId;
    }

    public int getDatabaseInstanceId() {
        return this.dbInstanceId;
    }

    public String getJdbcUrl() {
        return this.jdbcUrl;
    }

    public DataHandler createArtifact(String dbsFilePath) throws XMLStreamException, IOException {
        Assert.assertNotNull(jdbcUrl, "Initialize jdbcUrl");
        try {
            OMElement dbsFile = AXIOMUtil.stringToOM(FileManager.readFile(dbsFilePath));
            OMElement dbsConfig = dbsFile.getFirstChildWithName(new QName("config"));
            Iterator configElement1 = dbsConfig.getChildElements();
            while (configElement1.hasNext()) {
                OMElement property = (OMElement) configElement1.next();
                String value = property.getAttributeValue(new QName("name"));
                if ("org.wso2.ws.dataservice.protocol".equals(value)) {
                    property.setText(jdbcUrl);

                } else if ("org.wso2.ws.dataservice.driver".equals(value)) {
                    property.setText(jdbcDriver);

                } else if ("org.wso2.ws.dataservice.user".equals(value)) {
                    property.setText(databaseUser);

                } else if ("org.wso2.ws.dataservice.password".equals(value)) {
                    property.setText(databasePassword);
                }
            }
            log.debug(dbsFile);
            ByteArrayDataSource dbs = new ByteArrayDataSource(dbsFile.toString().getBytes());
            return new DataHandler(dbs);

        } catch (XMLStreamException e) {
            log.error("XMLStreamException when Reading Service File", e);
            throw new XMLStreamException("XMLStreamException when Reading Service File", e);
        } catch (IOException e) {
            log.error("IOException when Reading Service File", e);
            throw new IOException("IOException  when Reading Service File", e);
        }

    }

    public void createDataSource(List<File> sqlFileList)
            throws IOException, RSSAdminRSSDAOExceptionException, ClassNotFoundException,
                   SQLException {
        databaseName = frameworkProperties.getDataSource().getDbName();
        if (frameworkProperties.getEnvironmentSettings().is_runningOnStratos()) {
            rSSAdminConsoleService = new RSSAdminConsoleService(dssBackEndUrl);
            databaseUser = frameworkProperties.getDataSource().getRssDbUser();
            databasePassword = frameworkProperties.getDataSource().getRssDbPassword();
            setPriConditions();
            createDataBase();
            createPrivilegeGroup();
            createUser();
        } else {
            jdbcUrl = frameworkProperties.getDataSource().getDbUrl();
            jdbcDriver = frameworkProperties.getDataSource().getM_dbDriverName();
            databaseUser = frameworkProperties.getDataSource().getDbUser();
            databasePassword = frameworkProperties.getDataSource().getDbPassword();
            createDataBase(jdbcUrl, databaseUser, databasePassword);

        }
        executeUpdate(sqlFileList);
    }

    public void createDataSource(String dbName, String dbUser, String dbPassword,
                                 List<File> sqlFileList)
            throws IOException, RSSAdminRSSDAOExceptionException, ClassNotFoundException,
                   SQLException {
        databaseName = dbName;

        if (frameworkProperties.getEnvironmentSettings().is_runningOnStratos()) {
            rSSAdminConsoleService = new RSSAdminConsoleService(dssBackEndUrl);
            databaseUser = dbUser;
            databasePassword = dbPassword;
            setPriConditions();
            createDataBase();
            createPrivilegeGroup();
            createUser();
        } else {
            jdbcUrl = frameworkProperties.getDataSource().getDbUrl();
            jdbcDriver = frameworkProperties.getDataSource().getM_dbDriverName();
            databaseUser = frameworkProperties.getDataSource().getDbUser();
            databasePassword = frameworkProperties.getDataSource().getDbPassword();
            createDataBase(jdbcUrl, databaseUser, databasePassword);

        }
        executeUpdate(sqlFileList);
    }

    private void createDataBase() throws RSSAdminRSSDAOExceptionException, RemoteException {
        RSSInstanceEntry rssInstance;

        rssInstance = rSSAdminConsoleService.getRoundRobinAssignedRSSInstance(sessionCookie);
        rssInstanceId = rssInstance.getRssInstanceId();
        log.info("RSS Instance Id :" + rssInstanceId);

        //creating database
        rSSAdminConsoleService.createDatabase(sessionCookie, databaseName, rssInstanceId);
        log.info("Database created");
        //set database full name
        databaseName = databaseName + "_" + userInfo.getDomain().replace(".", "_");
        log.info("Database name :" + databaseName);

        jdbcUrl = rssInstance.getServerUrl() + "/" + databaseName;
        log.info("JDBC URL :" + jdbcUrl);

        dbInstanceId = rSSAdminConsoleService.getDatabaseInstance(sessionCookie, databaseName).getDbInstanceId();
        log.debug("Database instance id :" + dbInstanceId);

    }

    private void createDataBase(String jdbc, String user, String password)
            throws ClassNotFoundException, SQLException {
        try {
            DatabaseManager dbm = DatabaseFactory.getDatabaseConnector(jdbc, user, password);
            dbm.executeUpdate("DROP DATABASE IF EXISTS " + databaseName);
            dbm.executeUpdate("CREATE DATABASE " + databaseName);
            jdbcUrl = jdbc + "/" + databaseName;

            dbm.disconnect();
        } catch (ClassNotFoundException e) {
            log.error("Class Not Found. Check MySql-jdbc Driver in classpath: ", e);
            throw new ClassNotFoundException("Class Not Found. Check MySql-jdbc Driver in classpath: ", e);
        } catch (SQLException e) {
            log.error("SQLException When executing SQL: ", e);
            throw new SQLException("SQLException When executing SQL: ", e);
        }

    }

    private void createPrivilegeGroup() throws RSSAdminRSSDAOExceptionException, RemoteException {
        rSSAdminConsoleService.createPrivilegeGroup(sessionCookie, userPrivilegeGroup);
        userPrivilegeGroupId = rSSAdminConsoleService.getPrivilegeGroup(sessionCookie, userPrivilegeGroup).getPrivGroupId();
        log.info("privilege Group Created");
        log.debug("Privilege Group Id :" + userPrivilegeGroupId);
        Assert.assertNotSame(-1, userPrivilegeGroupId, "Privilege Group Not Found");
    }

    private void createUser() throws RSSAdminRSSDAOExceptionException, RemoteException {
        DatabaseUserEntry dbUser;
        rSSAdminConsoleService.createUser(sessionCookie, databaseUser, databasePassword, dbInstanceId, userPrivilegeGroupId);
        log.info("Database User Created");

        dbUser = rSSAdminConsoleService.getDatabaseUser(sessionCookie, databaseUser, dbInstanceId);
        databaseUserId = dbUser.getUserId();
        log.debug("Database UserId :" + databaseUserId);

        databaseUser = rSSAdminConsoleService.getFullyQualifiedUsername(databaseUser, userInfo.getDomain());
        log.info("Database User Name :" + databaseUser);
        Assert.assertEquals(dbUser.getUsername(), databaseUser, "Database UserName mismatched");

    }

    private void setPriConditions() throws RSSAdminRSSDAOExceptionException, RemoteException {
        DatabaseInstanceEntry dbInstance;
        DatabaseUserEntry userEntry;
        PrivilegeGroup privGroup;


        log.info("Setting pre conditions");

        dbInstance = rSSAdminConsoleService.getDatabaseInstance(sessionCookie, databaseName + "_" + userInfo.getDomain().replace(".", "_"));
        if (dbInstance != null) {
            log.info("Database name already in server");
            userEntry = rSSAdminConsoleService.getDatabaseUser(sessionCookie, rSSAdminConsoleService.getFullyQualifiedUsername(databaseUser, userInfo.getDomain()), dbInstance.getDbInstanceId());
            if (userEntry != null) {

                log.info("User already in Database. deleting user");
                rSSAdminConsoleService.deleteUser(sessionCookie, userEntry.getUserId(), dbInstance.getDbInstanceId());
                log.info("User Deleted");
            }
            log.info("Dropping database");
            rSSAdminConsoleService.dropDatabase(sessionCookie, dbInstance.getDbInstanceId());
            log.info("database Dropped");
        }

        privGroup = rSSAdminConsoleService.getPrivilegeGroup(sessionCookie, userPrivilegeGroup);
        if (privGroup != null) {
            log.info("Privilege Group name already in server");
            rSSAdminConsoleService.deletePrivilegeGroup(sessionCookie, privGroup.getPrivGroupId());
            log.info("Privilege Group Deleted");
        }
        log.info("pre conditions created");

    }

    private void executeUpdate(List<File> sqlFileList)
            throws IOException, ClassNotFoundException, SQLException {

        try {
            DatabaseManager dbm = DatabaseFactory.getDatabaseConnector(jdbcUrl, databaseUser, databasePassword);
            for (File sql : sqlFileList) {
                dbm.executeUpdate(sql);
            }
            dbm.disconnect();
        } catch (IOException e) {
            log.error("IOException When reading SQL files: ", e);
            throw new IOException("IOException When reading SQL files: ", e);
        } catch (ClassNotFoundException e) {
            log.error("Class Not Found. Check MySql-jdbc Driver in classpath: " + e);
            throw new ClassNotFoundException("Class Not Found. Check MySql-jdbc Driver in classpath: ", e);
        } catch (SQLException e) {
            log.error("SQLException When executing SQL: " + e);
            throw new SQLException("SQLException When executing SQL: ", e);
        }
    }
}
