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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.analytics.hive.HiveConstants;
import org.wso2.carbon.analytics.hive.ServiceHolder;
import org.wso2.carbon.analytics.hive.conf.HiveConnectionManager;
import org.wso2.carbon.analytics.hive.dto.QueryResult;
import org.wso2.carbon.analytics.hive.dto.QueryResultRow;
import org.wso2.carbon.analytics.hive.exception.HiveExecutionException;
import org.wso2.carbon.analytics.hive.service.HiveExecutorService;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.ndatasource.core.DataSourceMetaInfo;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.ndatasource.core.utils.DataSourceUtils;
import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.ndatasource.rdbms.RDBMSDataSourceReader;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HiveExecutorServiceImpl implements HiveExecutorService {

    private static final Log log = LogFactory.getLog(HiveExecutorServiceImpl.class);

    /**
     * @param script
     * @return The Resultset of all executed queries in the script
     * @throws HiveExecutionException
     */
    public QueryResult[] execute(String script) throws HiveExecutionException {
        if (script != null) {
            /*HiveConnectionManager confManager = HiveConnectionManager.getInstance();

            if (!initialized) {
                initialize(confManager.getConfValue(HiveConstants.HIVE_DRIVER_KEY));
            }*/

            Connection con = null;
            try {
                CarbonDataSource dataSource = ServiceHolder.getCarbonDataSourceService().
                        getDataSource(HiveConstants.DEFAULT_HIVE_DATASOURCE);

                if (dataSource == null) {
                    createDataSource();
                    dataSource = ServiceHolder.getCarbonDataSourceService().
                            getDataSource(HiveConstants.DEFAULT_HIVE_DATASOURCE);

                    if (dataSource != null) {
                        con = ((DataSource) dataSource.getDSObject()).getConnection();
                    }
                } else {

                    Element element = (Element) dataSource.getDSMInfo().getDefinition().
                            getDsXMLConfiguration();
                    RDBMSConfiguration rdbmsConfiguration = RDBMSDataSourceReader.loadConfig(
                            DataSourceUtils.elementToString(element));

                    // Remove jdbc: part and append http: so that URL will recognize it
                    String urlStr = "http:" + rdbmsConfiguration.getUrl().substring(10);
                    
                    URL url = new URL(urlStr);

                    int port = url.getPort();

                    int hiveServerPort =
                            org.wso2.carbon.analytics.hive.Utils.HIVE_SERVER_DEFAULT_PORT +
                            org.wso2.carbon.analytics.hive.Utils.getPortOffset();

                    if (port != hiveServerPort) { // Handle port change after a restart
                        createDataSource();

                        dataSource = ServiceHolder.getCarbonDataSourceService().
                                getDataSource(HiveConstants.DEFAULT_HIVE_DATASOURCE);
                    }

                    if (dataSource != null) {
                        con = ((DataSource) dataSource.getDSObject()).getConnection();
                    }
                }
            } catch (DataSourceException e) {
                log.error("Error while connecting to Hive service..", e);
                throw new HiveExecutionException("Error while connecting to Hive service..", e);
            } catch (IOException e) {
                log.error("Error while connecting to Hive service..", e);
                throw new HiveExecutionException("Error while connecting to Hive service..", e);
            } catch (SAXException e) {
                log.error("Error while connecting to Hive service..", e);
                throw new HiveExecutionException("Error while connecting to Hive service..", e);
            } catch (ParserConfigurationException e) {
                log.error("Error while connecting to Hive service..", e);
                throw new HiveExecutionException("Error while connecting to Hive service..", e);
            } catch (SQLException e) {
                log.error("Error while connecting to Hive service..", e);
                throw new HiveExecutionException("Error while connecting to Hive service..", e);
            }

            // TODO : create handle exception method


            try {
                Statement stmt = con.createStatement();

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

                List<QueryResult> queryResults = new ArrayList<QueryResult>();

                /* When we call executeQuery, execution start in separate thread (started by thrift thread pool),
                   therefore we can't get tenant ID from that thread. So we are appending the tenant ID to each query
                   in order to get it from hive side.
                 */
                int tenantId = CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId();

                for (String cmdLine : cmdLines) {

                    String trimmedCmdLine = cmdLine.trim();
                    trimmedCmdLine = trimmedCmdLine.replaceAll(";", "");
                    trimmedCmdLine = trimmedCmdLine.replaceAll("%%", ";");
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
                        queryResults.add(queryResult);
                    }

                }

                return queryResults.toArray(new QueryResult[]{});


            } catch (SQLException e) {
                throw new HiveExecutionException("Error while executing Hive script.\n" + e.getMessage(), e);
            } finally {
                if (null != con) {
                    try {
                        con.close();
                    } catch (SQLException e) {
                    }
                }
            }

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
            HiveConnectionManager connectionManager = HiveConnectionManager.getInstance();
            connectionManager.saveConfiguration(driverName, url, username, password);
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

    private void createDataSource()
            throws DataSourceException, IOException, SAXException, ParserConfigurationException {

        DataSourceService dataSourceService = ServiceHolder.getCarbonDataSourceService();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document = builder.parse(new InputSource(new StringReader(
                HiveConstants.DEFAULT_HIVE_DATASOURCE_CONFIGURATION)));

        Element configElement = document.getDocumentElement();

        DataSourceMetaInfo.DataSourceDefinition dsDef = new DataSourceMetaInfo.
                DataSourceDefinition();
        dsDef.setDsXMLConfiguration(configElement);
        dsDef.setType("RDBMS");

        DataSourceMetaInfo metaInfo = new DataSourceMetaInfo();
        metaInfo.setName(HiveConstants.DEFAULT_HIVE_DATASOURCE);
        metaInfo.setDefinition(dsDef);
        dataSourceService.addDataSource(metaInfo);
    }

}
