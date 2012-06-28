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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.toolbox.deployer.stub.BAMToolboxDepolyerServiceStub" %>
<%@ page import="org.wso2.carbon.bam.toolbox.deployer.stub.BAMToolboxDepolyerServiceStub.BasicToolBox" %>
<%@ page import="org.wso2.carbon.bam.toolbox.deployer.ui.client.BAMToolBoxDeployerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.bam.toolbox.deployer.ui.i18n.Resources">
<script src="../editarea/edit_area_full.js" type="text/javascript"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>

<carbon:breadcrumb label="available.bam.tools"
                   resourceBundle="org.wso2.carbon.bam.toolbox.deployer.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>


<%
    String success = request.getParameter("success");
    String message = "";
    if (null != success && success.equalsIgnoreCase("false")) {
        message = request.getParameter("message");
    }

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    BAMToolBoxDeployerClient client = new BAMToolBoxDeployerClient(cookie, serverURL, configContext);
    BasicToolBox[] toolBoxes = null;
    try {
        toolBoxes = client.getAllBasicTools();
    } catch (Exception e) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog("Error while getting the status of BAM ToolBox");
</script>
<%
    }
%>

<script type="text/javascript">
    enableCustomToolBox();

    function deployToolBox() {
        var opt = document.getElementsByName('typeToolbox');
        var selected = '';
        for (var i = 0, length = opt.length; i < length; i++) {
            if (opt[i].checked) {
                selected = opt[i].value;
            }
        }
        document.getElementById('selectedToolType').value = selected;
        var toolbox = document.getElementById('toolbox').value;
        if (selected == 0) {
            if ('' == toolbox) {
                CARBON.showErrorDialog('No ToolBox has been selected!');
            } else if (toolbox.indexOf('.bar') == -1) {
                CARBON.showErrorDialog('The ToolBox should be \'bar\' artifact');
            } else {
                document.getElementById('uploadBar').submit();
            }
        } else {
            document.getElementById('uploadBar').submit();
        }
    }

    function cancelDeploy() {
        location.href = "../bam-toolbox/list.bar";
    }

    function enableCustomToolBox() {
        var opt = document.getElementsByName('typeToolbox');
        var selected = '';
        for (var i = 0, length = opt.length; i < length; i++) {
            if (opt[i].checked) {
                selected = opt[i].value;
            }
        }
        if (selected == '0' || selected == '') {
            document.getElementById('toolbox').disabled = false;
        }
        else {
            document.getElementById('toolbox').disabled = true;
        }
    }
</script>

<div id="middle">
    <h2>Add Tool Box</h2>

    <div id="workArea">
        <form id="uploadBar" name="uploadBar" enctype="multipart/form-data"
              action="../../fileupload/bamToolboxDeploy" method="POST">

            <table class="styledLeft">
                <thead>
                <tr>
                    <th colspan="4">
                        <fmt:message key="inbuilt.toolbox"/>
                    </th>
                </tr>
                </thead>
                <tbody>

                    <%--<tr>--%>
                    <%--<td width="10px">--%>
                    <%--1.--%>
                    <%--</td>--%>
                    <%--<td width="10px">--%>
                    <%--<input type="radio" name="typeToolbox" value="1" checked="true"--%>
                    <%--onclick="enableCustomToolBox();"/>--%>
                    <%--</td>--%>
                    <%--<td>--%>
                    <%--Message Tracing--%>
                    <%--</td>--%>
                    <%--<td>--%>
                    <%--This toolbox setup the model to trace the messages which are fired from a set of--%>
                    <%--servers to WSO2 BAM by deploying respective Hive scripts and default gadgets.--%>
                    <%--</td>--%>
                    <%--</tr>--%>


                <% int count = 1;
                    if (null != toolBoxes) {
                        for (BasicToolBox aToolbox : toolBoxes) {
                %>
                <tr>
                    <td width="10px">
                        <%=count%>.
                    </td>
                    <td width="10px">
                        <%
                            if (count == 1) {
                        %>
                        <input type="radio" name="typeToolbox" value="<%=aToolbox.getSampleId()%>"
                               onclick="enableCustomToolBox();" checked="true"/>
                        <%
                        } else {
                        %>
                        <input type="radio" name="typeToolbox" value="<%=aToolbox.getSampleId()%>"
                               onclick="enableCustomToolBox();"/>
                        <%
                            }
                        %>
                    </td>
                    <td>
                        <%=aToolbox.getDisplayName()%> <fmt:message key="toolbox"/>
                    </td>
                    <td>
                        <%=aToolbox.getDescription()%>
                    </td>
                    <%
                        count++;
                    %>
                </tr>

                <%
                        }
                    }
                %>

                    <%--<tr>--%>
                    <%--<td width="10px">--%>
                    <%--3.--%>
                    <%--</td>--%>
                    <%--<td width="10px">--%>
                    <%--<input type="radio" name="typeToolbox" value="3"--%>
                    <%--onclick="enableCustomToolBox();"/>--%>
                    <%--</td>--%>
                    <%--<td>--%>
                    <%--Service Data Monitoring--%>
                    <%--</td>--%>
                    <%--<td>--%>
                    <%--This Toolbox enable monitor  message activity from Service Hosting WSO2 Servers such as WSO2 AS, DSS, BPS, CEP, BRS and any other WSO2 Carbon server.--%>
                    <%--</td>--%>
                    <%--</tr>--%>

                    <%--<tr>--%>
                    <%--<td width="10px">--%>
                    <%--4.--%>
                    <%--</td>--%>
                    <%--<td width="10px">--%>
                    <%--<input type="radio" name="typeToolbox" value="4"--%>
                    <%--onclick="enableCustomToolBox();"/>--%>
                    <%--</td>--%>
                    <%--<td>--%>
                    <%--Mediation Data Monitoring--%>
                    <%--</td>--%>
                    <%--<td>--%>
                    <%--This Toolbox enable monitor message activity using Message Activity Mediators from the WSO2 ESB--%>
                    <%--</td>--%>
                    <%--</tr>--%>


                <tr>
                    <td colspan="4"></td>
                </tr>
                </tbody>
            </table>


            <table class="styledLeft">
                <thead>
                <tr>
                    <th colspan="4">
                        <fmt:message key="custom.toolbox"/>
                    </th>
                </tr>
                </thead>
                <tbody>


                <tr>
                    <td width="10px">
                    </td>
                    <td width="10px">
                        <input type="radio" name="typeToolbox" value="0" onclick="enableCustomToolBox();"/>
                    </td>
                    <td>
                        <nobr><fmt:message key="bar.artifact"/> <span
                                class="required">*</span>&nbsp;&nbsp;&nbsp;
                            <input type="file" name="toolbox"
                                   id="toolbox" size="80px" disabled="true"/>
                        </nobr>
                    </td>
                </tr>

                <tr>
                    <td colspan="4">
                        <table class="normal-nopadding">
                            <tbody>

                            <tr>
                                <td class="buttonRow" colspan="2">
                                    <input type="button" value="<fmt:message key="deploy"/>"
                                           class="button" name="deploy"
                                           onclick="javascript:deployToolBox();"/>
                                    <input type="button" value="<fmt:message key="cancel"/>"
                                           name="cancel" class="button"
                                           onclick="javascript:cancelDeploy();"/>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>
                <input type="hidden" id="selectedToolType" name="selectedToolType"/>
                </tbody>
            </table>
        </form>
    </div>
</div>


</fmt:bundle>
