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
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
    <%
        String directTo = "";
        String scriptName = request.getParameter("scriptName");
        if (scriptName != null && !scriptName.equals("")) {
            directTo = "executeHiveScript.jsp";
        } else {
            directTo = "hiveexplorer.jsp";
        }
    %>

    <script type="text/javascript">

        function connectHive() {
            document.credentials.action = "../hive-explorer/SaveHiveConfiguration";

            var driverName = document.getElementById("driverName").value;
            if ('' == driverName) {
                    CARBON.showErrorDialog('Driver Name is empty. Please enter the driver to connect with Hive');
                return;
            }

            var url = document.getElementById("jdbcURL").value;
            if ('' == url) {
                    CARBON.showErrorDialog('JDBC URL is empty. Please enter the JDBC URL to connect with Hive');
                return;
            }

            var username = document.getElementById("username").value;
            var password = document.getElementById("password").value;

             new Ajax.Request('../hive-explorer/SaveHiveConfiguration', {
                        method: 'post',
                        parameters: {driver:driverName, url:url,
                            hiveusername:username, hivepassword:password},
                        onSuccess: function(transport) {
                            var message = transport.responseText;
                            CARBON.showInfoDialog(message);
                        },
                        onFailure: function(transport) {
                            CARBON.showErrorDialog(transport.responseText);
                        }
                    });
            return true;
        }

         function cancelConnect(){
        location.href="../hive-explorer/listscripts.jsp";
    }

    </script>

    <div id="middle">
        <h2>Connect to Hive</h2>

        <div id="workArea">

            <form id="credentials" name="credentials" action="" method="POST">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="hive.login"/></span>
                        </th>
                    </tr>
                    </thead>
                    <tbody>


                    <tr>
                        <td>
                            <table class="normal-nopadding">
                                <tbody>

                                <tr>
                                    <td width="180px"><fmt:message key="hive.driver"/> <span
                                            class="required">*</span></td>
                                    <td><input name="driverName"
                                               id="driverName" value="<fmt:message key="hive.default.driver"/>"
                                               size="60"/>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key="hive.jdbc.url"/><span
                                            class="required"> *</span>
                                    </td>
                                    <td><input name="jdbcURL"
                                               id="jdbcURL" value="<fmt:message key="hive.default.jdbc.url"/>"
                                               size="60"/>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key="hive.username"/>
                                    </td>
                                    <td><input name="username"
                                               id="username" value="" size="60"/>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key="hive.password"/>
                                    </td>
                                    <td><input name="password"
                                               id="password" type="password" value="" size="60"/>
                                    </td>
                                </tr>
                                <input type="hidden" id="scriptName" name="scriptName"
                                       value="<%=scriptName%>"/>
                                </tbody>
                            </table>
                        </td>
                    </tr>

                    </tbody>
                </table>


                <table class="normal-nopadding">
                    <tbody>

                    <tr>
                        <td class="buttonRow" colspan="2">
                            <input type="button" value="<fmt:message key="hive.connect"/>"
                                   class="button" name="connect" id="connect" onclick="connectHive();"/>
                            <input type="button" value="<fmt:message key="hive.cancel"/>"
                                   name="cancel" class="button" onclick="cancelConnect()"/>
                        </td>
                    </tr>
                    </tbody>

                </table>
            </form>
        </div>
    </div>


</fmt:bundle>