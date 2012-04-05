<%--
  ~  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  Licensed under the Apache License, Version 2.0 (the "License");
  ~  you may not use this file except in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~        http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing, software
  ~  distributed under the License is distributed on an "AS IS" BASIS,
  ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~  See the License for the specific language governing permissions and
  ~  limitations under the License.
  --%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.wso2.carbon.endpoint.ui.client.EndpointAdminClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.endpoint.stub.types.common.ConfigurationObject" %>

<%
    String endpointName = request.getParameter("endpointName");
    String endpointType = request.getParameter("endpointType");
    boolean forceDelete = false;
    if ("true".equals(request.getParameter("force"))) {
        forceDelete = true;
    }

    String url = CarbonUIUtil.getServerURL(this.getServletConfig().getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    EndpointAdminClient adminClient = new EndpointAdminClient(cookie, url, configContext);

    if ((endpointName != null) && (!"".equals(endpointName)) && (endpointType == null)) {
        if (!forceDelete) {
            try {
                ConfigurationObject[] dependents = adminClient.getDependents(endpointName);
                if (dependents != null) {
                    String msg = "";
                    ResourceBundle bundle = ResourceBundle.getBundle("org.wso2.carbon.mediation.initializer.ui.i18n.Resources",
                            request.getLocale());
                    for (ConfigurationObject o : dependents) {
                        msg += "&ensp;&ensp;- " + o.getId();
                        if (bundle != null) {
                            msg += " (" + bundle.getString("dependency.mgt." + o.getType()) + ")";
                        }
                        msg += "<br/>";
                    }
                    request.getSession().setAttribute("dependency.mgt.error", msg);
                    request.getSession().setAttribute("dependency.mgt.error.epr", endpointName);
                } else {
                    doForceDelete(adminClient, endpointName, request);
                }
            } catch (Exception e) {
                String msg = "Could not delete endpoint: " + e.getMessage();
                CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);   
            }
        } else {
            doForceDelete(adminClient, endpointName, request);
        }
    } else {
        adminClient.deleteDynamicEndpoint(endpointName);
    }
%>

<script type="text/javascript">
    location.href = "index.jsp";
</script>

<%!
    private void doForceDelete(EndpointAdminClient adminClient, String epr,
                             HttpServletRequest request) {
        try {
            adminClient.deleteEndpoint(epr);
        } catch (Exception e) {
            String msg = "Could not delete endpoint: " + e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
        }
    }
%>
