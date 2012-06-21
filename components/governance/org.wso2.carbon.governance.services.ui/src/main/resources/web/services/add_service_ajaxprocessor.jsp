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
<%@ page import="org.wso2.carbon.registry.common.ui.UIException" %>                            
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.governance.services.ui.utils.AddServicesUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.registry.extensions.utils.CommonUtil" %>
<%@ page import="org.wso2.carbon.governance.services.ui.utils.AddServiceUIGenerator" %>
<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="org.wso2.carbon.governance.services.ui.clients.AddServicesServiceClient" %>
<%@ page import="org.wso2.carbon.registry.extensions.utils.CommonConstants" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.wso2.carbon.registry.core.utils.RegistryUtils" %>
<%@ page import="java.net.URLEncoder" %>

<%
    String error1 = "Your modification cause replacement of another resource!";
    String error2 = "Unable to add the given service";
    AddServiceUIGenerator uigen = new AddServiceUIGenerator();
    AddServicesServiceClient client = new AddServicesServiceClient(config,session);
    String servicePath = client.getServicePath();
    OMElement head = null;
    //check whether this is adding a new service or this is editing the service content
    if(request.getAttribute("content") == null){
        head = uigen.getUIConfiguration(client.getServiceConfiguration(),request,config,session);
    }
    else{
        head = (OMElement)request.getAttribute("content");
    }

    String registryServicePath = null;
    try {
        String returnedServicePath = AddServicesUtil.addServiceContent(head,request,config,session);

        if (returnedServicePath.contains("A resource with the given name and namespace exists")) {
%>
<script type="text/javascript">
    <%--TODO:Needs improvement--%>
    <%--CARBON.showWarningDialog('<%=returnedServicePath%>',window.history.back());--%>
    window.location = "../services/services.jsp?wsdlError=" + encodeURIComponent("<%=returnedServicePath%>");
//    window.history.back();
</script>
<%
        return;
    }

        if (!returnedServicePath.equals("")){

            returnedServicePath =  RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH  + returnedServicePath;
            try {
                //Service Path encode remove to fix CARBON-13289.
                registryServicePath = returnedServicePath;
            } catch (Exception ignore) {}
              String resourcePagePath = "../resources/resource.jsp?region=region3&item=resource_browser_menu&path=" + URLEncoder.encode(registryServicePath,"Utf-8");
           // String resourcePagePath = "../resources/resource.jsp?region=region3&item=resource_browser_menu&path=" + registryServicePath;

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
       window.location = "../services/services.jsp?region=region3&item=governance_services_menu&wsdlError=" + encodeURIComponent("<%=e.getMessage()%>");
    </script><%
    return;
}
%>



    
