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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.transport.mgt.ui.TransportAdminClient" %>
<%@ page import="org.wso2.carbon.transport.mgt.stub.types.carbon.TransportParameter" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<script type="text/javascript" src="global-params.js"></script>

<%
    String backendServerURL;
    ConfigurationContext configContext;
    String cookie;
    TransportAdminClient client;
    TransportParameter[] transportInData;

    backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    client = new TransportAdminClient(cookie, backendServerURL,configContext);
    String transport = request.getParameter("transport");

    try {
        transportInData = client.getGloballyDefinedInParameters(transport);
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

<fmt:bundle basename="org.wso2.carbon.transport.mgt.ui.i18n.Resources">
<carbon:breadcrumb
        label="transport.enable.listener"
        resourceBundle="org.wso2.carbon.transport.mgt.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>" />

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>

    <div id="middle">
        <h2 id="listTransport"><fmt:message key="transport.mgmt"/></h2>
        <div id="workArea">

            <script type="text/javascript">

                function setType(chk,hidden) {
                    var val = document.getElementById(chk).checked;
                    var hiddenElement = document.getElementById(hidden);

                    if (val){
                        hiddenElement.value="true";
                    }else {
                        hiddenElement.value="false";
                    }
                }

                function enableTransport() {
                    CARBON.showConfirmationDialog("<fmt:message key='transport.listener.enable.message'/>",
                            function() {
                                document.transenableform .submit();
                            }, null);
                }
            </script>
            <form action="enable_transport.jsp" id="transenableform" name="transenableform">

                <input type="hidden" name="_transport" value="<%=transport%>"/>

                <table class="styledLeft" id="mailTransport" width="100%">
                    <tr>
                        <td colspan="2" style="border-left: 0px !important; border-right: 0px !important; border-top: 0px !important; padding-left: 0px !important;">
                            <h4><strong><%=transport.toUpperCase()%> Listener</strong></h4>
                        </td>
                    </tr>

                    <tr>
                        <td class="sub-header"><fmt:message key="transport.parameter.name"/></td>
                        <td class="sub-header"><fmt:message key="transport.parameter.value"/></td>
                    </tr>
                    <%
                        if (transportInData != null && transportInData.length>0) {
                            for (TransportParameter currentParam : transportInData) {
                    %>
                    <tr>
                        <td><%=currentParam.getName()%></td>
                        <%   String chkName = currentParam.getName()+"_chk";
                            if ("true".equalsIgnoreCase(currentParam.getValue())) {
                        %>
                        <td>
                            <input type='checkbox' name='<%=chkName%>' value='<%=chkName%>' id='<%=chkName%>' checked='checked' onclick="setType('<%=chkName%>','<%=currentParam.getName()%>')" />
                            <input type="hidden" name="<%=currentParam.getName()%>" id="<%=currentParam.getName()%>" value="true"/>
                        </td>
                        <%
                        }else if ("false".equalsIgnoreCase(currentParam.getValue())){
                        %>
                        <td>
                            <input type='checkbox' name='<%=chkName%>' id='<%=chkName%>' value='<%=chkName%>' onclick="setType('<%=chkName%>','<%=currentParam.getName()%>')" />
                            <input type='hidden' name='<%=currentParam.getName()%>' id='<%=currentParam.getName()%>' value='false' />
                        </td>
                        <%} else {
                        %>
                        <td>
                            <textarea rows="3" cols="60" name="<%=currentParam.getName()%>"><%=currentParam.getValue()%></textarea>
                        </td>
                        <%}%>
                    </tr>
                    <%}
                    } else { %>
                    <tr>
                        <td colspan="2"><fmt:message key="no.params.defined"/></td>
                    </tr>
                    <% } %>

                    <tr>
                        <td colspan="2" class="buttonRow">
                            <input type="reset" value="<fmt:message key="transport.enable"/>" class="button"  onclick="javascript:enableTransport(); return false;"/>
                            <input class="button" type="reset" value="<fmt:message key="transport.cancel"/>"  onclick="javascript:window.history.go(-1); return false;"/>
                        </td>
                    </tr>

                </table>
            </form>
        </div>
    </div>
</fmt:bundle>