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
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>


<%@page import="java.lang.Exception"%>
<%@page import="org.wso2.carbon.claim.mgt.ui.client.ClaimAdminClient"%>
<%@page import="org.wso2.carbon.claim.mgt.stub.dto.ClaimDialectDTO"%>

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String forwardTo = null;
    ClaimDialectDTO[] claimMappping = null;
    String dialectUri = request.getParameter("dialect");
	String BUNDLE = "org.wso2.carbon.claim.mgt.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    
    try {
        ClaimAdminClient client = new ClaimAdminClient(cookie, serverURL, configContext);
        claimMappping = client.getAllClaimMappings();
        session.setAttribute("claimMappping",claimMappping);
    } catch (Exception e) {
    	String message = resourceBundle.getString("error.while.loading.claim.mappings");
    	CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "../admin/error.jsp";
%>

<%@page import="java.util.ResourceBundle"%>
<%@ page import="org.wso2.carbon.claim.mgt.stub.dto.ClaimMappingDTO" %>
<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
<%
    }
%>


<fmt:bundle basename="org.wso2.carbon.claim.mgt.ui.i18n.Resources">
	<carbon:breadcrumb label="claim.view"
		resourceBundle="org.wso2.carbon.claim.mgt.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />

	<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
	<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
	<script type="text/javascript" src="../carbon/admin/js/main.js"></script>
    	<script type="text/javascript">
                jQuery(document).ready(function() {

                    jQuery(".toggle_container").hide();
                    /*Hide (Collapse) the toggle containers on load use show() insted of hide() 	in the above code if you want to keep the content section expanded. */

                    jQuery("a.trigger-title").click(function() {
                        if (jQuery(this.parentNode).next().is(":visible")) {
                            this.parentNode.className = "active trigger";
                        } else {
                            this.parentNode.className = "trigger";
                        }

                        jQuery(this.parentNode).next().slideToggle("fast");
                        return false; //Prevent the browser jump to the link anchor
                    });
                });
        </script>
    <style type="text/css">
        .editLink {
            background: transparent url(../admin/images/edit.gif) no-repeat 0px 0px !important;
            float: right !important;
            padding: 0px 0px 0px 20px !important;
            color: inherit !important;
            font-size: 12px !important;
            line-height: 20px !important;
            margin-right: 10px !important;
            margin-bottom: 5px !important;
        }
    </style>
    <div id="middle">
	<h2><fmt:message key='available.claims.for'/><%=dialectUri%></h2>
	<div id="workArea">
	
	<div style="height:30px;">
                <a href="javascript:document.location.href='add-claim.jsp?dialect=<%=dialectUri%>'" class="icon-link"
                   style="background-image:url(../admin/images/add.gif);"><fmt:message key='add.new.claim.mapping'/></a>
    </div>


	<%for (int i=0; i<claimMappping.length;i++ ){ 
        	 if (claimMappping[i].getDialectURI().equals(dialectUri)) {
        	 ClaimMappingDTO[] claims =  claimMappping[i].getClaimMappings();
           %>


		<% for (int j=0; j<claims.length;j++ ) {             
              if (claims[j].getClaim().getDisplayTag()!=null) {%>
        <h2 class="trigger active">
            <a href="#" class="trigger-title"><%=claims[j].getClaim().getDisplayTag()%></a>
            <a href="update-claim.jsp?dialect=<%=dialectUri%>&claimUri=<%=claims[j].getClaim().getClaimUri()%>" class="editLink icon-link">Edit</a>
        </h2>
        <div class="toggle_container">
        <table style="width: 100%" class="styledLeft">
		<tbody>
			<tr>
				<td class="leftCol-small"><fmt:message key='description'/></td>
				<td><%=claims[j].getClaim().getDescription()%></td>
			</tr>

			<tr>
				<td class="leftCol-small"><fmt:message key='claim.uri'/></td>
				<td><%=claims[j].getClaim().getClaimUri()%></td>
			</tr>

			<tr>
				<td class="leftCol-small"><fmt:message key='mapped.attribute'/></td>
				<td><%=claims[j].getMappedAttribute()%></td>
			</tr>

			<tr>
				<td class="leftCol-small"><fmt:message key='regular.expression'/></td>
				<td><%=claims[j].getClaim().getRegEx()%></td>
			</tr>
            <tr>
				<td class="leftCol-small"><fmt:message key='display.order'/></td>
				<td><%=claims[j].getClaim().getDisplayOrder()%></td>
			</tr>
			<tr>
				<td class="leftCol-small"><fmt:message key='supported.by.default'/></td>
				<%if (claims[j].getClaim().getSupportedByDefault()) { %>
				<td>true</td>
				<% } else { %>
				<td>false</td>
				<%} %>
			</tr>

			<tr>
				<td class="leftCol-small"><fmt:message key='required'/></td>
				<%if (claims[j].getClaim().getRequired()) { %>
				<td>true</td>
				<% } else { %>
				<td>false</td>
				<%} %>
			</tr>

		</tbody>
            </table>
        </div>
		<%}}%>

	<%} }%>
	</div>
	</div>
</fmt:bundle>
