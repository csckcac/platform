<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ejbservices.ui.EJBServicesAdminClient" %>
<%@ page import="org.wso2.carbon.ejbservices.stub.types.carbon.WrappedAllConfigurations" %>
<%@ page import="org.wso2.carbon.ejbservices.stub.types.carbon.EJBProviderData" %>
<%@ page import="org.wso2.carbon.ejbservices.stub.types.carbon.EJBAppServerData" %>

<script type="text/javascript" src="js/ejbservices.js"></script>

<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    WrappedAllConfigurations allConfigurations;
    EJBProviderData[] ejbConfigurations;
    EJBAppServerData[] appServers;
    EJBAppServerData[] appServerNameList;
    try {
        EJBServicesAdminClient ejbAdminClient =
                new EJBServicesAdminClient(configContext, backendServerURL, cookie);
        allConfigurations = ejbAdminClient.getAllConfigurations();
        ejbConfigurations = allConfigurations.getEjbProviderData();
        appServers = allConfigurations.getAppServerData();
        appServerNameList = allConfigurations.getAppServerNameList();
    } catch (Exception e) {
        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
        <script type="text/javascript">
               location.href = "../admin/error.jsp";
        </script>
<%
        return;
    }
%>

<fmt:bundle basename="org.wso2.carbon.ejbservices.ui.i18n.Resources">
<carbon:breadcrumb label="service.ejb"
		resourceBundle="org.wso2.carbon.ejbservices.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />

<script type="text/javascript">

    function addApplicationServerElement() {
        var combo = document.getElementById('existingAppServerConfigurations');
        if (combo.length == 0) {
            CARBON.showErrorDialog('<fmt:message key="add.application.server"/>.');
            return false;
        }

        var obj = document.getElementById('existingAppServerConfigurations');
        var providerUrl = obj[obj.selectedIndex].value;
        location.href = 'ejb_provider_wizard_step1.jsp?providerUrl=' + providerUrl;
    }

    function validateAddEJBApplicationServerSubmit()
    {
        var serverType = document.getElementById('serverType').value;
        var providerURL = document.getElementById('providerUrl').value;
        var jndiContextClass = document.getElementById('jndiContextClass').value;
        var jndiUserName = document.getElementById('userName').value;
        var password = document.getElementById('password').value;
        var confirmPassword = document.getElementById('confirmPassword').value;

        if (checkForExistingAppServerConfigurations(providerURL)) {
            if (serverType == null || wso2.wsf.Util.trim(serverType) == "") {
                CARBON.showWarningDialog('<fmt:message key="select.application.server.type"/>.');
                return false;
            }

            if (providerURL == null || wso2.wsf.Util.trim(providerURL) == "") {
                CARBON.showWarningDialog('<fmt:message key="enter.valid.provider.url"/>.');
                return false;
            }
            if (jndiContextClass == null || wso2.wsf.Util.trim(jndiContextClass) == "") {
                CARBON.showWarningDialog('<fmt:message key="enter.valid.jndi.context"/>.');
                return false;
            }
            if (password != null && wso2.wsf.Util.trim(password) != "") {
                if (jndiUserName == null || wso2.wsf.Util.trim(jndiUserName) == "") {
                    CARBON.showWarningDialog('<fmt:message key="enter.username"/>.');
                    return false;
                }
                if (password != confirmPassword) {
                    CARBON.showWarningDialog('<fmt:message key="re.entered.password"/>.');
                    return false;
                }
            }
            document.addEJBApplicationServerForm.submit();
        }
        return false;
    }

    function editServiceParameters(serviceName) {
        location.href = "edit_ejb_configuration.jsp?serviceName=" + serviceName;
    }

    function deleteServiceParameters(serviceName) {
        location.href = "delete_ejb_configuration.jsp?serviceName=" + serviceName;
    }
</script>

<div id="middle">

<h2><fmt:message key="service.ejb"/></h2>

<div id="workArea">

    <div id="existingEJBConfigurations">
                    <h5><b><fmt:message key="existing.configurations"/></b></h5>
                    <table class="styledLeft" id="existingEJBConfigurationsTable">
                        <thead>
                            <tr>
                                <th><fmt:message key="service.name"/></th>
                                <th><fmt:message key="provider.url"/></th>
                                <th><fmt:message key="bean.jndi.name"/></th>
                                <th><fmt:message key="edit"/></th>
                                <th><fmt:message key="delete"/></th>
                            </tr>
                        </thead>
                        <%
                            if (ejbConfigurations == null) {
                        %>
                        <tr>
                            <td>
                                <label style="width: 200px; color: brown;"><fmt:message key="no.existing.ejb.configurations"/></label>
                            </td>
                        </tr>
                        <%
                        } else {
                            for (EJBProviderData ejbProviderData : ejbConfigurations) {
                        %>
                        <tr>
                            <td><%=ejbProviderData.getServiceName()%></td>
                            <td><%=ejbProviderData.getProviderURL()%></td>
                            <td><%=ejbProviderData.getBeanJNDIName()%></td>
                            <td >
                                <%--&nbsp;&nbsp;--%>
                                <a title="Edit EJB Configuration"
                                   onclick="editServiceParameters('<%=ejbProviderData.getServiceName()%>'); return false;"
                                   style="background-image:url(../admin/images/edit.gif);"
                                   href="#" class="icon-link">
                                    <%--&nbsp;&nbsp;--%>
                                </a>
                            </td>
                            <td>
                                <a title="Delete EJB Configuration"
                                   onclick="deleteServiceParameters('<%=ejbProviderData.getServiceName()%>'); return false;"
                                   style="background-image:url(../admin/images/delete.gif);"
                                   href="#" class="icon-link">
                                    &nbsp;&nbsp;
                                </a>
                            </td>
                        </tr>
                        <%
                                }
                            }
                        %>
                    </table>
    </div>
    <p>&nbsp;</p>

    <div id="addEJBApplicationServer">
            <form name="addEJBApplicationServerForm" method="post" action="ejbServiceProvider.jsp">

                <h5><b><fmt:message key="create.new.ejb.service"/></b></h5>

                <table class="styledLeft" id="addEJBApplicationServerTable">
                        <thead>
                            <tr class="tableOddRow">
                                <th colspan="2"><fmt:message key="select.application.server"/></th>
                            </tr>

                        </thead>
                        <tbody>
                            <tr class="tableOddRow">
                                <td colspan="2" class="sub-header"><input checked="true"
                                           onclick="toggleEJBAppServerConfigurationEditScreen(this.value);"
                                           value="addNewEJBServer"
                                           id="appServerConfiguration2"
                                           name="appServerConfiguration2" type="radio"><lable><b><fmt:message key="add.new"/></b></lable></td>
                            </tr>
                            <tr class="tableEvenRow">
                                <td><label><fmt:message key="server.type"/><span class="required">*</span></label>
                                </td>
                                <td>
                                    <select onchange="setDefaultServerValues(this,document);return false;"
                                            name="serverType" id="serverType">
                                        <%
                                            if (appServerNameList != null) {
                                                for (EJBAppServerData appServer : appServerNameList) {
                                        %>
                                        <option value="<%=appServer.getServerId()%>"><%=appServer.getServerName()%>
                                        </option>
                                        <%
                                                }
                                            }
                                        %>
                                        <option value="" selected="true">--<fmt:message key="application.server"/>--
                                        </option>
                                    </select></td>
                            </tr>
                            <tr class="tableOddRow">
                                <td><label for="providerUrl"><fmt:message key="provider.url"/><span class="required">*</span></label></td>
                                <td><input maxlength="100" size="60" tabindex="2" id="providerUrl" name="providerUrl" type="text"></td>
                            </tr>
                            <tr class="tableEvenRow">
                                <td><label for="jndiContextClass"><fmt:message key="jndi.context.class"/><span class="required">*</span></label></td>
                                <td><input maxlength="100" size="60" tabindex="3" id="jndiContextClass" name="jndiContextClass" type="text"></td>
                            </tr>
                            <tr class="tableOddRow">
                                <td><label for="userName"><fmt:message key="user.name"/></label></td>
                                <td><input maxlength="20" size="40" tabindex="4"
                                           id="userName" name="userName"
                                           type="text"></td>
                            </tr>
                            <tr class="tableEvenRow">
                                <td><label for="password"><fmt:message key="password"/></label></td>
                                <td><input maxlength="20" size="40" tabindex="5"
                                           id="password" name="password"
                                           type="password"></td>
                            </tr>
                            <tr class="tableOddRow">
                                <td><label for="password"><fmt:message key="confirm.password"/></label></td>
                                <td><input maxlength="20" size="40" tabindex="5"
                                           id="confirmPassword" name="confirmPassword"
                                           type="password"></td>
                            </tr>
                            <tr>
                                <td class="buttonRow">
                                    <input value="<fmt:message key="add.new.application.server"/>"
                                           name="addApplicationServerButton"
                                           id="addApplicationServerButton"
                                           type="button" class="button"
                                           onclick="validateAddEJBApplicationServerSubmit()"/>
                                    <input value="<fmt:message key="reset"/>"
                                           name="resetAddApplicationServerButton"
                                           id="resetAddApplicationServerButton"
                                           type="button" class="button"
                                           onclick="location.href='';"/>
                                </td>
                            </tr>

                            <tr class="tableOddRow">
                                <td colspan="2" class="nopadding"><p>&nbsp;</p></td>
                            </tr>
                            <tr class="tableOddRow">
                                <td colspan="2" class="sub-header"><input onclick="toggleEJBAppServerConfigurationEditScreen(this.value);"
                                                                          value="existingEJBServer" name="appServerConfiguration1"
                                                                          id="appServerConfiguration1" type="radio"/><label><b><fmt:message key="use.existing"/></b></label></td>
                            </tr>
                            <tr class="tableEvenRow">
                                <td><label><fmt:message key="server.configuration"/></label></td>
                                <td>
                                    <select
                                            onchange="setDefaultServerValues(this,document);return false;"
                                            name="existingAppServerConfigurations"
                                            id="existingAppServerConfigurations">
                                        <%
                                            if (appServers != null) {
                                                for (EJBAppServerData appServer : appServers) {
                                        %>
                                        <option value="<%=appServer.getProviderURL()%>"><%=appServer.getProviderURL()%>
                                        </option>
                                        <%
                                                }
                                            }
                                        %>


                                    </select>
                                </td>
                            </tr>
                        </tbody>
                </table>
                <table class="styledLeft">

                </table>
                <p>&nbsp;</p>
    </div>

    <div class="buttonRow">
        <input type="button" value="<fmt:message key="create.new.ejb.service"/>&gt;" onclick="addApplicationServerElement();" id="ejbStep0NextButton">
    </div>
</div>
</div>

<script type="text/javascript">
    alternateTableRows('existingEJBConfigurationsTable', 'tableEvenRow', 'tableOddRow');
</script>

<script type="text/javascript">
    ejbProviderStep1DisableFields();
</script>

</fmt:bundle>
