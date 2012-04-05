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
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<jsp:include page="../dialog/display_messages.jsp"/>

<link href="../tenant-dashboard/css/dashboard-common.css" rel="stylesheet" type="text/css" media="all"/>
<%
        Object param = session.getAttribute("authenticated");
        String passwordExpires = (String) session.getAttribute(ServerConstants.PASSWORD_EXPIRATION);
        boolean hasPermissionService = CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/manage/add/service");

        boolean hasPermissionTask = CarbonUIUtil.isUserAuthorized(request,
            "/permission/admin/configure/tasks");

        boolean loggedIn = false;
        if (param != null) {
            loggedIn = (Boolean) param;             
        } 
%>
  
<div id="passwordExpire">
         <%
         if (loggedIn && passwordExpires != null) {
         %>
              <div class="info-box"><p>Your password expires at <%=passwordExpires%>. Please change by visiting <a href="../user/change-passwd.jsp?isUserChange=true&returnPath=../admin/index.jsp">here</a></p></div>
         <%
             }
         %>
</div>
<div id="middle">
    <div id="workArea">
        <style type="text/css">
            .tip-table td.scrape-the-web {
                background-image: url(../../carbon/tenant-dashboard/images/scrape-the-web.png);
            }

            .tip-table td.compose-and-expose {
                background-image: url(../../carbon/tenant-dashboard/images/compose-and-expose.png);
            }

            .tip-table td.schedule-tasks {
                background-image: url(../../carbon/tenant-dashboard/images/schedule-tasks.png);
            }

            .tip-table td.service-testing {
                background-image: url(../../carbon/tenant-dashboard/images/service-testing.png);
            }



            .tip-table td.message-tracing {
                background-image: url(../../carbon/tenant-dashboard/images/message-tracing.png);
            }
            .tip-table td.js-stub {
                background-image: url(../../carbon/tenant-dashboard/images/js-stub.png);
            }
            .tip-table td.modules {
                background-image: url(../../carbon/tenant-dashboard/images/modules.png);
            }
			.tip-table td.cachingandthrottling {
				background-image: url(../../carbon/tenant-dashboard/images/cachingandthrottling.png);
			}

        </style>
        <h2 class="dashboard-title">WSO2 Mashup Server quick start dashboard</h2>
        <table class="tip-table">
            <tr>
                <td class="tip-top scrape-the-web"></td>
                <td class="tip-empty"></td>
                <td class="tip-top compose-and-expose"></td>
                <td class="tip-empty "></td>
                <td class="tip-top schedule-tasks"></td>
                <td class="tip-empty "></td>
                <td class="tip-top service-testing"></td>
            </tr>
            <tr>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <%
                            if (hasPermissionService) {
                        %>
                        <a class="tip-title" href="../js_scraper/index.jsp?region=region5&item=js_scraper_menu">Scrape
                            the Web</a> <br/>
                        <%
                        } else {
                        %>
                        <h3 class="tip-title">Scrap the Web<br/></h3>
                        <%
                            }
                        %>

                        <p>Scrape legacy web pages, expose as SOAP/REST services quickly using Scraping Assistant</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <%
                            if (hasPermissionService) {
                        %>
                        <a class="tip-title" href="../js_service/newMashup.jsp?region=region1&item=js_create_menu">Compose
                            and Expose</a><br/>
                        <%
                        } else {
                        %>
                        <h3 class="tip-title">Compose and Expose<br/></h3>
                        <%
                            }
                        %>

                        <p>Compose Enterprise level mashups quickly using Javascript and expose as SOAP/REST Web
                            Services with ease</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <%
                            if (hasPermissionTask) {
                        %>
                        <a class="tip-title" href="../task/index.jsp?region=region1&item=tasks_menu">Schedule Tasks</a>
                        <br/>
                        <%
                        } else {
                        %>
                        <h3 class="tip-title">Schedule Tasks<br/></h3>
                        <%
                            }
                        %>

                        <p>Schedule recurring tasks, get updated via Emails and IMs using Email/IM Hostobjects.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <%
                            if (hasPermissionService) {
                        %>
                        <a class="tip-title" href="../tryit/index.jsp?region=region5&item=tryit">Service Testing</a><br/>
                        <%
                        } else {
                        %>
                        <h3 class="tip-title">Service Testing<br/></h3>
                        <%
                            }
                        %>

                        <p>Tryit tool can be used as a simple Web Service client which can be used to try your
                            services within BRS itself.</p>

                    </div>
                </td>
            </tr>
            <tr>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
            </tr>
        </table>
	<div class="tip-table-div"></div>

        <table class="tip-table">
            <tr>
                <td class="tip-top message-tracing"></td>
                <td class="tip-empty"></td>
                <td class="tip-top js-stub"></td>
                <td class="tip-empty "></td>
                <td class="tip-top cachingandthrottling"></td>
                <td class="tip-empty "></td>
                <td class="tip-top modules"></td>
            </tr>
            <tr>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <%
                            if (hasPermissionService) {
                        %>
                        <a class="tip-title" href="../tracer/index.jsp?region=region4&item=tracer_menu">Message
                            Tracing</a><br/>
                        <%
                        } else {
                        %>
                        <h3 class="tip-title">Message Tracing<br/></h3>
                        <%
                            }
                        %>

                        <p>Trace the request and responses to your service. Message Tracing is a vital debugging tool
                            when
                            you have clients from heterogeneous platforms.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <h3 class="tip-title">Javascript Stubs</h3><br/>

                        <p>Create Javascript stubs from a WSDL and invoke SOAP/REST services easily from a mashup or even from the browser.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <h3 class="tip-title">Caching & Throttling</h3> <br/>

                        <p>Caching and Throttling for Business processes</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                        <%
                            if (hasPermissionService) {
                        %>
                        <a class="tip-title" href="../modulemgt/index.jsp?region=region1&item=modules_list_menu">Modules</a> <br/>
                        <%
                        } else {
                        %>
                        <h3 class="tip-title">Modules<br/></h3>
                        <%
                            }
                        %>

                        <p>The WSO2 SOA platform has the capabilities of Axis2 to add modules to extend its capabilities. The global modules will affect all the services deployed within the server. </p>

                    </div>
                </td>
            </tr>
            <tr>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
            </tr>
        </table>
        <p>
            <br/>
        </p></div>
</div>
