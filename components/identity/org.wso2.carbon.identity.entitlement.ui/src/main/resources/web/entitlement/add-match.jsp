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
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.MatchElementDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />
<%

    String[] matchIds = entitlementPolicyBean.getMatchIds();
    String[] dataTypes = entitlementPolicyBean.getDataTypes();
    String[] mustPresents = entitlementPolicyBean.getMustBePresentValues();
    MatchElementDTO currentMatchElement = new MatchElementDTO();
    
    String subElementName = CharacterEncoder.getSafeText(request.getParameter("subElementName"));   
    String editSubElement = CharacterEncoder.getSafeText(request.getParameter("editSubElement"));
    String matchElementId = CharacterEncoder.getSafeText(request.getParameter("matchElementId"));
    String editMatchElement = CharacterEncoder.getSafeText(request.getParameter("editMatchElement"));

    if(matchElementId !=null && !matchElementId.trim().equals("") &&
            !matchElementId.trim().equals("null")) {
         currentMatchElement = entitlementPolicyBean.getMatchElement(Integer.parseInt(matchElementId));
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

        	var value = document.getElementsByName("attributeValue")[0].value;
        	if (value == '') {
            	CARBON.showWarningDialog('<fmt:message key="attribute.value.is.required"/>');
            	return false;
        	}

            var value1 =  document.getElementsByName("attributeDesignatorDataType")[0].value;
            var value2 =  document.getElementsByName("attributeId")[0].value;
        	if (value1 != 'Select' && value2 == '') {
            	CARBON.showWarningDialog('<fmt:message key="attribute.id.is.required"/>');
            	return false;
        	}

            return true;
    	}

        function doCancel() {
            var value1 = "<%=subElementName.toLowerCase()%>";
            location.href = 'add-'+value1+'s.jsp';
        }

        function doAdd() {
            var value1 = "<%=subElementName.toLowerCase()%>";
            var value2 = "<%=matchElementId%>";
            var value3 = "<%=editMatchElement%>";
            var value4 = "<%=editSubElement%>" ;

            document.matchElementForm.action = "add-" + value1 + "s.jsp?matchElementId=" + value2 +
                    "&editMatchElement=" + value3 + "&editSubElement=" + value4;
            if (doValidation()) {
                document.matchElementForm.submit();
            }
        }

  	   </script>

    <form name="matchElementForm"  action="create-policy.jsp" method="post">
    	<table style="width: 100%" class="styledLeft noBorders">
		<thead>
			<tr>
				<th colspan="2"><fmt:message key='add.match.element'/></th>
			</tr>
		</thead>
		<tbody>
            <tr>
            <td><fmt:message key="match.id"/></td>
            <td>
                <select id="matchId" name="matchId">
            <%
                if (matchIds != null) {
                    for (String matchId : matchIds) {
                        if(currentMatchElement != null && matchId.equals(currentMatchElement.getMatchId())) {
            %>
                        <option value="<%=matchId%>" selected="selected"><%=currentMatchElement.getMatchId()%></option>

            <%
                        } else {
            %>
                        <option value="<%=matchId%>"><%=matchId%></option>

            <%          }
                    }
              }
            %>
                </select>
            </td>
            </tr>

            <tr>
                <td colspan="2" class="middle-header"><fmt:message key="attribute.value.element"/></td>
            </tr>

            <tr>
                <td><fmt:message key="attribute.data.type"/></td>
                <td>
                    <select id="dataType" name="dataType">
                <%
                    if (dataTypes != null) {
                        for (String dataType : dataTypes) {
                            if(currentMatchElement != null && currentMatchElement.getAttributeValueElementDTO() != null && dataType.equals(currentMatchElement.getAttributeValueElementDTO().getAttributeDataType())) {
                %>
                            <option value="<%=dataType%>" selected="selected"><%=currentMatchElement.getAttributeValueElementDTO().getAttributeDataType()%></option>

                <%
                            } else {
                %>
                            <option value="<%=dataType%>"><%=dataType%></option>

                <%          }
                        }
                  }
                %>
                    </select>
                </td>
            </tr>


            <tr>
                <td ><fmt:message key='attribute.value'/><span class="required">*</span></td>
            <%
                if(currentMatchElement != null && currentMatchElement.getAttributeValueElementDTO() != null && currentMatchElement.getAttributeValueElementDTO().getAttributeValue() != null) {
            %>
                    <td><input id="attributeValue" name="attributeValue" class="text-box-big" type="text" value="<%=currentMatchElement.getAttributeValueElementDTO().getAttributeValue()%>"></td>
            <%
                } else {
            %>
                    <td><input type="text" name="attributeValue" id="attributeValue" class="text-box-big"/></td>
            <%
                }
            %>
            </tr>

            <tr>
                <td td colspan="2" class="middle-header"><fmt:message key="attribute.designator.element"/></td>
            </tr>

            <tr>
                <td><fmt:message key="attribute.designator.data.type"/></td>
                <td>
                    <select id="attributeDesignatorDataType" name="attributeDesignatorDataType">
                    <option value="Select" selected="selected">Select</option>
                <%
                    if (dataTypes != null) {
                        for (String attributeDesignatorDataType : dataTypes) {
                            if(currentMatchElement != null && currentMatchElement.getAttributeDesignatorDTO() != null && attributeDesignatorDataType.equals(currentMatchElement.getAttributeDesignatorDTO().getDataType())) {
                %>
                            <option value="<%=attributeDesignatorDataType%>" selected="selected" ><%=currentMatchElement.getAttributeDesignatorDTO().getDataType()%></option>

                <%
                            } else {
                %>
                                    <option value="<%=attributeDesignatorDataType%>"><%=attributeDesignatorDataType%></option>

                <%          }
                        }
                    }
                %>
                    </select>
                </td>
            </tr>

            <tr>
                <td class="leftCol-small"><fmt:message key='attribute.id'/></td>
            <%
                if(currentMatchElement != null && currentMatchElement.getAttributeDesignatorDTO() != null && currentMatchElement.getAttributeDesignatorDTO().getAttributeId() != null) {
            %>
                <td><input id="attributeId" name="attributeId" class="text-box-big" type="text" value="<%=currentMatchElement.getAttributeDesignatorDTO().getAttributeId()%>"></td>
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
                if(currentMatchElement != null && currentMatchElement.getAttributeDesignatorDTO() != null && currentMatchElement.getAttributeDesignatorDTO().getIssuer() != null) {
            %>
                <td><input id="issuerName" name="issuerName" class="text-box-big" type="text" value="<%=currentMatchElement.getAttributeDesignatorDTO().getIssuer()%>"></td>
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
                <option value="Select" selected="selected">Select</option>
            <%
                if (mustPresents != null) {
                    for (String mustPresent : mustPresents) {
                        if(currentMatchElement != null && currentMatchElement.getAttributeDesignatorDTO() != null && mustPresent.equals(currentMatchElement.getAttributeDesignatorDTO().getMustBePresent())) {
            %>
                        <option value="<%=mustPresent%>" selected="selected"><%=currentMatchElement.getAttributeDesignatorDTO().getMustBePresent()%></option>

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
                if(subElementName.equals(EntitlementPolicyConstants.SUBJECT_ELEMENT)) {
            %>

                <tr>
                    <td class="leftCol-small"><fmt:message key='subject.category'/></td>

                    <%
                        if(currentMatchElement != null && currentMatchElement.getAttributeDesignatorDTO() != null && currentMatchElement.getAttributeDesignatorDTO().getSubjectCategory() != null) {
                    %>
                        <td><input id="subjectCategory" name="subjectCategory" class="text-box-big" type="text" value="<%=currentMatchElement.getAttributeDesignatorDTO().getSubjectCategory()%>"></td>
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

            <tr>
                <td colspan="2" class="middle-header"><fmt:message key="attribute.selector.element"/></td>
            </tr>

            <tr>
                <td><fmt:message key="attribute.selector.data.type"/></td>
                <td>
                    <select id="attributeSelectorDataType" name="attributeSelectorDataType">
                    <option value="Select" selected="selected">Select</option>
                <%
                    if (dataTypes != null) {
                        for (String attributeSelectorDataType : dataTypes) {
                            if(currentMatchElement != null && currentMatchElement.getAttributeSelectorDTO() != null && attributeSelectorDataType.equals(currentMatchElement.getAttributeSelectorDTO().getAttributeSelectorDataType())) {
                %>
                            <option value="<%=attributeSelectorDataType%>" selected="selected"><%=currentMatchElement.getAttributeDesignatorDTO().getDataType()%></option>

                <%
                            } else {
                %>
                            <option value="<%=attributeSelectorDataType%>"><%=attributeSelectorDataType%></option>

                <%         }
                        }
                  }
                %>
                    </select>
                </td>
            </tr>


            <tr>
                <td class="leftCol-small"><fmt:message key='request.context.path'/></td>
            <%
                if(currentMatchElement != null && currentMatchElement.getAttributeSelectorDTO() != null && currentMatchElement.getAttributeSelectorDTO().getAttributeSelectorRequestContextPath() != null) {
            %>
                <td><input id="requestContextPath" name="requestContextPath" class="text-box-big" type="text" value="<%=currentMatchElement.getAttributeSelectorDTO().getAttributeSelectorRequestContextPath()%>"></td>
            <%
                } else {
            %>
                <td><input type="text" name="requestContextPath" id="requestContextPath" class="text-box-big"/></td>
            <%
                }
            %>
            </tr>

            <td><fmt:message key="must.present"/></td>
            <td>
                <select id="attributeSelectorMustPresent" name="attributeSelectorMustPresent">
                <option value="Select" selected="selected">Select</option>              
            <%
                if (mustPresents != null) {
                    for (String mustPresent : mustPresents) {
                        if(currentMatchElement != null && currentMatchElement.getAttributeSelectorDTO() != null && mustPresent.equals(currentMatchElement.getAttributeSelectorDTO().getAttributeSelectorMustBePresent())) {
            %>
                        <option value="<%=mustPresent%>" selected="selected"><%=currentMatchElement.getAttributeSelectorDTO().getAttributeSelectorMustBePresent()%></option>

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