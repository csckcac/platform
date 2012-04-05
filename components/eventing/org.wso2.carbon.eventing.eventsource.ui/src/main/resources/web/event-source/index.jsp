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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@page import="org.wso2.carbon.eventing.eventsource.ui.EventingSourceAdminClient"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.eventing.eventsource.stub.types.carbon.EventSourceDTO" %>

<link href="../styles/main.css" rel="stylesheet" type="text/css" media="all"/>

<fmt:bundle basename="org.wso2.carbon.eventing.eventsource.ui.i18n.Resources">
<carbon:breadcrumb 
		label="event.source.management"
		resourceBundle="org.wso2.carbon.eventing.eventsource.ui.i18n.Resources"
		topPage="true" 
		request="<%=request%>" />

	<script type="text/javascript" src="global-params.js"></script>
    <script type="text/javascript" src="dscommon.js"></script>
	
	<%
		EventingSourceAdminClient client = null;
		String backendServerURL = null;
		ConfigurationContext configContext = null;
		String cookie = null;
	    EventSourceDTO[] eventSources = null;

		backendServerURL = CarbonUIUtil.getServerURL(config
				.getServletContext(), session);
		configContext = (ConfigurationContext) config.getServletContext()
				.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

		try {
			cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
			client = new EventingSourceAdminClient(cookie, backendServerURL,configContext);
			eventSources = client.getEventSources();			
		} catch (Exception e) {
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
            <script type="text/javascript">
                window.location.href = "../admin/error.jsp";
            </script>
<%
		}
	%>


	<div id="middle">
	<h2 id="eventsources"><fmt:message key="event.sources"/></h2>
	<div id="workArea">
	
	<script type="text/javascript">
		function editRow(name) {
    		document.location.href = "event_source_details.jsp?eventsource=" + name;
    	}
		function deleteRow(name) {
			CARBON.showConfirmationDialog('<fmt:message key="remove.confirmation"/>'+ name +" ?",
                    function() {
              	       location.href = "remove_eventsource.jsp?eventsource=" + name;
                     }, null);		 
		}
	</script>
	
	    <% if (eventSources != null && eventSources.length > 0) { %>
	
		<table class="styledLeft" width="100%" id="eventsourcestab">
		
			 <thead>
                    <tr>
                        <th><fmt:message key="th.event.source"/></th>
                        <th><fmt:message key="th.action"/></th>
                    </tr>
             </thead>
			
			<%			
				for (EventSourceDTO source : eventSources) {
					if (source != null) {					
			%>
			
			<tr>
				<td><%=source.getName()%></td>
				<td ><a href="#" class="icon-link" style="background-image:url(../admin/images/edit.gif);" onclick="editRow('<%=source.getName()%>')"><fmt:message key="eventsource.edit"/></a>
				     <a href="#" class="icon-link" style="background-image:url(../admin/images/delete.gif);" onclick="deleteRow('<%=source.getName()%>')"><fmt:message key="eventsource.delete"/></a>
				</td>
			</tr>
			<%
					}
				}
			%>				
	    </table>
	    
	    <script type="text/javascript">
        	alternateTableRows('eventsourcestab', 'tableEvenRow', 'tableOddRow');
        </script>
	    
	    <%
		  } else {    
		%>		
		 <div style="height:30px;">
             <fmt:message key="no.event.sources"/>
         </div>		
		<% } %>
	   
	     <div style="height:30px;">
                <a href="javascript:document.location.href='event_source.jsp'" class="icon-link"
                   style="background-image:url(../admin/images/add.gif);"><fmt:message key="add.new"/></a>
         </div>
	
	</div>
	</div>
	
</fmt:bundle>
