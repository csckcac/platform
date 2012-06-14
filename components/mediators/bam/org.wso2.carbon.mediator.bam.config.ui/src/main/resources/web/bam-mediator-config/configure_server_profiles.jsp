<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.mediator.bam.config.ui.BamServerProfileUtils" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.mediator.bam.config.BamServerConfig" %>
<%@ page import="org.wso2.carbon.mediator.bam.config.stream.StreamConfiguration" %>
<%@ page import="org.wso2.carbon.bam.mediationstats.data.publisher.stub.conf.Property" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

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
    String userName = "admin";
    String password = "admin";
    String ip = "localhost";
    String port = "7611";
    String serverProfileLocation = "";
    String isLoading = "";
    String streamTable = "";

    BamServerConfig bamServerConfig = new BamServerConfig();
    List<StreamConfiguration> streamConfigurations = null;

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


    String tmpServerProfileLocation = request.getParameter("txtServerProfileLocation");
    String tmpIsLoading = request.getParameter("hfIsLoading");
    if(bamServerProfileUtils.isNotNullOrEmpty(tmpIsLoading) && tmpIsLoading.equals("true")){  // loading an existing configuration
        isLoading = tmpIsLoading;
        if(bamServerProfileUtils.isNotNullOrEmpty(tmpServerProfileLocation)){
            serverProfileLocation = tmpServerProfileLocation;
            if(bamServerProfileUtils.resourceAlreadyExists(serverProfileLocation)){
                bamServerConfig = bamServerProfileUtils.getResource(serverProfileLocation);

                userName = bamServerConfig.getUsername();
                password = bamServerConfig.getPassword();
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
    else{ // inserting a new configuration
        if(bamServerProfileUtils.isNotNullOrEmpty(tmpServerProfileLocation)){
            serverProfileLocation = tmpServerProfileLocation;

            if(!bamServerProfileUtils.resourceAlreadyExists(serverProfileLocation)){
                bamServerProfileUtils.addResource(ip, port, userName, password, serverProfileLocation);
            }
            else {
                %>

                <script type="text/javascript">
                    alert("Resource already exists!");
                </script>

                <%
            }
        }
    }

    





%>

<%--<script type="text/javascript" src="'<%=CarbonUtils.getCarbonHome()%>' + 'sequences/js/registry-browser.js'"/>
<script type="text/javascript" src="'<%=CarbonUtils.getCarbonHome()%>' + 'resources/js/resource_util.js'"/>
<script type="text/javascript" src="'<%=CarbonUtils.getCarbonHome()%>' + 'yui/build/connection/connection-min.js'"/>--%>

<script id="source" type="text/javascript">
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

    function addPropertyColumn() {
        propertyRowNum++;
        /*var n =  + parseInt(trId.charAt(trId.length-1))+1;
         jQuery("#"+trId+" td div.addIcon").remove();*/
        //alert(n);
        var sId = "propertyTable_" + propertyRowNum;
        //alert(sId);
        var tableContent = "<tr id=\"" + sId + "\">" +
                           "<td>\n" +
                           "                        <input type=\"text\" name=\"<%=PROPERTY_KEYS%>\" value=\"\">\n" +
                           "                    </td>\n" +
                           "                    <td>\n" +
                           "                        <input type=\"text\" name=\"<%=PROPERTY_VALUES%>\" value=\"\">\n" +
                           "                    </td>" +
                           "<td>\n" +
                           "                        <a onClick='javaScript:removePropertyColumn(\"" + sId + "\")'" +
                           "style='background-image: url(images/delete.gif);'class='icon-link addIcon'>Remove Property</a>\n" +
                           "                    </td>" +
                           "</tr>";

        jQuery("#propertyTable").append(tableContent);
        updatePropertyTableData();
    }

    function addStreamColumn() {
        streamRowNum++;
        /*var n =  + parseInt(trId.charAt(trId.length-1))+1;
         jQuery("#"+trId+" td div.addIcon").remove();*/
        //alert(n);
        var sId = "streamsTable_" + streamRowNum;
        //alert(sId);
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
                           "style='background-image: url(images/delete.gif);'class='icon-link addIcon'>Remove Stream</a></span>\n" +
                           "<span><a onClick='javaScript:editStreamData(\"" + streamRowNum + "\")''" +
                           "style='background-image: url(images/delete.gif);'class='icon-link addIcon'>Edit Stream</a></span>\n" +
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
            /*tableData = tableData + jQuery("#propertyName")[i].value + ":"
             + jQuery("#propertyValue")[i].value + ";" ;*/
            /*tableData[i]["name"] = jQuery("#propertyName")[i].value;
             tableData[i]["value"] = jQuery("#propertyValue")[i].value;*/
        }
        document.getElementById("hfPropertyTableData").value = tableData;
        var streamRowNumber = document.getElementById("hfStreamTableRowNumber").value;
        document.getElementById("hfStreamsTable_" + streamRowNumber).value = tableData;
        alert("hf table : " + document.getElementById("hfStreamsTable_" + streamRowNumber).value);
    }

    function savePropertyTableData(){
        var streamRowNumber = document.getElementById("hfStreamTableRowNumber").value;
        document.getElementById("hfStreamsTable_" + streamRowNumber).value = document.getElementById("hfPropertyTableData").value;
        alert("Properties table : " + document.getElementById("hfStreamsTable_" + streamRowNumber).value);
        document.getElementById("propertiesTr").style.display = "none";
    }

    function editStreamData(rowNumber){
        alert("rowID : " + rowNumber);
        document.getElementById("propertiesTr").style.display = "";
        document.getElementById("hfStreamTableRowNumber").value = rowNumber;
        //var properties = document.getElementById("propertyTable").getElementsByTagName("input");
        // TODO remaining
    }

    function updateStreamTableData(){
        var tableData = "", inputs, numOfInputs;
        inputs = document.getElementById("streamTable").getElementsByTagName("input");
        numOfInputs = inputs.length;
        for(var i=0; i<numOfInputs; i=i+2){
            if(inputs[i].value != "" && inputs[i+1].value != ""){
                tableData = tableData + inputs[i].value + ":" + inputs[i+1].value + ";";
            }
            /*tableData = tableData + jQuery("#propertyName")[i].value + ":"
             + jQuery("#propertyValue")[i].value + ";" ;*/
            /*tableData[i]["name"] = jQuery("#propertyName")[i].value;
             tableData[i]["value"] = jQuery("#propertyValue")[i].value;*/
        }
        document.getElementById("hfStreamTableData").value = tableData;
        alert("hf table : " + document.getElementById("hfStreamTableData").value);
    }
</script>




<div id="middle">
    <h2>
        <fmt:message key="bam.server.profile"/>
    </h2>

    <div id="workArea">
        <form action="configure_server_profiles.jsp" method="post">
        <table>
            <tr>
                <td>
                    Registry
                </td>
                <td>
                    <input type="text" name="txtServerProfileLocation" id="txtServerProfileLocation" value=""/>
                    <input type="submit" value="Load Profile" onclick="document.getElementById('hfIsLoading').value='true';"/>
                    <input type="hidden" name="hfIsLoading" id="hfIsLoading" value="false"/>
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
                                <td><span><a onClick='javaScript:addStreamColumn()' style='background-image:
                                        url(images/add.gif);'class='icon-link addIcon'>Add Stream</a></span>
                                    <span><a onClick='javaScript:editStreamData("<%=i%>")' style='background-image:
                                        url(images/add.gif);'class='icon-link addIcon'>Edit Stream</a></span>
                                    <input type="hidden" id="hfStreamsTable_<%=i%>" value=""/>
                                </td>
                                <% } else {  %>
                                <td>
                                    <span><a onClick='javaScript:removeStreamColumn("streamsTable_<%=i%>")' style='background-image:
                                        url(images/delete.gif);'class='icon-link addIcon'>Remove Stream</a></span>
                                    <span><a onClick='javaScript:editStreamData("<%=i%>")' style='background-image:
                                        url(images/add.gif);'class='icon-link addIcon'>Edit Stream</a></span>
                                    <input type="hidden" id="hfStreamsTable_<%=i%>" value=""/>
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
                                    <span><a onClick='javaScript:addStreamColumn()' style='background-image: url(images/add.gif);'class='icon-link addIcon'>Add Stream</a></span>
                                    <span><a onClick='javaScript:editStreamData("1")' style='background-image: url(images/add.gif);'class='icon-link addIcon'>Edit Stream</a></span>
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
                            <tr>
                                <td>
                                    <input type="text" name="<%=PROPERTY_KEYS%>" value=""/>
                                </td>
                                <td>
                                    <input type="text" name="<%=PROPERTY_VALUES%>" value=""/>
                                </td>

                                <td>
                                    <a onClick='javaScript:addPropertyColumn()' style='background-image: url(images/add.gif);'class='icon-link addIcon'>Add Property</a>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <input type="button" value="Update" onclick="savePropertyTableData()"/>
                </td>
            </tr>




            <tr>
                <td>
                    <input type="submit" value="Save"/>
                </td>
            </tr>
        </table>
        </form>
    </div>
</div>


</fmt:bundle>

