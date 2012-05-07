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
import org.wso2.carbon.analytics.hive.HiveConstants;
import org.wso2.carbon.analytics.hive.dto.QueryResult;
import org.wso2.carbon.analytics.hive.dto.QueryResultRow;
import org.wso2.carbon.analytics.hive.exception.HiveExecutionException;
import org.wso2.carbon.analytics.hive.service.HiveExecutorService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class HiveExecutorServiceImpl implements HiveExecutorService {

    private static final Log log = LogFactory.getLog(HiveExecutorServiceImpl.class);

    public HiveExecutorServiceImpl() {
        initialize();
    }

    public void initialize() {
        try {
            Class.forName(HiveConstants.HIVE_DRIVER);
        } catch (ClassNotFoundException e) {
            log.error("Error during initialization of Hive driver", e);
        }
    }

    public QueryResult[] execute(String script) throws HiveExecutionException {
        if (script != null) {

            try {
                Connection con = DriverManager.getConnection("jdbc:hive://localhost:10000/default",
                                                             "", "");
                Statement stmt = con.createStatement();

                String[] cmdLines = script.split(";\\r?\\n|;\\r"); // Tokenize with ;[new-line]

                List<QueryResult> queryResults = new ArrayList<QueryResult>();
                for (String cmdLine : cmdLines) {

                    String trimmedCmdLine = cmdLine.trim();

                    if (!"".equals(trimmedCmdLine)) {
                        QueryResult queryResult = new QueryResult();
                        queryResult.setQuery(cmdLine);

                        ResultSet rs = stmt.executeQuery(cmdLine);
                        ResultSetMetaData metaData = rs.getMetaData();

                        int columnCount = metaData.getColumnCount();
                        List<String> columnsList = new ArrayList<String>();
                        for (int i = 0; i < columnCount; i++) {
                            columnsList.add(metaData.getColumnName(i));
                        }

                        queryResult.setColumnNames(columnsList.toArray(new String[]{}));

                        List<QueryResultRow> results = new ArrayList<QueryResultRow>();
                        while (rs.next()) {
                            QueryResultRow resultRow = new QueryResultRow();

                            List<String> columnValues = new ArrayList<String>();
                            for (int i = 0; i < columnCount; i++) {
                                columnValues.add(rs.getObject(i).toString());
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
            }

        }

        return null;

    }

}
