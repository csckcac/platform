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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="org.wso2.carbon.rule.service.ui.wizard.RuleServiceAdminClient" %>
<%@ page import="org.wso2.carbon.rulecep.commons.descriptions.PropertyDescription" %>
<%@ page import="org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription" %>
<%@ page
        import="org.wso2.carbon.rulecep.commons.descriptions.rule.service.RuleServiceExtensionDescription" %>
<%@ page import="org.wso2.carbon.rulecep.commons.descriptions.service.ServiceDescription" %>

<%
    RuleServiceAdminClient ruleServiceAdminClient =
            new RuleServiceAdminClient(config.getServletContext(), session);
    ServiceDescription serviceDescription =
            ruleServiceAdminClient.getRuleServiceDescription(request);
    RuleServiceExtensionDescription extensionDescription =
            (RuleServiceExtensionDescription) serviceDescription.getServiceExtensionDescription();
    RuleSetDescription ruleSetDescription = extensionDescription.getRuleSetDescription();
    ruleSetDescription.clearCreationProperties();
    String CountParameter = request.getParameter("propertyCount");
    if (CountParameter != null && !"".equals(CountParameter)) {
        try {
            int count = Integer.parseInt(CountParameter.trim());
            for (int i = 0; i < count; i++) {
                String name = request.getParameter("name" + i);
                if (name == null || "".equals(name)) {
                    continue;
                }
                String value = request.getParameter("value" + i);
                if (value == null || "".equals(value)) {
                    continue;
                }
                ruleSetDescription.addCreationProperty(new PropertyDescription(name, value));
            }
        } catch (NumberFormatException ignored) {
        }
    }
%>

