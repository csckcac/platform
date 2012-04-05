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
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerConstants" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type=text/javascript src="js/uiValidator.js"></script>

<fmt:bundle basename="org.wso2.carbon.rssmanager.ui.i18n.Resources">
    <link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>

    <carbon:breadcrumb
            label="Add Database Instance"
            resourceBundle="org.wso2.carbon.rssmanager.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <div id="middle">
        <h2><fmt:message key="rss.manager.new.instance"/></h2>

        <div id="workArea">

            <form method="post" action="#" name="addInstanceForm"
                  id="addInstanceForm" onsubmit="return validateInstanceProperties()">
                <table class="styledLeft">
                    <tr>
                        <td>
                            <table class="normal">
                                <tr>
                                    <td class="leftCol-med"><fmt:message
                                            key="rss.manager.instance.name"/><font
                                            color='red'>*</font></td>
                                    <td><input value="" id="instanceName"
                                               name="instanceName"
                                               size="30" type="text"></td>
                                </tr>
                                <tr>
                                    <td class="leftCol-med"><fmt:message
                                            key="rss.manager.server.category"/>
                                        <font color="red">*</font>
                                    </td>
                                    <td><label>
                                        <select name="serverCategory" id="serverCategory">
                                            <option value="">--SELECT--
                                            </option>
                                            <option value="<%=RSSManagerConstants.RDS%>">
                                                <%=RSSManagerConstants.RDS%>
                                            </option>
                                            <option value="<%=RSSManagerConstants.LOCAL%>">
                                                <%=RSSManagerConstants.LOCAL%>
                                            </option>
                                        </select>
                                    </label>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="leftCol-med"><fmt:message
                                            key="rss.manager.instance.type"/>
                                        <font color="red">*</font>
                                    </td>
                                    <td><label>
                                        <select name="databaseEngine" id="databaseEngine"
                                                onchange="setJDBCValues(this,document)">
                                            <option value="">--SELECT--
                                            </option>
                                            <option value="jdbc:mysql://[machine-name/ip]:[port]#com.mysql.jdbc.Driver">
                                                MySQL
                                            </option>
                                        </select>
                                    </label>
                                    </td>
                                </tr>
                                <tr>
                                    <td align="left"><fmt:message
                                            key="rss.manager.instance.url"/><font
                                            color='red'>*</font></td>
                                    <td><input value="" id="instanceUrl"
                                               name="instanceUrl"
                                               size="60" type="text"></td>
                                </tr>
                                <tr>
                                    <td class="leftCol-med"><fmt:message
                                            key="rss.manager.instance.username"/><font
                                            color='red'>*</font></td>
                                    <td><input value="" id="username"
                                               name="username"
                                               size="30" type="text"></td>
                                </tr>
                                <tr>
                                    <td align="left"><fmt:message
                                            key="rss.manager.instance.password"/><font
                                            color='red'>*</font></td>
                                    <td><input value="" id="password"
                                               name="password"
                                               size="30" type="password"></td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <input type="hidden" id="flag" name="flag" value="save"/>
                    <tr>

                        <td class="buttonRow" colspan="3">
                            <div id="connectionStatusDiv" style="display: none;"></div>
                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.test.connection"/>"
                                   onclick="return testConnection();"/>

                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.save"/>"
                                   onclick="return validateInstanceProperties();"/>

                            <input class="button" type="button"
                                   value="<fmt:message key="rss.manager.cancel"/>"
                                   onclick="document.location.href = 'instances.jsp'"/>
                        </td>

                    </tr>
                </table>
            </form>
        </div>
    </div>

</fmt:bundle>