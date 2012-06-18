<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.analytics.hive.ui.client.HiveScriptStoreClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.poi.hssf.record.formula.functions.True" %>
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

<fmt:bundle basename="org.wso2.carbon.analytics.hive.ui.i18n.Resources">
    <script src="../editarea/edit_area_full.js" type="text/javascript"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>


    <%
        String scriptName = "";
        String scriptContent = "";
        scriptName = request.getParameter("scriptName");

        try {
            String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            HiveScriptStoreClient client = new HiveScriptStoreClient(cookie, serverURL, configContext);
            scriptContent = client.getScript(scriptName);
            if (null != scriptContent && !"".equals(scriptContent)) {
                scriptContent = scriptContent.trim();
                scriptContent = scriptContent.replaceAll("\"", "\\\\\"");
                scriptContent = scriptContent.replaceAll("'", "\\\\\'");
                String[] allQueries = scriptContent.split("\n");
                scriptContent = "";
                for (String aQuery : allQueries) {
                    scriptContent += aQuery.trim() + " ";
                }
            }
        } catch (Exception e) {
            String errorString = e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
    %>
    <script type="text/javascript">
        location.href = "../admin/error.jsp";
        alert('<%=errorString%>');
    </script>
    <%
        }
    %>
    <script type="text/javascript">
        jQuery(document).ready(function() {
            var allQueries = '<%=scriptContent%>';
            executeQuery(allQueries);
        });
    </script>


    <script type="text/javascript">

        function executeQuery(allQueries) {
            if (allQueries != "") {
                new Ajax.Request('../hive-explorer/queryresults.jsp', {
                            method: 'post',
                            parameters: {queries:allQueries},
                            onSuccess: function(transport) {
                                var allPage = transport.responseText;
                                var divText = '<div id="returnedResults">';
                                var closeDivText = '</div>';
                                var temp = allPage.indexOf(divText, 0);
                                var startIndex = temp + divText.length;
                                var endIndex = allPage.indexOf(closeDivText, temp);
                                var queryResults = allPage.substring(startIndex, endIndex);
                                document.getElementById('hiveResult').innerHTML = queryResults;
                            },
                            onFailure: function(transport) {
                                CARBON.showErrorDialog(transport.responseText);
                            }
                        });

            } else {
                var message = "Empty query can not be executed";
                CARBON.showErrorDialog(message);
            }
        }

    </script>


    <style type="text/css">
        .scrollable {
            border: 1px solid black;
            width: 85%;
            height: 300px;
            overflow-y: scroll;
            overflow-x: auto; /*clip-rect:(20px, 500px, 600px, 20px );*/
        }

        table.result {
            border-width: 2px;
            border-style: solid;
            border-color: maroon;
            background-color: white;
        }

        table.allResult {
            border-width: 1px;
            border-style: solid;
            border-color: white;
            background-color: white;
            width: 100%;
        }

    </style>


    <div id="middle">

        <h2>Script Editor<%=" - " + scriptName%>
        </h2>

        <div id="workArea">

            <form id="commandForm" name="commandForm" action="" method="POST">
                <table class="styledLeft noBorders">
                    <tbody>
                    <tr>
                        <td class="middle-header">
                            <fmt:message key="script.results"/>
                        </td>
                    </tr>
                    <tr>
                        <td>

                        </td>
                    </tr>
                    <tr>
                        <td>
                            <div id="hiveResult" class="scrollable" style="width:99%">
                                    <%--the results goes here...--%>
                            </div>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>

</fmt:bundle>