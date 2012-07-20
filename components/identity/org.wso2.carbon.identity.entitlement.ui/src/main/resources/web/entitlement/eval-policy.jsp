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

<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyCreator" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.RowDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.RequestElementDTO" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<jsp:include page="../dialog/display_messages.jsp"/>
<jsp:include page="../highlighter/header.jsp"/>

<%

    String policyRequest = (String)session.getAttribute("txtRequest");
    session.removeAttribute("policyreq");
    String forwardTo = null;
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    List<RowDTO> rowDTOs = new ArrayList<RowDTO>();
    String resourceNames = CharacterEncoder.getSafeText(request.getParameter("resourceNames"));
    String subjectNames = CharacterEncoder.getSafeText(request.getParameter("subjectNames"));
    String actionNames = CharacterEncoder.getSafeText(request.getParameter("actionNames"));
    String environmentNames = CharacterEncoder.getSafeText(request.getParameter("environmentNames"));

    if (resourceNames != null  && !resourceNames.trim().equals("")){
        RowDTO rowDTO = new RowDTO();
        rowDTO.setAttributeValue(resourceNames);
        rowDTO.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
        rowDTO.setAttributeId("urn:oasis:names:tc:xacml:1.0:resource:resource-id");
        rowDTO.setCategory("urn:oasis:names:tc:xacml:3.0:attribute-category:resource");
        rowDTOs.add(rowDTO);
        session.setAttribute("resourceNames",resourceNames);
    }
    if (subjectNames != null  && !subjectNames.trim().equals("")){
        RowDTO rowDTO = new RowDTO();
        rowDTO.setAttributeValue(subjectNames);
        rowDTO.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
        rowDTO.setAttributeId("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
        rowDTO.setCategory("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject");
        rowDTOs.add(rowDTO);
        session.setAttribute("subjectNames",subjectNames);
    }
    if (actionNames != null  && !actionNames.trim().equals("")){
        RowDTO rowDTO = new RowDTO();
        rowDTO.setAttributeValue(actionNames);
        rowDTO.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
        rowDTO.setAttributeId("urn:oasis:names:tc:xacml:1.0:action:action-id");
        rowDTO.setCategory("urn:oasis:names:tc:xacml:3.0:attribute-category:action");
        rowDTOs.add(rowDTO);
        session.setAttribute("actionNames",actionNames);
    }
    if (environmentNames != null  && !environmentNames.trim().equals("")){
        RowDTO rowDTO = new RowDTO();
        rowDTO.setAttributeValue(environmentNames);
        rowDTO.setAttributeDataType(EntitlementPolicyConstants.STRING_DATA_TYPE);
        rowDTO.setAttributeId("urn:oasis:names:tc:xacml:1.0:environment:environment-id");
        rowDTO.setCategory("urn:oasis:names:tc:xacml:3.0:attribute-category:environment");
        rowDTOs.add(rowDTO);
        session.setAttribute("environmentNames",environmentNames);
    }

    RequestElementDTO requestElementDTO = new RequestElementDTO();
    requestElementDTO.setRowDTOs(rowDTOs);

    EntitlementPolicyCreator entitlementPolicyCreator = new EntitlementPolicyCreator();

    try {
        if(policyRequest != null && policyRequest.trim().length() > 0){
            policyRequest = policyRequest.trim().replaceAll("><", ">\n<");
        } else {
            String createdRequest = entitlementPolicyCreator.createBasicRequest(requestElementDTO);
            if(createdRequest != null && createdRequest.trim().length() > 0){
                policyRequest = createdRequest.trim().replaceAll("><", ">\n<");
            }
        }
    } catch (Exception e) {
    	String message = resourceBundle.getString("error.while.loading.policy.resource");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "../admin/error.jsp";

%>

<script type="text/javascript">
    forward();
</script>
<%
    }
%>
<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
	<carbon:breadcrumb label="eval.policy"
		resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />

	<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
	<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
	<script type="text/javascript" src="../carbon/admin/js/main.js"></script>
    <script src="../editarea/edit_area_full.js" type="text/javascript"></script>
    <script type="text/javascript">
        jQuery(document).ready(function(){
            editAreaLoader.init({
                id : "txtRequestTemp"
                ,syntax: "xml"
                ,start_highlight: true
            });
        });

    </script>

    
    <script type="text/javascript">
        function validateRequest() {
           var value = document.getElementById("txtRequest").value;
           if (value == '') {
               CARBON.showWarningDialog("<fmt:message key='empty.request'/>");
               return false;
           }
           return true;
        }

        function forward() {
           location.href = "<%=forwardTo%>";
        }

        function evaluateXACMLRequest(){
            if(validateRequest()){
                document.getElementById("txtRequest").value = editAreaLoader.getValue("txtRequestTemp");
                document.evaluateRequest.submit();
            }
        }
    </script>


	<div id="middle">
	<h2><fmt:message key='eval.ent.policy'/></h2>
	<div id="workArea">
    <form name="evaluateRequest" id="evaluateRequest" action="eval-policy-submit.jsp" method="post">
	<table style="width: 100%" class="styledLeft">
	
		<thead>
			<tr>
				<th><fmt:message key='ent.eval.policy.request'/></th>
			</tr>
		</thead>
		<tbody>
        <tr>
            <td>
                <div>
                <textarea id="txtRequestTemp" name="txtRequestTemp" rows="30" cols="120"><%=policyRequest%>
                </textarea>
                <textarea name="txtRequest" id="txtRequest" style="display:none"><%=policyRequest%></textarea>
                <input type="hidden" id="forwardTo" name="forwardTo" value="eval-policy.jsp" />
                </div>
            </td>
        </tr>
        <tr>
            <td class="buttonRow">
                <input type="button" value="<fmt:message key='evaluate'/>" class="button" onclick="evaluateXACMLRequest();"/>
                <input class="button" type="reset" value="<fmt:message key='cancel'/>"  onclick="javascript:document.location.href='create-evaluation-request.jsp?region=region1&item=policy_tryit_menu'"/>
            </td>
        </tr>
		</tbody>	
	</table>
	</form>
	</div>	
	</div>
</fmt:bundle>
