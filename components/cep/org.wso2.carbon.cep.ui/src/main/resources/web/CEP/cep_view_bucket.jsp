<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.BucketDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.ExpressionDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.InputDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.MapPropertyDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.OutputDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.OutputElementMappingDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.OutputMapMappingDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.OutputTupleMappingDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.OutputXMLMappingDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.QueryDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.TuplePropertyDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.XMLPropertyDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.XpathDefinitionDTO" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<fmt:bundle basename="org.wso2.carbon.cep.ui.i18n.Resources">
<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css" rel="stylesheet"/>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript"
        src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>
<script type="text/javascript" src="js/expression_utils.js"></script>
<script type="text/javascript">
    jQuery(document).ready(function () {
        initSections('');
        editAreaLoader.init({
                                id:"xmlSource"        // textarea id, syntax:"xml"            // syntax to be uses for highgliting, start_highlight:true        // to display with highlight mode on start-up
                            });
    })
</script>
<script src="../editarea/edit_area_full.js" type="text/javascript"></script>

<script type="text/javascript">


    function loadBucketFromBackend(bucketName) {

        $.ajax({
                   type:"POST",
                   url:"cep_load_bucket_from_bEnd.jsp",
                   data:{'bucketName':bucketName},
                   async:false,
                   success:function (msg) {
//                    alert("Data Saved: " + msg);
                       location.href = 'cep_buckets.jsp?edit=true';
                   }
               });

    }
</script>
<%
    String fromEdit = request.getParameter("edit");
    if (fromEdit != null) {
        if (!fromEdit.equals("true")) {
            session.removeAttribute("editingBucket");
        }
    } else {
        session.removeAttribute("editingBucket");
    }
    session.removeAttribute("inputsHashMap");
    session.removeAttribute("queryHashMap");
    boolean isEditing = false;
    String bucketName = "";
    String description = "";
    String engineProvider = "";
    try {
        if (session.getAttribute("editingBucket") != null) {
            isEditing = true;
            BucketDTO editingBucket = (BucketDTO) session.getAttribute("editingBucket");
            bucketName = editingBucket.getName();
            description = editingBucket.getDescription();
            engineProvider = editingBucket.getEngineProvider();
        }
    } catch (Exception e) {
        e.printStackTrace();
    }


    InputDTO[] inputs = null;
    if (session.getAttribute("editingBucket") != null) {
        BucketDTO editingBucket = (BucketDTO) session.getAttribute("editingBucket");
        if (session.getAttribute("inputsHashMap") == null) {
            inputs = editingBucket.getInputs();
            HashMap<Integer, InputDTO> inputsHashMap = new HashMap<Integer, InputDTO>();
            int inputIndex = 0;
            if (inputs != null) {
                for (InputDTO input : inputs) {
                    inputsHashMap.put(inputIndex, input);
                    inputIndex++;
                }
            }
            session.setAttribute("inputsHashMap", inputsHashMap);
        } else {
            HashMap<Integer, InputDTO> inputsHashMap = (HashMap<Integer, InputDTO>) session.getAttribute("inputsHashMap");
            inputs = new InputDTO[inputsHashMap.size()];
            for (int key : inputsHashMap.keySet()) {
                inputs[key] = inputsHashMap.get(key);
            }
        }
    } else {
        if (session.getAttribute("inputsHashMap") != null) {
            HashMap<Integer, InputDTO> inputsHashMap = (HashMap<Integer, InputDTO>) session.getAttribute("inputsHashMap");
            inputs = new InputDTO[inputsHashMap.size()];
            for (int key : inputsHashMap.keySet()) {
                inputs[key] = inputsHashMap.get(key);
            }
        }
    }


    QueryDTO[] queries = null;
    HashMap<Integer, QueryDTO> queryHashMap = null;
    if (session.getAttribute("editingBucket") != null) {
        BucketDTO editingBucket = (BucketDTO) session.getAttribute("editingBucket");
        if (session.getAttribute("queryHashMap") == null) {
            queries = editingBucket.getQueries();
            queryHashMap = new HashMap<Integer, QueryDTO>();
            int queryIndex = 0;
            if (queries != null) {
                for (QueryDTO query : queries) {
                    queryHashMap.put(queryIndex, query);
                    queryIndex++;
                }
            }
            session.setAttribute("queryHashMap", queryHashMap);
        } else {
            queryHashMap = (HashMap<Integer, QueryDTO>) session.getAttribute("queryHashMap");
            queries = new QueryDTO[queryHashMap.size()];
            for (int key : queryHashMap.keySet()) {
                queries[key] = queryHashMap.get(key);
            }
        }
    } else {
        if (session.getAttribute("queryHashMap") != null) {
            queryHashMap = (HashMap<Integer, QueryDTO>) session.getAttribute("queryHashMap");
            queries = new QueryDTO[queryHashMap.size()];
            for (int key : queryHashMap.keySet()) {
                queries[key] = queryHashMap.get(key);
            }
        }
    }

    boolean isAuthorizedForEditingBuckets =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/cep/editBucket");


    final int NONE = -1;
    final int XML = 0;
    final int TUPLE = 1;
    final int MAP = 2;
    int inputMapping = NONE;


%>

<div id="middle">
<h2><img src="images/cep-buckets.gif" alt=""/> <fmt:message key="bucket.view"/></h2>
        <%
    if (isAuthorizedForEditingBuckets) {
%>
<table style="width:100%; margin-bottom:20px;">
    <tbody>
    <tr>
        <td><a class="icon-link" style="background-image:url(../admin/images/edit.gif)"
               onclick="loadBucketFromBackend('<%=bucketName%>')"><font color="#4682b4">
            <fmt:message key="bucket.edit"/></font></a></td>
    </tr>
    </tbody>
</table>

        <%
    }
%>

<div id="workArea">
<div class="sectionSeperator togglebleTitle"><fmt:message key="bucket.info"/></div>
<div class="sectionSub">
    <table class="carbonFormTable">
        <tr>
            <td class="leftCol-med labelField"><fmt:message key="bucket.name"/></td>
            <td><input id="bucketName" type="text" readonly="true" value="<%=bucketName%>"/>
            </td>
        </tr>
        <tr>
            <td class="leftCol-med labelField"><fmt:message
                    key="bucket.description"/></td>
            <td>
                <textArea class="expandedTextarea" id="bucketDescription"
                          cols="60" readonly="true"><%=description%>
                </textArea>
            </td>
        </tr>
        <tr>
            <td class="leftCol-med labelField"><fmt:message key="cep.runtime"/></td>
            <td><input id="engineProvider" type="text" readonly="true"
                       value="<%=engineProvider%>"/></td>
        </tr>
    </table>
</div>
<div class="sectionSeperator togglebleTitle"><fmt:message key="inputs"/></div>
<div class="sectionSub">
    <table class="carbonFormTable">

        <%
            if (inputs != null) {
                for (InputDTO input : inputs) {
                    XpathDefinitionDTO[] xpathDefinitions = null;
                    XMLPropertyDTO[] xmlProperties = null;
                    TuplePropertyDTO[] tupleProperties = null;
                    MapPropertyDTO[] mapProperties = null;
                    String stream = "";
                    if (input.getInputXMLMappingDTO() != null) {
                        inputMapping = XML;
                        xpathDefinitions = input.getInputXMLMappingDTO().getXpathDefinition();
                        xmlProperties = input.getInputXMLMappingDTO().getProperties();
                        stream = input.getInputXMLMappingDTO().getStream();
                    }
                    if (input.getInputTupleMappingDTO() != null) {
                        inputMapping = TUPLE;
                        tupleProperties = input.getInputTupleMappingDTO().getProperties();
                        stream = input.getInputTupleMappingDTO().getStream();
                    }
                    if (input.getInputMapMappingDTO() != null) {
                        inputMapping = MAP;
                        mapProperties = input.getInputMapMappingDTO().getProperties();
                        stream = input.getInputMapMappingDTO().getStream();
                    }

        %>
        <tr>
            <td>
                <div class="sectionSeperator togglebleTitle"><fmt:message key="input"/></div>
                <div class="sectionSub">
                    <table class="carbonFormTable">
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key="input.topic"/></td>
                            <td><input type="text" readonly="true" id="inputTopic"
                                       value="<%=input.getTopic()%>"></td>
                        </tr>
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key="broker.name"/></td>
                            <td><input type="text" readonly="true" id="inputBrokerName"
                                       value="<%=input.getBrokerName()%>"></td>
                        </tr>

                        <tr>
                            <td colspan="2">
                                <div class="heading_A"><fmt:message
                                        key="input.mapping.stream"/></div>
                            </td>
                        <tr>
                            <td class="leftCol-med labelField"><fmt:message key="stream"/></td>
                            <td><input type="text" readonly="true" id="mappingStream"
                                       value="<%=stream%>">
                            </td>
                        </tr>
                        <%if (inputMapping == XML) {%>
                        <tr>

                            <td colspan="2">
                                <div class="heading_B"><fmt:message key="xml.mapping"/></div>
                            </td>

                        </tr>
                        <tr>
                            <td colspan="2"><fmt:message key="xpath_definition"/></td>
                        </tr>
                        <tr>
                            <td colspan="2">
                                <table style="width:100%" class="styledLeft">
                                    <thead>
                                    <th class="leftCol-med"><fmt:message key="prefix"/></th>
                                    <th class="leftCol-med"><fmt:message key="namespace"/></th>
                                    </thead>
                                    <tbody>
                                    <% if (xpathDefinitions != null) {
                                        for (XpathDefinitionDTO xpathDefinition : xpathDefinitions) {
                                    %>
                                    <tr>
                                        <td><%=xpathDefinition.getPrefix()%>
                                        </td>
                                        <td><%=xpathDefinition.getNamespace()%>
                                        </td>
                                    </tr>
                                    <%
                                            }
                                        }
                                    %>
                                    </tbody>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2" class="middle-header"><fmt:message
                                    key="property"/></td>
                        </tr>
                        <tr>
                            <td colspan="2">
                                <table class="styledLeft" style="width:100%">
                                    <thead>
                                    <th class="leftCol-med"><fmt:message
                                            key="property.name"/></th>
                                    <th class="leftCol-med"><fmt:message
                                            key="property.xpath"/></th>
                                    <th class="leftCol-med"><fmt:message
                                            key="property.type"/></th>
                                    </thead>
                                    <tbody>
                                    <% if (xmlProperties != null) {
                                        for (XMLPropertyDTO property : xmlProperties) {
                                    %>
                                    <tr>
                                        <td><%=property.getName()%>
                                        </td>
                                        <td><%=property.getXpath()%>
                                        </td>
                                        <td><%=property.getType()%>
                                        </td>
                                    </tr>
                                    <%
                                            }
                                        }
                                    %>
                                    </tbody>
                                </table>
                            </td>
                        </tr>

                        <%
                            }
                            if (inputMapping == TUPLE) {
                        %>

                        <tr>
                            <td colspan="2">
                                <div class="heading_B"><fmt:message key="tuple.mapping"/></div>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2"><fmt:message key="property"/></td>
                        </tr>
                        <tr>
                            <td colspan="2">
                                <table class="styledLeft" style="width:100%">
                                    <thead>
                                    <th class="leftCol-med"><fmt:message
                                            key="property.name"/></th>
                                    <th class="leftCol-med"><fmt:message
                                            key="property.data.type"/></th>
                                    <th class="leftCol-med"><fmt:message
                                            key="property.type"/></th>
                                    </thead>
                                    <tbody>
                                    <% if (tupleProperties != null) {
                                        for (TuplePropertyDTO property : tupleProperties) {
                                    %>
                                    <tr>
                                        <td><%=property.getName()%>
                                        </td>
                                        <td><%=property.getDataType()%>
                                        </td>
                                        <td><%=property.getType()%>
                                        </td>
                                    </tr>
                                    <%
                                            }
                                        }
                                    %>
                                    </tbody>

                                </table>
                            </td>
                        </tr>
                        <%
                            }
                            if (inputMapping == MAP) {
                        %>

                        <tr>
                            <td colspan="2">
                                <div class="heading_B"><fmt:message key="map.mapping"/></div>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2"><fmt:message key="property"/></td>
                        </tr>
                        <tr>
                            <td colspan="2">
                                <table class="styledLeft" style="width:100%">
                                    <thead>
                                    <th class="leftCol-med"><fmt:message
                                            key="property.name"/></th>
                                    <th class="leftCol-med"><fmt:message
                                            key="property.type"/></th>
                                    </thead>
                                    <tbody>
                                    <% if (mapProperties != null) {
                                        for (MapPropertyDTO property : mapProperties) {
                                    %>
                                    <tr>
                                        <td><%=property.getName()%>
                                        </td>
                                        <td><%=property.getType()%>
                                        </td>
                                    </tr>
                                    <%
                                            }
                                        }
                                    %>
                                    </tbody>

                                </table>
                            </td>
                        </tr>
                        <%}%>
                    </table>
                </div>
            </td>
        </tr>
        <%
                }
            }
        %>
    </table>
</div>
<div class="sectionSeperator togglebleTitle"><fmt:message key="queries"/></div>
<div class="sectionSub">
<table class="carbonFormTable">

        <%
    if (queryHashMap != null) {
        int queryIndex = 0;
        for (QueryDTO query : queryHashMap.values()) {
            ExpressionDTO expression = null;
            OutputDTO output = null;
            OutputElementMappingDTO elementMapping = null;
            OutputXMLMappingDTO xmlMapping = null;
            OutputTupleMappingDTO tupleMapping = null;
            OutputMapMappingDTO mapMapping = null;
            boolean inline;
            expression = query.getExpression();
            output = query.getOutput();
            if (output != null) {
                elementMapping = output.getOutputElementMapping();
                xmlMapping = output.getOutputXmlMapping();
                tupleMapping = output.getOutputTupleMapping();
                mapMapping = output.getOutputMapMapping();
            }
            if (expression.getType().equals("registry")) {
                inline = false;
            } else {
                inline = true;
            }
            String inlineDisplay = inline ? "" : "display:none;";
            String registryDisplay = inline ? "display:none;" : "";
            String key = expression.getText();
%>
<tr>
<td>

<div class="sectionSeperator togglebleTitle"><fmt:message key="query"/></div>
<div class="sectionSub">
<table class="carbonFormTable">

<tr>

    <td class="leftCol-med labelField"><fmt:message key="query.name"/></td>
    <td><input class="longInput" type="text" id="queryName" readonly="true"
               value="<%=query.getName()%>"></td>

</tr>
<tr>
    <td class="leftCol-med labelField"><fmt:message key="query.as"/></td>
    <td>
        <input type="radio" checked="true" value="inline"
               name="expressionType"
               id="expressioninlinedRd" disabled="disabled" <%if (inline){%>
               checked="checked"<%}%>>
        <label for="expressioninlinedRd"><fmt:message
                key="inlined"/></label>

        <input type="radio" value="registry" name="expressionType"
               id="expressionRegistryRd"
               disabled="disabled"  <%if (!inline){%>
               checked="checked"<%}%>>
        <label for="expressionRegistryRd"><fmt:message
                key="reg.key"/></label>
    </td>
</tr>

<tr id="expressionInlined" style="<%=inlineDisplay%>">
    <td class="leftCol-med labelField"><fmt:message key="query.expression"/></td>
    <td>
        <textarea id="querySource" name="querySource" class="expandedTextarea"
                  readonly="true"><%=expression.getText()%>
        </textarea>
    </td>
</tr>
<tr id="expressionRegistry" style="<%=registryDisplay%>">
    <td class="leftCol-med labelField"><fmt:message
            key="query.expression.key"/></td>
    <td>
        <input class="longInput" type="text" name="expressionKey"
               readonly="true"
               id="expressionKey"
               value="<%=inline?"":key.trim()%>"/>
    </td>
</tr>


<% if (output != null && output.getTopic().length() > 0) { %>


<tr>
    <th colspan="2">
        <div class="heading_A"><fmt:message key="output"/></div>
    </th>
</tr>

<tr>
    <td class="leftCol-med labelField"><fmt:message key="output.topic"/></td>
    <td><input type="text" id="newTopic" readonly="true" value="<%=output.getTopic()%>">
    </td>
</tr>
<tr>
    <td class="leftCol-med labelField"><fmt:message key="broker.name"/></td>
    <td><input type="text" id="outputBrokerName" readonly="true"
               value="<%=output.getBrokerName()%>">
    </td>
</tr>


<%if (elementMapping != null) { %>

<tr>
    <td colspan="2">
        <div class="heading_B"><fmt:message key="element.mapping"/></div>
    </td>
</tr>
<tr>
    <td colspan="2">
        <table style="width:100%">
            <tr>
                <td>
                    <table>
                        <tr>
                            <td class="leftCol-small labelField">
                                <fmt:message key="element.mapping.xmlDocumentElement"/></td>
                            <td><input type="text" readonly="true" id="documentElement"
                                       value="<%=elementMapping!=null?elementMapping.getDocumentElement():""%>">
                            </td>
                        </tr>
                    </table>
                </td>
                <td>
                    <table>
                        <tr>
                            <td class="leftCol-small labelField"><fmt:message key="element.mapping.namespace"/></td>
                            <td><input type="text" readonly="true" id="namespace"
                                       value="<%=elementMapping!=null?elementMapping.getNamespace():""%>">
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>

    </td>
</tr>
<tr>
    <td colspan="2">
        <fmt:message key="property"/>
    </td>
</tr>
<tr>
    <td colspan="2">
        <table class="styledLeft" id="propertyTable" style="width:100%">
            <thead>
            <th class="leftCol-med"><fmt:message key="property.name"/></th>
            <th class="leftCol-med"><fmt:message
                    key="property.xmlFName"/></th>
            <th class="leftCol-med"><fmt:message
                    key="property.xmlFType"/></th>
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
            </tr>
            <%
                }
            %>
            </tbody>
            <%
                }
            %>
        </table>
    </td>
</tr>

<%
    }
    if (xmlMapping != null) {
%>

<tr>
    <td colspan="2">
        <div class="heading_B"><fmt:message key="xml.mapping"/></div>
    </td>
</tr>
<tr>
    <td class="leftCol-med labelField"><fmt:message key="xml.mapping.text"/></td>
    <td>
        <textarea class="expandedTextarea" id="xmlSource"
                  name="xmlSource"
                  readonly="true"><%=xmlMapping.getMappingXMLText()%>
        </textarea>
    </td>
</tr>
<%
    }
    if (tupleMapping != null) {
%>

<tr>
    <td colspan="2">
        <div class="heading_B"><fmt:message key="tuple.mapping"/></div>
    </td>
</tr>
<tr>
    <td colspan="2">
        <fmt:message key="property.data.type.meta"/>
    </td>
</tr>
<tr>
    <td colspan="2">
        <table class="styledLeft" style="width:100%">
            <thead>
            <th class="leftCol-med"><fmt:message key="property.name"/></th>
            </thead>
            <%
                if (tupleMapping != null && tupleMapping.getMetaDataProperties() != null) {
            %>
            <tbody>
            <%
                for (String property : tupleMapping.getMetaDataProperties()) {
            %>
            <tr>
                <td><%=property%>
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
    </td>
</tr>
<tr>
    <td colspan="2">
        <fmt:message key="property.data.type.correlation"/>
    </td>
</tr>
<tr>
    <td colspan="2">
        <table class="styledLeft" style="width:100%">
            <thead>
            <th class="leftCol-med"><fmt:message key="property.name"/></th>
            </thead>
            <%
                if (tupleMapping != null && tupleMapping.getCorrelationDataProperties() != null) {
            %>
            <tbody>
            <%
                for (String property : tupleMapping.getCorrelationDataProperties()) {
            %>
            <tr>
                <td><%=property%>
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
    </td>
</tr>
<tr>
    <td colspan="2">
        <fmt:message key="property.data.type.payload"/>
    </td>
</tr>
<tr>
    <td colspan="2">
        <table class="styledLeft" style="width:100%">
            <thead>
            <th class="leftCol-med"><fmt:message key="property.name"/></th>
            </thead>
            <%
                if (tupleMapping != null && tupleMapping.getPayloadDataProperties() != null) {
            %>
            <tbody>
            <%
                for (String property : tupleMapping.getPayloadDataProperties()) {
            %>
            <tr>
                <td><%=property%>
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
    </td>
</tr><%
    }
    if (mapMapping != null) {
%>

<tr>
    <td colspan="2">
        <div class="heading_B"><fmt:message key="map.mapping"/></div>
    </td>
</tr>
<tr>
    <td colspan="2">
        <table class="styledLeft" style="width:100%">
            <thead>
            <th class="leftCol-med"><fmt:message key="property.name"/></th>
            </thead>
            <%
                if (mapMapping != null && mapMapping.getProperties() != null) {
            %>
            <tbody>
            <%
                for (String property : mapMapping.getProperties()) {
            %>
            <tr>
                <td><%=property%>
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
    </td>
</tr>
<%
        }
    }
%>
</table>
</div>
        <%

            queryIndex++;
        }
    }
%>
  </td>
</tr>
</table>
</div>


    <%--<table style="width:100%">--%>
    <%--<tbody>--%>
    <%--<tr>--%>
    <%--<td class="buttonRow">--%>
    <%--<input type="button" onclick="javascript:location.href='cep_deployed_buckets.jsp'"--%>
    <%--value=" &lt;Back" class="button">--%>
    <%--</td>--%>
    <%--</tr>--%>
    <%--</tbody>--%>

    <%--</table>--%>

<div class="buttonRow">
    <input class="button" type="button" value=" &lt;Back"
           onclick="javascript:location.href='cep_deployed_buckets.jsp'"/>
</div>
</div>
</fmt:bundle>