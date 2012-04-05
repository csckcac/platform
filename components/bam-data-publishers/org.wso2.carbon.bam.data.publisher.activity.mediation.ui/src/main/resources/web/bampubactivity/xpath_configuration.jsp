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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%@ page import="org.wso2.carbon.bam.data.publisher.activity.mediation.ui.ActivityPublisherAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.bam.data.publisher.activity.mediation.stub.config.XPathConfigData" %>

<fmt:bundle
        basename="org.wso2.carbon.bam.data.publisher.activity.mediation.ui.i18n.Resources">
<carbon:breadcrumb label="activity.xpath"
                   resourceBundle="org.wso2.carbon.bam.data.publisher.activity.mediation.ui.i18n.Resources"
                   topPage="true" request="<%=request%>"/>

<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>


<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ActivityPublisherAdminClient client = new ActivityPublisherAdminClient(cookie, backendServerURL,
            configContext, request.getLocale());

    XPathConfigData[] datas = null;
    try {
        datas = client.getXPathConfigData();
    } catch (Exception e) {
        if (e.getCause().getMessage().toLowerCase().indexOf("you are not authorized") == -1) {
            response.setStatus(500);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        }
    }
%>

<div id="middle">
    <h2>XPath Configuration</h2>

    <div id="workArea">
        <table class="styledLeft">
            <thead>
            <tr>
                <th>Key</th>
                <th>XPath</th>
                <th>Action</th>
            </tr>
            </thead>

            <% if (datas != null) {
                for (XPathConfigData data : datas) {

            %>
            <tr>
                <td><%= data.getKey() %>
                </td>
                <td><%= data.getXpath() %>
                </td>
                <td><a href="javascript:gotoUpdate('<%= data.getKey() %>')">update</a></td>
            </tr>

            <%
                    }
                }
            %>

            <tbody>
            </tbody>
        </table>
        <a class="add-icon-link" href="javascript:gotoAdd()">Add New XPath Definition</a>
    </div>
</div>
<form name="updateDirectForm" action="namespace_configuration.jsp" method="post">
    <input type="hidden" name="getConfig" value="on"/>
    <input type="hidden" name="XPathKey" value="on" id="toUpdate"/>
</form>
<form name="addDirectForm" action="namespace_configuration.jsp" method="post">
    <input type="hidden" name="addConfig" value="on" id="toAdd"/>
</form>
<script>
    function gotoUpdate(key) {
        document.getElementById("toUpdate").value = key;
        document.updateDirectForm.submit();
    }

    function gotoAdd() {
        document.addDirectForm.submit();
    }
</script>
</fmt:bundle>