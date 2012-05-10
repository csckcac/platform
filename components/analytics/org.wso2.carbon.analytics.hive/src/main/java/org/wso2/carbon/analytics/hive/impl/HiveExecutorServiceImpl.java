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
import org.wso2.carbon.analytics.hive.dto.QueryResult;
import org.wso2.carbon.analytics.hive.dto.QueryResultRow;
import org.wso2.carbon.analytics.hive.exception.HiveExecutionException;
import org.wso2.carbon.analytics.hive.service.HiveExecutorService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HiveExecutorServiceImpl implements HiveExecutorService {

    private static final Log log = LogFactory.getLog(HiveExecutorServiceImpl.class);

    public void initialize(String driverName) {
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            log.error("Error during initialization of Hive driver", e);
        }
    }

    /**
     *
     * @param script
     * @param credentials it has the drivername,url, username, password as elements in array to connect to hive
     * @return The Resultset of all executed queries in the script
     * @throws HiveExecutionException
     */
    public QueryResult[] execute(String script, String[] credentials) throws HiveExecutionException {
        if (script != null) {
            Connection con = null;
            initialize(credentials[0]);
            try {
               con = DriverManager.getConnection(credentials[1],
                                                            credentials[2], credentials[3]);
                Statement stmt = con.createStatement();

                String[] cmdLines = script.split(";\\r?\\n|;\\r"); // Tokenize with ;[new-line]

                List<QueryResult> queryResults = new ArrayList<QueryResult>();
                for (String cmdLine : cmdLines) {

                    String trimmedCmdLine = cmdLine.trim();
                     trimmedCmdLine = trimmedCmdLine.replaceAll(";","");
                    if (!"".equals(trimmedCmdLine)) {
                        QueryResult queryResult = new QueryResult();
                        queryResult.setQuery(trimmedCmdLine);

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
                                if(null != resObj)columnValues.add(rs.getObject(i).toString());
                                else columnValues.add("");
                            }

                            resultRow.setColumnValues(columnValues.toArray(new String[]{}));

                            results.add(resultRow);
                        }

                        queryResult.setResultRows(results.toArray(new QueryResultRow[]{}));
                        queryResults.add(queryResult);
                    }

                }

                return queryResults.toArray(new QueryResult[]{});


            } catch (SQLException e) {
                throw new HiveExecutionException("Error while executing Hive script..", e);
            }finally {
               if(null != con) try {
                   con.close();
               } catch (SQLException e) {
               }
            }

        }

        return null;

    }

    @Override
    public boolean authenticateHive(String driverName, String url, String username, String password) {
        Connection con = null;
        try {
            Class.forName(driverName);
            con = DriverManager.getConnection(url, username,password);
        } catch (ClassNotFoundException e) {
            log.error("Error during initialization of Hive driver", e);
        } catch (SQLException e) {
            log.error("URL | Username | password in incorrect. Unable to connect to hive");
        }finally {
            if(null != con){
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
}
