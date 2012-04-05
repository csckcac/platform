/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

var xslFilepath  ;
var serverDataSet ;
var proxyServiceDataSet ;
var endPointDataSet ;
var sequenceDataSet ;

var xTicksArrayServerGraph ;
var xTicksArrayProxyServiceGraph ;
var xTicksArrayEndPointGraph ;
var xTicksArraySequenceGraph ;

var showGraphDivHome = false;
var showGraphDivInterval = 0;
var REFRESH_GRAPHS = 70000;
var shouldRefesh = 0;

// Response Times
var graphAvgResponseTimeArrayObj;

function draw(xTicksArray, datasetArray, target) {
    if (xTicksArray == null || datasetArray == null) {
        return;
    }
    var xTicks = xTicksArray.get();
    var dataset = datasetArray.get();
    if (xTicks == null || xTicks.length == 0) {
        return;
    }
    if (dataset == null || dataset.length == null) {
        return;
    }
    var hexColor = MochiKit.Color.Color.fromHexString;
    var options = {
        "IECanvasHTC": "/plotkit/iecanvas.htc",
        "axisLabelColor": hexColor("#666666"),
        "backgroundColor":hexColor("#f5f5f5"),
        "axisLabelFont": "Arial",
        "axisLabelFontSize": 12,
        "axisLabelWidth": 180,
        "colorScheme": PlotKit.Base.palette(hexColor("#a9c5d9")),
        "padding": {left: 2, right: 2, top: 2, bottom: 2},
        "xTicks": xTicks,
        "pieRadius": 0.3,
        "drawYAxis": false
    };
    var layout = new PlotKit.Layout("pie", options);
    layout.addDataset("sqrt", dataset);
    layout.evaluate();
    var canvas = MochiKit.DOM.getElement(target);
    var plotter = new PlotKit.SweetCanvasRenderer(canvas, layout, options);
    plotter.clear();
    plotter.render();
}
function drawGraphs() {
    drawServerGraph();
    drawProxyServiceGraph();
    drawEndPointGraph();
    drawSequencesGraph();
    resetGraphData();
}
function drawServerGraph() {
    draw(xTicksArrayServerGraph, serverDataSet, "serverGraph");
}
function drawProxyServiceGraph() {
    draw(xTicksArrayProxyServiceGraph, proxyServiceDataSet, "proxyServiceGraph");
}
function drawEndPointGraph() {
    draw(xTicksArrayEndPointGraph, endPointDataSet, "endPointGraph");
}
function drawSequencesGraph() {
    draw(xTicksArraySequenceGraph, sequenceDataSet, "sequenceGraph");
}
function fillDataForGraph(xTicksArray, dataSetArray, valueStr) {
    var values = valueStr.split(";");
    for (var i = 0; i < values.length - 1; i++) {
        var aValue = values[i].split(",");
        xTicksArray.add(aValue[0]);
        dataSetArray.add(parseFloat(aValue[1]));
    }
}
function resetGraphData() {
    serverDataSet = new DataSetArray();
    proxyServiceDataSet = new DataSetArray();
    endPointDataSet = new DataSetArray();
    sequenceDataSet = new DataSetArray();

    xTicksArrayServerGraph = new XTicksArray();
    xTicksArrayProxyServiceGraph = new XTicksArray();
    xTicksArrayEndPointGraph = new XTicksArray();
    xTicksArraySequenceGraph = new XTicksArray();
}
function populateAllGraphs(serverStr, psStr, epStr, seqStr) {

    if (serverStr != "") {
        serverDataSet = new DataSetArray();
        xTicksArrayServerGraph = new XTicksArray();
        fillDataForGraph(xTicksArrayServerGraph, serverDataSet, serverStr);
    }
    if (psStr != "") {
        proxyServiceDataSet = new DataSetArray();
        xTicksArrayProxyServiceGraph = new XTicksArray();
        fillDataForGraph(xTicksArrayProxyServiceGraph, proxyServiceDataSet, psStr);
    }
    if (epStr != "") {
        endPointDataSet = new DataSetArray();
        xTicksArrayEndPointGraph = new XTicksArray();
        fillDataForGraph(xTicksArrayEndPointGraph, endPointDataSet, epStr);
    }
    if (seqStr != "") {
        sequenceDataSet = new DataSetArray();
        xTicksArraySequenceGraph = new XTicksArray();
        fillDataForGraph(xTicksArraySequenceGraph, sequenceDataSet, seqStr);
    }
}
function isNumeric(sText) {
    var validChars = "0123456789.";
    var isNumber = true;
    var character;
    for (var i = 0; i < sText.length && isNumber; i++) {
        character = sText.charAt(i);
        if (validChars.indexOf(character) == -1) {
            isNumber = false;
        }
    }
    return isNumber;
}

function initResponseTimeGraph(responseTimeXScale) {
    if (responseTimeXScale < 1 || !isNumeric(responseTimeXScale)) {
        return;
    }
    graphAvgResponseTimeArrayObj = new QueueForGraphs(responseTimeXScale);
}