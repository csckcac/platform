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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<link href="../tenant-dashboard/css/dashboard-common.css" rel="stylesheet" type="text/css" media="all"/>
<%
        Object param = session.getAttribute("authenticated");
        String passwordExpires = (String) session.getAttribute(ServerConstants.PASSWORD_EXPIRATION);
        boolean hasPermission = CarbonUIUtil.isUserAuthorized(request,
		"/permission/admin/manage/mediation");
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
    .tip-table td.apiproection {
        background-image: url(../../carbon/tenant-dashboard/images/api-protection.png);
    }
    .tip-table td.pvtdata {
        background-image: url(../../carbon/tenant-dashboard/images/private-data-access.png);
    }

    .tip-table td.outsourcecomp {
        background-image: url(../../carbon/tenant-dashboard/images/outsource-computations.png);
    }

</style>
 <h2 class="dashboard-title">WSO2 CSG Quick Start Dashboard</h2>
        <table class="tip-table">
            <tr>
                <td class="tip-top apiproection"></td>
                <td class="tip-empty "></td>
                <td class="tip-top pvtdata"></td>
                <td class="tip-empty "></td>
                <td class="tip-top outsourcecomp"></td>
            </tr>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                   	   <%
							if (hasPermission) {
						%>
                        <a class="tip-title" href="#">API protection</a><br/>
                        <%
							} else {
						%>
						<h3>API protection</h3><br/>
						<%
							}
						%>
                        <p>Protect services published to outside world from exploits and attacks using schema validations, WS-security and throttling.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                         <%
							if (hasPermission) {
						%>
                        <a class="tip-title" href="#">Private Data Access</a> <br/>
                         <%
							} else {
						%>
						<h3>Private Data Access</h3><br/>
						<%
							}
						%>
                        <p>Selectively publish data stored in private networks to public cloud through services in a secured and controlled manner.</p>

                    </div>
                </td>
                <td class="tip-empty"></td>
                <td class="tip-content">
                    <div class="tip-content-lifter">
                     <%
							if (hasPermission) {
						%>
                        <a class="tip-title" href="#">Outsource Computations</a> <br/>
						  <%
							} else {
						%>
						<h3>Outsource Computations</h3> <br/>
							<%
							}
						%>
                        <p>Make use of computational resources hosted outside the enterprise with data/resources reside on premises in a secure and controlled manner.</p>

                    </div>
                </td>
            </tr>
            <tr>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
                <td class="tip-empty"></td>
                <td class="tip-bottom"></td>
            </tr>
        </table>
	<div class="tip-table-div"></div>  
     </div>
</div>
