<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.hive.explorer.ui.client.HiveScriptStoreClient" %>
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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.hive.explorer.ui.i18n.Resources">
    <script type="text/javascript">
        function deleteRow(name, msg) {
    CARBON.showConfirmationDialog(msg + "' " + name + " ' ?", function() {
        document.location.href = "deleteScript.jsp?" + "scriptName=" + name;
    });
}
    </script>
    <%
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        HiveScriptStoreClient client = new HiveScriptStoreClient(cookie, serverURL, configContext);
        String[] scriptNames = null;

        try {
            scriptNames = client.getAllScriptNames();
        } catch (Exception e) {
    %>
    <script type="text/javascript">
        location.href = "../admin/error.jsp";
        alert("Error while getting the list of scripts");
    </script>
    <%
        }
    %>
    <div id="middle">
        <h2>Available Hive Scripts</h2>

        <div id="workArea">

            <form id="listScripts" name="listScripts" action="" method="POST">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th colspan="4"><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="hive.scripts"/></span>
                        </th>
                    </tr>
                    </thead>
                    <tbody>

                    <%  if(null != scriptNames){
                        for (String aName : scriptNames) {
                    %>
                    <tr>
                        <td width="50%"> <label>
                            <%=aName%>
                            </label>
                        </td>
                        <td width="25%"><a href="../hive-explorer/hiveexplorer.jsp?scriptName=<%=aName%>"><img src="../hive-explorer/images/reports.png">Execute</a>
                        </td>
                        <td width="25%">
                        <a onclick="deleteRow('<%=aName%>','Do you want to delete')"
                               class="delete-icon-link" href="#">Delete</a>
                        </td>

                    </tr>
                    <%
                        }
                        }else { %>
                    <tr>
                        <td><b>No scripts found.</b></td>
                    </tr>

                       <% }
                    %>
                    </tbody>
                     <input type="hidden" id="driver" name="driver" value="">
                </table>
            </form>
        </div>
    </div>


</fmt:bundle>