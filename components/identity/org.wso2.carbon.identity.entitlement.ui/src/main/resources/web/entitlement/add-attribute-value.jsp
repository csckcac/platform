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
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.AttributeValueElementDTO" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />   
<%

    AttributeValueElementDTO attributeValueElementDTO = new AttributeValueElementDTO();
    String applyElementId = CharacterEncoder.getSafeText(request.getParameter("applyElementId"));
    String attributeValueElementId = CharacterEncoder.getSafeText(request.
            getParameter("attributeValueElementId"));
    String editAttributeValueElement = CharacterEncoder.getSafeText(request.
            getParameter("editAttributeValueElement"));

    if(attributeValueElementId !=null && !attributeValueElementId.trim().equals("") &&
            !attributeValueElementId.equals("null")) {
         attributeValueElementDTO = entitlementPolicyBean.getAttributeValueElement(Integer.
                 parseInt(attributeValueElementId));
    }
%>

<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
	<carbon:breadcrumb label="policy.create"
		resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />

	<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
	<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
	<script type="text/javascript" src="../carbon/admin/js/main.js"></script>

	<div id="middle">
	<h2><fmt:message key='entitlement.policy.creation'/></h2>
	<div id="workArea">
	
	   <script type="text/javascript">


        function doValidation() {

        	var value = document.getElementsByName("attributeDataType")[0].value;
        	if (value == '') {
            	CARBON.showWarningDialog('<fmt:message key="policy.name.is.required"/>');
            	return false;
        	}
                  
        	value = document.getElementsByName("attributeValue")[0].value;
        	if (value == '') {
            	CARBON.showWarningDialog('<fmt:message key="policy.description.is.required"/>');
            	return false;
        	}

        	return true;
    	}

        function doCancel() {
            var value = "<%=applyElementId%>"
            location.href = 'add-apply.jsp?applyElementId=' + value;
        }

        function doAdd() {
            var value1 = "<%=applyElementId%>"
            var value2 = "<%=editAttributeValueElement%>"
            var value3 = "<%=attributeValueElementId%>"

            if (doValidation()) {
                document.addAttributeValue.action = "add-apply.jsp?applyElementId=" + value1 +
                        "&editAttributeValueElement=" + value2 + "&attributeValueElementId=" + value3;
                document.addAttributeValue.submit();
            }
        }
        
  	   </script>

    <form name="addAttributeValue" action="create-policy-submit.jsp" method="post">
    	<table style="width: 100%" class="styledLeft">
		<thead>
			<tr>
				<th colspan="2"><fmt:message key='add.attribute.value.element'/></th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td class="formRow">
					<table class="normal" cellspacing="0">
                        
                        <td><fmt:message key="attribute.data.type"/></td>
                        <td>
                            <select id="attributeDataType" name="attributeDataType">
                        <%
                            if (entitlementPolicyBean.getDataTypes() != null) {
                                for (String attributeDataType : entitlementPolicyBean.getDataTypes()) {
                                    if(attributeValueElementDTO != null && attributeDataType.equals(attributeValueElementDTO.getAttributeDataType())) {
                        %>
                                    <option value="<%=attributeDataType%>" selected="selected"><%=attributeValueElementDTO.getAttributeDataType()%></option>

                        <%
                                    } else {
                        %>
                                    <option value="<%=attributeDataType%>"><%=attributeDataType%></option>

                        <%
                                    }
                                }
                            }
                        %>
                            </select>
                        </td>

                        <tr>
                                <td class="leftCol-small"><fmt:message key='attribute.value'/><font class="required">*</font></td>
                        <%
                            if(attributeValueElementDTO != null && attributeValueElementDTO.getAttributeValue() != null) {
                        %>
                                <td><input type="text" name="attributeValue" id="attributeValue" class="text-box-big" value="<%=attributeValueElementDTO.getAttributeValue()%>"/></td>
                        <%
                            } else {
                        %>
                                <td><input type="text" name="attributeValue" id="attributeValue" class="text-box-big"/></td>
                        <%
                            }
                        %>
                        </tr>
                        
					</table>
				</td>
			</tr>
			<tr>
			    <td colspan="2" class="buttonRow">
                          <input class="button" type="button" value="<fmt:message key='add'/>" onclick="doAdd();"/>
	                      <input class="button" type="button" value="<fmt:message key='cancel'/>"  onclick="doCancel();"/>
                </td>
			</tr>
		</tbody>	
	</table>
	</form>
	</div>	
	</div>
</fmt:bundle>