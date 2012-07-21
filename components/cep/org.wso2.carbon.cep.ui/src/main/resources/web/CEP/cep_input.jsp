<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ page import="org.apache.axis2.client.Options" %>
<%@ page import="org.apache.axis2.client.ServiceClient" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.CEPAdminServiceStub" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.InputDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.TuplePropertyDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.XMLPropertyDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.XpathDefinitionDTO" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.util.List" %>
<fmt:bundle basename="org.wso2.carbon.cep.ui.i18n.Resources">

<link type="text/css" href="../CEP/css/buckets.css" rel="stylesheet"/>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../CEP/js/cep_buckets.js"></script>
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


    String inputIndex = request.getParameter("index");
    int index = Integer.parseInt(inputIndex);
    InputDTO input = null;
    XMLPropertyDTO[] xmlProperties = null;
    TuplePropertyDTO[] tupleProperties = null;
    XpathDefinitionDTO[] xpathDefinitions = null;
    List<InputDTO> inputs = (List<InputDTO>) session.getAttribute("inputs");
    input = inputs.get(index);
    boolean xmlMapping = false;
    boolean tupleMapping = false;
    boolean mapMapping = false;
    if (input.getInputXMLMappingDTO() != null) {
        xmlMapping = true;
        xmlProperties = input.getInputXMLMappingDTO().getProperties();
        xpathDefinitions = input.getInputXMLMappingDTO().getXpathDefinition();
    } else if (input.getInputTupleMappingDTO() != null) {
        tupleMapping = true;
        tupleProperties = input.getInputTupleMappingDTO().getProperties();
    } else if (input.getInputMapMappingDTO() != null){
        mapMapping = true;
    }

%>
<div id="middle">
<h2><fmt:message key="inputs"/></h2>

<div id="workArea">
<table class="styledLeft" style="width:100%; margin-bottom:20px;">

<tbody>
<tr>
    <td colspan="2" class="middle-header">
        <fmt:message key="input"/>
    </td>
</tr>

<tr>
    <td class="leftCol-small"><fmt:message key="input"/><font color="red"> *</font></td>
    <td><input type="text" id="inputTopic" value="<%=input.getTopic()%>"></td>
</tr>
<tr>
    <td class="leftCol-small"><fmt:message key="broker.name"/></td>
    <td><select name="inputBrokerName" id="inputBrokerName">
        <%
            String[] brokerNames = stub.getBrokerNames();
            if (brokerNames != null && brokerNames.length > 0) {
                for (String brokerName : brokerNames) {
                    if (brokerName.equals(input.getBrokerName())) {
        %>
        <option value="<%=brokerName%>" selected="selected"><%=brokerName%>
        </option>
        <%
        } else {
        %>
        <option value="<%=brokerName%>"><%=brokerName%>
        </option>
        <% }
        }
        }
        %>
    </select>
    </td>
</tr>
<tr>
    <td colspan="2" class="middle-header">
        <fmt:message key="input.mapping.stream"/>
    </td>
</tr>
<% if (xmlMapping) { %>
<tr>
    <td class="leftCol-small"><fmt:message key="stream"/><span class="required">*</span></td>
    <td><input type="text" id="mappingStream"
               value="<%=input.getInputXMLMappingDTO().getStream()%>"></td>
</tr>
<% } else if (tupleMapping) { %>
<tr>
    <td class="leftCol-small"><fmt:message key="stream"/><span class="required">*</span></td>
    <td><input type="text" id="mappingStream"
               value="<%=input.getInputTupleMappingDTO().getStream()%>"></td>
</tr>
<% } else if (mapMapping) { %>
<tr>
    <td class="leftCol-small"><fmt:message key="stream"/><span class="required">*</span></td>
    <td><input type="text" id="mappingStream"
               value="<%=input.getInputMapMappingDTO().getStream()%>"></td>
</tr>
<% } %>

    <%--<tr>--%>
    <%--<td class="leftCol-small"><fmt:message key="event.class.name"/></td>--%>
    <%--<td><input type="text" id="eventClassName"></td>--%>
    <%--</tr>--%>
<tr>
    <td class="leftCol-small"><fmt:message key="input.mapping.type"/></td>
    <td><select name="inputMappingType" id="inputMappingType" onchange="setInputMapping()">
        <option value="xml" <%=xmlMapping ? " selected=\"selected\"" : "" %>><fmt:message
                key="input.mapping.type.xml"/></option>
        <option value="tuple" <%=(tupleMapping) ? " selected=\"selected\"" : "" %>><fmt:message
                key="input.mapping.type.tuple"/></option>
        <option value="tuple" <%=(mapMapping) ? " selected=\"selected\"" : "" %>><fmt:message
                key="input.mapping.type.map"/></option>
    </select>
    </td>
</tr>
<tr name="inputXMLMapping" style="width:100%;<%=xmlMapping?"":"display:none;" %>">
    <td colspan="2" class="middle-header">
        <fmt:message key="xpath_definition"/>
    </td>
</tr>
<tr name="inputXMLMapping" style="width:100%;<%=xmlMapping?"":"display:none;" %>">
    <td colspan="2">

        <div id="noXpathDiv" class="noDataDiv-plain"
             style="width:100%;display:<%=xmlMapping&&xpathDefinitions != null?"none":"" %>;">
            No XPath prefixes Defined
        </div>
        <table class="styledLeft" id="xpathNamespacesTable"
               style="width:100%;display:<%=xmlMapping&&xpathDefinitions != null?"":"none" %>;">
            <thead>
            <th class="leftCol-med"><fmt:message key="prefix"/></th>
            <th class="leftCol-med"><fmt:message key="namespace"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
            <% if (xmlMapping) {
                if (xpathDefinitions != null) {
                    for (XpathDefinitionDTO xpathDefinition : xpathDefinitions) {
            %>
            <tr>
                <td><%=xpathDefinition.getPrefix()%>
                </td>
                <td><%=xpathDefinition.getNamespace()%>
                </td>
                <td><a class="icon-link"
                       style="background-image:url(../admin/images/delete.gif)"
                       onclick="removeNSPrefix(this)">Delete</a></td>
            </tr>
            <script type="text/javascript">
                addNSprefixesToSession('<%=xpathDefinition.getPrefix()%>', '<%=xpathDefinition.getNamespace()%>');
            </script>
            <%
                        }
                    }
                }
            %>
        </table>

        <table id="addNamespaceTable" class="normal">
            <tbody>
            <tr>
                <td class="leftCol-small"><fmt:message key="prefix"/>:</td>
                <td><input type="text" id="NSPrefix"/></td>
                <td><fmt:message key="namespace"/>: <input type="text" id="NSValue"/></td>
                <td><input type="button" class="button" value="<fmt:message key="add"/>"
                           onclick="addNSPrefix()"/>
                </td>
            </tr>
            </tbody>
        </table>

    </td>
</tr>
<tr name="inputXMLMapping" style="width:100%;<%=xmlMapping?"":"display:none;" %>">
    <td colspan="2" class="middle-header">
        <fmt:message key="property"/>
    </td>
</tr>
<tr name="inputXMLMapping" style="width:100%;<%=xmlMapping?"":"display:none;" %>">
    <td colspan="2">
        <div id="noInputXMLPropertyDiv" class="noDataDiv-plain"
             style="width:100%;display:<%=xmlMapping&xmlProperties != null?"none":"" %>;">
            No Properties Defined
        </div>

        <table class="styledLeft" id="inputXMLPropertyTable"
               style="width:100%;display:<%=xmlMapping&&xpathDefinitions != null?"":"none" %>;">
            <thead>
            <th class="leftCol-med"><fmt:message key="property.name"/></th>
            <th class="leftCol-med"><fmt:message key="property.xpath"/></th>
            <th class="leftCol-med"><fmt:message key="property.type"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
            <% if (xmlMapping) {
                if (xmlProperties != null) {
                    for (XMLPropertyDTO xmlPropertyDTO : xmlProperties) {
            %>
            <tr>
                <td><%=xmlPropertyDTO.getName()%>
                </td>
                <td><%=xmlPropertyDTO.getXpath()%>
                </td>
                <td><%=xmlPropertyDTO.getType()%>
                </td>
                <td><a class="icon-link"
                       style="background-image:url(../admin/images/delete.gif)"
                       onclick="removeInputProperty(this,'xml')">Delete</a></td>
            </tr>
            <script type="text/javascript">
                addInputPropertyToSession('<%=xmlPropertyDTO.getName()%>', '<%=xmlPropertyDTO.getXpath()%>', '<%=xmlPropertyDTO.getType()%>', 'xml');
            </script>
            <%
                        }
                    }
                }
            %>
        </table>

        <table id="addXMLInputPropertyTable" class="normal">
            <tbody>
            <tr>
                <td class="leftCol-small"><fmt:message key="property.name"/>:</td>
                <td><input type="text" id="inputXMLPropName"/></td>
                <td><fmt:message key="property.xpath"/>: <input type="text" id="inputXMLPropValue"/>
                </td>
                <td><fmt:message key="property.type"/>:
                    <select id="inputXMLPropertyTypes">
                        <option value="java.lang.Integer">Integer</option>
                        <option value="java.lang.Long">Long</option>
                        <option value="java.lang.Double">Double</option>
                        <option value="java.lang.String">String</option>
                    </select>
                </td>
                <td><input type="button" class="button" value="<fmt:message key="add"/>"
                           onclick="addXMLInputProperty()"/>
                </td>
            </tr>
            </tbody>
        </table>

    </td>
</tr>
<tr name="inputTupleMapping" style="display:<%=(tupleMapping)?"":"none" %>;">
    <td colspan="2" class="middle-header">
        <fmt:message key="property"/>
    </td>
</tr>
<tr name="inputTupleMapping" style="display:<%=(tupleMapping)?"":"none" %>;">
    <td colspan="2">
        <div id="noInputTuplePropertyDiv" class="noDataDiv-plain"
             style="width:100%;display:<%=(tupleMapping)&&tupleProperties!=null?"none":"" %>;">
            No Properties Defined
        </div>

        <table class="styledLeft" id="inputTuplePropertyTable"
               style="width:100%;display:<%=(tupleMapping)&&tupleProperties!=null?"":"none" %>;">
            <thead>
            <th class="leftCol-med"><fmt:message key="property.name"/></th>
            <th class="leftCol-med"><fmt:message key="property.data.type"/></th>
            <th class="leftCol-med"><fmt:message key="property.type"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
            <%
                if (tupleMapping) {
                    if (tupleProperties != null) {
                        for (TuplePropertyDTO tuplePropertyDTO : tupleProperties) {
            %>
            <tr>
                <td><%=tuplePropertyDTO.getName()%>
                </td>
                <td><%=tuplePropertyDTO.getDataType()%>
                </td>
                <td><%=tuplePropertyDTO.getType()%>
                </td>
                <td><a class="icon-link"
                       style="background-image:url(../admin/images/delete.gif)"
                       onclick="removeInputProperty(this,'tuple')">Delete</a></td>
            </tr>
            <script type="text/javascript">
                addInputPropertyToSession('<%=tuplePropertyDTO.getName()%>', '<%=tuplePropertyDTO.getDataType()%>', '<%=tuplePropertyDTO.getType()%>', 'tuple');
            </script>
            <%
                        }
                    }
                }
            %>
        </table>

        <table id="addTupleInputPropertyTable" class="normal">
            <tbody>
            <tr>
                <td class="leftCol-small"><fmt:message key="property.name"/>:</td>
                <td><input type="text" id="inputTuplePropName"/></td>
                <td><fmt:message key="property.data.type"/>:
                    <select id="inputTuplePropertyDataTypes">
                        <option value="metaData"><fmt:message
                                key="property.data.type.meta"/></option>
                        <option value="correlationData"><fmt:message
                                key="property.data.type.correlation"/></option>
                        <option value="payloadData"><fmt:message
                                key="property.data.type.payload"/></option>
                    </select>
                </td>
                <td><fmt:message key="property.type"/>:
                    <select id="inputTuplePropertyTypes">
                        <option value="java.lang.Integer">Integer</option>
                        <option value="java.lang.Long">Long</option>
                        <option value="java.lang.Double">Double</option>
                        <option value="java.lang.String">String</option>
                    </select>
                </td>
                <td><input type="button" class="button" value="<fmt:message key="add"/>"
                           onclick="addTupleInputProperty()"/>
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
            <input type="button" onclick="goBack()" value=" &lt;Back" class="button">
            <input type="button" value="<fmt:message key="save"/>"
                   onclick="addOldInputToList(<%=index%>)" class="button">
        </td>
    </tr>
    </tbody>
</table>
</div>
</div>
</fmt:bundle>