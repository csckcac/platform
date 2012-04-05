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

<%@ include file="../includes/head.jsp" %>

<%
    String errorMessage = request.getParameter("error");
%>
<script type="text/javascript" language="javascript" src="../js/ui-utils.js"></script>
<script>

    function showPopup() {

        popup();
        jQuery('#btnCancel').live('click', function () {
            jQuery('#dialog-overlay, #dialog-box').hide();
            return false;
        });
        jQuery('#dialog-overlay').live('click', function () {
            jQuery('#dialog-overlay, #dialog-box').hide();
            return false;
        });


    }
    function loadtabs(cat, thelink) {
        jQuery.ajax({
            data:"category=" + cat,
            url: "tables.jsp",
            success: function(data) {
                jQuery('#tableDiv').html(data);
                jQuery('#loadingDiv').hide();
            }
        });
        jQuery('#loadingDiv').show();
        document.getElementById('catNameShow').innerHTML = cat;
        jQuery('#tabs ul li').removeClass("ui-tabs-selected");
        //thelink.parentNode.className = "ui-tabs-selected";
    }
    jQuery(document).ready(
            function() {
                jQuery("#exportDataForm").validate();
            });
</script>
<style>
    #dialog-box {
        width: 600px;
    }

    .textBoxSpecial {
        width: 150px;
    }

    .dots {
        border: solid 1px #ccc;
        width: 10px;
        height: 10px;
        float: left;
        margin: 5px;
        -moz-border-radius: 4px;
        border-radius: 4px;
        -moz-box-shadow: 3px 3px 3px #888;
        -webkit-box-shadow: 3px 3px 3px #888;
        box-shadow: 3px 3px 3px #888;
    }

    .dots-pane {
        width: 150px;
        margin: 0px 10px;
    }

    .dot0 {
        background-color: #111;
    }

    .dot1 {
        background-color: #444;
    }

    .dot2 {
        background-color: #777;
    }

    .dot3 {
        background-color: #ddd;
    }

    .dot4 {
        background-color: #eee;
    }

    .dot5 {
        background-color: #fff;
    }

    .loading-text {
        padding: 10px;
        font-weight: bold;
        font-size: 16px;
    }
</style>


<img src="../images/logo.png" alt="Logo"/>

<div class="logoutButton">
    <a href="../otauth/logout.jsp">Log out</a>
</div>


<form action="../GoogleServiceProviderServlet" method="post" id="exportDataForm"
      name="exportDataForm" onsubmit="checkAllExport()">
    <div id="dialog-box">
        <div class="dialog-content">
            <div id="dialog-message">
                <h2 class="mainTitle" style="margin-top:0px;">Export To Google Spread Sheet</h2>
                <table style="width:700px;">
                    <tr>
                        <td class="textBoxSpecial">Google Username :</td>
                        <td class="valueBox"><input id="g_username" type="text" name="g_username"
                                                    class="required"/></td>
                    </tr>
                    <tr>
                        <td class="textBoxSpecial">Google Password :</td>
                        <td class="valueBox"><input id="g_password" type="password"
                                                    name="g_password" class="required"/></td>
                    </tr>
                    <tr>
                        <td class="textBoxSpecial">Google SpreadSheet Name :</td>
                        <td class="valueBox"><input id="title" type="text" name="title"
                                                    class="required"/></td>
                    </tr>
                    <tr>
                        <td></td>
                        <td><input type="checkbox" name="cbExportAll" id="cbExportAll"/><label
                                for="cbExportAll"> Export All</label></td>
                    </tr>
                    <tr>
                        <td></td>
                        <td>
                            <input id="btnExport" class="button" name="btnExport" type="submit"
                                   value="Export to Google"/>
                            <input id="btnCancel" class="button" type="button" value="Cancel"/>
                        </td>
                    </tr>
                   <input id="all" name="all" type="hidden"/>
                </table>

                <table>
                    <tr>
                        <td>
                            <%if (errorMessage != null) {%>
                            <font color="red">Error creating Google spreadsheet - <%=errorMessage%>
                            </font>
                            <script type="text/javascript" language="javascript">
                                jQuery(document).ready(
                                        function() {
                                            showPopup();
                                        }
                                        );
                            </script>
                            <%} else {%>
                            &nbsp;
                            <%}%>
                        </td>
                    </tr>
                </table>

            </div>
        </div>
    </div>
    <div id="tabs">
        <ul>
            <li>
                <a href="javascript:loadtabs('Leads',this)">Leads</a>
            </li>
            <li>
                <a href="javascript:loadtabs('Contacts',this)">Contacts</a>
            </li>
            <li>
                <a href="javascript:loadtabs('Opportunities',this)">Opportunities</a>
            </li>
            <li>
                <a href="javascript:loadtabs('Cases',this)">Cases</a>
            </li>
        </ul>
    </div>
    <div style="clear:both"></div>
    <div class="loginBox overrideClass" style="_margin-top:-20px;*margin-top:-20px;">
        <div id="loadingDiv" style="display:none">
            <table style="width:500px;">
                <tr>
                    <td>
                        <div class="loading-text">Loading <span id="catNameShow"></span>....</div>
                    </td>
                    <td>
                        <div class="dots-pane" id="dotsContainer" style="width:200px;">
                            <div class="dots"></div>
                            <div class="dots"></div>
                            <div class="dots"></div>
                            <div class="dots"></div>
                            <div class="dots"></div>
                            <div class="dots"></div>
                        </div>
                    </td>
                </tr>
            </table>


        </div>
        <div id="tableDiv">
            <%@ include file="tables.jsp" %>
        </div>

    </div>
</form>
<script>
    var t;
    var timer_is_on = 0;
    var j = 0;
    var dotsContainer = document.getElementById('dotsContainer');
    var dots = dotsContainer.childNodes;
    var divdots = new Array();
    for (var i = 0; i < dots.length; i++) {
        if (dots[i].nodeName == "DIV") {
            divdots.push(dots[i]);
        }
    }
    function animateStuff() {
        for (var i = 0; i < divdots.length; i++) {
            var classNumber;
            if ((i + j) < divdots.length) {
                classNumber = i + j;
            } else {
                classNumber = i + j - divdots.length;
            }
            divdots[i].className = "dots dot" + classNumber;

        }
        if (j <= 5) {
            j++;
        } else {
            j = 0;
        }
        t = setTimeout(animateStuff, 200);
    }

    if (!timer_is_on) {
        animateStuff();
    }
</script>

