<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.apache.http.HttpStatus" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bpel.stub.mgt.types.*" %>
<%@ page import="org.wso2.carbon.bpel.ui.BpelUIUtil" %>
<%@ page import="org.wso2.carbon.bpel.ui.clients.BPELPackageManagementServiceClient" %>
<%@ page import="org.wso2.carbon.bpel.ui.clients.ProcessManagementServiceClient" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.Utils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.io.File" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<!--
 ~ Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<%
    response.setHeader("Cache-Control",
            "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String)session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    BPELPackageManagementServiceClient client;
    ProcessManagementServiceClient processClient = null;
    DeployedPackagesPaginated packageList = null;
    int pageNumberInt = 0;

    String pageNumber = request.getParameter("pageNumber");
    String operation = request.getParameter("operation");
    String packageName = request.getParameter("packageName");

    boolean isAuthorizedToManageProcesses = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/bpel/processes");
    boolean isAuthorizedToManagePackages =
                CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/bpel/packages");
    boolean isAuthorizedToMonitor =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/monitor/bpel");


     String processList[] = null;

    if (isAuthorizedToMonitor || isAuthorizedToManageProcesses) {
        try {
            processClient = new ProcessManagementServiceClient(cookie, backendServerURL,
                    configContext, request.getLocale());
        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
            return;
        }
    }

    if(isAuthorizedToManageProcesses && operation != null && processClient != null) {
        String pid = request.getParameter("processID");
        if(operation.toLowerCase().trim().equals("retire")) {
            if(pid != null && pid.length() > 0){
                try{
                    processClient.retireProcess(BpelUIUtil.stringToQName(pid));
                } catch (Exception e) {
                    response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                    session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
                    return;
                }
            }

        } else if(operation.toLowerCase().trim().equals("activate")) {
            if(pid != null && pid.length() > 0){
                try{
                    processClient.activateProcess(BpelUIUtil.stringToQName(pid));
                } catch (Exception e) {
                    response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                    session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
                    return;
                }
            }
        }
    }

    if (isAuthorizedToManagePackages) {
        try {
            client = new BPELPackageManagementServiceClient(cookie, backendServerURL, configContext);
            processList = client.listProcessesInPackage(packageName).getProcess();

        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
        }
    }

    if (isAuthorizedToManagePackages || isAuthorizedToMonitor ) {
        try{
            client = new BPELPackageManagementServiceClient(cookie, backendServerURL, configContext);

        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
            return;
        }

        if(isAuthorizedToManagePackages && operation != null && packageName != null &&
           operation.equals("undeploy")) {
            try {
                UndeployStatus_type0 status = client.undeploy(packageName);
                if(status.equals(UndeployStatus_type0.FAILED)) {
                    response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    CarbonUIMessage uiMsg = new CarbonUIMessage(
                            CarbonUIMessage.ERROR,
                            "BPEL package "+ packageName +" undeployment failed.",
                            null);
                    session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
                    return;
                }
            } catch (Exception e) {
                response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
                return;
        }
    }

        if (isAuthorizedToManagePackages && operation != null && packageName != null) {
        try {
            processList = client.listProcessesInPackage(packageName).getProcess();

        } catch (Exception e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
        }
    }

        if (isAuthorizedToMonitor || isAuthorizedToManagePackages) {
            if(pageNumber == null) {
                pageNumber = "0";
            }

            try{
                pageNumberInt = Integer.parseInt(pageNumber);
            } catch (NumberFormatException ignored) {

            }

            try{
                packageList = client.getPaginatedPackageList(pageNumberInt);

            } catch (Exception e) {
                response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
                session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
                return;
            }
        }
    }
%>
<fmt:bundle basename="org.wso2.carbon.bpel.ui.i18n.Resources">
    <carbon:breadcrumb
            label="bpel.headertext_package_dashboard"
            resourceBundle="org.wso2.carbon.bpel.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
        <jsp:include page="../dialog/display_messages.jsp"/>
    <div id="middle">
        <div id="package-list-main">
            <h2><fmt:message key="bpel.headertext_package_dashboard"/>&nbsp;(<%=packageName%>)</h2>
            <div id="workArea">
                <div id="package-list">
<%
    if (isAuthorizedToManagePackages || isAuthorizedToMonitor) {
                        if(packageList != null && packageList.get_package() != null ) {

%>
                    <table id="packageListTable" class="styledLeft" width="100%">
                        <thead>

<%
        if (isAuthorizedToManagePackages) {
%>
                            <tr>
                                    <th><nobr><fmt:message key="actions"/> </nobr></th>
                            </tr>
<%
        }
%>
                        </thead>

                        <tbody>

                        <%
                            if (isAuthorizedToManagePackages) {
                                //abstract only the package name from the request param
                                String name = packageName.substring(0, packageName.lastIndexOf("-"));
                        %>
                             <tr>
                                 <td>
<%
                                    String jQueryCompliantID = BpelUIUtil.
                                                generateJQueryCompliantID(name);
%>
                                    <a id="<%=jQueryCompliantID%>"
                                       class="icon-link-nofloat registryWriteOperation"
                                       style="background-image:url(images/undeploy.gif);"
                                       href="<%=BpelUIUtil.getUndeployLink(name, pageNumberInt)%>">Undeploy</a>

                                    <a id="<%=jQueryCompliantID%>"
                                       class="icon-link-nofloat registryNonWriteOperation"
                                       style="background-image:url(images/undeploy.gif);color:#777;cursor:default;"
                                       onclick="return false"
                                       href="<%=BpelUIUtil.getUndeployLink(name, pageNumberInt)%>">Undeploy</a>

                                    <script type="text/javascript">
                                        jQuery('#<%=jQueryCompliantID%>').click(function() {
                                            function handleYes<%=jQueryCompliantID%>() {
                                                window.location = jQuery('#<%=jQueryCompliantID%>').attr('href');
                                            }

                                            CARBON.showConfirmationDialog(
                                                    "Do you want to undeploy package <%=name%>?",
                                                    handleYes<%=jQueryCompliantID%>,
                                                    null);
                                            return false;
                                        });
                                    </script>
                                  </td>


                             </tr>
                             <%
                                 String path = RegistryConstants.CONFIG_REGISTRY_BASE_PATH + RegistryConstants.PATH_SEPARATOR + "bpel" + RegistryConstants.PATH_SEPARATOR + "packages" + RegistryConstants.PATH_SEPARATOR + name + RegistryConstants.PATH_SEPARATOR + name.concat(".zip");
                                 if (isAuthorizedToManagePackages && packageName.contains(name)) {
                                %>
                            <tr>
                                <td>
                                <a id="<%=name%>" class="icon-link-nofloat"
                                   style="background-image:url(images/icon-download.jpg);"
                                   href="javascript:sessionAwareFunction(function() {window.location = '<%=Utils.getResourceDownloadURL(request, path)%>'}, org_wso2_carbon_registry_resource_ui_jsi18n['session.timed.out']);"
                                   target="_self">Download</a>

                                </td>
                            </tr>
                        </tbody>
                        <%
                                        }
                                    }
                                }
                            }
                        %>
                    </table>

                    <table>
                        <tr>&nbsp;</tr>
                    </table>


                    <table id="packageListTable" class="styledLeft" width="100%">
                        <thead>
                        <tr>
                            <th>
                                <nobr><fmt:message key="bpel.process"/></nobr>
                            </th>
                        </tr>
                        </thead>

                        <tbody>

                        <% if (processList != null) {
                            for (int i = 0; i < processList.length; i++) {
                        %>
                        <tr>
                            <td>
                                <a href="./process_info.jsp?Pid=<%=processList[i]%>"><%=processList[i]%>
                                </a>

                            </td>
                        </tr>
                        <%
                                }
                            }
                        %>

                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</fmt:bundle>
