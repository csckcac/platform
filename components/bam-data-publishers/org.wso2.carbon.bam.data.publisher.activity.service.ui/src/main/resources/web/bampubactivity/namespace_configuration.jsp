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
<%@page import="org.wso2.carbon.bam.data.publisher.activity.service.ui.ActivityPublisherAdminClient"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page
        import="org.wso2.carbon.bam.data.publisher.activity.service.stub.types.carbon.XPathConfigData" %>

<fmt:bundle
        basename="org.wso2.carbon.bam.data.publisher.activity.service.ui.i18n.Resources">
<carbon:breadcrumb label="xpath.configuration"
                   resourceBundle="org.wso2.carbon.bam.data.publisher.activity.service.ui.i18n.Resources"
                   topPage="true" request="<%=request%>"/>

<%
    String xpathKey;
    String xpathValue;
    String xpathPrefix = null;
    String xpathURI = null;
    String getConfig;
    String setConfig;
    String addConfig;
    ArrayList<String> nameSpaces = new ArrayList<String>();

    xpathKey = request.getParameter("XPathKey");
    xpathValue = request.getParameter("XPathValue");
    getConfig = request.getParameter("getConfig");
    setConfig = request.getParameter("setConfig");
    addConfig = request.getParameter("addConfig");

    XPathConfigData data = new XPathConfigData();
    data.setKey(xpathKey);
    data.setXpath(xpathValue);

    if (addConfig != null) {
        data.setEditing(false);
    } else {
        data.setEditing(true);
    }

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ActivityPublisherAdminClient client = new ActivityPublisherAdminClient(cookie, backendServerURL,
                                                                           configContext, request.getLocale());

    if (setConfig != null) { // form submitted requesing to set xpath config
        String nsCountStr = request.getParameter("nsCount");
        int nsCount = 0;

        if (nsCountStr != null) {
            nsCount = Integer.parseInt(nsCountStr);
        }

        int paramIndex = 0;
        for (int count = 1; count <= nsCount; count++) {
            xpathPrefix = request.getParameter("XPathPrefix" + paramIndex);
            xpathURI = request.getParameter("XPathURI" + paramIndex);

            while (xpathPrefix == null || xpathURI == null) {
                paramIndex++;
                xpathPrefix = request.getParameter("XPathPrefix" + paramIndex);
                xpathURI = request.getParameter("XPathURI" + paramIndex);
            }

            paramIndex++;

            if (!"".equals(xpathPrefix) && !"".equals(xpathURI)) {
                nameSpaces.add(xpathPrefix + "@" + xpathURI);   // Here we store ns and prefix appended with an '@' symbol//
            }
        }

/*        xpathPrefix = request.getParameter("XPathPrefix" + counter);
        xpathURI = request.getParameter("XPathURI" + counter);

        while (xpathPrefix != null && xpathURI != null) {
            if (!"".equals(xpathPrefix) && !"".equals(xpathURI)) {
                nameSpaces.add(xpathPrefix + "@" + xpathURI);   // Here we store ns and prefix appended with an '@' symbol
            }

            counter = counter + 1;
            xpathPrefix = request.getParameter("XPathPrefix" + counter);
            xpathURI = request.getParameter("XPathURI" + counter);
        }*/

        data.setNameSpaces(nameSpaces.toArray(new String[nameSpaces.size()]));

        try {
            client.setXPathConfigData(data);
        } catch (Exception e) {

            if (e.getMessage().indexOf("XPath Expression Key should be unique") != -1) {
%>

<script type="text/javascript">
    CARBON.showErrorDialog("XPath configuration with given name already exists for this server.");
</script>

<%

} else if (e.getCause().getMessage().toLowerCase().indexOf("you are not authorized") == -1) {
    response.setStatus(500);
    CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
    session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        }
    }
%>

<script type="text/javascript">
    jQuery(document).init(function() {
        function handleOK() {
            window.location = '../bampubactivity/xpath_configuration.jsp';
        }

        CARBON.showInfoDialog("XPath Configuration Successfully Updated!", handleOK);
    });
</script>

<%
} else if (getConfig != null) { // this is the initial loading of the page with values for particular xpath. Hence load information from backend.
    XPathConfigData[] datas = null;
    try {
        datas = client.getXPathConfigData();

        if (datas != null) {

            for (XPathConfigData xPathConfigData : datas) {
                if (xPathConfigData.getKey().equals(xpathKey)) {
                    data = xPathConfigData;
                    xpathValue = data.getXpath();

                    if (xPathConfigData.getNameSpaces() != null) {
                        for (String ns : xPathConfigData.getNameSpaces()) {
                            nameSpaces.add(ns);
                        }
                    }
                }
            }
        }

    } catch (Exception e) {
        if (e.getCause().getMessage().toLowerCase().indexOf("you are not authorized") == -1) {
            response.setStatus(500);
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
            }
        }
    }

%>
<script id="source" type="text/javascript">
    function showHideDiv(divId) {
        var theDiv = document.getElementById(divId);
        if (theDiv.style.display == "none") {
            theDiv.style.display = "";
        } else {
            theDiv.style.display = "none";
        }
    }
</script>

<script type="text/javascript">
    //add a new row to the table
    function addRow() {

        if (row == 0) {
            row = document.getElementById('xpathTbl').rows.length - 4;
        }

        row++;

        //add a row to the rows collection and get a reference to the newly added row
        var newRow = document.getElementById("xpathTbl").insertRow(-1);
        newRow.id = 'row' + row;

        var oCell = newRow.insertCell(-1);
        oCell.innerHTML = "<td><fmt:message key="xpath.expression"/></td>";
        oCell.className = "formRow";

        oCell = newRow.insertCell(-1);

        oCell.innerHTML = "<input id='tmp' type='text' value='' name='xpathExpression' class='xpathClass'/><input id='bt' type='button' width='20px' class='button' value=' - ' onclick=\"deleteRow('file" + row + "');\" />";

        var input = document.getElementById('tmp');
        input.name = "xpathExpression" + row;
        input.id = 'id' + row;

        var btInput = document.getElementById('bt');
        btInput.id = 'bt' + row;

        oCell.className = "formRow";

        // alternateTableRows('xpathTbl', 'tableEvenRow', 'tableOddRow');
    }

    function deleteRow(rowId) {
        var tableRow = document.getElementById(rowId);
        rowCount = document.getElementById("namespaceTbl").rows.length;
        //compactRows(rowId);
        if (rowCount == 2) {

            var lastRow = document.getElementById("namespaceTbl").rows[rowCount - 1];
            var lastRowTD = lastRow.getElementsByTagName("td")[0];
            var lastRowTDInput = lastRowTD.getElementsByTagName("input")[0];
            var lastId = lastRowTDInput.name.substring("XPathPrefix".length);

            document.getElementById("XPathPrefix" + lastId).readOnly = 'readOnly';
            document.getElementById("XPathPrefix" + lastId).value = '';
            document.getElementById("XPathURI" + lastId).readOnly = 'readOnly';
            document.getElementById("XPathURI" + lastId).value = '';

            tableRow.deleteCell(2);
        } else {
            tableRow.parentNode.deleteRow(tableRow.rowIndex);
            //alternateTableRows('xpathTbl', 'tableEvenRow', 'tableOddRow');
        }
    }

    function registerCount() {
        var field = document.getElementById('nsCount');

        rowCount = document.getElementById("namespaceTbl").rows.length;
        if (rowCount == 2) {

            var lastRow = document.getElementById("namespaceTbl").rows[rowCount - 1];
            var lastRowTD = lastRow.getElementsByTagName("td")[0];
            var lastRowTDInput = lastRowTD.getElementsByTagName("input")[0];
            var lastId = lastRowTDInput.name.substring("XPathPrefix".length);

            if (document.getElementById("XPathPrefix" + lastId).hasAttribute("readonly") && document.getElementById("XPathURI" + lastId).hasAttribute("readonly")) {
                field.value = 0;
            } else {
                field.value = rowCount - 1;
            }
        } else {
            field.value = rowCount - 1;
        }

        if (validate()) {
            document.configForm.submit();
        } else {
            CARBON.showErrorDialog("Empty values for Name or Xpath are not allowed.");
        }
    }

    function validate() {
        var valid = true;

        if (document.getElementById("XPathKey").value == "" || document.getElementById("XPathValue").value == "") {
            valid = false;
        }

        return valid;
    }
    var rowCount = 1;
    function addNamespace() {

        //add a row to the rows collection and get a reference to the newly added row
        rowCount = document.getElementById("namespaceTbl").rows.length;

        var lastRow = document.getElementById("namespaceTbl").rows[rowCount - 1];
        var lastRowTD = lastRow.getElementsByTagName("td")[0];
        var lastRowTDInput = lastRowTD.getElementsByTagName("input")[0];
        var lastId = lastRowTDInput.name.substring("XPathPrefix".length);

        if (rowCount == 2) {

            if (document.getElementById("XPathPrefix" + lastId).hasAttribute("readonly") && document.getElementById("XPathURI" + lastId).hasAttribute("readonly")) {

                document.getElementById("XPathPrefix" + lastId).removeAttribute("readonly");
                document.getElementById("XPathURI" + lastId).removeAttribute("readonly");
                var oCell = document.getElementById("row" + lastId).insertCell(-1);
                oCell.innerHTML = "<input type='button' width='20px' class='button' value=' - ' onclick=\"deleteRow('row" + lastId + "');\" />";
                return;
            }
        }

        var newRow = document.getElementById("namespaceTbl").insertRow(-1);

        lastId ++;

        newRow.id = 'row' + lastId;

        var oCell = newRow.insertCell(-1);
        var newInput = document.createElement('input');
        newInput.type = 'text';
        newInput.value = '';
        newInput.size = '10';
        newInput.name = 'XPathPrefix' + lastId;
        newInput.id = 'XPathPrefix' + lastId;
        oCell.appendChild(newInput);


        var oCell = newRow.insertCell(-1);
        var newInput = document.createElement('input');
        newInput.type = 'text';
        newInput.value = '';
        newInput.size = '20';
        newInput.name = 'XPathURI' + lastId;
        newInput.id = 'XPathURI' + lastId;
        oCell.appendChild(newInput);

        var oCell = newRow.insertCell(-1);
        oCell.innerHTML = "<input type='button' width='20px' class='button' value=' - ' onclick=\"deleteRow('row" + lastId + "');\" />";

    }

</script>
<script type="text/javascript" src="../yui/build/yahoo/dom-min.js"></script>
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<div id="middle">
    <h2><fmt:message key="xpath.configuration"/></h2>

    <div id="workArea">
        <div id="result"></div>
        <p>&nbsp;</p>

        <form action="namespace_configuration.jsp" method="post" name="configForm">
            <input type="hidden" name="setConfig" value="on"/>
            <input id="nsCount" type="hidden" name="nsCount" value=""/>

            <%
                if (addConfig != null) {
            %>
            <input type="hidden" name="addConfig" value="on"/>
            <%
                }
            %>

            <table width="100%" class="styledLeft noBorders"
                   style="margin-left: 0px;">
                <thead>
                <tr>
                    <th colspan="4"><fmt:message key="activity.xpath"/></th>
                </tr>
                </thead>

                <tr>
                    <td colspan="4">&nbsp;</td>
                </tr>

                <tr id="xpathTr">

                    <td>
                        <table id="xpathTbl" class="noBorders" style="border: medium none">

                            <tr>
                                <td width="20%"><fmt:message key="xpath.key"/><span
                                        class="required">*</span></td>
                                <td width="30%"><input type="text" size="20"
                                                       value="<%= (xpathKey != null) ? xpathKey: "" %>"
                                                       name="XPathKey" id="XPathKey"/>
                                </td>

                                <%
                                    if (getConfig != null) {
                                %>
                                <script type="text/javascript">
                                    document.getElementById('XPathKey').readOnly = 'readOnly';
                                </script>
                                <%
                                    }
                                %>

                                <td></td>

                            </tr>

                            <tr>
                                <td width="20%"><fmt:message key="xpath.value"/><span
                                        class="required">*</span></td>
                                <td width="30%"><input type="text" size="20"
                                                       value="<%= (xpathValue != null) ? xpathValue: "" %>"
                                                       name="XPathValue" id="XPathValue"/>
                                </td>
                                <td><a class="nseditor-icon-link" href="javascript:addNamespace();"><fmt:message
                                        key="add.namespace"/></a></td>
                            </tr>

                            <tr>
                                <td width="20%"><fmt:message key="xpath.namespace"/></td>
                                <td>
                                    <table id="namespaceTbl">
                                        <th>
                                            <fmt:message key="xpath.prefix"/>
                                        </th>
                                        <th>
                                            <fmt:message key="xpath.uri"/>
                                        </th>

                                        <%
                                            if (nameSpaces.size() != 0 && (getConfig != null || setConfig != null)) {
                                                int counter = 0;
                                                for (String ns : nameSpaces) {
                                                    String[] tokens = ns.split("@");
                                                    String prefix = null;
                                                    String uri = null;

                                                    if (tokens != null && tokens.length == 2) {
                                                        prefix = tokens[0];
                                                        uri = tokens[1];
                                                    }
                                        %>

                                        <tr id="<%= "row" + counter %>">
                                            <td width="30%"><input
                                                    id="<%= "XPathPrefix" + counter %>"
                                                    type="text" size="10"
                                                    value="<%= (prefix != null) ? prefix: "" %>"
                                                    name="<%= "XPathPrefix" + counter %>"/>
                                            </td>
                                            <td width="30%"><input id="<%= "XPathURI" + counter %>"
                                                                   type="text" size="20"
                                                                   value="<%= (uri != null) ? uri: "" %>"
                                                                   name="<%= "XPathURI" + counter %>"/>
                                            </td>
                                            <td>
                                                <input type='button' width='20px' class='button'
                                                       value=' - '
                                                       onclick="deleteRow('<%= "row" + counter %>')"/>
                                            </td>

                                        </tr>

                                        <%
                                                counter++;
                                            }
                                        } else if (addConfig != null || nameSpaces.size() == 0) {
                                        %>

                                        <tr id="row0">
                                            <td width="30%"><input id="XPathPrefix0" type="text"
                                                                   size="10"
                                                                   value=""
                                                                   name="XPathPrefix0" readonly=""/>
                                            </td>
                                            <td width="30%"><input id="XPathURI0" type="text"
                                                                   size="20"
                                                                   value=""
                                                                   name="XPathURI0" readonly=""/>
                                            </td>

                                                <%--<td><a href="javascript:addNamespace();">Add</a></td>--%>
                                        </tr>

                                        <%
                                            }
                                        %>

                                            <%--                                       <script type="text/javascript">
                                                var addTD = document.createElement('td');
                                                var anchor = document.createElement('a');
                                                anchor.href = 'javascript:addNamespace();';
                                                anchor.innerHTML = 'Add';
                                                addTD.appendChild(anchor);
                                                document.getElementById('row0').appendChild(addTD);
                                            </script>--%>

                                    </table>
                                </td>
                            </tr>

                        </table>
                    </td>
                </tr>

                <tr>
                    <td colspan="4" class="buttonRow"><input type="button"
                                                             class="button"
                                                             onclick="registerCount()"
                                                             value="<fmt:message key="update"/>"
                                                             id="updateStats"/>&nbsp;&nbsp;
                    </td>
                </tr>
            </table>
        </form>
    </div>
</div>
</fmt:bundle>