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

<%@ page import="org.wso2.carbon.mediator.bam.ui.BamMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="java.util.List" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@ page import="org.wso2.carbon.bam.mediationstats.data.publisher.stub.conf.Property" %>
<%@ page import="java.util.ArrayList" %>


<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);

    
    if (!(mediator instanceof BamMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    BamMediator bamMediator = (BamMediator) mediator;

    String configuration = "";
    String streamName = "";
    String streamVersion = "";


    if(bamMediator.getServerProfilePath() != null){
        configuration = bamMediator.getServerProfilePath();
    }

    if(bamMediator.getStreamName() != null){
        streamName = bamMediator.getStreamName();
    }

    if(bamMediator.getStreamVersion() != null){
        streamVersion = bamMediator.getStreamVersion();
    }

    /*String inputType = bamMediator.getInputType();
    String outputType = bamMediator.getOutputType();

    String inputExpr = "";
    if (bamMediator.getInputExpression() != null) {
        inputExpr = bamMediator.getInputExpression().toString();
    }

    String outputExpr = "";
    if (bamMediator.getOutputExpression() != null) {
        outputExpr = bamMediator.getOutputExpression().toString();
    }

    String outputProperty = "";
    if (bamMediator.getOutputProperty() != null) {
        outputProperty = bamMediator.getOutputProperty();
    }

    String outputAction = "";
    if (bamMediator.getOutputAction() != null) {
        outputAction = bamMediator.getOutputAction();
    }

    boolean isExpression = bamMediator.getOutputProperty() == null;

    NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
    nameSpacesRegistrar.registerNameSpaces(bamMediator.getInputExpression(), "inputExpr", session);

    nameSpacesRegistrar.registerNameSpaces(bamMediator.getOutputExpression(), "outputExpr", session);*/
%>

<fmt:bundle basename="org.wso2.carbon.mediator.bam.ui.i18n.Resources">
    <carbon:jsi18n
		resourceBundle="org.wso2.carbon.mediator.bam.ui.i18n.JSResources"
		request="<%=request%>" i18nObjectName="propertyMediatorJsi18n"/>
    <div>
        <script type="text/javascript" src="../bam-mediator/js/mediator-util.js"></script>
        <%--<script id="source" type="text/javascript">
            function showHideDiv(divId) {
                var theDiv = document.getElementById(divId);
                if (theDiv.style.display == "none") {
                    theDiv.style.display = "";
                } else {
                    theDiv.style.display = "none";
                }
            }

            var rowNum = 1;

            function addColumn() {
                rowNum++;
                /*var n =  + parseInt(trId.charAt(trId.length-1))+1;
                 jQuery("#"+trId+" td div.addIcon").remove();*/
                //alert(n);
                var sId = "propertyTable_" + rowNum;
                //alert(sId);
                var tableContent = "<tr id=\"" + sId + "\">" +
                                   "<td>\n" +
                                   "                        <fmt:message key='property.name'/>\n" +
                                   "                        <input type=\"text\" name=\"<%=PROPERTY_KEYS%>\" value=\"\">\n" +
                                   "                    </td>\n" +
                                   "                    <td>\n" +
                                   "                        <fmt:message key='property.value'/>\n" +
                                   "                        <input type=\"text\" name=\"<%=PROPERTY_VALUES%>\" value=\"\">\n" +
                                   "                    </td>" +
                                   "<td>\n" +
                                   "                        <a onClick='javaScript:removeColumn(\"" + sId + "\")'" +
                                   "style='background-image: url(../bampubsvcstat/images/delete.gif);'class='icon-link addIcon'>Remove Property</a>\n" +
                                   "                    </td>" +
                                   "</tr>";

                jQuery("#propertyTable").append(tableContent);
                updatePropertyTableData();
            }

            function removeColumn(id) {
                jQuery("#" + id).remove();
                updatePropertyTableData();
            }

            function updatePropertyTableData(){
                var tableData = "", inputs, numOfInputs;
                inputs = document.getElementById("propertyTable").getElementsByTagName("input");
                numOfInputs = inputs.length;
                for(var i=0; i<numOfInputs; i=i+2){
                    if(inputs[i].value != "" && inputs[i+1].value != ""){
                        tableData = tableData + inputs[i].value + ":" + inputs[i+1].value + ";";
                    }
                    /*tableData = tableData + jQuery("#propertyName")[i].value + ":"
                                        + jQuery("#propertyValue")[i].value + ";" ;*/
                    /*tableData[i]["name"] = jQuery("#propertyName")[i].value;
                    tableData[i]["value"] = jQuery("#propertyValue")[i].value;*/
                }
                document.getElementById("hfPropertyTableData").value = tableData;
                alert("hf table : " + document.getElementById("hfPropertyTableData").value);
            }
        </script>--%>

        <table class="normal" width="100%">
            <tbody>
            <tr>
                <td colspan="4">
                    <h2><fmt:message key="bam.mediator"/></h2>
                </td>
            </tr>


            <tr>
                <td colspan="4">
                    <h3 class="mediator">
                        <fmt:message key="server.profile.header"/>
                    </h3>
                </td>

            </tr>

            <tr>
                <td>
                    <fmt:message key="server.profile"/><span class="required">*</span>
                </td>
                <td>
                    <input class="longInput" type="text"
                           value="<%=configuration%>"
                           id="serverProfile" name="serverProfile" readonly="true"/>
                </td>
                <td>
                    <a href="#registryBrowserLink"
                       class="registry-picker-icon-link"
                       onclick="showRegistryBrowser('serverProfile','/_system/config')"><fmt:message key="conf.registry.browser"/>
                    </a>
                </td>
                <%--<td>
                    <a href="#registryBrowserLink"
                       class="registry-picker-icon-link"
                       onclick="showRegistryBrowser('serverProfile','/_system/governance')"><fmt:message key="gov.registry.browser"/>
                    </a>
                </td>--%>
            </tr>

            <tr>
                <td colspan="4">
                    <h3 class="mediator">
                        <fmt:message key="stream.configuration"/>
                    </h3>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="stream.name"/></td>
                <td><input type="text" name="streamName" id="streamName" value="<%=streamName%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="stream.version"/></td>
                <td><input type="text" name="streamVersion" id="streamVersion" value="<%=streamVersion%>"/></td>
            </tr>

        </table>
    </div>
</fmt:bundle>