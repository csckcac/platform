<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.net.URL" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
 * Copyright 2006,2007 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
--%>

<%--<link rel="stylesheet" type="text/css" href="../yui/build/grids/grids.css">
<link rel="stylesheet" type="text/css" href="../yui/build/menu/assets/menu.css">
<link rel="stylesheet" type="text/css" href="../yui/build/menu/assets/skins/sam/menu.css">--%>

<link rel="stylesheet" type="text/css" href="js/yui/grids/grids.css">
<link rel="stylesheet" type="text/css" href="js/yui/menu/assets/menu.css">

<style type="text/css">
    .scraper-config-section {
        background-color: #ffffff;
        text-align: center;
        height: 60%;
        border-color: darkgray;
        color: #333333;
        font-family: "Lucida Grande", "Lucida Sans Unicode", sans-serif;
        font-size: 0.9em;
        font-size-adjust: none;
        font-style: normal;
        font-variant: normal;
        font-weight: normal;
        line-height: 150%;
        cursor: default;
        direction: ltr;
    }

    .scraper-config-section1 {
        background-color: #ffffff;
        text-align: center;
        height: 60%;
        border-color: darkgray;
        color: #333333;
        font-family: "Lucida Grande", "Lucida Sans Unicode", sans-serif;
        font-size: 0.9em;
        font-size-adjust: none;
        font-style: normal;
        font-variant: normal;
        font-weight: normal;
        line-height: 150%;
        cursor: default;
        direction: ltr;
    }

    #main-menubar .bd {
        background: url( ../admin/images/table-header.gif ) bottom left repeat-x;
        border: 1px solid #CCCCCC;
        font-weight: normal;
        height: 22px;
        width: auto;
    }

    #main-menubar .bd .bd {
        background-image: none;
        border: none;
        height: auto;
    }

    #main-menubar a:hover {
        color: #FFFFFF;
    }

    #scraper-config {
        height: 400px;
        width: 99.8%;
        border: 1px solid #CCCCCC;
        margin: 0px;
    }

    #page {
        padding: 10px;
        background-color: #FFFFFF;
    }
    
    #page-container {
        border: 1px solid #CCCCCC;
        padding: 10px;
        margin-top: 10px;    	
    }

    /* --------------- scraping assistant drop-down menu styles -------------------- */
    div.yuimenu li.selected, div.yuimenubar li.selected {
        background-color: #B5121B !important;
    }
</style>


<!-- YUI Dependencies-->
<%--<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/container/container_core.js"></script>
<script type="text/javascript" src="../yui/build/menu/menu.js"></script>--%>

<script type="text/javascript" src="js/yui/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="js/yui/container/container_core.js"></script>
<script type="text/javascript" src="js/yui/menu/menu.js"></script>

<!-- XML Parser -->
<script type="text/javascript" src="js/xml-for-script/tinyxmlsax.js"></script>
<script type="text/javascript" src="js/xml-for-script/tinyxmlw3cdom.js"></script>

<!--WSO2 Dependencies-->
<script type="text/javascript">
   var splitUrl =" <%=CarbonUIUtil.getAdminConsoleURL(request).split("/carbon/")[0]%>";
   var path ="/services/";
   var mashupServerURL = splitUrl.trim()+ path;
</script>

<script type="text/javascript" src="js/mashup-main.js"></script>
<script type="text/javascript" src="js/services.js"></script>
<script type="text/javascript" src="js/mashup.js"></script>
<script type="text/javascript" src="js/mashup-utils.js"></script>
<script type="text/javascript" src="js/common.js"></script>
<script type="text/javascript" src="js/scraper.js"></script>
<script type="text/javascript">
    $("#page").ready(function() {
        wso2.mashup.Scraper.init();
    });
</script>

<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.mashup.javascript.scraper.ui.i18n.Resources">

    <carbon:breadcrumb
            label="scraper.headertext"
            resourceBundle="org.wso2.carbon.mashup.javascript.scraper.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
    <div id="middle">
        <h2><fmt:message key="scraper.headertext"/></h2>

        <div id="page">
            <div id="simple-content">

                <!-- Menu Bar -->
                <div id="menu-bar"></div>
                <!-- Scraper Config Geneartion Section -->
                <div id="config-section" class="scraper-config-section"><span
                        class="scraper-config-section1">
      <textarea id="scraper-config"> </textarea>
    </span></div>
                <!-- XPath expression gathering section-->
                <div id="page-container"></div>
            </div>
        </div>
    </div>
</fmt:bundle> 
