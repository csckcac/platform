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


<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>
<%--Yahoo Esentials--%>
<script type="text/javascript" src="js/yahoo-dom-event/yahoo-dom-event.js"></script>


<%--Tree animation handling lib--%>
<script type="text/javascript" src="../yui/build/animation/animation-min.js"></script>

<%--Calender includes--%>
<link rel="stylesheet" type="text/css" href="../yui/build/calendar/assets/skins/sam/calendar.css"/>
<script type="text/javascript" src="../yui/build/calendar/calendar-min.js"></script>

<%--Slider includes--%>
<link rel="stylesheet" type="text/css" href="js/slider/assets/skins/sam/slider.css"/>
<script type="text/javascript" src="js/dragdrop/dragdrop-min.js"></script>
<script type="text/javascript" src="js/slider/slider-min.js"></script>

<%--Local includes--%>
<script type="text/javascript" src="js/dates.js"></script>
<script type="text/javascript" src="js/mediation_analytics.js"></script>
<script type="text/javascript" src="js/server-data-tree.js"></script>
<script type="text/javascript" src="js/cookies.js"></script>
<link type="text/css" rel="stylesheet" href="css/dates.css"/>
<link type="text/css" rel="stylesheet" href="css/tabview.css"/>

<fmt:bundle basename="org.wso2.carbon.bam.ui.i18n.Resources">
<carbon:breadcrumb
        label="server.list"
        resourceBundle="org.wso2.carbon.bam.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<%
    //To change graph colors you only need to do it here
    String[][] colorsTree = {{"b5b5b5", "Server"}, {"dadde5", "Service"}, {"e4e5da", "Operation"}};
    String[][] colorsSubTree = {{"bcc5d2", "Endpoints"}, {"c4c4a2", "Proxy Services"}, {"c7d9af", "Sequences"}};
    String[][] colorsSubTreeChilds = {{"dee2e9", ""}, {"e2e2bb", ""}, {"dbe7cb", ""}};

%>
<style type="text/css">
    <%--Generate tree lvel row styles--%>
    <%
        String color = "";
        for(int i=0;i<colorsSubTree.length;i++){
            %>.levelSub <%=i%> {
        background-color: # <%=colorsSubTree[i][0]%>;
    }

    <%  }
    %>
    <%--Generate tree lvel row styles--%>
    <%
        color = "";
        for(int i=0;i<colorsSubTreeChilds.length;i++){
            %>.levelSubChild <%=(i+1)%> {
        background-color: # <%=colorsSubTreeChilds[i][0]%>;
    }

    <%  }
    %>
    <%--Generate tree lvel row styles--%>
    <%
        color = "";
        for(int i=0;i<colorsTree.length;i++){
            %>.level <%=(i+1)%> {
        background-color: # <%=colorsTree[i][0]%>;
    }

    <%  }
    %>

</style>

<div id="chartSizes">

</div>
<div id="middle">
<h2>
    <fmt:message key="mediation.analytics"/>
</h2>

<div id="workArea">
<ul class="dates-types" id="datesTypes">
    <li><a class="nor-right" onclick="setPageMode('month',this)" id="monthLink">Month</a></li>
    <li><a class="nor-rep" onclick="setPageMode('day',this)" id="dayLink">Day</a></li>
    <li><a class="sel-left" onclick="setPageMode('hour',this)" id="hourLink">Hour</a></li>
</ul>
<br/>

<div style="height:40px;"><a style="cursor:pointer;" onclick="toggleDateSelector()">
    <table class="time-header">
        <tr>
            <td><span id="dateDisplay"></span><img
                    src="images/down.png"
                    alt="Show Calendar"
                    align="middle"
                    style="margin-bottom: 4px;margin-left:5px;margin-right:5px"
                    id="imgObj"/></td>
        </tr>
    </table>
</a></div>
<div class="dates-selection-Box yui-skin-sam" style="display:none" id="datesSelectionBox">
    <div id="cal1Container" style="display:none;"></div>
    <div class="timeBox-main" id="timeBox-main"></div>
    <div class="monthBox-main" id="monthBox-main" style="display:none"></div>
    <div class="dates">
        <strong>Date Range</strong>
        <table>
            <tr>
                <td><input type="text" name="in" id="in"></td>
                <td> -</td>
                <td><input type="text" name="out" id="out"></td>
            </tr>
        </table>
    </div>
    <div style="clear:both;padding-top:5px;"><input type="button" value="Apply"
                                                    onclick="updatePage();toggleDateSelector()"
                                                    class="button"/></div>
</div>
<div style="clear:both"></div>

<br/>
<a name="navigateto"/>

<div id="chartData">
    <div class="loadingPosition"><img align="top" src="images/ajax-loader.gif"/></div>
</div>
    <%--<jsp:include page="get_service_stats_ajaxprocessor.jsp" />--%>


<script type="text/javascript">

function showAllGraphs(serverArrays, type, label) {
    serverArrays.sort(compareTimestamps);
    var allareZero = true;
    for (var j = 0; j < serverArrays.length; j++) {
        if (type == 'count') {
            if (serverArrays[j].request != 0) {
                allareZero = false;
            }
            if (serverArrays[j].fault != 0) {
                allareZero = false;
            }
        } else {
            if (serverArrays[j].average != 0) {
                allareZero = false;
            }
            if (serverArrays[j].maximum != 0) {
                allareZero = false;
            }
            if (serverArrays[j].minimum != 0) {
                allareZero = false;
            }
        }
    }
    var wpWidth = YAHOO.util.Dom.getViewportWidth();
    YAHOO.widget.Chart.SWFURL = "js/charts/assets/charts.swf";

    //series definition for Column and Line Charts
    var myDataSource = new YAHOO.util.DataSource(serverArrays);
    myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;

    if (type == 'count') {
        myDataSource.responseSchema =
        {
            fields: [ "time", "request", "response","fault" ]
        };
        var seriesDef =
                [
                    {
                        displayName: "<fmt:message key="request.count"/>",
                        yField: "request",
                        style: { skin:"CircleSkin"}
                    },
                    {
                        displayName: "<fmt:message key="fault.count"/>",
                        yField: "fault",
                        style: { color: 0x42632f,skin:"RectangleSkin"}
                    }
                ];
        var barChartSeriesDef =
                [
                    {
                        displayName: "<fmt:message key="request.count"/>",
                        xField: "request",
                        style: { skin:"CircleSkin"}
                    },
                    {
                        displayName: "<fmt:message key="fault.count"/>",
                        xField: "fault",
                        style: { color: 0x42632f,skin:"RectangleSkin"}
                    }
                ];
    } else {
        myDataSource.responseSchema =
        {
            fields: [ "time", "average", "maximum","minimum" ]
        };
        var seriesDef =
                [
                    {
                        displayName: "<fmt:message key="average.response.time"/>",
                        yField: "average",
                        style: { skin:"CircleSkin"}
                    },
                    {
                        displayName: "<fmt:message key="maximum.response.time"/>",
                        yField: "maximum",
                        style: { color:0xf38800,skin:"DiamondSkin"}
                    },
                    {
                        displayName: "<fmt:message key="minimum.response.time"/>",
                        yField: "minimum",
                        style: { color: 0x42632f,skin:"RectangleSkin"}
                    }
                ];
        var barChartSeriesDef =
                [
                    {
                        displayName: "<fmt:message key="average.response.time"/>",
                        xField: "average",
                        style: { skin:"CircleSkin"}
                    },
                    {
                        displayName: "<fmt:message key="maximum.response.time"/>",
                        xField: "maximum" ,
                        style: { color:0xf38800,skin:"DiamondSkin"}
                    },
                    {
                        displayName: "<fmt:message key="minimum.response.time"/>",
                        xField: "minimum",
                        style: { color: 0x42632f,skin:"RectangleSkin"}
                    }
                ];
    }


    formatValueAxisLabel = function(value)
    {
        return YAHOO.util.Number.format(value,
        {
            prefix: "",
            thousandsSeparator: ",",
            decimalPlaces: 0
        });
    }


    getDataTipText = function(item, index, series)
    {
        var toolTipText = series.displayName + " for " + item.time;
        toolTipText += "\n" + formatValueAxisLabel(item[series.yField]);
        return toolTipText;
    }
    //DataTip function for the Line Chart and Column Chart
    var getYAxisDataTipText = function(item, index, series)
    {
        return getDataTipText(item, index, series, "yField");
    }

    //DataTip function for the Bar Chart
    var getXAxisDataTipText = function(item, index, series)
    {
        return getDataTipText(item, index, series, "xField");
    }

    var valueAxis = new YAHOO.widget.NumericAxis();

    if (allareZero) {
        valueAxis.minimum = 130;
    }
    valueAxis.labelFunction = formatValueAxisLabel;
    //YAHOO.util.Dom.setStyle('chart'+type,'width','700px');

    var mychartCount = new YAHOO.widget.LineChart("chartline" + type, myDataSource,
    {
        series: seriesDef,
        xField: "time",
        yAxis: valueAxis,
        dataTipFunction: getYAxisDataTipText,
        //only needed for flash player express install
        expressInstall: "assets/expressinstall.swf",
        style:
        {
            padding: 5,
            legend:
            {
                display: "top",
                padding: 0,
                spacing: 5,
                font:
                {
                    family: "Arial",
                    size: 11
                }
            },
            xAxis:
            {
                labelRotation:-40,
                hideOverlappingLabels:true
            },
            yAxis:
            {
                hideOverlappingLabels:true
            }
        }
    });
    //Create Bar Chart
    var barChart = new YAHOO.widget.BarChart("chartbar" + type, myDataSource,
    {
        series:barChartSeriesDef,
        yField: "time",
        xAxis: valueAxis,
        dataTipFunction: getXAxisDataTipText,
        //only needed for flash player express install
        expressInstall: "assets/expressinstall.swf",
        style:
        {
            padding: 5,
            legend:
            {
                display: "top",
                padding: 0,
                spacing: 5,
                font:
                {
                    family: "Arial",
                    size: 11
                }
            },
            yAxis:
            {
                hideOverlappingLabels:true
            },
            xAxis:
            {
                hideOverlappingLabels:true
            }
        }
    });

    //Create Column Chart
    var columnChart = new YAHOO.widget.ColumnChart("chartcolumn" + type, myDataSource,
    {
        series: seriesDef,
        xField: "time",
        yAxis: valueAxis,
        dataTipFunction: getYAxisDataTipText,
        //only needed for flash player express install
        expressInstall: "assets/expressinstall.swf",
        style:
        {
            padding: 5,
            legend:
            {
                display: "top",
                padding: 0,
                spacing: 5,
                font:
                {
                    family: "Arial",
                    size: 11
                }
            },
            xAxis:
            {
                labelRotation:-40,
                hideOverlappingLabels:true
            },
            yAxis:
            {
                hideOverlappingLabels:true
            }
        }
    });
    if (label != null) {
        document.getElementById('titleRight').innerHTML = label;
    }

}

</script>
</div>
</div>
<HEAD>
    <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
</HEAD>
</fmt:bundle>
