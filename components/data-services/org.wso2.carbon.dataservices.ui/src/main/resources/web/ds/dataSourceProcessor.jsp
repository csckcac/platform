<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@page import="org.wso2.carbon.dataservices.common.DBConstants" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Config" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Data" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Property" %>
<%@ page import="org.wso2.carbon.dataservices.ui.beans.Query" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Iterator" %>
<jsp:useBean id="dataService" class="org.wso2.carbon.dataservices.ui.beans.Data" scope="session"/>
<jsp:useBean id="newConfig" class="org.wso2.carbon.dataservices.ui.beans.Config" scope="session"/>
<%!
    private void updateConfiguration(Config config, String propertyName, Object value) {
        if (value instanceof String) {
            String s = value.toString();
            if (s != null && s.trim().length() != 0) {
                config.updateProperty(propertyName, value);
            } else {
                config.removeProperty(propertyName);
            }
        }
    }
%>
<%
    //retrieve form values set in addDataSource.jsp page
    String serviceName = request.getParameter("serviceName");
    String datasourceId = request.getParameter("datasourceId");
    String datasourceType = request.getParameter("datasourceType");
    String selectBox = request.getParameter("selectbox");
    String driverClass = request.getParameter(DBConstants.RDBMS.DRIVER);
    String jdbcUrl = request.getParameter(DBConstants.RDBMS.PROTOCOL);
    String dsUserName = request.getParameter(DBConstants.RDBMS.USER);
    String dsPassword = request.getParameter(DBConstants.RDBMS.PASSWORD);
    String xaDataSourceClass = request.getParameter(DBConstants.RDBMS.XA_DATASOURCE_CLASS);
    String user = request.getParameter("User");
    String url = request.getParameter("URL");
    String password = request.getParameter("Password");
    int propertyCount = 0;
    if (request.getParameter("propertyCount")!=null && !request.getParameter("propertyCount").equals("")){
       propertyCount = Integer.parseInt(request.getParameter("propertyCount")); 
    }
    String xaType = request.getParameter("isXAType");
    String transactionIsolation = request.getParameter(DBConstants.RDBMS.TRANSACTION_ISOLATION);
    String initialSize = request.getParameter(DBConstants.RDBMS.INITIAL_SIZE);
    String maxPool = request.getParameter(DBConstants.RDBMS.MAX_POOL_SIZE);
    String maxIdle = request.getParameter(DBConstants.RDBMS.MAX_IDLE);
    String minPool = request.getParameter(DBConstants.RDBMS.MIN_POOL_SIZE);
    String maxWait = request.getParameter(DBConstants.RDBMS.MAX_WAIT);
    String validationQuery = request.getParameter(DBConstants.RDBMS.VALIDATION_QUERY);
    String testOnBorrow = request.getParameter(DBConstants.RDBMS.TEST_ON_BORROW);
    String testOnReturn = request.getParameter(DBConstants.RDBMS.TEST_ON_RETURN);
    String testWhileIdle = request.getParameter(DBConstants.RDBMS.TEST_WHILE_IDLE);
    String timeBetweenEvictionRunsMillis = request.getParameter(DBConstants.RDBMS.TIME_BETWEEN_EVICTION_RUNS_MILLS);
    String numTestsPerEvictionRun = request.getParameter(DBConstants.RDBMS.NUM_TESTS_PER_EVICTION_RUN);
    String minEvictableIdleTimeMillis = request.getParameter(DBConstants.RDBMS.MIN_EVICTABLE_IDLE_TIME_MILLIS);
    String removeAbandoned = request.getParameter(DBConstants.RDBMS.REMOVE_ABANDONED);
    String removeAbandonedTimeout = request.getParameter(DBConstants.RDBMS.REMOVE_ABONDONED_TIMEOUT);
    String logAbandoned = request.getParameter(DBConstants.RDBMS.LOG_ABANDONED);

    String excelDatasource = request.getParameter(DBConstants.Excel.DATASOURCE);

    String rdfDatasource = request.getParameter(DBConstants.RDF.DATASOURCE);

    String sparqlDatasource = request.getParameter(DBConstants.SPARQL.DATASOURCE);

    String csvDatasource = request.getParameter(DBConstants.CSV.DATASOURCE);
    String csvColumnSeperator = request.getParameter(DBConstants.CSV.COLUMN_SEPARATOR);
    String csvStartingRow = request.getParameter(DBConstants.CSV.STARTING_ROW);
    String csvMaxRowCount = request.getParameter(DBConstants.CSV.MAX_ROW_COUNT);
    String csvHasHeader = request.getParameter(DBConstants.CSV.HAS_HEADER);

    String jndiContextClass = request.getParameter(DBConstants.JNDI.INITIAL_CONTEXT_FACTORY);
    String jndiProviderUrl = request.getParameter(DBConstants.JNDI.PROVIDER_URL);
    String jndiResourceName = request.getParameter(DBConstants.JNDI.RESOURCE_NAME);
    String jndiUserName = request.getParameter(DBConstants.JNDI.USERNAME);
    String jndiPassword = request.getParameter(DBConstants.JNDI.PASSWORD);

    String gspreadDatasource = request.getParameter(DBConstants.GSpread.DATASOURCE);
    String gspreadVisibility = request.getParameter(DBConstants.GSpread.VISIBILITY);
    String gspreadUserName = request.getParameter(DBConstants.GSpread.USERNAME);
    String gspreadPassword = request.getParameter(DBConstants.GSpread.PASSWORD);

    String detailedServiceName = request.getParameter("detailedServiceName");

    String configuration = request.getParameter("config");

    String carbonDatasourceName = request.getParameter(DBConstants.CarbonDatasource.NAME);

    String cassandraDriverClass = request.getParameter(DBConstants.RDBMS.DRIVER);
    String cassandraJdbcUrl = request.getParameter(DBConstants.RDBMS.PROTOCOL);
    if(cassandraJdbcUrl != null) {
        cassandraJdbcUrl = DBConstants.CASSANDRA.CASSANDRA_URL_PREFIX + cassandraJdbcUrl;
    }
    String cassandraUserName = request.getParameter(DBConstants.RDBMS.USER);
    String cassandraPassword = request.getParameter(DBConstants.RDBMS.PASSWORD);

    String webConfig;
    boolean isXAType = false;
    if (xaType != null) {
        isXAType = Boolean.parseBoolean(xaType);
    }


    if (configuration != null && configuration.equals("config")) {
        webConfig = request.getParameter("web_harvest_config_textArea");
    } else {
        webConfig = request.getParameter(DBConstants.WebDatasource.WEB_CONFIG);
    }
    webConfig = (webConfig == null) ? "" : webConfig;

    String flag = request.getParameter("flag");
    flag = (flag == null) ? "" : flag;
    String forwardTo = "dataSources.jsp?ordinal=1";
    boolean remove = true;
    if (datasourceId != null) {
        Config dsConfig = dataService.getConfig(datasourceId);
        if (flag.equals("") && dsConfig != null) {
            String message = "Data source " + datasourceId + " is already available. Please use different data-source name.";
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        } else {
        if (dsConfig == null) {
            dsConfig = newConfig;
            dataService.setConfig(dsConfig);
        }
        if (dsConfig != null) {
            if (flag.equals("delete")) {
                ArrayList<Query> queryList = dataService.getQueries();
                if (queryList.size() >= 0) {
                    for (int a = 0; a < queryList.size(); a++) {
                        if (datasourceId.equals(queryList.get(a).getConfigToUse())) {
                            String message = "Data source " + datasourceId + " has been used by queries. Please remove them to proceed.";
                            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
                            forwardTo = "dataSources.jsp?ordinal=1";
                            remove = false;
                        }
                    }
                }
                if (remove) {
                    dataService.removeConfig(dsConfig);
                }
            } else {
                if (DBConstants.DataSourceTypes.RDBMS.equals(datasourceType)) {
                    if (isXAType) {
                        ArrayList<Property> property = new ArrayList<Property>();
                        Iterator<Property> iterator = dsConfig.getProperties().iterator();
                        while (iterator.hasNext()) {
                            Property availableProperty = iterator.next();
                            if (availableProperty.getName().equals("org.wso2.ws.dataservice.xa_datasource_properties")) {
                                if (availableProperty.getValue() instanceof ArrayList) {
                                    ArrayList<Property> nestedPropertyList = (ArrayList<Property>) availableProperty.getValue();
                                    Iterator<Property> nestedPropertyIterator = nestedPropertyList.iterator();
                                    while (nestedPropertyIterator.hasNext()) {
                                        Property nestedProperty = nestedPropertyIterator.next();
                                        if (nestedProperty.getName().equals("URL")) {
                                            nestedProperty.setValue(url);
                                        } else if (nestedProperty.getName().equals("User")) {
                                            nestedProperty.setValue(user);
                                        } else if (nestedProperty.getName().equals("Password")) {
                                            nestedProperty.setValue(password);
                                        }
                                    }

                                    for (int j = 0; j < propertyCount; j++) {
                                        Property newProperty = new Property();

                                        String propertyName = request.getParameter("propertyNameRaw" + j);
                                        String propertValue = request.getParameter("propertyValueRaw" + j);
                                        if (propertyName != null) {
                                            newProperty.setName(propertyName);
                                            newProperty.setValue((String) propertValue);
                                            nestedPropertyList.add(newProperty);
                                        }
                                    }
                                    break;
                                }
                            }
                            updateConfiguration(dsConfig, DBConstants.RDBMS.XA_DATASOURCE_CLASS, xaDataSourceClass);
                            updateConfiguration(dsConfig, DBConstants.RDBMS.XA_DATASOURCE_PROPS, property);

                            dsConfig.removeProperty(DBConstants.RDBMS.DRIVER);
                            dsConfig.removeProperty(DBConstants.RDBMS.PROTOCOL);
                            dsConfig.removeProperty(DBConstants.RDBMS.USER);
                            dsConfig.removeProperty(DBConstants.RDBMS.PASSWORD);
                        }
                    } else {
                        updateConfiguration(dsConfig, DBConstants.RDBMS.DRIVER, driverClass);
                        updateConfiguration(dsConfig, DBConstants.RDBMS.PROTOCOL, jdbcUrl);
                        updateConfiguration(dsConfig, DBConstants.RDBMS.USER, dsUserName);
                        updateConfiguration(dsConfig, DBConstants.RDBMS.PASSWORD, dsPassword);

                        dsConfig.removeProperty(DBConstants.RDBMS.XA_DATASOURCE_CLASS);
                        dsConfig.removeProperty(DBConstants.RDBMS.XA_DATASOURCE_PROPS);
                    }
                    if (!"TRANSACTION_UNKNOWN".equals(transactionIsolation)) {
                        updateConfiguration(dsConfig, DBConstants.RDBMS.TRANSACTION_ISOLATION, transactionIsolation);
                    } else {
                        dsConfig.removeProperty(DBConstants.RDBMS.TRANSACTION_ISOLATION);
                    }
                    updateConfiguration(dsConfig, DBConstants.RDBMS.INITIAL_SIZE, initialSize);
                    updateConfiguration(dsConfig, DBConstants.RDBMS.MAX_POOL_SIZE, maxPool);
                    updateConfiguration(dsConfig, DBConstants.RDBMS.MAX_IDLE, maxIdle);
                    updateConfiguration(dsConfig, DBConstants.RDBMS.MIN_POOL_SIZE, minPool);
                    updateConfiguration(dsConfig, DBConstants.RDBMS.MAX_WAIT, maxWait);
                    updateConfiguration(dsConfig, DBConstants.RDBMS.VALIDATION_QUERY, validationQuery);
                    if (!"true".equals(testOnBorrow)) {
                        updateConfiguration(dsConfig, DBConstants.RDBMS.TEST_ON_BORROW, testOnBorrow);
                    } else {
                        dsConfig.removeProperty(DBConstants.RDBMS.TEST_ON_BORROW);
                    }
                    if (!"false".equals(testOnReturn)) {
                        updateConfiguration(dsConfig, DBConstants.RDBMS.TEST_ON_RETURN, testOnReturn);
                    } else {
                        dsConfig.removeProperty(DBConstants.RDBMS.TEST_ON_RETURN);
                    }
                    if (!"false".equals(testWhileIdle)) {
                        updateConfiguration(dsConfig, DBConstants.RDBMS.TEST_WHILE_IDLE, testWhileIdle);
                    } else {
                        dsConfig.removeProperty(DBConstants.RDBMS.TEST_WHILE_IDLE);
                    }
                    updateConfiguration(dsConfig, DBConstants.RDBMS.TIME_BETWEEN_EVICTION_RUNS_MILLS, timeBetweenEvictionRunsMillis);
                    updateConfiguration(dsConfig, DBConstants.RDBMS.NUM_TESTS_PER_EVICTION_RUN, numTestsPerEvictionRun);
                    updateConfiguration(dsConfig, DBConstants.RDBMS.MIN_EVICTABLE_IDLE_TIME_MILLIS, minEvictableIdleTimeMillis);
                    if (!"false".equals(removeAbandoned)) {
                        updateConfiguration(dsConfig, DBConstants.RDBMS.REMOVE_ABANDONED, removeAbandoned);
                    } else {
                        dsConfig.removeProperty(DBConstants.RDBMS.REMOVE_ABANDONED);
                    }
                    updateConfiguration(dsConfig, DBConstants.RDBMS.REMOVE_ABONDONED_TIMEOUT, removeAbandonedTimeout);
                    if (!"false".equals(logAbandoned)) {
                        updateConfiguration(dsConfig, DBConstants.RDBMS.LOG_ABANDONED, logAbandoned);
                    } else {
                        dsConfig.removeProperty(DBConstants.RDBMS.LOG_ABANDONED);
                    }
                } else if (DBConstants.DataSourceTypes.EXCEL.equals(datasourceType)) {
                    updateConfiguration(dsConfig, DBConstants.Excel.DATASOURCE, excelDatasource);
                } else if (DBConstants.DataSourceTypes.RDF.equals(datasourceType)) {
                    updateConfiguration(dsConfig, DBConstants.RDF.DATASOURCE, rdfDatasource);
                } else if (DBConstants.DataSourceTypes.SPARQL.equals(datasourceType)) {
                    updateConfiguration(dsConfig, DBConstants.SPARQL.DATASOURCE, sparqlDatasource);
                } else if (DBConstants.DataSourceTypes.CSV.equals(datasourceType)) {
                    updateConfiguration(dsConfig, DBConstants.CSV.DATASOURCE, csvDatasource);
                    updateConfiguration(dsConfig, DBConstants.CSV.COLUMN_SEPARATOR, csvColumnSeperator);
                    updateConfiguration(dsConfig, DBConstants.CSV.STARTING_ROW, csvStartingRow);
                    updateConfiguration(dsConfig, DBConstants.CSV.MAX_ROW_COUNT, csvMaxRowCount);
                    updateConfiguration(dsConfig, DBConstants.CSV.HAS_HEADER, csvHasHeader);
                } else if (DBConstants.DataSourceTypes.JNDI.equals(datasourceType)) {
                    updateConfiguration(dsConfig, DBConstants.JNDI.INITIAL_CONTEXT_FACTORY, jndiContextClass);
                    updateConfiguration(dsConfig, DBConstants.JNDI.PROVIDER_URL, jndiProviderUrl);
                    updateConfiguration(dsConfig, DBConstants.JNDI.RESOURCE_NAME, jndiResourceName);
                    updateConfiguration(dsConfig, DBConstants.JNDI.USERNAME, jndiUserName);
                    updateConfiguration(dsConfig, DBConstants.JNDI.PASSWORD, jndiPassword);
                } else if (DBConstants.DataSourceTypes.GDATA_SPREADSHEET.equals(datasourceType)) {
                    updateConfiguration(dsConfig, DBConstants.GSpread.DATASOURCE, gspreadDatasource);
                    updateConfiguration(dsConfig, DBConstants.GSpread.VISIBILITY, gspreadVisibility);
                    updateConfiguration(dsConfig, DBConstants.GSpread.USERNAME, gspreadUserName);
                    updateConfiguration(dsConfig, DBConstants.GSpread.PASSWORD, gspreadPassword);
                } else if (DBConstants.DataSourceTypes.CARBON.equals(datasourceType)) {
                    if (carbonDatasourceName == null || carbonDatasourceName.length() == 0) {
                        String message = "Please select a valid data source name";
                        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
                        forwardTo = "addDataSource.jsp?configId=" + datasourceId + "&ordinal=1";
                    } else {
                        updateConfiguration(dsConfig, DBConstants.CarbonDatasource.NAME, carbonDatasourceName);
                    }
                } else if (DBConstants.DataSourceTypes.WEB.equals(datasourceType)) {
                    updateConfiguration(dsConfig, DBConstants.WebDatasource.WEB_CONFIG, webConfig);
                } else if (DBConstants.DataSourceTypes.CASSANDRA.equals(datasourceType)) {
                    updateConfiguration(dsConfig, DBConstants.RDBMS.PROTOCOL, cassandraJdbcUrl);
                    updateConfiguration(dsConfig, DBConstants.RDBMS.USER, cassandraUserName);
                    updateConfiguration(dsConfig, DBConstants.RDBMS.PASSWORD, cassandraPassword);
                    updateConfiguration(dsConfig, DBConstants.RDBMS.DRIVER, cassandraDriverClass);
                }
            }
        }
    }
    }
%>
<table>
    <input type="hidden" id="selectbox" value="<%=selectBox%>"/>
    <input type="hidden" id="configId" value="<%=request.getParameter("configId")%>"/>
    <input type="hidden" id="selectedType" value="<%=request.getParameter("selectedType")%>"/>
    <input type="hidden" id="serviceName" value="<%=serviceName%>"/>
    <input type="hidden" id="detailedServiceName" value="<%=detailedServiceName%>"/>
</table>

<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
