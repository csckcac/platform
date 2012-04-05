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
<%@ page import="org.wso2.carbon.rule.core.RuleConstants" %>
<%@ page import="org.wso2.carbon.rule.service.ui.ns.NameSpacesRegistrar" %>
<%@ page import="org.wso2.carbon.rule.service.ui.wizard.RuleServiceAdminClient" %>
<%@ page import="org.wso2.carbon.rulecep.adapters.impl.OMElementResourceAdapter" %>
<%@ page import="org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription" %>
<%@ page import="org.wso2.carbon.rulecep.commons.descriptions.service.OperationDescription" %>
<%@ page import="org.wso2.carbon.rulecep.commons.descriptions.service.ServiceDescription" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script type="text/javascript" src="js/rule-services.js"></script>
<script type="text/javascript" src="js/ns-editor.js"></script>
<%
    RuleServiceAdminClient ruleServiceAdminClient =
            new RuleServiceAdminClient(config.getServletContext(), session);
    ServiceDescription serviceDescription =
            ruleServiceAdminClient.getRuleServiceDescription(request);
    String opname = request.getParameter("opname");
//    boolean isNew = opname == null || "".equals(opname);
    if (opname == null) {
        opname = "";
    }
%>
<fmt:bundle basename="org.wso2.carbon.rule.service.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.rule.service.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="ruleservicejsi18n"/>
<carbon:breadcrumb
        label="step5.msg"
        resourceBundle="org.wso2.carbon.rule.service.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<style type="text/css">
     a.fact-selector-icon-link {
         background-image: url("../rule_service/images/facts-selector.gif");
         background-position: left top;
         background-repeat: no-repeat;
         float: left;
         height: 17px;
         line-height: 17px;
         margin-bottom: 3px;
         margin-left: 10px;
         margin-top: 5px;
         padding-left: 20px;
         position: relative;
         white-space: nowrap;
     }
 </style>
<script type="text/javascript">
    function validateStep5() {
        var serviceName = document.getElementById('operationName').value;
        if (serviceName == '') {
            CARBON.showWarningDialog('<fmt:message key="operation.name.empty"/>');
            return false;
        }
        var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
        // Check for white space
        if (!reWhiteSpace.test(serviceName)) {
            CARBON.showWarningDialog('<fmt:message key="operation.name.validate"/>');
            return false;
        }
        var returnValue = validateFacts("fact");
        if (returnValue) {
            returnValue = validateFacts("result");
        }
        return returnValue;
    }

</script>
<div id="middle">
<h2>
    <fmt:message key="step5.msg"/>
</h2>

<div id="workArea">
<form method="post" action="rule_service_wizard_step4.jsp" name="dataForm"
      onsubmit="return validateStep5();">
<table class="styledLeft">
<thead>
<tr>
    <th><fmt:message key="step5.msg"/></th>
</tr>
</thead>
<tr>
    <td class="formRaw">
        <table class="normal">
            <tr>
                <td><fmt:message key="name"/><font
                        color="red">*</font>
                </td>
                <td>
                    <input type="text" name="operationName" id="operationName"
                           value="<%=opname.trim()%>"/>
                </td>
            </tr>
        </table>
    </td>
</tr>
<%
    OperationDescription operationDescription =
            serviceDescription.getRuleServiceOperationDescription(opname);
    List<ResourceDescription> facts;
    if (operationDescription == null) {
        facts = new ArrayList<ResourceDescription>();
    } else {
        facts = operationDescription.getFactDescriptions();
    }
    List<ResourceDescription> results;
    if (operationDescription == null) {
        results = new ArrayList<ResourceDescription>();
    } else {
        results = operationDescription.getResultDescriptions();
    }

    String wrapperName = null;

    if (results.size() == 1) {
        ResourceDescription firstDescription = results.get(0);
        if (!OMElementResourceAdapter.TYPE.equals(firstDescription.getType())) {
            CarbonUIMessage.sendCarbonUIMessage("Invalid result !! result type is invalid",
                    CarbonUIMessage.ERROR, request,
                    response, "../admin/error.jsp");
        }
        results = (List<ResourceDescription>) firstDescription.getChildResources();
        wrapperName = firstDescription.getName();
    }

    if (wrapperName == null || "".equals(wrapperName)) {
        wrapperName = RuleConstants.DEFAULT_WRAPPER_NAME;
    }

    if (!"".equals(opname)) {
        NameSpacesRegistrar.getInstance().registerNameSpaces(facts, "factValue", opname, session);
        NameSpacesRegistrar.getInstance().registerNameSpaces(results, "resultValue", opname, session);
    }
    String factTableStyle = facts.isEmpty() ? "display:none;" : "";
    String resultTableStyle = results.isEmpty() ? "display:none;" : "";
%>
<tr>
    <td class="formRaw">
        <h3 class="mediator"><fmt:message key="facts"/></h3>

        <div style="margin-top:0px;">
            <table id="facttable" style="<%=factTableStyle%>;"
                   class="styledInner">
                <thead>
                <tr>
                    <th width="10%"><fmt:message key="th.type"/></th>
                    <th width="10%"><fmt:message key="th.selector"/></th>
                    <th width="10%"><fmt:message key="th.name"/></th>
                    <th><fmt:message key="namespaceeditor"/></th>
                    <th><fmt:message key="actions"/></th>
                </tr>
                <tbody id="facttbody">
                <%
                    int k = 0;
                    for (ResourceDescription property : facts) {
                        if (property != null) {
                            String type = property.getType();
                            String name = property.getName();
                            if (name == null) {
                                name = "";
                            }
                %>
                <tr id="factRaw<%=k%>">
                    <td>
                        <input class="longInput" name="factType<%=k%>"
                               id="factType<%=k%>" value="<%=type%>"
                               type="text"/>
                    </td>
                    <td><a href="#factEditorLink" class="fact-selector-icon-link"
                           style="padding-left:40px"
                           onclick="showFactEditor('fact','<%=k%>')"><fmt:message
                            key="fact.type"/></a></td>

                    <td>
                        <input class="longInput" id="factName<%=k%>"
                               name="factName<%=k%>"
                               type="text" value="<%=name%>"/>
                    </td>
                    <td id="factNsEditorButtonTD<%=k%>">
                        <a href="#nsEditorLink" class="nseditor-icon-link"
                           style="padding-left:40px"
                           onclick="showNameSpaceEditor('factValue<%=k%>','<%=opname.trim()%>')"><fmt:message
                                key="namespaces"/></a>
                    </td>
                    <td><a href="#" href="#" class="delete-icon-link" style="padding-left:40px"
                           onclick="deleteFact('fact','<%=k%>')"><fmt:message
                            key="delete"/></a></td>
                </tr>
                <% }
                    k++;
                } %>
                <input type="hidden" name="factCount" id="factCount"
                       value="<%=k%>"/>
                </tbody>
                </thead>
            </table>
        </div>
    </td>
</tr>
<tr>
    <td>
        <div style="margin-top:0px;">
            <a name="addfactLink"></a>
            <a class="add-icon-link"
               href="#addfactLink"
               onclick="addFact('fact','<%=opname%>')">
                <fmt:message key="add.fact"/></a>
        </div>
    </td>
</tr>
<tr>
    <td><h3 class="mediator"><fmt:message key="result"/></h3></td>
</tr>
<tr>
    <td class="formRaw">
        <table class="normal">
            <tr>
                <td><fmt:message key="result.wrapper"/>
                </td>
                <td>
                    <input type="text" name="wrapperName" id="wrapperName"
                           value="<%=wrapperName%>"/>
                </td>
            </tr>
        </table>
    </td>
</tr>
<tr>
    <td><h3 class="mediator"><fmt:message key="result.elements"/></h3></td>
</tr>
<tr>
    <td class="formRaw">
        <div style="margin-top:0px;">
            <table id="resulttable" style="<%=resultTableStyle%>;"
                   class="styledInner">
                <thead>
                <tr>
                    <th width="10%"><fmt:message key="th.type"/></th>
                    <th width="10%"><fmt:message key="th.selector"/></th>
                    <th width="10%"><fmt:message key="th.name"/></th>
                    <th><fmt:message key="namespaceeditor"/></th>
                    <th><fmt:message key="actions"/></th>
                </tr>
                <tbody id="resulttbody">
                <%
                    int j = 0;
                    for (ResourceDescription property : results) {
                        if (property != null) {
                            String type = property.getType();
                            String name = property.getName();
                            if (name == null) {
                                name = "";
                            }
                %>
                <tr id="resultRaw<%=j%>">
                    <td>
                        <input class="longInput" name="resultType<%=j%>"
                               id="resultType<%=j%>" value="<%=type%>"
                               type="text"/>
                    </td>
                    <td><a href="#resultEditorLink" class="fact-selector-icon-link"
                           style="padding-left:40px"
                           onclick="showFactEditor('result','<%=j%>')"><fmt:message
                            key="result.type"/></a></td>

                    <td>
                        <input class="longInput" id="resultName<%=j%>"
                               name="resultName<%=j%>"
                               type="text" value="<%=name%>"/>
                    </td>
                    <td id="factNsEditorButtonTD<%=k%>">
                        <a href="#nsEditorLink" class="nseditor-icon-link"
                           style="padding-left:40px"
                           onclick="showNameSpaceEditor('resultValue<%=j%>','<%=opname%>')"><fmt:message
                                key="namespaces"/></a>
                    </td>
                    <td><a href="#" href="#" class="delete-icon-link" style="padding-left:40px"
                           onclick="deleteFact('result','<%=j%>')"><fmt:message
                            key="delete"/></a></td>
                </tr>
                <% }
                    j++;
                } %>
                <input type="hidden" name="resultCount" id="resultCount"
                       value="<%=j%>"/>
                </tbody>
                </thead>
            </table>
        </div>
    </td>
</tr>
<tr>
    <td>
        <div style="margin-top:0px;">
            <a name="addresultLink"></a>
            <a class="add-icon-link"
               href="#addresultLink"
               onclick="addFact('result','<%=opname%>')">
                <fmt:message key="add.result.element"/></a>
        </div>
    </td>
</tr>

<tr>
    <td class="buttonRow">
        <input type="hidden" id="stepID" name="stepID" value="step5"/>
        <input class="button" type="submit"
               value="<fmt:message key="add"/>"/>
        <input class="button" type="button" value="<fmt:message key="cancel"/>"
               onclick="location.href = 'rule_service_wizard_step4.jsp'"/>
    </td>
</tr>
</table>
</form>
</div>

<a name="factEditorLink"></a>

<div id="factEditor" style="display:none;"></div>

<a name="nsEditorLink"></a>

<div id="nsEditor" style="display:none;"></div>

</div>
</fmt:bundle>
