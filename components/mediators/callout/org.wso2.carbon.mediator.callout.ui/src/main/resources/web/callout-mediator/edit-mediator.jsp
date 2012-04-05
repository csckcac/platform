<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%--
  ~  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  Licensed under the Apache License, Version 2.0 (the "License");
  ~  you may not use this file except in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~        http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing, software
  ~  distributed under the License is distributed on an "AS IS" BASIS,
  ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~  See the License for the specific language governing permissions and
  ~  limitations under the License.
  --%>

<%@ page import="org.wso2.carbon.mediator.callout.CalloutMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    String serviceURL = "", action = "";
    String targetVal = "", sourceVal = "";
    String repo = "", axis2XML = "";
    NameSpacesRegistrar nmspRegistrar = NameSpacesRegistrar.getInstance();
    boolean isTargetXpath = false, isSourceXpath = false;
    if (!(mediator instanceof CalloutMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    CalloutMediator calloutMediator = (CalloutMediator) mediator;
    if (calloutMediator.getServiceURL() != null) {
        serviceURL = calloutMediator.getServiceURL();
    }
    if (calloutMediator.getAction() != null) {
        action = calloutMediator.getAction();
    }
    if (calloutMediator.getClientRepository() != null) {
        repo = calloutMediator.getClientRepository();
    }
    if (calloutMediator.getAxis2xml() != null) {
        axis2XML = calloutMediator.getAxis2xml();
    }

    if (calloutMediator.getRequestKey() != null) {
        isSourceXpath = false;
        sourceVal = calloutMediator.getRequestKey();
    } else if (calloutMediator.getRequestXPath() != null){
        isSourceXpath = true;
        sourceVal = calloutMediator.getRequestXPath().toString();
        nmspRegistrar.registerNameSpaces(calloutMediator.getRequestXPath(), "mediator.callout.source.xpath_val", session);
    }

    if (calloutMediator.getTargetKey() != null) {
        isTargetXpath = false;
        targetVal = calloutMediator.getTargetKey();
    } else if (calloutMediator.getTargetXPath() != null){
        isTargetXpath = true;
        targetVal = calloutMediator.getTargetXPath().toString();
        nmspRegistrar.registerNameSpaces(calloutMediator.getTargetXPath(), "mediator.callout.target.xpath_val", session);
    }
%>

<fmt:bundle basename="org.wso2.carbon.mediator.callout.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.mediator.callout.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="calloutMediatorJsi18n"/>
<div>
    <script type="text/javascript" src="../callout-mediator/js/mediator-util.js"></script>
    <script type="text/javascript" src="../resources/js/resource_util.js"></script>
    <table class="normal" width="100%">
    <tr>
        <td>
            <h2><fmt:message key="mediator.callout.header"/></h2>
        </td>
    </tr>
    <tr>
    <td>
    <table border="0" class="normal" >
            <tr>
                <td class="leftCol-small">
                    <fmt:message key="mediator.callout.serviceurl"/>
                    <span class="required">*</span></td>
                <td>
                    <input type="text" size="40" id="mediator.callout.svcURL" name="mediator.callout.svcURL" value="<%=serviceURL%>" style="width:300px"/>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="mediator.callout.action"/>
                </td>
                <td>
                    <input type="text" size="40" id="mediator.callout.action" name="mediator.callout.action" value="<%=action%>" style="width:300px"/>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="mediator.callout.repo"/>
                </td>
                <td>
                    <input type="text" size="40" id="mediator.callout.repo" name="mediator.callout.repo" value="<%=repo%>" style="width:300px"/>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="mediator.callout.axis2XML"/>
                </td>
                <td>
                    <input type="text" size="40" id="mediator.callout.axis2XML" name="mediator.callout.axis2XML" value="<%=axis2XML%>" style="width:300px"/>
                </td>
            </tr>
    </table>
    </td>
    </tr>
    <tr>
        <td>
        <h3 class="mediator"><fmt:message key="mediator.callout.source"/> <span class="required">*</span></h3>
        <table class="normal">
                <tr>
                    <td class="leftCol-small">
                        <fmt:message key="mediator.callout.specifyas"/> :
                    </td>
                    <td>
                        <input type="radio" id="sourceGroupXPath"
                               onclick="javascript:displayElement('mediator.callout.source.xpath', true); displayElement('mediator.callout.source.namespace.editor', true); displayElement('mediator.callout.source.key', false);"
                               name="sourcegroup" <%=isSourceXpath ? "checked=\"checked\" value=\"XPath\"" : "value=\"XPath\""%>"/>
                        <fmt:message key="mediator.callout.xpath"/>
                        <input type="radio"
                               onclick="javascript:displayElement('mediator.callout.source.xpath', false); javascript:displayElement('mediator.callout.source.key', true); displayElement('mediator.callout.source.namespace.editor', false);"
                               name="sourcegroup" <%=!isSourceXpath ? "checked=\"checked\" value=\"Key\"" : "value=\"Key\""%>/>
                        <fmt:message key="property.th"/>
                    </td>
                    <td></td>
                </tr>
                <tr id="mediator.callout.source.xpath" <%=!isSourceXpath ? "style=\"display:none\";" : ""%>>
                    <td><fmt:message key="mediator.callout.xpath"/></td>
                    <td><input type="text" id="source_xpath" name="mediator.callout.source.xpath_val" style="width:300px"
                               id="mediator.callout.source.xpath_val"
                               value="<%=sourceVal%>"/></td>
                    <td><a id="mediator.callout.source.xpath_nmsp_button" href="#"
                           onclick="showNameSpaceEditor('mediator.callout.source.xpath_val')" class="nseditor-icon-link"
                                   style="padding-left:40px">
                        <fmt:message key="mediator.callout.namespace"/></a>
                    </td>
                </tr>
                <tr id="mediator.callout.source.key" <%=isSourceXpath ? "style=\"display:none\";" : ""%>>
                    <td><fmt:message key="mediator.callout.key"/></td>
                    <td><input type="text" name="mediator.callout.source.key_val" style="width:300px"
                               id="mediator.callout.source.key_val" value="<%=sourceVal%>"/>
                    </td>
                    <%--<td>--%>
                        <%--<a href="#" class="registry-picker-icon-link"--%>
                        <%--onclick="showRegistryBrowser('mediator.callout.source.key_val','/_system/config')"><fmt:message key="registry.conf.browser"/></a>--%>
                        <%--<a href="#" class="registry-picker-icon-link"--%>
                        <%--onclick="showRegistryBrowser('mediator.callout.source.key_val','/_system/governance')"><fmt:message key="registry.gov.browser"/></a>--%>
                    <%--</td>--%>
                </tr>
        </table>
    </td>
    </tr>
    <tr>
    <td>
        <h3 class="mediator">Target <span class="required">*</span></h3>
        <table class="normal">
                <tr>
                    <td class="leftCol-small">
                        <fmt:message key="mediator.callout.specifyas"/> :
                    </td>
                    <td>
                        <input type="radio" id="targetGroupXPath"
                               onclick="displayElement('mediator.callout.target.xpath', true); displayElement('mediator.callout.target.namespace.editor', true); displayElement('mediator.callout.target.key', false);"
                               name="targetgroup" <%=isTargetXpath ? "checked=\"checked\" value=\"XPath\"" : "value=\"XPath\""%>/>
                        <fmt:message key="mediator.callout.xpath"/>
                        <input type="radio"
                               onclick="displayElement('mediator.callout.target.xpath', false); displayElement('mediator.callout.target.namespace.editor', false); displayElement('mediator.callout.target.key', true);"
                               name="targetgroup" <%=!isTargetXpath ? "checked=\"checked\" value=\"Key\"" : "value=\"Key\""%>/>
                        <fmt:message key="property.th"/>
                    </td>
                    <td/>
                </tr>
                <tr id="mediator.callout.target.xpath" <%=!isTargetXpath ? "style=\"display:none\";" : ""%>>
                    <td><fmt:message key="mediator.callout.xpath"/></td>
                    <td><input type="text" name="mediator.callout.target.xpath_val" style="width:300px"
                               id="mediator.callout.target.xpath_val"
                               value="<%=targetVal%>"/></td>
                    <td><a id="mediator.callout.target.xpath_nmsp_button" href="#"
                           onclick="showNameSpaceEditor('mediator.callout.target.xpath_val')" class="nseditor-icon-link"
                                   style="padding-left:40px">
                        <fmt:message key="mediator.callout.namespace"/></a>
                    </td>
                </tr>
                <tr id="mediator.callout.target.key" <%=isTargetXpath ? "style=\"display:none\";" : ""%>>
                    <td><fmt:message key="mediator.callout.key"/></td>
                    <td><input type="text" name="mediator.callout.target.key_val"
                               id="mediator.callout.target.key_val" value="<%=targetVal%>" style="width:300px"/>
                    </td>
                    <%--<td>--%>
                        <%--<a href="#" class="registry-picker-icon-link"--%>
                        <%--onclick="showRegistryBrowser('mediator.callout.target.key_val','/_system/config')">--%>
                            <%--<fmt:message key="registry.conf.browser"/></a>--%>
                        <%--<a href="#" class="registry-picker-icon-link"--%>
                        <%--onclick="showRegistryBrowser('mediator.callout.target.key_val','/_system/governance')">--%>
                            <%--<fmt:message key="registry.gov.browser"/></a>--%>
                    <%--</td>--%>
                </tr>
        </table>
    </td>
    </tr>
    </table>
</div>
</fmt:bundle>
