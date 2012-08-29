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
<%@ page import="org.wso2.carbon.logging.view.ui.LogViewerClient"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%-- <%@ page --%>
<!-- 	import="org.wso2.carbon.logging.view.stub.types.carbon.LogEvent"%> -->
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.logging.view.stub.types.carbon.PaginatedLogEvent"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.logging.view.stub.types.carbon.LogEvent"%>
<script type="text/javascript" src="js/logviewer.js"></script>
<script type="text/javascript" src="../admin/dialog/js/dialog.js"></script>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>View Application Logs</title>
</head>
<body>
	<%
		String backendServerURL = CarbonUIUtil
				.getServerURL(config.getServletContext(), session);
		ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
				.getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
		String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
		LogViewerClient logViewerClient;
		LogEvent[] events = null;
		String type;
		String keyword;
		String action;
		String pageNumberStr = request.getParameter("pageNumber");
		int pageNumber = 0;
		int numberOfPages = 0;
		PaginatedLogEvent paginatedLogEvents;
		String parameter = "";
		try {
			pageNumber = Integer.parseInt(pageNumberStr);
		} catch (NumberFormatException ignored) {
			// page number format exception
		}
		String appName;
		String applicationNames[];
		try {
			type = CharacterEncoder.getSafeText(request.getParameter("type"));
			type = (type == null) ? "":type;
			keyword = CharacterEncoder.getSafeText(request.getParameter("keyword"));
			keyword = (keyword == null )? "":keyword;
			action = CharacterEncoder.getSafeText(request.getParameter("action"));
			appName = CharacterEncoder.getSafeText(request.getParameter("appName"));
			logViewerClient = new LogViewerClient(cookie, backendServerURL, configContext);
			applicationNames = logViewerClient.getApplicationNames();
			
			if (appName == null && applicationNames !=null  && applicationNames.length > 0) {
				appName = applicationNames[0];
			}
			if (applicationNames == null) {
				applicationNames = new String[]{"No applications"};
				appName = applicationNames[0];
				}
			paginatedLogEvents = logViewerClient.getPaginatedApplicationLogEvents(pageNumber,type,keyword,appName);
			if (paginatedLogEvents != null) {
				events = paginatedLogEvents.getLogInfo();
				numberOfPages = paginatedLogEvents.getNumberOfPages();
			}
			
			parameter = "type=" + type + "&keyword=" + keyword+ "&appName="+appName;
		} catch (Exception e) {
			CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request,
					e);
	%>
	<script type="text/javascript">
		location.href = "../admin/error.jsp";
	</script>
	<%
		return;
		}
	%>
	<fmt:bundle basename="org.wso2.carbon.logging.view.ui.i18n.Resources">

		<carbon:breadcrumb label="app.logs"
			resourceBundle="org.wso2.carbon.logging.ui.i18n.Resources"
			topPage="true" request="<%=request%>" />
		<div id="middle">
			<h2>
				<fmt:message key="app.logs" />
			</h2>
			<div id="workArea">
			
			
			
		      <table border="0" class="styledLeft">
                        <tbody>
                        <tr>
                            <td>
                            <table class="normal">
                            <tr>
                            <td><fmt:message key="view"/></td>
                            <td><select class="log-select" id="logLevelID"
                                        onchange="javascript:viewApplicationLogs(); return false;">
                                <%
                                	String[] logLevels = logViewerClient.getLogLevels();
                                	if (keyword != null && !keyword.equals("")) {
                                			type = "Custom";
                                %>

                                <option value="<%=type%>" selected="true"><%=type%></option>

                                <%
                                	}
                                		for (String logLevel : logLevels) {
                                			if (logLevel.equals(type)) {
                                %>

                                <option value="<%=logLevel%>" selected="true"><%=logLevel%></option>

                                <%
                                	} else {
                                %>

                                <option value="<%=logLevel%>"><%=logLevel%></option>

                                <%
                                	}
                                		}
                                %>
                            </select></td>
                            	<td ><select
										name="appName" id="appName"  onchange="javascript:viewApplicationLogs(); return false;">
											<%
												for (String name : applicationNames) {
											%>
											<%
												    if (name.equals(appName)) {
											%>
											<option selected="selected" value="<%=name%>">
												<%=name%>
											</option>
											<%
												} else {
											%>
											<option value="<%=name%>">
												<%=name%>
											</option>
											<%
												}
											%>
											<%
											    }
											    %>

									</select></td>
                            <td style="width: 100%;"></td>
                            <td>
                                <nobr><fmt:message key="search.logs"/></nobr>
                            </td>
                                <td style="padding-right: 2px !important;">
                                    <input onkeypress="appSubmitenter(event)" value="" class="log-select"
                                        size="40" id="logkeyword" type="text"></td>
                                <td style="padding-left: 0px !important;"><input type="button"
                                                                                 value="Search"
                                                                                 onclick="javascript:searchAppLogs(); return false;"
                                                                                 class="button">
                                </td>
                            </tr>
                        </table>
                        </td>
                        </tr>
                        </tbody>
                    </table>


                   <br/>
                   
			<table border="1" class="styledLeft">
		
				<tbody>

					<tr>
						<td class="formRow">

							<table  class="styledLeft">
							<thead>
									<tr>
										<th><b><fmt:message key="log.type" />
										</b>
										</th>
										<th><b><fmt:message key="date" />
										</b>
										</th>
										<th colspan="2"><b><fmt:message key="log.message" />
										</b>
										</th>
									</tr>
								</thead>
							<%
								if (events == null || events.length == 0 || events[0] == null) {
							%>
								 <fmt:message key="no.logs" /> 
							<%
 								} else {
 										int index = 0;
 										for (LogEvent logMessage : events) {
 											index++;
 											if (index % 2 != 0) {
 							%>
								<tr>
									<%
										} else {
									%>
								
								<tr bgcolor="#eeeffb">
									<%
										}
									%>
								   <td border-bottom="gray" width="2%"><img
										style="margin-right: 10px;"
										src="<%=logViewerClient.getImageName(logMessage.getPriority().trim())%>">
									</td>
									<td><nobr><%=logMessage.getLogTime()%></nobr></td>
									<td><%=logMessage.getMessage()%></td>
										<%
											String imgId = "traceSymbolMax" + index;
										%>
									<td><a
											class="icon-link"
											style="background-image: url(images/plus.gif);"
											href="javascript:showTrace(<%=index%>)"
											id="<%=imgId%>"></a> <fmt:message
												key="view.stack.trace" /></td>
								</tr>
								
							<%
																String id = "traceTable" + index;
																			if (index % 2 != 0) {
															%>
									<tr id="<%=id%>" style="display: none" >
									<%
										} else {
									%>
								
									<tr id="<%=id%>" style="display: none" bgcolor="#eeeffb">
									<%
										}
									%>

                                    <td colspan="4" width="100%">TID[<%=logMessage.getTenantId()%>] AppID[<%=logMessage.getAppName()%>] [<%=logMessage.getServerName()%>] [<%=logMessage.getLogTime()%>] <%=logMessage.getPriority().trim()%> {<%=logMessage.getLogger()%>} - <%=logMessage.getMessage()%>
                                        <%=logMessage.getStacktrace()%><br/>
                                    </td>
									</tr>
							<%
								}
									}
							%>
							
							</table>
							 <carbon:paginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                      page="application_log_viewer.jsp" pageNumberParameterName="pageNumber"
                      prevKey="prev" nextKey="next"
                      parameters="<%= parameter%>"/>
					</tr>
					
				</tbody>
			</table>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</fmt:bundle>
</body>
</html>
