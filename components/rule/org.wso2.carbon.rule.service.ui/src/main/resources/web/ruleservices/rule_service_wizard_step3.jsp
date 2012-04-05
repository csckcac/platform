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
<%@ page import="org.wso2.carbon.rule.service.ui.wizard.RuleServiceAdminClient" %>
<%@ page import="org.wso2.carbon.rule.service.ui.wizard.RuleServiceManagementHelper" %>
<%@ page
        import="org.wso2.carbon.rulecep.commons.descriptions.rule.service.RuleServiceExtensionDescription" %>
<%@ page import="org.wso2.carbon.rulecep.commons.descriptions.service.ServiceDescription" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%
//    String[] classes = (String[]) session.getAttribute(RuleServiceAdminClient.FACTS);
    RuleServiceAdminClient ruleServiceAdminClient =
            new RuleServiceAdminClient(config.getServletContext(), session);
    ServiceDescription serviceDescription =
            ruleServiceAdminClient.getRuleServiceDescription(request);
    RuleServiceExtensionDescription extensionDescription =
            (RuleServiceExtensionDescription) serviceDescription.getServiceExtensionDescription();
    RuleServiceManagementHelper.saveStep2(extensionDescription.getRuleSetDescription(), request);
    String [] classes =  ruleServiceAdminClient.getAllFacts(serviceDescription,session);
    int classesCount = 0;
    if(classes != null && classes.length != 0 && classes.length > 1){
        classesCount = classes.length - 1;
    }
    boolean factAdded = classesCount != 0;
%>

<fmt:bundle basename="org.wso2.carbon.rule.service.ui.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.rule.service.ui.i18n.JSResources"
            request="<%=request%>" i18nObjectName="ruleservicejsi18n"/>
    <carbon:breadcrumb label="upload.ruleservice.facts"
                       resourceBundle="org.wso2.carbon.rule.service.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>

    <script type="text/javascript">
        function validate() {
            var fileName = document.factUpload.factFilename.value;
            if (fileName == '') {
                CARBON.showErrorDialog('<fmt:message key="select.fact"/>');
            } else if (fileName.lastIndexOf(".jar") == -1) {
                CARBON.showErrorDialog('<fmt:message key="select.fact.file"/>');
            } else {
                document.factUpload.submit();
            }
        }
    </script>

    <div id="middle">
        <h2><fmt:message key="upload.ruleservice.facts"/></h2>

        <div id="workArea">

            <table class="styledLeft">
                <thead>
                <tr>
                    <th colspan="2"><fmt:message key="upload.ruleservice.facts"/> (.jar)</th>
                </tr>
                </thead>
                <tr>
                    <td class="formRow">
                        <form method="post" name="factUpload"
                              action="../../fileupload/facts"
                              enctype="multipart/form-data" target="_self" style="margin-bottom: 0">
                            <table class="normal">
                                <tr>
                                    <td>
                                        <label><fmt:message key="path.to.ruleservice.facts"/>
                                            (.jar) :</label>
                                    </td>
                                    <td>
                                        <input type="file" id="factFilename" name="factFilename"
                                               size="75"/>
                                    </td>
                                    <td class="buttonRow">
                                        <input type="hidden"
                                               value="<%=serviceDescription.getName()%>"
                                               name="ruleServiceName"/>
                                        <input name="upload" type="button" class="button"
                                               value=" <fmt:message key="upload"/> "
                                               onclick="validate();"/>
                                    </td>
                                </tr>
                            </table>
                        </form>
                    </td>
                </tr>
                <% if (factAdded) {
                %>
                <tr>
                    <td class="formRow">
                        <table class="normal">
                            <tr>
                                <td>
                                    <label><fmt:message key="uploaded.facts"/> <%=classesCount%>
                                    </label>
                                </td>
                                    <%--<td>--%>
                                    <%--<% String prefix = "";--%>
                                    <%--for (String fact : classes) {--%>
                                    <%--if (fact != null && !"".equals(fact)) {--%>
                                    <%--%><%=prefix + fact%><%--%>
                                    <%--prefix = ",";--%>
                                    <%--}--%>
                                    <%--}--%>
                                    <%--%>--%>
                                    <%--</td>--%>
                            </tr>
                        </table>
                    </td>
                </tr>
                <% } %>
                <tr>
                    <td class="buttonRow">
                        <form method="post" action="rule_service_wizard_step4.jsp" name="dataForm">
                            <input type="hidden" id="stepID" name="stepID" value="step3"/>
                            <input class="button" type="button" value="< <fmt:message key="back"/>"
                                   onclick="location.href = 'rule_service_wizard_step2.jsp'"/>
                            <input class="button" type="submit"
                                   value="<fmt:message key="next"/> >"/>
                            <input class="button" type="button" value="<fmt:message key="cancel"/>"
                                   onclick="location.href = 'cancel_handler.jsp'"/>
                        </form>
                    </td>
                </tr>
            </table>

        </div>
    </div>
</fmt:bundle>