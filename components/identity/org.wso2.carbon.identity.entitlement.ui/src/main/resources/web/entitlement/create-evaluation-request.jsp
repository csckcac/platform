<!--
/*
* Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
-->

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<jsp:include page="../dialog/display_messages.jsp"/>
<jsp:include page="../highlighter/header.jsp"/>
<%
    String resourceNames = "";
    String subjectNames = "";
    String actionNames = "";
    String environmentNames = "";

    resourceNames = (String)session.getAttribute("resourceNames");
    subjectNames = (String)session.getAttribute("subjectNames");
    actionNames = (String)session.getAttribute("actionNames");
    environmentNames = (String)session.getAttribute("environmentNames");
    String clearAttributes = (String)request.getParameter("clearAttributes");

    if("true".equals(clearAttributes)){
        resourceNames = null;
        subjectNames = null;
        actionNames = null;
        environmentNames = null;
        session.removeAttribute("resourceNames");
        session.removeAttribute("subjectNames");
        session.removeAttribute("attributeId");
        session.removeAttribute("environmentNames");
        session.removeAttribute("actionNames");
    }
%>


<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
<carbon:breadcrumb
		label="create.evaluation.request"
		resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
		topPage="false"
		request="<%=request%>" />

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="resources/js/main.js"></script>
    <!--Yahoo includes for dom event handling-->
    <script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
    <script src="../entitlement/js/create-basic-policy.js" type="text/javascript"></script>
    <link href="../entitlement/css/entitlement.css" rel="stylesheet" type="text/css" media="all"/>

<script type="text/javascript">

    function submitForm(){
        document.requestForm.action = "eval-policy-submit.jsp?";
        document.requestForm.submit();
    }

    function clearForm(){
        location.href = 'create-evaluation-request.jsp?clearAttributes=true';        
    }

    function doCancel(){
        location.href = 'index.jsp?';
    }

    function createRequest(){
        document.requestForm.action = "eval-policy.jsp?";
        document.requestForm.submit();
    }

</script>

<div id="middle">
    <h2><fmt:message key="eval.ent.policy"/></h2>
    <div id="workArea">
        <div class="goToAdvance">
            <a class='icon-link' href="../entitlement/eval-policy.jsp"
                       style='background-image:url(images/advanceview.png);float:none'><fmt:message key="create.request.using.editor"/></a>
        </div>
        <form id="requestForm" name="requestForm" method="post" action="">
        <table class="styledLeft noBorders">
        <tr>
            <td><fmt:message key='resource.names.are'/></td>
            <td colspan="2">
            <%
                if (resourceNames != null && resourceNames.trim().length() > 0) {
            %>
            <input type="text" size="60" name="resourceNames" id="resourceNames"
                       value="<%=resourceNames%>" class="text-box-big"/>
            <%
                } else {
            %>
            <input type="text" size="60" name="resourceNames" id="resourceNames"
                       class="text-box-big"/>
            <%
                }
            %>
            </td>
        </tr>

        <tr>
            <td class="leftCol-small"><fmt:message key='subject.name'/></td>
            <td colspan="2">
            <%
                if (subjectNames != null && subjectNames.trim().length() > 0) {
            %>
            <input type="text" name="subjectNames" id="subjectNames"
                       value="<%=subjectNames%>" class="text-box-big"/>
            <%
                } else {
            %>
            <input type="text" name="subjectNames" id="subjectNames" class="text-box-big"/>
            <%
                }
            %>
            </td>
        </tr>

        <tr>
            <td class="leftCol-small"><fmt:message key='action.names'/></td>
            <td colspan="2">
            <%
                if (actionNames != null && actionNames.trim().length() > 0) {
            %>
            <input type="text" name="actionNames" id="actionNames" value="<%=actionNames%>"
                       class="text-box-big"/>
            <%
                } else {
            %>
            <input type="text" name="actionNames" id="actionNames" class="text-box-big"/>
            <%
                }
            %>
            </td>
        </tr>            

        <tr>
            <td class="leftCol-small"><fmt:message key='environment.names'/></td>
            <td colspan="2">
            <%
                if (environmentNames != null && environmentNames.trim().length() > 0) {
            %>
            <input type="text" name="environmentNames" id="environmentNames" value="<%=environmentNames%>"
                       class="text-box-big"/>
            <%
                } else {
            %>
            <input type="text" name="environmentNames" id="environmentNames" class="text-box-big"/>
            <%
                }
            %>
            </td>
        </tr>


        <tr>
            <td class="buttonRow" colspan="3">
                <input type="button" onclick="submitForm();" value="<fmt:message key="evaluate"/>"  class="button"/>
                <input type="button" onclick="createRequest();" value="<fmt:message key="create.request"/>"  class="button"/>
                <input type="button" onclick="clearForm();" value="<fmt:message key="clear"/>"  class="button"/>
            </td>
        </tr>
        </table>
        </form>
    </div>
</div>
</fmt:bundle>
