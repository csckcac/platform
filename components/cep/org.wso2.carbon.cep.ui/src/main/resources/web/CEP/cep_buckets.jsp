<%@ page import="org.apache.axis2.client.Options" %>
<%@ page import="org.apache.axis2.client.ServiceClient" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.CEPAdminServiceStub" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.BucketDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.InputDTO" %>
<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.QueryDTO" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.LinkedList" %>
<%@ page import="org.wso2.carbon.cep.core.Bucket" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<fmt:bundle basename="org.wso2.carbon.cep.ui.i18n.Resources">
<%--Includes for tabs--%>

<link type="text/css" href="../CEP/css/buckets.css" rel="stylesheet"/>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="../CEP/js/cep_buckets.js"></script>

<%--Includes for registry browser--%>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>
<script type="text/javascript" src="js/expression_utils.js"></script>

<script type="text/javascript">
    var allInputsSelected = false;

    function resetInputVars() {
        allInputsSelected = false;

        var isSelected = false;
        if (document.inputsForm.inputs != null) {
            if (document.inputsForm.inputs[0] != null) { // there is more than 1 sg
                for (var j = 0; j < document.inputsForm.inputs[0].length; j++) {
                    if (document.inputsForm.inputs[0][j].checked) {
                        isSelected = true;
                    }
                }
            } else if (document.inputsForm.inputs[0] != null) { // only 1 sg
                if (document.inputsForm.inputs[0].checked) {
                    isSelected = true;
                }
            }
        }

        return false;
    }
    function selectAllInputsInAllPages() {
        selectAllInputsInThisPage(true);
        allInputsSelected = true;
        return false;
    }
    function selectAllInputsInThisPage(isSelected) {
        allInputsSelected = false;
        if (document.inputsForm.inputs != null &&
            document.inputsForm.inputs[0] != null) { // there is more than 1 sg
            if (isSelected) {
                for (var j = 0; j < document.inputsForm.inputs.length; j++) {
                    document.inputsForm.inputs[j].checked = true;
                }
            } else {
                for (j = 0; j < document.inputsForm.inputs.length; j++) {
                    document.inputsForm.inputs[j].checked = false;
                }
            }
        } else if (document.inputsForm.inputs != null) { // only 1 sg
            document.inputsForm.inputs.checked = isSelected;
        }

        return false;
    }
    function deleteInputs() {
        var selected = false;
        if (document.inputsForm.inputs != null) {
            if (document.inputsForm.inputs[0] != null) { // there is more than 1 sg
                for (var j = 0; j < document.inputsForm.inputs.length; j++) {
                    selected = document.inputsForm.inputs[j].checked;
                    if (selected) break;
                }
            } else if (document.inputsForm.inputs != null) { // only 1 sg
                selected = document.inputsForm.inputs.checked;
            }
        }

        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.inputs.to.be.deleted"/>');
            return;
        }
        if (allInputsSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="delete.all.inputs.prompt"/>", function () {
                location.href = 'cep_delete_inputs.jsp?deleteAllinputs=true';
            });
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="delete.inputs.on.page.prompt"/>", function () {
                document.inputsForm.submit();
            });
        }
    }

    var allqueriesSelected = false;

    function resetVars() {
        allqueriesSelected = false;

        var isSelected = false;
        if (document.queriesForm.queries != null) {
            if (document.queriesForm.queries[0] != null) { // there is more than 1 sg
                for (var j = 0; j < document.queriesForm.queries.length; j++) {
                    if (document.queriesForm.queries[j].checked) {
                        isSelected = true;
                    }
                }
            } else if (document.queriesForm.queries != null) { // only 1 sg
                if (document.queriesForm.queries.checked) {
                    isSelected = true;
                }
            }
        }

        return false;
    }
    function selectAllInAllPages() {
        selectAllInThisPage(true);
        allqueriesSelected = true;
        return false;
    }
    function selectAllInThisPage(isSelected) {
        allqueriesSelected = false;
        if (document.queriesForm.queries != null) {
            if (document.queriesForm.queries != null &&
                document.queriesForm.queries[0] != null) { // there is more than 1 sg
                if (isSelected) {
                    for (var j = 0; j < document.queriesForm.queries.length; j++) {
                        document.queriesForm.queries[j].checked = true;
                    }
                } else {
                    for (j = 0; j < document.queriesForm.queries.length; j++) {
                        document.queriesForm.queries[j].checked = false;
                    }
                }
            } else if (document.queriesForm.queries != null) { // only 1 sg
                document.queriesForm.queries.checked = isSelected;
            }
        }
        return false;
    }
    function deleteQueries() {
        var selected = false;
        if (document.queriesForm.queries != null) {
            if (document.queriesForm.queries[0] != null) { // there is more than 1 sg
                for (var j = 0; j < document.queriesForm.queries.length; j++) {
                    selected = document.queriesForm.queries[j].checked;
                    if (selected) break;
                }
            } else if (document.queriesForm.queries != null) { // only 1 sg
                selected = document.queriesForm.queries.checked;
            }
        }
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.queries.to.be.deleted"/>');
            return;
        }
        if (allqueriesSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="delete.all.queries.prompt"/>", function () {
                location.href = 'cep_delete_queries.jsp?deleteAllqueries=true';
            });
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="delete.queries.on.page.prompt"/>", function () {
                document.queriesForm.submit();
            });
        }
    }

</script>
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

    /*"item" parameter is taken from the URL eg:
    https://localhost:9443/carbon/CEP/cep_buckets.jsp?region=region1&item=cep_buckets_add_menu*/
    String fromAdd = request.getParameter("item");
    boolean isEditing = true;
    boolean edit = false;
    if (request.getParameter("edit") != null) {
        edit = Boolean.parseBoolean(request.getParameter("edit"));
    }
    if (fromAdd != null) {
        if (fromAdd.equals("cep_buckets_add_menu")) {
            session.removeAttribute("editingBucket");
            session.removeAttribute("inputs");
            session.removeAttribute("queries");
            session.removeAttribute("bucketInformation");
            isEditing = false;
        }
    }

    String bucketName = "";
    String description = "";
    String engineProvider = "";
    LinkedList<InputDTO> inputs = null;
    LinkedList<QueryDTO> queries = null;
    int currentInputPageNo = 0;
    int inputCounts = 0;
    int inputPages = 0;
    int currentQueryPageNo = 0;
    int queryCounts = 0;
    int queryPages = 0;
    String parameters = "serviceTypeFilter=" + "&serviceGroupSearchString=";

    if (edit) {
        try {
            if (session.getAttribute("editingBucket") != null) {
                BucketDTO editingBucket = (BucketDTO) session.getAttribute("editingBucket");
                bucketName = editingBucket.getName();
                description = editingBucket.getDescription();
                engineProvider = editingBucket.getEngineProvider();
                if (editingBucket.getInputs() != null) {
                    inputs = new LinkedList<InputDTO>(Arrays.asList(editingBucket.getInputs()));
                    inputCounts = inputs.size();
                }
                if (editingBucket.getQueries() != null) {
                    queries = new LinkedList<QueryDTO>(Arrays.asList(editingBucket.getQueries()));
                    queryCounts = queries.size();
                }
            }
            session.setAttribute("inputs", inputs);
            session.setAttribute("queries", queries);
        } catch (Exception e) {
            e.printStackTrace();
        }

    } else {
        if (isEditing) {
            BucketDTO editingBucket = (BucketDTO) session.getAttribute("editingBucket");
            bucketName = editingBucket.getName();
            description = editingBucket.getDescription();
            engineProvider = editingBucket.getEngineProvider();
            inputs = (LinkedList<InputDTO>) session.getAttribute("inputs");
            queries = (LinkedList<QueryDTO>) session.getAttribute("queries");
        }
    }
    inputPages = (int) Math.ceil(((float) inputCounts) / 10);
    if (inputPages <= 0) {
        //this is to make sure it works with defualt values
        inputPages = 1;
    }
    queryPages = (int) Math.ceil(((float) queryCounts) / 10);
    if (queryPages <= 0) {
        //this is to make sure it works with defualt values
        queryPages = 1;
    }

    String pageNumberAsStr = request.getParameter("inputPageNumber");
    if (pageNumberAsStr != null) {
        currentInputPageNo = Integer.parseInt(pageNumberAsStr);
    }
    pageNumberAsStr = request.getParameter("queryPageNumber");
    if (pageNumberAsStr != null) {
        currentQueryPageNo = Integer.parseInt(pageNumberAsStr);
    }


    boolean isAuthorizedForAddInput =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/cep/addInput");
    boolean isAuthorizedForAddQuery =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/cep/addQuery");
    boolean isAuthorizedForDeleteInput =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/cep/deleteInput");
    boolean isAuthorizedForDeleteQuery =
            CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/cep/deleteQuery");
%>

<div id="middle">
<%
    if (!isEditing) {
%>
<h2><img src="images/cep-buckets.gif" alt=""/> <fmt:message key="bucket.add"/></h2>
<%
} else {
%>
<h2><img src="images/cep-buckets.gif" alt=""/> <fmt:message key="bucket.edit"/></h2>
<%
    }
%>

<div id="workArea">
<table class="styledLeft noBorders spacer-bot" style="width:100%">
    <thead>
    <tr>
        <th><fmt:message key="bucket.info"/></th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td class="formRaw">
            <table class="normal">
                <tbody>
                <tr>
                    <td class="leftCol-small labelField" for="bucketName"><fmt:message
                            key="bucket.name"/><span
                            class="required">*</span></td>
                    <td><input id="bucketName"
                               type="text" <%=!isEditing ? "" : " readonly=\"true\""%>
                               value="<%=bucketName%>"/></td>
                </tr>
                <tr>
                    <td class="leftCol-small labelField" for="bucketDescription"><fmt:message
                            key="bucket.description"/></td>
                    <td>
                        <textArea class="expandedTextarea" id="bucketDescription"
                                  cols="60"  <%=!isEditing ? "" : " readonly=\"true\""%> ><%=description%></textArea>

                    </td>
                </tr>
                <tr>
                    <td class="leftCol-small labelField" for="engineProviders"><fmt:message
                            key="cep.runtime"/><span
                            class="required">*</span>
                    <td><select name="engineProviders"
                                id="engineProviders" <%=!isEditing ? "" : "disabled=\"true\""%> >
                        <%
                            String[] engineProviders = stub.getEngineProviders();
                            if (engineProviders != null && engineProviders.length > 0) {
                                for (String engineProviderS : engineProviders) {
                        %>
                        <option <%=engineProvider.equalsIgnoreCase(engineProviderS) ? "selected=\"true\"" : ""%>
                                value="<%=engineProviderS%>"><%=engineProviderS%>
                        </option>
                        <%
                                }
                            }
                        %>
                    </select>
                    </td>
                </tr>
                </tbody>
            </table>
        </td>
    </tr>
        <%--<tr>--%>
        <%--<td class="buttonRow">--%>
        <%--<input type="button" onclick="saveBucketDetails()" value="<fmt:message key="next"/> &gt;" class="button">--%>
        <%--<input type="button" onclick="javascript:location.href='cep_deployed_buckets.jsp'" value="<fmt:message key="cancel"/>" class="button">--%>
        <%--</td>--%>
        <%--</tr>--%>
    </tbody>
</table>
<h3><fmt:message key="inputs"/></h3>
<%
    if (isAuthorizedForDeleteInput) {
%>
<carbon:itemGroupSelector selectAllInPageFunction="selectAllInputsInThisPage(true)"
                          selectAllFunction="selectAllInputsInAllPages()"
                          selectNoneFunction="selectAllInputsInThisPage(false)"
                          addRemoveFunction="deleteInputs()"
                          addRemoveButtonId="delete1"
                          resourceBundle="org.wso2.carbon.cep.ui.i18n.Resources"
                          selectAllInPageKey="selectAllInPage"
                          selectAllKey="selectAll"
                          selectNoneKey="selectNone"
                          addRemoveKey="delete"
                          numberOfPages="0"/>
<%
    }
%>
<form name="inputsForm" action="cep_delete_inputs.jsp" method="post">
    <input type="hidden" name="pageNumber" value="<%= currentInputPageNo%>"/>
    <input type="hidden" name="bucketName" value="<%= bucketName%>"/>
    <table class="styledLeft noBorders spacer-bot"
           <%if (inputs == null ) {%>style="display:none"<%}%> id="topicsTable">
        <thead>
        <tr>
            <th><fmt:message key="topic.name"/></th>
            <th><fmt:message key="stream.name"/></th>
            <th><fmt:message key="broker.name"/></th>
        </tr>
        </thead>
        <tbody id="topicsTableBody">
        <%
            int inputIndex = 0;
            if (inputs != null && inputs.size() > 0) {
                int position = 0;
                for (InputDTO input : inputs) {
                    String bgColor = ((position % 2) == 1) ? "#EEEFFB" : "white";
                    position++;
        %>
        <tr bgcolor="<%= bgColor%>">
            <td><input type="checkbox" name="inputs"
                       value="<%=inputIndex%>"
                       onclick="resetInputVars()" class="chkBox"/>
                <a href="cep_input.jsp?index=<%=inputIndex%>"><%=input.getTopic()%>
                </a>
            </td>
            <td><%=input.getInputTupleMappingDTO() != null ? input.getInputTupleMappingDTO().getStream() :
                   input.getInputXMLMappingDTO() != null ? input.getInputXMLMappingDTO().getStream() : ""%>
            </td>
            <td><%=input.getBrokerName()%></td>
        </tr>

        <%
                    inputIndex++;
                }
            }

        %>
        </tbody>

    </table>
</form>
<div id="noInputsDiv" class="noDataDiv" <%if (inputs != null ) {%>style="display:none"<%}%>>
    No Inputs Defined
</div>
<%
    if (isAuthorizedForDeleteInput) {
%>
<carbon:itemGroupSelector selectAllInPageFunction="selectAllInputsInThisPage(true)"
                          selectAllFunction="selectAllInputsInAllPages()"
                          selectNoneFunction="selectAllInputsInThisPage(false)"
                          addRemoveFunction="deleteInputs()"
                          addRemoveButtonId="delete1"
                          resourceBundle="org.wso2.carbon.cep.ui.i18n.Resources"
                          selectAllInPageKey="selectAllInPage"
                          selectAllKey="selectAll"
                          selectNoneKey="selectNone"
                          addRemoveKey="delete"
                          numberOfPages="0"/>
<%
    }
%>

<%
    if (isAuthorizedForAddInput) {
%>
<a style="background-image: url(../admin/images/add.gif);"
   class="icon-link spacer" onclick="showAddInput()"><fmt:message key="input.topics.add"/></a>
<%
    }
%>

<table id="addInputTable" class="styledLeft noBorders spacer-bot"
       style="width:100%; margin-bottom:10px; margin-top:10px; display:none;">
    <thead>
    <tr>
        <th colspan="2"><fmt:message key="input.topics.add"/></th>
    </tr>
    </thead>
    <tbody>

    <tr>
        <td class="leftCol-small"><fmt:message key="input"/><font color="red"> *</font></td>
        <td><input type="text" id="inputTopic"></td>
            <%-- <td><select id="inputTopics" onchange="selectInputTopic()">
                <option value="InputTopic03">InputTopic03</option>
                <option value="InputTopic04">InputTopic04</option>
                <option value="InputTopic05">InputTopic05</option>
            </select>
            </td>--%>
    </tr>
    <tr>
        <td class="leftCol-small"><fmt:message key="broker.name"/></td>
        <td><select name="inputBrokerName" id="inputBrokerName" onchange="selectInputBrokerName()">
            <%
                String[] brokerNames = stub.getBrokerNames();
                if (brokerNames != null && brokerNames.length > 0) {
                    for (String brokerName : brokerNames) {
            %>
            <option value="<%=brokerName%>"><%=brokerName%>
            </option>
            <%
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
    <tr>
        <td class="leftCol-small"><fmt:message key="stream"/><span class="required">*</span></td>
        <td><input type="text" id="mappingStream"></td>
    </tr>
        <%--<tr>--%>
        <%--<td class="leftCol-small"><fmt:message key="event.class.name"/></td>--%>
        <%--<td><input type="text" id="eventClassName"></td>--%>
        <%--</tr>--%>
    <tr>
        <td class="leftCol-small"><fmt:message key="input.mapping.type"/></td>
        <td><select name="inputMappingType" id="inputMappingType" onchange="setInputMapping()">
            <option value="xml"><fmt:message key="input.mapping.type.xml"/></option>
            <option value="tuple"><fmt:message key="input.mapping.type.tuple"/></option>
        </select>
        </td>
    </tr>
    <tr name="inputXMLMapping">
        <td colspan="2" class="middle-header">
            <fmt:message key="xpath_definition"/>
        </td>
    </tr>
    <tr name="inputXMLMapping">
        <td colspan="2">

            <div id="noXpathDiv" class="noDataDiv-plain">
                No XPath prefixes Defined
            </div>
                <%-- <a style="background-image: url(../admin/images/add.gif);"
              class="icon-link" onclick="showAddNSPrefix()"><fmt:message key="prefix.add"/></a>--%>

            <table class="styledLeft" id="xpathNamespacesTable" style="width:100%;display:none;">
                <thead>
                <th class="leftCol-med"><fmt:message key="prefix"/></th>
                <th class="leftCol-med"><fmt:message key="namespace"/></th>
                <th><fmt:message key="actions"/></th>
                </thead>
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
    <tr name="inputXMLMapping">
        <td colspan="2" class="middle-header">
            <fmt:message key="property"/>
        </td>
    </tr>
    <tr name="inputXMLMapping">
        <td colspan="2">
            <div id="noInputXMLPropertyDiv" class="noDataDiv-plain">
                No Properties Defined
            </div>

            <table class="styledLeft" id="inputXMLPropertyTable" style="width:100%;display:none">
                <thead>
                <th class="leftCol-med"><fmt:message key="property.name"/></th>
                <th class="leftCol-med"><fmt:message key="property.xpath"/></th>
                <th class="leftCol-med"><fmt:message key="property.type"/></th>
                <th><fmt:message key="actions"/></th>
                </thead>
            </table>

            <table id="addXMLInputPropertyTable" class="normal">
                <tbody>
                <tr>
                    <td class="leftCol-small"><fmt:message key="property.name"/>:</td>
                    <td><input type="text" id="inputXMLPropName"/></td>
                    <td><fmt:message key="property.xpath"/>: <input type="text"
                                                                    id="inputXMLPropValue"/></td>
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
    <tr name="inputTupleMapping" style="display: none">
        <td colspan="2" class="middle-header">
            <fmt:message key="property"/>
        </td>
    </tr>
    <tr name="inputTupleMapping" style="display: none">
        <td colspan="2">
            <div id="noInputTuplePropertyDiv" class="noDataDiv-plain">
                No Properties Defined
            </div>

            <table class="styledLeft" id="inputTuplePropertyTable" style="width:100%;display:none">
                <thead>
                <th class="leftCol-med"><fmt:message key="property.name"/></th>
                <th class="leftCol-med"><fmt:message key="property.data.type"/></th>
                <th class="leftCol-med"><fmt:message key="property.type"/></th>
                <th><fmt:message key="actions"/></th>
                </thead>
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
    <tr>
        <td colspan="2" class="buttonRow">
            <input type="button" value="<fmt:message key="add.Input"/>"
                   onclick="addNewInputToList()" class="button"
                <%--style="margin-left:10px;"--%>/>
        </td>
    </tr>
    </tbody>

</table>
<div style="clear:both"></div>
<h3><fmt:message key="queries"/></h3>

<%
    if (true) {
%>
<carbon:paginator pageNumber="<%=currentQueryPageNo%>" numberOfPages="<%=queryPages%>"
                  page="cep_buckets.jsp" pageNumberParameterName="pageNumber"
                  resourceBundle="org.wso2.carbon.cep.ui.i18n.Resources"
                  prevKey="prev" nextKey="next"
                  parameters="<%=parameters%>"/>
<%
    if (isAuthorizedForDeleteQuery) {
%>
<carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                          selectAllFunction="selectAllInAllPages()"
                          selectNoneFunction="selectAllInThisPage(false)"
                          addRemoveFunction="deleteQueries()"
                          addRemoveButtonId="delete1"
                          resourceBundle="org.wso2.carbon.cep.ui.i18n.Resources"
                          selectAllInPageKey="selectAllInPage"
                          selectAllKey="selectAll"
                          selectNoneKey="selectNone"
                          addRemoveKey="delete"
                          numberOfPages="<%=queryPages%>"/>
<%
        }
    }

%>
<div id="noQueryDiv" class="noDataDiv" <%if (queries != null ) {%>style="display:none"<%}%>>
    No Queries Defined
</div>
<form name="queriesForm" action="cep_delete_queries.jsp" method="post">
    <input type="hidden" name="pageNumber" value="<%= currentQueryPageNo%>"/>
    <input type="hidden" name="bucketName" value="<%= bucketName%>"/>
    <table class="styledLeft noBorders spacer-bot" <% if (queries == null  )
    { %>style="display:none"<% } %>
           id="queriesTable">
        <thead>
        <tr>
            <th><fmt:message key="query.name"/></th>
            <th><fmt:message key="topic.name"/></th>
            <th><fmt:message key="broker.name"/></th>
                <%--<th>Action</th>--%>
        </tr>
        </thead>
        <tbody>
        <%
            int queryIndex = 0;
            if (queries != null) {
                int position = 0;
                for (QueryDTO query : queries) {
                    String bgColor = ((position % 2) == 1) ? "#EEEFFB" : "white";
                    position++;
        %>
        <tr bgcolor="<%= bgColor%>">
            <td><input type="checkbox" name="queries"
                       value="<%=query.getName()%>"
                       onclick="resetVars()" class="chkBox"/>
                <a href="cep_query.jsp?index=<%=queryIndex%>"><%=query.getName()%>
                </a>
            </td>
            <td><%=query.getOutput()!=null?query.getOutput().getTopic():""%></td>
            <td><%=query.getOutput()!=null?query.getOutput().getBrokerName():""%></td>
                <%--<td>--%>
                <%--&lt;%&ndash; <a class="icon-link" style="background-image:url(../admin/images/delete.gif)"--%>
                <%--onclick="removeQuery(this)">Delete</a>&ndash;%&gt;--%>
                <%--<a class="icon-link" style="background-image:url(../admin/images/edit.gif)"--%>
                <%--onclick="editQuery(this)">Edit</a>--%>
                <%--</td>--%>
        </tr>
        <%
                    queryIndex++;
                }
            }
        %>

        </tbody>
    </table>
</form>
<%
    if (true) {
        if (isAuthorizedForDeleteQuery) {

%>
<carbon:itemGroupSelector selectAllInPageFunction="selectAllInThisPage(true)"
                          selectAllFunction="selectAllInAllPages()"
                          selectNoneFunction="selectAllInThisPage(false)"
                          addRemoveFunction="deleteQueries()"
                          addRemoveButtonId="delete1"
                          resourceBundle="org.wso2.carbon.cep.ui.i18n.Resources"
                          selectAllInPageKey="selectAllInPage"
                          selectAllKey="selectAll"
                          selectNoneKey="selectNone"
                          addRemoveKey="delete"
                          numberOfPages="<%=queryPages%>"/>
<%
    }
%>
<carbon:paginator pageNumber="<%=currentQueryPageNo%>" numberOfPages="<%=queryPages%>"
                  page="cep_buckets.jsp" pageNumberParameterName="pageNumber"
                  resourceBundle="org.wso2.carbon.cep.ui.i18n.Resources"
                  prevKey="prev" nextKey="next"
                  parameters="<%=parameters%>"/>

<%
    }
    if (isAuthorizedForAddQuery) {
%>
<a style="background-image: url(../admin/images/add.gif);"
   class="icon-link spacer-bot" onclick="showAddQuery()"><fmt:message key="query.add"/></a>
<%
    }
%>

<table id="addQueryTable" class="styledLeft noBorders spacer-bot" style="width:100%; display:none;">
<thead>
<tr>
    <th colspan="2"><fmt:message key="query"/></th>
</tr>
</thead>
<tbody>

<tr>
    <td class="leftCol-small"><fmt:message key="query.name"/><span class="required">*</span></td>
    <td><input type="text" id="queryName"></td>
</tr>

<%
    boolean inline = true;
    boolean registry = false;
    String inlineDisplay = inline ? "" : "display:none;";
    String registryDisplay = registry ? "" : "display:none;";
    String key = "testRegistryKey";
%>
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
                  name="querySource" rows="30"></textarea>
    </td>
</tr>
<tr id="expressionRegistry" style="<%=registryDisplay%>">
    <td class="leftCol-small"><fmt:message key="query.expression.key"/></td>
    <td>
        <input class="longInput" type="text" name="expressionKey"
               id="expressionKey"
               value="<%=registry?key.trim():""%>"/>

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
    <td><input type="text" id="newTopic"></td>
</tr>
<tr>
    <td class="leftCol-small"><fmt:message key="broker.name"/></td>
    <td><select name="outputBrokerName" id="outputBrokerName" onchange="selectOutputBrokerName()">
        <%
            if (brokerNames != null && brokerNames.length > 0) {
                for (String brokerName : brokerNames) {
        %>
        <option value="<%=brokerName%>"><%=brokerName%>
        </option>
        <%
                }
            }
        %>
    </select>
    </td>
</tr>
<tr>
    <td class="leftCol-small"><fmt:message key="output.mapping"/></td>
    <td><select name="outputBrokerName" id="outputMapping" onchange="setOutputMapping()">
        <option value="xml"><fmt:message key="xml.mapping"/></option>
        <option value="element"><fmt:message key="element.mapping"/></option>
        <option value="tuple"><fmt:message key="tuple.mapping"/></option>
    </select>
    </td>
</tr>
<tr name="outputXMLMapping">
    <td colspan="2" class="middle-header">
        <fmt:message key="xml.mapping"/>
    </td>
</tr>
<tr name="outputXMLMapping">
    <td colspan="2">
        <p><fmt:message key="xml.mapping.text"/></P>

        <p>
            <textarea id="xmlSourceText"
                      style="border:solid 1px rgb(204, 204, 204); width: 99%;
                                     height: 150px; margin-top: 5px;"
                      name="xmlSource" rows="30"></textarea>
        </p>
    </td>
</tr>
<tr name="outputElementMapping" style="display: none">
    <td colspan="2" class="middle-header">
        <fmt:message key="element.mapping"/>
    </td>
</tr>
<tr name="outputElementMapping" style="display: none">
    <td colspan="2">
        <table>
            <tbody>
            <td class="leftCol-small"><fmt:message key="element.mapping.xmlDocumentElement"/></td>
            <td><input type="text" id="documentElement"></td>
            <td><fmt:message key="element.mapping.namespace"/></td>
            <td><input type="text" id="namespace"></td>
            </tbody>
        </table>
        <h4><fmt:message key="property"/></h4>
        <table class="styledLeft" id="outputElementPropertyTable" style="display:none">
            <thead>
            <th class="leftCol-med"><fmt:message key="property.name"/></th>
            <th class="leftCol-med"><fmt:message key="xml.field.name"/></th>
            <th class="leftCol-med"><fmt:message key="xml.field.type"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
        </table>
        <div class="noDataDiv-plain" id="noOutputElementPropertyDiv">
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
<tr name="outputTupleMapping" style="display: none">
    <td colspan="2" class="middle-header">
        <fmt:message key="tuple.mapping"/>
    </td>
</tr>
<tr name="outputTupleMapping" style="display: none">
    <td colspan="2">

        <h4><fmt:message key="property.data.type.meta"/></h4>
        <table class="styledLeft" id="outputMetaDataTable" style="display:none">
            <thead>
            <th class="leftCol-med"><fmt:message key="property.name"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
        </table>
        <div class="noDataDiv-plain" id="noOutputMetaData">
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
<tr name="outputTupleMapping" style="display: none">
    <td colspan="2">

        <h4><fmt:message key="property.data.type.correlation"/></h4>
        <table class="styledLeft" id="outputCorrelationDataTable" style="display:none">
            <thead>
            <th class="leftCol-med"><fmt:message key="property.name"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
        </table>
        <div class="noDataDiv-plain" id="noOutputCorrelationData">
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
<tr name="outputTupleMapping" style="display: none">
    <td colspan="2">

        <h4><fmt:message key="property.data.type.payload"/></h4>
        <table class="styledLeft" id="outputPayloadDataTable" style="display:none">
            <thead>
            <th class="leftCol-med"><fmt:message key="property.name"/></th>
            <th><fmt:message key="actions"/></th>
            </thead>
        </table>
        <div class="noDataDiv-plain" id="noOutputPayloadData">
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
<tr>
    <td class="buttonRow" style="margin-bottom:20px;" colspan="2">
        <input type="button" onclick="addNewQueriesToList()" value="<fmt:message key="add.query"/>"
               class="button">
    </td>
</tr>
</tbody>
</table>
<table class="styledLeft">
    <tbody>
    <tr>
        <td class="buttonRow">
                <%--<input type="button" onclick="javascript:location.href='cep_add_step2.jsp'"--%>
                <%--value=" &lt;<fmt:message key="back"/>"--%>
                <%--class="button">--%>
            <%
                if (session.getAttribute("editingBucket") != null) {
            %>
            <input type="button" onclick="finishEditBucketWizard()"
                   value="<fmt:message key="save"/>"
                   class="button">
            <%
            } else {
            %>
            <input type="button" onclick="finishAddBucketWizard()" value="<fmt:message key="save"/>"
                   class="button">
            <%
                }
            %>

            <input type="button" onclick="javascript:location.href='cep_deployed_buckets.jsp'"
                   value="<fmt:message key="cancel"/>"
                   class="button">
        </td>
    </tr>
    </tbody>
</table>
</div>
</div>
</fmt:bundle>