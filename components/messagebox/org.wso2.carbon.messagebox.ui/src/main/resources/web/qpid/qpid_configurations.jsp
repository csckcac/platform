<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.qpid.stub.service.QpidAdminServiceStub" %>
<%@ page import="org.wso2.carbon.messagebox.ui.UIUtils" %>
<fmt:bundle basename="org.wso2.carbon.messagebox.ui.i18n.Resources">

    <carbon:breadcrumb
            label="brokermanager.list"
            resourceBundle="org.wso2.carbon.messagebox.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>


    <div id="middle">
    <div id="workArea">
        <h3>Qpid Server Configurations </h3>
        <table class="styledLeft">
            <thead>
            <tr>
                <th>Configuration name</th>
                <th>Details</th>
            </tr>
            </thead>
            <tbody>
            <%
                QpidAdminServiceStub qpidAdminServiceStub = UIUtils.getQpidAdminServiceStub(config, session, request);
                String qpidPort = qpidAdminServiceStub.getPort();
                String clientID = qpidAdminServiceStub.getClientID();
                String virtualHostName = qpidAdminServiceStub.getVirtualHostName();
                String carbonhostName = qpidAdminServiceStub.getHostname();
                String SSLPort = qpidAdminServiceStub.getSSLPort();

                if (qpidPort != null) {
            %>
            <tr>
                <td>TCP port of the broker
                </td>
                <td><%=qpidPort%>
                </td>
            </tr>
            <%
                }

                if (clientID != null) {
            %>
            <tr>
                <td>Client id (machine name) that is used to connect to the broker
                </td>
                <td><%=clientID%>
                </td>
            </tr>
            <%
                }

                if (virtualHostName != null) {
            %>
            <tr>
                <td>Default virtual host name of the broker
                </td>
                <td><%=virtualHostName%>
                </td>
            </tr>

            <%
                }

                if (carbonhostName != null) {
            %>
            <tr>
                <td>Hostname of the machine that broker runs on
                </td>
                <td><%=carbonhostName%>
                </td>
            </tr>
            <%
                }

                if (SSLPort != null) {
            %>
            <tr>
                <td>SSL port client can use to communicate with the broker
                </td>
                <td><%=SSLPort%>
                </td>
            </tr>
            <%
                }
            %>
            </tbody>
        </table>
    </div>

</fmt:bundle>
