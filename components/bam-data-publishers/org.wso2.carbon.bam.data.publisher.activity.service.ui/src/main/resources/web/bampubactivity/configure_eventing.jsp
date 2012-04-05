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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.bam.data.publisher.activity.service.ui.Utils" %>
<%@ page
        import="org.wso2.carbon.bam.data.publisher.activity.service.ui.ActivityPublisherAdminClient" %>
<%@ page
        import="org.wso2.carbon.bam.data.publisher.activity.service.stub.types.carbon.EventingConfigData" %>

<fmt:bundle
        basename="org.wso2.carbon.bam.data.publisher.activity.service.ui.i18n.Resources">
<carbon:breadcrumb label="activity.statistics"
                   resourceBundle="org.wso2.carbon.bam.data.publisher.activity.service.ui.i18n.Resources"
                   topPage="true" request="<%=request%>"/>

<%
    int messageThreshold = 0;
    if (request.getParameter("messageThreshold") != null) {
        try {
            messageThreshold = Integer.parseInt(request.getParameter("messageThreshold"));
        } catch (NumberFormatException ignored) {
            // let systemRequestThreshold be 0, meaning it is not set
        }
    }

/*    String xpathCountStr = request.getParameter("xpathCount");
    int xpathCount = 0;

    if (xpathCountStr != null) {
        xpathCount = Integer.parseInt(xpathCountStr);
    }

    ArrayList<String> expList = new ArrayList<String>();

    int paramIndex = 0;
    for (int count = 1; count <= xpathCount; count++) {
        String xpathExpression = request.getParameter("xpathExpression" + (++paramIndex));

        while (xpathExpression == null) {
            xpathExpression = request.getParameter("xpathExpression" + (++paramIndex));
        }

        expList.add(xpathExpression);
    }*/

    String setConfig = request.getParameter("setConfig"); // hidden parameter to check if the form is being submitted
    String enableEventing = request.getParameter("enableEventing"); // String value is "on" of checkbox clicked, else null
    String msgLookup = request.getParameter("msgLookup");
    String msgDumping = request.getParameter("msgDumping");

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
            .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ActivityPublisherAdminClient client = new ActivityPublisherAdminClient(cookie, backendServerURL,
                                                                           configContext, request.getLocale());
    EventingConfigData eventingConfigData = null;

    if (setConfig != null) { // form submitted requesing to set eventing config
        eventingConfigData = new EventingConfigData();
        if (enableEventing != null) {
            eventingConfigData.setEnableEventing(Utils.EVENTING_ON);
            eventingConfigData.setMessageThreshold(messageThreshold);
            eventingConfigData.setEnableMessageLookup(msgLookup);
            //eventingConfigData.setXPathExpressions(expList.toArray(new String[]{}));
            eventingConfigData.setEnableMessageDumping(msgDumping);
        } else {
            eventingConfigData.setEnableEventing("OFF");
        }
        try {
            client.setEventingConfigData(eventingConfigData);
%>
<script type="text/javascript">
    jQuery(document).init(function() {
        function handleOK() {

        }

        CARBON.showInfoDialog("Eventing Configuration Successfully Updated!", handleOK);
    });
</script>
<%
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
} else { // this is the initial loading of the page, hence load current values from backend
    try {
        eventingConfigData = client.getEventingConfigData();
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
    boolean eventingOn = Utils.EVENTING_ON.equals(eventingConfigData.getEnableEventing());
    if (eventingOn) {
        messageThreshold = eventingConfigData.getMessageThreshold();
        msgDumping = eventingConfigData.getEnableMessageDumping();
        msgLookup = eventingConfigData.getEnableMessageLookup();
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
    /*row = 0;
     //add a new row to the table
     function addRow() {

     if (row == 0) {
     row = document.getElementById('eventingTbl').rows.length- 4;
     }

     row++;

     //add a row to the rows collection and get a reference to the newly added row
     var newRow = document.getElementById("eventingTbl").insertRow(-1);
     newRow.id = 'file' + row;

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

     alternateTableRows('eventingTbl', 'tableEvenRow', 'tableOddRow');
     }

     function deleteRow(rowId) {
     var tableRow = document.getElementById(rowId);
     //compactRows(rowId);
     tableRow.parentNode.deleteRow(tableRow.rowIndex);
     alternateTableRows('eventingTbl', 'tableEvenRow', 'tableOddRow');
     }

     function registerCount() {
     var field = document.getElementById('xpathCount');
     field.value = document.getElementById('eventingTbl').rows.length - 4;
     if(validate()){
     document.configForm.submit();
     }else{
     CARBON.showErrorDialog("Empty XPath Expressions not allowed.");
     }
     }

     function validate(){
     var valid = true;
     var elms = YAHOO.util.Dom.getElementsByClassName('xpathClass', 'input',document.getElementById('eventingTbl'));
     for(var i=0;i<elms.length;i++){
     if(elms[i].value == ""){
     valid = false;
     }
     }
     return valid;
     }*/

    /*    function compactRows(rowId) {

     var tableRow = document.getElementById(rowId);
     var index = rowId.substring(4,rowId.length);
     var idx = parseInt(index);

     var counter = 0;
     var lastRow = tableRow.parentNode.lastChild;
     var lastIndex = lastRow.id.substring(4, lastRow.id.length);
     var lastIdx = parseInt(lastIndex);

     for (counter = idx; counter < lastIdx; counter++) {
     idx++;
     var input = document.getElementById('id'+idx);

     if (tableRow.id != '') {
     tableRow.id = '';
     }

     input.parentNode.parentNode.id = 'file' + (idx - 1);
     input.name = 'xpathExpression' + (idx - 1);
     input.id = 'id' + (idx - 1);

     var btInput = document.getElementById('bt'+idx);
     var str=btInput.onclick.toString();
     btInput.onClick = "deleteRow('file" + (idx - 1) + "')";
     btInput.id='bt' + (idx - 1);

     alert("deleteRow('file" + (idx - 1) + "')");

     }

     rows--;
     }*/

</script>
<script type="text/javascript" src="../yui/build/yahoo/dom-min.js"></script>
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<div id="middle">
    <h2><fmt:message key="bam.statpublisher.config"/></h2>

    <div id="workArea">
        <div id="result"></div>
        <p>&nbsp;</p>

        <form action="configure_eventing.jsp" method="post" name="configForm">
            <input
                    type="hidden" name="setConfig" value="on"/>
            <input id="xpathCount"
                   type="hidden" name="xpathCount" value=""/>
            <table width="100%" class="styledLeft noBorders"
                   style="margin-left: 0px;">
                <thead>
                <tr>
                    <th colspan="4"><fmt:message key="eventing.configuration"/></th>
                </tr>
                </thead>
                <tr>
                    <td>
                        <%
                            if (eventingOn) {
                        %> <input type="checkbox" name="enableEventing"
                                  onclick="showHideDiv('thresholdTr')" checked="true">&nbsp;&nbsp;&nbsp;&nbsp;
                        <%
                        } else {
                        %> <input type="checkbox" name="enableEventing"
                                  onclick="showHideDiv('thresholdTr')">&nbsp;&nbsp;&nbsp;&nbsp;
                        <%
                            }
                        %> <fmt:message key="enable.eventing"/></td>
                </tr>
                <tr>
                    <td colspan="4">&nbsp;</td>
                </tr>

                <%
                    if (eventingOn) {
                %>
                <tr id="thresholdTr" style="display: block">
                            <%
			    } else {
			%>

                <tr id="thresholdTr" style="display: none">
                    <%
                        }
                    %>

                    <td>
                        <table id="eventingTbl" class="noBorders" style="border: medium none">
                            <tr>
                                <td colspan="2"><strong><i> <fmt:message
                                        key="eventing.note.1"/><br/>
                                    <fmt:message key="eventing.note.2"/> </i></strong></td>
                            </tr>
                            <tr>
                                <td width="20%"><fmt:message key="message.threshold"/></td>
                                <td width="30%"><input type="text" size="5"
                                                       value="<%=messageThreshold%>"
                                                       name="messageThreshold"
                                                       maxlength="4"/></td>
                            </tr>
                            <tr>
                                <td width="20%"><fmt:message key="message.dumping"/></td>
                                <td><input type="radio" name="msgDumping"
                                           value="ON" <% if(!(msgDumping==null) && msgDumping.equals("ON")){%>
                                           checked=""<%}%>><fmt:message
                                        key="message.dumpingTypeON"/>
                                    <input type="radio" name="msgDumping"
                                           value="OFF" <% if(msgDumping==null ||!msgDumping.equals("ON")){%>
                                           checked=""<%}%>><fmt:message
                                            key="message.dumpingTypeOFF"/>
                                </td>
                            </tr>
                            <tr>
                                <td width="20%"><fmt:message key="message.lookup"/></td>
                                <td>
                                    <input type="radio"
                                           name="msgLookup"
                                           value="ON"
                                            <%
                                                if (!(msgLookup == null) && msgLookup.equals("ON")) {
                                            %>
                                           checked="" <%}%> />
                                    <fmt:message key="message.lookupON"/>

                                    <input type="radio"
                                           name="msgLookup"
                                           value="OFF" <% if (msgLookup == null || !msgLookup.equals("ON")) {%>
                                           checked="" <%}%> />
                                    <fmt:message key="message.lookupOFF"/>
                                </td>
                            </tr>
                                <%--<tr id="file1">
                                    <td><fmt:message key="xpath.expression"/></td>
                                    <td>
                                        <%  String firstXPath = "";
                                            String[] oldXPaths = eventingConfigData.getXPathExpressions();
                                            if (oldXPaths != null && oldXPaths.length > 0) {
                                                firstXPath = oldXPaths[0];
                                            }
                                        %>
                                        <input id='id1' type="text" value="<%=firstXPath%>"
                                               name="xpathExpression1" class="xpathClass"/>
                                        <input id='bt1' type="button" width='20px' class="button"
                                               onclick="addRow();" value=" + "/>
                                    </td>
                                </tr>

                                <%
                                    if (oldXPaths != null && oldXPaths.length > 0) {
                                        for (int i = 1; i < oldXPaths.length; i++) {
                                            String rowId = "file" + (i + 1);
                                %>

                                <tr id=<%= rowId %>>
                                    <td><fmt:message key="xpath.expression"/></td>
                                    <td>
                                        <input id="<%= "id" + (i + 1) %>" type="text"
                                               value='<%= oldXPaths[i] %>'
                                               name="<%= "xpathExpression" + (i + 1)%>" class="xpathClass"/>
                                        <input id="<%= "bt" + (i + 1)%>" type="button" width='20px'
                                               class='button' value=' - '
                                               onclick="deleteRow('<%= rowId %>')"/>
                                    </td>
                                </tr>

                                <%
                                        }
                                    }
                                %>--%>

                        </table>
                            <%--                        <script type="text/javascript">
                                var xpathExp = document.getElementById("file1");
                                function showHideRow(clickValue) {
                                    var counter = 1;
                                    var xpathExp = document.getElementById("file" + counter);
                                    while (xpathExp != null) {
                                        if (clickValue == "ON") {
                                            xpathExp.style.display = "";
                                        } else {
                                            xpathExp.style.display = "none";
                                        }

                                        counter++;
                                        xpathExp = document.getElementById("file" + counter);
                                    }
                                }
                            </script>--%>
                    </td>
                </tr>

                <tr>
                    <td colspan="4" class="buttonRow"><input type="submit"
                                                             class="button"
                                                             value="<fmt:message key="update"/>"
                                                             id="updateStats"/>&nbsp;&nbsp;
                    </td>
                </tr>
            </table>
        </form>
    </div>
</div>
</fmt:bundle>
