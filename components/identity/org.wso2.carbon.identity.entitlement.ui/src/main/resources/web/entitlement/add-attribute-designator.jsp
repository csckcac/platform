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
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.AttributeDesignatorDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />   
<%

    AttributeDesignatorDTO  attributeDesignatorDTO = new AttributeDesignatorDTO();
    String applyElementId = CharacterEncoder.getSafeText(request.getParameter("applyElementId"));
    String attributeDesignatorElementId = CharacterEncoder.getSafeText(request.
            getParameter("attributeDesignatorElementId"));
    String editAttributeDesignatorElement = CharacterEncoder.getSafeText(request.
            getParameter("editAttributeDesignatorElement"));
    String elementName = CharacterEncoder.getSafeText(request.getParameter("elementName"));

    if(attributeDesignatorElementId !=null && !attributeDesignatorElementId.trim().equals("") &&
            !attributeDesignatorElementId.trim().equals("null")) {
         attributeDesignatorDTO = entitlementPolicyBean.
                 getAttributeDesignatorElement(Integer.parseInt(attributeDesignatorElementId));
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

        	var value = document.getElementsByName("dataType")[0].value;
        	if (value == '') {
            	CARBON.showWarningDialog('<fmt:message key="policy.name.is.required"/>');
            	return false;
        	}
                  
        	value = document.getElementsByName("attributeId")[0].value;
        	if (value == '') {
            	CARBON.showWarningDialog('<fmt:message key="policy.description.is.required"/>');
            	return false;
        	}

        	return true;
    	}

        function doCancel() {
            var value = "<%=applyElementId%>";
            location.href = 'add-apply.jsp?applyElementId=' + value;
        }

        function doAdd() {
            var value1 = "<%=applyElementId%>";
            var value2 = "<%=editAttributeDesignatorElement%>";
            var value3 = "<%=attributeDesignatorElementId%>";
            var value4 = "<%=elementName%>";
            if (doValidation()) {
                if(value3 == '-1') {
                    document.addAttributeDesignator.action = "add-condition.jsp?attributeDesignatorElementId=" + value3;
                } else {
                    document.addAttributeDesignator.action = "add-apply.jsp?applyElementId=" + value1 + "&editAttributeDesignatorElement=" + value2 + "&attributeDesignatorElementId=" + value3 + "&elementName=" + value4;
                }

                document.addAttributeDesignator.submit();
            }
        }
        
  	   </script>

    <form name="addAttributeDesignator" action="create-policy-submit.jsp" method="post">
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

                        <tr>
                        <td><fmt:message key="attribute.designator.data.type"/></td>
                        <td>
                            <select id="dataType" name="dataType">
                        <%
                            if (entitlementPolicyBean.getDataTypes() != null) {
                                for (String attributeDataType : entitlementPolicyBean.getDataTypes()) {
                                    if(attributeDesignatorDTO != null && attributeDataType.equals(attributeDesignatorDTO.getDataType())) {

                        %>
                                            <option value="<%=attributeDataType%>" selected="selected"><%=attributeDesignatorDTO.getDataType()%></option>

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
                        </tr>

                        <tr>
                        <td class="leftCol-small"><fmt:message key='attribute.id'/><font class="required">*</font></td>
                        <%
                            if(attributeDesignatorDTO != null && attributeDesignatorDTO.getAttributeId() != null) {
                        %>
                            <td><input type="text" name="attributeId" id="attributeId" class="text-box-big" value="<%=attributeDesignatorDTO.getAttributeId()%>"/></td>
                        <%
                            } else {
                        %>
                            <td><input type="text" name="attributeId" id="attributeId" class="text-box-big"/></td>
                        <%
                            }
                        %>
                        </tr>

                        <tr>
                        <td class="leftCol-small"><fmt:message key='issuer'/></td>
                        <%
                            if(attributeDesignatorDTO != null && attributeDesignatorDTO.getIssuer() != null) {
                        %>
                            <td><input id="issuerName" name="issuerName" class="text-box-big" type="text" value="<%=attributeDesignatorDTO.getIssuer()%>"></td>
                        <%
                            } else {
                        %>
                            <td><input type="text" name="issuerName" id="issuerName" class="text-box-big"/></td>
                        <%
                            }
                        %>
                        </tr>

                        <tr>
                        <td><fmt:message key="must.present"/></td>
                        <td>
                            <select id="attributeDesignatorMustPresent" name="attributeDesignatorMustPresent">
                            <option value="Select" selected="selected">----Select-----</option>
                        <%
                            if (entitlementPolicyBean.getMustBePresentValues() != null) {
                                for (String mustPresent : entitlementPolicyBean.getMustBePresentValues()) {
                                    if(attributeDesignatorDTO != null && mustPresent.equals(attributeDesignatorDTO.getMustBePresent())) {
                        %>
                                    <option value="<%=mustPresent%>" selected="selected"><%=attributeDesignatorDTO.getMustBePresent()%></option>

                        <%
                                    } else {
                        %>
                                    <option value="<%=mustPresent%>"><%=mustPresent%></option>

                        <%          }

                                }
                            }
                        %>
                            </select>
                        </td>
                        </tr>

                        <%
                            if(elementName != null && elementName.equals(EntitlementPolicyConstants.SUBJECT_ELEMENT)) {
                        %>

                            <tr>
                            <td class="leftCol-small"><fmt:message key='subject.category'/></td>

                            <%
                                if(attributeDesignatorDTO != null && attributeDesignatorDTO.getSubjectCategory() != null) {
                            %>
                                <td><input id="subjectCategory" name="subjectCategory" class="text-box-big" type="text" value="<%=attributeDesignatorDTO.getSubjectCategory()%>"></td>
                            <%
                                } else {
                            %>
                                <td><input type="text" name="subjectCategory" id="subjectCategory" class="text-box-big"/></td>

                            <%
                                }
                            %>
                            </tr>

                        <%
                            }
                        %>
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