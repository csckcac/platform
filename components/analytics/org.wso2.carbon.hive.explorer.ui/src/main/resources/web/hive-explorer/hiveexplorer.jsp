<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.hive.explorer.ui.client.HiveExecutionClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.sql.Connection" %>
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
<script src="../editarea/edit_area_full.js" type="text/javascript"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript">

    YAHOO.util.Event.onDOMReady(function() {
        editAreaLoader.init({
                    id : "allcommands"
                    ,syntax: "sql"
                    ,start_highlight: true
                });
    });
</script>


<script type="text/javascript">

    function executeQuery() {
        var allQueries = editAreaLoader.getValue("allcommands");
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
            CARBON.showWarningDialog(message);
        }
    }

    function saveScript() {
        var allQueries = editAreaLoader.getValue("allcommands");
        var scriptName = document.getElementById('scriptName').value;
        if (allQueries != "") {
            if (scriptName != "") {
                new Ajax.Request('../hive-explorer/SaveScriptProcessor', {
                            method: 'post',
                            parameters: {queries:allQueries, scriptName:scriptName},
                            onSuccess: function(transport) {
                                var result = transport.responseText;
                                alert(result);
                                if (result.contains('Success')) {
                                    jQuery(document).init(function () {
                                        CARBON.showInfoDialog(result);
                                    });
                                } else {
                                    jQuery(document).init(function () {
                                        CARBON.showErrorDialog(result);
                                    });
                                }
                            },
                            onFailure: function(transport) {
                                jQuery(document).init(function () {
                                    CARBON.showErrorDialog(result);
                                });
                            }
                        });
            } else {
                var message = "Please enter script name to save";
                jQuery(document).init(function () {
                    CARBON.showErrorDialog(message);
                });
            }

        } else {
            var message = "Empty query can not be executed";
            jQuery(document).init(function () {
                CARBON.showErrorDialog(message);
            });
        }
    }

    function cancelScript(){
        location.href="../hive-explorer/listscripts.jsp";
    }
</script>

<style type="text/css">
    .scrollable {
        border: 1px solid black;
        width: 85%;
        height: 300px;
        overflow-y: scroll;
        overflow-x: auto;
        /*clip-rect:(20px, 500px, 600px, 20px );*/
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
            border-color: black;
            background-color: white;
            width: 100%;
        }

</style>


<% String driver = request.getParameter("driverName");
    String url = request.getParameter("jdbcURL");
    String username = request.getParameter("username");
    String password = request.getParameter("password");
    String scriptName = "";

    try {
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        HiveExecutionClient client = new HiveExecutionClient(cookie, serverURL, configContext);
        boolean authorized = client.getConnection(driver, url, username, password);
        if (!authorized) {
            session.setAttribute("authorized", "false");
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
    alert("Couldn't connect to hive. Check your credentials you provided");
</script>
<%
    } else {
        session.setAttribute("authorized", "true");
        session.setAttribute("driver", driver);
        session.setAttribute("url", url);
        session.setAttribute("username", username);
        session.setAttribute("password", password);
    }

} catch (Exception e) {
    String errorString = e.getMessage();
    CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
    alert(<%=errorString%>);
</script>
<%
        return;
    }
%>

<div id="middle">
    <h2>Hive Explorer</h2>

    <div id="workArea">

        <form id="commandForm" name="commandForm" action="" method="POST">
            <table class="styledLeft">
                <thead>
                <tr>
                    <th><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="hive.commands"/></span>
                    </th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <table class="normal-nopadding">
                            <tbody>
                            <tr>
                                <td>
                                    <fmt:message key="hive.script.name"/>
                                </td>
                                <td>
                                    <input id="scriptName" name="scriptName" size="60" value="<%=scriptName%>"/>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>

                <tr>
                    <td>
                        <table class="normal-nopadding">
                            <tbody>
                            <tr>
                                <td>
                                    <textarea id="allcommands" name="allcommands" cols="150" rows="15"></textarea>
                                </td>
                            </tr>

                            <tr>
                                <td>
                                    <input class="button" type="button" onclick="executeQuery()" value="Run>"/>
                                    <input class="button" type="button" onclick="saveScript()" value="Save"/>
                                    <input type="button"  value="Cancel" onclick="cancelScript()"
                                           class="button" />
                                </td>
                            </tr>

                            </tbody>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td>
                        <table class="normal-nopadding">
                            <tbody>
                            <tr>
                                <td>
                                </td>
                                <td>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="middle-header">
                        <fmt:message key="hive.query.results"/>
                    </td>
                </tr>
                <tr>
                    <td>

                    </td>
                </tr>
                <tr>
                    <td>
                        <div id="hiveResult" class="scrollable">
                                <%--<table class="sample">--%>
                                <%--<tbody>--%>
                                <%--<tr>--%>
                                <%--<td></td>--%>
                                <%--<td>--%>
                                <%--</td>--%>

                                <%--</tr>--%>
                                <%--</tbody>--%>
                                <%--</table>--%>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
            </td>
            </tr>
            </tbody>
            </table>

        </form>


    </div>
</div>


</fmt:bundle>