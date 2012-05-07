<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page import="org.wso2.carbon.rule.common.Rule" %>
<%@ page import="org.wso2.carbon.rule.common.RuleService" %>
<%@ page import="org.wso2.carbon.rule.common.RuleSet" %>
<%@ page import="org.wso2.carbon.rule.common.util.Constants" %>
<%@ page import="org.wso2.carbon.rule.ws.ui.wizard.RuleServiceAdminClient" %>
<%@ page import="org.wso2.carbon.rule.ws.ui.wizard.RuleServiceManagementHelper" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<script type="text/javascript" src="js/rule-services.js"></script>
<script type="text/javascript" src="js/ns-editor.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>
<%
    RuleServiceAdminClient ruleServiceAdminClient =
            new RuleServiceAdminClient(config.getServletContext(), session);
    RuleService serviceDescription =
            ruleServiceAdminClient.getRuleServiceDescription(request);
    RuleServiceManagementHelper.saveStep1(serviceDescription, request);
    Object ruleSourceAsObject = null;


    boolean isSource = true;
    boolean isRegistry = false;
    boolean isPath = false;
    boolean isURL = false;

    String path = (String) request.getSession().getAttribute("ruleScript");     // To get the uploaded file name
    String key = null;
    String url = null;


    if (serviceDescription != null) {
        RuleSet ruleSet = serviceDescription.getRuleSet();
        if (ruleSet != null) {
            List<Rule> ruleList = ruleSet.getRules();
            int i = 1;
            for (Rule rule : ruleList) {
                if (i == 1) {
                    ruleSourceAsObject = rule.getValue();

                    if (rule.getSourceType().equals("registry")) {
                        isSource = false;
                        isRegistry = true;
                        isPath = false;
                        isURL = false;
                        String[] vale = ruleSourceAsObject.toString().split(":");
                        String type = vale[0];
                        String keyValue = vale[1];
                        if (type.equals("conf")) {
                            key = "/_system/config/" + keyValue;

                        } else if (type.equals("gov")) {
                            key = "/_system/governance/" + keyValue;

                        }
                    } else if (rule.getSourceType().equals("file")) {
                        isSource = false;
                        isRegistry = false;
                        isPath = true;
                        isURL = false;
                        path = ruleSourceAsObject.toString();

                    } else if (rule.getSourceType().equals("url")) {
                        isSource = false;
                        isRegistry = false;
                        isPath = false;
                        isURL = true;
                        url = ruleSourceAsObject.toString();

                    }

                }
            }

        }


    }

    if(path != null && !"".equals(path)) {
            isSource = false;
            isPath = true;

    }

    if (ruleSourceAsObject == null || "".equals(ruleSourceAsObject)) {
        if (key != null && !"".equals(key.trim())) {
            isSource = false;
            isRegistry = true;
        }  else {
            if (url != null && !"".equals(url)) {
                isSource = false;
                isURL = true;

            }
        }
    }

    key = key == null ? "" : key;
    path = path == null ? "" : path;
    url = url == null ? "" : url;
    ruleSourceAsObject = ruleSourceAsObject == null ? "" : ruleSourceAsObject;

    String ruleSourceDisplay = isSource ? "" : "display:none;";
    String ruleKeyDisplay = isRegistry ? "" : "display:none;";
    String ruleUploadDisplay = isPath ? "" : "display:none;";
    String ruleURLDisplay = isURL ? "" : "display:none;";
    String rulesetCreationDisplay = "";

%>
<fmt:bundle basename="org.wso2.carbon.rule.ws.ui.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.rule.ws.ui.i18n.JSResources"
        request="<%=request%>" i18nObjectName="ruleservicejsi18n"/>
<carbon:breadcrumb
        label="step2.msg"
        resourceBundle="org.wso2.carbon.rule.ws.ui.i18n.Resources"
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
    function validateRuleFileUpload() {
        var fileName = document.ruleScriptUpload.ruleFilename.value;
        if (fileName == '') {
            CARBON.showErrorDialog('<fmt:message key="select.rule.script"/>');
        } else {
            document.ruleScriptUpload.submit();
        }
    }

    function validate() {
        var value;
        if (document.getElementById('ruleScriptTypeinlined').checked) {
            document.getElementById('ruleSourceType').value = "inlined";
            value = document.getElementById("ruleSourceInlined").value;
            value = value.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
            if (value == '') {
                CARBON.showErrorDialog('<fmt:message key="inlined.script.empty"/>');
                return false;
            }
        } else if (document.getElementById('ruleScriptTypekey').checked) {
            document.getElementById('ruleSourceType').value = "key";
            value = document.getElementById("ruleSourceKey").value;
            if (value == '') {
                CARBON.showErrorDialog('<fmt:message key="key.script.empty"/>');
                return false;
            }
        }
        else if (document.getElementById('ruleScriptTypeurl').checked) {
            document.getElementById('ruleSourceType').value = "url";
            value = document.getElementById("ruleSourceURL").value;
            if (value == '') {
                CARBON.showErrorDialog('<fmt:message key="url.script.empty"/>');
                return false;
            }
        } else {
            document.getElementById('ruleSourceType').value = "upload";
            value = document.getElementById("uploadedFileName").value;
            if (value == '') {
                CARBON.showErrorDialog('<fmt:message key="upload.script.empty"/>');
                return false;
            }
        }
        document.dataForm.submit();
        return true;
    }

    function onRegistryResourceSelect() {
        var ruleSourceKey = document.getElementById("ruleSourceKey").value;
        if (ruleSourceKey != undefined && ruleSourceKey != null) {
            document.getElementById("registryResourcePath").value = ruleSourceKey;
            var index = ruleSourceKey.indexOf("/_system/governance/");
            var configIndex = ruleSourceKey.indexOf("/_system/config/");
            if (index >= 0) {
                document.getElementById("ruleSourceKey").value =
                        ruleSourceKey.substring("/_system/governance/".length);
            } else if (configIndex >= 0) {
                document.getElementById("ruleSourceKey").value =
                        ruleSourceKey.substring("/_system/config/".length);
            }


        }
    }

</script>

<div id="middle">
<h2>
    <h2><fmt:message key="step2.msg"/></h2>
</h2>

<div id="workArea">
<table class="styledLeft">
    <thead>
    <tr>
        <th><fmt:message key="step2.msg"/></th>
    </tr>
    </thead>
    <tr>
        <td class="formRaw">
            <table class="normal">
                <tr>
                    <td><fmt:message key="rule.script.as"/><font color="red">*</font>
                    </td>
                    <td>
                        <%
                            if (isSource) {
                        %>
                        <input type="radio" name="ruleScriptType"
                               id="ruleScriptTypeinlined"
                               value="inlined"
                               onclick="setRuleScriptType('inlined');"
                               checked="checked"/>
                        <fmt:message key="inlined"/>
                        <input type="radio" name="ruleScriptType"
                               id="ruleScriptTypekey"
                               value="key"
                               onclick="setRuleScriptType('key');"/>
                        <fmt:message key="reg.key"/>
                        <input type="radio" name="ruleScriptType"
                               id="ruleScriptTypeUpload"
                               value="upload"
                               onclick="setRuleScriptType('upload');"/>
                        <fmt:message key="reg.upload"/>
                        <input type="radio" name="ruleScriptType"
                               id="ruleScriptTypeurl"
                               value="url"
                               onclick="setRuleScriptType('url');"/>
                        <fmt:message key="reg.url"/>
                        <% } else if (isRegistry) { %>
                        <input type="radio" name="ruleScriptType"
                               id="ruleScriptTypeinlined"
                               value="inlined"
                               onclick="setRuleScriptType('inlined');"/>
                        <fmt:message key="inlined"/>
                        <input type="radio" name="ruleScriptType"
                               id="ruleScriptTypekey"
                               value="key"
                               onclick="setRuleScriptType('key');"
                               checked="checked"/>
                        <fmt:message key="reg.key"/>
                        <input type="radio" name="ruleScriptType"
                               id="ruleScriptTypeUpload"
                               value="upload"
                               onclick="setRuleScriptType('upload');"/>
                        <fmt:message key="reg.upload"/>
                        <input type="radio" name="ruleScriptType"
                               id="ruleScriptTypeurl"
                               value="url"
                               onclick="setRuleScriptType('url');"/>
                        <fmt:message key="reg.url"/>


                        <% } else if (isPath) { %>

                        <input type="radio" name="ruleScriptType"
                               id="ruleScriptTypeinlined"
                               value="inlined"
                               onclick="setRuleScriptType('inlined');"/>
                        <fmt:message key="inlined"/>
                        <input type="radio" name="ruleScriptType"
                               id="ruleScriptTypekey"
                               value="key"
                               onclick="setRuleScriptType('key');"/>
                        <fmt:message key="reg.key"/>
                        <input type="radio" name="ruleScriptType"
                               id="ruleScriptTypeUpload"
                               value="upload"
                               onclick="setRuleScriptType('upload');"
                               checked="checked"/>
                        <fmt:message key="reg.upload"/>
                        <input type="radio" name="ruleScriptType"
                               id="ruleScriptTypeurl"
                               value="url"
                               onclick="setRuleScriptType('url');"/>
                        <fmt:message key="reg.url"/>


                        <%} else { %>
                        <input type="radio" name="ruleScriptType"
                               id="ruleScriptTypeinlined"
                               value="inlined"
                               onclick="setRuleScriptType('inlined');"/>
                        <fmt:message key="inlined"/>
                        <input type="radio" name="ruleScriptType"
                               id="ruleScriptTypekey"
                               value="key"
                               onclick="setRuleScriptType('key');"/>
                        <fmt:message key="reg.key"/>
                        <input type="radio" name="ruleScriptType"
                               id="ruleScriptTypeUpload"
                               value="upload"
                               onclick="setRuleScriptType('upload');"/>
                        <fmt:message key="reg.upload"/>

                        <input type="radio" name="ruleScriptType"
                               id="ruleScriptTypeurl"
                               value="url"
                               onclick="setRuleScriptType('url');"
                               checked="checked"/>
                        <fmt:message key="reg.url"/>


                        <%} %>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>


<table class="styledLeft">
    <tr id="ruleScriptUploadTR" style="<%=ruleUploadDisplay%>">
        <td><fmt:message key="rule.source.upload"/></td>
        <td class="formRow">
            <form method="post" name="ruleScriptUpload"
                  action="../../fileupload/facts"
                  enctype="multipart/form-data" target="_self">
                <table class="normal">
                    <tr>
                        <td>
                            <input type="file" id="ruleFilename"
                                   name="ruleFilename"
                                   size="75"/>
                        </td>
                        <td class="buttonRow">
                            <input type="hidden"
                                   value="<%=serviceDescription.getName()%>"
                                   name="ruleServiceName"/>
                            <input id="uploadedFileName" type="hidden" value="<%=path%>"/>
                            <input name="upload" type="button"
                                   class="button"
                                   value="<fmt:message key="upload"/> "
                                   onclick="validateRuleFileUpload();"/>
                        </td>
                    </tr>
                    <% if (isPath) {%>
                    <tr>
                        <td><%if (!"".equals(path)) {%>
                            <label><fmt:message
                                    key="uploaded.script"/></label>
                            : <%=path%>
                            <%} else { %><label><fmt:message
                                    key="not.yet.uploaded.script"/></label>
                            <%} %></td>
                    </tr>
                    <%} %>
                </table>
            </form>
        </td>
    </tr>
</table>



<table class="styledLeft">
    <form method="post" action="rule_service_wizard_step3.jsp" name="dataForm">
        <tr>
            <td class="formRaw">
                <table class="normal">
                    <tr id="ruleScriptKeyTR" style="<%=ruleKeyDisplay%>">
                        <td><fmt:message key="rule.source.key"/></td>
                        <td>
                            <input class="longInput" type="text" name="ruleSourceKey"
                                   id="ruleSourceKey"
                                   value="<%=isRegistry?key.trim():""%>"/>
                        </td>
                        <td>
                            <a href="#registryBrowserLink"
                               class="registry-picker-icon-link"
                               onclick="showResourceTree('ruleSourceKey',onRegistryResourceSelect,'/_system/config')"><fmt:message
                                    key="registry.config"/></a>
                        </td>
                        <td>
                            <a href="#registryBrowserLink"
                               class="registry-picker-icon-link"
                               onclick="showResourceTree('ruleSourceKey',onRegistryResourceSelect,'/_system/governance')"><fmt:message
                                    key="registry.governance"/></a>
                        </td>
                    </tr>

                    <tr id="ruleScriptURLTR" style="<%=ruleURLDisplay%>">
                        <td><fmt:message key="rule.source.url"/></td>
                        <td>
                            <input  type="text" name="ruleSourceURL"
                                   id="ruleSourceURL"  size="75"
                                   value="<%=isURL?url.trim():""%>"/>
                        </td>
                    </tr>

                    <tr id="ruleScriptSourceTR" style="<%=ruleSourceDisplay%>">
                        <td><fmt:message key="rule.source.inlined"/></td>
                        <td><textarea cols="80" rows="15"
                                      name="ruleSourceInlined"
                                      id="ruleSourceInlined"><%=ruleSourceAsObject.toString()%>
                        </textarea></td>
                    </tr>

                    <tr id="rulesetCreationTR" style="<%=rulesetCreationDisplay%>">
                        <td><label><fmt:message
                                key="rule.resource.type"/></label></td>
                        <td>
                            <select id="ruleResouceTypeID" name="ruleResouceType">
                                <option id="regularID" value="<%=Constants.RULE_RESOURCE_TYPE_REGULAR%>">
                                    <fmt:message key="rule.resource.type.regular"/>
                                </option>
                                <option id="dtableID" value="<%=Constants.RULE_RESOURCE_TYPE_DTABLE%>">
                                    <fmt:message key="rule.resource.type.dtable"/>
                                </option>
                            </select>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="buttonRow">
                <input type="hidden" id="stepID" name="stepID" value="step2"/>
                <input type="hidden" id="ruleSourceType" name="ruleSourceType"  value="inline"/>
                <input type="hidden" id="registryResourcePath" name="registryResourcePath" value="">
                <input type="hidden" id="ruleResourceURL" name="ruleResourceURL" value="">
                <input class="button" type="button" value="< <fmt:message key="back"/>"
                       onclick="location.href = 'rule_service_wizard_step1.jsp'"/>
                <input class="button" type="button" onclick="validate()"
                       value="<fmt:message key="next"/> >"/>
                <input class="button" type="button" value="<fmt:message key="cancel"/>"
                       onclick="location.href = 'cancel_handler.jsp'"/>

            </td>
        </tr>
    </form>
</table>


</div>
<a name="propertyEditorLink"></a>

<div id="propertyEditor" style="display:none;"></div>

<a name="registryBrowserLink"></a>

<div id="registryBrowser" style="display:none;"></div>

</div>

</fmt:bundle>
