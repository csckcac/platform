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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.mashup.jsservices.ui.MashupServiceAdminClient" %>
<%@ page import="java.net.URL" %>
<%
    String serviceName = request.getParameter("serviceName");
    String backendServerURL =
            CarbonUIUtil.getServerURL(config.getServletContext(), session);

    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String serviceURL = (String) request.getAttribute("serviceURL");
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    MashupServiceAdminClient client =
            new MashupServiceAdminClient(cookie, backendServerURL, configContext);
%>
<fmt:bundle basename="org.wso2.carbon.mashup.jsservices.ui.i18n.Resources">

    <tr>
        <%
        if(CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/add/service")){
        %>
        <td colspan="1"><a
                href="../js_service/editor.jsp?serviceName=<%=serviceName%>&action=edit"
                class="icon-link"
                style="background-image: url(../js_service/images/edit-mashup.gif);"> <fmt:message
                key="jsservice.editor"/> </a></td>
        <%}%>
        <td colspan="1"><a
                href="<%=serviceURL%>?doc"
                target="_blank"
                class="icon-link"
                style="background-image: url(../js_service/images/api-doc.gif);"> <fmt:message
                key="jsservice.dashboard.api"/> </a></td>
    </tr>
    <tr>
        <td colspan="1"><a
                href="<%=serviceURL%>"
                target="_blank"
                class="icon-link"
                style="background-image: url(../js_service/images/source-code-html.gif);"> <fmt:message
                key="jsservice.dashboard.custom.ui"/> </a></td>
        <td colspan="1"><a
                href="<%=serviceURL%>?source"
                target="_blank"
                class="icon-link"
                style="background-image: url(../js_service/images/source-code.gif);"> <fmt:message
                key="jsservice.dashboard.mashup.source"/> </a></td>
    </tr>
    <tr>
        <td colspan="1"><a
                href="<%=serviceURL%>?xsd"
                target="_blank"
                class="icon-link"
                style="background-image: url(../js_service/images/xml-schema.gif);"> <fmt:message
                key="jsservice.dashboard.schema"/> </a></td>

        <td colspan="1"><a
                href="<%=serviceURL%>?stub"
                target="_blank"
                class="icon-link"
                style="background-image: url(../js_service/images/javascript-dom.gif);"> <fmt:message
                key="jsservice.dashboard.stub.dom"/> </a></td>
    </tr>
    <tr>
        <td colspan="1"><a
                href="<%=serviceURL%>?stub&lang=e4x"
                target="_blank"
                class="icon-link"
                style="background-image: url(../js_service/images/javascript-e4x.gif);"> <fmt:message
                key="jsservice.dashboard.stub.e4x"/> </a></td>

        <td colspan="1"><a
                href="<%=serviceURL%>?stub&lang=e4x&localhost=true"
                target="_blank"
                class="icon-link"
                style="background-image: url(../js_service/images/javascript-e4x-endpoint.gif);"> <fmt:message
                key="jsservice.dashboard.stub.e4x.localhost"/> </a></td>
    </tr>

</fmt:bundle>