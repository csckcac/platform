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
package org.wso2.carbon.analytics.hive.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.service.Utils;
import org.wso2.carbon.analytics.hive.dto.QueryResult;
import org.wso2.carbon.analytics.hive.dto.QueryResultRow;
import org.wso2.carbon.analytics.hive.dto.ScriptResult;
import org.wso2.carbon.analytics.hive.exception.HiveExecutionException;
import org.wso2.carbon.analytics.hive.service.HiveExecutorService;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HiveExecutorServiceImpl implements HiveExecutorService {

    static String asScript = "CREATE EXTERNAL TABLE IF NOT EXISTS AppServerStats (key STRING, service_name STRING,operation_name STRING,\n" +
                             "\trequest_count INT,response_count INT,fault_count INT, response_time BIGINT,remote_address STRING,\n" +
                             "\tpayload_timestamp BIGINT,host STRING) STORED BY \n" +
                             "\t'org.apache.hadoop.hive.cassandra.CassandraStorageHandler' WITH SERDEPROPERTIES ( \"cassandra.host\" = \"127.0.0.1\",\n" +
                             "\t\"cassandra.port\" = \"9160\",\"cassandra.ks.name\" = \"EVENT_KS\",\n" +
                             "\t\"cassandra.ks.username\" = \"admin\",\"cassandra.ks.password\" = \"admin\",\n" +
                             "\t\"cassandra.cf.name\" = \"org_wso2_bam_stats_dsf\",\n" +
                             "\t\"cassandra.columns.mapping\" = \":key,payload_service_name,payload_operation_name,payload_request_count,payload_response_count,payload_fault_count, payload_response_time,meta_remote_address, payload_timestamp,meta_host\" );\n" +
                             "\n" +
                             "CREATE EXTERNAL TABLE IF NOT EXISTS AppServerStatsPerServer(host STRING, total_request_count INT,total_response_count INT,\n" +
                             "\ttotal_fault_count INT,avg_response_time DOUBLE) STORED BY 'org.wso2.carbon.hadoop.hive.jdbc.storage.JDBCStorageHandler' TBLPROPERTIES ( \n" +
                             "\t'mapred.jdbc.driver.class' = 'com.mysql.jdbc.Driver',\n" +
                             "\t'mapred.jdbc.url' = 'jdbc:mysql://localhost:3306/testdb',\n" +
                             "\t'mapred.jdbc.username' = 'root','mapred.jdbc.password' = 'root',\n" +
                             "\t'hive.jdbc.update.on.duplicate' = 'true',\n" +
                             "\t'hive.jdbc.primary.key.fields' = 'host','hive.jdbc.table.create.query' = 'CREATE TABLE AppServerStatsPerServer ( host VARCHAR(100) NOT NULL PRIMARY KEY,total_request_count  INT,total_response_count INT,total_fault_count INT,avg_response_time DOUBLE)' );\n" +
                             "\n" +
                             "insert overwrite table AppServerStatsPerServer select host, sum(request_count) as total_request_count,sum(response_count) as total_response_count, sum(fault_count) as total_fault_count,avg(response_time) as avg_response_time from AppServerStats group by host;\n" +
                             "\n" +
                             "\n" +
                             "CREATE EXTERNAL TABLE IF NOT EXISTS AppServerStatsPerMonth(host STRING, total_request_count INT,total_response_count INT,\n" +
                             "\ttotal_fault_count INT,avg_response_time DOUBLE, month INT,year INT) \n" +
                             "\tSTORED BY 'org.wso2.carbon.hadoop.hive.jdbc.storage.JDBCStorageHandler' TBLPROPERTIES ( \n" +
                             "\t'mapred.jdbc.driver.class' = 'com.mysql.jdbc.Driver',\n" +
                             "\t'mapred.jdbc.url' = 'jdbc:mysql://localhost:3306/testdb',\n" +
                             "\t'mapred.jdbc.username' = 'root','mapred.jdbc.password' = 'root',\n" +
                             "\t'hive.jdbc.update.on.duplicate' = 'true',\n" +
                             "\t'hive.jdbc.primary.key.fields' = 'host','hive.jdbc.table.create.query' = 'CREATE TABLE AppServerStatsPerServerPerMonth ( host VARCHAR(100) NOT NULL PRIMARY KEY,total_request_count INT,total_response_count INT,\n" +
                             "\ttotal_fault_count INT,avg_response_time DOUBLE, month INT,year INT)' );\n" +
                             "\n" +
                             "insert overwrite table AppServerStatsPerMonth select host, sum(request_count) as total_request_count, sum(response_count) as total_response_count,sum(fault_count) as total_fault_count,avg(response_time) as avg_response_time, month(from_unixtime(payload_timestamp,'yyyy-MM-dd HH:mm:ss.SSS' )) as month, year(from_unixtime(payload_timestamp,'yyyy-MM-dd HH:mm:ss.SSS' )) as year from AppServerStats group by month(from_unixtime(payload_timestamp,'yyyy-MM-dd HH:mm:ss.SSS' )),year(from_unixtime(payload_timestamp,'yyyy-MM-dd HH:mm:ss.SSS' )),host;\n" +
                             "\n" +
                             "\n" +
                             "\n" +
                             "CREATE EXTERNAL TABLE IF NOT EXISTS AppServerStatsPerYear(host STRING, total_request_count INT, total_response_count INT, \n" +
                             "total_fault_count INT,avg_response_time DOUBLE,year INT) STORED BY 'org.wso2.carbon.hadoop.hive.jdbc.storage.JDBCStorageHandler' \n" +
                             "TBLPROPERTIES ( 'mapred.jdbc.driver.class' = 'com.mysql.jdbc.Driver' , \n" +
                             "'mapred.jdbc.url' = 'jdbc:mysql://localhost:3306/testdb' , \n" +
                             "'mapred.jdbc.username' = 'root' , 'mapred.jdbc.password' = 'root' , 'hive.jdbc.update.on.duplicate' = 'true' , \n" +
                             "'hive.jdbc.primary.key.fields' = 'host' , 'hive.jdbc.table.create.query' = 'CREATE TABLE AppServerStatsPerServerPerYear ( host VARCHAR(100) NOT NULL PRIMARY KEY, total_request_count  INT, total_response_count INT, total_fault_count INT, avg_response_time DOUBLE, year INT)' );\n" +
                             "\n" +
                             "insert overwrite table AppServerStatsPerYear select host,sum(request_count) as total_request_count, sum(response_count) as total_response_count,sum(fault_count) as total_fault_count, avg(response_time) as total_response_time, year(from_unixtime(payload_timestamp,'yyyy-MM-dd HH:mm:ss.SSS')) as year from AppServerStats group by year(from_unixtime(payload_timestamp,'yyyy-MM-dd HH:mm:ss.SSS')),host;";


    private static final Log log = LogFactory.getLog(HiveExecutorServiceImpl.class);

    static {
        try {
            Class.forName("org.apache.hadoop.hive.jdbc.HiveDriver");
        } catch (ClassNotFoundException e) {
            log.fatal("Hive JDBC Driver not found in the class path. Hive query execution will" +
                      " fail..", e);
        }
    }

    /**
     * @param script
     * @return The Resultset of all executed queries in the script
     * @throws HiveExecutionException
     */
    public QueryResult[] execute(String script) throws HiveExecutionException {
        if (script != null) {

            ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

            ScriptCallable callable = new ScriptCallable(script);

            Future<ScriptResult> future = singleThreadExecutor.submit(callable);

            ScriptResult result;
            try {
                result = future.get();
            } catch (InterruptedException e) {
                log.error("Query execution interrupted..", e);
                throw new HiveExecutionException("Query execution interrupted..", e);
            } catch (ExecutionException e) {
                log.error("Error during query execution..", e);
                throw new HiveExecutionException("Error during query execution..", e);
            }

            if (result != null) {
                if (result.getErrorMessage() != null) {
                    throw new HiveExecutionException(result.getErrorMessage());
                }

                return result.getQueryResults();

            } else {
                throw new HiveExecutionException("Query returned a NULL result..");
            }

/*            int threadCount = 0;
            try {
                threadCount = Integer.parseInt(script);
            } catch (Exception e) {
                ScriptCallable callable = new ScriptCallable(script);
                ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

                Future<ScriptResult> future = singleThreadExecutor.submit(callable);

                ScriptResult result;
                try {
                    result = future.get();
                } catch (InterruptedException x) {
                    log.error("Query execution interrupted..", x);
                    throw new HiveExecutionException("Query execution interrupted..", x);
                } catch (ExecutionException z) {
                    log.error("Error during query execution..", z);
                    throw new HiveExecutionException("Error during query execution..", z);
                }
            }

            for (int i = 0; i < threadCount; i++) {
                ScriptCallable callable = new ScriptCallable(asScript);
                ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

                singleThreadExecutor.submit(callable);

            }*/

        }

        return null;

    }

    @Override
    public boolean setConnectionParameters(String driverName, String url, String username,
                                           String password) {
        Connection con = null;
        try {
            Class.forName(driverName);
            con = DriverManager.getConnection(url, username, password);
/*            HiveConnectionManager connectionManager = HiveConnectionManager.getInstance();
            connectionManager.saveConfiguration(driverName, url, username, password);*/
        } catch (ClassNotFoundException e) {
            log.error("Error during initialization of Hive driver", e);
        } catch (SQLException e) {
            log.error("URL | Username | password in incorrect. Unable to connect to hive");
        } finally {
            if (null != con) {
                try {
                    con.close();
                } catch (SQLException e) {
                }
                return true;
            } else {
                return false;
            }
        }

    }

    private class ScriptCallable implements Callable<ScriptResult> {

        private String script;

        public ScriptCallable(String script) {
            this.script = script;
        }

        public ScriptResult call() {
            Connection con;
            try {
                con = DriverManager.getConnection("jdbc:hive://", null, null);
            } catch (SQLException e) {
                log.error("Error getting connection..", e);

                ScriptResult result = new ScriptResult();
                result.setErrorMessage("Error getting connection." + e.getMessage());
                return result;
            }

            Statement stmt;
            try {
                stmt = con.createStatement();
            } catch (SQLException e) {
                log.error("Error getting statement..", e);

                ScriptResult result = new ScriptResult();
                result.setErrorMessage("Error getting statement." + e.getMessage());
                return result;
            }

            try {

                Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
                Matcher regexMatcher = regex.matcher(script);
                String formattedScript = "";
                while (regexMatcher.find()) {
                    String temp = "";
                    if (regexMatcher.group(1) != null) {
                        // Add double-quoted string without the quotes
                        temp = regexMatcher.group(1).replaceAll(";", "%%");
                        if (temp.contains("%%")) {
                            temp = temp.replaceAll(" ", "");
                            temp = temp.replaceAll("\n", "");
                        }
                        temp = "\"" + temp + "\"";
                    } else if (regexMatcher.group(2) != null) {
                        // Add single-quoted string without the quotes
                        temp = regexMatcher.group(2).replaceAll(";", "%%");
                        if (temp.contains("%%")) {
                            temp = temp.replaceAll(" ", "");
                            temp = temp.replaceAll("\n", "");
                        }
                        temp = "\'" + temp + "\'";
                    } else {
                        temp = regexMatcher.group();
                    }
                    formattedScript += temp + " ";
                }


                String[] cmdLines = formattedScript.split(";\\r?\\n|;"); // Tokenize with ;[new-line]

                /* When we call executeQuery, execution start in separate thread (started by thrift thread pool),
                   therefore we can't get tenant ID from that thread. So we are appending the tenant ID to each query
                   in order to get it from hive side.
                 */
                int tenantId = CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId();

                ScriptResult result = new ScriptResult();
                for (String cmdLine : cmdLines) {

                    String trimmedCmdLine = cmdLine.trim();
                    trimmedCmdLine = trimmedCmdLine.replaceAll(";", "");
                    trimmedCmdLine = trimmedCmdLine.replaceAll("%%", ";");
                    //Fixing some issues in the hive query due to /n/t
                    trimmedCmdLine = trimmedCmdLine.replaceAll("\n", " ");
                    trimmedCmdLine = trimmedCmdLine.replaceAll("\t", " ");

                    if (!"".equals(trimmedCmdLine)) {
                        QueryResult queryResult = new QueryResult();

                        queryResult.setQuery(trimmedCmdLine);

                        //Append the tenant ID to query
                        trimmedCmdLine += Utils.TENANT_ID_SEPARATOR_CHAR_SEQ + tenantId;

                        ResultSet rs = stmt.executeQuery(trimmedCmdLine);
                        ResultSetMetaData metaData = rs.getMetaData();

                        int columnCount = metaData.getColumnCount();
                        List<String> columnsList = new ArrayList<String>();
                        for (int i = 1; i <= columnCount; i++) {
                            columnsList.add(metaData.getColumnName(i));
                        }

                        queryResult.setColumnNames(columnsList.toArray(new String[]{}));

                        List<QueryResultRow> results = new ArrayList<QueryResultRow>();
                        while (rs.next()) {
                            QueryResultRow resultRow = new QueryResultRow();

                            List<String> columnValues = new ArrayList<String>();
                            for (int i = 1; i <= columnCount; i++) {
                                Object resObj = rs.getObject(i);
                                if (null != resObj) {
                                    columnValues.add(rs.getObject(i).toString());
                                } else {
                                    columnValues.add("");
                                }
                            }

                            resultRow.setColumnValues(columnValues.toArray(new String[]{}));

                            results.add(resultRow);
                        }

                        queryResult.setResultRows(results.toArray(new QueryResultRow[]{}));
                        result.addQueryResult(queryResult);
                    }

                }

                return result;


            } catch (SQLException e) {
                log.error("Error while executing Hive script.\n" + e.getMessage(), e);

                ScriptResult result = new ScriptResult();
                result.setErrorMessage("Error while executing Hive script." + e.getMessage());

                return result;
            } finally {
                if (null != con) {
                    try {
                        con.close();
                    } catch (SQLException e) {
                    }
                }
            }

        }

    }

}
