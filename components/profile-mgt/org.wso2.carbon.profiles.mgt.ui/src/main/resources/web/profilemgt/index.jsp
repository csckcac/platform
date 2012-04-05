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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
    prefix="carbon"%>
<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<%@page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@page import="org.wso2.carbon.CarbonConstants"%>
<%@page import="org.wso2.carbon.CarbonError"%>
<%@page import="org.wso2.carbon.ui.CarbonUIMessage"%>

<%@page import="org.wso2.carbon.user.core.UserCoreConstants"%>
<%@page import="org.wso2.carbon.profiles.mgt.stub.dto.AvailableProfileConfigurationDTO"%>
<%@page import="org.wso2.carbon.profiles.mgt.stub.dto.DialectDTO"%>


<%@page import="org.wso2.carbon.profiles.mgt.ui.client.ProfileMgtClient"%><script type="text/javascript" src="extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp" />

<%
    AvailableProfileConfigurationDTO configuration = null;
    ProfileMgtClient client=null;

    try {
        session.removeAttribute("profileconfigurations");
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session);
        ConfigurationContext configContext = (ConfigurationContext) config
                .getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        client = new ProfileMgtClient(cookie, backendServerURL, configContext);
        configuration = client.getAllAvailableProfileConfiguraions();  
    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp" />
<%
            return;
    }
%>


<fmt:bundle basename="org.wso2.carbon.profiles.mgt.ui.i18n.Resources">
    <carbon:breadcrumb
            label="profile.management"
            resourceBundle="org.wso2.carbon.profiles.mgt.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
            
            <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
            <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
            <script type="text/javascript" src="../carbon/admin/js/main.js"></script>
            
    <div id="middle">
      <script type="text/javascript">
              function remove(dialect,configName) {
                 if(configName == "default"){
                     CARBON.showWarningDialog("<fmt:message key='cannot.remove.default.profile'/>", null, null);
                     return;
                 }
                 CARBON.showConfirmationDialog("<fmt:message key='remove.configuration.message1'/>"+ configName +"<fmt:message key='remove.configuration.message2'/>",
                    function() {
                       location.href ="remove-config.jsp?dialect="+dialect+"&configname="+configName;
                     }, null);
                 }
        </script>
        <h2><fmt:message key='profile.configiurations'/></h2>
        <div id="workArea">          
           <%
           DialectDTO[] dialects = configuration.getDialects();
           %>   
           <%if (dialects!=null && dialects.length>0 && dialects[0]!=null) { %>
            <div style="height:30px;">
                <%if (client.isAddProfileEnabled()) {%>
                    <a href="javascript:document.location.href='add-profile.jsp?dialect=<%=dialects[0].getDialectUri()%>'" class="icon-link"
                       style="background-image:url(../admin/images/add.gif);"><fmt:message key='add.new.new.profile.configuration'/></a>
                <%}%>
             </div> 
            <table style="width: 100%" class="styledLeft">
            <thead>
                <tr>
                    <th width="100%" colspan="2"><fmt:message key='available.profile.configurations'/></th>
                </tr>
            </thead>
            <tbody>         
            <%for(int j=0; j<dialects.length;j++) { 
                String[] names = dialects[j].getProfileConfigurations();
            %>
                   <%if (names!=null && names.length>0 && names[0]!=null) { %>
                   <% for (int k=0;k<names.length;k++) { %>
                   <tr>
                   <td width="50%">
                     <a href="profile-edit.jsp?dialect=<%=dialects[j].getDialectUri()%>&profile=<%=names[k]%>"><%=names[k]%></a>
                   </td>

                   <%
                    if (names[k].equals("default")) {
                   %>
                        <td width="50%"><a title="<fmt:message key='remove.configuration'/>"
                                   onclick="return false;"
                                   style="background-image: url(images/delete.gif);" class="icon-link"><font color="gray"><fmt:message key='delete'/></font></a></td>
                   <%
                       } else {
                   %>

                        <td width="50%"><a title="<fmt:message key='remove.configuration'/>"
                                   onclick="remove('<%=dialects[j].getDialectUri()%>','<%=names[k]%>');return false;"
                                   href="#" style="background-image: url(images/delete.gif);" class="icon-link"><fmt:message key='delete'/></a></td>

                   <%
                       }
                   %>


                   </tr>
                   <%}%> 
                   <%}else { %>
                   <tr>
                      <td colspan="2"><i><fmt:message key='no.configurations.available'/></i></td>  
                   </tr>            
                    <%}}%>                                           
          <%} else { %> 
           <tr>
               <td colspan="2"><i><fmt:message key='no.configurations.available'/></i></td>
           </tr>
          <%}%> 
          </tbody>
          </table>      
          <br/><br/>
          </div>
    </div>
</fmt:bundle>
