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

<%@page import="org.wso2.carbon.profiles.mgt.ui.client.ProfileMgtClient"%>
<%@page import="org.wso2.carbon.profiles.mgt.stub.dto.ClaimConfigurationDTO"%><script type="text/javascript" src="extensions/js/vui.js"></script>
<script type="text/javascript" src="../extensions/core/js/vui.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp" />
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>

<%
    String profile = request.getParameter("profile");
    String dialect = request.getParameter("dialect");
    ClaimConfigurationDTO[] configurations = null;

    try {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        ProfileMgtClient client = new ProfileMgtClient(cookie, backendServerURL, configContext);
        configurations = client.getProfileConfiguration(dialect, profile);
        session.setAttribute("profileconfigurations",configurations);
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
    <carbon:breadcrumb label="update.view.configuration"
        resourceBundle="org.wso2.carbon.profiles.mgt.ui.i18n.Resources"
        topPage="false" request="<%=request%>" />

    <script type="text/javascript">
           
    </script>

    <div id="middle">
    <h2><fmt:message key='profile.configurations.for'/><%=profile%></h2>
    <div id="workArea">
    <form action="profile-edit-finish.jsp">
    <input type="hidden" name="profile" value="<%=profile%>"/>
    <input type="hidden" name="dialect" value="<%=dialect%>"/>
    <table class="styledLeft">
        <thead>
            <tr>
                <th width="50%"><fmt:message key='claim.uri'/></th>
                <th width="50%"><fmt:message key='behaviour'/></th>
            </tr>
        </thead>
        <tbody>
            <%
              if (configurations != null && configurations.length>0) {
                for (ClaimConfigurationDTO configuraion : configurations) {
                  if (configuraion != null) { 
                    String claimURI = configuraion.getClaimUri();
                    String behavior = configuraion.getBehavior();
            %>
            <tr>
                 <td width="50%"><%=claimURI%></td>
                 <td width="50%">
                    <select name="<%=claimURI%>">
                       <% if(behavior.equals("Inherited")){ %>
                       <option value="Inherited" selected="selected"><fmt:message key='inherited'/></option>
                       <%} else { %>
                       <option value="Inherited"><fmt:message key='inherited'/></option>
                       <%} %>
                       <% if(behavior.equals("Overridden")){ %>
                       <option value="Overridden" selected="selected"><fmt:message key='overridden'/></option>
                       <%} else { %>
                       <option value="Overridden"><fmt:message key='overridden'/></option>
                       <%} %>
                         <% if(behavior.equals("Hidden")){ %>
                       <option value="Hidden" selected="selected"><fmt:message key='hidden'/></option>
                       <%} else { %>
                       <option value="Hidden"><fmt:message key='hidden'/></option>
                       <%} %>
                    </select>
                  </td>
            </tr>
            <%     }
                }
              } else {
            %>       
            <tr><td colspan="2"><i><fmt:message key='no.configurations.available'/></i></td></tr>
            <%} %>
        <tr>
            <td class="buttonRow" colspan="2">
                 <input value="<fmt:message key='update'/>" type="submit" class="button" />
                 <input class="button" type="reset" value="<fmt:message key='cancel'/>"  onclick="javascript:document.location.href='index.jsp?region=region1&item=profile_mgt_menu&ordinal=0'"/ >
            </td>
        </tr>
        </tbody>
    </table>
    </form>
    </div>
    </div>
</fmt:bundle>