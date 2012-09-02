<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@page import="org.wso2.carbon.security.ui.client.KeyStoreAdminClient" %>
<%@page import="org.wso2.carbon.security.mgt.stub.keystore.xsd.KeyStoreData" %>
<%@page import="org.wso2.carbon.security.ui.client.SecurityAdminClient" %>
<%@page import="org.wso2.carbon.security.mgt.stub.config.xsd.SecurityScenarioData" %>
<%@page import="org.wso2.carbon.security.mgt.stub.config.xsd.SecurityConfigData" %>
<%@page import="java.util.List" %>
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.Arrays" %>
<%@page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.user.mgt.common.FlaggedName"%>

<%@page import="org.wso2.carbon.user.mgt.ui.UserAdminClient"%>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.security.mgt.stub.config.xsd.KerberosConfigData" %>
<link href="../../styles/main.css" rel="stylesheet" type="text/css" media="all"/>
<script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../../main/admin/js/main.js" type="text/javascript"></script>
<script type="text/javascript" src="../../main/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../../main/admin/js/cookies.js"></script>
<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.security.ui.i18n.Resources">
<carbon:breadcrumb label="activate.security"
                   resourceBundle="org.wso2.carbon.security.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>

<script type="text/javascript">
    function doValidation(isPolicyFromReg, isKerberos) {
        if (isPolicyFromReg) {
            return true;
        }

        if (isKerberos) {

            var errorValue = validateEmpty("org.wso2.kerberos.service.principal.name");
            if (errorValue != '') {
                CARBON.showWarningDialog("<fmt:message key="please.specify.valid.principal.name"/>");
                return false;
            }

            errorValue = validateEmpty("org.wso2.kerberos.service.principal.password");

            if (errorValue != '') {
                CARBON.showWarningDialog("<fmt:message key="please.specify.valid.principal.password"/>");
                return false;
            }

        } else {
            var isChecked = false;
            isChecked = isAtleastOneCheckedIfExisting("userGroups");
            if (isChecked != true) {
                CARBON.showWarningDialog("<fmt:message key="please.select.at.leaset.one.user.group"/>");
                return false;
            }

            isChecked = isAtleastOneCheckedIfExisting("trustStore");
            if (isChecked != true) {
                CARBON.showWarningDialog("<fmt:message key="please.select.at.least.one.trust.store"/>");
                return false;
            }

        }



        return true;
    }
</script>
            <%
                FlaggedName[] groupData = null;
                KeyStoreData[] datas = null;
                String curr_pvtks = "";
                List<String> curr_tstks = new ArrayList<String>();
                List<String> curr_ugs = new ArrayList<String>();
                String category = null;
                boolean isPolicyFromRegistry = false;
                boolean fault = false;

                String serviceName = (String) session.getAttribute("serviceName");

                String BUNDLE = "org.wso2.carbon.security.ui.i18n.Resources";
                ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
                String info = MessageFormat.format(resourceBundle.getString("service.secured.using.a.default.scenario"), serviceName);

                String scenId = request.getParameter("scenarioId");
                String registryPolicyPath = null;
                if ("policyFromRegistry".equals(scenId)) {
                    isPolicyFromRegistry = true;
                    registryPolicyPath = request.getParameter("secPolicyRegText");
                    info = MessageFormat.format(resourceBundle.getString("service.secured.using.custom.policy.select.users.and.key.stores"), serviceName);
                }
                if (scenId != null) {
                    session.setAttribute("scenarioId", scenId);
                } else {
                    /**
                     * This is needed for proper functionality of breadcrumbs. If the user goes
                     * forward and clicks on "Activate Security" breadcrumb, scenario Id can only
                     * be accessed from the session.
                     */
                    scenId = (String) session.getAttribute("scenarioId");
                }

                boolean kerberosScenario = false;
                KerberosConfigData kerberosConfigData = null;

                try {
                    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                    ConfigurationContext configContext =
                            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
                    SecurityAdminClient secClient = new SecurityAdminClient(cookie, backendServerURL, configContext);
                    SecurityScenarioData scenData = secClient.getSecurityScenario(scenId);

                    SecurityConfigData configData = secClient.getSecurityConfigData(serviceName, scenId, registryPolicyPath);
                    category = scenData.getCategory();

                    //place holders for existing configs
                    if (configData != null) {
                        if (configData.getPrivateStore() != null) {
                            curr_pvtks = configData.getPrivateStore();
                        }

                        if (configData.getTrustedKeyStores() != null &&
                                configData.getTrustedKeyStores().length > 0 &&
                                configData.getTrustedKeyStores()[0] != null) {
                            curr_tstks = Arrays.asList(configData.getTrustedKeyStores());
                        }

                        if (configData.getUserGroups() != null &&
                                configData.getUserGroups().length > 0 &&
                                configData.getUserGroups()[0] != null) {
                            curr_ugs = Arrays.asList(configData.getUserGroups());
                        }

                        if (category.contains("kerberos")) {
                            kerberosConfigData = configData.getKerberosConfigurations();
                        }
                    }

                    if (category.contains("ut")) {
                        UserAdminClient client = new UserAdminClient(cookie, backendServerURL, configContext);
                        groupData = client.getAllRolesNames();
                    }

                    if (category.contains("keystore")) {
                        KeyStoreAdminClient client = new KeyStoreAdminClient(cookie, backendServerURL, configContext);
                        datas = client.getKeyStores();
                    }

                    if (category.contains("kerberos")) {
                        kerberosScenario = true;
                    }

                } catch (Exception e) {
                     fault = true;
                     CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
                <script type="text/javascript">
                    location.href = "../admin/error.jsp";
                </script>
<%
                }
    if(!fault) {
%>
<div id="middle">
        <h2><fmt:message key="activate.security"/></h2>
    <div id="workArea">
        <p><%=info%></p>
        <p>&nbsp;</p>

        <form method="post" action="add-security.jsp" name="dataForm"
              onsubmit="return doValidation(<%= isPolicyFromRegistry%>, <%=kerberosScenario%>)">
            <input type="hidden" name="scenarioId" id="scenarioId"
                   value="<%= scenId%>"/>
            <input type="hidden" name="policyPath" id="policyPath"
                   value="<%= registryPolicyPath%>"/>
            <%
                if (category.contains("ut")) {
            %>
            <table id="ut" class="styledLeft">
                <thead>
                <tr>
                    <th><fmt:message key="user.groups"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="formRow">
                        <table class="normal">
                            <%
                                if (groupData != null) {
                                    for (FlaggedName data : groupData) {
                                        if (data != null) { //Confusing!!. Sometimes a null object comes. Maybe a bug in Axis!!
                                            
                                            if(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME.equals(data.getItemName())) {
                                                continue;
                                            }
                                            
                                            String checked = "";
                                            if (curr_ugs.contains(data.getItemName())) {
                                                checked = "checked=\"checked\"";
                                            }
                            %>
                            <tr>
                                <td><input type="checkbox" name="userGroups"
                                           value="<%=data.getItemName()%>" <%=checked%>/> <%=data.getItemName()%>
                                </td>
                            </tr>
                            <%
                                        }
                                    }
                                }
                            %>
                        </table>
                    </td>
                </tr>
                </tbody>
            </table>
            <%
                }

            %>
            <%

                if (category.contains("keystore")) {
            %>
            <table id="trtks" class="styledLeft">
                <thead>
                <tr>
                    <th><fmt:message key="trusted.key.stores"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="formRow">
                        <table class="normal">
                            <%
                                if (datas != null) {
                                    for (KeyStoreData data : datas) {
                                        if (data != null) { //Confusing!!. Sometimes a null object comes. Maybe a bug in Axis!!

                                            String checked = "";
                                            if (curr_tstks.contains(data.getKeyStoreName())) {
                                                checked = "checked=\"checked\"";
                                            }
                            %>
                            <tr>
                                <td><input type="checkbox" name="trustStore"
                                           value="<%=data.getKeyStoreName()%>" <%=checked%>/> <%=data.getKeyStoreName()%>
                                </td>
                            </tr>
                            <%
                                        }
                                    }
                                }
                            %>
                        </table>
                    </td>
                </tr>
                </tbody>
            </table>
            <table id="pvtks" class="styledLeft">
                <thead>
                <tr>
                    <th><fmt:message key="private.key.store"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="formRow">
                        <table class="normal">
                            <tr>
                                <td>
                                    <select name="privateStore">
                                        <%
                                            if (datas != null) {
                                                for (KeyStoreData data : datas) {
                                                    if (data != null && data.getPrivateStore()) {
                                                        String selected = "";
                                                        if (data.getKeyStoreName().equals(curr_pvtks)) {
                                                            selected = "selected=\"selected\"";
                                                        }

                                        %>
                                        <option value="<%=data.getKeyStoreName()%>" <%=selected %>><%=data.getKeyStoreName()%>
                                        </option>
                                        <%
                                                    }
                                                }
                                            }
                                        %>
                                    </select>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                </tbody>
            </table>
            <%
                }
            %>

            <!-- If the scenario is a kerberos category one, configure, KDC, service principle etc ... -->
            <%
                if (category.contains("kerberos")) {

                    String servicePrincipleName = "";
                    String servicePrinciplePassword = "";
                    if (kerberosConfigData != null) {

                        servicePrincipleName = kerberosConfigData.getServicePrincipleName();
                        servicePrinciplePassword = kerberosConfigData.getServicePrinciplePassword();
                    }
            %>

            <table id="kerberosTable" class="styledLeft">
                <thead>
                <tr>
                    <th><fmt:message key="configure.kerberos.parameters"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="formRow">
                        <table class="normal">

                            <tr>
                                <td>
                                    <fmt:message key="kerberos.service.principal.name"/><font color="red">*</font>
                                </td>
                                <td>
                                    <input type="text" name="org.wso2.kerberos.service.principal.name" value="<%=servicePrincipleName%>"/>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <fmt:message key="kerberos.service.principal.password"/><font color="red">*</font>
                                </td>
                                <td>
                                    <input type="password" name="org.wso2.kerberos.service.principal.password" value="<%=servicePrinciplePassword%>"/>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                </tbody>
            </table>
            <input type="hidden" name="org.wso2.security.category" value="kerberos">

            <%
                }
            %>


            <p></p>
            <table class="styledLeft">
                <tr class="buttonRow">
                    <td>
                         <input class="button" type="button" value="< <fmt:message key="back"/>"
                               onclick="location.href = 'index.jsp?serviceName=<%=serviceName%>'"/>
                        <input class="button" type="submit" value="<fmt:message key="finish"/>"/>
                        <input class="button" type="button" value="<fmt:message key="cancel"/>" 
                               onclick="location.href = '../service-mgt/service_info.jsp?serviceName=<%=serviceName%>'"/>
                    </td>
                </tr>
            </table>
            <%

            %>
        </form>
    </div>
</div>
<%
    }
%>
</fmt:bundle>