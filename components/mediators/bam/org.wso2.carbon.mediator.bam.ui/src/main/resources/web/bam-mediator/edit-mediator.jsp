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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>



<%! public static final String PROPERTY_VALUES = "propertyValues";
    public static final String PROPERTY_KEYS = "propertyKeys";
%>







<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);

    
    if (!(mediator instanceof BamMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    BamMediator bamMediator = (BamMediator) mediator;

    String url = bamMediator.getServerUrl();
    String userName = bamMediator.getUserName();
    String password = bamMediator.getPassword();
    String port = bamMediator.getPort();

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

        <table class="normal" width="100%">
            <tbody>
            <tr>
                <td colspan="4">
                    <h2><fmt:message key="bam.mediator"/></h2>
                </td>
            </tr>


            <%
                 boolean isMediationStatsEnable =true;
                 //String url = "test url";
                 //String userName = "user name";
                 //String password = "password";
                 boolean isHttpTransportEnable = true;
                 boolean isSocketTransportEnable = true;
                 //String port = "port";
            %>




            <tr>
                <td>
                    <fmt:message key="config.key"/><span class="required">*</span>
                </td>
                <td>
                    <input class="longInput" type="text"
                           value="<%= bamMediator.getConfigKey() != null ? bamMediator.getConfigKey() : "" %>"
                           id="seq_ref" name="seq_ref" readonly="true"/>
                </td>
                <td>
                    <a href="#registryBrowserLink"
                       class="registry-picker-icon-link"
                       onclick="showRegistryBrowser('seq_ref','/_system/config')"><fmt:message key="conf.registry.browser"/>
                    </a>
                </td>
                <td>
                    <a href="#registryBrowserLink"
                       class="registry-picker-icon-link"
                       onclick="showRegistryBrowser('seq_ref','/_system/governance')"><fmt:message key="gov.registry.browser"/>
                    </a>
                </td>
            </tr>




            <tr>
                <td colspan="4">
                    <h3 class="mediator">
                        <fmt:message key="mediation.stats.configuration"/>
                    </h3>
                </td>

            </tr>


            <tr>
                <td>
                    <% if (isMediationStatsEnable) { %>
                    <input type="checkbox" name="enableMediationStats"
                           checked="true">&nbsp;&nbsp;&nbsp;&nbsp;
                    <% } else { %>
                    <input type="checkbox" name="enableMediationStats">&nbsp;&nbsp;&nbsp;&nbsp;
                    <% } %>
                    <fmt:message key="enable.mediation.stats"/>

                </td>
            </tr>

                <%--                    <% if (isServiceStatsEnable || isMsgDumpingEnable) { %>--%>
            <tr>
                <td colspan="4">
                    <h3 class="mediator">
                        <fmt:message key="bam.credential"/>
                    </h3>
                </td>
            </tr>
            <tr>
                <td><fmt:message key="bam.url"/></td>
                <td><input type="text" name="serverUrl" id="serverUrl" value="<%=url%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="username"/></td>
                <td><input type="text" name="userName" id="userName" value="<%=userName%>"/></td>
            </tr>
            <tr>
                <td><fmt:message key="password"/></td>
                <td><input type="password" name="password" id="password" value="<%=password%>"/></td>
            </tr>
            <tr>
                <td colspan="4">
                    <h3 class="mediator">
                        <fmt:message key="publisher.transport"/>
                    </h3>
                </td>
            </tr>
            <tr>
                <td>
                    <% if (isHttpTransportEnable) { %>
                    <input type="checkbox" name="enableHttpTransport" checked="true"/>
                    <% } else { %>
                    <input type="checkbox" name="enableHttpTransport"/>
                    <% } %>
                    <fmt:message key="http.transport"/>
                </td>
            </tr>
            <tr>
                <td>
                    <%if (isSocketTransportEnable) { %>
                    <input type="checkbox" name="enableSocketTransport"
                           onclick="showHideDiv('socketPortId')" checked="true"/>
                    <fmt:message key="socket.transport"/>
                </td>
                <td id="socketPortId">
                    <fmt:message key="port"/>
                    <input type="text" name="port" id="port" value="<%=port%>"/>
                </td>
                <%} else { %>
                <input type="checkbox" name="enableSocketTransport"
                       onclick="showHideDiv('socketPortId')"/>
                <fmt:message key="socket.transport"/>
                </td>
                <td id="socketPortId" style="display: none">
                    <fmt:message key="port"/>
                    <input type="text" name="port" value="<%=port%>"/>
                </td>
                <%}%>
            </tr>

            <tr>
                <td colspan="4">
                    <h3 class="mediator">
                        <fmt:message key="properties"/>
                    </h3>
                </td>
            </tr>
            <tbody>
            <tr>
                <%--<table id="propertyTable" width="100%" class="styledLeft" style="margin-left: 0px;">

                    <%  if (properties != null) {
                        int i = 1;
                        for(Property property : properties) {

                    %>
                    <tr id="propertyTable_<%=i%>">
                        <td>
                            <fmt:message key="property.name"/>
                            <input type="text" name="<%=PROPERTY_KEYS%>" value="<%=property.getKey()%>">
                        </td>
                        <td>
                            <fmt:message key="property.value"/>
                            <input type="text" name="<%=PROPERTY_VALUES%>" value="<%=property.getValue()%>">
                        </td>
                        <% if (i == 1) { %>
                        <td>
                            <a onClick='javaScript:addColumn()' style='background-image:
                                url(../bammediationstatpub/images/add.gif);'class='icon-link addIcon'>Add Property</a>
                        </td>
                        <% } else {  %>
                        <td>
                            <a onClick='javaScript:removeColumn("propertyTable_<%=i%>")' style='background-image:
                                url(../bammediationstatpub/images/delete.gif);'class='icon-link addIcon'>Remove Property</a>
                        </td>
                        <% } %>

                    </tr>
                    <script type="text/javascript">
                        rowNum++;
                    </script>
                    <%  i++;
                    }
                    } else { %>
                    >
                    <tr>
                        <td>
                            <fmt:message key="property.name"/>
                            <input type="text" name="<%=PROPERTY_KEYS%>" value="">
                        </td>
                        <td>
                            <fmt:message key="property.value"/>
                            <input type="text" name="<%=PROPERTY_VALUES%>" value="">
                        </td>

                        <td>
                            <a onClick='javaScript:addColumn()' style='background-image: url(../bammediationstatpub/images/add.gif);'class='icon-link addIcon'>Add Property</a>
                        </td>
                    </tr>


                    <% } %>
                </table>--%>
            </tr>






            </tbody>
        </table>
    </div>
</fmt:bundle>