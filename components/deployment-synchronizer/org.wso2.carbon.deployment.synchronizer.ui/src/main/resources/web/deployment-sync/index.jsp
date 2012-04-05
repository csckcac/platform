<!--
 ~ Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.deployment.synchronizer.ui.client.DeploymentSyncAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.deployment.synchronizer.stub.types.util.DeploymentSynchronizerConfiguration" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.deployment.synchronizer.ui.i18n.Resources">
    <carbon:breadcrumb
            label="deployment.sync.menu.text"
            resourceBundle="org.wso2.carbon.deployment.synchronizer.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript">
        function disable() {
            var form = document.getElementById('sync_form');
            form.action = 'index.jsp?action=disable';
            form.submit();
            return true;
        }

        function update() {
            var period = document.getElementById('syncPeriod').value;
            if (period == null || period == '') {
                CARBON.showErrorDialog('Synchronization period has not been specified');
                return false;
            }
            if (isNaN(period) || period <= 0) {
                CARBON.showErrorDialog('Invalid value for synchronization period');
                return false;
            }
            var form = document.getElementById('sync_form');
            form.action = 'index.jsp?action=update';
            form.submit();
            return true;
        }

        function enable() {
            var period = document.getElementById('syncPeriod').value;
            if (period == null || period == '') {
                CARBON.showErrorDialog('Synchronization period has not been specified');
                return false;
            }
            if (isNaN(period) || period <= 0) {
                CARBON.showErrorDialog('Invalid value for synchronization period');
                return false;
            }
            var form = document.getElementById('sync_form');
            form.action = 'index.jsp?action=enable';
            form.submit();
            return true;
        }

        function enableEventingCheckbox() {
            var checkbox = document.getElementById('auto.checkout.chkbox');
            if (checkbox.checked) {
                document.getElementById('use.eventing.chkbox').removeAttribute('disabled');
            } else {
                document.getElementById('use.eventing.chkbox').setAttribute('disabled', 'true');
            }
            return true;
        }

        function getLastCommitTime() {
            jQuery.get('statusCheck-ajaxprocessor.jsp', { 'mode' : 'commit' },
                    function(data, status) {
                        var text;
                        if (data == 'error') {
                            text = 'Unable to get the last commit time';
                        } else {
                            text = data;
                        }
                        document.getElementById('lastCommitTimeCell').innerHTML = text;
                    });
            var timer1 = setTimeout("getLastCommitTime()",10000);
        }

        function getLastCheckoutTime() {
            jQuery.get('statusCheck-ajaxprocessor.jsp', { 'mode' : 'checkout' },
                    function(data, status) {
                        var text;
                        if (data == 'error') {
                            text = 'Unable to get the last checkout time';
                        } else {
                            text = data;
                        }
                        document.getElementById('lastCheckoutTimeCell').innerHTML = text;
                    });
            var timer2 = setTimeout("getLastCheckoutTime()",10000);
        }

        function commit() {
            var form = document.getElementById('sync_form');
            form.action = 'index.jsp?action=commit';
            form.submit();
            return true;
        }

        function checkout() {
            var form = document.getElementById('sync_form');
            form.action = 'index.jsp?action=checkout';
            form.submit();
            return true;
        }
    </script>

    <%
        DeploymentSynchronizerConfiguration synchronizerConfiguration = null;
        boolean syncPerformed = false;
        try {
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            DeploymentSyncAdminClient client = new DeploymentSyncAdminClient(
                    configContext, backendServerURL, cookie, request.getLocale());

            boolean submitted = Boolean.parseBoolean(request.getParameter("submitted"));
            if (submitted) {
                String action = request.getParameter("action");
                if ("disable".equals(action)) {
                    client.disableSynchronizer();
                } else if ("enable".equals(action)) {
                    DeploymentSynchronizerConfiguration newConfig = new DeploymentSynchronizerConfiguration();
                    newConfig.setEnabled(true);
                    newConfig.setAutoCommit(request.getParameter("autoCommit") != null);
                    newConfig.setAutoCheckout(request.getParameter("autoCheckout") != null);
                    newConfig.setPeriod(Long.parseLong(request.getParameter("syncPeriod")));
                    newConfig.setUseEventing(request.getParameter("useEventing") != null);
                    newConfig.setRepositoryType("registry"); // TODO: Get this via user input
                    client.enableSynchronizer(newConfig);
                } else if ("update".equals(action)) {
                    DeploymentSynchronizerConfiguration newConfig = new DeploymentSynchronizerConfiguration();
                    newConfig.setEnabled(true);
                    newConfig.setAutoCommit(request.getParameter("autoCommit") != null);
                    newConfig.setAutoCheckout(request.getParameter("autoCheckout") != null);
                    newConfig.setPeriod(Long.parseLong(request.getParameter("syncPeriod")));
                    newConfig.setUseEventing(request.getParameter("useEventing") != null);
		    newConfig.setRepositoryType("registry"); // TODO: Get this via user input
                    client.updateSynchronizer(newConfig);
                } else if ("commit".equals(action)) {
                    if (client.getConfiguration().getEnabled()) {
                        client.commit();
                        syncPerformed = true;
                    }
                } else if ("checkout".equals(action)) {
                    if (client.getConfiguration().getEnabled()) {
                        client.checkout();
                        syncPerformed = true;
                    }
                }
            }

            synchronizerConfiguration = client.getConfiguration();
        } catch (Exception e) {
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
    %>
        <script type="text/javascript">
               location.href = "../admin/error.jsp";
        </script>
    <%
            synchronizerConfiguration = new DeploymentSynchronizerConfiguration();
        }
    %>

    <div id="middle">
        <h2><fmt:message key="deployment.sync.menu.text"/></h2>
        <div id="workArea">
            <p>
                <%
                    if (synchronizerConfiguration.getEnabled()) {
                %>
                <font color="green"><fmt:message key="deployment.sync.enabled"/></font>
                <%
                    } else {
                %>
                <font color="red"><fmt:message key="deployment.sync.disabled"/></font>
                <%
                    }
                %>
            </p>
            <p>&nbsp;</p>
            <form id="sync_form" action="" method="POST">
                <input name="submitted" type="hidden" value="true"/>
                <table id="syncTable" class="styledLeft">
                    <thead>
                        <tr>
                            <th colspan="2"><fmt:message key="deployment.sync.config"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td width="30%"><fmt:message key="deployment.sync.auto.commit"/></td>
                            <td>
                                <input id="auto.commit.chkbox" type="checkbox" name="autoCommit"/>
                                <%
                                    if (synchronizerConfiguration.getAutoCommit()) {
                                %>
                                <script type="text/javascript">
                                    document.getElementById('auto.commit.chkbox').setAttribute('checked', 'true');
                                </script>
                                <%
                                    }
                                %>
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="deployment.sync.auto.checkout"/></td>
                            <td>
                                <input id="auto.checkout.chkbox" type="checkbox" name="autoCheckout" onclick="enableEventingCheckbox();"/>
                                <%
                                    if (synchronizerConfiguration.getAutoCheckout()) {
                                %>
                                <script type="text/javascript">
                                    document.getElementById('auto.checkout.chkbox').setAttribute('checked', 'true');
                                </script>
                                <%
                                    }
                                %>
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="deployment.sync.use.eventing"/></td>
                            <td>
                                <input id="use.eventing.chkbox" type="checkbox" name="useEventing"/>
                                <%
                                    if (synchronizerConfiguration.getUseEventing()) {
                                %>
                                <script type="text/javascript">
                                    document.getElementById('use.eventing.chkbox').setAttribute('checked', 'true');
                                </script>
                                <%
                                    }

                                    if (!synchronizerConfiguration.getAutoCheckout()) {
                                %>
                                <script type="text/javascript">
                                    document.getElementById('use.eventing.chkbox').setAttribute('disabled', 'true');
                                </script>
                                <%
                                    }
                                %>
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="deployment.sync.period"/></td>
                            <td><input type="text" value="<%=synchronizerConfiguration.getPeriod()%>" name="syncPeriod" id="syncPeriod"/> s</td>
                        </tr>
                        <%
                            if (!"svn".equals(synchronizerConfiguration.getRepositoryType())) {
                        %>
                        <tr>
                            <td class="buttonRow" colspan="2">
                                <%
                                    if (synchronizerConfiguration.getEnabled()) {
                                %>
                                <button class="button" onclick="update()"><fmt:message key="deployment.sync.update"/></button>
                                <button class="button" onclick="disable(); return false;"><fmt:message key="deployment.sync.disable"/></button>
                                <%
                                } else {
                                %>
                                <button class="button" onclick="enable(); return false;"><fmt:message key="deployment.sync.enable"/></button>
                                <%
                                    }
                                %>
                            </td>
                        </tr>
                        <%
                            }
                        %>
                    </tbody>
                </table>
                <p>&nbsp;</p>
                <%
                    if (synchronizerConfiguration.getEnabled()) {
                %>
                    <table id="commitStatusTable" class="styledLeft">
                        <thead>
                            <tr>
                                <th colspan="2"><fmt:message key="deployment.sync.commit.status"/></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td width="30%"><fmt:message key="deployment.sync.auto.commit"/></td>
                                <td>
                                    <%
                                        if (synchronizerConfiguration.getAutoCommit()) {
                                    %>
                                    <font color="green"><fmt:message key="deployment.sync.on"/></font>
                                    <%
                                        } else {
                                    %>
                                    <font color="red"><fmt:message key="deployment.sync.off"/></font>
                                    <%
                                        }
                                    %>
                                </td>
                            </tr>
                            <tr>
                                <td><fmt:message key="deployment.sync.last.commit"/></td>
                                <td id="lastCommitTimeCell"></td>
                            </tr>
                            <tr>
                                <td class="buttonRow" colspan="2">
                                    <button class="button" onclick="commit()"><fmt:message key="deployment.sync.commit.now"/></button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <p>&nbsp;</p>
                    <table id="checkoutStatusTable" class="styledLeft">
                        <thead>
                            <tr>
                                <th colspan="2"><fmt:message key="deployment.sync.checkout.status"/></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td width="30%"><fmt:message key="deployment.sync.auto.checkout"/></td>
                                <td>
                                    <%
                                        if (synchronizerConfiguration.getAutoCheckout()) {
                                    %>
                                    <font color="green"><fmt:message key="deployment.sync.on"/></font>
                                    <%
                                        } else {
                                    %>
                                    <font color="red"><fmt:message key="deployment.sync.off"/></font>
                                    <%
                                        }
                                    %>
                                </td>
                            </tr>
                            <tr>
                                <td><fmt:message key="deployment.sync.last.checkout"/></td>
                                <td id="lastCheckoutTimeCell"></td>
                            </tr>
                            <tr>
                                <td class="buttonRow" colspan="2">
                                    <button class="button" onclick="checkout()"><fmt:message key="deployment.sync.checkout.now"/></button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                <%
                    }
                %>
            </form>
        </div>
    </div>

    <script type="text/javascript">
        alternateTableRows('syncTable', 'tableEvenRow', 'tableOddRow');
    </script>

    <%
        if (synchronizerConfiguration.getEnabled()) {
    %>
        <script type="text/javascript">
            alternateTableRows('commitStatusTable', 'tableEvenRow', 'tableOddRow');
            alternateTableRows('checkoutStatusTable', 'tableEvenRow', 'tableOddRow');
            getLastCommitTime();
            getLastCheckoutTime();
        </script>
    <%
        }

        if (syncPerformed) {
    %>
        <script type="text/javascript">
            CARBON.showInfoDialog('Synchronization operation performed successfully');
        </script>
    <%
        }
    %>

</fmt:bundle>
