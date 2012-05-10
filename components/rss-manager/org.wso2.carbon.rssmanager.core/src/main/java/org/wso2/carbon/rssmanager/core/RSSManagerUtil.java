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
package org.wso2.carbon.rssmanager.core;

import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.util.Base64;
import org.wso2.carbon.rssmanager.common.RSSManagerCommonUtil;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.connections.DBConnectionHandler;
import org.wso2.carbon.rssmanager.core.dao.RSSConfig;
import org.wso2.carbon.rssmanager.core.dao.RSSDAOFactory;
import org.wso2.carbon.rssmanager.core.description.*;
import org.wso2.carbon.rssmanager.core.exception.RSSDAOException;
import org.wso2.carbon.rssmanager.core.internal.RSSManagerServiceComponent;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class contains all the util method required for the core functionalities.
 */
public class RSSManagerUtil {

    private static RSSConfig currentRSSConfig;

    private static final OMFactory omFactory = (OMFactory) OMAbstractFactory.getOMFactory();

    private static final String NULL_NAMESPACE = "";

    private static final OMNamespace NULL_OMNS = omFactory.createOMNamespace(NULL_NAMESPACE, "");

    /**
     * Retrieves the RSS config reading the rss-instance configuration file.
     *
     * @return RSSConfig
     * @throws RSSDAOException rssDaoException
     */
    public static RSSConfig getRSSConfig() throws RSSDAOException {
        if (currentRSSConfig == null) {
            String rssConfigXMLPath = CarbonUtils.getCarbonConfigDirPath()
                    + File.separator + "advanced" + File.separator
                    + RSSManagerConstants.WSO2_RSS_CONFIG_XML_NAME;
            try {
                currentRSSConfig = new RSSConfig(AXIOMUtil.stringToOM(
                        new String(CarbonUtils.getBytesFromFile(new File(rssConfigXMLPath)))));
            } catch (Exception e) {
                throw new RSSDAOException("Error in creating RSS config", e);
            }
        }
        return currentRSSConfig;
    }

    /**
     * Creating a DatabaseInstance out of the information serialized into an OMElement.
     *
     * @param dbEl OMElement containing DatabaseInstance information.
     * @return DatabaseInstance object.
     */
    public static DatabaseInstance buildDatabaseInstance(OMElement dbEl) throws RSSDAOException {
        OMElement el;
        try {
            el = AXIOMUtil.stringToOM(((OMTextImpl) (dbEl.getChildren().next())).getText());
        } catch (XMLStreamException e) {
            throw new RSSDAOException("Unable to retrieve database instance data", e);
        }
        DatabaseInstance db = new DatabaseInstance();
        String name = el.getAttributeValue(new QName(NULL_NAMESPACE, "name"));
        if (name != null) {
            db.setName(name);
        }
        String rssInsId = el.getAttributeValue(new QName(NULL_NAMESPACE, "rssInsId"));
        if (rssInsId != null) {
            db.setRssInstanceId(Integer.parseInt(rssInsId));
        }
        String dbInsId = el.getAttributeValue(new QName(NULL_NAMESPACE, "dbInsId"));
        if (dbInsId != null) {
            db.setDatabaseInstanceId(Integer.parseInt(dbInsId));
        }
        String properties = el.getAttributeValue(new QName(NULL_NAMESPACE, "properties"));
        if (properties != null) {
            db.setProperties(null);
        }

        return db;
    }

    /**
     * Creating a DatabasePermissions object out of the information serialized into an OMElement.
     *
     * @param privilegesElement OMElement containing the DatabasePermissions information.
     * @return DatabasePermissions object.
     */
    public static DatabasePermissions getPermissionObject(OMElement privilegesElement) {
        DatabasePermissions permissions = new DatabasePermissions();
        Iterator attributeIterator = privilegesElement.getAllAttributes();
        
        while (attributeIterator.hasNext()) {
            OMAttribute attribute = (OMAttribute) attributeIterator.next();
            String attributeName = attribute.getLocalName();
            String value = attribute.getAttributeValue();
            for (String priv : RSSManagerCommonUtil.getDatabasePrivilegeList()) {
                if (priv.equals(attributeName)) {
                    if (value != null) {
                        if (RSSManagerCommonUtil.getBooleanResponsePrivilegeList().contains(priv)) {
                            permissions.setPermission(priv, value);
                        } else if (RSSManagerCommonUtil.getBlobResponsePrivilegeList().
                                contains(priv)) {
                            permissions.setPermission(priv, value);
                        } else if (RSSManagerCommonUtil.getIntegerResponsePrivilegeList().
                                contains(priv)) {
                            permissions.setPermission(priv, Integer.parseInt(value));
                        } else if (RSSManagerCommonUtil.getStringResponsePrivilegeList().
                                contains(priv)) {
                            permissions.setPermission(priv, value);
                        }
                    } else {
                        if (RSSManagerCommonUtil.getBooleanResponsePrivilegeList().contains(priv)) {
                            permissions.setPermission(priv, "N");
                        } else if (RSSManagerCommonUtil.getBlobResponsePrivilegeList().
                                contains(priv)) {
                            permissions.setPermission(priv, "");
                        } else if (RSSManagerCommonUtil.getIntegerResponsePrivilegeList().
                                contains(priv)) {
                            permissions.setPermission(priv, 0);
                        } else if (RSSManagerCommonUtil.getStringResponsePrivilegeList().
                                contains(priv)) {
                            permissions.setPermission(priv, "");
                        }
                    }
                }
            }
        }
        return permissions;
    }

    /**
     * Serializes database instance information to an OMElement.
     *
     * @param targetNamespace targetNamespace.
     * @param dbe database instance entry object to be serialized.
     * @return serialized DatabaseInstance object.
     * @throws RSSDAOException rssDaoException.
     */
    public static OMElement serializeDatabaseInstanceEntry(
            OMNamespace targetNamespace, DatabaseInstanceEntry dbe) throws RSSDAOException {
        if (dbe == null) {
            throw new RSSDAOException("Database instance entry cannot be null");
        }

        OMElement dbEl = omFactory.createOMElement("db", targetNamespace);
        String dbName = dbe.getDbName();
        if (!"".equals(dbName) && dbName != null) {
            dbEl.addAttribute("dbName", dbName, NULL_OMNS);
        }
        String dbInstId = String.valueOf(dbe.getDbInstanceId());
        if (!"".equals(dbInstId) && dbInstId != null) {
            dbEl.addAttribute("dbInstanceId", dbInstId, NULL_OMNS);
        }
        String dbUrl = dbe.getDbUrl();
        if (!"".equals(dbUrl) && dbUrl != null) {
            dbEl.addAttribute("dbUrl", dbUrl, NULL_OMNS);
        }
        String rssName = dbe.getRssName();
        if (rssName != null && !"".equals(rssName)) {
            dbEl.addAttribute("rssName", rssName, NULL_OMNS);
        }
        String rssTenantDomain = dbe.getRssTenantDomain();
        if (rssTenantDomain != null && !"".equals(rssTenantDomain)) {
            dbEl.addAttribute("rssTenantDomain", rssTenantDomain, NULL_OMNS);
        }
        return dbEl;
    }

    /**
     * Serializes database instance information to an OMElement.
     *
     * @param targetNamespace targetNamespace.
     * @param db              database instance object to be serialized.
     * @return serialized DatabaseInstance object.
     * @throws RSSDAOException rssDaoException.
     */
    public static OMElement serializeDatabaseInstance(
            OMNamespace targetNamespace, DatabaseInstance db) throws RSSDAOException {
        if (db == null) {
            throw new RSSDAOException("Database instance cannot be null");
        }

        OMElement dbEl = omFactory.createOMElement("db", targetNamespace);
        String name = db.getName();
        if (!"".equals(name) && name != null) {
            dbEl.addAttribute("name", name, NULL_OMNS);
        }
        String dbInstId = String.valueOf(db.getDatabaseInstanceId());
        if (!"".equals(dbInstId) && dbInstId != null) {
            dbEl.addAttribute("dbInsId", dbInstId, NULL_OMNS);
        }
        String rssInstId = String.valueOf(db.getRssInstanceId());
        if (!"".equals(rssInstId) && rssInstId != null) {
            dbEl.addAttribute("rssInsId", rssInstId, NULL_OMNS);
        }

        return dbEl;
    }

    /**
     * Serializes database user permissions to an OMElement.
     *
     * @param targetNamespace targetNamespace.
     * @param permissions     database permissions.
     * @return serialized DatabasePermissions list.
     */
    public static OMElement serializeUserPermissions(OMNamespace targetNamespace,
                                                     Map<String, Object> permissions) {
        OMElement permissionsElement = omFactory.createOMElement("permissions", targetNamespace);
        if (permissions != null) {
            for (Map.Entry entry : permissions.entrySet()) {
                Object attributeValue = entry.getValue();
                if (attributeValue != null) {
                    permissionsElement.addAttribute(entry.getKey().toString(),
                            attributeValue.toString(), targetNamespace);
                } else {
                    permissionsElement.addAttribute(entry.getKey().toString(), "",
                            targetNamespace);
                }
            }
        }
        return permissionsElement;
    }

    /**
     * Util method to prepare JDBC url of a particular RSS instance to be a valid url to be stored
     * in the metadata repository.
     *
     * @param url    JDBC url.
     * @param dbName Name of the database instance.
     * @return Processed JDBC url.
     */
    public static String processJdbcUrl(String url, String dbName) {
        if (url != null && !"".equals(url)) {
            return url.endsWith("/") ? (url + dbName) : (url + "/" + dbName);
        }
        return "";
    }

    /**
     * Util method to prepareUpdateSqlQueries corresponding to data manipulations done against
     * the system databases.
     *
     * @param queryPrefix queryPrefix.
     * @param querySuffix querySuffix.
     * @param privileges  list of privileges.
     * @return sqlQuery for updating system database entities.
     */
    public static String prepareUpdateSQLString(String queryPrefix, String querySuffix,
                                                List<String> privileges) {
        StringBuilder sql = new StringBuilder(queryPrefix);
        for (int i = 0; i < privileges.size(); i++) {
            if (i != privileges.size() - 1) {
                sql.append(privileges.get(i)).append("=?,");
            } else {
                sql.append(privileges.get(i)).append("=?");
            }
        }
        sql.append(querySuffix);
        return sql.toString();
    }

    /**
     * Returns the fully qualified name of the database to be created. This will append an
     * underscore and the tenant's domain name to the database to make it unique for that particular
     * tenant. It will return the database name as it is, if it is created in Super tenant mode.
     *
     * @param dbName Name of the database
     * @return Fully qualified name of the database
     */
    public static String getFullyQualifiedDatabaseName(String dbName) {
        String tenantDomain =
                CarbonContextHolder.getCurrentCarbonContextHolder().getTenantDomain();
        if (tenantDomain != null) {
            return dbName + "_" + RSSManagerCommonUtil.processDomainName(tenantDomain);
        }
        return dbName;
    }

    /**
     * Converts a list of database privileges to a map containing {priv_name, priv_value} pairs.
     *
     * @param privs The list of privileges to be converted to the map.
     * @return A map of {priv_name, priv_value} pairs.
     */
    public static Map<String, Object> convertPrivListToMap(List<DatabasePrivilege> privs) {
        Map<String, Object> privMap = new HashMap<String, Object>();
        for (DatabasePrivilege priv : privs) {
            privMap.put(priv.getPrivName(), priv.getPrivValue());
        }
        return privMap;
    }

    /**
     * Returns the fully qualified username of a particular database user. For an ordinary tenant,
     * the tenant domain will be appended to the username together with an underscore and the given
     * username will be returned as it is in the case of super tenant.
     *
     * @param username Username of the database user.
     * @return Fully qualified username of the database user.
     */
    public static String getFullyQualifiedUsername(String username) {
        String tenantDomain = CarbonContextHolder.getCurrentCarbonContextHolder().getTenantDomain();
        if (tenantDomain != null) {

            /* The maximum number of characters allowed for the username in mysql system tables is
             * 16. Thus, to adhere the aforementioned constraint as well as to give the username
             * an unique identification based on the tenant domain, we append a hash value that is
             * created based on the tenant domain */
            byte[] bytes = RSSManagerCommonUtil.intToByteArray(tenantDomain.hashCode());
            return username + "_" + Base64.encode(bytes);
        }
        return username;
    }

    /**
     * Check whether a user with the same name already exists.
     *
     * @param rssIns   Details of the RSS instance to which the user belongs to.
     * @param username Username of the database user to be validated.
     * @return A boolean representing the existence of the given username.
     * @throws RSSDAOException rssDaoException.
     */
    public static boolean validateUser(RSSInstance rssIns, String username) throws RSSDAOException {
        try {
            PreparedStatement stmt = DBConnectionHandler.getConnection(rssIns).prepareStatement(
                    "SELECT 1 FROM mysql.user WHERE user=? AND host=?");
            stmt.setString(1, username);
            stmt.setString(2, "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) == 1) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new RSSDAOException("Unable to validate the user " + username, e);
        }
        return false;
    }

    /**
     * Checks whether the particular privilege group identified by the given id belongs to the
     * current tenant.
     *
     * @param privGroupId Id of the privilege group.
     * @return Boolean confirming the ownership of the privilege group identified by the given Id.
     * @throws RSSDAOException rssDaoException.
     */
    public static boolean privilegeGroupBelongsToCurrentTenant(int privGroupId) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        int privGroupTenantId = -1;
        int currentTenantId = CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId();
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT tenant_id FROM USER_PRIVILEGE_GROUP WHERE priv_group_id = ?");
            stmt.setInt(1, privGroupId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                privGroupTenantId = rs.getInt(1);
            }
            return (currentTenantId == privGroupTenantId);
        } catch (SQLException e) {
            throw new RSSDAOException("Unable to retrieve tenant id of the privilege group");
        }
    }

    /**
     * Checks whether the particular database user identified by the given user id belongs to the
     * current tenant.
     *
     * @param userId Id of the database user.
     * @return Boolean confirming the ownership of the database user identified by the given Id.
     * @throws RSSDAOException rssDaoException.
     */
    public static boolean userBelongsToCurrentTenant(int userId) throws RSSDAOException {
        Connection conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
        int userTenantId = -1;
        int currentTenantId = CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId();
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT user_tenant_id FROM " +
                    "DATABASE_USER WHERE user_id = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                userTenantId = rs.getInt(1);
            }
            return (currentTenantId == userTenantId);
        } catch (SQLException e) {
            throw new RSSDAOException("Unable to retrieve tenant id of the database user");
        }
    }

    /**
     * Checks whether the database instance belongs to the current tenant.
     *
     * @param ins Database instance bean object to be analyzed.
     * @return Boolean confirming the ownership of the database instance.
     * @throws RSSDAOException rssDaoException.
     */
    public static boolean dbBelongsToCurrentTenant(DatabaseInstance ins) throws RSSDAOException {
        int currentTenantId = CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId();
        return (ins != null) && (currentTenantId == ins.getTenantId());
    }

    public static boolean rssInstanceBelongsToTenant(int rssInsId) throws RSSDAOException {
        int currentTenantId = CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId();
        RSSInstance rssIns = RSSDAOFactory.getRSSDAO().getRSSInstanceById(rssInsId);
        return (rssIns != null) && (currentTenantId == rssIns.getTenantId());
    }

    public static boolean isSuperTenant() {
        int currentTenantId = CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId();
        return (currentTenantId == MultitenantConstants.SUPER_TENANT_ID);
    }

    public static RSSInstanceEntry createRSSInstanceEntryFromRSSInstanceData(
            RSSInstance rssIns) throws RSSDAOException {
       return new RSSInstanceEntry(rssIns.getRssInstanceId(), rssIns.getName(),
               rssIns.getServerURL(), rssIns.getInstanceType(), rssIns.getServerCategory(),
               getTenantDomain(rssIns.getTenantId()));
    }

    public static DatabaseInstanceEntry createDbInstanceEntryFromDbInstanceData(DatabaseInstance
            dbIns) throws RSSDAOException {
        RSSInstance rssIns = RSSDAOFactory.getRSSDAO().getRSSInstanceById(dbIns.getRssInstanceId());
        if (rssIns.getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
            rssIns.setName(RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
        }
        return new DatabaseInstanceEntry(dbIns.getDatabaseInstanceId(), dbIns.getName(),
                rssIns.getServerURL() + "/" + dbIns.getName(), rssIns.getRssInstanceId(),
                rssIns.getName(), getTenantDomain(dbIns.getTenantId()));
    }

    /**
     * Retireves the tenant domain corresponding to a particular tenant id.
     *
     * @param tenantId tenant Id.
     * @return Domain name of the tenant.
     * @throws RSSDAOException rssDaoException
     */
    public static String getTenantDomain(int tenantId) throws RSSDAOException {
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            return RSSManagerConstants.STRATOS_RSS;
        } else {
            try {
                return RSSManagerServiceComponent.getTenantManager().getDomain(tenantId);
            } catch (UserStoreException e) {
                throw new RSSDAOException("Unable to retrieve domain name");
            }
        }
    }

    /**
     * Checks whether a particular database already exists for a particular tenant.
     *
     * @param dbName           Name of the database
     * @return                 Boolean representing the existence of database with the given name
     * @throws RSSDAOException Is thrown when encountered issues in creating RSSConfig
     * @throws SQLException    Is thrown when encountered issues in closing/acquiring
     *         database connections/prepared statements/result sets
     */
    public static boolean databaseExists(String dbName) throws RSSDAOException, SQLException {
        Connection conn = null;
        int tid = CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId();
        try {
            conn = RSSManagerUtil.getRSSConfig().getRSSDBConnection();
            String sql = "SELECT 1 FROM DATABASE_INSTANCE WHERE name = ? AND tenant_id = ?";
            PreparedStatement stmt = conn.prepareCall(sql);
            stmt.setString(1, dbName);
            stmt.setInt(2, tid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int existCode = rs.getInt(1);
                if (existCode == 1) {
                    rs.close();
                    stmt.close();
                    return true;
                }
            }
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return false;
    }


}


