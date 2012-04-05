<!--
~ Copyright 2010 WSO2, Inc. (http://wso2.com)
~
~ Licensed under the Apache License, Version 2.0 (the "License");
~ you may not use this file except in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing, software
~ distributed under the License is distributed on an "AS IS" BASIS,
~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~ See the License for the specific language governing permissions and
~ limitations under the License.
-->

<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.bam.analyzer.ui.IndexAdminClient" %>
<%@ page import="org.apache.axis2.AxisFault" %>
<%@ page import="org.wso2.carbon.bam.index.stub.service.types.IndexDTO" %>
<%@ page import="org.wso2.carbon.bam.index.stub.service.types.TableDTO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>

<fmt:bundle basename="org.wso2.carbon.bam.analyzer.ui.i18n.Resources">
<%--<carbon:breadcrumb label="bam.database"
                   resourceBundle="org.wso2.carbon.bam.analyzer.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>--%>
<style type="text/css">
    .bam-fixed-width-input {
        width: 230px;
    }

    input.bam-fixed-width-input {
        width: 224px;
    }

    #comboTable {
        display: block;
    }

    #comboTable td {
        padding-left: 0 !important;
        display: inline-block;
    }

    .ui-bam-td-fixedWidth {
        width: 370px;
    }
</style>
<script type="text/javascript">

var comboTable = jQuery("#comboTable");
//var comboTableTr = jQuery("#comboTable tr");
var RowNum = 1;


var tableColumns = "<tr id='selectTableRow" + RowNum + "'><td class='ui-bam-td-fixedWidth'>" +
                   "<select name='indexedColumn' onChange='javascript:CheckOtherSelected(\"selectTableRow" + RowNum + "\")' id='indexedColumnsCombo'></select>" +
                   "</td><td><div style='height:25px;'>" +
                   "<a onClick='javaScript:addNewSelect(\"selectTableRow1\")' " +
                   "style='background-image: url(../admin/images/add.gif);' " +
                   "class='icon-link'>Add Column</a></div></td></tr>";

var dataSourceType = '';

function checkForAdvancedOptions(manuallyIndexed) {

    if (manuallyIndexed === 'true') {
        document.getElementById('advancedOptions').style.display = "";
    } else if (manuallyIndexed === 'false') {
        document.getElementById('advancedOptions').style.display = "none";
    }

}

function processTableMetaData(tableName) {

    var dataURL = "index_ajaxprocessor.jsp?function=getDataSourceTypeOfTable&tableName=" + tableName;

    var xmlHttpReq = createXmlHttpRequest();
    var opList = [];
    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq) {
        // This is a synchronous GET, hence UI blocking.
        xmlHttpReq.open("GET", dataURL, false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            var type = removeCarriageReturns(xmlHttpReq.responseText);

            if (type != null && type != '') {
                var tokens = type.split(":");
                dataSourceType = tokens[0];
                var manuallyIndexed = tokens[1];

                checkForAdvancedOptions(manuallyIndexed);
            }
        }

        xmlHttpReq = createXmlHttpRequest();
        dataURL = "index_ajaxprocessor.jsp?function=getColumnsOfTable&tableName=" + tableName;

        // This is a synchronous GET, hence UI blocking.
        xmlHttpReq.open("GET", dataURL, false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            var columns = removeCarriageReturns(xmlHttpReq.responseText);

            if (columns != null && columns != '') {
                var columnArray = columns.split(":");

                if (columnArray.length > 0) {


                    jQuery("#comboTable").html(tableColumns);

                    //var selectElement = document.createElement("select");
                    var selectElement = jQuery("#indexedColumnsCombo");

                    selectElement.append("<option value='other'>---None---</option>")

                    // Populate columns
                    for (var j = 0; j < columnArray.length; j++) {
                        var column = columnArray[j];

                        /*var option = document.createElement("option");
                         option.text = column;
                         option.value = column;*/

                        selectElement.append("<option value='" + column + "'>" + column + "</option> ");

                    }

                    selectElement.append("<option value='other'>---Specify other---</option>");

                    // Add an option to specify custom column
                    /*var option = document.createElement("option");
                     option.text = "other..";
                     option.value = "other..";
                     option.onclick = "javascript:switchInputType()";

                     selectElement.appendChild(option);*/

                    //comboCell.appendChild(selectElement);

                    // Add 'Add Column' button
                    /*var div = document.createElement("div");
                     div.style = "height:25px;"

                     var anchor = document.createElement("a");
                     anchor.setAttribute("onClick", "javascript:addColumn()");
                     anchor.setAttribute("style", "background-image: url(../admin/images/add.gif)");
                     anchor.className = 'icon-link';
                     anchor.textContent = 'Add Column';*/

                    /*div.appendChild(anchor);

                     comboCell.appendChild(div);*/

                    //comboTable.html(tableColumns);


                } else {
                    addTextInput();
                }
            } else {
                addTextInput();
            }
        } else {
            addTextInput();
        }

    }

}

function addTextInput() {/*
 var comboCell = document.getElementById("comboCell");

 // Remove existing children
 if (comboCell.hasChildNodes()) {
 while (comboCell.childNodes.length >= 1) {
 comboCell.removeChild(comboCell.firstChild);
 }
 }

 var input = document.createElement("input");
 input.type = "text";

 comboCell.appendChild(input);

 // Add 'Add Column' button
 var div = document.createElement("div");
 div.style = "height:25px;"

 var anchor = document.createElement("a");
 anchor.setAttribute("onClick", "javascript:addColumn()");
 anchor.setAttribute("style", "background-image: url(../admin/images/add.gif)");
 anchor.className = 'icon-link';
 anchor.textContent = 'Add Column'

 div.appendChild(anchor);

 comboCell.appendChild(div);
 */
    //jQuery("#comboTable").html("");
    var tableContent = "<tr id='comboTableTr_" + RowNum + "'><td><input name='indexedColumn' type='text' />" +
                       "</td><td><div style='height:25px;'>" +
                       "<a onClick='javaScript:addColumn()' " +
                       "style='background-image: url(../admin/images/add.gif);' " +
                       "class='icon-link'>Add Column</a>" +
                       "</div></td></tr>";
    jQuery("#comboTable").html(tableContent)
}
function switchInputType() {
    /*jQuery(this).next().append("<input type='text' name='cha' />");*/
}


function removeCarriageReturns(string) {
    return string.replace(/\n/g, "");
}

function createXmlHttpRequest() {
    var request;

    // Lets try using ActiveX to instantiate the XMLHttpRequest
    // object
    try {
        request = new ActiveXObject("Microsoft.XMLHTTP");
    } catch (ex1) {
        try {
            request = new ActiveXObject("Msxml2.XMLHTTP");
        } catch (ex2) {
            request = null;
        }
    }

    // If the previous didn't work, lets check if the browser natively support XMLHttpRequest
    if (!request && typeof XMLHttpRequest != "undefined") {
        //The browser does, so lets instantiate the object
        request = new XMLHttpRequest();
    }

    return request;
}

function addColumn() {
    RowNum++;
    /*var n =  + parseInt(trId.charAt(trId.length-1))+1;
     jQuery("#"+trId+" td div.addIcon").remove();*/
    //alert(n);
    var sId = "comboTableTr_" + RowNum;
    //alert(sId);
    var tableContent = "<tr id='" + sId + "'><td><input name='indexedColumn' type='text' />" +
                       "</td><td><div style='height:25px;'>" +
                       "<a onClick='javaScript:removeColumn(\"" + sId + "\")' " +
                       "style='background-image: url(../images/delete.gif);' " +
                       "class='icon-link'>Remove Column</a>" +
                       "</div></td></tr>";

    $("#comboTable").append(tableContent);
}
function CheckOtherSelected(id) {
    if (jQuery("#" + id + " select option:selected").val() == "other") {
        jQuery("#" + id + " select").parent().append("<input name='indexedColumn' type='text'/>");
    }
    else {
        jQuery("#" + id + " input").remove();
    }
}
function removeColumn(id) {
    jQuery("#" + id).remove();
}
function addNewSelect() {
    var comboValues = jQuery("#indexedColumnsCombo");
    RowNum++;
    var cId = "comboTableTr_" + RowNum;
    var newTrCombo = "<tr id='" + cId + "'><td class='ui-bam-td-fixedWidth'><select name='indexedColumn' onChange='javascript:CheckOtherSelected(\"" + cId + "\")'>" + comboValues.html() +
                     "</select></td><td><div style='height:25px;'>" +
                     "<a onClick='javaScript:removeColumn(\"" + cId + "\")' " +
                     "style='background-image: url(../admin/images/add.gif);' " +
                     "class='icon-link'>Remove Column</a>" +
                     "</div></td></tr>";
    jQuery("#comboTable").append(newTrCombo);
}

function showAdvancedOptions(id) {
    var formElem = document.getElementById(id + '_advancedForm');
    if (formElem.style.display == 'none') {
        formElem.style.display = '';
        document.getElementById(id + '_adv').innerHTML = '<a class="icon-link" ' +
                                                         'onclick="javascript:showAdvancedOptions(\'' + id + '\');" style="background-image: url(images/up.gif);">' + jsi18n['hide.advanced.options'] + '</a>';
    } else {
        formElem.style.display = 'none';
        document.getElementById(id + '_adv').innerHTML = '<a class="icon-link" ' +
                                                         'onclick="javascript:showAdvancedOptions(\'' + id + '\');" style="background-image: url(images/down.gif);">' + jsi18n['show.advanced.options'] + '</a>';
    }
}

</script>
<%

    /*    String mode = "";
    if (request.getParameter("mode") != null) {
        mode = request.getParameter("mode");
    }*/

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    IndexAdminClient client = new IndexAdminClient(cookie, serverURL, configContext);

    String[] dataSourceTypes = new String[0];
    try {
        dataSourceTypes = client.getDataSourceTypes();
    } catch (AxisFault e) {
        String errorString = "Unable to fetch data source meta data.";
%>
<script type="text/javascript">
    jQuery(document).init(function () {
        CARBON.showErrorDialog('<%=errorString%>');
    });
</script>
<%
    }

    String indexName = "";
    String indexedTable = "";
    String indexedColumns = "";

    String dataSourceType = "";

    boolean autoGenerated = false;
    boolean manuallyIndexed = false;

/*    if (request.getParameter("indexName") != null) {
        indexName = request.getParameter("indexName");
    }

    if (!"".equals(indexName)) {
        IndexDTO index = client.getIndex(indexName);
        indexedTable = index.getIndexedTable();
        String[] columns = index.getIndexedColumns();

        StringBuffer sb = new StringBuffer();
        for (String column : columns) {
            sb.append(column);
            sb.append("\n");
        }

        indexedColumns = sb.toString();

        dataSourceType = index.getDataSourceType();

        autoGenerated = index.getAutoGenerated();
        manuallyIndexed = index.getManuallyIndexed();

    }*/

    TableDTO[] tables = null;
    try {
        tables = client.getAllTableMetaData(false);
    } catch (AxisFault e) {
        String errorString = "Unable to fetch table meta data.";
%>
<script type="text/javascript">
    jQuery(document).init(function () {
        CARBON.showErrorDialog('<%=errorString%>');
    });
</script>
<%
    }

    List<String> tableNames = new ArrayList<String>();
    if (tables != null) {
        for (TableDTO table : tables) {
            tableNames.add(table.getTableName());
        }
    }

    TableDTO firstTable = null;
    for (int i = 0; i < tableNames.size(); i++) {
        String table = tableNames.get(i);

        if (i == 0) {
            try {
            firstTable = client.getTableMetaData(table);
            } catch (AxisFault e) {
                String errorString = "Unable to fetch table meta data.";
%>
<script type="text/javascript">
    jQuery(document).init(function () {
        CARBON.showErrorDialog('<%=errorString%>');
    });
</script>
<%
            }

            String type;
            for (String sourceType : dataSourceTypes) {
                type = sourceType.split(":")[0];
                if (type.equals(firstTable.getDataSourceType())) {
                    %>
<script type="text/javascript">
    dataSourceType = '<%= type%>';
</script>
<%
                }
            }

        }
    }


%>

<%--Don't move these functions to upper script tag. 'dataSourceType' variable needs to be set prior to
these functions.--%>
<script type="text/javascript">

    function submitIndexData() {
        document.indexForm.action = 'index-save.jsp?mode=save';
        var columns = document.getElementsByName('indexedColumn');

        var columnList = '';
        for (var i = 0; i < columns.length; i++) {
            var column = columns[i];

            var inputValue = column.value;

            if (inputValue != '' && inputValue != null && inputValue != 'other') {
                columnList = columnList + inputValue + ':';
            }
        }

        if (columnList != '') {
            columnList = columnList.substring(0, columnList.lastIndexOf(':'));
        }

        document.getElementById('indexedColumns').value = columnList;
        document.getElementById('dataSourceType').value = dataSourceType;

        document.indexForm.submit();
        return true;
    }

    function cancelIndexData() {
        location.href = "data-config.jsp";
    }

</script>

<div id="middle">

    <h2>Add Index</h2>

    <div id="workArea">

        <form id="indexForm" name="indexForm" action="" method="POST">
            <table class="styledLeft">
                <thead>
                <tr>
                    <th><span style="float: left; position: relative; margin-top: 2px;">
                            <fmt:message key="index.configuration"/></span>
                    </th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <table class="normal-nopadding">
                            <tbody>

                            <tr>
                                <td width="180px"><fmt:message key="name"/> <span
                                        class="required">*</span></td>
                                <td><input class="bam-fixed-width-input" name="indexName"
                                           id="indexName"/>
                                </td>
                            </tr>

                            <tr>
                                <td class="leftCol-small"><fmt:message key="indexed.table"/><span
                                        class="required"> *</span>
                                </td>
                                <td>
                                    <% if (tableNames.size() > 0) { %>

                                    <select class="bam-fixed-width-input" id="table" name="table">
                                        <%

                                            for (int i = 0; i < tableNames.size(); i++) {
                                                String table = tableNames.get(i);

                                                if (i == 0) {
                                                    String type;
                                                    for (String sourceType : dataSourceTypes) {
                                                        type = sourceType.split(":")[0];
                                                        if (type.equals(firstTable.getDataSourceType())) {
                                                            manuallyIndexed = Boolean.valueOf(sourceType.split(":")[1]);
                                                        }
                                                    }

                                                }
                                        %>
                                        <option value="<%=table%>" <%=i == 0 ? "selected=\"selected\"" : ""%>
                                                onclick="javascript:processTableMetaData('<%= table%>')">
                                            <%=table%>
                                        </option>
                                        <% }%>
                                    </select>

                                    <% } else { %>
                                    <input class="bam-fixed-width-input" id="table" name="table"
                                           type="text"/>
                                    <% } %>
                                </td>
                            </tr>

                                <%--                            <tr>
                                    <td class="leftCol-small"><fmt:message key="dataSource.type"/><span
                                            class="required"> *</span>
                                    </td>
                                    <td>
    
                                        <select id="dataSourceType" name="dataSourceType">
                                            <%
                                                String firstIndexStrategy = "false";
                                                // To catch the case where the default selected data source may be
                                                // manually indexed.
    
                                                for (int i = 0; i < dataSourceTypes.length; i++) {
                                                    String type = dataSourceTypes[i].split(":")[0];
                                                    String indexStrategy = dataSourceTypes[i].split(":")[1];
    
                                                    if (i == 0) {
                                                        firstIndexStrategy = indexStrategy;
                                                    }
    
                                            %>
                                            <option value="<%=type%>" <%=i == 0 ? "selected=\"selected\"" : ""%>
                                                    onclick="javascript:checkForAdvancedOptions('<%= indexStrategy%>')">
                                                <%=type%>
                                            </option>
                                            <% }%>
                                        </select>
    
                                        <script type="text/javascript">
                                            checkForAdvancedOptions(<%=firstIndexStrategy%>)
                                        </script>
    
                                    </td>
                                </tr>--%>

                            <tr>
                                <td class="leftCol-small"><fmt:message key="indexed.columns"/><span
                                        class="required"> *</span></td>
                                <td id="comboCell">
                                    <table id="comboTable">
                                        <tr id="comboTableTr1">
                                            <td>
                                                <input class="bam-fixed-width-input" name="indexedColumn"
                                                       type="text"/>
                                            </td>
                                            <td>
                                                <div style="height:25px;">
                                                    <a onclick="javaScript:addColumn()"
                                                       style="background-image: url(../admin/images/add.gif);"
                                                       class="icon-link addIcon">Add
                                                                                 Column</a>
                                                </div>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                                <input id="indexedColumns" name="indexedColumns" type="hidden"/>
                                <input id="dataSourceType" name="dataSourceType" type="hidden"/>
                            </tr>

                            <tr id="granularity" <%=manuallyIndexed ? "" : "style=\"display:none\"" %> >
                                <td class="leftCol-small"><fmt:message
                                        key="time.index.granularity"/></td>
                                <td>
                                    <select class="bam-fixed-width-input" id="granularityCombo"
                                            name="granularity">
                                        <%
                                            String[] granularities = {"--Index unused--", "MINUTE", "HOUR", "DAY", "MONTH", "YEAR"};
                                            for (int i = 0; i < granularities.length; i++) {
                                                String granularity = granularities[i];
                                        %>
                                        <option value="<%=granularity%>">
                                        <%=granularity%>
                                        </option>
                                        <% }%>
                                    </select>
                                </td>
                            </tr>

                            <tr id="advancedOptions" <%=manuallyIndexed ? "" : "style=\"display:none\"" %> >
                                <td><span id="_adv" style="float: left; position: relative;">
                                    <a class="icon-link"
                                       onclick="javascript:showAdvancedOptions('');"
                                       style="background-image: url(images/down.gif);">
                                        <fmt:message key="show.advanced.options"/></a></span>
                                </td>
                            </tr>
                            </tbody>
                        </table>

                        <div id="_advancedForm" style="display:none">
                            <table class="normal-nopadding">
                                <tbody>
                                <tr>
                                    <td colspan="2" class="sub-header"><fmt:message
                                            key="indexing.frequency"/></td>
                                </tr>
                                <tr>
                                    <td width="180px"><fmt:message key="cron"/></td>
                                    <td><input id="cron" name="cron" type="text"
                                               value=""/>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>

                        <table class="normal-nopadding">
                            <tbody>

                            <tr>
                                <td class="buttonRow" colspan="2">
                                    <input type="button" value="<fmt:message key="save"/>"
                                           class="button" name="save"
                                           onclick="javascript:submitIndexData();"/>
                                    <input type="button" value="<fmt:message key="cancel"/>"
                                           name="cancel" class="button"
                                           onclick="javascript:cancelIndexData();"/>
                                </td>
                            </tr>
                            </tbody>

                        </table>
                    </td>
                </tr>
                </tbody>
            </table>
        </form>

    </div>
</div>

</fmt:bundle>