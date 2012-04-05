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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.RSSManagerClient" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.stub.types.RSSInstance" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerCommonUtil" %>
<%@ page import="org.wso2.carbon.utils.multitenancy.MultitenantConstants" %>
<%@ page import="java.net.URISyntaxException" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    String rssInsIdString = request.getParameter("rssInsId");
    int rssInsId = (rssInsIdString != null) ? Integer.parseInt(rssInsIdString) : 0;
    String instanceName = request.getParameter("instanceName");
    instanceName = (instanceName != null) ? instanceName : "";
    String instanceUrl = request.getParameter("instanceUrl");
    try {
        instanceUrl = (instanceUrl != null) ? RSSManagerConstants.JDBC_PREFIX +
                RSSManagerCommonUtil.getDatabasePrefix(instanceUrl) + "://" + RSSManagerCommonUtil.validateRSSInstanceHostname(
                instanceUrl) : "";
    } catch (URISyntaxException e) {

    }
    String username = request.getParameter("username");
    username = (username != null) ? username : "";
    String password = request.getParameter("password");
    password = (password != null) ? password : "";
    String flag = request.getParameter("flag");
    String serverCategory = request.getParameter("serverCategory");
    String dbmsType = RSSManagerCommonUtil.getDatabasePrefix(instanceUrl);
    RSSManagerClient client;
    String tenantDomain = (String) session.getAttribute(MultitenantConstants.TENANT_DOMAIN);

    String backendServerUrl = CarbonUIUtil.getServerURL(
            getServletConfig().getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.
            getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    client = new RSSManagerClient(cookie, backendServerUrl, configContext, request.getLocale());

    if ("save".equals(flag)) {
        RSSInstance rssIns = new RSSInstance();
        rssIns.setName(instanceName);
        rssIns.setServerURL(instanceUrl);
        rssIns.setAdminUsername(username);
        rssIns.setAdminPassword(password);
        rssIns.setDbmsType(dbmsType.toUpperCase());
        if (tenantDomain == null) {
            if (RSSManagerConstants.WSO2_LOCAL_RDS_INSTANCE_TYPE.equals(
                    serverCategory.toUpperCase())) {
                rssIns.setInstanceType(RSSManagerConstants.WSO2_LOCAL_RDS_INSTANCE_TYPE);
            } else {
                rssIns.setInstanceType(RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
            }
        } else {
            rssIns.setInstanceType(RSSManagerConstants.WSO2_USER_DEFINED_INSTANCE_TYPE);
        }
        rssIns.setServerCategory(serverCategory.toUpperCase());

        try {
            String msg = client.addRSSInstance(rssIns);
            response.setContentType("text/xml; charset=UTF-8");
            // Set standard HTTP/1.1 no-cache headers.
            response.setHeader("Cache-Control",
                    "no-store, max-age=0, no-cache, must-revalidate");
            // Set IE extended HTTP/1.1 no-cache headers.
            response.addHeader("Cache-Control", "post-check=0, pre-check=0");
            // Set standard HTTP/1.0 no-cache header.
            response.setHeader("Pragma", "no-cache");

            PrintWriter pw = response.getWriter();
            pw.write(msg);
            pw.flush();
        } catch (Exception e) {
            PrintWriter pw = response.getWriter();
            pw.write(e.getMessage());
            pw.flush();
        }
    } else if ("remove".equals(flag)) {
        try {
            String msg = client.removeDatabaseInstance(rssInsId);
            response.setContentType("text/xml; charset=UTF-8");
            // Set standard HTTP/1.1 no-cache headers.
            response.setHeader("Cache-Control",
                    "no-store, max-age=0, no-cache, must-revalidate");
            // Set IE extended HTTP/1.1 no-cache headers.
            response.addHeader("Cache-Control", "post-check=0, pre-check=0");
            // Set standard HTTP/1.0 no-cache header.
            response.setHeader("Pragma", "no-cache");

            PrintWriter pw = response.getWriter();
            pw.write(msg);
            pw.flush();
        } catch (Exception e) {
            PrintWriter pw = response.getWriter();
            pw.write(e.getMessage());
            pw.flush();
        }
    } else if ("edit".equals(flag)) {
        RSSInstance rssIns = new RSSInstance();
        rssIns.setRssInstanceId(rssInsId);
        rssIns.setName(instanceName);
        rssIns.setServerURL(instanceUrl);
        rssIns.setAdminUsername(username);
        rssIns.setAdminPassword(password);
        rssIns.setDbmsType(dbmsType.toUpperCase());
        rssIns.setServerCategory(serverCategory.toUpperCase());
        if (tenantDomain == null) {
            if (RSSManagerConstants.WSO2_LOCAL_RDS_INSTANCE_TYPE.equals(
                    serverCategory.toUpperCase())) {
                rssIns.setInstanceType(RSSManagerConstants.WSO2_LOCAL_RDS_INSTANCE_TYPE);
            } else {
                rssIns.setInstanceType(RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE);
            }
        } else {
            rssIns.setInstanceType(RSSManagerConstants.WSO2_USER_DEFINED_INSTANCE_TYPE);
        }
        try {
            String msg = client.editRSSInstanceInfo(rssIns);
            response.setContentType("text/xml; charset=UTF-8");
            // Set standard HTTP/1.1 no-cache headers.
            response.setHeader("Cache-Control",
                    "no-store, max-age=0, no-cache, must-revalidate");
            // Set IE extended HTTP/1.1 no-cache headers.
            response.addHeader("Cache-Control", "post-check=0, pre-check=0");
            // Set standard HTTP/1.0 no-cache header.
            response.setHeader("Pragma", "no-cache");

            PrintWriter pw = response.getWriter();
            pw.write(msg);
            pw.flush();
        } catch (Exception e) {
            PrintWriter pw = response.getWriter();
            pw.write(e.getMessage());
            pw.flush();
        }
    }

%>

