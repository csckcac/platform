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
<%@ page import="org.wso2.carbon.rssmanager.common.RSSManagerConstants" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.RSSManagerClient" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.beans.DatabasePermissions" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.stub.types.DatabaseUser" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.io.PrintWriter" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    RSSManagerClient client;
    String flag = request.getParameter("flag");
    String strDatabaseInsId = request.getParameter("dbInsId");
    int dbInsId = (strDatabaseInsId != null && !"".equals(strDatabaseInsId)) ? Integer.parseInt(strDatabaseInsId) : 0;
    session.setAttribute(RSSManagerConstants.DATABASE_INSTANCE_ID, String.valueOf(dbInsId));
    String strRssInsId = request.getParameter("rssInsId");
    int rssInsId = (strRssInsId != null && !"".equals(strRssInsId)) ? Integer.parseInt(strRssInsId) : 0;
    session.setAttribute(RSSManagerConstants.RSS_INSTANCE_ID, String.valueOf(rssInsId));
    String privGroupIdString = request.getParameter("privGroupId");
    int privGroupId = (privGroupIdString != null && !"".equals(privGroupIdString)) ? Integer.parseInt(privGroupIdString) : 0;

    String backendServerUrl = CarbonUIUtil.getServerURL(
            getServletConfig().getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.
            getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    client = new RSSManagerClient(cookie, backendServerUrl, configContext, request.getLocale());
    String msg;


    if ("add".equals(flag)) {
        try {
            DatabasePermissions permissions = new DatabasePermissions();
            for (String priv : RSSManagerCommonUtil.getDatabasePrivilegeList()) {
                String value = request.getParameter(priv.toLowerCase());
                if (value != null) {
                    if (RSSManagerCommonUtil.getBooleanResponsePrivilegeList().contains(priv)) {
                        permissions.setPermission(priv, "Y");
                    }
                } else {
                    if (RSSManagerCommonUtil.getBooleanResponsePrivilegeList().contains(priv)) {
                        permissions.setPermission(priv, "N");
                    }
                }
            }
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            DatabaseUser user = new DatabaseUser();
            user.setUsername(username);
            user.setPassword(password);
            //user.setRssInstanceId(rssInsId);
            //msg = client.createUserWithPrivileges(permissions, user, dbInsId);
            msg = client.createUser(user, privGroupId, dbInsId);

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
    } else if ("delete".equals(flag)) {
        String strUserId = request.getParameter("userId");
        int userId = (strUserId != null) ? Integer.parseInt(strUserId) : 0;
        try {
            msg = client.deleteUser(userId, dbInsId);

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
        String userIdString = request.getParameter("userId");
        int userId = (userIdString == null) ? 0 : Integer.parseInt(userIdString);
        DatabasePermissions permissions = new DatabasePermissions();
        for (String priv : RSSManagerCommonUtil.getDatabasePrivilegeList()) {
            String value = request.getParameter(priv.toLowerCase());
            if (value != null && "on".equals(value)) {
                if (RSSManagerCommonUtil.getBooleanResponsePrivilegeList().contains(priv)) {
                    permissions.setPermission(priv, "Y");
                } else if (RSSManagerCommonUtil.getBlobResponsePrivilegeList().contains(priv)) {
                    permissions.setPermission(priv, value);
                } else if (RSSManagerCommonUtil.getIntegerResponsePrivilegeList().contains(priv)) {
                    permissions.setPermission(priv, Integer.parseInt(value));
                } else if (RSSManagerCommonUtil.getStringResponsePrivilegeList().contains(priv)) {
                    permissions.setPermission(priv, value);
                }
            } else {
                if (RSSManagerCommonUtil.getBooleanResponsePrivilegeList().contains(priv)) {
                    permissions.setPermission(priv, "N");
                } else if (RSSManagerCommonUtil.getBlobResponsePrivilegeList().contains(priv)) {
                    permissions.setPermission(priv, "");
                } else if (RSSManagerCommonUtil.getIntegerResponsePrivilegeList().contains(priv)) {
                    permissions.setPermission(priv, 0);
                } else if (RSSManagerCommonUtil.getStringResponsePrivilegeList().contains(priv)) {
                    permissions.setPermission(priv, "");
                }
            }
        }

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        DatabaseUser user = new DatabaseUser();
        user.setUsername(username);
        user.setUserId(userId);
        user.setPassword(password);
        user.setRssInstanceId(rssInsId);

        try {
            msg = client.editUserPrivileges(permissions, user, dbInsId);
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
        }

    } else if ("createDS".equals(flag)) {
        String tmpUserId = request.getParameter("userId");
        int userId = (tmpUserId != null) ? Integer.parseInt(tmpUserId) : 0;

        try {
            msg = client.createCarbonDataSource(dbInsId, userId);

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


