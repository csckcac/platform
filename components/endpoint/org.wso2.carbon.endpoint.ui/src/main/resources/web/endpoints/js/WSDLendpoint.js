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

function displayWSDLURI() {
    var inlineRaw = document.getElementById('inLineWSDLID');
    var wsdlURIRaw = document.getElementById('wsdlURIID');
    if (inlineRaw != null && inlineRaw != undefined) {
        inlineRaw.style.display = "none";
    }

    if (wsdlURIRaw != null && wsdlURIRaw != undefined) {
        wsdlURIRaw.style.display = "";
    }
}

function displayinLineWSDLArea() {
    var inlineRaw = document.getElementById('inLineWSDLID');
    var wsdlURIRaw = document.getElementById('wsdlURIID');
    if (inlineRaw != null && inlineRaw != undefined) {
        inlineRaw.style.display = "";
    }

    if (wsdlURIRaw != null && wsdlURIRaw != undefined) {
        wsdlURIRaw.style.display = "none";
    }
}

// check if a valid WSDL
function isValidWSDL(WSDLDoc) {
    if (window.ActiveXObject) {
        try {
            var doc = new ActiveXObject("Microsoft.XMLDOM");
            doc.async = 'false';
            var isLoaded = doc.loadXML(WSDLDoc);
            if (!isLoaded) {
                return false;
            }
        } catch (e) {
            return false;
        }
    } else {
        var parser = new DOMParser();
        var dom = parser.parseFromString(WSDLDoc, "text/xml");
        if (dom.documentElement.nodeName == 'parsererror') {
            return false;
        }
    }
    if(WSDLDoc.indexOf("wsdl2")!= -1) {
        CARBON.showWarningDialog(jsi18n['wsdl2.not.supported']);
        return false;
    }else if(WSDLDoc.indexOf('definitions') == -1){
        CARBON.showWarningDialog(jsi18n['invalid.wsdl.definition.xml.parse.error']);
        return false;
    } else {
        return true;
    }
}
