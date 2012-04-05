/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.cassandra.mgt;


import me.prettyprint.cassandra.model.BasicColumnDefinition;
import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHost;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.cassandra.service.ThriftCluster;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ddl.ColumnDefinition;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ColumnIndexType;
import me.prettyprint.hector.api.ddl.ColumnType;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.cassandra.thrift.TokenRange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.cassandra.mgt.internal.CassandraAdminDSComponent;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Cassandra Management(Admin) Service
 */

public class CassandraKeyspaceAdmin extends AbstractAdmin {

    //TODO use constants from user manager
    private static final String ACTION_WRITE = "write";
    private static final String ACTION_READ = "read";
    private static final String KEYSPACE_SYSTEM = "system";
    private static final String USER_ACCESSKEY_ATTR_NAME = "cassandra.user.password";
    private static final String CASSANDRA_AUTH_CONF = "repository" + File.separator + "conf"
                                                      + File.separator + "etc" + File.separator
                                                      + "cassandra-auth.xml";


    private static final Log log = LogFactory.getLog(CassandraKeyspaceAdmin.class);

    /**
     * @return
     * @throws CassandraServerManagementException
     *
     */

    public String getClusterName() throws CassandraServerManagementException {
        Cluster cluster = getCluster(null);
        return cluster.getClusterName();
    }

    /**
     * @param clusterName The name of the cluster
     * @param username    The name of the current user
     * @param password    The password of the current user
     * @return A list of keyspace names
     * @throws CassandraServerManagementException
     *          for any errors during locating keyspaces
     */
    public String[] listKeyspaces(String clusterName, String username, String password)
            throws CassandraServerManagementException {

        if (username == null || "".equals(username.trim())) {
            throw new CassandraServerManagementException("The username is empty or null", log);
        }

        if (password == null || "".equals(password.trim())) {
            throw new CassandraServerManagementException("The password is empty or null", log);
        }

        ClusterInformation clusterInformation = new ClusterInformation(username.trim(), password.trim());
        clusterInformation.setClusterName(clusterName);
        return getKeyspaces(clusterInformation);
    }

    /**
     * Returns the all the keyspaces of the current user
     *
     * @return A list of keyspace names
     * @throws CassandraServerManagementException
     *          for any errors during locating keyspaces
     */
    public String[] listKeyspacesOfCurrentUser() throws CassandraServerManagementException {
        return getKeyspaces(null);
    }

    /**
     * Returns the all the column family names of the current user for the given keyspace
     *
     * @param keyspaceName The name of the keyspace
     * @return A list of column family names
     * @throws CassandraServerManagementException
     *          For any errors
     */
    public String[] listColumnFamiliesOfCurrentUser(String keyspaceName)
            throws CassandraServerManagementException {

        KeyspaceDefinition keyspaceDefinition = getKeyspaceDefinition(keyspaceName);

        List<String> cfNames = new ArrayList<String>();
        for (ColumnFamilyDefinition columnFamilyDefinition : keyspaceDefinition.getCfDefs()) {
            String name = columnFamilyDefinition.getName();
            if (name != null && !"".equals(name)) {
                cfNames.add(name);
            }
        }
        return cfNames.toArray(new String[cfNames.size()]);
    }

    /**
     * Returns meta-data for a given keyspace
     *
     * @param keyspaceName The name of the keyspace      *
     * @return meta-data about the keyspace
     * @throws CassandraServerManagementException
     *          For any errors during accessing a keyspace
     */
    public KeyspaceInformation getKeyspaceofCurrentUser(String keyspaceName)
            throws CassandraServerManagementException {

        KeyspaceDefinition keyspaceDefinition = getKeyspaceDefinition(keyspaceName);
        KeyspaceInformation keyspaceInformation = new KeyspaceInformation(keyspaceDefinition.getName());
        keyspaceInformation.setStrategyClass(keyspaceDefinition.getStrategyClass());
        keyspaceInformation.setReplicationFactor(keyspaceDefinition.getReplicationFactor());
        List<ColumnFamilyInformation> columnFamilyInformations = new ArrayList<ColumnFamilyInformation>();
        for (ColumnFamilyDefinition definition : keyspaceDefinition.getCfDefs()) {
            if (definition != null) {
                columnFamilyInformations.add(createColumnFamilyInformation(definition));
            }
        }
        keyspaceInformation.setColumnFamilies(
                columnFamilyInformations.toArray(new ColumnFamilyInformation[columnFamilyInformations.size()]));
        return keyspaceInformation;
    }

    /**
     * Retrieve a CF
     *
     * @param keyspaceName     the name of the keyspace
     * @param columnFamilyName the name of the CF
     * @return CF meta-data
     * @throws CassandraServerManagementException
     *          for errors in removing operation
     */
    public ColumnFamilyInformation getColumnFamilyOfCurrentUser(String keyspaceName,
                                                                String columnFamilyName)
            throws CassandraServerManagementException {

        try {
            KeyspaceDefinition keyspaceDefinition = getKeyspaceDefinition(keyspaceName);
            validateCF(columnFamilyName);
            //TODO change hector to get exactly one CF
            for (ColumnFamilyDefinition definition : keyspaceDefinition.getCfDefs()) {
                if (definition != null && columnFamilyName.equals(definition.getName())) {
                    return createColumnFamilyInformation(definition);
                }
            }
            throw new CassandraServerManagementException("There is no column family with the name :" + columnFamilyName,
                                                         log);
        } catch (HectorException e) {
            throw new CassandraServerManagementException("Error accessing a column family with the name :" +
                                                         columnFamilyName, e, log);
        }
    }

    /**
     * Shared a resource with a role
     *
     * @param role the name of the role
     * @param path resource path
     * @return true if the sharing would be successful.
     * @throws CassandraServerManagementException
     *          For any errors
     */
    public boolean shareResource(String role, String path)
            throws CassandraServerManagementException {

        if (role == null || "".equals(role.trim())) {
            throw new CassandraServerManagementException("Username is null or empty", log);
        }
        role = role.trim();

        if (path == null || "".equals(path.trim())) {
            throw new CassandraServerManagementException("Resource path is null or empty", log);
        }
        path = path.trim();

        try {
            CassandraAdminComponentManager adminComponentManager = CassandraAdminComponentManager.getInstance();
            UserRealm userRealm = adminComponentManager.getRealmForCurrentTenant();
            //TODO ask the best way from security team
            AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();
            authorizationManager.clearRoleAuthorization(role, path, ACTION_WRITE);
            authorizationManager.clearRoleAuthorization(role, path, ACTION_READ);
            authorizationManager.authorizeRole(role, path, ACTION_WRITE);
            authorizationManager.authorizeRole(role, path, ACTION_READ);
            return true;
        } catch (UserStoreException e) {
            throw new CassandraServerManagementException("Error during sharing a resource at path :" + path + " and" +
                                                         " for role :" + role, e, log);
        }
    }

     public boolean shareResourceRead(String role, String path)
            throws CassandraServerManagementException {

        if (role == null || "".equals(role.trim())) {
            throw new CassandraServerManagementException("Username is null or empty", log);
        }
        role = role.trim();

        if (path == null || "".equals(path.trim())) {
            throw new CassandraServerManagementException("Resource path is null or empty", log);
        }
        path = path.trim();

        try {
            CassandraAdminComponentManager adminComponentManager = CassandraAdminComponentManager.getInstance();
            UserRealm userRealm = adminComponentManager.getRealmForCurrentTenant();
            AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();
            //authorizationManager.clearRoleAuthorization(role, path, ACTION_WRITE);
            authorizationManager.clearRoleAuthorization(role, path, ACTION_READ);
            //authorizationManager.authorizeRole(role, path, ACTION_WRITE);
            authorizationManager.authorizeRole(role, path, ACTION_READ);
            return true;
        } catch (UserStoreException e) {
            throw new CassandraServerManagementException("Error during sharing a resource at path :" + path + " and" +
                                                         " for role :" + role, e, log);
        }
    }

    public boolean shareResourceWrite(String role, String path)
            throws CassandraServerManagementException {

        if (role == null || "".equals(role.trim())) {
            throw new CassandraServerManagementException("Username is null or empty", log);
        }
        role = role.trim();

        if (path == null || "".equals(path.trim())) {
            throw new CassandraServerManagementException("Resource path is null or empty", log);
        }
        path = path.trim();

        try {
            CassandraAdminComponentManager adminComponentManager = CassandraAdminComponentManager.getInstance();
            UserRealm userRealm = adminComponentManager.getRealmForCurrentTenant();
            AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();
            authorizationManager.clearRoleAuthorization(role, path, ACTION_WRITE);
//            authorizationManager.clearRoleAuthorization(role, path, ACTION_READ);
            authorizationManager.authorizeRole(role, path, ACTION_WRITE);
//            authorizationManager.authorizeRole(role, path, ACTION_READ);
            return true;
        } catch (UserStoreException e) {
            throw new CassandraServerManagementException("Error during sharing a resource at path :" + path + " and" +
                                                         " for role :" + role, e, log);
        }
    }

    /**
     * Clear sharing of a resource with a role
     *
     * @param role the name of the role
     * @param path resource path
     * @return true if the sharing would be successful.
     * @throws CassandraServerManagementException
     *          For any errors
     */
    public boolean clearResource(String role, String path)
            throws CassandraServerManagementException {

        if (role == null || "".equals(role.trim())) {
            throw new CassandraServerManagementException("Username is null or empty", log);
        }
        role = role.trim();

        if (path == null || "".equals(path.trim())) {
            throw new CassandraServerManagementException("Resource path is null or empty", log);
        }
        path = path.trim();

        try {
            CassandraAdminComponentManager adminComponentManager = CassandraAdminComponentManager.getInstance();
            UserRealm userRealm = adminComponentManager.getRealmForCurrentTenant();
            //TODO ask the best way from security team
            AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();
            authorizationManager.clearRoleAuthorization(role, path, ACTION_WRITE);
            authorizationManager.clearRoleAuthorization(role, path, ACTION_READ);
            return true;
        } catch (UserStoreException e) {
            throw new CassandraServerManagementException("Error during clear sharing a resource at path :" + path + " and" +
                                                         " for role :" + role, e, log);
        }
    }

    public boolean clearResourceRead(String role, String path)
            throws CassandraServerManagementException {

        if (role == null || "".equals(role.trim())) {
            throw new CassandraServerManagementException("Username is null or empty", log);
        }
        role = role.trim();

        if (path == null || "".equals(path.trim())) {
            throw new CassandraServerManagementException("Resource path is null or empty", log);
        }
        path = path.trim();

        try {
            CassandraAdminComponentManager adminComponentManager = CassandraAdminComponentManager.getInstance();
            UserRealm userRealm = adminComponentManager.getRealmForCurrentTenant();
            AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();
//            authorizationManager.clearRoleAuthorization(role, path, ACTION_WRITE);
            authorizationManager.clearRoleAuthorization(role, path, ACTION_READ);
            return true;
        } catch (UserStoreException e) {
            throw new CassandraServerManagementException("Error during clear sharing a resource at path :" + path + " and" +
                                                         " for role :" + role, e, log);
        }
    }

    public boolean clearResourceWrite(String role, String path)
            throws CassandraServerManagementException {

        if (role == null || "".equals(role.trim())) {
            throw new CassandraServerManagementException("Username is null or empty", log);
        }
        role = role.trim();

        if (path == null || "".equals(path.trim())) {
            throw new CassandraServerManagementException("Resource path is null or empty", log);
        }
        path = path.trim();

        try {
            CassandraAdminComponentManager adminComponentManager = CassandraAdminComponentManager.getInstance();
            UserRealm userRealm = adminComponentManager.getRealmForCurrentTenant();
            AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();
            authorizationManager.clearRoleAuthorization(role, path, ACTION_WRITE);
//            authorizationManager.clearRoleAuthorization(role, path, ACTION_READ);
            return true;
        } catch (UserStoreException e) {
            throw new CassandraServerManagementException("Error during clear sharing a resource at path :" + path + " and" +
                                                         " for role :" + role, e, log);
        }
    }

    /**
     * Create a new keyspace
     *
     * @param keyspaceInformation information about a keyspace
     * @throws CassandraServerManagementException
     *          For any error
     */
    public void addKeyspace(KeyspaceInformation keyspaceInformation)
            throws CassandraServerManagementException {

        validateKeyspaceInformation(keyspaceInformation);
        addOrUpdateKeyspace(true, keyspaceInformation.getName(), keyspaceInformation.getReplicationFactor(),
                            keyspaceInformation.getStrategyClass());
    }

    /**
     * Update an existing keyspace
     *
     * @param keyspaceInformation information about a keyspace
     * @throws CassandraServerManagementException
     *          For any error during update operation
     */
    public void updatedKeyspace(KeyspaceInformation keyspaceInformation)
            throws CassandraServerManagementException {

        validateKeyspaceInformation(keyspaceInformation);
        addOrUpdateKeyspace(false, keyspaceInformation.getName(), keyspaceInformation.getReplicationFactor(),
                            keyspaceInformation.getStrategyClass());
    }

    /**
     * All the users signed into the Cassandra
     *
     * @return A list of user names
     * @throws CassandraServerManagementException
     *          For errors in  loading user names
     */
    public String[] getAllRoles() throws CassandraServerManagementException {
        try {
            return super.getUserRealm().getUserStoreManager().getRoleNames();
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            throw new CassandraServerManagementException("Error loading all the users", e, log);
        }
    }

    /**
     * Return the shared roles list  for READ of a given Keyspace name
     * @param path
     * @return  a list of user roles
     * @throws CassandraServerManagementException
     */

     public String[] getKeyspaceSharedRolesRead(String path) throws CassandraServerManagementException {

         try {
            CassandraAdminComponentManager adminComponentManager = CassandraAdminComponentManager.getInstance();
            UserRealm userRealm = adminComponentManager.getRealmForCurrentTenant();
            AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();
            String [] readAllowedRoles = authorizationManager.getAllowedRolesForResource(path,ACTION_READ);
            return readAllowedRoles;
        } catch (UserStoreException e) {
            throw new CassandraServerManagementException("Error retrieving role list for :" + path, e, log);
        }

    }


     /**
     * Return the shared roles list for WRITE of a given Keyspace name
     * @param path
     * @return  a list of user roles
     * @throws CassandraServerManagementException
     */

     public String[] getKeyspaceSharedRolesWrite(String path) throws CassandraServerManagementException {

         try {
            CassandraAdminComponentManager adminComponentManager = CassandraAdminComponentManager.getInstance();
            UserRealm userRealm = adminComponentManager.getRealmForCurrentTenant();
            AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();
            String [] writeAllowedRoles = authorizationManager.getAllowedRolesForResource(path,ACTION_WRITE);
            return writeAllowedRoles;
        } catch (UserStoreException e) {
            throw new CassandraServerManagementException("Error retrieving role list for :" + path, e, log);
        }

    }
    /**
     * Remove a keyspace
     *
     * @param keyspaceName the name of the keyspace to be removed
     * @return true for success in removing operation
     * @throws CassandraServerManagementException
     *          for errors in removing operation
     */
    public boolean deleteKeyspace(String keyspaceName) throws CassandraServerManagementException {

        validateKeyspace(keyspaceName);
        try {
            Cluster cluster = getCluster(null);
            cluster.dropKeyspace(keyspaceName.trim());
        } catch (HectorException e) {
            throw new CassandraServerManagementException("Error removing a keyspace with the name :" + keyspaceName, e, log);
        }
        return true;
    }

    /**
     * Create a ColumnFamily in a given key space
     *
     * @param columnFamilyInformation mata-data about a CF
     * @throws CassandraServerManagementException
     *          For errors during adding a CF
     */
    public void addColumnFamily(ColumnFamilyInformation columnFamilyInformation)
            throws CassandraServerManagementException {
        addOrUpdateCF(true, columnFamilyInformation);
    }

    /**
     * Update an existing ColumnFamily in a given keyspace
     *
     * @param columnFamilyInformation mata-data about a CF
     * @throws CassandraServerManagementException
     *          For errors during updating a CF
     */
    public void updateColumnFamily(ColumnFamilyInformation columnFamilyInformation)
            throws CassandraServerManagementException {
        addOrUpdateCF(false, columnFamilyInformation);
    }

    /**
     * Remove a column family from a given keyspace
     *
     * @param keyspaceName     the name of the keyspace of the CF to be deleted
     * @param columnFamilyName the name of the CF to be deleleted
     * @return true for success in removing operation
     * @throws CassandraServerManagementException
     *          for errors in removing operation
     */
    public boolean deleteColumnFamily(String keyspaceName, String columnFamilyName)
            throws CassandraServerManagementException {

        validateKeyspace(keyspaceName);
        validateCF(columnFamilyName);
        try {
            Cluster cluster = getCluster(null);
            cluster.dropColumnFamily(keyspaceName.trim(), columnFamilyName.trim());
        } catch (HectorException e) {
            throw new CassandraServerManagementException("Error removing a column family with the name :" +
                                                         columnFamilyName, e, log);
        }
        return true;
    }

    /**
     * Access the teken range of a keyspace
     *
     * @param keyspace keyspace name
     * @return a list of <code>TokenRangeInformation </code>
     * @throws CassandraServerManagementException
     *          for errors during getting the token ring
     */
    public TokenRangeInformation[] getTokenRange(String keyspace)
            throws CassandraServerManagementException {
        validateKeyspace(keyspace);
        ThriftCluster thriftCluster = (ThriftCluster) getCluster(null);     // TODO  hector limitation
        Set<CassandraHost> cassandraHosts = thriftCluster.getKnownPoolHosts(true);  // This returns all endpoints if only auto discovery is set.
        int rpcPort = CassandraHost.DEFAULT_PORT;
        for (CassandraHost cassandraHost : cassandraHosts) {
            if (cassandraHost != null) {
                rpcPort = cassandraHost.getPort();  // With hector, each node has the same RPC port.
                break;
            }
        }

        List<TokenRangeInformation> tokenRangeInformations = new ArrayList<TokenRangeInformation>();

        if (!KEYSPACE_SYSTEM.equals(keyspace)) {
            List<TokenRange> tokenRanges = thriftCluster.describeRing(keyspace);
            for (TokenRange tokenRange : tokenRanges) {
                if (tokenRange != null) {
                    TokenRangeInformation tokenRangeInformation = new TokenRangeInformation();
                    tokenRangeInformation.setStartToken(tokenRange.getStart_token());
                    tokenRangeInformation.setEndToken(tokenRange.getEnd_token());
                    List<String> eps = new ArrayList<String>();
                    //if public dns is configured in cassandra-cluseter-dns.xml
                    // //insert public cassandra nodes
//                            eps.add("css0.stratoslive.wso2.com" + ":" + rpcPort);
//                            eps.add("css1.stratoslive.wso2.com" + ":" + rpcPort);
//                            break;
                    //esle
                    for (String ep : tokenRange.getEndpoints()) {
                        if (ep != null && !"".equals(ep.trim())) {
                            eps.add(ep + ":" + rpcPort); // With hector, each node has the same RPC port.

                        }
                    }
                    //end else
                    if (!eps.isEmpty()) {
                        tokenRangeInformation.setEndpoints(eps.toArray(new String[eps.size()]));
                    }
                    tokenRangeInformations.add(tokenRangeInformation);
                }
            }
        }
        return tokenRangeInformations.toArray(new TokenRangeInformation[tokenRangeInformations.size()]);
    }

    /**
     * Helper method to get all keyspace names
     *
     * @param clusterInformation Information about the target cluster
     * @return A list of keyspace names
     * @throws CassandraServerManagementException
     *          for errors during accessing keyspaces
     */
    private String[] getKeyspaces(ClusterInformation clusterInformation)
            throws CassandraServerManagementException {
        Cluster cluster = getCluster(clusterInformation);
        List<String> keyspaceNames = new ArrayList<String>();
        for (KeyspaceDefinition keyspaceDefinition : cluster.describeKeyspaces()) {
            String name = keyspaceDefinition.getName();
            if (name != null && !"".equals(name)) {
                keyspaceNames.add(name);
            }
        }
        return keyspaceNames.toArray(new String[keyspaceNames.size()]);
    }

    /**
     * helper method to get a Cassandra cluster
     *
     * @param clusterInformation Information about the target cluster
     * @return <code>Cluster</code> Instance
     * @throws CassandraServerManagementException
     *          for errors during accessing a hector cluster
     */
    private Cluster getCluster(ClusterInformation clusterInformation)
            throws CassandraServerManagementException {
        DataAccessService dataAccessService =
                CassandraAdminComponentManager.getInstance().getDataAccessService();
        Cluster cluster;
        boolean resetConnection = true;
        try {
            if (clusterInformation != null) {
                cluster = dataAccessService.getCluster(clusterInformation,resetConnection);
            } else {
                //TODO: add key caching
                String sharedKey = getSharedKey();
                cluster = dataAccessService.getClusterForCurrentUser(sharedKey,resetConnection);
            }
            return cluster;
        } catch (Throwable e) {
            super.getHttpSession().removeAttribute(USER_ACCESSKEY_ATTR_NAME); //this allows to get a new key
            String message = "Error getting cluster";
            throw new CassandraServerManagementException(message, log);
        }
    }

    /* Helper method for adding or updating a keyspace */

    private void addOrUpdateKeyspace(boolean isAdd,
                                     String keyspaceName,
                                     int replicationFactor,
                                     String replicationStrategy)
            throws CassandraServerManagementException {

        Cluster cluster = getCluster(null);
        try {
            KeyspaceDefinition definition =
                    HFactory.createKeyspaceDefinition(keyspaceName.trim(), replicationStrategy, replicationFactor, null);
            if (isAdd) {
                cluster.addKeyspace(definition);
            } else {
                cluster.updateKeyspace(definition);
            }
        } catch (HectorException e) {
            throw new CassandraServerManagementException("Error " + (isAdd ? "adding" : "updating") + " a keyspace" +
                                                         " with name :" + keyspaceName, e, log);
        }
    }

    /* Helper method for adding or updating a CF */

    private void addOrUpdateCF(boolean isAdd, ColumnFamilyInformation columnFamilyInformation)
            throws CassandraServerManagementException {

        String keyspaceName = columnFamilyInformation.getKeyspace();
        String columnFamilyName = columnFamilyInformation.getName();

        validateKeyspace(keyspaceName);
        validateCF(columnFamilyName);

        ColumnType columnType = ColumnType.STANDARD;
        String type = columnFamilyInformation.getType();
        if (type != null && !"".equals(type.trim())) {
            columnType = ColumnType.getFromValue(type.trim());
        }

        BasicColumnFamilyDefinition familyDefinition = new BasicColumnFamilyDefinition();   //TODO remove with a thrift cfd
        familyDefinition.setColumnType(columnType);
        familyDefinition.setId(columnFamilyInformation.getId());
        familyDefinition.setName(columnFamilyName);
        familyDefinition.setKeyspaceName(keyspaceName);
        familyDefinition.setKeyCacheSize(columnFamilyInformation.getKeyCacheSize());
        familyDefinition.setComment(columnFamilyInformation.getComment());
        familyDefinition.setGcGraceSeconds(columnFamilyInformation.getGcGraceSeconds());
        familyDefinition.setRowCacheSize(columnFamilyInformation.getRowCacheSize());
        familyDefinition.setReadRepairChance(columnFamilyInformation.getReadRepairChance());
        familyDefinition.setComparatorType(ComparatorType.getByClassName(columnFamilyInformation.getComparatorType()));
        if (ColumnType.SUPER == columnType) {
            familyDefinition.setSubComparatorType(
                    ComparatorType.getByClassName(columnFamilyInformation.getSubComparatorType()));
        } else {
            familyDefinition.setSubComparatorType(null);
        }
        familyDefinition.setMaxCompactionThreshold(columnFamilyInformation.getMaxCompactionThreshold());
        familyDefinition.setMinCompactionThreshold(columnFamilyInformation.getMinCompactionThreshold());

        String defaultValidationClass = columnFamilyInformation.getDefaultValidationClass();
        if (defaultValidationClass != null && !"".equals(defaultValidationClass.trim())) {
            familyDefinition.setDefaultValidationClass(defaultValidationClass.trim());
        }

        ColumnInformation[] columns = columnFamilyInformation.getColumns();
        if (columns != null && columns.length > 0) {
            for (ColumnInformation column : columns) {
                validateColumnInformation(column);

                BasicColumnDefinition columnDefinition = new BasicColumnDefinition();
                columnDefinition.setName(StringSerializer.get().toByteBuffer(column.getName().trim()));

                String indexName = column.getIndexName();
                if (indexName != null && !"".equals(indexName.trim())) {
                    columnDefinition.setIndexName(indexName.trim());
                }

                String validationClass = column.getValidationClass();
                if (validationClass != null && !"".equals(validationClass.trim())) {
                    columnDefinition.setValidationClass(validationClass.trim());
                }

                String indexType = column.getIndexType();
                if (indexType != null && !"".equals(indexType.trim())) {
                    columnDefinition.setIndexType(ColumnIndexType.valueOf(indexType.trim().toUpperCase()));
                }
                familyDefinition.addColumnDefinition(columnDefinition);
            }
        }

        try {
            Cluster cluster = getCluster(null);
            if (isAdd) {
                cluster.addColumnFamily(new ThriftCfDef(familyDefinition));
            } else {
                cluster.updateColumnFamily(new ThriftCfDef(familyDefinition));
            }
        } catch (HectorException e) {
            throw new CassandraServerManagementException("Error " + (isAdd ? "adding" : "updating ") + " a column family with" +
                                                         " the name :" + columnFamilyName, e, log);
        }
    }

    private void validateKeyspaceInformation(KeyspaceInformation information)
            throws CassandraServerManagementException {
        if (information == null) {
            throw new CassandraServerManagementException("The keyspace information is null", log);
        }
        validateKeyspace(information.getName());
    }

    private void validateColumnInformation(ColumnInformation information)
            throws CassandraServerManagementException {
        if (information == null) {
            throw new CassandraServerManagementException("The column information is null", log);
        }
        String name = information.getName();
        if (name == null || "".equals(name.trim())) {
            throw new CassandraServerManagementException("The column name is null", log);
        }
    }

    private void validateKeyspace(String keyspaceName) throws CassandraServerManagementException {
        if (keyspaceName == null || "".equals(keyspaceName.trim())) {
            throw new CassandraServerManagementException("The keyspace name is empty or null", log);
        }
    }

    private void validateCF(String columnFamilyName) throws CassandraServerManagementException {
        if (columnFamilyName == null || "".equals(columnFamilyName.trim())) {
            throw new CassandraServerManagementException("The column family name is empty or null", log);
        }
    }

    private KeyspaceDefinition getKeyspaceDefinition(String keyspace)
            throws CassandraServerManagementException {
        validateKeyspace(keyspace);
        Cluster cluster = getCluster(null);
        KeyspaceDefinition keyspaceDefinition = cluster.describeKeyspace(keyspace.trim());
        if (keyspaceDefinition == null) {
            throw new CassandraServerManagementException("Cannot find a keyspace for : " + keyspace, log);
        }
        return keyspaceDefinition;
    }

    private ColumnFamilyInformation createColumnFamilyInformation(ColumnFamilyDefinition definition)
            throws CassandraServerManagementException {
        ColumnFamilyInformation information =
                new ColumnFamilyInformation(definition.getKeyspaceName(), definition.getName());
        information.setId(definition.getId());
        information.setComment(definition.getComment());
        information.setComparatorType(definition.getComparatorType().getClassName());
        information.setKeyCacheSize(definition.getKeyCacheSize());
        int gcGrace = definition.getGcGraceSeconds();
        if (gcGrace > 0) {
            information.setGcGraceSeconds(gcGrace);
        }
        int maxThreshold = definition.getMaxCompactionThreshold();
        if (maxThreshold > 0) {
            information.setMaxCompactionThreshold(maxThreshold);
        }
        int minThreshold = definition.getMinCompactionThreshold();
        if (maxThreshold > 0) {
            information.setMinCompactionThreshold(minThreshold);
        }
        information.setReadRepairChance(definition.getReadRepairChance());
        information.setRowCacheSavePeriodInSeconds(definition.getRowCacheSavePeriodInSeconds());
        information.setType(definition.getColumnType().getValue());
        information.setRowCacheSize(definition.getRowCacheSize());
        //return null with 1.0.2 cassandra
     //   information.setSubComparatorType(definition.getSubComparatorType().getClassName());
        information.setDefaultValidationClass(definition.getDefaultValidationClass());

        //TODO change hector to get a columns of a CF on demand
        List<ColumnDefinition> columnDefinitions = definition.getColumnMetadata();
        ColumnInformation[] columnInformations = new ColumnInformation[columnDefinitions.size()];
        int index = 0;
        for (ColumnDefinition column : columnDefinitions) {
            if (column == null) {
                throw new CassandraServerManagementException("Column cannot be null", log);
            }

            ByteBuffer byteBuffer = column.getName();
            if (byteBuffer == null) {
                throw new CassandraServerManagementException("Column name cannot be null", log);
            }

            byte[] byteArray = new byte[byteBuffer.remaining()];   //TODO best way to do this
            byteBuffer.get(byteArray);
            String name = new String(byteArray);
            if (name.isEmpty()) {
                throw new CassandraServerManagementException("Column name cannot be empty", log);
            }

            ColumnInformation columnInformation = new ColumnInformation();
            columnInformation.setName(name);
            columnInformation.setIndexName(column.getIndexName());
            columnInformation.setValidationClass(column.getValidationClass());
            ColumnIndexType columnIndexType = column.getIndexType();
            if (columnIndexType != null) {
                columnInformation.setIndexType(columnIndexType.name());
            }
            columnInformations[index] = columnInformation;
            index++;
        }
        information.setColumns(columnInformations);
        return information;
    }

    private String getSharedKey() throws AxisFault {

        String sharedKey = (String) super.getHttpSession().getAttribute(USER_ACCESSKEY_ATTR_NAME);
        if (sharedKey == null) {
            try {
                synchronized (CassandraKeyspaceAdmin.class) {
                    sharedKey =
                            (String) super.getHttpSession()
                                    .getAttribute(USER_ACCESSKEY_ATTR_NAME);
                    if (sharedKey == null) {

                        OMElement cassandraAuthConfig = loadCassandraAuthConfigXML();
                        String epr = null;
                        OMElement serverEPR = cassandraAuthConfig.getFirstChildWithName(new QName("EPR"));
                          if (serverEPR != null) {
                            String url = serverEPR.getText();
                            if (url != null && !"".equals(url.trim())) {
                                epr = url;
                            }
                        }

                        String username = null;
                        OMElement cassandraUser = cassandraAuthConfig.getFirstChildWithName(new QName("User"));
                        if (cassandraUser != null) {
                            String user = cassandraUser.getText();
                            if (user != null && !"".equals(user.trim())) {
                                username = user;
                            }
                        }

                        String password = null;
                        OMElement cassandraPasswd = cassandraAuthConfig.getFirstChildWithName(new QName("Password"));
                        if (cassandraPasswd != null) {
                            String passwd = cassandraPasswd.getText();
                            if (passwd != null && !"".equals(passwd.trim())) {
                                password = passwd;
                            }
                        }
                        String targetUser = (String) super.getHttpSession().
                                getAttribute(ServerConstants.USER_LOGGED_IN);
                        String targetDomain = (String) super.getTenantDomain();
                        if (targetDomain != null) {
                            targetUser = targetUser + "@" + targetDomain;
                        }
                        sharedKey = UUID.randomUUID().toString();
                        super.getHttpSession().setAttribute(USER_ACCESSKEY_ATTR_NAME, sharedKey);
                        OMElement payload = getPayload(username, password, targetUser, sharedKey);
                        ServiceClient serviceClient = new ServiceClient(CassandraAdminDSComponent.getConfigCtxService()
                                                                                .getClientConfigContext(), null);
                        Options options = new Options();
                        options.setAction("urn:injectAccessKey");
                        options.setProperty(Constants.Configuration.TRANSPORT_URL, epr);
                        serviceClient.setOptions(options);
                        serviceClient.sendRobust(payload);
                        serviceClient.cleanupTransport();
                    }
                }
            } catch (AxisFault e) {
                sharedKey = null;
                super.getHttpSession().removeAttribute(USER_ACCESSKEY_ATTR_NAME);
                log.error(e.getMessage(), e);
                throw e;
            }
        }
        return sharedKey;
    }

    public static OMElement getPayload(String username, String password, String targetUser,
                                       String accessKey) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace("http://sharedkey.cassandra.carbon.wso2.org", "m0");

        OMElement getKey = factory.createOMElement("giveMeAccessKey", ns);

        OMElement usernameElem = factory.createOMElement("username", ns);
        OMElement passwordElem = factory.createOMElement("password", ns);
        OMElement targetUserElem = factory.createOMElement("targetUser", ns);
        OMElement targetAccessKeyElem = factory.createOMElement("accessKey", ns);

        usernameElem.setText(username);
        passwordElem.setText(password);
        targetUserElem.setText(targetUser);
        targetAccessKeyElem.setText(accessKey);

        getKey.addChild(usernameElem);
        getKey.addChild(passwordElem);
        getKey.addChild(targetUserElem);
        getKey.addChild(targetAccessKeyElem);

        return getKey;
    }

    /**
     * Read cassandra-auth.xml
     *
     * @return
     */
    private OMElement loadCassandraAuthConfigXML() {
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        String path = carbonHome + File.separator + CASSANDRA_AUTH_CONF;
        BufferedInputStream inputStream = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                log.info("There is no " + CASSANDRA_AUTH_CONF + ". Using the default configuration");
                inputStream = new BufferedInputStream(
                        new ByteArrayInputStream("<Cassandra/>".getBytes()));
            } else {
                inputStream = new BufferedInputStream(new FileInputStream(file));
            }
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            return builder.getDocumentElement();
        } catch (FileNotFoundException e) {
            log.error(CASSANDRA_AUTH_CONF + "cannot be found in the path : " + path, e);
        } catch (XMLStreamException e) {
            log.error("Invalid XML for " + CASSANDRA_AUTH_CONF + " located in " +
                      "the path : " + path, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ingored) {
            }
        }
        return null;
    }

}
