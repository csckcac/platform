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
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.util.ResourceBundle" %>
<%@page import="java.lang.Exception"%>

<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />
<%
    entitlementPolicyBean.removeBasicRuleElements();
    entitlementPolicyBean.setBasicTargetElementDTO(null);
    entitlementPolicyBean.setRuleElementOrder(null);    
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.
                    CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String forwardTo = null;
    String[] algorithmNames = null;
    entitlementPolicyBean.setCurrentRuleId(null);
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {
        EntitlementPolicyAdminServiceClient client = new EntitlementPolicyAdminServiceClient(cookie,
                serverURL, configContext);
        algorithmNames = client.getEntitlementPolicyDataFromRegistry("ruleCombiningAlgorithms");
        entitlementPolicyBean.setFunctionIds(client.getEntitlementPolicyDataFromRegistry("functionId"));
        entitlementPolicyBean.setMatchIds(client.getEntitlementPolicyDataFromRegistry("matchId"));
        entitlementPolicyBean.setMustBePresentValues(client.
                getEntitlementPolicyDataFromRegistry("mustBePresent"));
        entitlementPolicyBean.setRuleEffectValues(client.
                getEntitlementPolicyDataFromRegistry("ruleEffect"));
        entitlementPolicyBean.setDataTypes(client.getEntitlementPolicyDataFromRegistry("dataType"));

    } catch (Exception e) {
    	String message = resourceBundle.getString("error.while.loading.policy.resource");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "../admin/error.jsp";

%>
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

<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
	<carbon:breadcrumb label="add.policy.Element"
		resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />

	<div id="middle">
	<h2><fmt:message key='entitlement.policy.creation'/></h2>
	<div id="workArea">
    <%
        if(entitlementPolicyBean.isEditPolicy()){
    %>
        <h3 class="page-subtitle"><fmt:message key='edit.policy.element'/></h3>
    <%
        } else {
    %>
        <h3 class="page-subtitle"><fmt:message key='add.policy.element'/></h3>
    <%
        }
    %>

	   <script type="text/javascript">

        function doValidation() {

        	var value = document.getElementsByName("policyName")[0].value;
        	if (value == '') {
            	CARBON.showWarningDialog('<fmt:message key="policy.name.is.required"/>');
            	return false;
        	}
        	return true;
    	}

        function doCancel() {
            location.href = 'index.jsp?';
        }

        function doNext() {
            document.createPolicy.action = "add-target.jsp";
            if (doValidation()) {
                document.createPolicy.submit();
            }
        }

        function doFinish() {
            document.createPolicy.action = "finish.jsp";
            if (doValidation()) {
                document.createPolicy.submit();
            }
        }
        
  	   </script>

    <form name="createPolicy" action="add-target.jsp" method="post">
    	<table style="width: 100%" class="styledLeft">
		<thead>
			<tr>
                <th colspan="2"></th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td class="formRow">
					<table class="normal" cellspacing="0">
	      		    	<tr>
							<td class="leftCol-med"><fmt:message key='policy.name'/><span class="required">*</span></td>
							<td>
                                <%
                                    if(entitlementPolicyBean.getPolicyName() != null){
                                %>
                                <input type="text" name="policyName" id="policyName" class="text-box-big" value="<%=entitlementPolicyBean.getPolicyName()%>"/>
                                <%
                                    } else {
                                %>
                                <input type="text" name="policyName" id="policyName" class="text-box-big"/>
                                <%
                                    }
                                %>
                            </td>
						</tr>
                        <tr>
                        <td><fmt:message key="rule.combining.algorithm"/></td>
                        <td>
                            <select id="algorithmName" name="algorithmName">
                        <%
                            if (algorithmNames != null && algorithmNames.length > 0) {
                                for (String algorithmName : algorithmNames) {
                                    if(algorithmName.equals(entitlementPolicyBean.getAlgorithmName())){
                        %>
                                <option value="<%=algorithmName%>" selected="selected"><%=algorithmName%></option>
                        <%
                                    } else {
                        %>
                                <option value="<%=algorithmName%>"><%=algorithmName%></option>
                        <%
                                    }
                                }
                            }
                        %>
                            </select>
                        </td>
                        </tr>
	      		    	<tr>
							<td><fmt:message key='policy.description'/></td>
                            <td>
                            <%
                                if(entitlementPolicyBean.getPolicyDescription() != null){
                            %>
                                <input type="text" name="policyDescription" id="policyDescription" class="text-box-big" value="<%=entitlementPolicyBean.getPolicyDescription()%>"/>
                            <%
                                } else {
                            %>
                                <input type="text" name="policyDescription" id="policyDescription" class="text-box-big"/>
                            <%
                                }
                            %>
                            </td>
						</tr>
					</table>
				</td>
			</tr>
		</tbody>	
	</table>

    <table class="styledLeft" style="margin-top:20px;">
        <tbody>
        <tr>
            <td colspan="2" class="buttonRow">
                      <input class="button" type="button" value="<fmt:message key='next'/>" onclick="doNext();"/>
                      <input class="button" type="button" value="<fmt:message key='finish'/>" onclick="doFinish();"/>
                      <input class="button" type="button" value="<fmt:message key='cancel'/>"  onclick="doCancel();"/>
            </td>
        </tr>
        </tbody>
    </table>    
	</form>
	</div>	
	</div>
</fmt:bundle>