<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ page import="org.apache.axis2.client.Options" %>
<%@ page import="org.apache.axis2.client.ServiceClient" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.CEPAdminServiceStub" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.util.LinkedList" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.*" %>
<fmt:bundle basename="org.wso2.carbon.cep.ui.i18n.Resources">

<link type="text/css" href="../CEP/css/buckets.css" rel="stylesheet"/>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../CEP/js/cep_buckets.js"></script>
"/>

<%--Includes for registry browser--%>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>
<script type="text/javascript" src="js/expression_utils.js"></script>
<%

    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    //Server URL which is defined in the server.xml
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                                                 session) + "CEPAdminService.CEPAdminServiceHttpsSoap12Endpoint";
    CEPAdminServiceStub stub = new CEPAdminServiceStub(configContext, serverURL);

    String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);

    ServiceClient client = stub._getServiceClient();
    Options option = client.getOptions();
    option.setManageSession(true);
    option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);


    String queryIndex = request.getParameter("index");
    int index = Integer.parseInt(queryIndex);
    QueryDTO query = null;
    ExpressionDTO expression = null;
    OutputDTO output = null;
    OutputElementMappingDTO elementMapping = null;
    OutputXMLMappingDTO xmlMapping = null;
    OutputTupleMappingDTO tupleMapping = null;
    OutputMapMappingDTO mapMapping = null;

    LinkedList<QueryDTO> queries = (LinkedList<QueryDTO>) session.getAttribute("queries");
    query = queries.get(index);

    expression = query.getExpression();
    output = query.getOutput();
    if (output != null) {
        elementMapping = output.getOutputElementMapping();
        xmlMapping = output.getOutputXmlMapping();
        tupleMapping = output.getOutputTupleMappingDTO();
        mapMapping = output.getOutputMapMappingDTO();
    }

    boolean inline;
    if (expression.getType().equals("registry")) {
        inline = false;
    } else {
        inline = true;
    }
    String inlineDisplay = inline ? "" : "display:none;";
    String registryDisplay = inline ? "display:none;" : "";
    String key = expression.getText();

    boolean isViewingBucket = false;
    String viewingBucket = request.getParameter("view");
    if (viewingBucket != null) {
        isViewingBucket = true;
    }
%>

<div id="middle">
<h2><fmt:message key="query"/></h2>

<div id="workArea">
<table class="styledLeft noBorders spacer-bot" style="width:100%">
<tbody>

<tr>
    <td class="leftCol-small"><fmt:message key="query.name"/><span class="required">*</span></td>
    <td><input type="text" id="queryName" value="<%= query.getName()%>"></td>
</tr>

<tr>
    <td><fmt:message key="query.as"/><span class="required">*</span>
    </td>
    <td>
        <%
            if (inline) {
        %>
        <input type="radio" name="expressionType"
               id="expressioninlinedRd"
               value="inline"
               onclick="setExpressionType('inline');"
               checked="checked"/>
        <fmt:message key="inlined"/>
        <input type="radio" name="expressionType"
               id="expressionRegistryRd"
               value="registry"
               onclick="setExpressionType('registry');"/>
        <fmt:message key="reg.key"/>
        <% } else { %>
        <input type="radio" name="expressionType"
               id="expressioninlinedRd"
               value="inline"
               onclick="setExpressionType('inline');"/>
        <fmt:message key="inlined"/>
        <input type="radio" name="expressionType"
               id="expressionRegistryRd"
               value="registry"
               checked="checked"
               onclick="setExpressionType('registry');"/>
        <fmt:message key="reg.key"/>
        <%
            }
        %>
    </td>
</tr>
<tr id="expressionInlined" style="<%=inlineDisplay%>">
    <td style="vertical-align:top !important;"><fmt:message key="query.expression"/><span
            class="required">*</span>
    </td>
    <td>
        <textarea id="querySource"
                  style="border:solid 1px rgb(204, 204, 204); width: 99%; height: 275px; margin-top: 5px;"
                  name="querySource" rows="30"><%=expression.getText() %>
        </textarea>
    </td>
</tr>
<tr id="expressionRegistry" style="<%=registryDisplay%>">
    <td class="leftCol-small"><fmt:message key="query.expression.key"/></td>
    <td>
        <input class="longInput" type="text" name="expressionKey"
               id="expressionKey"
               value="<%=(!inline)?key.trim():""%>"/>

        <a href="#registryBrowserLink"
           class="registry-picker-icon-link"
           onclick="showRegistryBrowser('expressionKey','/_system/config')"><fmt:message
                key="registry.config"/></a>
    </td>
</tr>
<tr>
    <td class="middle-header" colspan="2">
        <fmt:message key="output"/>
    </td>
</tr>
<tr>
    <td><fmt:message key="output.topic"/><font color="red">*</font></td>
    <td><input type="text" id="newTopic"
               value="<%=output!=null&&output.getTopic()!=null?output.getTopic():"" %>"></td>
</tr>
<tr>
    <td class="leftCol-small"><fmt:message key="broker.name"/></td>
    <td><select name="outputBrokerName" id="outputBrokerName">
        <%
            String[] brokerNames = stub.getBrokerNames();
            if (brokerNames != null && brokerNames.length > 0) {
                for (String brokerName : brokerNames) {
                    if (output != null && brokerName.equals(output.getBrokerName())) {
        %>
        <option value="<%=brokerName%>" selected="selected"><%=brokerName%>
        </option>
        <%
        } else {
        %>
        <option value="<%=brokerName%>"><%=brokerName%>
        </option>
        <%
                    }
                }
            }
        %>
    </select>
    </td>
</tr>
<tr>
    <td class="leftCol-small"><fmt:message key="output.mapping"/></td>
    <td><select name="outputBrokerName" id="outputMapping" onchange="setOutputMapping()">
        <option value="xml" <%=xmlMapping != null ? "selected=\"selected\"" : "" %>><fmt:message
                key="xml.mapping"/></option>
        <option value="element" <%=elementMapping != null ? "selected=\"selected\"" : "" %>>
            <fmt:message key="element.mapping"/></option>
        <option value="tuple" <%=tupleMapping != null ? "selected=\"selected\"" : "" %>><fmt:message
                key="tuple.mapping"/></option>
        <option value="tuple" <%=mapMapping != null ? "selected=\"selected\"" : "" %>><fmt:message
                key="map.mapping"/></option>
    </select>
    </td>
</tr>
<tr name="outputXMLMapping"
    style="display:<%=xmlMapping!=null||(xmlMapping==null&&elementMapping==null&&tupleMapping==null)?"":"none" %>">
    <td colspan="2" class="middle-header">
        <fmt:message key="xml.mapping"/>
    </td>
</tr>
<tr name="outputXMLMapping"
    style="display:<%=xmlMapping!=null||(xmlMapping==null&&elementMapping==null&&tupleMapping==null)?"":"none" %>">
    <td colspan="2">
        <p><fmt:message key="xml.mapping.text"/></P>

        <p>
            <textarea id="xmlSourceText"
                      style="border:solid 1px rgb(204, 204, 204); width: 99%;
                                     height: 150px; margin-top: 5px;"
                      name="xmlSource"
                      rows="30"><%=xmlMapping != null ? xmlMapping.getMappingXMLText() : ""%>
            </textarea>
        </p>
    </td>
</tr>
<tr name="outputElementMapping" style="display:<%=elementMapping!=null?"":"none" %>">
    <td colspan="2" class="middle-header">
        <fmt:message key="element.mapping"/>
    </td>
</tr>
<tr name="outputElementMapping" style="display:<%=elementMapping!=null?"":"none" %>">
    <td colspan="2">
        <table>
            <tbody>
            <td class="leftCol-small"><fmt:message key="element.mapping.xmlDocumentElement"/></td>
            <td><input type="text" id="documentElement"
                       value="<%=elementMapping!=null?elementMapping.getDocumentElement():""%>">
            </td>
            <td><fmt:message key="element.mapping.namespace"/></td>
            <td><input type="text" id="namespace"
                       value="<%=elementMapping!=null?elementMapping.getNamespace():""%>"></td>
            </tbody>
        </table>
        <h4><fmt:message key="property"/></h4>
        <table class="styledLeft" id="outputElementPropertyTable"
               style="display:<%=elementMapping!=null&&elementMapping.getProperties() != null?"":"none" %>">
            <thead>
            <th class="leftCol-med"><fmt:message key="property.name"/></th>
            <th class="leftCol-med"><fmt:message key="xml.field.name"/></th>
            <th class="leftCol-med"><fmt:message key="xml.field.type"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
            <%
                if (elementMapping != null && elementMapping.getProperties() != null) {
            %>
            <tbody>
            <%
                XMLPropertyDTO[] properties = elementMapping.getProperties();
                for (XMLPropertyDTO property : properties) {
            %>
            <tr>
                <td><%=property.getName()%>
                </td>
                <td><%=property.getXmlFieldName()%>
                </td>
                <td><%=property.getXmlFieldType()%>
                </td>
                <td><a class="icon-link"
                       style="background-image:url(../admin/images/delete.gif)"
                       onclick="removeOutputProperty(this,'xml')">Delete</a>
                    <script type="text/javascript">
                        addOutputMappingPropertyToSession('<%=property.getName()%>', '<%=property.getXmlFieldName()%>', '<%=property.getXmlFieldType()%>');
                    </script>
                </td>
            </tr>

            <%
                }
            %>
            </tbody>
            <%
                }
            %>
        </table>
        <div class="noDataDiv-plain" id="noOutputElementPropertyDiv"
             style="display:<%=elementMapping!=null&&elementMapping.getProperties() != null?"none":"" %>">
            No XPath prefixes Defined
        </div>
        <table id="addPropertyTable" class="normal">
            <tbody>
            <tr>
                <td class="leftCol-small"><fmt:message key="property.name"/> :</td>
                <td>
                    <input type="text"
                           id="xmlPropName"/>
                </td>
                <td><fmt:message key="property.xmlFName"/> : <input type="text"
                                                                    id="xmlFieldName"/>
                </td>
                <td><fmt:message key="property.xmlFType"/> :
                    <select id="outputPropertyTypes">
                        <option value="attribute">Attribute</option>
                        <option value="element">Element</option>
                    </select>
                </td>
                <td><input type="button" class="button" value="<fmt:message key="add"/>"
                           onclick="addOutputElementProperty()"/>
                </td>
            </tr>
            </tbody>
        </table>
    </td>
</tr>
<tr name="outputTupleMapping" style="display:<%=tupleMapping!=null?"":"none" %>">
    <td colspan="2" class="middle-header">
        <fmt:message key="tuple.mapping"/>
    </td>
</tr>
<tr name="outputTupleMapping" style="display:<%=tupleMapping!=null?"":"none" %>">
    <td colspan="2">

        <h4><fmt:message key="property.data.type.meta"/></h4>
        <table class="styledLeft" id="outputMetaDataTable"
               style="display:<%=tupleMapping!=null&&tupleMapping.getMetaDataProperties() != null?"":"none" %>">
            <thead>
            <th class="leftCol-med"><fmt:message key="property.name"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
            <%
                if (tupleMapping != null && tupleMapping.getMetaDataProperties() != null) {
            %>
            <tbody>
            <%
                String[] properties = tupleMapping.getMetaDataProperties();
                for (String property : properties) {
            %>
            <tr>
                <td><%=property%>
                </td>
                <td><a class="icon-link"
                       style="background-image:url(../admin/images/delete.gif)"
                       onclick="removeOutputProperty(this,'Meta')">Delete</a>
                    <script type="text/javascript">
                        addOutputTupleDataPropertyToSession('<%=property%>', 'Meta');
                    </script>
                </td>

            </tr>

            <%
                }
            %>
            </tbody>
            <%
                }
            %>
        </table>
        <div class="noDataDiv-plain" id="noOutputMetaData"
             style="display:<%=tupleMapping!=null&&tupleMapping.getMetaDataProperties() != null?"none":"" %>">
            No Meta Data properties Defined
        </div>
        <table id="addMetaData" class="normal">
            <tbody>
            <tr>
                <td class="leftCol-small"><fmt:message key="property.name"/> :</td>
                <td>
                    <input type="text" id="outputMetaDataPropName"/>
                </td>
                <td><input type="button" class="button" value="<fmt:message key="add"/>"
                           onclick="addOutputTupleProperty('Meta')"/>
                </td>
            </tr>
            </tbody>
        </table>
    </td>
</tr>
<tr name="outputTupleMapping" style="display:<%=tupleMapping!=null?"":"none" %>">
    <td colspan="2">

        <h4><fmt:message key="property.data.type.correlation"/></h4>
        <table class="styledLeft" id="outputCorrelationDataTable"
               style="display:<%=tupleMapping!=null&&tupleMapping.getCorrelationDataProperties() != null?"":"none" %>">
            <thead>
            <th class="leftCol-med"><fmt:message key="property.name"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
            <%
                if (tupleMapping != null && tupleMapping.getCorrelationDataProperties() != null) {
            %>
            <tbody>
            <%
                String[] properties = tupleMapping.getCorrelationDataProperties();
                for (String property : properties) {
            %>
            <tr>
                <td><%=property%>
                </td>
                <td><a class="icon-link"
                       style="background-image:url(../admin/images/delete.gif)"
                       onclick="removeOutputProperty(this,'Correlation')">Delete</a>
                    <script type="text/javascript">
                        addOutputTupleDataPropertyToSession('<%=property%>', 'Correlation');
                    </script>
                </td>
            </tr>
            <%
                }
            %>
            </tbody>
            <%
                }
            %>
        </table>
        <div class="noDataDiv-plain" id="noOutputCorrelationData"
             style="display:<%=tupleMapping!=null&&tupleMapping.getCorrelationDataProperties() != null?"none":"" %>">
            No Correlation Data properties Defined
        </div>
        <table id="addCorrelationData" class="normal">
            <tbody>
            <tr>
                <td class="leftCol-small"><fmt:message key="property.name"/> :</td>
                <td>
                    <input type="text" id="outputCorrelationDataPropName"/>
                </td>
                <td><input type="button" class="button" value="<fmt:message key="add"/>"
                           onclick="addOutputTupleProperty('Correlation')"/>
                </td>
            </tr>
            </tbody>
        </table>
    </td>
</tr>
<tr name="outputTupleMapping" style="display:<%=tupleMapping!=null?"":"none" %>">
    <td colspan="2">

        <h4><fmt:message key="property.data.type.payload"/></h4>
        <table class="styledLeft" id="outputPayloadDataTable"
               style="display:<%=tupleMapping!=null&&tupleMapping.getPayloadDataProperties() != null?"":"none" %>">
            <thead>
            <th class="leftCol-med"><fmt:message key="property.name"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
            <%
                if (tupleMapping != null && tupleMapping.getPayloadDataProperties() != null) {
            %>
            <tbody>
            <%
                String[] properties = tupleMapping.getPayloadDataProperties();
                for (String property : properties) {
            %>
            <tr>
                <td><%=property%>
                </td>
                <td><a class="icon-link"
                       style="background-image:url(../admin/images/delete.gif)"
                       onclick="removeOutputProperty(this,'Payload')">Delete</a>
                    <script type="text/javascript">
                        addOutputTupleDataPropertyToSession('<%=property%>', 'Payload');
                    </script>
                </td>
            </tr>

            <%
                }
            %>
            </tbody>
            <%
                }
            %>
        </table>
        <div class="noDataDiv-plain" id="noOutputPayloadData"
             style="display:<%=tupleMapping!=null&&tupleMapping.getPayloadDataProperties() != null?"none":"" %>">
            No Payload Data properties Defined
        </div>
        <table id="addPayloadData" class="normal">
            <tbody>
            <tr>
                <td class="leftCol-small"><fmt:message key="property.name"/> :</td>
                <td>
                    <input type="text" id="outputPayloadDataPropName"/>
                </td>
                <td><input type="button" class="button" value="<fmt:message key="add"/>"
                           onclick="addOutputTupleProperty('Payload')"/>
                </td>
            </tr>
            </tbody>
        </table>
    </td>
</tr>
</tbody>

</table>

<table style="width:100%">
    <tbody>
    <tr>
        <td class="buttonRow">
            <%
                if (isViewingBucket) {
            %>
            <input type="button" onclick="goBack()" value=" &lt;Back" class="button">

            <%
            } else {
            %>
            <input type="button" onclick="goBack()" value=" &lt;Back" class="button">
            <%
                }
            %>
            <input type="button" onclick="addOldQueriesToList(<%=index%>)"
                   value="<fmt:message key="save"/>" class="button">
        </td>
    </tr>
    </tbody>
</table>
</div>
</div>
</fmt:bundle>