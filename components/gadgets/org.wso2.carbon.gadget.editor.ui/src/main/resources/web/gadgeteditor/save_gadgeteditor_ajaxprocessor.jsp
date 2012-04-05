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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.gadget.editor.ui.clients.GadgetEditorServiceClient" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%
    String error1 = "error1";
    String error2 = "error2";
    String content = request.getParameter("codeField");
    String operation = request.getParameter("operation");
    String path = request.getParameter("path");

    try {
        GadgetEditorServiceClient client = new GadgetEditorServiceClient(config,session);
        client.saveGadget(path, content);
    } catch (Exception e) {
        if (e.getCause() != null && e.getCause().getMessage() != null) {
            %><jsp:forward page="../admin/error.jsp?<%=error1%>"/><%
            return;
        }
        request.setAttribute(CarbonUIMessage.ID,new CarbonUIMessage(error2,error2,null));%>
        <jsp:forward page="../admin/error.jsp?<%=error2%>"/><%
        return;
    }
%>



    
