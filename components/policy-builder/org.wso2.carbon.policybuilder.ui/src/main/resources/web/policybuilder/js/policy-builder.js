/*
 * Copyright 2005,2006 WSO2, Inc. http://wso2.com
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

var lastUsedServiceId = "";
var lastUsedServiceVersion = "";

function getPolicyDoc(url) {

    var body_xml = '<ns1:getPolicyDocRequest xmlns:ns1="http://org.wso2.wsf/tools">' +
                   '<ns1:policyURL>' + url + '</ns1:policyURL>' +
                   '</ns1:getPolicyDocRequest>';

    var callURL = serviceBaseURL + "PolicyEditorService";

    new wso2.wsf.WSRequest(callURL, "getPolicyDoc", body_xml, getPolicyDocCallBack);

}


function getPolicyDocCallBack() {
    var policyDoc = removeCDATA(this.req.responseXML.getElementsByTagName("return")[0].firstChild.nodeValue);
    syncRawPolicyView(policyDoc);
    buildTreeView(policyDoc);

}

function getPolicSchemaDefs() {
    var body_xml = '<ns1:getAvailableSchemasRequest xmlns:ns1="http://org.wso2.wsf/tools">' +
                   '</ns1:getAvailableSchemasRequest>';

    var callURL = serviceBaseURL + "PolicyEditorService";

    new wso2.wsf.WSRequest(callURL, "getAvailableSchemas", body_xml, getPolicSchemaDefsCallback);

}

function getPolicSchemaDefsCallback() {
    var schemaFilesList = removeCDATA(this.req.responseXML.getElementsByTagName("return")[0].firstChild.nodeValue);

    var domParser = new DOMImplementation();
    var fileListXML = domParser.loadXML(schemaFilesList);

    var fileList = fileListXML.getElementsByTagName("file");
    for (var x = 0; x < fileList.length; x++) {
        if (x == (fileList.length - 1)) {
            getPolicySchemaDef(fileList.item(x).firstChild.nodeValue.toString(), true);
        } else {
            getPolicySchemaDef(fileList.item(x).firstChild.nodeValue.toString(), false);
        }

    }
}

function getPolicySchemaDef(policyName, isLastCall) {

    var body_xml = '<ns1:getSchemaRequest xmlns:ns1="http://org.wso2.wsf/tools">' +
                   '<ns1:fileName>' + policyName + '</ns1:fileName>' +
                   '</ns1:getSchemaRequest>';

    var callURL = serviceBaseURL + "PolicyEditorService";

    new wso2.wsf.WSRequest(callURL, "getSchema", body_xml, getPolicySchemaDefCallBack, isLastCall);
}

function getPolicySchemaDefCallBack() {
    var schemaDoc = removeCDATA(this.req.responseXML.getElementsByTagName("return")[0].firstChild.nodeValue);
    storeSchema(schemaDoc);
    if (this.params) {
        //this is the last call, build the policy menu
        buildPolicyMenu();
    }
}

function removeCDATA(candidateString)
{
    //Verify whether this is a CDATA string
    if (candidateString.substring(0, 9) == "<![CDATA[") {
        //Removing <![CDATA[
        candidateString = candidateString.substring(9, candidateString.length);
        //Removing ]]>
        candidateString = candidateString.substring(0, candidateString.length - 3);
    } else if (candidateString.substring(0, 12) == "&lt;![CDATA[") {
        //Removing &lt;![CDATA[
        candidateString = candidateString.substring(12, candidateString.length);
        //Removing ]]&lt;
        candidateString = candidateString.substring(0, candidateString.length - 6);
    }

    return candidateString;


}

function syncRawPolicyView(policyDocument) {

    var rawPolicyTextArea = document.getElementById("raw-policy");

    if (policyDocument.indexOf("?>") > -1) {
        policyDocument = policyDocument.substring(policyDocument.indexOf("?>") + 2);
    }

    try {
        var parser = new DOMImplementation();
        currentPolicyDoc = parser.loadXML(policyDocument);

        var browser = WSRequest.util._getBrowser();
        if (browser == "gecko") {
            // Gecko has inbuilt E4X. This formats XML nicely
            rawPolicyTextArea.value = "" + new XML(policyDocument);
        } else {
            // There's no known way to format in the client side. Sending to the backend
            formatXMLUsingService(policyDocument);
        }

        if (rawPolicyTextArea.value == "") {
            rawPolicyTextArea.value =
            '<wsp:Policy xmlns:wsp="http://schemas.xmlsoap.org/ws/2004/09/policy" />';
        }
    } catch(e) {
        //alert("Failed to parse the policy XML. Please check. [" + e.toString() + "]");
        rawPolicyTextArea.value = "" + policyDocument;
    }
}

function formatXMLUsingService(xml) {
    xml = "<![CDATA[" + xml + "]]>";

    var body_xml = '<ns1:formatXMLRequest xmlns:ns1="http://org.wso2.wsf/tools">' +
                   '<ns1:xml>' + xml + '</ns1:xml>' +
                   '</ns1:formatXMLRequest>';

    var callURL = serviceBaseURL + "PolicyEditorService";

    new wso2.wsf.WSRequest(callURL, "formatXML", body_xml, formatXMLUsingServiceCallback);
}

function formatXMLUsingServiceCallback() {
    document.getElementById("raw-policy").value =
    removeCDATA(this.req.responseXML.getElementsByTagName("return")[0].firstChild.nodeValue);
}

function syncPolicyTreeView() {
    var rawPolicy = document.getElementById("raw-policy").value;

    if (rawPolicy.indexOf("?>") > -1) {
        rawPolicy = rawPolicy.substring(rawPolicy.indexOf("?>") + 2);
    }

    buildTreeView(rawPolicy);
}

/**
 * Generates HTML UI elements required to gather input from the user in order to add/update
 * an element.
 * 
 * @param targetElement - To which elements the updations should be commited to
 * @param schemaElement - The remplate schema element to use to generate the UI
 * @param namespaceURI - The namespace URI to use for the new element
 * @param mode - Mode can be either 'add' or 'edit'
 */
function generateGathererUI(targetElement, schemaElement, namespaceURI, mode) {
    // Store the target element globally
    currentUITargetElement = targetElement;

    var actionName = "";
    if (mode == "add") {
        actionName = "Adding new element ";
    } else if (mode == "edit") {
        actionName = "Editing element ";
    }

    var prefix = schemaElement.prefix;

    // Start processing the attributes
    var elementAttributes = schemaElement.getElementsByTagName(prefix + ":attribute");

    if (elementAttributes.length > 0) {

        var uiHTML = "<div id='element-attribs'><table><tr><th>" + actionName +
                     schemaElement.getAttribute("name") +
                     "</th></tr>";

        for (var x = 0; x < elementAttributes.length; x++) {

            var attrbuteName = elementAttributes.item(x).getAttribute("name");
            if (attrbuteName != undefined) {
                uiHTML = uiHTML + "<tr><td>" + attrbuteName + "</td>";

                var attributeType = elementAttributes.item(x).getAttribute("type");
                if ((attributeType == prefix + ":anyURI") || (attributeType == prefix + ":float") ||
                    (attributeType == prefix + ":decimal") ||
                    (attributeType == prefix + ":double") ||
                    (attributeType == prefix + ":QName") ||
                    (attributeType == prefix + ":base64Binary") ||
                    (attributeType == prefix + ":integer")) {

                    // decide what to put as the default value
                    var defaultVal = "";
                    if (mode == "add") {
                        defaultVal = elementAttributes.item(x).getAttribute("default")
                        if (defaultVal == undefined) {
                            defaultVal = attributeType;
                        }
                    } else if (mode == "edit") {
                        // In this case, the default value should be whatever is already there
                        try {
                            defaultVal =
                            targetElement.getAttributes().getNamedItem(attrbuteName).getNodeValue();
                        } catch(ex) {
                        }
                    }
                    // Display a Text Box to collect data
                    uiHTML =
                    uiHTML + "<td><input id = '" + attrbuteName + "' type='text' value='" +
                    defaultVal +
                    "'/></td></tr>";
                } else if (attributeType == prefix + ":date") {
                    // This is a date type. The XML date format is "YYYY-MM-DD"
                    //todo: Implement with a date-picker component
                }
            }
        }

        // Add the button panel
        uiHTML = uiHTML +
                 "</table></div><div id='button-panel'>";

        if (mode == "add") {
            uiHTML = uiHTML +
                     "<input id='cmdAddElement' type='button' value='Add Element to Document' onclick='createElementFromUIData(\"" +
                     schemaElement.getAttribute("name") + "\",\"" +
                     namespaceURI + "\");'/>";
        } else if (mode == "edit") {
            uiHTML = uiHTML +
                     "<input id='cmdEditElement' type='button' value='Update Element' onclick='updateElementFromUIdata();'/>";
        }

        uiHTML = uiHTML + "</div>";

        document.getElementById("divPolicyInputGatherer").innerHTML = uiHTML;

    } else {
        document.getElementById("divPolicyInputGatherer").innerHTML =
        "The element '" + schemaElement.getAttribute("name") +
        "' does not seem to have editable attributes.";
    }
}

/**
 * Updates the current UI target element with inputs from the UI 
 */
function updateElementFromUIdata() {
    // Collect inputs from UI
    var inputTags = document.getElementById("element-attribs").getElementsByTagName("input");

    // Update the target element
    for (var x = 0; x < inputTags.length; x++) {
        var attributeName = inputTags[x].id;
        var attributeValue = inputTags[x].value;
        // Store the attribute in element
        currentUITargetElement.setAttribute(attributeName, attributeValue);
    }

    // Refresh and sync
    syncRawPolicyView(currentPolicyDoc.toString());
    buildTreeView(currentPolicyDoc.toString());

    alert("Element updated");
}

/**
 * Creates a document element from the data available in the UI
 *
 * @param elementName - The tag name of the new element
 * @param namespaceURI - Namespace URI to use
 */
function createElementFromUIData(elementName, namespaceURI) {
    var newXMLElement;

    var inputTags = document.getElementById("element-attribs").getElementsByTagName("input");

    // Create  the element
    if (namespaceURI != "") {
        var prefix = namespaceMap[namespaceURI];

        if (prefix == undefined) {
            // We need to define a prefix for this URI
            prefix = "poled" + Math.floor(Math.random() * 10001);

            newXMLElement =
            currentPolicyDoc.createElement(prefix + ":" + elementName);
            newXMLElement.setAttribute("xmlns:" + prefix, namespaceURI);

                    // Add the new URI to map
            namespaceMap[namespaceURI] = prefix;
        } else {
            newXMLElement =
            currentPolicyDoc.createElement(prefix + ":" + elementName);
        }
    } else {
        newXMLElement =
        currentPolicyDoc.createElement(elementName);
    }

    for (var x = 0; x < inputTags.length; x++) {
        var attributeName = inputTags[x].id;
        var attributeValue = inputTags[x].value;
        // Store the attribute in element
        newXMLElement.setAttribute(attributeName, attributeValue);
    }

    // Append the new element to the document
    currentUITargetElement.appendChild(newXMLElement);

    // Refresh and sync
    syncRawPolicyView(currentPolicyDoc.toString());
    buildTreeView(currentPolicyDoc.toString());

    // Clear UI components
    document.getElementById("divPolicyInputGatherer").innerHTML = "";
}


function getSchemaForElement(elementName) {
    // searching the element array
    for (var x = 0; x < elements.length; x++) {
        if (elements[x].schemaElement.getAttribute("name") == elementName) {
            return elements[x].schemaElement;
        }
    }

    // searching the attributes array
    for (x = 0; x < attributes.length; x++) {
        if (attributes[x].schemaElement.getAttribute("name") == elementName) {
            return attributes[x].schemaElement;
        }
    }

    return null;
}

function savePolicyXML() {
    // Ensure the in memory policy is in sync with the UI
    var rawPolicy = document.getElementById("raw-policy").value;
    if (rawPolicy.indexOf("?>") > -1) {
        rawPolicy = rawPolicy.substring(rawPolicy.indexOf("?>") + 2);
    }
    var domParser = new DOMImplementation();
    currentPolicyDoc = domParser.loadXML(rawPolicy);
    

    // check whether the root policy was removed
    if (currentPolicyDoc.getXML() == "") {
        CARBON.showErrorDialog("Policy content is blank. Please create a valid policy!");
    } else {

        if (currentPolicyURL != "null") {
            var body_xml = '<ns1:savePolicyXMLRequest xmlns:ns1="http://org.wso2.wsf/tools">' +
                           '<ns1:url>' + currentPolicyURL + '</ns1:url>' +
                           '<ns1:policy>' + currentPolicyDoc.toString() + '</ns1:policy>' +
                           '</ns1:savePolicyXMLRequest>';

            var callURL = serviceBaseURL + "PolicyEditorService";

            new wso2.wsf.WSRequest(callURL, "savePolicyXML", body_xml, savePolicyXMLCallback);
        } else if (callbackURL != "") {
            postbackUpdatedPolicy();
        }
    }
}

function savePolicyXMLCallback() {

}

function postbackUpdatedPolicy() {
    var formEl = document.getElementById("post-back-form");
    
    // Break down the call back URL into operation and parameters
    var parts = callbackURL.split("?");
    var endpoint = parts[0];
    formEl.setAttribute("action", endpoint);

    var formContentHTML = "";

    if (parts.length > 1) {
        var params = parts[1].split("&");
        if (params.length > 0) {
            for (var x = 0; x < params.length; x++) {
                // Break into key and value
                var pair = params[x].split("=");
                var key = pair[0];
                var value = pair[1];

                formContentHTML =
                formContentHTML + "<input type='hidden' name='" + key + "' value='" + value + "'/>"
            }
        }
    }

    formEl.innerHTML =
    formContentHTML + '<input type="hidden" name="policy" id="policy-content"/>';

    YAHOO.util.Event.onDOMReady(function() {
        document.getElementById("policy-content").value = currentPolicyDoc.toString();
        document.postbackForm.submit();
    });

}