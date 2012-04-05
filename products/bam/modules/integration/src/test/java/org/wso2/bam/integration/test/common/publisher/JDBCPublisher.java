/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.bam.integration.test.common.publisher;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.bam.integration.test.common.events.EventException;
import org.wso2.bam.integration.test.common.statistics.StatisticsData;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class JDBCPublisher implements DataPublisher {

    private static final Log log = LogFactory.getLog(JDBCPublisher.class);

    private static final String EVENTING_SERVER = "EventingServer";
    private static final int SERVICE_TYPE = 1;

    private static final int DEDUCT_AMOUNT = -3;
    private static final String SQL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private DataSource dataSource;
    private boolean inBatch = false;
    private Calendar batchTimestamp;
    private Connection connection;

    public JDBCPublisher(Connection connection) {
        this.connection = connection;
    }

    public void publishServiceEvent(StatisticsData event, BackDate backdate)
            throws EventException {
        int serverId = -1;
        int serviceId = -1;
        int operationId;
        if (event.getServerName() != null) {
            try {

                if (!isServerExisting(event, connection)) {
                    serverId = addServer(event, connection);
                } else {
                    serverId = getServerId(event, connection);
                    log.info("Server already exists. Skip adding server..");
                }

            } catch (Exception e) {
                throw new EventException("Database error while adding server..", e);
            }
        } else {
            throw new EventException("Event is malformed. Must contain server name..");
        }

        if (event.getServiceName() != null) {
            try {

                if (!isServiceExisting(serverId, event, connection)) {
                    serviceId = addService(serverId, event, connection);
                } else {
                    serviceId = getServiceId(serverId, event, connection);
                    log.info("Service already exists. Skip adding service..");
                }

            } catch (Exception e) {
                throw new EventException("Database error while persisting events..", e);
            }
        } else {   // Publish server data event
            try {
                if (serverId != -1) {
                    addServerData(serverId, event, backdate, connection);
                }
            } catch (Exception e) {
                throw new EventException("Database error while persisting events....");
            }
        }

        if (event.getOperationName() != null) { // Publish operation data event
            try {
                if (!isOperationExisting(serviceId, event, connection)) {
                    operationId = addOperation(serviceId, event, connection);
                } else {
                    operationId = getOperationId(serviceId, event, connection);
                    log.info("Operation already exists. Skip adding operation..");
                }

                addOperationData(operationId, event, backdate, connection);

            } catch (Exception e) {
                throw new EventException("Database error while persisting events..", e);
            }
        } else {  // Publish service data event
            try {
                if (serviceId != -1) {
                    addServiceData(serviceId, event, backdate, connection);
                }
            } catch (Exception e) {
                throw new EventException("Database error while persisting events..");
            }
        }
    }

    public void publishMediationEvents(MediationEvent type, String baseFile, int evenCount) {

    }

    public void publishActivityEvents(ActivityEvent type, String baseFile, int eventCount) {

    }

    public void beginBatch() {
        this.inBatch = true;
    }

    public void endBatch() {
        this.inBatch = false;
        this.batchTimestamp = null;
    }

    /**
     * ************************ Server related db methods *******************************
     */

    // Check whether server is already present in the database
    private boolean isServerExisting(StatisticsData event, Connection connection) {

        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("BAM_URL", event.getServerName());
        queryParams.put("BAM_TENENT_ID", Integer.toString(event.getTenantID()));
        queryParams.put("BAM_TYPE", EVENTING_SERVER);
        queryParams.put("BAM_CATEGORY", Integer.toString(SERVICE_TYPE));

        List<String> columns = new ArrayList<String>();
        columns.add("BAM_SERVER_ID");

        String select = QueryTemplate.getSelectString("BAM_SERVER", columns, queryParams, false);
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            ResultSet selectResults = stmt.executeQuery(select);

            int id = -1;
            while (selectResults.next()) {
                id = selectResults.getInt(1);
            }

            selectResults.close();

            if (id == -1) {
                return false;
            }

            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    private int addServer(StatisticsData event, Connection connection) throws SQLException {
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("BAM_URL", event.getServerName());
        queryParams.put("BAM_TENENT_ID", Integer.toString(event.getTenantID()));
        queryParams.put("BAM_TYPE", EVENTING_SERVER);
        queryParams.put("BAM_CATEGORY", Integer.toString(SERVICE_TYPE));
        queryParams.put("USERNAME", "admin");
        queryParams.put("PASSWORD", "admin");

        String insert = QueryTemplate.getInsertString("BAM_SERVER", queryParams);

        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        stmt.executeUpdate(insert, Statement.RETURN_GENERATED_KEYS);
        ResultSet insertResults = stmt.getGeneratedKeys();

        int serverId = -1;
        if (insertResults.next()) {
            serverId = insertResults.getInt(1);
        }

        insertResults.close();

        return serverId;

    }

    private int getServerId(StatisticsData event, Connection connection) throws SQLException {
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("BAM_URL", event.getServerName());
        queryParams.put("BAM_TENENT_ID", Integer.toString(event.getTenantID()));
        queryParams.put("BAM_TYPE", EVENTING_SERVER);
        queryParams.put("BAM_CATEGORY", Integer.toString(SERVICE_TYPE));

        List<String> columns = new ArrayList<String>();
        columns.add("BAM_SERVER_ID");

        String select = QueryTemplate.getSelectString("BAM_SERVER", columns, queryParams, false);
        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        ResultSet selectResults = stmt.executeQuery(select);

        int serverId = -1;
        if (selectResults.next()) {
            serverId = selectResults.getInt(1);
        }

        selectResults.close();
        stmt.close();

        return serverId;

    }

    private void addServerData(int serverId, StatisticsData data, BackDate backdate,
                               Connection connection) throws SQLException {
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("BAM_SERVER_ID", Integer.toString(serverId));
        queryParams.put("BAM_AVG_RES_TIME", Double.toString(data.getAvgResTime()));
        queryParams.put("BAM_MAX_RES_TIME", Long.toString(data.getMaxResTime()));
        queryParams.put("BAM_MIN_RES_TIME", Long.toString(data.getMinResTime()));
        queryParams.put("BAM_CUM_REQ_COUNT", Integer.toString(data.getRequestCount()));
        queryParams.put("BAM_CUM_RES_COUNT", Integer.toString(data.getResponseCount()));
        queryParams.put("BAM_CUM_FAULT_COUNT", Integer.toString(data.getFaultCount()));

        Calendar cal;
        if (inBatch && batchTimestamp != null) {
            cal = this.batchTimestamp;
        } else {
            cal = Calendar.getInstance();
            if (backdate != null) {
                switch (backdate) {
                    case HOUR:
                        cal.add(Calendar.HOUR, DEDUCT_AMOUNT);
                        break;
                    case DAY:
                        cal.add(Calendar.DAY_OF_YEAR, DEDUCT_AMOUNT);
                        break;
                    case MONTH:
                        cal.add(Calendar.MONTH, DEDUCT_AMOUNT);
                        break;
                    case YEAR:
                        cal.add(Calendar.YEAR, DEDUCT_AMOUNT);
                        break;
                }
            }
        }

        if (inBatch && batchTimestamp == null) {
            this.batchTimestamp = cal;
        }

        DateFormat dateFormat = new SimpleDateFormat(SQL_DATE_FORMAT, Locale.ENGLISH);
        String dateString = dateFormat.format(cal.getTime());
        queryParams.put("BAM_TIMESTAMP", dateString);

        String insert = QueryTemplate.getInsertString("BAM_SERVER_DATA", queryParams);

        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        stmt.executeUpdate(insert);

        stmt.close();

    }

    /**
     * *********************** Service related db methods ********************************
     */

    private boolean isServiceExisting(int serverId, StatisticsData event, Connection connection) {

        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("BAM_SERVER_ID", Integer.toString(serverId));
        queryParams.put("BAM_SERVICE_NAME", event.getServiceName());

        List<String> columns = new ArrayList<String>();
        columns.add("BAM_ID");

        String select = QueryTemplate.getSelectString("BAM_SERVICE", columns, queryParams, false);
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            ResultSet selectResults = stmt.executeQuery(select);

            int id = -1;
            while (selectResults.next()) {
                id = selectResults.getInt(1);
            }

            selectResults.close();

            if (id != -1) {
                return true;
            }

            return false;

        } catch (SQLException e) {
            return false;
        }
    }

    private int addService(int serverId, StatisticsData event, Connection connection)
            throws SQLException {
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("BAM_SERVER_ID", Integer.toString(serverId));
        queryParams.put("BAM_SERVICE_NAME", event.getServiceName());

        String insert = QueryTemplate.getInsertString("BAM_SERVICE", queryParams);

        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        stmt.executeUpdate(insert, Statement.RETURN_GENERATED_KEYS);
        ResultSet insertResults = stmt.getGeneratedKeys();

        int serviceId = -1;
        if (insertResults.next()) {
            serviceId = insertResults.getInt(1);
        }

        insertResults.close();

        return serviceId;

    }

    private int getServiceId(int serverId, StatisticsData event, Connection connection)
            throws SQLException {
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("BAM_SERVER_ID", Integer.toString(serverId));
        queryParams.put("BAM_SERVICE_NAME", event.getServiceName());

        List<String> columns = new ArrayList<String>();
        columns.add("BAM_ID");

        String select = QueryTemplate.getSelectString("BAM_SERVICE", columns, queryParams, false);
        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        ResultSet selectResults = stmt.executeQuery(select);

        int serviceId = -1;
        if (selectResults.next()) {
            serviceId = selectResults.getInt(1);
        }

        selectResults.close();
        stmt.close();

        return serviceId;

    }

    private void addServiceData(int serviceId, StatisticsData data, BackDate backdate,
                                Connection connection) throws SQLException {
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("BAM_SERVICE_ID", Integer.toString(serviceId));
        queryParams.put("BAM_AVG_RES_TIME", Double.toString(data.getAvgResTime()));
        queryParams.put("BAM_MAX_RES_TIME", Long.toString(data.getMaxResTime()));
        queryParams.put("BAM_MIN_RES_TIME", Long.toString(data.getMinResTime()));
        queryParams.put("BAM_CUM_REQ_COUNT", Integer.toString(data.getRequestCount()));
        queryParams.put("BAM_CUM_RES_COUNT", Integer.toString(data.getResponseCount()));
        queryParams.put("BAM_CUM_FAULT_COUNT", Integer.toString(data.getFaultCount()));

        Calendar cal;
        if (inBatch && batchTimestamp != null) {
            cal = this.batchTimestamp;
        } else {
            cal = Calendar.getInstance();
            if (backdate != null) {
                switch (backdate) {
                    case HOUR:
                        cal.add(Calendar.HOUR, DEDUCT_AMOUNT);
                        break;
                    case DAY:
                        cal.add(Calendar.DAY_OF_YEAR, DEDUCT_AMOUNT);
                        break;
                    case MONTH:
                        cal.add(Calendar.MONTH, DEDUCT_AMOUNT);
                        break;
                    case YEAR:
                        cal.add(Calendar.YEAR, DEDUCT_AMOUNT);
                        break;
                }
            }
        }

        if (inBatch && batchTimestamp == null) {
            this.batchTimestamp = cal;
        }

        DateFormat dateFormat = new SimpleDateFormat(SQL_DATE_FORMAT, Locale.ENGLISH);
        String dateString = dateFormat.format(cal.getTime());
        queryParams.put("BAM_TIMESTAMP", dateString);

        String insert = QueryTemplate.getInsertString("BAM_SERVICE_DATA", queryParams);

        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        stmt.executeUpdate(insert);

        stmt.close();

    }

    /**
     * ************************** Operation related db methods ********************************
     */

    private boolean isOperationExisting(int serviceId, StatisticsData event,
                                        Connection connection) {

        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("BAM_SERVICE_ID", Integer.toString(serviceId));
        queryParams.put("BAM_OP_NAME", event.getOperationName());

        List<String> columns = new ArrayList<String>();
        columns.add("BAM_ID");

        String select = QueryTemplate.getSelectString("BAM_OPERATION", columns, queryParams, false);
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            ResultSet selectResults = stmt.executeQuery(select);

            int id = -1;
            while (selectResults.next()) {
                id = selectResults.getInt(1);
            }

            selectResults.close();

            if (id != -1) {
                return true;
            }

            return false;

        } catch (SQLException e) {
            return false;
        }
    }

    private int addOperation(int serviceId, StatisticsData event, Connection connection)
            throws SQLException {
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("BAM_SERVICE_ID", Integer.toString(serviceId));
        queryParams.put("BAM_OP_NAME", event.getOperationName());

        String insert = QueryTemplate.getInsertString("BAM_OPERATION", queryParams);

        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        stmt.executeUpdate(insert, Statement.RETURN_GENERATED_KEYS);
        ResultSet insertResults = stmt.getGeneratedKeys();

        int operationId = -1;
        if (insertResults.next()) {
            operationId = insertResults.getInt(1);
        }

        insertResults.close();

        return operationId;

    }

    private int getOperationId(int serviceId, StatisticsData event, Connection connection)
            throws SQLException {
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("BAM_SERVICE_ID", Integer.toString(serviceId));
        queryParams.put("BAM_OP_NAME", event.getOperationName());

        List<String> columns = new ArrayList<String>();
        columns.add("BAM_ID");

        String select = QueryTemplate.getSelectString("BAM_OPERATION", columns, queryParams, false);
        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        ResultSet selectResults = stmt.executeQuery(select);

        int operationId = -1;
        if (selectResults.next()) {
            operationId = selectResults.getInt(1);
        }

        selectResults.close();
        stmt.close();

        return operationId;

    }

    private void addOperationData(int operationId, StatisticsData data, BackDate backdate,
                                  Connection connection) throws SQLException {
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("BAM_OPERATION_ID", Integer.toString(operationId));
        queryParams.put("BAM_AVG_RES_TIME", Double.toString(data.getAvgResTime()));
        queryParams.put("BAM_MAX_RES_TIME", Long.toString(data.getMaxResTime()));
        queryParams.put("BAM_MIN_RES_TIME", Long.toString(data.getMinResTime()));
        queryParams.put("BAM_CUM_REQ_COUNT", Integer.toString(data.getRequestCount()));
        queryParams.put("BAM_CUM_RES_COUNT", Integer.toString(data.getResponseCount()));
        queryParams.put("BAM_CUM_FAULT_COUNT", Integer.toString(data.getFaultCount()));

        Calendar cal;
        if (inBatch && batchTimestamp != null) {
            cal = this.batchTimestamp;
        } else {
            cal = Calendar.getInstance();
            if (backdate != null) {
                switch (backdate) {
                    case HOUR:
                        cal.add(Calendar.HOUR, DEDUCT_AMOUNT);
                        break;
                    case DAY:
                        cal.add(Calendar.DAY_OF_YEAR, DEDUCT_AMOUNT);
                        break;
                    case MONTH:
                        cal.add(Calendar.MONTH, DEDUCT_AMOUNT);
                        break;
                    case YEAR:
                        cal.add(Calendar.YEAR, DEDUCT_AMOUNT);
                        break;
                }
            }
        }

        if (inBatch && batchTimestamp == null) {
            this.batchTimestamp = cal;
        }

        DateFormat dateFormat = new SimpleDateFormat(SQL_DATE_FORMAT, Locale.ENGLISH);
        String dateString = dateFormat.format(cal.getTime());
        queryParams.put("BAM_TIMESTAMP", dateString);

        String insert = QueryTemplate.getInsertString("BAM_OPERATION_DATA", queryParams);

        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        stmt.executeUpdate(insert);

        stmt.close();

    }

    /**
     * ****************** Initialize data source **************************************
     */

    private DataSource initDataSource() {

        Properties props = new Properties();
        try {
            props.load(JDBCPublisher.class.getResourceAsStream("/" + "datasource.properties"));
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
            log.error("Error while loading publisher DB configuration from: datasource.properties", e);
            return null;
        }
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (Exception ignored) {
            log.error("Error closing database connection..", ignored);
        }
    }

}