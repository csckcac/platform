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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.stub.types.DatabasePrivilegeTemplate" %>
<%@ page import="org.wso2.carbon.rssmanager.ui.stub.types.DatabasePrivilegeSet" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
    RSSManagerClient client;
    String flag = request.getParameter("flag");
    String privilegeTemplateName = request.getParameter("privilegeTemplateName");

    //Database privileges
    String selectPriv = request.getParameter("select_priv");
    String insertPriv = request.getParameter("insert_priv");
    String updatePriv = request.getParameter("update_priv");
    String deletePriv = request.getParameter("delete_priv");
    String createPriv = request.getParameter("create_priv");
    String dropPriv = request.getParameter("drop_priv");
    String grantPriv = request.getParameter("grant_priv");
    String referencesPriv = request.getParameter("references_priv");
    String indexPriv = request.getParameter("index_priv");
    String alterPriv = request.getParameter("alter_priv");
    String createTmpTablePriv = request.getParameter("create_tmp_table_priv");
    String lockTablesPriv = request.getParameter("lock_tables_priv");
    String createViewPriv = request.getParameter("create_view_priv");
    String showViewPriv = request.getParameter("show_view_priv");
    String createRoutinePriv = request.getParameter("create_routine_priv");
    String alterRoutinePriv = request.getParameter("alter_routine_priv");
    String executePriv = request.getParameter("execute_priv");
    String eventPriv = request.getParameter("event_priv");
    String triggerPriv = request.getParameter("trigger_priv");
    selectPriv = (selectPriv != null && !"".equals(selectPriv) && "on".equals(selectPriv)) ? "Y" : "N";
    insertPriv = (insertPriv != null && !"".equals(insertPriv) && "on".equals(insertPriv)) ? "Y" : "N";
    updatePriv = (updatePriv != null && !"".equals(updatePriv) && "on".equals(updatePriv)) ? "Y" : "N";
    deletePriv = (deletePriv != null && !"".equals(deletePriv) && "on".equals(deletePriv)) ? "Y" : "N";
    createPriv = (createPriv != null && !"".equals(createPriv) && "on".equals(createPriv)) ? "Y" : "N";
    dropPriv = (dropPriv != null && !"".equals(dropPriv) && "on".equals(dropPriv)) ? "Y" : "N";
    grantPriv = (grantPriv != null && !"".equals(grantPriv) && "on".equals(grantPriv)) ? "Y" : "N";
    referencesPriv = (referencesPriv != null && !"".equals(referencesPriv) && "on".equals(referencesPriv)) ? "Y" : "N";
    indexPriv = (indexPriv != null && !"".equals(indexPriv) && "on".equals(indexPriv)) ? "Y" : "N";
    alterPriv = (alterPriv != null && !"".equals(alterPriv) && "on".equals(alterPriv)) ? "Y" : "N";
    createTmpTablePriv = (createTmpTablePriv != null && !"".equals(createTmpTablePriv) && "on".equals(createTmpTablePriv)) ? "Y" : "N";
    lockTablesPriv = (lockTablesPriv != null && !"".equals(lockTablesPriv) && "on".equals(lockTablesPriv)) ? "Y" : "N";
    createViewPriv = (createViewPriv != null && !"".equals(createViewPriv) && "on".equals(createViewPriv)) ? "Y" : "N";
    showViewPriv = (showViewPriv != null && !"".equals(showViewPriv) && "on".equals(showViewPriv)) ? "Y" : "N";
    createRoutinePriv = (createRoutinePriv != null && !"".equals(createRoutinePriv) && "on".equals(createRoutinePriv)) ? "Y" : "N";
    alterRoutinePriv = (alterRoutinePriv != null && !"".equals(alterRoutinePriv) && "on".equals(alterRoutinePriv)) ? "Y" : "N";
    executePriv = (executePriv != null && !"".equals(executePriv) && "on".equals(executePriv)) ? "Y" : "N";
    eventPriv = (eventPriv != null && !"".equals(eventPriv) && "on".equals(eventPriv)) ? "Y" : "N";
    triggerPriv = (triggerPriv != null && !"".equals(triggerPriv) && "on".equals(triggerPriv)) ? "Y" : "N";

    String backendServerUrl = CarbonUIUtil.getServerURL(
            getServletConfig().getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.
            getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    client = new RSSManagerClient(cookie, backendServerUrl, configContext, request.getLocale());
    String msg;

    if ("create".equals(flag)) {
        try {
            DatabasePrivilegeSet privileges = new DatabasePrivilegeSet();
            privileges.setSelectPriv(selectPriv);
            privileges.setInsertPriv(insertPriv);
            privileges.setUpdatePriv(updatePriv);
            privileges.setDeletePriv(deletePriv);
            privileges.setCreatePriv(createPriv);
            privileges.setDropPriv(dropPriv);
            privileges.setGrantPriv(grantPriv);
            privileges.setReferencesPriv(referencesPriv);
            privileges.setIndexPriv(indexPriv);
            privileges.setAlterPriv(alterPriv);
            privileges.setCreateTmpTablePriv(createTmpTablePriv);
            privileges.setLockTablesPriv(lockTablesPriv);
            privileges.setCreateViewPriv(createViewPriv);
            privileges.setShowViewPriv(showViewPriv);
            privileges.setCreateRoutinePriv(createRoutinePriv);
            privileges.setAlterRoutinePriv(alterRoutinePriv);
            privileges.setExecutePriv(executePriv);
            privileges.setEventPriv(eventPriv);
            privileges.setTriggerPriv(triggerPriv);

            DatabasePrivilegeTemplate template = new DatabasePrivilegeTemplate();
            template.setName(privilegeTemplateName);
            template.setPrivileges(privileges);
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
        DatabasePrivilegeSet privileges = new DatabasePrivilegeSet();
            privileges.setSelectPriv(selectPriv);
            privileges.setInsertPriv(insertPriv);
            privileges.setUpdatePriv(updatePriv);
            privileges.setDeletePriv(deletePriv);
            privileges.setCreatePriv(createPriv);
            privileges.setDropPriv(dropPriv);
            privileges.setGrantPriv(grantPriv);
            privileges.setReferencesPriv(referencesPriv);
            privileges.setIndexPriv(indexPriv);
            privileges.setAlterPriv(alterPriv);
            privileges.setCreateTmpTablePriv(createTmpTablePriv);
            privileges.setLockTablesPriv(lockTablesPriv);
            privileges.setCreateViewPriv(createViewPriv);
            privileges.setShowViewPriv(showViewPriv);
            privileges.setCreateRoutinePriv(createRoutinePriv);
            privileges.setAlterRoutinePriv(alterRoutinePriv);
            privileges.setExecutePriv(executePriv);
            privileges.setEventPriv(eventPriv);
            privileges.setTriggerPriv(triggerPriv);

        try {
            DatabasePrivilegeTemplate template = new DatabasePrivilegeTemplate();
            template.setName(privilegeTemplateName);
            template.setPrivileges(privileges);
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


