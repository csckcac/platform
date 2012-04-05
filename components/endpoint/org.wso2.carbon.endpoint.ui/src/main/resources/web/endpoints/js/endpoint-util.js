/*
 ~  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function isValidProperties(propstr) {
    if (propstr != null && propstr != '') {
	if (propstr.indexOf(',,') != -1 || propstr.indexOf(',') == 0) {
	    return false;
	}
    }
    return true;
}

// check the url is a valid one
function isValidURL(url) {
    if (url.search(/['",]/) != -1) { // we have , ' " in the URL
        return false;
    }
    var regx = RegExp("(^[0-9A-Za-z][0-9A-Za-z]*(:/|:)[^:].*)");
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

function cancelEndpointData(annonOriginator,isFromTemplateEditor) {
    if (annonOriginator != 'null') {
        document.location.href = annonOriginator + '?cancelled=true';
    } else if (isFromTemplateEditor == 'true') {
        document.location.href = '../templates/list_templates.jsp';
    } else if (annonOriginator == 'null') {
        document.location.href = 'index.jsp';
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
    if (navigator.appName == "Netscape")
    {
        var str = new XMLSerializer().serializeToString(xmlObj);
        str = str.replace(' xmlns=""',"");
        return str;
    }
    if (navigator.appName == "Microsoft Internet Explorer")
    {
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
    if (n.nodeType == 3 /* Node.TEXT_NODE */)
        strings.push(n.data);
    else if (n.nodeType == 1 /* Node.ELEMENT_NODE */) {
        for (var m = n.firstChild; m != null; m = m.nextSibling) {
            getStrings(m, strings);
        }
    }
}

//checks property defined parameter defined in other location with  '$' sign
function isParamFound(param){
	var propertyCount = document.getElementById("propertyCount");
	if(propertyCount == null){
		CARBON.showErrorDialog(param+" "+ jsi18n['uri.endpoint.param.not.defined']);
		return false;
	}
	var i = propertyCount.value;
	var currentCount = parseInt(i);
	for (i = 0; i < currentCount; i++) {
        var paramElem = document.getElementById("propertyName" + i);
        if (paramElem != null) {
            var paramName = "$"+paramElem.value;
            if(paramName == param){
                 return true;
            }
             
        }
    }
	
	CARBON.showErrorDialog(param+" "+ jsi18n['uri.endpoint.param.not.defined']);
	return false;
}


// validate an Addresss or WSDL data entered
function isValidAddressORWSDLEndpoint(isAnonymous, endpointType) {

    if (isAnonymous == 'false') {
        if (isEmptyField('endpointName')) {
            CARBON.showWarningDialog(jsi18n['name.field.cannot.be.empty']);
            return false;
        }
    }

    if (endpointType == 'address') {
    	    	   	
        if (isEmptyField('address')) {
            CARBON.showWarningDialog(jsi18n['address.field.cannot.be.empty']);
            return false;
        }

        // check for a valid URL
        var endpointURI = getElementValue('address');
        if (endpointURI != null) {
        	
        	if(endpointURI.indexOf('$')==0){
        		return isParamFound(endpointURI);
        	}
        	
            if (!isValidURL(endpointURI)) {
                CARBON.showWarningDialog(jsi18n['invalid.url.provided']);
                return false;
            }
        }

    } else if (endpointType == 'wsdl') {
        var inlineWSDL = document.getElementById('inlineWSDL');
        if (inlineWSDL != null && inlineWSDL != undefined) {
            if (inlineWSDL.checked) {
                // not empty
                var inlineWSDLVal = document.getElementById('inlineWSDLVal').value;
                if (inlineWSDLVal != null && inlineWSDLVal != undefined) {
                    // is empty ?
                    if (trim(inlineWSDLVal) == '' || ltrim(inlineWSDLVal) == ''
                            || rtrim(inlineWSDLVal) == '') {
                        CARBON.showWarningDialog(jsi18n['inline.wsdl.cannot.be.empty']);
                        return false;
                    }
                    // is a valid xml doc ?
                    // the WSDL should not contain the xml delecraion
                    if (!isValidWSDL(inlineWSDLVal)) {
                        return false;
                    }
                }
            }
        }
        var uri = document.getElementById('uriWSDL');
        if (uri != null && uri != undefined) {
            if (uri.checked) {
                if (isEmptyField('uriWSDLVal')) {
                    CARBON.showWarningDialog(jsi18n['wsdl.uri.field.cannot.be.empty']);
                    return false;
                }

                var wsdluri = trim(document.getElementById('uriWSDLVal').value);
                if(!isValidWSDLURL(wsdluri)){
                    CARBON.showWarningDialog(jsi18n['invalid.address.cannot.proceed']);
                    return false;
                }

            }
        }

        if (isEmptyField('wsdlendpointService')) {
            CARBON.showWarningDialog(jsi18n['service.field.cannot.be.empty']);
            return false;
        }

        if (isEmptyField('wsdlendpointPort')) {
            CARBON.showWarningDialog(jsi18n['port.field.cannot.be.empty']);
            return false;
        }
    }

    /*var errorVal = getElementValue('suspendErrorCode');
     if (errorVal != null) {
     if (isNaN(errorVal)) {
     CARBON.showWarningDialog(jsi18n['please.enter.a.numeric.value.to.the.error.code']);
     return false;
     }
     } */

    var durationVal = getElementValue('suspendDuration');
    if (durationVal != null) {
        if (isNaN(durationVal)) {
            CARBON.showWarningDialog(jsi18n['please.enter.a.numeric.value.to.the.suspend.duration.seconds.field']);
            return false;
        }
    }

    var maxDurationVal = getElementValue('suspendMaxDuration');
    if (maxDurationVal != null) {
        if (isNaN(maxDurationVal)) {
            CARBON.showWarningDialog(jsi18n['please.enter.a.valid.number.to.the.max.duration.seconds.field']);
            return false;
        }
    }

    var factorVal = getElementValue('factor');
    if (factorVal != null) {
        if (isNaN(factorVal)) {
            CARBON.showWarningDialog(jsi18n['please.enter.a.valid.number.to.the.factor.field']);
            return false;
        }
    }

    /*var retryErroCode = getElementValue('retryErroCode');
     if (retryErroCode != null) {
     if (isNaN(retryErroCode)) {
     CARBON.showWarningDialog(jsi18n['please.enter.a.numeric.value.to.the.error.code']);
     return false;
     }
     } */

    var retryTimeoutVal = getElementValue('retryTimeOut');
    if (retryTimeoutVal != null) {
        if (isNaN(retryTimeoutVal)) {
            CARBON.showWarningDialog(jsi18n['please.enter.a.valid.number.to.the.retry.field']);
            return false;
        }
    }

    var retryDelay = getElementValue('retryDelay');
    if (retryDelay != null) {
        if (isNaN(retryDelay)) {
            CARBON.showWarningDialog(jsi18n['please.enter.a.valid.number.to.the.retrydelay.field']);
            return false;
        }
    }

    return true;
}

function convertToValidXMlString(originalStr) {
    //Replace all the correct code with invalid code
    var convertedStr = relpaceString(originalStr, "&amp;", "&");
    convertedStr = relpaceString(convertedStr, "&lt;", "<");
    convertedStr = relpaceString(convertedStr, "&gt;", ">");
    convertedStr = relpaceString(convertedStr, "&quot;", '"');
    //    convertedStr = relpaceString(convertedStr, "&#39;", "'");

    //Replace all the invalid code with correct code
    convertedStr = relpaceString(convertedStr, "&", "&amp;");
    convertedStr = relpaceString(convertedStr, "<", "&lt;");
    convertedStr = relpaceString(convertedStr, ">", "&gt;");
    convertedStr = relpaceString(convertedStr, '"', "&quot;");
    //    convertedStr = relpaceString(convertedStr, "'", "&#39;");
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
        jQuery.get("testConnection-ajaxprocessor.jsp?type=address&", {'url' : url},
                function(data, status) {
                    if (data.replace(/^\s+|\s+$/g, '') == 'success') {
                        CARBON.showInfoDialog(jsi18n['valid.address']+" "+ url);
                    } else if (data.replace(/^\s+|\s+$/g, '') == 'unknown') {
                        CARBON.showErrorDialog(jsi18n['unknown.address']+" " + url);
                    } else if (data.replace(/^\s+|\s+$/g, '') == 'malformed') {
                        CARBON.showErrorDialog(jsi18n['malformed.address']+" " + url);
                    } else if (data.replace(/^\s+|\s+$/g, '') == 'ssl_error') {
                        CARBON.showErrorDialog(jsi18n['ssl.error']+" " + url);
                    } else if (data.replace(/^\s+|\s+$/g, '') == 'unknown_service') {
                        CARBON.showErrorDialog(jsi18n['unknown.service']+" " + url);
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
//                CARBON.showErrorDialog(jsi18n['invalid.conf']);
                CARBON.showErrorDialog('Invalid Configuration');
                return false;
            }
        } catch (e) {
//            CARBON.showErrorDialog(jsi18n['invalid.conf']);
            CARBON.showErrorDialog('Invalid Configuration');
            return false;
        }
    } else {
        var parser = new DOMParser();
        var doc = parser.parseFromString(docStr, "text/xml");
        if (doc.documentElement.nodeName == "parsererror") {
//            CARBON.showErrorDialog(jsi18n['invalid.conf']);
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
        url : "testConnection-ajaxprocessor.jsp?type=wsdl&",
        data : {'type': 'wsdl', 'url': url},
        success : function(data, status) {
                        if (data.replace(/^\s+|\s+$/g, '') == 'success') {
                            //CARBON.showWarningDialog(jsi18n['invalid.address.cannot.proceed']);
                            isValid = true;
                        }
                    },
        async : false

    });    

    return isValid;
}


function showHideOnSelect(selectID,element){
    if(document.getElementById(selectID).checked == true){
        document.getElementById(element).style.display='';
    } else {
        document.getElementById(element).style.display="none"
    }
}