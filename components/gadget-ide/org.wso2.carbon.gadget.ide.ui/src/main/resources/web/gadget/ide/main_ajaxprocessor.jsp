<html>
<head>
    <link href="css/WireIt.css" rel="stylesheet">
    <link href="css/gadgetide.css" rel="stylesheet">
    <link href="css/UIElements.css" rel="stylesheet">
    <link href="css/datepicker.css" rel="stylesheet">
    <link href="css/combobox.css" rel="stylesheet">
    <link href="css/ui-lightness/jquery-ui-1.8.16.custom.css" rel="stylesheet">
    <link href="css/jqplot.css" rel="stylesheet">
</head>
<body style="">
<div class="gide-head">
    <div class="gide-head-left-1">
        <div class="gide-head-left-n-inner">WSO2</div>
    </div>
    <div class="gide-head-left-2">
        <div class="gide-head-left-n-inner">Gadget IDE</div>
    </div>
</div>
<div style="width:20%;float:left">
    <div id="leftTabBarDiv" class="gide-tab-bar gide-tab-bar-top">
        <div class="gide-tab gide-tab-selected">Units</div>
        <div class="gide-tab">Settings</div>
    </div>
    <div id="itemsDiv"></div>
    <div id="settingsDiv">
        <div id="settingsInnerDiv">
            <div class="gide-settings-panel">
                <h2 id="publishHeader" class="gide-settings-title">Publish</h2>

                <div id="publishContentDiv">
                    <label for="publishNameText" class="gide-settings-label">xml
                        name</label>

                    <div class="gide-settings-input">
                        <input id="publishNameText"/>
                    </div>
                    <div id="publishButtonDiv">
                        <div id="publishButton" class="goog-css3-button">
                            Publish
                        </div>
                    </div>
                    <div class="gide-clear"></div>
                </div>
            </div>

            <div class="gide-settings-panel">
                <h2 id="saveHeader" class="gide-settings-title">Save</h2>
            </div>

            <div class="gide-settings-panel">
                <h2 id="loadHeader" class="gide-settings-title">Load</h2>
            </div>
        </div>
    </div>
</div>
<div style="width:80%;float:left">
    <div id="rightTabBarDiv" class="gide-tab-bar gide-tab-bar-top">
        <div class="gide-tab gide-tab-selected">Data Flow</div>
        <div class="gide-tab">Design</div>
    </div>
    <div id="layerDiv"></div>
    <div id="uiEditDiv">
        <div id="designEditDiv"></div>
        <div id="specEditDiv"></div>
    </div>
</div>

<script src="js/ws/WSRequest.js"></script>
<script src="js/wireit/utilities.js"></script>
<script src="js/wireit/excanvas.js"></script>
<script src="js/wireit/wireit.js"></script>


<script src="js/compiled/gadgetide.js"></script>

<!-- uncomment for debugging
<!--
<script src="uncompiled/lib/closure-library/closure/goog/base.js"></script>
<script>
    goog.require('goog.dom');
    goog.require('gadgetide.pluginlist');
    goog.require('goog.debug.Console');
    goog.require('gadgetide.client.Admin');
    goog.require('gadgetide.ui.Editor');
</script>
<script src="uncompiled/ide.js"></script>
-->

</body>
</html>
