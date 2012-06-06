<!--
 ~ Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
        <%@ page import="org.apache.axis2.context.ConfigurationContext" %>
        <%@ page import="org.wso2.carbon.CarbonConstants" %>
      	<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
		<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
        <%@ page import="org.wso2.carbon.utils.ServerConstants" %>
        <%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
        <%@ page import="org.wso2.carbon.utils.multitenancy.CarbonContextHolder" %>
        <%@ page import="org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext" %>
        <script type="text/javascript" src="js/mapping_validator.js"></script>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
        <%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<!--         <script type="text/javascript" src="../admin/dialog/js/dialog.js"></script> -->
        <%@ page import="org.wso2.carbon.utils.CarbonUtils" %>
        <%@ page import="org.wso2.carbon.url.mapper.ui.UrlMapperServiceClient" %>
<%
	String requestType = request.getParameter("type");
	String carbonEndpoint = request.getParameter("carbonEndpoint");
	String backendServerURL = CarbonUIUtil
			.getServerURL(config.getServletContext(), session);
	ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
			.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
	String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	
	UrlMapperServiceClient hostAdmin = new UrlMapperServiceClient(cookie, backendServerURL,
			configContext);
	String hosts[];
	String referance ="";
	try {
		if (carbonEndpoint.contains("services")) {
			String urlParts[] = carbonEndpoint.split(":\\d{4}");
			if (urlParts.length>1) {
				referance = urlParts[1];
			}
			hosts = hostAdmin.getHostForEpr(carbonEndpoint);
		} else {
			hosts = hostAdmin.getHostForWebApp(carbonEndpoint);
			referance = carbonEndpoint;
		}
	} catch (Exception e) {
		CarbonUIMessage.sendCarbonUIMessage(e.getLocalizedMessage(), CarbonUIMessage.ERROR,
				request, e);
%>
<script type="text/javascript">
               location.href = "../admin/error.jsp";
        </script>
<%
	return;
	}
%>

<fmt:bundle basename="org.wso2.carbon.url.mapper.ui.i18n.Resources"> 
	<carbon:breadcrumb label="url.mapping"
		resourceBundle="org.wso2.carbon.url.mapper.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />
		<div id="middle">
		<script type="text/javascript">
			function    showSucessMessage(msg,myepr) {
				var failMsg = new RegExp("Failed to add URL Mapping.");
            	if (msg.match(failMsg)) //if match sucess 
				{	
            		CARBON.showErrorDialog(msg);
				} else {
            		CARBON.showInfoDialog(msg, function(){
   		  				document.location.href = "index.jsp?&carbonEndpoint=" + myepr;  				 
   		  			 });
				}
			}
		</script>
		<script type="text/javascript">
		  
   function add(myepr){
        CARBON.showInputDialog("Enter URL Mapping name :\n",function(inputVal){
            var reason = checkMappingAvailability(inputVal);
            if(reason == "") {
                jQuery.ajax({
                                type: "POST",
                                url: "contextMapper_ajaxprocessor.jsp",
                                data: "type=add&carbonEndpoint=" + myepr + "&userEndpoint=" + inputVal + "&endpointType=Endpoint_1",
                                success: function(msg){
                                    showSucessMessage(msg,myepr);
                                }
                            });
            } else {
                CARBON.showWarningDialog(reason);
            }
        });
    }   
</script> 

 <script type="text/javascript">
   function edit(myepr,host){
        CARBON.showInputDialog("Enter URL Mapping name :\n",function(inputVal){
        var reason = checkMappingAvailability(inputVal);
        if(reason == "") {
            jQuery.ajax({
                            type: "POST",
                            url: "contextMapper_ajaxprocessor.jsp",
                            data: "type=edit&carbonEndpoint=" + myepr + "&userEndpoint=" + inputVal +  "&oldHost=" + host + "&endpointType=Endpoint_1",
                            success: function(msg){
                            	showSucessMessage(msg,myepr);	
                            }
                        });
        } else {
            CARBON.showWarningDialog(reason);
        }
        });
    }   
</script> 
 <script type="text/javascript">
   function deleteHost(myepr,host){
	   CARBON.showConfirmationDialog('<fmt:message key="select.webapps.to.be.deleted"/>',function(){
            jQuery.ajax({
                            type: "POST",
                            url: "contextMapper_ajaxprocessor.jsp",
                            data: "type=delete&carbonEndpoint=" + myepr +"&userEndpoint=" + host + "&endpointType=Endpoint_1",
                            success: function(msg){
                            	showSucessMessage(msg,myepr);
                            }
                        });
        });
    }   
</script> 
		
		<h2>
			<fmt:message key="url.mapping" />
		</h2>
		<div id="workArea">
		<b><fmt:message
					key="url.mapping.for" /> <%=referance%>
			</b><br/><br/>
		
						 <%   if (hosts == null || hosts.length == 0) {
						  %>
								<fmt:message key="no.mappings.found" />
									<%
										} else {
									%>
							<table class="styledLeft">
								<thead>
									<tr>
										<th><b><fmt:message key="host.name" /> </b></th>
										<th><b><fmt:message key="action" /> </b></th>
									</tr>
								</thead>
								<%
									int index = -1;
										for (String host : hosts) {
											++index;
											if (index % 2 != 0) {
								%>
								<tr>
									<%
										} else {
									%>		
								<tr bgcolor="#eeeffb">
									<%
										}
												if (hosts == null || hosts.length == 0) {
									%>
									<td colspan="2"><fmt:message key="no.mappings.found" /></td>
									<%
										} else {
									%>
									<td><%=host%></td>
									<td>
										<a class="icon-link"
										style="background-image: url(images/edit.gif);"
										onclick="edit('<%=carbonEndpoint%>','<%=host%>');"
										title="Edit"><fmt:message key="edit" /></a> <a class="icon-link"
										style="background-image: url(images/delete.gif);"
										onclick="deleteHost('<%=carbonEndpoint%>','<%=host%>');"
										title="Delete"><fmt:message key="delete" /></a>							     
									</td>
									<%
										}
									%>
								</tr>
								<%
									} 
										}
								%>
							
						
                <tr>             
                    <td td colspan="2">
                        <a class="icon-link"
                           style="background-image:url(images/add.gif);" onclick="add('<%=carbonEndpoint%>');" title="Add Service Specific Url">
                           Add New Mapping
                        </a>
                    </td>
                </tr>    
             </table>
		
		 </div></div>
</fmt:bundle>