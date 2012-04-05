<%@ page import="org.wso2.carbon.bam.analyzer.ui.IndexAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.wso2.carbon.bam.index.stub.service.types.TableDTO" %>
<%
    /**
     Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

     WSO2 Inc. licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file except
     in compliance with the License.
     You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on an
     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied.  See the License for the
     specific language governing permissions and limitations
     under the License.
     */
%>

<%

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    IndexAdminClient client = new IndexAdminClient(cookie, serverURL, configContext);

    String function = request.getParameter("function");

    if (function.equals("getDataSourceTypeOfTable")) {

        String tableName = request.getParameter("tableName");

        String dataSourceType = "";
        try {
            dataSourceType = client.getDataSourceTypeOfTable(tableName);
        } catch (AxisFault e) {
            out.print("");
        }

        out.print(dataSourceType);

    } else if (function.equals("getColumnsOfTable")) {
        String tableName = request.getParameter("tableName");

        String[] columns = null;
        try {
            TableDTO table = client.getTableMetaData(tableName);

            if (table != null) {
                columns = table.getColumns();
            }
        } catch (AxisFault e) {
            out.print("");
        }

        StringBuffer sb = new StringBuffer("");

        if (columns != null) {
            for (String column : columns) {
                if (column != null) {
                    sb.append(column);
                    sb.append(":");
                }
            }
        }

        String result = "";
        if (!"".equals(sb.toString())) {
            result = sb.substring(0, sb.lastIndexOf(":"));
        }

        out.print(result);
    }

%>