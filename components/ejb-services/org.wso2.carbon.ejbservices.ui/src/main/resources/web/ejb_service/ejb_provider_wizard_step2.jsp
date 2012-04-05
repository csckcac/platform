<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ejbservices.ui.EJBServicesAdminClient" %>
<%@ page import="org.wso2.carbon.ejbservices.stub.types.carbon.WrappedAllConfigurations" %>
<%@ page import="org.wso2.carbon.ejbservices.stub.types.carbon.EJBProviderData" %>
<%@ page import="org.wso2.carbon.ejbservices.stub.types.carbon.EJBAppServerData" %>
<%@ page import="java.util.Random" %>

<script type="text/javascript" src="js/ejbservices.js"></script>

<fmt:bundle basename="org.wso2.carbon.ejbservices.ui.i18n.Resources">
<carbon:breadcrumb label="select.ejb.remote.interface"
		resourceBundle="org.wso2.carbon.ejbservices.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />

    <%
        String archiveId = request.getParameter("archiveId");
        String jnpProviderUrl = (String) session.getAttribute("providerUrl");
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        EJBServicesAdminClient ejbAdminClient;

        Random random = new Random();
        String[] classNames;
        try {
            ejbAdminClient = new EJBServicesAdminClient(configContext, backendServerURL, cookie);
            classNames = ejbAdminClient.getClassNames(request.getParameter("archiveId"));

        } catch (Exception e) {
    %>
    <jsp:forward page="../admin/error.jsp?<%=e.getMessage()%>"/>
    <%
            return;
        }
    %>

    <script type="text/javascript">

        function validateClassList() {
            var remoteInterfaceClass = '';
            var beanJNDIName = document.getElementById('beanJNDIName').value;

            var remoteInterfaces = document.getElementsByName("chkRemoteInterface");
            for (var a = 0; a < remoteInterfaces.length; a++) {
                if (remoteInterfaces[a].checked) {
                    remoteInterfaceClass = remoteInterfaces[a].value;
                }
            }

            if (remoteInterfaceClass == null || wso2.wsf.Util.trim(remoteInterfaceClass) == "") {
                CARBON.showErrorDialog('<fmt:message key="please.enter.remote.interface.class"/>.');
                return false;
            }
            if (beanJNDIName == null || wso2.wsf.Util.trim(beanJNDIName) == "") {
                CARBON.showErrorDialog('<fmt:message key="please.enter.jndi.name.of.ejb"/>.');
                return false;
            }

            document.addEJBDeployServerForm.submit();
        }
        function cancel() {
            location.href = 'index.jsp';
        }

        function handleBackButton(){
            location.href = 'ejb_provider_wizard_step1.jsp?ordinal=1&providerUrl= + <%=jnpProviderUrl%>';
        }
    </script>

    <div id="middle">

        <h2><fmt:message key="create.new.ejb.service.step2"/></h2>

        <div id="workArea">
                            <h5><fmt:message key="select.ejb.remote.interface"/></h5>
                            <table class="styledLeft" id="selectEJBRemoteHomeInterfaces">
                                <thead>
                                    <tr>
                                        <th><fmt:message key="class.name"/></th>
                                        <th><fmt:message key="remote"/></th>
                                        <%--<th><fmt:message key="home"/></th>--%>
                                    </tr>
                                </thead>

                                <%
                                    if (classNames == null || classNames[0].equals("")) {
                                %>
                                <tr>
                                    <td><fmt:message key="no.classes.are.available.in.the.uploaded.jar"/></td>
                                    <td></td>
                                    <td></td>
                                </tr>
                                <%
                                } else {
                                    for (String className : classNames) {
                                %>
                                <tr>
                                    <td><%=className%>
                                    </td>
                                    <td><input
                                            onclick="javascript:setRemoteInterfaceClass(this.value);"
                                            value="<%=className%>"
                                            name="chkRemoteInterface" type="radio"></td>
                                </tr>
                                <%
                                        }
                                    }
                                %>

                            </table>
                    <br/>
            
                    <form name="addEJBDeployServerForm" method="post"
                          action="deploy_ejb_service.jsp">
                            <table class="styledLeft" id="addEJBDeployServerTable">
                                <thead>
                                    <tr>
                                        <th colspan="2"><fmt:message key="ejb.details"/></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr class="tableOddRow">
                                        <td><label for="remoteInterface"><fmt:message key="remote.interface"/><font
                                                color="red">*</font></label></td>
                                        <td><input
                                                maxlength="100" size="70"
                                                tabindex="6"
                                                readonly="true" id="remoteInterface"
                                                name="remoteInterface"
                                                type="text"/></td>
                                    </tr>
                                    <tr  class="tableEvenRow">
                                        <td><label for="beanJNDIName"><fmt:message key="bean.jndi.name"/><font
                                                color="red">*</font></label></td>
                                        <td><input
                                                maxlength="100" size="70"
                                                tabindex="7"
                                                id="beanJNDIName" name="beanJNDIName"
                                                type="text"/></td>
                                    </tr>
                                </tbody>
                            </table>
                        </br>
                        <table class="styledLeft" id="addEJBServiceDetailsTable">
                            <thead>
                                <tr>
                                    <th colspan="2"><fmt:message key="ejb.service.details"/></th>
                                </tr>
                                </thead>
                            <tbody>
                                <tr class="tableOddRow">
                                    <td><label for="serviceName"><fmt:message key="service.name"/><font
                                            color="red">*</font></label></td>
                                    <td><input
                                            maxlength="100" size="70" tabindex="8"
                                            id="serviceName" name="serviceName" type="text"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="buttonRow" colspan="2">
                                        <input name="back_1" type="button" tabindex="9"
                                               value=" &lt; <fmt:message key="back"/>"
                                               onclick="handleBackButton();return false;" class="button"/>
                                        <input type="button" tabindex="10"
                                               value="<fmt:message key="deploy.service"/>"
                                               onclick="validateClassList()" class="button"/>
                                        <input type="button" onClick="cancel()" tabindex="11"
                                               value="<fmt:message key="cancel"/>" class="button"/>
                                        <input type="hidden" id="archiveId" value="<%=archiveId%>"
                                               name="archiveId"/>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </form>
        </div>
    </div>

    <script type="text/javascript">
        alternateTableRows('selectEJBRemoteHomeInterfaces', 'tableEvenRow', 'tableOddRow');
    </script>
</fmt:bundle>