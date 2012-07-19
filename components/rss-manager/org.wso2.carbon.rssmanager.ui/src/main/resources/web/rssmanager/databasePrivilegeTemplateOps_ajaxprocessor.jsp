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
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerCommonUtil" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.RSSManagerClient" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.stub.types.DatabasePrivilege" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.stub.types.DatabasePrivilegeTemplate" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    RSSManagerClient client;
    String flag = request.getParameter("flag");
    String privilegeTemplateName = request.getParameter("privilegeTemplateName");

    String backendServerUrl = CarbonUIUtil.getServerURL(
            getServletConfig().getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.
            getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    client = new RSSManagerClient(cookie, backendServerUrl, configContext, request.getLocale());
    String msg;

    if ("create".equals(flag)) {
        try {
            List<String> permissions = RSSManagerCommonUtil.getDatabasePrivilegeList();
            List<DatabasePrivilege> privs = new ArrayList<DatabasePrivilege>();
            for (String priv : permissions) {
                String value = request.getParameter(priv);
                if (value != null && "on".equals(value)) {
                    if (RSSManagerCommonUtil.getBooleanResponsePrivilegeList().contains(priv)) {
                        DatabasePrivilege dp = new DatabasePrivilege();
                        dp.setName(priv);
                        dp.setValue("Y");
                        privs.add(dp);
                    }
                } else {
                    if (RSSManagerCommonUtil.getBooleanResponsePrivilegeList().contains(priv)) {
                        DatabasePrivilege dp = new DatabasePrivilege();
                        dp.setName(priv);
                        dp.setValue("N");
                        privs.add(dp);
                    }
                }
            }
            DatabasePrivilegeTemplate template = new DatabasePrivilegeTemplate();
            template.setName(privilegeTemplateName);
            template.setPrivileges(privs.toArray(new DatabasePrivilege[permissions.size()]));
            client.createDatabasePrivilegesTemplate(template);

            response.setContentType("text/xml; charset=UTF-8");
            // Set standard HTTP/1.1 no-cache headers.
            response.setHeader("Cache-Control",
                    "no-store, max-age=0, no-cache, must-revalidate");
            // Set IE extended HTTP/1.1 no-cache headers.
            response.addHeader("Cache-Control", "post-check=0, pre-check=0");
            // Set standard HTTP/1.0 no-cache header.
            response.setHeader("Pragma", "no-cache");

            PrintWriter pw = response.getWriter();
            msg = "Database privilege template '" + template.getName() +
                    "' has been successfully created";
            pw.write(msg);
            pw.flush();
        } catch (Exception e) {
            PrintWriter pw = response.getWriter();
            pw.write(e.getMessage());
            pw.flush();
        }
    } else if ("drop".equals(flag)) {
        try {
            client.dropDatabasePrivilegesTemplate(privilegeTemplateName);
            response.setContentType("text/xml; charset=UTF-8");
            // Set standard HTTP/1.1 no-cache headers.
            response.setHeader("Cache-Control",
                    "no-store, max-age=0, no-cache, must-revalidate");
            // Set IE extended HTTP/1.1 no-cache headers.
            response.addHeader("Cache-Control", "post-check=0, pre-check=0");
            // Set standard HTTP/1.0 no-cache header.
            response.setHeader("Pragma", "no-cache");

            PrintWriter pw = response.getWriter();
            msg = "Database privilege template '" + privilegeTemplateName +
                    "' has been successfully dropped";
            pw.write(msg);
            pw.flush();
        } catch (Exception e) {
            PrintWriter pw = response.getWriter();
            pw.write(e.getMessage());
            pw.flush();
        }
    } else if ("edit".equals(flag)) {
        List<String> permissions = RSSManagerCommonUtil.getDatabasePrivilegeList();
        List<DatabasePrivilege> privs = new ArrayList<DatabasePrivilege>();
        for (String priv : permissions) {
            String value = request.getParameter(priv.toLowerCase());
            if (value != null && "on".equals(value)) {
                if (RSSManagerCommonUtil.getBooleanResponsePrivilegeList().contains(priv)) {
                    DatabasePrivilege dp = new DatabasePrivilege();
                    dp.setName(priv);
                    dp.setValue("Y");
                    privs.add(dp);
                }
            } else {
                if (RSSManagerCommonUtil.getBooleanResponsePrivilegeList().contains(priv)) {
                    DatabasePrivilege dp = new DatabasePrivilege();
                    dp.setName(priv);
                    dp.setValue("N");
                    privs.add(dp);
                }
            }
        }

        try {
            DatabasePrivilegeTemplate template = new DatabasePrivilegeTemplate();
            template.setName(privilegeTemplateName);
            template.setPrivileges(privs.toArray(new DatabasePrivilege[permissions.size()]));
            client.editDatabasePrivilegesTemplate(template);

//            response.setContentType("text/xml; charset=UTF-8");
//            // Set standard HTTP/1.1 no-cache headers.
//            response.setHeader("Cache-Control",
//                    "no-store, max-age=0, no-cache, must-revalidate");
//            // Set IE extended HTTP/1.1 no-cache headers.
//            response.addHeader("Cache-Control", "post-check=0, pre-check=0");
//            // Set standard HTTP/1.0 no-cache header.
//            response.setHeader("Pragma", "no-cache");

            PrintWriter pw = response.getWriter();
            msg = "Database privilege template '" + privilegeTemplateName +
                    "' has been successfully edited";
            pw.write(msg);
            pw.flush();
        } catch (Exception e) {
            PrintWriter pw = response.getWriter();
            pw.write(e.getMessage());
            pw.flush();
        }

    }

%>


