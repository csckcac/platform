<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.mediator.bam.config.ui.BamServerProfileUtils" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.mediator.bam.config.BamServerConfig" %>
<%@ page import="org.wso2.carbon.mediator.bam.config.stream.StreamConfiguration" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<fmt:bundle basename="org.wso2.carbon.mediator.bam.config.ui.i18n.Resources">

<carbon:breadcrumb
        label="system.statistics"
        resourceBundle="org.wso2.carbon.mediator.bam.config.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<%! public static final String PROPERTY_VALUES = "propertyValues";
    public static final String PROPERTY_KEYS = "propertyKeys";
    public static final String STREAM_NAMES = "streamNames";
    public static final String STREAM_VERSIONS = "streamVersions";
    public static final String STREAM_NICKNAME = "streamNickname";
    public static final String STREAM_DESCRIPTION = "streamDescription";
%>

<%
    String userName = "";
    String password = "";
    String ip = "";
    String port = "";
    String serverProfileLocation = "";
    String action = "";
    String overwrite = "false";
    String streamTable = "";


    BamServerConfig bamServerConfig = new BamServerConfig();
    List<StreamConfiguration> streamConfigurations;

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

    BamServerProfileUtils bamServerProfileUtils =
            new BamServerProfileUtils(cookie, backendServerURL, configContext, request.getLocale());

    String tmpUserName = request.getParameter("txtUsername");
    if(tmpUserName != null && !tmpUserName.equals("")){
        userName = tmpUserName;
    }


    String tmpPassword = request.getParameter("txtPassword");
    if(tmpPassword != null && !tmpPassword.equals("")){
        password = tmpPassword;
    }

    String tmpIp = request.getParameter("txtIp");
    if(tmpIp != null && !tmpIp.equals("")){
        ip = tmpIp;
    }

    String tmpPort = request.getParameter("txtPort");
    if(tmpPort != null && !tmpPort.equals("")){
        port = tmpPort;
    }

    String tmpStreamTable = request.getParameter("hfStreamTableData");
    if(tmpStreamTable != null && !tmpStreamTable.equals("")){
        streamTable = tmpStreamTable;
    }
    
    String tmpOverwrite = request.getParameter("overwrite");
    if(tmpOverwrite != null && !tmpOverwrite.equals("")){
        overwrite = tmpOverwrite;
    }


    String tmpServerProfileLocation = request.getParameter("txtServerProfileLocation");
    if(bamServerProfileUtils.isNotNullOrEmpty(tmpServerProfileLocation)){
        serverProfileLocation = tmpServerProfileLocation;
    }

    String tmpAction = request.getParameter("hfAction");
    if(bamServerProfileUtils.isNotNullOrEmpty(tmpAction)){
        action = tmpAction;
    }

    %>

        <%--<script type="text/javascript" src="../sequences/js/registry-browser.js"/>
        <script type="text/javascript" src="../resources/js/resource_util.js"/>
        <script type="text/javascript" src="../yui/build/connection/connection-min.js"/>--%>

        <!-- Dependencies -->
        <script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
        <script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>

        <!-- Connection handling lib -->
        <script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
        <script type="text/javascript" src="../yui/build/event/event-min.js"></script>
        <script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
        <script type="text/javascript" src="../resources/js/resource_util.js"></script>
        <script type="text/javascript" src="../yui/build/utilities/utilities.js"></script>
        <script type="text/javascript" src="../ajax/js/prototype.js"></script>

        <!-- Source File -->
        <script type="text/javascript" src="../sequences/js/registry-browser.js"></script>
        <script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>
        <script type="text/javascript" src="../admin/js/main.js"></script>








        <script id="source" type="text/javascript">

            function showConfigRegistryBrowser(id, path) {
                elementId = id;
                rootPath = path;
                showResourceTree(id, setValue , path);
            }


            var commonParameterString = "txtUsername=" + "<%=request.getParameter("txtUsername")%>" + "&"
                                                + "txtPassword=" + "<%=request.getParameter("txtPassword")%>" + "&"
                                                + "txtIp=" + "<%=request.getParameter("txtIp")%>" + "&"
                                                + "txtPort=" + "<%=request.getParameter("txtPort")%>" + "&"
                                                + "hfStreamTableData=" + "<%=request.getParameter("hfStreamTableData")%>" + "&"
                                                + "txtServerProfileLocation=" + "<%=request.getParameter("txtServerProfileLocation")%>";

            function saveOverwrite(){
                window.location.href = "configure_server_profiles.jsp?" + commonParameterString + "&hfAction=save&overwrite=true";
            }

            function reloadPage(){
                window.location.href = "configure_server_profiles.jsp?" + commonParameterString + "&hfAction=load";
            }

            function stayInPage(){
                window.location.href = "configure_server_profiles.jsp?" + commonParameterString + "&hfAction=stay";
            }

            function showHideDiv(divId) {
                var theDiv = document.getElementById(divId);
                if (theDiv.style.display == "none") {
                    theDiv.style.display = "";
                } else {
                    theDiv.style.display = "none";
                }
            }

            var streamRowNum = 1;
            var propertyRowNum = 1;

            function addPropertyRow() {
                propertyRowNum++;
                var sId = "propertyTable_" + propertyRowNum;
                var tableContent = "<tr id=\"" + sId + "\">" +
                                   "<td>\n" +
                                   "                        <input type=\"text\" name=\"<%=PROPERTY_KEYS%>\" value=\"\">\n" +
                                   "                    </td>\n" +
                                   "                    <td>\n" +
                                   "                        <input type=\"text\" name=\"<%=PROPERTY_VALUES%>\" value=\"\">\n" +
                                   "                    </td>" +
                                   "<td>\n" +
                                   "                        <a onClick='javaScript:removePropertyColumn(\"" + sId + "\")'" +
                                   "style='background-image: url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove Property</a>\n" +
                                   "                    </td>" +
                                   "</tr>";

                jQuery("#propertyTable").append(tableContent);
                updatePropertyTableData();
            }

            function addStreamRow() {
                streamRowNum++;
                var sId = "streamsTable_" + streamRowNum;
                var tableContent = "<tr id=\"" + sId + "\">" +
                                   "<td>\n" +
                                   "<input type=\"text\" name=\"<%=STREAM_NAMES%>\" value=\"\">\n" +
                                   "</td>\n" +
                                   "<td>\n" +
                                   "<input type=\"text\" name=\"<%=STREAM_VERSIONS%>\" value=\"\">\n" +
                                   "</td>" +
                                   "<td>\n" +
                                   "<input type=\"text\" name=\"<%=STREAM_NICKNAME%>\" value=\"\">\n" +
                                   "</td>\n" +
                                   "<td>\n" +
                                   "<input type=\"text\" name=\"<%=STREAM_DESCRIPTION%>\" value=\"\">\n" +
                                   "</td>" +
                                   "<td>\n" +
                                   "<span><a onClick='javaScript:removeStreamColumn(\"" + sId + "\")'" +
                                   "style='background-image: url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove Stream</a></span>\n" +
                                   "<span><a onClick='javaScript:editStreamData(\"" + streamRowNum + "\")''" +
                                   "style='background-image: url(../admin/images/edit.gif);'class='icon-link addIcon'>Edit Stream</a></span>\n" +
                                   "<input type=\"hidden\" id=\"hfStreamsTable_" + streamRowNum + "\" value=\"\"/>"
                                   "</td>" +
                                   "</tr>";

                jQuery("#streamTable").append(tableContent);
                updateStreamTableData();
            }

            function removeStreamColumn(id) {
                jQuery("#" + id).remove();
            }

            function removePropertyColumn(id) {
                jQuery("#" + id).remove();
                updatePropertyTableData();
            }

            function updatePropertyTableData(){
                var tableData = "", inputs, numOfInputs;
                inputs = document.getElementById("propertyTable").getElementsByTagName("input");
                numOfInputs = inputs.length;
                for(var i=0; i<numOfInputs; i=i+2){
                    if(inputs[i].value != "" && inputs[i+1].value != ""){
                        tableData = tableData + inputs[i].value + ":" + inputs[i+1].value + ";";
                    }
                }
                document.getElementById("hfPropertyTableData").value = tableData;
                //alert("hfPropertyTableData : " + document.getElementById("hfPropertyTableData").value);
            }

            function savePropertyTableData(){
                updatePropertyTableData();
                var streamRowNumber = document.getElementById("hfStreamTableRowNumber").value;
                document.getElementById("hfStreamsTable_" + streamRowNumber).value = document.getElementById("hfPropertyTableData").value;
                //alert("hfStreamsTable_ : " + document.getElementById("hfStreamsTable_" + streamRowNumber).value);
                document.getElementById("propertiesTr").style.display = "none";
                jQuery("#streamsTable_" + document.getElementById("hfStreamTableRowNumber").value).css("background-color","");
            }

            function saveDumpData(){
                var data = "";
                if(document.getElementById("mHeader").checked){
                    data = "dump";
                } else{
                    data = "notDump";
                }
                data = data + ";";
                if(document.getElementById("mBody").checked){
                    data = data + "dump";
                } else{
                    data = data + "notDump";
                }
                var streamRowNumber = document.getElementById("hfStreamTableRowNumber").value;
                document.getElementById("hfStreamsTable_" + streamRowNumber).value = document.getElementById("hfStreamsTable_" + streamRowNumber).value + "^" + data;
                document.getElementById("mHeader").checked = "checked";
                document.getElementById("mBody").checked = "checked";
            }

            function saveStreamData(){
                savePropertyTableData();
                saveDumpData();
            }

            function editStreamData(rowNumber){
                //alert("rowID : " + rowNumber);
                jQuery("#streamsTable_" + document.getElementById("hfStreamTableRowNumber").value).css("background-color","");
                jQuery("#streamsTable_" + rowNumber).css("background-color","rgb(234,234,255)");
                document.getElementById("propertiesTr").style.display = "";
                document.getElementById("hfStreamTableRowNumber").value = rowNumber;
                loadPropertyDataTable();
                loadDumpData();
            }

            function loadPropertyDataTable(){
                emptyPropertyTable();
                var rowNumber =  document.getElementById("hfStreamTableRowNumber").value;
                var configDataString = document.getElementById("streamsTable_" + rowNumber).getElementsByTagName("input")[4].value;
                var propertyDataString = configDataString.split("^")[0];
                var propertyDataArray = propertyDataString.split(";");
                var numOfProperties = 0;
                for(var i=0; i<propertyDataArray.length; i++){
                    if(propertyDataArray[i] != ""){
                        addPropertyRow();
                        numOfProperties++;
                    }
                }
                for(var i=0; i<numOfProperties; i=i+1){
                    if(propertyDataArray[i].split(":").length == 2){
                        jQuery("#propertyTable").find("tr").find("input")[2*i].value = propertyDataArray[i].split(":")[0];
                        jQuery("#propertyTable").find("tr").find("input")[2*i+1].value = propertyDataArray[i].split(":")[1];
                    }
                }
                updatePropertyTableData();
            }

            function loadDumpData(){
                cancelDumpData();
                var rowNumber =  document.getElementById("hfStreamTableRowNumber").value;
                var configDataString = document.getElementById("streamsTable_" + rowNumber).getElementsByTagName("input")[4].value;
                var dumpDataString = "";
                if(configDataString.split("^").length == 2){
                    dumpDataString = configDataString.split("^")[1];
                    var dumpDataArray = dumpDataString.split(";");
                    if(dumpDataArray.length == 2){
                        if(dumpDataArray[0] == "dump"){
                            document.getElementById("mHeader").checked = "checked";
                        } else {
                            document.getElementById("mHeader").checked = "";
                        }
                        if(dumpDataArray[1] == "dump"){
                            document.getElementById("mBody").checked = "checked";
                        } else {
                            document.getElementById("mBody").checked = "";
                        }
                    }
                }
            }

            function emptyPropertyTable(){
                document.getElementById("hfPropertyTableData").value = "";
                jQuery("#propertyTable").find("tr").find("input")[0].value = "";
                jQuery("#propertyTable").find("tr").find("input")[1].value = "";
                var tableRowNumber = jQuery("#propertyTable").find("tr").length;
                if(tableRowNumber > 2){
                    for(var i=2; i<tableRowNumber; i++){
                        var currentRowId = jQuery("#propertyTable").find("tr")[2].id;
                        jQuery("#" + currentRowId).remove();
                    }
                }
            }

            function cancelPropertyTableData(){
                emptyPropertyTable();
                document.getElementById("propertiesTr").style.display = "none";
                jQuery("#streamsTable_" + document.getElementById("hfStreamTableRowNumber").value).css("background-color","");
            }

            function cancelDumpData(){
                document.getElementById("mHeader").checked = "checked";
                document.getElementById("mBody").checked = "checked";
            }

            function cancelStreamData(){
                cancelPropertyTableData();
                cancelDumpData();
            }

            function updateStreamTableData(){
                var tableData = "", inputs, numOfInputs;
                inputs = document.getElementById("streamTable").getElementsByTagName("input");
                numOfInputs = inputs.length;
                for(var i=0; i<numOfInputs; i=i+5){
                    if(inputs[i].value != "" && inputs[i+1].value != ""){
                        if(i != 0){
                            tableData = tableData + "~";
                        }
                        tableData = tableData + inputs[i].value + "^"
                                            + inputs[i+1].value + "^" + inputs[i+2].value + "^"
                                            + inputs[i+3].value + "^" + inputs[i+4].value;
                    }
                }
                document.getElementById("hfStreamTableData").value = tableData;
                //alert("hfStreamTableData : " + document.getElementById("hfStreamTableData").value);
            }

            function submitPage(){
                updateStreamTableData();
                document.getElementById('hfAction').value='save';
            }
        </script>


    <%

    if("save".equals(action) && !"true".equals(overwrite) && bamServerProfileUtils.resourceAlreadyExists(serverProfileLocation)){
        %>

            <script>
                CARBON.showConfirmationDialog("Are you sure you want to overwrite the existing Server Profile Configuration?", saveOverwrite, stayInPage, true);
            </script>

        <%
    }

    else if("load".equals(action)){  // loading an existing configuration
        if(bamServerProfileUtils.isNotNullOrEmpty(tmpServerProfileLocation)){
            serverProfileLocation = tmpServerProfileLocation;
            if(bamServerProfileUtils.resourceAlreadyExists(serverProfileLocation)){
                bamServerConfig = bamServerProfileUtils.getResource(serverProfileLocation);

                userName = bamServerConfig.getUsername();
                password = bamServerProfileUtils.decryptPassword(bamServerConfig.getPassword());
                ip = bamServerConfig.getIp();
                port = bamServerConfig.getPort();
            }
            else {
                %>

                <script type="text/javascript">
                    alert("Resource is not existing in the given location!");
                </script>

                <%
            }
        }
        else {
            %>

            <script type="text/javascript">
                alert("Enter the Server Profile Location.");
            </script>

            <%
        }
    }

    else if("stay".equals(action)){  // staying in the existing page

    }

    else if("save".equals(action) && !"".equals(serverProfileLocation)){ // Saving a configuration
        if("true".equals(overwrite)){
            bamServerProfileUtils.addResource(ip, port, userName, password, streamTable, serverProfileLocation);
        }
        else if (!"true".equals(overwrite)){
            if(!bamServerProfileUtils.resourceAlreadyExists(serverProfileLocation)){
                bamServerProfileUtils.addResource(ip, port, userName, password, streamTable, serverProfileLocation);
            }
            else {
                %>

                    <script type="text/javascript">
                        alert("Resource already exists!");
                    </script>

                <%
            }
        }
        %>

            <script type="text/javascript">
                reloadPage();
            </script>

        <%
    }


%>


<div id="middle">
    <h2>
        <fmt:message key="bam.server.profile"/>
    </h2>

    <div id="workArea">
        <form action="configure_server_profiles.jsp" method="post">
        <table>
            <tr>
                <td>
                    <fmt:message key="profile.location"/><span class="required">*</span>
                </td>
                <td>
                    <table>
                        <tr>

                            <td>
                                <input class="longInput" type="text"
                                       value="<%=serverProfileLocation%>"
                                       id="txtServerProfileLocation" name="txtServerProfileLocation"/>
                            </td>
                            <td>
                                <a href="#registryBrowserLink"
                                   class="registry-picker-icon-link"
                                   onclick="showConfigRegistryBrowser('txtServerProfileLocation','/_system/config')"><fmt:message key="conf.registry.browser"/>
                                </a>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>

            <tr>
                <td></td>
                <td>
                    <input type="submit" value="Load Profile" onclick="document.getElementById('hfAction').value='load';"/>
                    <input type="hidden" name="hfAction" id="hfAction" value=""/>
                </td>
            </tr>




            <tr>
                <td>
                    <fmt:message key="username"/>
                </td>
                <td>
                    <input type="text" name="txtUsername" id="txtUsername" value="<%=userName%>"/>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="password"/>
                </td>
                <td>
                    <input type="password" name="txtPassword" id="txtPassword" value="<%=password%>"/>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="ip"/>
                </td>
                <td>
                    <input type="text" name="txtIp" id="txtIp" value="<%=ip%>"/>
                </td>
            </tr>
            <tr>
                <td>
                    <fmt:message key="port"/>
                </td>
                <td>
                    <input type="text" name="txtPort" id="txtPort" value="<%=port%>"/>
                </td>
            </tr>

            <tr>
                <td colspan="2">
                    <h3>
                        <fmt:message key="streams.configuration"/>
                    </h3>
                </td>
            </tr>

            <tr id="streamsTr">
                <td colspan="2">
                    <input name="hfStreamTableData" id="hfStreamTableData" type="hidden" value="" />
                    <table id="streamTable" width="100%" class="styledLeft" style="margin-left: 0px;">
                        <thead>
                            <tr>
                                <th width="40%">
                                    <fmt:message key="stream.name"/>
                                </th>
                                <th width="40%">
                                    <fmt:message key="stream.version"/>
                                </th>
                                <th width="40%">
                                    <fmt:message key="stream.nickName"/>
                                </th>
                                <th width="40%">
                                    <fmt:message key="stream.description"/>
                                </th>
                                <th>

                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            <%
                                if (bamServerConfig.getStreamConfigurations() != null && !bamServerConfig.getStreamConfigurations().isEmpty()) {
                                streamConfigurations = bamServerConfig.getStreamConfigurations();
                                int i = 1;
                                for (StreamConfiguration streamConfiguration : streamConfigurations) {
                            %>
                            <tr id="streamsTable_<%=i%>">
                                <td>
                                    <input id="streamName" type="text" name="<%=STREAM_NAMES%>" value="<%=streamConfiguration.getName()%>"/>
                                </td>
                                <td>
                                    <input id="streamVersion" type="text" name="<%=STREAM_VERSIONS%>" value="<%=streamConfiguration.getVersion()%>"/>
                                </td>
                                <td>
                                    <input id="streamNickname" type="text" name="<%=STREAM_NICKNAME%>" value="<%=streamConfiguration.getNickname()%>"/>
                                </td>
                                <td>
                                    <input id="streamDescription" type="text" name="<%=STREAM_DESCRIPTION%>" value="<%=streamConfiguration.getDescription()%>"/>
                                </td>
                                <% if (i == 1) { %>
                                <td><span><a onClick='javaScript:addStreamRow()' style='background-image:
                                        url(images/add.gif);'class='icon-link addIcon'>Add Stream</a></span>
                                    <span><a onClick='javaScript:editStreamData("<%=i%>")' style='background-image:
                                        url(../admin/images/edit.gif);'class='icon-link addIcon'>Edit Stream</a></span>
                                    <input type="hidden" id="hfStreamsTable_<%=i%>" value="<%=bamServerProfileUtils.getStreamConfigurationListString(streamConfiguration)%>"/>
                                </td>
                                <% } else {  %>
                                <td>
                                    <span><a onClick='javaScript:removeStreamColumn("streamsTable_<%=i%>")' style='background-image:
                                        url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove Stream</a></span>
                                    <span><a onClick='javaScript:editStreamData("<%=i%>")' style='background-image:
                                        url(../admin/images/edit.gif);'class='icon-link addIcon'>Edit Stream</a></span>
                                    <input type="hidden" id="hfStreamsTable_<%=i%>" value="<%=bamServerProfileUtils.getStreamConfigurationListString(streamConfiguration)%>"/>
                                </td>
                                <% } %>

                            </tr>
                            <script type="text/javascript">
                                streamRowNum++;
                            </script>
                            <%  i++;
                            }
                            } else { %>
                            <tr id="streamsTable_1">
                                <td>
                                    <input type="text" name="<%=STREAM_NAMES%>" value=""/>
                                </td>
                                <td>
                                    <input type="text" name="<%=STREAM_VERSIONS%>" value=""/>
                                </td>
                                <td>
                                    <input type="text" name="<%=STREAM_NICKNAME%>" value=""/>
                                </td>
                                <td>
                                    <input type="text" name="<%=STREAM_DESCRIPTION%>" value=""/>
                                </td>

                                <td>
                                    <span><a onClick='javaScript:addStreamRow()' style='background-image: url(images/add.gif);'class='icon-link addIcon'>Add Stream</a></span>
                                    <span><a onClick='javaScript:editStreamData("1")' style='background-image: url(../admin/images/edit.gif);'class='icon-link addIcon'>Edit Stream</a></span>
                                    <input type="hidden" id="hfStreamsTable_1" value=""/>
                                </td>
                            </tr>
                        </tbody>

                        <% } %>
                    </table>
                </td>
            </tr>





            <tr id="propertiesTr" style="display: none;">
                <td colspan="2">
                    <input name="hfPropertyTableData" id="hfPropertyTableData" type="hidden" value="" />
                    <input id="hfStreamTableRowNumber" type="hidden" value="1" />
                    <h3>
                        <fmt:message key="stream.configuration"/>
                    </h3>

                    <table>
                        <tr>
                            <td>
                                <h4>
                                    <fmt:message key="stream.payload"/>
                                </h4>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <table>
                                    <tr>
                                        <td>
                                            <fmt:message key="dump.header"/>
                                        </td>
                                        <td>
                                            <input type="checkbox" id="mHeader" name="mHeader" checked="checked" value="dump"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <fmt:message key="dump.body"/>
                                        </td>
                                        <td>
                                            <input type="checkbox" id="mBody" name="mBody" checked="checked" value="dump"/>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>


                        <tr>
                            <td>
                                <h4>
                                    <fmt:message key="stream.properties"/>
                                </h4>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <table id="propertyTable" width="100%" class="styledLeft" style="margin-left: 0px;">
                                    <thead>
                                    <tr>
                                        <th width="40%">
                                            <fmt:message key="property.name"/>
                                        </th>
                                        <th width="40%">
                                            <fmt:message key="property.value"/>
                                        </th>
                                        <th></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr id="propertyTable_1">
                                        <td>
                                            <input type="text" name="<%=PROPERTY_KEYS%>" value=""/>
                                        </td>
                                        <td>
                                            <input type="text" name="<%=PROPERTY_VALUES%>" value=""/>
                                        </td>

                                        <td>
                                            <a onClick='javaScript:addPropertyRow()' style='background-image: url(images/add.gif);'class='icon-link addIcon'>Add Property</a>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input type="button" value="Update" onclick="saveStreamData()"/>
                                <input type="button" value="Cancel" onclick="cancelStreamData()"/>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>




            <tr>
                <td>
                    <input type="submit" value="Save" onclick="submitPage()"/>
                </td>
            </tr>
        </table>
        </form>
    </div>
</div>


</fmt:bundle>
