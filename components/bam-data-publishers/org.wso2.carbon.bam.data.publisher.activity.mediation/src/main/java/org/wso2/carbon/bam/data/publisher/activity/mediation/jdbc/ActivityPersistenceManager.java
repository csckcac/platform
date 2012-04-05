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

package org.wso2.carbon.bam.data.publisher.activity.mediation.jdbc;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.activity.mediation.ActivityPublisherConstants;
import org.wso2.carbon.bam.data.publisher.activity.mediation.ActivityPublisherUtils;
import org.wso2.carbon.bam.data.publisher.activity.mediation.MessageActivity;
import org.wso2.carbon.bam.data.publisher.activity.mediation.config.EventingConfigData;
import org.wso2.carbon.bam.data.publisher.activity.mediation.config.XPathConfigData;
import org.wso2.carbon.utils.CarbonUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ActivityPersistenceManager {

    private static final Log log = LogFactory.getLog(ActivityPersistenceManager.class);

    private DataSource dataSource;

    private int serverId = -1;

    private Map<String, Integer> services = new ConcurrentHashMap<String, Integer>();
    private Map<String, Integer> operations = new ConcurrentHashMap<String, Integer>();
    private SimpleLRUCache<String, Integer> activities = new SimpleLRUCache<String, Integer>(200);

    private volatile Object serviceTable = new Object();
    private volatile Object operationTable = new Object();
    private volatile Object activityTable = new Object();
    private volatile Object messageTable = new Object();
    private volatile Object messageDataTable = new Object();
    private volatile Object propertiesTable = new Object();

    public ActivityPersistenceManager() {
        dataSource = initDataSource();
    }

    public void persistActivity(MessageActivity activity) {
        if (serverId == -1) {
            serverId = getServerId();
            if (serverId == -1) {
                log.error("Unable to find the server ID of the monitored server - Activities " +
                          "will not be saved");
                return;
            }
        }

        try {
            int serviceId = getServiceId(activity.getService());
            int operationId = getOperationId(activity.getOperation(), serviceId);
            int activityId = getActivityId(activity.getActivityId(), activity.getActivityName());
            int messageId = saveMessage(activity, operationId, activityId);
            saveMessageProperties(messageId, activityId, activity);

            EventingConfigData data = ActivityPublisherUtils.getActivityPublisherAdmin().
                    getEventingConfigData();
            if (data != null && data.getEnableMessageDumping().
                    equals(ActivityPublisherConstants.MESSAGE_DUMPING_ON)) {
                saveMessageData(messageId, activityId, activity);
            }

            if (data != null && data.messageLookupEnabled()) {

                XPathConfigData[] xpathConfigs = null;
                try {
                    xpathConfigs = ActivityPublisherUtils.getActivityPublisherAdmin().getXPathData();
                } catch (Exception ignored) {

                }

                if (xpathConfigs != null) {
                    for (XPathConfigData xpathConfig : xpathConfigs) {
                        saveXpathConfigData(xpathConfig);
                    }
                }

                saveXPathProperties(messageId, activityId, activity);
            }

        } catch (SQLException e) {
            log.error("Error while saving the activity to the database", e);
        }
    }

    private int getServiceId(String serviceName) throws SQLException {
        if (services.containsKey(serviceName)) {
            return services.get(serviceName);
        } else {
            String select = "SELECT BAM_ID FROM BAM_SERVICE WHERE BAM_SERVICE_NAME='" + serviceName +
                            "' AND BAM_SERVER_ID=" + serverId;
            String insert = "INSERT INTO BAM_SERVICE (BAM_SERVER_ID, BAM_SERVICE_NAME) VALUES " +
                            "(" + serverId + ", '" + serviceName + "')";
            return getIdFromDatabase(select, insert, serviceTable, serviceName, services);
        }

    }

    private int getOperationId(String operationName, int serviceId) throws SQLException {
        String key = serviceId + "-" + operationName;
        if (operations.containsKey(key)) {
            return operations.get(key);
        } else {
            String select = "SELECT BAM_ID FROM BAM_OPERATION WHERE BAM_OP_NAME='" + operationName +
                            "' AND BAM_SERVICE_ID=" + serviceId;
            String insert = "INSERT INTO BAM_OPERATION (BAM_SERVICE_ID, BAM_OP_NAME) VALUES " +
                            "(" + serviceId + ", '" + operationName + "')";
            return getIdFromDatabase(select, insert, operationTable, key, operations);
        }
    }

    private int getActivityId(String activityKey, String activityName) throws SQLException {
        Integer activityId = activities.get(activityKey);
        if (activityId != null) {
            return activities.get(activityKey);
        } else {
            String select = "SELECT BAM_ID FROM BAM_ACTIVITY WHERE BAM_USER_DEFINED_ID=" +
                            "'" + activityKey + "'";
            String insert = "INSERT INTO BAM_ACTIVITY (BAM_NAME, BAM_USER_DEFINED_ID) VALUES ('" +
                            (activityName != null ? activityName : "") + "', '" + activityKey + "')";
            return getIdFromDatabase(select, insert, activityTable, activityKey, activities);
        }
    }

    private XPathConfigData getXpathConfigData(String xpathKey, int serverId) throws SQLException {
        String select = "SELECT BAM_ID,BAM_ALIAS,BAM_XPATH FROM BAM_XPATH WHERE BAM_NAME =" +
                        "'" + xpathKey + "' AND BAM_SERVER_ID =" + "'" + serverId + "'";

        Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet selectResults = stmt.executeQuery(select);

        int id = -1;
        if (selectResults.next()) {
            id = selectResults.getInt(1);
        }

        if (id != -1) {
            XPathConfigData data = new XPathConfigData();
            data.setKey(xpathKey);
            data.setXpath(selectResults.getString(3));
            data.setAlias(selectResults.getString(2));
            data.setId(id);
            selectResults.close();

            select = "SELECT BAM_ID, BAM_PREFIX, BAM_URI FROM BAM_NAMESPACE WHERE BAM_XPATH_ID =" +
                     "'" + id + "'";
            selectResults = stmt.executeQuery(select);
            List<String> namespaces = new ArrayList<String>();

            while (selectResults.next()) {
                namespaces.add(selectResults.getString(2) + "@" + selectResults.getString(3));
            }

            if (namespaces.size() > 0) {
                data.setNameSpaces(namespaces.toArray(new String[namespaces.size()]));
            }

            return data;
        }

        selectResults.close();
        stmt.close();
        conn.close();

        return null;

    }

    /**
     * This method assumes that multiple activities will not contain the same Message ID and
     * Activity ID. This situation can occur if the activity mediator is redundantly engaged
     * in the same sequence. So the ESB configuration should be written to avoid that scenario.
     * In case of failure scenarios, the activity mediator will change the message ID of the
     * requests as a defense to this problem. DLC replay logic also applies a new message ID
     * on each replayed message.
     *
     * @param act         MessageAcitivty containing message ID and other metadata
     * @param operationId Operation ID from the database
     * @param activityId  Activity ID from the database
     * @return the auto generated message ID from the database
     * @throws java.sql.SQLException on error
     */
    private int saveMessage(MessageActivity act, int operationId, int activityId)
            throws SQLException {
        Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement();
        String sql = "INSERT INTO BAM_MESSAGE (BAM_MSG_ID, BAM_OP_ID, BAM_ACTIVITY_ID, " +
                     "BAM_TIMESTAMP, BAM_IP_ADDRESS, BAM_TARGET_IP_ADDRESS) VALUES ('" + act.getMessageId() + "', " +
                     operationId + ", " + activityId + ", '" + act.getTimestamp().getBAMTimestamp() +
                     "', '" + (act.getSenderHost() != null ? act.getSenderHost() : "") + "', " +
                     "'" + (act.getReceiverHost() != null ? act.getReceiverHost() : "") + "')";
        synchronized (messageTable) {
            stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        }
        ResultSet rs = stmt.getGeneratedKeys();
        int id = -1;
        while (rs.next()) {
            id = rs.getInt(1);
        }

        rs.close();
        stmt.close();
        conn.close();
        return id;
    }

    private void saveMessageData(int messageId, int activityId,
                                 MessageActivity act) throws SQLException {

        String direction;
        if (act.getDirection() == ActivityPublisherConstants.DIRECTION_IN) {
            direction = "Request";
        } else {
            direction = "Response";
        }

        String status;
        String appFailure = act.getProperty(ActivityPublisherConstants.PROP_APPLICATION_FAILURE);
        String techFailure = act.getProperty(ActivityPublisherConstants.PROP_TECHNICAL_FAILURE);
        if ((appFailure != null && !"".equals(appFailure)) ||
            (techFailure != null && !"".equals(techFailure))) {
            status = "Fail";
        } else {
            status = "Success";
        }

        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO BAM_MESSAGE_DATA (BAM_MESSAGE_ID, BAM_ACTIVITY_ID, BAM_TIMESTAMP, " +
                                                       "BAM_DIRECTION, BAM_MESSAGE, BAM_STATUS) VALUES (?,?,?,?,?,?)");
        stmt.setInt(1, messageId);
        stmt.setInt(2, activityId);
        stmt.setString(3, act.getTimestamp().getBAMTimestamp());
        stmt.setString(4, direction);
        stmt.setString(5, act.getPayload());
        stmt.setString(6, status);
        synchronized (messageDataTable) {
            stmt.execute();
        }
        stmt.close();
        conn.close();
    }

    private void saveXpathConfigData(XPathConfigData xpathConfig) throws SQLException {
        XPathConfigData data = getXpathConfigData(xpathConfig.getKey(), serverId);

        if (data != null && xpathConfig.equals(data)) {
            return;
        }

        Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement();

        if (data == null) {
            String insertXpath = "INSERT INTO BAM_XPATH(BAM_ALIAS,BAM_NAME,BAM_XPATH,BAM_SERVER_ID) VALUES ('" +
                                 xpathConfig.getAlias() + "','" + xpathConfig.getKey() + "','" +
                                 xpathConfig.getXpath() + "','" + serverId + "')";
            stmt.executeUpdate(insertXpath, Statement.RETURN_GENERATED_KEYS);

            int id = -1;
            ResultSet insertResults = stmt.getGeneratedKeys();
            if (insertResults.next()) {
                id = insertResults.getInt(1);
            }

            insertResults.close();

            saveNamespaceData(xpathConfig, id);

        } else if (!xpathConfig.equals(data)) {

            String updateXpath = "UPDATE BAM_XPATH SET BAM_ALIAS = '" + xpathConfig.getAlias() +
                                 "', BAM_NAME = '" + xpathConfig.getKey() + "', " + "BAM_XPATH = '" +
                                 xpathConfig.getXpath() + "'," + " BAM_SERVER_ID = '" + serverId +
                                 "' WHERE BAM_ID = '" + data.getId() + "'";

            stmt.executeUpdate(updateXpath, Statement.RETURN_GENERATED_KEYS);

            int id = -1;
            ResultSet updateResults = stmt.getGeneratedKeys();
            if (updateResults.next()) {
                id = updateResults.getInt(1);
            }

            updateResults.close();

            String deleteNS = "DELETE FROM BAM_NAMESPACE WHERE BAM_XPATH_ID ='" + id + "'";
            stmt.executeUpdate(deleteNS);

            saveNamespaceData(xpathConfig, id);

        }

        stmt.close();
        conn.close();

    }

    private void saveNamespaceData(XPathConfigData xpathConfig, int xpathId) throws SQLException {

        Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement();

        if (xpathConfig.getNameSpaces() != null) {
            for (String ns : xpathConfig.getNameSpaces()) {
                String[] tokens = ns.split("@");

                if (tokens != null && tokens.length == 2) {
                    String prefix = tokens[0];
                    String namespace = tokens[1];
                    String insertNS = "INSERT INTO BAM_NAMESPACE(BAM_XPATH_ID,BAM_PREFIX, BAM_URI)" +
                                      " VALUES('" + xpathId + "','" + prefix + "','" + namespace + "')";
                    stmt.executeUpdate(insertNS);
                }
            }
        }

        stmt.close();
        conn.close();

    }

    private void saveMessageProperties(int messageId, int activityId,
                                       MessageActivity act) throws SQLException {
        String sql = "INSERT INTO BAM_MESSAGE_PROPERTIES (BAM_MESSAGE_ID, BAM_ACTIVITY_ID, BAM_KEY, " +
                     "BAM_VALUE) VALUES (?,?,?,?)";

        Set<String> keys = act.getPropertyKeys();
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        for (String k : keys) {
            stmt.setInt(1, messageId);
            stmt.setInt(2, activityId);
            stmt.setString(3, k);
            stmt.setString(4, act.getProperty(k));
            stmt.addBatch();
        }

        synchronized (propertiesTable) {
            stmt.executeBatch();
        }
        stmt.close();
        conn.close();
    }

    private void saveXPathProperties(int messageId, int activityId, MessageActivity activity)
            throws SQLException {

        String sql = "INSERT INTO BAM_MESSAGE_PROPERTIES (BAM_MESSAGE_ID, BAM_ACTIVITY_ID, BAM_KEY, " +
                     "BAM_VALUE) VALUES (?,?,?,?)";

        Set<XPathConfigData> xpathConfigs = activity.getXpathKeys();
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        for (XPathConfigData xpathConfig : xpathConfigs) {
            stmt.setInt(1, messageId);
            stmt.setInt(2, activityId);
            stmt.setString(3, xpathConfig.getKey());
            stmt.setString(4, activity.getXpath(xpathConfig));
            stmt.addBatch();
        }

        synchronized (propertiesTable) {
            stmt.executeBatch();
        }

        stmt.close();
        conn.close();

    }

    private int getIdFromDatabase(String selectQuery, String insertQuery, Object tableLock,
                                  String cacheKey, Map<String, Integer> cache) throws SQLException {

        synchronized (tableLock) {
            if (cache.containsKey(cacheKey)) {
                return cache.get(cacheKey);
            }

            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet selectResults = stmt.executeQuery(selectQuery);
            int id = -1;
            while (selectResults.next()) {
                id = selectResults.getInt(1);
            }
            selectResults.close();

            if (id == -1) {
                stmt.executeUpdate(insertQuery, Statement.RETURN_GENERATED_KEYS);
                ResultSet insertResults = stmt.getGeneratedKeys();
                while (insertResults.next()) {
                    id = insertResults.getInt(1);
                    cache.put(cacheKey, id);
                    break;
                }
                insertResults.close();
            }

            stmt.close();
            conn.close();
            return id;
        }
    }

    private int getServerId() {
        int serverId = -1;
        String url = System.getProperty("bam.server.url");
        if (url == null) {
            url = ActivityPublisherUtils.getServerName();
        }

        if (log.isDebugEnabled()) {
            log.debug("Determining the server ID for the URL: " + url);
        }

        try {
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT BAM_SERVER_ID FROM BAM_SERVER WHERE BAM_URL='" +
                                             url + "'");
            while (rs.next()) {
                serverId = rs.getInt("BAM_SERVER_ID");
                break;
            }
            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            log.error("Error while retreiving the server ID from BAM database", e);
        }
        return serverId;
    }

    private DataSource initDataSource() {
        String path = CarbonUtils.getCarbonConfigDirPath() + File.separator + "bam-publisher.properties";
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(path));
            BasicDataSource dataSource = new BasicDataSource();
            dataSource.setDriverClassName(props.getProperty("driver"));
            dataSource.setUrl(props.getProperty("url"));
            dataSource.setUsername(props.getProperty("username"));
            dataSource.setPassword(props.getProperty("password"));

            String validationQuery = props.getProperty("validationQuery");
            if (validationQuery != null) {
                dataSource.setValidationQuery(validationQuery);
            }

            String maxActive = props.getProperty("maxActive");
            if (maxActive != null) {
                dataSource.setMaxActive(Integer.parseInt(maxActive));
            }

            String initialSize = props.getProperty("initialSize");
            if (initialSize != null) {
                dataSource.setInitialSize(Integer.parseInt(initialSize));
            }

            String maxIdle = props.getProperty("maxIdle");
            if (maxIdle != null) {
                dataSource.setMaxIdle(Integer.parseInt(maxIdle));
            }

            log.info("Created new data source to: " + dataSource.getUrl());
            return dataSource;

        } catch (IOException e) {
            log.error("Error while loading publisher DB configuration from: " + path, e);
            return null;
        }
    }

}