/*
 ~  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~  Licensed under the Apache License, Version 2.0 (the "License");
 ~  you may not use this file except in compliance with the License.
 ~  You may obtain a copy of the License at
 ~
 ~        http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~  Unless required by applicable law or agreed to in writing, software
 ~  distributed under the License is distributed on an "AS IS" BASIS,
 ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~  See the License for the specific language governing permissions and
 ~  limitations under the License.
 */

// This contains some utility functions shared by two or more endpoints

// check if a given field is empty
function isEmptyField(id) {
    var elementId = document.getElementById(id);
    if (elementId != null && elementId != undefined) {
        if (elementId.value == "" || elementId.value == null || elementId.value == undefined) {
            return true;
        }
    }
    return false;
}

// check the url is a valid one
function isValidURL(url) {
    if (url.search(/['",]/) != -1) { // we have , ' " in the URL
        return false;
    }
    var regx = RegExp("((fix|jms|http|https|local|ftp|file):/.*)|file:.*|mailto:.*");
    if (!(url.match(regx))) {
        return false;
    }
    return true;
}

function getElementValue(id) {
    var elementValue = document.getElementById(id);
    if (elementValue != null && elementValue != undefined) {
        elementValue = elementValue.value;
    }
    if (elementValue != null && elementValue != undefined) {
        return elementValue;
    }
    return null;
}

// set the action for time out
function activateDurationField(selectNode) {
    var selectOption = selectNode.options[selectNode.selectedIndex].value;
    var actionDuration = document.getElementById('actionDuration');
    if (selectOption != null && selectOption != undefined) {
        if (selectOption == 'neverTimeout') {
            if (actionDuration != null && actionDuration != undefined) {
                actionDuration.disabled = 'disabled';
                actionDuration.value = 0;
            }
        } else {
            if (actionDuration != null && actionDuration != undefined) {
                actionDuration.disabled = '';
            }
        }
    }
}

// a trim function- remove spaces
function trim(stringToTrim) {
    return stringToTrim.replace(/^\s+|\s+$/g, "");
}
function ltrim(stringToTrim) {
    return stringToTrim.replace(/^\s+/, "");
}
function rtrim(stringToTrim) {
    return stringToTrim.replace(/\s+$/, "");
}

function cancelEndpointData(annonOriginator, isFromTemplateEditor) {
    if (annonOriginator != 'null') {
        if (annonOriginator.toString().indexOf('../sequences') != -1) {
            annonOriginator = annonOriginator + '?cancelled=true&region=region1&item=sequences_menu';
        } else if (annonOriginator.toString().indexOf('../proxy') != -1) {
            annonOriginator = annonOriginator + '?cancelled=true&region=region1&item=proxy_services_menu';
        } else {
            annonOriginator = annonOriginator + '?cancelled=true';
        }
        location.href = annonOriginator;
    } else if (isFromTemplateEditor == 'true') {
        location.href = '../templates/list_templates.jsp';
    } else if (annonOriginator == 'null') {
        location.href = '../endpoints/index.jsp?region=region1&item=endpoints_menu&tabs=0';
    }
}

function XMLToString(xmlData) {
    var xmlDoc;
    if (window.ActiveXObject) {
        //for IE
        xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
        xmlDoc.async = "false";
        xmlDoc.loadXML(xmlData);
        return xmlDoc.xml;
    } else if (document.implementation && document.implementation.createDocument) {
        //for Mozila
        parser = new DOMParser();
        xmlDoc = parser.parseFromString(xmlData, "text/xml");
        return (new XMLSerializer()).serializeToString(xmlDoc);
    }
    return null;
}

function xmlToString(xmlObj) {
    if (navigator.appName == "Netscape") {
        var str = new XMLSerializer().serializeToString(xmlObj);
        str = str.replace(' xmlns=""', "");
        return str;
    }
    if (navigator.appName == "Microsoft Internet Explorer") {
        return xmlObj.xml;
    }
    return null;
}

function getText(ele) {
    var strings = [];
    getStrings(ele, strings);
    return strings.join("");
}

function getStrings(n, strings) {
    if (n.nodeType == 3 /* Node.TEXT_NODE */) {
        strings.push(n.data);
    }
    else if (n.nodeType == 1 /* Node.ELEMENT_NODE */) {
        for (var m = n.firstChild; m != null; m = m.nextSibling) {
            getStrings(m, strings);
        }
    }
}

function convertToValidXMlString(originalStr) {
    //Replace all the correct code with invalid code
    var convertedStr = relpaceString(originalStr, "&amp;", "&");
    convertedStr = relpaceString(convertedStr, "&lt;", "<");
    convertedStr = relpaceString(convertedStr, "&gt;", ">");
    convertedStr = relpaceString(convertedStr, "&quot;", '"');

    //Replace all the invalid code with correct code
    convertedStr = relpaceString(convertedStr, "&", "&amp;");
    convertedStr = relpaceString(convertedStr, "<", "&lt;");
    convertedStr = relpaceString(convertedStr, ">", "&gt;");
    convertedStr = relpaceString(convertedStr, '"', "&quot;");
    return convertedStr;
}

function relpaceString(originalStr, originalword, relaceword) {
    if (originalStr == undefined || originalStr == null) {
        return null;
    }
    return originalStr.replace(originalword, relaceword);
}

function testURL(url) {
    if (url == '') {
        CARBON.showWarningDialog(jsi18n['invalid.address.empty']);
    } else {
        jQuery.get("ajaxprocessors/testConnection-ajaxprocessor.jsp?type=address&", {'url' : url},
                   function(data, status) {
                       if (data.replace(/^\s+|\s+$/g, '') == 'success') {
                           CARBON.showInfoDialog(jsi18n['valid.address'] + " " + url);
                       } else if (data.replace(/^\s+|\s+$/g, '') == 'unknown') {
                           CARBON.showErrorDialog(jsi18n['unknown.address'] + " " + url);
                       } else if (data.replace(/^\s+|\s+$/g, '') == 'malformed') {
                           CARBON.showErrorDialog(jsi18n['malformed.address'] + " " + url);
                       } else if (data.replace(/^\s+|\s+$/g, '') == 'ssl_error') {
                           CARBON.showErrorDialog(jsi18n['ssl.error'] + " " + url);
                       } else if (data.replace(/^\s+|\s+$/g, '') == 'unknown_service') {
                           CARBON.showErrorDialog(jsi18n['unknown.service'] + " " + url);
                       } else if (data.replace(/^\s+|\s+$/g, '') == 'unsupported') {
                           CARBON.showErrorDialog(jsi18n['unsupported.protocol']);
                       } else {
                           CARBON.showErrorDialog(jsi18n['invalid.address']);
                       }
                   });
    }
}

function showAdvancedOptions(id) {
    var formElem = document.getElementById(id + '_advancedForm');
    if (formElem.style.display == 'none') {
        formElem.style.display = '';
        document.getElementById(id + '_adv').innerHTML = '<a class="icon-link" ' +
                                                         'onclick="javascript:showAdvancedOptions(\'' + id + '\');" style="background-image: url(images/up.gif);">' + jsi18n['hide.advanced.options'] + '</a>';
    } else {
        formElem.style.display = 'none';
        document.getElementById(id + '_adv').innerHTML = '<a class="icon-link" ' +
                                                         'onclick="javascript:showAdvancedOptions(\'' + id + '\');" style="background-image: url(images/down.gif);">' + jsi18n['show.advanced.options'] + '</a>';
    }
}

function isValidXml(docStr) {
    if (window.ActiveXObject) {
        try {
            var doc = new ActiveXObject("Microsoft.XMLDOM");
            doc.async = "false";
            var hasParse = doc.loadXML(docStr);
            if (!hasParse) {
                CARBON.showErrorDialog('Invalid Configuration');
                return false;
            }
        } catch (e) {
            CARBON.showErrorDialog('Invalid Configuration');
            return false;
        }
    } else {
        var parser = new DOMParser();
        var doc = parser.parseFromString(docStr, "text/xml");
        if (doc.documentElement.nodeName == "parsererror") {
            CARBON.showErrorDialog('Invalid Configuration');
            return false;
        }
    }
    return true;
}

//This function is added to check whether the WSDL URL is valid and return a boolean value
//It is important to make the ajax request synchronous
function isValidWSDLURL(url) {
    var isValid = false;
    jQuery.ajax({
                    url : "ajaxprocessors/testConnection-ajaxprocessor.jsp",
                    data : {'type': 'wsdl', 'url': url},
                    success : function(data, status) {
                        if (data.replace(/^\s+|\s+$/g, '') == 'success') {
                            isValid = true;
                        }
                    },
                    async : false

                });
    return isValid;
}

function showHideOnSelect(selectID, element) {
    if (document.getElementById(selectID).checked == true) {
        document.getElementById(element).style.display = '';
    } else {
        document.getElementById(element).style.display = "none"
    }
}

function showErrorCodeEditor(inputID) {
    var url = 'ajaxprocessors/errorCodeEditor-ajaxprocessor.jsp?codes=' + document.getElementById(inputID).value + "&inputID=" + inputID;
    var loadingContent = "<div id='workArea' style='overflow-x:hidden;'><div id='popupContent'><div class='ajax-loading-message'> <img src='../resources/images/ajax-loader.gif' align='top'/> <span>" + jsi18n["ns.editor.waiting.text"] + "</span> </div></div></div>";
    CARBON.showPopupDialog(loadingContent, jsi18n["errorcode.editor.title"], 420, false, null, 560);

    jQuery("#popupContent").load(url, null, function (responseText, status, XMLHttpRequest) {
        if (status != "success") {
            CARBON.showErrorDialog(jsi18n["errorcode.editor.load.error"]);
        }
    });
}
