<!--
~ Copyright 2009 WSO2, Inc. (http://wso2.com)
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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>
<!-- local styles -->
<link type="text/css" rel="stylesheet" href="css/tree-styles.css"/>

<!-- Dependencies -->
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu-min.js"></script>

<!-- Connection handling lib -->
<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/event/event-min.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<%--Drag drop handling lib--%>
<script type="text/javascript" src="../yui/build/dragdrop/dragdrop-min.js"></script>

<%--Tree animation handling lib--%>
<script type="text/javascript" src="../yui/build/animation/animation-min.js"></script>

<script type="text/javascript" src="js/server-data-tree.js"></script>

<fmt:bundle basename="org.wso2.carbon.bam.ui.i18n.Resources">
<carbon:breadcrumb
        label="server.list"
        resourceBundle="org.wso2.carbon.bam.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<%
    String[][] colorsTree = {{"b5b5b5","Server"},{"dadde5","Service"},{"e4e5da","Operation"}};
    String[][] colorsSubTree = {{"bcc5d2","Endpoints"},{"c4c4a2","Proxy Services"},{"c7d9af","Sequences"}};
    String[][] colorsSubTreeChilds = {{"dee2e9",""},{"e2e2bb",""},{"dbe7cb",""}};
    String[][] colorsGraphA = {{"6eb65a","Average"},{"a870bb","Maximum"},{"e2ed2d","Minimum"}};
    String[][] colorsGraphB = {{"8d3d00","In"},{"e23124","Fault"}};
    String[][] colorsGraphC = {{"ee6b00","Out"},{"e23124","Fault"}};

%>

<style type="text/css">
    <%--Generate tree lvel row styles--%>
    <%
        String color = "";
        for(int i=0;i<colorsSubTree.length;i++){
            %>.levelSub<%=i%>{
                background-color:#<%=colorsSubTree[i][0]%>;
            }
      <%  }
    %>
    <%--Generate tree lvel row styles--%>
    <%
        color = "";
        for(int i=0;i<colorsSubTreeChilds.length;i++){
            %>.levelSubChild<%=(i+1)%>{
                background-color:#<%=colorsSubTreeChilds[i][0]%>;
            }
      <%  }
    %>
    <%--Generate tree lvel row styles--%>
    <%
        color = "";
        for(int i=0;i<colorsTree.length;i++){
            %>.level<%=(i+1)%>{
                background-color:#<%=colorsTree[i][0]%>;
            }
      <%  }
    %>
    <%--Generate graph colors A --%>
    <%
        for(int i=0;i<colorsGraphA.length;i++){
            %>.barA<%=(i+1)%>{
                background-color:#<%=colorsGraphA[i][0]%>;
            }
      <%  }
    %>
    <%--Generate graph colors B--%>
    <%
        for(int i=0;i<colorsGraphB.length;i++){
            %>.barB<%=(i+1)%>{
                background-color:#<%=colorsGraphB[i][0]%>;
            }
      <%  }
    %>
    <%--Generate graph colors C--%>
    <%
        for(int i=0;i<colorsGraphC.length;i++){
            %>.barC<%=(i+1)%>{
                background-color:#<%=colorsGraphC[i][0]%>;
            }
      <%  }
    %>
</style>

<script type="text/javascript">
    YAHOO.util.Event.addListener(window, 'scroll', positionLegendTop);
    YAHOO.util.Event.addListener(window, 'resize', positionLegendLeft);
    var legend = document.getElementById('serverDataLegend');

var sUrl = '../bam-server-data/get_mediation_data_ajaxprocessor.jsp'
    $(document).ready(function() {
        updatePage();
        var timerId = setInterval(updatePage, 600000);    //600,000 milliseconds(10 minute)
    });

    function updatePage(){
          $.ajax({
            type:"GET",
            url:sUrl,
            dataType: "html",
            success:
                    function(data, status)
                    {
                        $('#treeData').html(data);
                    }
        });
        serverDataInit();

    }
    var selectedServer =null;
    var selectedService =null;
    var selectedOperation = null;
    var selected=null;
</script>

<div id="middle">
<h2>
    <fmt:message key="mediation.data.summary"/>
</h2>
<div id="workArea">
<div id="report_ui">
<carbon:report component="org.wso2.carbon.bam.core"
               template="mediation_data"
               pdfReport="true"
               htmlReport="true"
               excelReport="true"
               reportDataSession="reportMediationData"/>
    </div>

<div id="treeData" style="padding-top:100px;"></div>
</div>
</div>
<div id="serverDataLegend" class="legendBox">
    <div class="legendBox-head"></div>
    <div id="serverDataLegendInside">
    <table cellpadding="0" cellspacing="0" class="noBorders">
        <tr>
            <td colspan="2">
            <%
                for(int i=0;colorsSubTree.length>i;i++){
            %>
            <div class="legendBox-out"><div class="legendBox-label"><%=colorsSubTree[i][1]%></div><div class="legendBox-color" style="background-color:#<%=colorsSubTree[i][0]%>"></div> </div>
            <% } %>
            </td>
        </tr>
        <tr>
            <td class="legendBox-box" style="vertical-align:bottom !important;width:325px;">
                <table>
                    <tr>
                        <td>
                            <div class="legendBox-label-big">Service Counts - In</div>
                            <%
                                for (int i = 0; i < colorsGraphB.length; i++) {
                            %>
                            <div class="legendBox-out">
                                <div class="legendBox-label"><%=colorsGraphB[i][1]%>
                                </div>
                                <div class="legendBox-color" style="background-color:#<%=colorsGraphB[i][0]%>"></div>
                            </div>
                            <% } %>
                            <div style="clear:both"></div>
                        </td>
                        <td>
                            <div class="legendBox-label-big">Service Counts - Out</div>
                            <%
                                for (int i = 0; i < colorsGraphC.length; i++) {
                            %>
                            <div class="legendBox-out">
                                <div class="legendBox-label"><%=colorsGraphC[i][1]%>
                                </div>
                                <div class="legendBox-color" style="background-color:#<%=colorsGraphC[i][0]%>"></div>
                            </div>
                            <% } %>
                        </td>
                    </tr>
                </table>

            </td>
            <td class="legendBox-box" style="vertical-align:bottom !important;width:265px;">
            <div class="legendBox-label-big">Response Time </div>
            <%
                for(int i=0;i<colorsGraphA.length;i++){
            %>
            <div class="legendBox-out"><div class="legendBox-label"><%=colorsGraphA[i][1]%></div><div class="legendBox-color" style="background-color:#<%=colorsGraphA[i][0]%>"></div> </div>
            <% } %>
            </td>
        </tr>
    </table>
    </div>
</div>
</fmt:bundle>
