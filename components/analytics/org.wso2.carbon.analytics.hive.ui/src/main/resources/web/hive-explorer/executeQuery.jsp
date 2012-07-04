<%@ page contentType="text/html; charset=iso-8859-1" language="java" %>

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.analytics.hive.stub.HiveExecutionServiceStub.QueryResult" %>
<%@ page import="org.wso2.carbon.analytics.hive.stub.HiveExecutionServiceStub.QueryResultRow" %>
<%@ page import="org.wso2.carbon.analytics.hive.ui.client.HiveExecutionClient" %>
<%@ page import="org.wso2.carbon.analytics.hive.ui.client.HiveScriptStoreClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ page import="java.util.regex.Pattern" %>

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

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    String scriptName = request.getParameter("scriptName");
    try {
        HiveExecutionClient client = new HiveExecutionClient(cookie, serverURL, configContext);
        HiveScriptStoreClient storeClient = new HiveScriptStoreClient(cookie, serverURL, configContext);
        String scriptContent = storeClient.getScript(scriptName);
        QueryResult[] results = null;
        if (scriptContent != null && !scriptContent.equals("")) {
            Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
            Matcher regexMatcher = regex.matcher(scriptContent);
            String formattedScript = "";
            while (regexMatcher.find()) {
                String temp = "";
                if (regexMatcher.group(1) != null) {
                    // Add double-quoted string without the quotes
                    temp = regexMatcher.group(1).trim().replaceAll(";", "%%");
                    temp = "\"" + temp + "\"";
                } else if (regexMatcher.group(2) != null) {
                    // Add single-quoted string without the quotes
                    temp = regexMatcher.group(2).trim().replaceAll(";", "%%");
                    temp = "\'" + temp + "\'";
                } else {
                    temp = regexMatcher.group().trim();
                }
                formattedScript += temp + " ";
            }
            String[] queries = formattedScript.split(";");
            scriptContent = "";
            for (String aquery : queries) {
                aquery = aquery.trim();
                if (!aquery.equals("")) {
                    aquery = aquery.replaceAll("%%\n", ";");
                    aquery = aquery.replaceAll("%% ", ";");
                    aquery = aquery.replaceAll(" %% ", ";");
                     aquery = aquery.replaceAll("%%", ";");
                    scriptContent = scriptContent + aquery + ";" + "\n";
                }
            }
            results = client.executeScript(scriptContent);
        }

%>
<div id="returnedResults">
    <table class="allResult">
        <tbody>

        <% if (null != results) {
            for (QueryResult result : results) {
        %>
        <tr>
            <td>
                <hr color="#E66C2C"/>
            </td>
        </tr>
        <tr>
            <td><b><font color="#8a2be2">Query: <%=result.getQuery()%>
            </font></b>
            </td>
        </tr>
        <%
            QueryResultRow[] rows = result.getResultRows();
            if (null != rows && rows.length > 0) {
                String[] columnNames = result.getColumnNames();
        %>
        <tr>
            <td>
                <b><font color="#006400"> Results: </font></b>
            </td>
        </tr>
        <tr>
            <td>
                <table class="result">
                    <tbody>

                    <tr>
                        <% for (String aColumnName : columnNames) {
                        %>

                        <td class="resultCol"><b><%=aColumnName%>
                        </b>
                        </td>

                        <% }

                        %>
                    </tr>
                    <%
                        for (QueryResultRow aRow : rows) {

                    %>
                    <tr>

                        <%
                            String[] colValues = aRow.getColumnValues();
                            for (String aValue : colValues) {
                        %>
                        <td class="resultCol"><%=aValue%>
                        </td>

                        <% }
                        %>
                    </tr>

                    <%
                        }
                    %>
                    </tbody>
                </table>
            </td>
        </tr>
        <tr>
            <td>OK</td>
        </tr>
        <% } else {
        %>
        <tr>
            <td><b><font color="#006400">Nothing to Display</font></b></td>
        </tr>
        <tr>
            <td>OK</td>
        </tr>
        <%
                }
            }
        } else {
        %>
        <tr>
            <td><b><font color="#006400">Nothing to Display</font></b></td>
        </tr>

        <%
            }
        %>

        </tbody>
    </table>
</div>
<%
} catch (Exception e) {
%>
<div id="returnedResults">
    <table class="allResult">
        <tbody>
        <tr>
            <td><b><font color="red">ERROR:</font></b></td>
        </tr>
        <tr>
            <td><%=e.getMessage()%>
            </td>
        </tr>
        </tbody>
    </table>
</div>

<% }
%>
