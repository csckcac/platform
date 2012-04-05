<%@ page
        import="org.wso2.carbon.messagebox.stub.admin.internal.MessageBoxAdminServiceMessageBoxAdminExceptionException" %>
<%@ page import="org.wso2.carbon.messagebox.stub.admin.internal.MessageBoxAdminServiceStub" %>
<%@ page import="org.wso2.carbon.messagebox.stub.admin.internal.xsd.SQSKeys" %>
<%@ page import="org.wso2.carbon.messagebox.ui.UIUtils" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<fmt:bundle basename="org.wso2.carbon.messagebox.ui.i18n.Resources">


    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>

    <%
        MessageBoxAdminServiceStub messageBoxAdminServiceStub = UIUtils.getMessageBoxAdminServiceStub(config, session, request);
        String loggedInUser = session.getAttribute("logged-user").toString();
        SQSKeys sqsKeys = null;
        if (loggedInUser != null) {
            try {
                sqsKeys = messageBoxAdminServiceStub.getSQSKeys(loggedInUser);
            } catch (MessageBoxAdminServiceMessageBoxAdminExceptionException e) {
    %>
    <script type="text/javascript">
        CARBON.showErrorDialog('<%=e.getFaultMessage().getMessageBoxAdminException().getErrorMessage()%>');

    </script>
    <%
            return;
        }
    } else {
    %>
    <script type="text/javascript">
        CARBON.showErrorDialog('logged in user is null.');

    </script>
    <%
            return;
        }
    %>

    <carbon:breadcrumb
            label="access.keys"
            resourceBundle="org.wso2.carbon.messagebox.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
    <div id="middle">
        <h2><fmt:message key='access.keys'/></h2>

        <div id="workArea">
            <form name="inputForm" action="#" method="post" id="saveSecretId">
                <table style="width:100%" class="styledLeft">
                    <tbody>
                    <tr>
                        <td class="leftCol-med"><fmt:message key='access.key.id'/></td>
                        <td><%=sqsKeys.getAccessKeyId()%>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med"><fmt:message key='secret.access.key'/></td>
                        <td><%=sqsKeys.getSecretAccessKeyId()%>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>