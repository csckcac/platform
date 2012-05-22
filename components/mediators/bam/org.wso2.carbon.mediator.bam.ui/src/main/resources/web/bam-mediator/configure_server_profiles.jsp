<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.mediationstats.data.publisher.stub.conf.MediationStatConfig" %>
<%@ page import="org.wso2.carbon.bam.mediationstats.data.publisher.stub.conf.Property" %>
<%--<%@ page import="org.wso2.carbon.bam.mediationstats.data.publisher.ui.MediationStatPublisherAdminClient" %>--%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.mediator.bam.ui.BamServerProfileUtils" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<fmt:bundle basename="org.wso2.carbon.mediator.bam.ui.i18n.Resources">

<carbon:breadcrumb
        label="system.statistics"
        resourceBundle="org.wso2.carbon.mediator.bam.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>
<%
    String userName = "admin";
    String password = "admin";
    String port = "port";

    BamServerProfileUtils bamServerProfileUtils = new BamServerProfileUtils();
    bamServerProfileUtils.addArtifacts();
%>


<div id="middle">
    <h2>
        <fmt:message key="bam.server.profile"/>
    </h2>

    <div id="workArea">
        <table>
            <tr>
                <td>
                    <fmt:message key="username"/>
                </td>
                <td>
                    <input type="text" id="username" value="<%=userName%>"/>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="password"/>
                </td>
                <td>
                    <input type="password" id="password" value="<%=password%>"/>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="port"/>
                </td>
                <td>
                    <input type="text" id="port" value="<%=port%>"/>
                </td>
            </tr>
        </table>
    </div>
</div>


</fmt:bundle>

