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
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.clients.ManageGenericArtifactServiceClient" %>
<%@ page import="org.wso2.carbon.governance.generic.ui.utils.ManageGenericArtifactUtil" %>
<%@ page import="org.wso2.carbon.governance.services.ui.utils.AddServiceUIGenerator" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.net.URLEncoder" %>
<%
    String error1 = "Your modification cause replacement of another resource!";
    String dataName = request.getParameter("dataName");
    String dataNamespace = request.getParameter("dataNamespace");
    AddServiceUIGenerator uigen = new AddServiceUIGenerator(dataName, dataNamespace);
    ManageGenericArtifactServiceClient
            client = new ManageGenericArtifactServiceClient(config,session);
    OMElement head = null;
    //check whether this is adding a new artifact or this is editing the artifact content
    if(request.getAttribute("content") == null){
        head = uigen.getUIConfiguration(client.getArtifactUIConfiguration(
                request.getParameter("key")),request,config,session);
    }
    else{
        head = (OMElement)request.getAttribute("content");
    }
    String registryArtifactPath = null;
    try {
        String effectivePath = ManageGenericArtifactUtil.addArtifactContent(
                head, request, config, session, dataName, dataNamespace);
        if (effectivePath != null){
            try {
//REGISTRY-698
//                if(request.getParameter("path")!=null){
//                    effectivePath = request.getParameter("path");
//                }
                registryArtifactPath = effectivePath;
            } catch (Exception ignore) {}
            String resourcePagePath = "../resources/resource.jsp?region=region3&item=resource_browser_menu&path=" +
                    URLEncoder.encode(registryArtifactPath,"Utf-8");
            response.sendRedirect(resourcePagePath);
        }else{
            request.setAttribute(CarbonUIMessage.ID,new CarbonUIMessage(error1,error1,null));
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, error1, null);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);

%>

<jsp:forward page="../admin/error.jsp"/>

<%}
} catch (Exception e) {
%>
    <script type="text/javascript">
        window.location = "../generic/add_edit.jsp?region=<%=request.getParameter("region")%>&item=<%=request.getParameter("item")%>&key=<%=request.getParameter("key")%>&lifecycleAttribute=<%=request.getParameter("lifecycleAttribute")%>&breadcrumb=<%=request.getParameter("breadcrumb")%>&wsdlError=" + encodeURIComponent("<%=e.getMessage()%>");
    </script><%
    return;
}
%>



    