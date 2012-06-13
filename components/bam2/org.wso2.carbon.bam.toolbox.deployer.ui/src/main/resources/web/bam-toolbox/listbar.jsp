<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.toolbox.deployer.stub.BAMToolboxDepolyerServiceStub.ToolBoxStatusDTO" %>
<%@ page import="org.wso2.carbon.bam.toolbox.deployer.ui.client.BAMToolBoxDeployerClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.bam.toolbox.deployer.ui.i18n.Resources">
    <script type="text/javascript">
        function deleteRow(name, msg) {
            CARBON.showConfirmationDialog(msg + "' " + name + " ' ?", function() {
                document.location.href = "undeploy.jsp?" + "toolBoxName=" + name;
            });
        }
    </script>
    <%
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        BAMToolBoxDeployerClient client = new BAMToolBoxDeployerClient(cookie, serverURL, configContext);
        String[] deployedTools = null;
        String[] toBeDeployedTools = null;
        String[] toBeUndeployedTools = null;

        String message = request.getParameter("message");
        String success = request.getParameter("undeploysuccess");

        try {
            ToolBoxStatusDTO statusDTO = client.getToolBoxStatus();
            deployedTools = statusDTO.getDeployedTools();
            toBeDeployedTools = statusDTO.getToBeDeployedTools();
            toBeUndeployedTools = statusDTO.getToBeUndeployedTools();

        } catch (Exception e) {
    %>
    <script type="text/javascript">
        CARBON.showErrorDialog("Error while getting the status of BAM ToolBox");
    </script>
    <%
        }
    %>
    <%
        if (null != success && !success.equals("")) {
            if (success.equalsIgnoreCase("true")) {
    %>
    <script type="text/javascript">
        CARBON.showInfoDialog('<%=message%>');
    </script>
    <%
    } else {
    %>
    <script type="text/javascript">
        CARBON.showErrorDialog('<%=message%>');
    </script>
    <%
            }
        }
    %>
    <div id="middle">
        <h2>Available BAM Tool Boxes</h2>

        <div id="workArea">

            <form id="listTools" name="listTools" action="" method="POST">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th colspan="4"><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="bam.toolboxes"/></span>
                        </th>
                    </tr>
                    </thead>
                    <tbody>

                    <% if (null != deployedTools) {
                        for (String aName : deployedTools) {
                    %>
                    <tr>
                        <td width="60%"><label>
                            <%=aName%>
                        </label>
                        </td>
                        <td width="20%"><fmt:message key="bam.tool.status.deployed"></fmt:message>
                        </td>
                        <td width="20%">
                            <a onclick="deleteRow('<%=aName%>','Do you want to Undeploy toolbox ')"
                               class="delete-icon-link" href="#"><fmt:message key="bam.undeploy"/></a>
                        </td>

                    </tr>
                    <%
                            }
                        }
                    %>

                    <% if (null != toBeDeployedTools) {
                        for (String aName : toBeDeployedTools) {
                    %>
                    <tr>
                        <td width="60%"><label>
                            <%=aName%>
                        </label>
                        </td>
                        <td width="20%"><fmt:message key="bam.tool.status.tobedeployed"></fmt:message>
                        </td>
                        <td width="20%">
                        </td>

                    </tr>
                    <%
                            }
                        }
                    %>

                    <% if (null != toBeUndeployedTools) {
                        for (String aName : toBeUndeployedTools) {
                    %>
                    <tr>
                        <td width="60%"><label>
                            <%=aName%>
                        </label>
                        </td>
                        <td width="20%"><fmt:message key="bam.tool.status.tobeundeployed"></fmt:message>
                        </td>
                        <td width="20%">
                        </td>

                    </tr>
                    <%
                            }
                        }
                    %>
                    <%
                        if ((null == deployedTools || deployedTools.length == 0) &&
                                (null == toBeDeployedTools || toBeDeployedTools.length == 0) &&
                                (null == toBeUndeployedTools || toBeUndeployedTools.length == 0)) {
                    %>
                    <tr>
                        <td><b>No Tool Boxes Found.</b></td>
                    </tr>
                    <%
                        }
                    %>
                    </tbody>
                </table>
            </form>
        </div>
    </div>


</fmt:bundle>
