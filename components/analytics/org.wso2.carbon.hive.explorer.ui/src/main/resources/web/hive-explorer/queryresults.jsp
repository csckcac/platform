<%@ page contentType="text/html; charset=iso-8859-1" language="java" %>

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.analytics.hive.stub.HiveExecutionServiceStub.*" %>
<%@ page import="org.wso2.carbon.hive.explorer.ui.client.HiveExecutionClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

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

    String hiveScript = request.getParameter("queries");
    HiveExecutionClient client = new HiveExecutionClient(cookie, serverURL, configContext);
    String authorized = session.getAttribute("authorized").toString();
    if (authorized.equalsIgnoreCase("true")) {
        String[] credentials = new String[4];
        credentials[0] = session.getAttribute("driver").toString();
        credentials[1] = session.getAttribute("url").toString();
        credentials[2] = session.getAttribute("username").toString();
        credentials[3] = session.getAttribute("password").toString();
        QueryResult[] results = client.executeScript(hiveScript, credentials);
%>
<div id="returnedResults">
    <table class="normal-nopadding">
        <tbody>

        <%
            for (QueryResult result : results) {
        %>
        <tr>
            <td>
                <hr/>
            </td>
        </tr>
        <tr>
            <td>Query: <%=result.getQuery()%>
            </td>
        </tr>
        <%
            QueryResultRow[] rows = result.getResultRows();
            if (null != rows && rows.length > 0) {
                String[] columnNames = result.getColumnNames();
        %>
        <tr>
            <td>
                Results:
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
            <td>Nothing to Display</td>
        </tr>
        <tr>
            <td>OK</td>
        </tr>
        <%
                }
            }
        %>

        </tbody>
    </table>
</div>
<%
    }
%>