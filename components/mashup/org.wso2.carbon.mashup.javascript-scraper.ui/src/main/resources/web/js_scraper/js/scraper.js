/*
 * Copyright 2007 WSO2, Inc. http://www.wso2.org
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
 */

var scraperConfig;

var scraperConfigTextArea;

var xpathExpressionCount = 0;

var urlCount = 0;

var currentUrlVariable = "";

var variableCount = 0;

var parameterCount = 0;

var formatxslt = null;

if (typeof wso2 == "undefined") {
    /**
     * The WSO2 global namespace object.  If WSO2 is already defined, the
     * existing WSO2 object will not be overwritten so that defined
     * namespaces are preserved.
     * @class WSO2
     * @static
     */
    var wso2 = {};
}

wso2.mashup.Scraper = function() {

};

wso2.mashup.Scraper.init = function () {
    wso2.mashup.init(false);
    scraperConfigTextArea = document.getElementById("scraper-config");

    $.get('../tryit/xslt/prettyprinter.xslt', null, function(data) {
        formatxslt = data;
    }, "xml");

    var xml = "<config></config>";
    scraperConfigTextArea.value = xml;
    wso2.mashup.Scraper.setCursorTo(scraperConfigTextArea, 8, 8);
    //instantiate the W3C DOM Parser
    var parser = new DOMImplementation();

    //load the XML into the parser and get the DOMDocument
    scraperConfig = parser.loadXML(xml);

    var fetchUrlMenuItems = [
        { text: "Dhtml Rendered Html", onclick : { fn: wso2.mashup.Scraper.getHTML, obj: "true" } },
        { text: "Basic Html", onclick: { fn: wso2.mashup.Scraper.getHTML, obj: "false" } }
    ];
    var xpathMenuItems = [
        { text: "Fetch URL to capture Xpath expressions", submenu: { id: "fetchUrl-menu", itemdata: fetchUrlMenuItems } },
        { text: "Add Regular Expression at cursor", onclick: { fn: wso2.mashup.Scraper.addAtCursor, obj: "regexp" } },
        { text: "Add XQuery Expression at cursor", onclick: { fn: wso2.mashup.Scraper.addAtCursor, obj: "xquery" } }
    ];

    var configMenuItems = [
        { text: "New Scraper Configuration", onclick: { fn: wso2.mashup.Scraper.launchUrl, obj: "index.jsp" } },
        { text: "Add HTTP request", onclick: { fn: wso2.mashup.Scraper.addHttpRequest } },
        { text: "Add HTTP request parameter at cursor", onclick: { fn: wso2.mashup.Scraper.addAtCursor, obj: "http-param" } },
        { text: "Convert HTML to XML", onclick: { fn: wso2.mashup.Scraper.SurroundWith, obj: "html-to-xml" } },
        { text: "Convert to variable", onclick: { fn: wso2.mashup.Scraper.SurroundWith, obj: "var-def" } },
        { text: "XPressions", submenu: {  id: "xpath-menu", itemdata: xpathMenuItems } },
        { text: "Add Variable at cursor", onclick: { fn: wso2.mashup.Scraper.addAtCursor, obj: "var" } },
        { text: "Add XSLT Transformation at cursor", onclick: { fn: wso2.mashup.Scraper.addAtCursor, obj: "xslt" } },
        { text: "Add CASE Statement at cursor", onclick: { fn: wso2.mashup.Scraper.addAtCursor, obj: "case" } },
        { text: "Add LOOP Statement at cursor", onclick: { fn: wso2.mashup.Scraper.addAtCursor, obj: "loop" } },
        { text: "Add Function Call Statement at cursor", onclick: { fn: wso2.mashup.Scraper.addAtCursor, obj: "function-call" } },
        { text: "Surround with a WHILE Statement", onclick: { fn: wso2.mashup.Scraper.SurroundWith, obj: "while" } },
        { text: "Surround to form a Function", onclick: { fn: wso2.mashup.Scraper.SurroundWith, obj: "function" } },
        { text: "Surround with TRY/CATCH", onclick: { fn: wso2.mashup.Scraper.SurroundWith, obj: "try" } },
        /*    { text: "Surround with &lt;empty&gt;&lt;/empty&gt;", onclick: { fn: wso2.mashup.Scraper.SurroundWith, obj: "empty" } },
         { text: "Surround with &lt;text&gt;&lt;/text&gt;", onclick: { fn: wso2.mashup.Scraper.SurroundWith, obj: "text" } },
         { text: "Surround with &lt;file&gt;&lt;/file&gt;", onclick: { fn: wso2.mashup.Scraper.SurroundWith, obj: "file" } },*/
        { text: "Surround with <empty></empty>", onclick: { fn: wso2.mashup.Scraper.SurroundWith, obj: "empty" } },
        { text: "Surround with <text></text>", onclick: { fn: wso2.mashup.Scraper.SurroundWith, obj: "text" } },
        { text: "Surround with <file></file>", onclick: { fn: wso2.mashup.Scraper.SurroundWith, obj: "file" } },
        { text: "Format XML", onclick: { fn: wso2.mashup.Scraper.formatXML } }
    ];

    /*var rightClickMenuItems = [
     { text: "Add HTTP request", onclick: { fn: wso2.mashup.Scraper.addHttpRequest } },
     { text: "Add HTTP request parameter", onclick: { fn: wso2.mashup.Scraper.addHttpParam } },
     { text: "Convert HTML to XML", onclick: { fn: wso2.mashup.Scraper.SurroundWith, obj: "html-to-xml" } },
     { text: "Convert to variable", onclick: { fn: wso2.mashup.Scraper.SurroundWith, obj: "var-def" } },
     { text: "Surround with <empty></empty>", onclick: { fn: wso2.mashup.Scraper.SurroundWith, obj: "empty" } },
     { text: "Surround with <text></text>", onclick: { fn: wso2.mashup.Scraper.SurroundWith, obj: "text" } },
     { text: "Surround with <file></file>", onclick: { fn: wso2.mashup.Scraper.SurroundWith, obj: "file" } },
     { text: "Format XML", onclick: { fn: wso2.mashup.Scraper.formatXML } }
     ];*/

    /* var toolsMenuItems = [
     { text: "Copy configuration to Clipboard", onclick: {  } },
     { text: "Select All", onclick: {  } },
     { text: "Copy", onclick: { fn: wso2.mashup.Scraper.copySelectedText  } },
     { text: "Paste", onclick: {  } },
     ];*/

    var helpMenuItems = [
        { text: "Web Harvest Manual", onclick: { fn: wso2.mashup.Scraper.launchUrl, obj: "http://web-harvest.sourceforge.net/manual.php" }  }
    ];

    //Adding the context menu for the the config text area
    /*var configContextMenu = new YAHOO.widget.ContextMenu("config-contextmenu", { trigger: scraperConfigTextArea, itemdata: rightClickMenuItems});
     configContextMenu.render('config-section');*/

    //Adding the Top Menu Bar
    var menuSet = [
        { text: "Scraper Configuration", submenu: {  id: "config-menu", itemdata: configMenuItems}},
        /*{ text: "Tools", submenu: {  id: "tools-menu", itemdata: toolsMenuItems}},*/
        { text: "Help", submenu: {  id: "help-menu", itemdata: helpMenuItems }}
    ];

    var topMenuBar = new YAHOO.widget.MenuBar("main-menubar", { lazyload: true, itemdata: menuSet });
    topMenuBar.render('menu-bar');
}

wso2.mashup.Scraper.listOfScrapes = new Array();

wso2.mashup.Scraper.currURL = null;

wso2.mashup.Scraper.getHTML = function(p_sType, p_aArgs, renderDHTML) {


    wso2.mashup.Scraper.showInputUrlDialog("Enter a URL to fetch", "http://www.google.com", viewHtml);

    function viewHtml(url) {
        if (WSO2.MashupUtils.isUrl(url)) {
            wso2.mashup.Scraper.currURL = url;

            var scpr_cont = document.getElementById('page-container');

            //Adding html-xml tag with the given url
            currentUrlVariable = "page_" + ++urlCount;

            function DisableEnableLinks(xHow) {
                //Disabling all links inside the fetched html page
                var objLinks = scpr_cont.getElementsByTagName("a");
                for (var i = 0; i < objLinks.length; i++) {
                    objLinks[i].disabled = xHow;
                    //link with onclick
                    if (objLinks[i].onclick && xHow) {
                        objLinks[i].onclick = function() {
                            return false;
                        };
                    }
                    //link without onclick
                    else if (xHow) {
                        objLinks[i].href = "#";
                    }
                }

                //Making img urls absolute
                var imgs = scpr_cont.getElementsByTagName("img");
                var index = url.indexOf('/', url.indexOf('://') + 3);
                var host = url;
                var webContext = "";
                if (index != -1) {
                    host = url.substring(0, index);
                    webContext = url.substring(host.length + 1, url.lastIndexOf('/'));

                }
                for (var i = 0; i < imgs.length; i++) {
                    var src = imgs[i].getAttribute('src');
                    if (src.indexOf('/') == 0) {
                        imgs[i].src = host + src;
                    } else if ((src.indexOf('http://') != 0) && (src.indexOf('https://') != 0)) {
                        imgs[i].src = host + webContext + '/' + src;
                    }
                }
                //Disabling all submit buttons
                var inputs = document.getElementsByTagName("input");
                for (i = 0; i < inputs.length; i++) {
                    if (inputs[i].type == "submit") {
                        var oldElement = inputs[i];

                        var newElement = document.createElement('input');
                        newElement.type = "button";
                        if (oldElement.name) {
                            newElement.name = oldElement.name;
                        }
                        if (oldElement.id) {
                            newElement.id = oldElement.id;
                        }
                        if (oldElement.value) {
                            newElement.value = oldElement.value;
                        }
                        if (oldElement.className) {
                            newElement.className = oldElement.className;
                        }
                        if (oldElement.size) {
                            newElement.size = oldElement.size;
                        }
                        if (oldElement.tabIndex) {
                            newElement.tabIndex = oldElement.tabIndex;
                        }
                        if (oldElement.accessKey) {
                            newElement.accessKey = oldElement.accessKey;
                        }

                        //Replacing the old submit with the new one
                        oldElement.parentNode.replaceChild(newElement, oldElement);
                    }
                }
            }

            function getUrlContentsCallback() {
                scpr_cont.innerHTML =
                WSO2.MashupUtils.removeCdata(this.req.responseXML.getElementsByTagName("response")[0].
                                                     firstChild.nodeValue);

                YAHOO.util.Event.onContentReady('page-container', function() {
                    DisableEnableLinks(true);
                    wso2.mashup.Scraper.addEvents();
                });
            }


            wso2.mashup.services.getUrlContents(url, renderDHTML, navigator.userAgent, getUrlContentsCallback);

            scpr_cont.innerHTML =
            "<div style='margin-left:50px; height:50%'><img src='images/loading.gif' align='center'> Loading page ...</div>";

        } else {
            CARBON.showErrorDialog("The entered URL is invalid");
        }
    }

};


wso2.mashup.Scraper.addEvents = function() {
    var nodes = document.getElementById('page-container').childNodes;

    for (var i = 0; i < nodes.length; i++) {
        var node = nodes[i];
        YAHOO.util.Event.addListener(node, "mouseover", wso2.mashup.Scraper.borderHighlight);
        YAHOO.util.Event.addListener(node, "mouseout", wso2.mashup.Scraper.resetBorderHighlight);
        YAHOO.util.Event.addListener(node, "click", wso2.mashup.Scraper.elementClicked);

    }
};

wso2.mashup.Scraper.captureNodeXPath = function(node) {
    var xpath = "";
    var namespace = node.ownerDocument.documentElement.namespaceURI;
    var prefix = namespace ? "x:" : "";
    var node2 = node;

    while (node2 && (!(node2.id == "page-container"))) {
        var tag = node2.tagName.toLowerCase();
        var id = node2.id;
        var className = node2.className;

        var segment = prefix + tag;
        if (tag == "tr") {
            var rowCount = node2.parentNode.rows.length;
            if (rowCount > 1 && rowCount < 5) {
                segment += '[' + (node2.rowIndex + 1) + ']';
            }
        } else if (tag == "td") {
            var cellCount = node2.parentNode.cells.length;
            if (cellCount > 1 && cellCount < 5) {
                segment += '[' + (node2.cellIndex + 1) + ']';
            }
        }

        if (className && className != "") {
            var classes = className.split(" ");
            var newClasses = [];
            for (var c = 0; c < classes.length; c++) {
                var cl = classes[c];
                if (cl.indexOf("simile-solvent-") < 0) {
                    newClasses.push(cl);
                }
            }
            if (newClasses.length > 0) {
                segment += '[@class=\'' + newClasses.join(" ") + '\']';
            }
        }

        if (id && id != "") {
            xpath = "//" + segment + '[@id=\'' + id + '\']' + xpath;
            break;
        }

        xpath = "/" + segment + xpath;

        node2 = node2.parentNode;
    }
    return xpath;
};

wso2.mashup.Scraper.borderHighlight = function(e) {
    //e.preventDefault();
    var obj;
    if (e.target) {
        obj = e.target;
        obj.style.border = ".03em solid red";
    } else if (e.srcElement) {
        obj = e.srcElement;
        obj.style.border = ".03em solid red";
    }

};

wso2.mashup.Scraper.resetBorderHighlight = function(e) {
    //e.preventDefault();
    var obj;
    if (e.target) {
        obj = e.target;
        obj.style.border = "";
    } else if (e.srcElement) {
        obj = e.srcElement;
        obj.style.border = "";
    }

};

wso2.mashup.Scraper.elementClicked = function(e) {
    //Generating the xpath of the clicked element
    var obj = null;

    try {

        if (scraperConfigTextArea.value == "") {
            CARBON.showErrorDialog("Please create a Scraper Configuration before executing this option.");
            return;
        }

        if (e.target) {
            obj = e.target;
        } else if (e.srcElement) {
            obj = e.srcElement;
        }

        if (!(obj == null)) {
            var xpath = wso2.mashup.Scraper.captureNodeXPath(obj);

            //Adding the xpath expression to the scraper config and updating the text area.
            var parser = new DOMImplementation();
            scraperConfig = parser.loadXML(scraperConfigTextArea.value);

            var xpathExpressionXml = scraperConfig.createDocumentFragment();

            var xpathExpressionElement = scraperConfig.createElement("xpath");
            var xpathExpressionAttribute_1 = scraperConfig.createAttribute("expression");
            xpathExpressionAttribute_1.setNodeValue(xpath);
            xpathExpressionElement.setAttributeNode(xpathExpressionAttribute_1);

            var variableElement = scraperConfig.createElement("var");
            var variableElementAttribute_1 = scraperConfig.createAttribute("name");
            variableElementAttribute_1.setNodeValue(currentUrlVariable);
            variableElement.setAttributeNode(variableElementAttribute_1);
            xpathExpressionElement.appendChild(variableElement);

            xpathExpressionXml.appendChild(xpathExpressionElement);
            scraperConfig.documentElement.appendChild(xpathExpressionXml);
            wso2.mashup.Scraper.setCursorTo(scraperConfigTextArea, 8, 8);
            scraperConfigTextArea.value = scraperConfig.getXML();

        }

        //Calling the XML Formatter
        wso2.mashup.Scraper.formatXML();
    } catch(ex) {
        CARBON.showErrorDialog("Failed to add the requested segment to the configuration.")
    }
};

wso2.mashup.Scraper.createSElement = function(xpath, url) {
    var tempScrape = new Object;
    tempScrape.scrapeXPath = xpath;
    tempScrape.scrapeURL = url;
    tempScrape.getScrapeXPath = function() {
        return this.scrapeXPath;
    }
    tempScrape.getScrapeURL = function() {
        return this.scrapeURL;
    }

    return tempScrape;
};


wso2.mashup.Scraper.getSelectedText = function () {
    return (scraperConfigTextArea.value).substring(scraperConfigTextArea.selectionStart,
                                                   scraperConfigTextArea.selectionEnd);
};

wso2.mashup.Scraper.SurroundWith = function (p_sType, p_aArgs, tag) {

    var before = "";
    var after = "";
    var selText = "";

    try {

        if (scraperConfigTextArea.value == "") {
            CARBON.showErrorDialog("Please create a Scraper Configuration before executing this option.");
            return;
        }

        if (scraperConfigTextArea.selectionStart ||
            (scraperConfigTextArea.selectionStart == '0')) {
            before = scraperConfigTextArea.value.substring(0, scraperConfigTextArea.selectionStart);
            after =
            scraperConfigTextArea.value.substring(scraperConfigTextArea.selectionEnd, scraperConfigTextArea.value.length);
            selText =
            scraperConfigTextArea.value.substring(scraperConfigTextArea.selectionStart, scraperConfigTextArea.selectionEnd);
        } else {
            //Fallback method for IE
            if (window.getSelection) {
                selText = window.getSelection();
            } else if (document.getSelection) {
                selText = document.getSelection();
            } else if (document.selection) {
                selText = document.selection.createRange().text;
            }

            var startPos = scraperConfigTextArea.value.indexOf(selText);

            if (startPos != 0) {
                var endPos = scraperConfigTextArea.value.indexOf(selText) + selText.length;
                before = scraperConfigTextArea.value.substr(0, startPos);
                if (before == "undefined") {
                    before = "";
                }

                after =
                scraperConfigTextArea.value.substr(endPos, scraperConfigTextArea.value.length);

                if (after == "undefined") {
                    after = "";
                }
            }
        }

        if (selText == "") {
            CARBON.showErrorDialog("You need to select a portion of the code to surround first.");
            return;
        }

        if (tag == "empty") {
            selText = "<empty>" + selText + "</empty>";
        } else if (tag == "text") {
            selText = "<text>" + selText + "</text>";
        } else if (tag == "file") {
            selText =
            '<file action="file_action" path="file_path" type="file_type" charset="UTF-8">' +
            selText + "</file>";
        } else if (tag == "var-def") {
            selText = '<var-def name="variable_' + ++variableCount + '">' + selText + "</var-def>";
        } else if (tag == 'html-to-xml') {
            selText = '<html-to-xml outputtype="pretty">' + selText + "</html-to-xml>";
        } else if (tag == "while") {
            selText =
            '<while condition="expression" index="index_var_name" maxloops="max_loops">' + selText +
            '</while>';
        } else if (tag == "function") {
            selText = '<function name="function_name">' + selText + '</function>';
        } else if (tag == "try") {
            selText = '<try><body>' + selText + '</body><catch> catch body </catch></try>';
        }

        scraperConfigTextArea.value = before + selText + after;

        //Calling the XML Formatter
        wso2.mashup.Scraper.formatXML();

    } catch(ex) {
        CARBON.showErrorDialog("Failed to add the requested segment to the configuration.")
    }

};

wso2.mashup.Scraper.addAtCursor = function (p_sType, p_aArgs, tag) {
    try {

        if (scraperConfigTextArea.value == "") {
            CARBON.showErrorDialog("Please create a Scraper Configuration before executing this option.");
            return;
        }

        var insertionText = "";

        if (tag == "var") {
            insertionText = '<var name="variable_name"/>';
        } else if (tag == "regexp") {
            insertionText =
            '<regexp replace="true_or_false" max="max_found_occurrences"><regexp-pattern> body as pattern value </regexp-pattern><regexp-source> body as the text source </regexp-source>[<regexp-result> body as the result </regexp-result>]</regexp>';
        } else if (tag == "xquery") {
            insertionText =
            '<xquery><xq-expression> body as xquery language construct </xq-expression></xquery>';
        } else if (tag == "xslt") {
            insertionText =
            '<xslt><xml> body as xml </xml><stylesheet> body as xsl </stylesheet></xslt>';
        } else if (tag == "case") {
            insertionText =
            '<case>[<if condition="expression"> if body </if>] * [<else> else body </else>]</case>';
        } else if (tag == "loop") {
            insertionText =
            '<loop item="item_var_name" index="index_var_name" maxloops="max_loops" filter="list_filter"><list> body as list value </list><body> body for each list item </body></loop>';
        } else if (tag == "function-call") {
            insertionText =
            '<call name="function_name">[<call-param name="function_name">body as actual parameter value</call-param>] *</call>';
        } else if (tag == "http-param") {
            insertionText = '<http-param name="param_' + ++parameterCount + '"></http-param>';
        }

        var before = "";
        var after = "";
        var startPos = 0;

        if (scraperConfigTextArea.selectionStart ||
            (scraperConfigTextArea.selectionStart == '0')) {
            before = scraperConfigTextArea.value.substring(0, scraperConfigTextArea.selectionStart);
            after =
            scraperConfigTextArea.value.substring(scraperConfigTextArea.selectionEnd, scraperConfigTextArea.value.length);
            scraperConfigTextArea.value = before + insertionText + after;
        } else {
            //Fallback method for IE
            scraperConfigTextArea.focus();

            if (window.getSelection) {
                startPos = window.getSelection();
            } else if (document.getSelection) {
                startPos = document.getSelection();
            } else if (document.selection) {
                startPos = document.selection.createRange();
            }

            startPos.text = insertionText;
        }

        //Calling the XML Formatter
        wso2.mashup.Scraper.formatXML();

    } catch(ex) {
        CARBON.showErrorDialog("Failed to add the requested segment to the configuration.")
    }
};

wso2.mashup.Scraper.addHttpRequest = function () {

    try {

        if (scraperConfigTextArea.value == "") {
            CARBON.showErrorDialog("Please create a Scraper Configuration before executing this option.");
            return;
        }

        var parser = new DOMImplementation();

        scraperConfig = parser.loadXML(scraperConfigTextArea.value);

        var httpRequestXml = scraperConfig.createDocumentFragment();

        var httpRequestElement = scraperConfig.createElement("http");

        var httpRequestAttribute_1 = scraperConfig.createAttribute("url");
        httpRequestAttribute_1.setNodeValue("url-to-fetch");
        httpRequestElement.setAttributeNode(httpRequestAttribute_1);

        var httpRequestAttribute_2 = scraperConfig.createAttribute("method");
        httpRequestAttribute_2.setNodeValue("post");
        httpRequestElement.setAttributeNode(httpRequestAttribute_2);

        httpRequestXml.appendChild(httpRequestElement);

        scraperConfig.documentElement.appendChild(httpRequestXml);

        scraperConfigTextArea.value = scraperConfig.getXML();

        //Calling the XML Formatter
        wso2.mashup.Scraper.formatXML();

    } catch(ex) {
        CARBON.showErrorDialog("Failed to add the requested segment to the configuration.")
    }
};

wso2.mashup.Scraper.formatXML = function () {
    try {
        var parser = new DOMImplementation();
        scraperConfig = parser.loadXML(scraperConfigTextArea.value);
        var browser = WSRequest.util._getBrowser();
        if (browser == "ie" || browser == "ie7") {

            var XMLdoc = new ActiveXObject("Microsoft.XMLDOM");
            XMLdoc.loadXML(scraperConfig.documentElement.getXML());
            XMLdoc.async = false;

            var xsl = new ActiveXObject("Microsoft.XMLDOM");
            xsl.async = false;
            xsl.load("xslt/XMLFormatter.xsl");

            var outxml = new ActiveXObject("Microsoft.XMLDOM");
            outxml.async = false;

            XMLdoc.transformNodeToObject(xsl, outxml);
            scraperConfigTextArea.value = outxml.xml;

        } else if (browser == "gecko") {
            //Using the E4X support to format the xml
            scraperConfigTextArea.value = new XML(scraperConfig.getXML());

        } else if (browser == "safari") {
            scraperConfigTextArea.value = wso2.mashup.Scraper.prettyPrintXML(scraperConfig.getXML());
        } else {
            CARBON.showErrorDialog("Sorry. XML formatting is not implemented for this browser.")
        }
    } catch(ex) {
        CARBON.showErrorDialog("The Scraper Configuration failed XML format validation. Please check the latest changes.")
    }


};

wso2.mashup.Scraper.launchUrl = function (p_sType, p_aArgs, url) {
    window.open(url);
};

wso2.mashup.Scraper.showInputUrlDialog = function(message, input, handleNext) {
    var strInput = "<div style='margin:20px;'><p><b>" + message + "</b></p><br/>" +
                   "<input type='text' id='carbon-ui-dialog-input' size='40' value='" + input + "'></div>";
    var strDialog = "<div id='dialog' title='WSO2 Carbon'>" + strInput + "</div>";

    jQuery("#dcontainer").html(strDialog);
    jQuery("#dialog").dialog({
                                 close:function() {
                                     jQuery(this).dialog('destroy').remove();
                                     jQuery("#dcontainer").empty();
                                     return false;
                                 },
                                 buttons:{
                                     "OK":function() {
                                         var returnVal = document.getElementById("carbon-ui-dialog-input").value;
                                         handleNext(returnVal);
                                         jQuery(this).dialog("destroy").remove();
                                         jQuery("#dcontainer").empty();
                                         return false;
                                     },
                                     "Cancel":function() {
                                         jQuery(this).dialog("destroy").remove();
                                         jQuery("#dcontainer").empty();
                                         return false;

                                     }
                                 },
                                 height:160,
                                 width:450,
                                 minHeight:160,
                                 minWidth:330,
                                 modal:true
                             });
};

wso2.mashup.Scraper.prettyPrintXML = function (doc) {
    if (!doc) {
        return null;
    }
    var output;
    if (typeof doc === "string") {
        if (!doc) {
            return null;
        }
        doc = WSRequest.util.xml2DOM(doc);
    }
    var oProcessor = new XSLTProcessor();
    oProcessor.importStylesheet(formatxslt);
    output = oProcessor.transformToDocument(doc);

    return $.trim(WSRequest.util._serializeToString(output));
};
wso2.mashup.Scraper.setCursorTo = function (el, st, end) {

    if (el.setSelectionRange) {
        el.focus();
        el.setSelectionRange(st, end);
    }
    else {
        if (el.createTextRange) {
            range = el.createTextRange();
            range.collapse(true);
            range.moveEnd('character', end);
            range.moveStart('character', st);
            range.select();
        }
    }


};
