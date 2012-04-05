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

function treeColapse(icon) {
    var parentNode = icon.parentNode;
    var allChildren = parentNode.childNodes;
    var todoOther = "";
    var isRoot = false;
    var i;
    //Do minimizing for the root node
    if (icon.id == "treeColapser") {
        isRoot = true;
    }

    if (isRoot) {
        var iconChilds = icon.parentNode.childNodes;
        for (i = 0; i < iconChilds.length; i++) {
            if (iconChilds[i].nodeName == "DIV") {
                if (iconChilds[i].className == "branch-node" && iconChilds[i].style.display == "none") {
                    iconChilds[i].style.display = "";
                    YAHOO.util.Dom.removeClass(icon, "plus-icon");
                    YAHOO.util.Dom.addClass(icon, "minus-icon");
                }
                if (iconChilds[i].className == "branch-node" && iconChilds[i].style.display == "") {
                    iconChilds[i].style.display = "none";
                    YAHOO.util.Dom.removeClass(icon, "minus-icon");
                    YAHOO.util.Dom.addClass(icon, "plus-icon");
                }
            }
        }
    }

    //Do minimizing for the rest of the nodes
    for (i = 0; i < allChildren.length; i++) {
        if (allChildren[i].nodeName == "UL") {

            if (allChildren[i].style.display == "none") {
                allChildren[i].style.display = "";
                YAHOO.util.Dom.removeClass(icon, "plus-icon");
                YAHOO.util.Dom.addClass(icon, "minus-icon");
                todoOther = "show";
                parentNode.style.height = "auto";
            }
            else {
                allChildren[i].style.display = "none";
                YAHOO.util.Dom.removeClass(icon, "minus-icon");
                YAHOO.util.Dom.addClass(icon, "plus-icon");
                todoOther = "hide";
                parentNode.style.height = "50px";
            }
        }
    }
    for (i = 0; i < allChildren.length; i++) {
        if (allChildren[i].className == "branch-node") {
            if (todoOther == "hide") {
                allChildren[i].style.display = "none";
            } else {
                allChildren[i].style.display = "";
            }
        }
    }
}

// create a XML doc given the xml string
function createXMLDoc(text) {
    var xmlDoc;
    var parser;
    //Internet Explorer
    try {
        xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
        xmlDoc.async = "false";
        xmlDoc.loadXML(text);
    } catch(e) {
        //Firefox, Mozilla, Opera, etc.
        try {
            parser = new DOMParser();
            xmlDoc = parser.parseFromString(text, "application/xml");
        }
        catch(e) {
            // TODO - error handling
        }
    }
    return xmlDoc;
}

// menu for options
var aMenuItems = [

    { text: "Address" },
    { text: "WSDL" },
    { text: "Failover Group" },
    { text: "Load-balance group"}

];

// Creating the menu
// oMenu is defined in either failOverEndpoint.jsp or in loadBalacneEndpoint.jsp
function createMenu() {
    oMenu = new YAHOO.widget.Menu("basicmenu");
    var aMenuItems = [

        { text: "Address" },
        { text: "WSDL" },
        { text: "Failover Group" },
        { text: "Load-balance group"}];

    oMenu.addItems(aMenuItems);
}

// initilaze the 'doc' document defined in
function initDoc() {
    if (doc == null) {
        if (endpointConfiguration == 'null') {
            doc = newXML(null, null);
            var rootElm;
            if (navigator.appName == "Netscape") {
                rootElm = doc.createElementNS("http://ws.apache.org/ns/synapse", "endpoint");
            } else if (navigator.appName == "Microsoft Internet Explorer") {
                rootElm = doc.createElement("endpoint");
                rootElm.setAttribute("xmlns", "http://ws.apache.org/ns/synapse");
            }

            var rootFailover;
            if (mainType == "load") {
                rootFailover = doc.createElement("loadbalance");
            } else {
                rootFailover = doc.createElement("failover");
            }

            rootElm.setAttribute("id", mainType + ".0");
            rootElm.appendChild(rootFailover);
            doc.appendChild(rootElm);
        }
        else {
            doc = stringToXML(endpointConfiguration);
            // asign ids to the endpoint nodes in the xml doc
            var rootElement = doc.documentElement;
            rootElement.setAttribute("id", mainType + ".0");
            // if this is a load balance configuration it can have session and loadbalance configuration element in same
            // level
            if (mainType == 'load') {
                for (var i = 0; i < rootElement.childNodes.length; i++) {
                    if (rootElement.childNodes[i].nodeName == 'loadbalance') {
                        assignIds(rootElement.childNodes[i], mainType + '.0');
                        break;
                    }

                }
            } else {
                assignIds(rootElement.childNodes[0], mainType + '.0');
            }
            YAHOO.util.Event.onDOMReady(createHTMLTree);

        }
    }
}


// Create a new Document object. If no arguments are specified,
// the document will be empty. If a root tag is specified, the document
// will contain that single root tag. If the root tag has a namespace
// prefix, the second argument must specify the URL that identifies the
// namespace.

function newXML(rootTagName, namespaceURL) {
    if (!rootTagName) rootTagName = "";
    if (!namespaceURL) namespaceURL = "";
    if (document.implementation && document.implementation.createDocument) {
        // This is the W3C standard way to do it
        return document.implementation.createDocument(namespaceURL, rootTagName, null);
    }
    else { // This is the IE way to do it
        // Create an empty document as an ActiveX object
        // If there is no root element, this is all we have to do
        var doc = new ActiveXObject("MSXML2.DOMDocument");
        // If there is a root tag, initialize the document
        if (rootTagName) {
            // Look for a namespace prefix
            var prefix = "";
            var tagname = rootTagName;
            var p = rootTagName.indexOf(':');
            if (p != -1) {
                prefix = rootTagName.substring(0, p);
                tagname = rootTagName.substring(p + 1);
            }
            // If we have a namespace, we must have a namespace prefix
            // If we don't have a namespace, we discard any prefix
            if (namespaceURL) {
                if (!prefix) prefix = "a0"; // What Firefox uses
            }
            else prefix = "";
            // Create the root element (with optional namespace) as a
            // string of text
            var text = "<" + (prefix ? (prefix + ":") : "") + tagname +
                       (namespaceURL
                               ? (" xmlns:" + prefix + '="' + namespaceURL + '"')
                               : "") +
                       "/>";
            // And parse that text into the empty document
            doc.loadXML(text);
        }
        return doc;
    }
}

// TODO this needs some refactoring...
// create a XML document given the xml string
function stringToXML(text) {
    var xmlDoc, parser;
    //    var newString = new String(text);

    //Internet Explorer
    try {
        xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
        xmlDoc.async = "false";
        xmlDoc.loadXML(text);
        //        xmlDoc.loadXML(newString);
    }
    catch(e) {
        //Firefox, Mozilla, Opera, etc.
        try {
            parser = new DOMParser();
            xmlDoc = parser.parseFromString(text, "application/xml");
            //            xmlDoc = parser.parseFromString(newString, "application/xml");
        }
        catch(e) {
            // TODO
        }
    }
    return xmlDoc;
}

//recursive method to fill the id values to config
function assignIds(elm, parentId) {
    var splitId = parentId.split(".");

    var elmChilds = elm.childNodes;
    var j = 0;
    var type = '';
    for (var i = 0; i < elmChilds.length; i++) {
        if (elmChilds[i].nodeName == 'endpoint') {
            if (elmChilds[i].childNodes[0].nodeName == 'address') {
                type = 'address';
            } else if (elmChilds[i].childNodes[0].nodeName == 'wsdl') {
                type = 'wsdl';
            } else if (elmChilds[i].childNodes[0].nodeName == 'failover') {
                type = 'failover';
            } else {
                type = 'load';
            }
            var newId = type;
            for (var x = 1; x < splitId.length; x++) {
                newId += ("." + splitId[x]);
            }
            newId += '.' + j;
            elmChilds[i].setAttribute('id', newId);

            //Create the forms for each new ID (endpoint) created

            if (type == 'address' || type == 'wsdl') {
                createAddressOrWSDLEndpointForm(newId, type);
            } else if (type == 'failover') {
                //Call recursive method if the type is load or failover
                assignIds(elmChilds[i].childNodes[0], newId);
            } else if (type == 'load') {
                createLoadBalanceForm(newId);
                assignIds(elmChilds[i].childNodes[0], newId);
            }
            j++;
        }
    }
}


function createHTMLTree() {
    var treeRoot = document.getElementById("failoverTree");
    var li = document.createElement("LI");

    //Check for the root child elements and update the tree
    var rootEndpoint = getNodeById(mainType + ".0");
    var hasRootSubs = false;

    if(rootEndpoint.childNodes[0] != null && rootEndpoint.childNodes[0] != undefined){
        if(rootEndpoint.childNodes[0].nodeName == 'session'){
            hasRootSubs = true;
        }
    }

    if (rootEndpoint.childNodes[0].childNodes.length >= 1) {
        hasRootSubs = true;
    }
    li.innerHTML = "<div class=\"minus-icon\" onclick=\"treeColapse(this)\"" +
                   " id=\"treeColapser\" ></div>" +
                   "<div class=\"endpoints\" id=\"" + mainType + ".0\">" +
                   "<a class=\"root-endpoint\">root</a>" +
                   "<div class=\"sequenceToolbar\" style=\"width: 100px;\">" +
                   "<div>" +
                   "<a class=\"addChildStyle\">Add Endpoint</a>" +
                   "</div>" +
                   "</div>" +
                   "</div>";

    //Add a vertical line if root node has children
    if (hasRootSubs) {
        li.innerHTML += "<div class=\"branch-node\"></div>" +
                        "<ul class=\"child-list\"></ul>";
    }

    treeRoot.innerHTML = "";
    treeRoot.appendChild(li);
    //Extracting doc data
    createNodes(mainType + ".0", treeRoot.childNodes[0].childNodes[3]);
    selectNode(selectedFlag);
}

function getNodeById(id) {
    var subEndpoints = doc.getElementsByTagName("*");
    var toReturn = "";
    for (var i = 0; i < subEndpoints.length; i++) {
        if (subEndpoints[i].getAttribute('id') == id) toReturn = subEndpoints[i];
    }
    return toReturn;
}

function createNodes(id, toAdd) {
    var rootEndpoint = getNodeById(id);
    var rootFailover, rootChildren;
    var i;

    // if this is a loadbalance configuaration it can have session and loadbalance same level configuration
    // elements...
    if (mainType == 'load') {
        for (i = 0; i < rootEndpoint.childNodes.length; i++) {
            if (rootEndpoint.childNodes[i].nodeName == 'loadbalance') {
                rootFailover = rootEndpoint.childNodes[i];
                rootChildren = rootFailover.childNodes;
                break;
            }
        }
    }
    if (rootFailover == undefined) { // this mean we are running for the first time...
        rootFailover = rootEndpoint.childNodes[0];
        rootChildren = rootFailover.childNodes;
    }
    var isLast;
    var li;
    var the_ul;
    var colapseIcon = 'dot-icon';
    for (i = 0; i < rootChildren.length; i++) {

        if (i == (rootChildren.length - 1)) {
            isLast = true;
        } else {
            isLast = false;
        }

        if (rootChildren[i].childNodes[0].nodeName == "address") {
            li = document.createElement("LI");
            li.innerHTML = "<div class=\"dot-icon\"></div>" +
                           "<div class=\"endpoints\" id=" + rootChildren[i].getAttribute("id") + "><a class=\"address-endpoint\">Address</a>" +
                           "<div class=\"sequenceToolbar\" style=\"width: 100px;\">" +
                           "<div>" +
                           "<a class=\"deleteStyle\">Delete</a>" +
                           "</div>" +
                           "</div>" +
                           "</div>";
            if (!isLast) {
                li.innerHTML += "<div class=\"vertical-line-alone\"/>";
            }
            toAdd.appendChild(li);
        } else if (rootChildren[i].childNodes[0].nodeName == "wsdl") {
            li = document.createElement("LI");
            li.innerHTML = "<div class=\"dot-icon\"></div>" +
                           "<div class=\"endpoints\" id=" + rootChildren[i].getAttribute("id") + "><a class=\"wsdl-endpoint\">WSDL</a>" +
                           "<div class=\"sequenceToolbar\" style=\"width: 100px;\">" +
                           "<div>" +
                           "<a class=\"deleteStyle\">Delete</a>" +
                           "</div>" +
                           "</div>" +
                           "</div>";

            if (!isLast) {
                li.innerHTML += "<div class=\"vertical-line-alone\"/>";
            }
            toAdd.appendChild(li);
        } else if (rootChildren[i].childNodes[0].nodeName == "failover") {
            //Check wheather this failover node has child nodes
            var hasFailoverSubs = false;

            if (rootChildren[i].childNodes[0].childNodes.length >= 1) {
                hasFailoverSubs = true;
                colapseIcon = 'minus-icon';
            }

            li = document.createElement("LI");
            li.innerHTML = "<div class=" + colapseIcon + " onclick=\"treeColapse(this)\"></div>" +
                           "<div class=\"endpoints\" id=" + rootChildren[i].getAttribute("id") + "><a class=\"failover-endpoint\">Failover</a>" +
                           "<div class=\"sequenceToolbar\">" +
                           "<div><a class=\"addChildStyle\">Add Endpoint</a></div>" +
                           "<div><a class=\"deleteStyle\">Delete</a></div>" +
                           "</div>" +
                           "</div>";
            if (hasFailoverSubs) {
                li.innerHTML += "<div class=\"branch-node\"></div>";
                the_ul = document.createElement("UL");
                the_ul.className = "child-list";
                li.appendChild(the_ul);
                if (!isLast) {
                    li.className = "vertical-line";
                }
                toAdd.appendChild(li);

                //Call the recousive function
                createNodes(rootChildren[i].getAttribute("id"), the_ul);
            }

            else {
                if (!isLast) {
                    //                        li.className = "vertical-line";
                    li.innerHTML += "<div class=\"vertical-line-alone\"/>";
                }
                toAdd.appendChild(li);
            }
        } else if (rootChildren[i].childNodes[0].nodeName == "loadbalance") {
            var hasLoadSubs = false;
            if (rootChildren[i].childNodes[0].childNodes.length >= 1) {
                hasLoadSubs = true;
                colapseIcon = 'minus-icon';
            }

            li = document.createElement("LI");
            li.innerHTML = "<div class=" + colapseIcon + " onclick=\"treeColapse(this)\"></div>" +
                           "<div class=\"endpoints\" id=" + rootChildren[i].getAttribute("id") + "><a class=\"failover-endpoint\">LoadBalance</a>" +
                           "<div class=\"sequenceToolbar\">" +
                           "<div><a class=\"addChildStyle\">Add Endpoint</a></div>" +
                           "<div><a class=\"deleteStyle\">Delete</a></div>" +
                           "</div>" +
                           "</div>";
            if (hasLoadSubs) {
                li.innerHTML += "<div class=\"branch-node\"></div>";
                the_ul = document.createElement("UL");
                the_ul.className = "child-list";
                li.appendChild(the_ul);
                if (!isLast) {
                    li.className = "vertical-line";
                }
                toAdd.appendChild(li);
                //Call the recousive function
                createNodes(rootChildren[i].getAttribute("id"), the_ul);
            } else {
                if (!isLast) {
                    //                            li.className = "vertical-line";
                    li.innerHTML += "<div class=\"vertical-line-alone\"/>";
                }
                toAdd.appendChild(li);
            }
        }
    }
}

function selectNode(objId) {
//Check the availability of the dom object with the id objId
YAHOO.util.Event.onAvailable(objId, function() {
    var endpointDesign = document.getElementById('endpointDesign');
    var selectedType = objId.split(".")[0];

    if (selectedType == "failover" || objId == "load.0") {
        endpointDesign.style.display = "none";
    } else {
        endpointDesign.style.display = "";
    }
    var allNodes = document.getElementById("treePane").getElementsByTagName("*");
    for (var i = 0; i < allNodes.length; i++) {
        if (YAHOO.util.Dom.hasClass(allNodes[i], "selected-node")) {
            YAHOO.util.Dom.removeClass(allNodes[i], "selected-node");
        }
    }
    selectedFlag = objId;
    var nodeToFillList = document.getElementById(objId).childNodes;
    var nodeToFill;
    for (var j = 0; j < nodeToFillList.length; j++) {
        if (nodeToFillList[j].nodeName == "A") {
            nodeToFill = nodeToFillList[j];
        }
    }
    YAHOO.util.Dom.addClass(nodeToFill, "selected-node");

    //Select the form
    hideOtherForms(selectedFlag);
});
    
}

function hideOtherForms(id) {
    var allForms = document.getElementById("info").childNodes;

    for (var i = 0; i < allForms.length; i++) {
        if (allForms[i].nodeName == "DIV") {
            allForms[i].style.display = "none";
        }
    }
    if (document.getElementById("form_" + id) != null) {
        document.getElementById("form_" + id).style.display = "";
    }
}


//This method reads the dom tree of the html tree and register events and callback funtions for them
function initEndpoints() {
YAHOO.util.Event.onDOMReady(function() {
    var allDivs = document.getElementById("treePane").getElementsByTagName("*");
    var nodeDivs = new Array();  // store root-endpoint, load balance and failover group
    var otherDivs = new Array(); // store wsdl enpoint and address endpoints
    var addChildDivs = new Array();  // store endpoints which comes from 'Add Endpoint' endpoint
    var deleteDivs = new Array();   // store endpoints which comes from 'Delete' endpoint

    // YAHOO.util.Event.addListener("saveButton", "click", trigerSave);

    for (var i = 0; i < allDivs.length; i++) {
        if (YAHOO.util.Dom.hasClass(allDivs[i], "addChildStyle")) {
            addChildDivs.push(allDivs[i]);
        }
        if (YAHOO.util.Dom.hasClass(allDivs[i], "deleteStyle")) {
            deleteDivs.push(allDivs[i]);
        }
        if (YAHOO.util.Dom.hasClass(allDivs[i], "root-endpoint") || YAHOO.util.Dom.hasClass(allDivs[i], "failover-endpoint") || YAHOO.util.Dom.hasClass(allDivs[i], "load-endpoint")) {
            nodeDivs.push(allDivs[i]);
        }
        if (YAHOO.util.Dom.hasClass(allDivs[i], "wsdl-endpoint") || YAHOO.util.Dom.hasClass(allDivs[i], "address-endpoint")) {
            otherDivs.push(allDivs[i]);
        }
    }
    for (i = 0; i < nodeDivs.length; i++) {
        YAHOO.util.Event.addListener(nodeDivs[i], "click", selectCallback, nodeDivs[i].parentNode.id);

    }
    for (i = 0; i < otherDivs.length; i++) {
        YAHOO.util.Event.addListener(otherDivs[i], "click", selectCallback, otherDivs[i].parentNode.id);
    }
    for (i = 0; i < addChildDivs.length; i++) {
        YAHOO.util.Event.addListener(addChildDivs[i], "click", menuCallback, [addChildDivs[i],addChildDivs[i].parentNode.parentNode.parentNode.id]);
    }
    for (i = 0; i < deleteDivs.length; i++) {
        YAHOO.util.Event.addListener(deleteDivs[i], "click", deleteCallback, [deleteDivs[i],deleteDivs[i].parentNode.parentNode.parentNode.id]);
    }
});
}

//This is to generate the menu when someone clicks a Add endpoint
var menuCount = 0;
function menuCallback(e, obj) {
    menuCount++;
    var parms = [obj[0],'bl','tr'];

    var endpointNameId = '';

    if (mainType == 'load') {
        endpointNameId = 'load.name';

    } else if (mainType == 'failover') {
        endpointNameId = 'failover.name';
    }

    var isAnonEndpoint = false;
    var isAnon = document.getElementById('isAnnonEndpointID');
    if (isAnon != null && isAnon != undefined) {
        if (isAnon.value == 'true') {
            isAnonEndpoint = true;
        }
    }

    if (!isAnonEndpoint) {
        if (isEmptyField(endpointNameId)) {
            CARBON.showWarningDialog("Please specify the endpoint name before adding child endpoints");
            return;
        }
    }

    //The following block is to fix a bug with the YUI menu whic display at different places when clicked twice
    if (menuCount >= 2) {
        oMenu.destroy();
        oMenu = new YAHOO.widget.Menu("basicmenu");
        oMenu.clearContent();
        oMenu.addItems(aMenuItems);
    }
    selectNode(obj[1]);
    //Select the form

    oMenu.getItem(0).cfg.setProperty("onclick", { fn: onMenuItemClick, obj: [obj[1],"address"] });
    oMenu.getItem(1).cfg.setProperty("onclick", { fn: onMenuItemClick, obj: [obj[1],"wsdl"] });
    oMenu.getItem(2).cfg.setProperty("onclick", { fn: onMenuItemClick, obj: [obj[1],"failover"] });
    oMenu.getItem(3).cfg.setProperty("onclick", { fn: onMenuItemClick, obj: [obj[1],"load"] });


    oMenu.cfg.setProperty('context', parms);

    oMenu.render(document.body);

    oMenu.show();
}

//This is to select the from when someone clicks an icon
function selectCallback(e, obj) {
    selectNode(obj);
}


//This is the method called when the menu items are selected
function onMenuItemClick(e, e2, objx) {
    //Extracting the obj elements
    createXMLTree(objx[0], objx[1]);
    var treeRoot = document.getElementById("failoverTree");
    treeRoot.innerHTML = "";
    YAHOO.util.Event.onDOMReady(createHTMLTree);
    YAHOO.util.Event.onDOMReady(initEndpoints);
}

// calls to create the XML configuration at design view
function createXMLTree(toid, type) {
    var mainEndpointNode = getNodeById(toid);

    //Get the failover of load-balance node under the endpoint node to add sub element
    var addingFrom = mainEndpointNode.childNodes[0];
    var addingFromChildren = addingFrom.childNodes;
    var numChildren = addingFromChildren.length;
    var splitId = toid.split(".");
    var newId = type;
    for (var i = 1; i < splitId.length; i++) {
        newId += ("." + splitId[i]);
    }
    newId += "." + (numChildren + 1);
    //adding a random number to end of id to prevent browser form loading previous endpoint values
    newId += "." + Math.random();
    //Put this id as the selected flag
    selectedFlag = newId;
    var newNodeToAdd = doc.createElement('endpoint');
    newNodeToAdd.setAttribute("id", newId);

    var newSubNodeToAdd = '';

    if (type == 'address') {
        newSubNodeToAdd = doc.createElement('address');
    } else if (type == 'wsdl') {
        newSubNodeToAdd = doc.createElement('wsdl');
    } else if (type == 'failover') {
        newSubNodeToAdd = doc.createElement('failover');
    } else if (type == 'load') {
        newSubNodeToAdd = doc.createElement('loadbalance');
    }
    newNodeToAdd.appendChild(newSubNodeToAdd);

    //Append the created xml node to the main xml object (doc)
    addingFrom.appendChild(newNodeToAdd);

    // display the lower part
    var childEpDiv = document.getElementById('endpointDesign');
    if (childEpDiv != null && childEpDiv.style == 'display:none') {
        childEpDiv.style = '';
    }

    if (type == 'address' || type == 'wsdl') {
        createAddressOrWSDLEndpointForm(newId, type);
    } else if (type == 'load') {
        createLoadBalanceForm(newId);
    }

    // once the above form is ready display it
    YAHOO.util.Event.onAvailable('form_' + newId, function() {
        document.getElementById('form_' + newId).style.display = '';
    });
}

function deleteCallback(e, obj) {
    var nodeIdToRemove = obj[1];
    CARBON.showConfirmationDialog('Are you sure you whant to delete this endpoint?', function() {
        var nodeToRemove = getNodeById(nodeIdToRemove);
        nodeToRemove.parentNode.removeChild(nodeToRemove);
        selectedFlag = mainType + ".0";
        //Update the tree
        var treeRoot = document.getElementById("failoverTree");
        treeRoot.innerHTML = "";
        YAHOO.util.Event.onDOMReady(createHTMLTree);
        YAHOO.util.Event.onDOMReady(initEndpoints);
    }, null);
}

function activateDuration(selectNode, formID) {
    var selectOption = selectNode.options[selectNode.selectedIndex].value;
    var actionDuration = document.getElementById(formID + '_duration');
    if (selectOption != null && selectOption != undefined) {
        if (selectOption == 'SelectAValue') {
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

function showWSDLURI(formID) {
    var inlineWSDL = document.getElementById(formID + '_inLineWSDLID');
    if (inlineWSDL != null && inlineWSDL != undefined) {
        inlineWSDL.style.display = 'none';
    }
    var uriWSDL = document.getElementById(formID + '_wsdlURIID');
    if (uriWSDL != null && uriWSDL != undefined) {
        uriWSDL.style.display = '';
    }
}

function showinLineWSDLArea(formID) {
    var inlineWSDL = document.getElementById(formID + '_inLineWSDLID');
    if (inlineWSDL != null && inlineWSDL != undefined) {
        inlineWSDL.style.display = '';
    }
    var uriWSDL = document.getElementById(formID + '_wsdlURIID');
    if (uriWSDL != null && uriWSDL != undefined) {
        uriWSDL.style.display = 'none';
    }
}

// remove the id attribute and values
function cleanIDs(XMLDoc) {
    var allChildren = XMLDoc.getElementsByTagName('*');
    for (var i = 0; i < allChildren.length; i++) {
        allChildren[i].removeAttribute('id');
    }
    return XMLDoc;
}

// this method validate and update the endpoints
function upDateEndpoint(formID, ePtype) {

    // set the endpoint name for the doc
    /*var endpointName;
     var sessionType;
     var selectedSessionType;
     var sessionTimeOut = '0';
     if (mainType == 'failover') {
     endpointName = document.getElementById('failover.name').value;
     } else if (mainType == 'load') {
     endpointName = document.getElementById('load.name').value;
     }

     if (endpointName != null && endpointName != undefined) {
     if (mainType == 'failover') {
     getNodeById("failover.0").setAttribute('name', endpointName);
     } else if (mainType == 'load') {
     getNodeById("load.0").setAttribute('name', endpointName);
     // create a sesstion type element and attach it to main document
     // we are updating root endpoint
     var childNode = getNodeById('load.0');
     if (!updateLoadBalanceEndpoint('', childNode)) {
     CARBON.showWarningDialog(jsi18n['error.updating.the.configuration']);
     return false;
     }
     }
     }*/

    var endpointNode = getNodeById(selectedFlag);
    // validate and update
    if (ePtype == 'address' || ePtype == 'wsdl') {
        if (!isValidChildAddressOrWSDLEndpoint(formID, ePtype)) {
            return false;
        }
        // updating the child endpoint
        updateAddressOrWSDLEndpoint(formID, ePtype, endpointNode);
    } else if (ePtype == 'loadbalance') {
        // we are updating the child endpoint
        if (!updateLoadBalanceEndpoint(formID, endpointNode)) {
            CARBON.showWarningDialog(jsi18n['error.updating.the.configuration']);
            return false;
        }
    }

    // hide the child endpoing form
    var childEndpointForm = document.getElementById('endpointDesign');
    if (childEndpointForm != null && childEndpointForm != undefined) {
        childEndpointForm.style.display = 'none';
        CARBON.showInfoDialog('Endpoint configuaration for child endpoint updated at client side sucessfully.' +
                              'Use save button if you want to save this configuration');
        return true;
    }
    return false;
}

function isValidChildAddressOrWSDLEndpoint(formID, endpointType) {

    if (endpointType == 'address') {

        var address = document.getElementById(formID + '_address');
        if (isEmptyField(formID + '_address') || ltrim(address.value) == '' ||
            trim(address.value) == '' || rtrim(address.value) == '') {
            CARBON.showWarningDialog(jsi18n['address.field.cannot.be.empty']);
            return false;
        }

        // check for a valid URL
        var endpointURI = getElementValue(formID + '_address');
        if (endpointURI != null) {
            if (!isValidURL(endpointURI)) {
                CARBON.showWarningDialog(jsi18n['invalid.url.provided']);
                return false;
            }
        }

    } else if (endpointType == 'wsdl') {
        var inlineWSDL = document.getElementById(formID + '_inlineWSDL');
        if (inlineWSDL != null && inlineWSDL != undefined) {
            if (inlineWSDL.checked) {
                // not empty
                var inlineWSDLVal = document.getElementById(formID + '_inlineWSDLVal').value;
                if (inlineWSDLVal != null && inlineWSDLVal != undefined) {
                    // is empty ?
                    if (trim(inlineWSDLVal) == '' || ltrim(inlineWSDLVal) == ''
                            || trim(inlineWSDLVal) == '') {
                        CARBON.showWarningDialog(jsi18n['inline.wsdl.cannot.be.empty']);
                        return false;
                    }
                    // is a valid xml doc ?
                    // the WSDL should not contain the xml delecraion
                    if (!isValidWSDL(inlineWSDLVal)) {
                        CARBON.showWarningDialog(jsi18n['invalid.wsdl.definition.xml.parse.error']);
                        return false;
                    }
                }
            }
        }
        var uri = document.getElementById(formID + '_uriWSDL');
        if (uri != null && uri != undefined) {
            if (uri.checked) {
                if (isEmptyField('uriWSDLVal')) {
                    CARBON.showWarningDialog(jsi18n['wsdl.uri.field.cannot.be.empty']);
                    return false;
                }

            }
        }

        var service = document.getElementById(formID + '_service');
        var port = document.getElementById(formID + '_port');
        if (isEmptyField(formID + '_service') || trim(service.value) == '' ||
            ltrim(service.value) == '' || rtrim(service.value) == '') {
            CARBON.showWarningDialog(jsi18n['service.field.cannot.be.empty']);
            return false;
        }

        if (isEmptyField(formID + '_port') || trim(port.value) == '' ||
            ltrim(port.value) == '' || rtrim(port.value) == '') {
            CARBON.showWarningDialog(jsi18n['port.field.cannot.be.empty']);
            return false;
        }
    }

    /*var errorVal = getElementValue(formID + '_errorCodes');
    if (errorVal != null) {
        if (isNaN(errorVal)) {
            CARBON.showWarningDialog(jsi18n['please.enter.a.numeric.value.to.the.error.code']);
            return false;
        }
    }*/

    var durationVal = getElementValue(formID + '_suspend');
    if (durationVal != null) {
        if (isNaN(durationVal)) {
            CARBON.showWarningDialog(jsi18n['please.enter.a.numeric.value.to.the.suspend.duration.seconds.field']);
            return false;
        }
    }

    var maxDurationVal = getElementValue(formID + '_maxDur');
    if (maxDurationVal != null) {
        if (isNaN(maxDurationVal)) {
            CARBON.showWarningDialog(jsi18n['please.enter.a.valid.number.to.the.max.duration.seconds.field']);
            return false;
        }
    }

    var factorVal = getElementValue(formID + '_factor');
    if (factorVal != null) {
        if (isNaN(factorVal)) {
            CARBON.showWarningDialog(jsi18n['please.enter.a.valid.number.to.the.factor.field']);
            return false;
        }
    }

    /*var retryErroCode = getElementValue(formID + '_timeoutErrorCodes');
    if (retryErroCode != null) {
        if (isNaN(retryErroCode)) {
            CARBON.showWarningDialog(jsi18n['please.enter.a.numeric.value.to.the.error.code']);
            return false;
        }
    }*/

    var retry = getElementValue(formID + '_retry');
    if (retry != null) {
        if (isNaN(retry)) {
            CARBON.showWarningDialog(jsi18n['please.enter.a.valid.number.to.the.retry.field']);
            return false;
        }
    }

    var retryTimeoutVal = getElementValue(formID + '_retryDelay');
    if (retryTimeoutVal != null) {
        if (isNaN(retryTimeoutVal)) {
            CARBON.showWarningDialog(jsi18n['please.enter.a.valid.number.to.the.retrydelay.field']);
            return false;
        }
    }
    return true;
}

// update 'doc' according to the child endpoint conf
function updateAddressOrWSDLEndpoint(formId, type, endpointNode) {
    // clear previously added configuration settings from the doc
    // NOTE: while developing there was a strange behaviour here. If i put a debug point here before
    // the following line and contine it says undefined. but a debug point after this line work
    // without any problem...
    var childEndpointNode = endpointNode.childNodes[0];
    endpointNode.appendChild(childEndpointNode); // TODO do we need this ?
    // read the values
    var suspendErrorCode = document.getElementById(formId + '_errorCodes');
    var suspend = document.getElementById(formId + '_suspend');
    var maxDur = document.getElementById(formId + '_maxDur');
    var factor = document.getElementById(formId + '_factor');

    var timeoutErrCodes = document.getElementById(formId + '_timeoutErrorCodes');
    var retry = document.getElementById(formId + '_retry');
    var retryDelay = document.getElementById(formId + '_retryDelay');

    var timeoutAction = document.getElementById(formId + '_timeoutAction');
    var duration = document.getElementById(formId + '_duration');

    var wsAddressing = document.getElementById(formId + '_wsAddressing');
    var useSeperatelistener = document.getElementById(formId + '_separeteListener');

    var WSSecurity = document.getElementById(formId + '_wssecurity');
    var securityPolicy = document.getElementById(formId + '_wssecurityPolicy');

    var WSRM = document.getElementById(formId + '_wsRM');
    var WSRMPolicy = document.getElementById(formId + '_wsrmPolicy');

    var endpointName = document.getElementById(formId + '_name').value;
    if (endpointName != null && endpointName != '') {
        endpointNode.setAttribute('name', endpointName);
    } else {
        endpointNode.removeAttribute('name');
    }

    if (type == 'address') {
        var address = document.getElementById(formId + '_address');
        var format = document.getElementById(formId + '_format');
        var optimize = document.getElementById(formId + '_optimize');

        var validAddress = convertToValidXMlString(address.value);
        // address
        childEndpointNode.setAttribute('uri', validAddress);

        // format
        if (format.childNodes[format.selectedIndex].value != 'SelectAValue') {
            childEndpointNode.setAttribute('format', format.childNodes[format.selectedIndex].value);
        } else {
            childEndpointNode.removeAttribute('format');
        }

        // optimize
        if (optimize.childNodes[optimize.selectedIndex].value != 'SelectAValue') {
            childEndpointNode.setAttribute('optimize', optimize.childNodes[optimize.selectedIndex].value);
        } else {
            childEndpointNode.removeAttribute('optimize');
        }

    } else if (type == 'wsdl') {
        var inlineWSDL = document.getElementById(formId + '_inlineWSDL');
        var service = document.getElementById(formId + '_service');
        var port = document.getElementById(formId + '_port');
        var hasinLineWSDL = false;

        if (inlineWSDL != null && inlineWSDL != undefined) {
            if (inlineWSDL.checked) {
                var inlineWSDLVal = document.getElementById(formId + '_inlineWSDLVal');

                hasinLineWSDL = childEndpointNode.getElementsByTagName('wsdl:definitions');
                if (hasinLineWSDL.length == 0) {
                    hasinLineWSDL = childEndpointNode.getElementsByTagName('wsdl20:description');
                    if (hasinLineWSDL.length == 0) {
                        hasinLineWSDL = childEndpointNode.getElementsByTagName('definitions');
                        if (hasinLineWSDL.length > 0) {
                            childEndpointNode.removeChild(childEndpointNode.getElementsByTagName('definitions')[0]);
                        }
                    } else if (hasinLineWSDL.length > 0) {
                        childEndpointNode.removeChild(childEndpointNode.getElementsByTagName('wsdl20:description')[0]);
                    }

                } else if (hasinLineWSDL.length > 0) {
                    childEndpointNode.removeChild(childEndpointNode.getElementsByTagName('wsdl:definitions')[0]);
                }
                childEndpointNode.appendChild(stringToXML(inlineWSDLVal.value).firstChild);
                childEndpointNode.removeAttribute('uri');
            }
        }

        var urisel = document.getElementById(formId + '_uriWSDL');
        if (urisel != null && urisel != undefined) {
            if (urisel.checked) {
                var uriWSDLVal = document.getElementById(formId + '_uriWSDLVal');
                childEndpointNode.setAttribute('uri', uriWSDLVal.value);
            }
        }

        // service
        childEndpointNode.setAttribute('service', service.value);

        // port
        childEndpointNode.setAttribute('port', port.value);
    }

    // suspend error code
    if (suspend.value != '' || suspendErrorCode.value != '' || maxDur.value != '' || factor.value != '') {
        if (childEndpointNode.getElementsByTagName('suspendOnFailure').length > 0) {
            // this is to ensure alwyas a new copy is added to 'doc' configuration
            // rather than concatinating a new node each  time when updating the configuration
            childEndpointNode.removeChild(childEndpointNode.getElementsByTagName('suspendOnFailure')[0]);
        }

        var suspendOnFailureNode = doc.createElement('suspendOnFailure');
        if (suspendErrorCode.value != '') {
            var suspendErorCodeNode = doc.createElement('errorCodes');
            suspendErorCodeNode.appendChild(doc.createTextNode(suspendErrorCode.value));
            suspendOnFailureNode.appendChild(suspendErorCodeNode);
        }
        if (suspend.value != '') {
            var intialDurationNode = doc.createElement('initialDuration');
            intialDurationNode.appendChild(doc.createTextNode(suspend.value));
            suspendOnFailureNode.appendChild(intialDurationNode);
        }
        if (factor.value != '') {
            var factorNode = doc.createElement('progressionFactor');
            factorNode.appendChild(doc.createTextNode(factor.value));
            suspendOnFailureNode.appendChild(factorNode);
        }
        if (maxDur.value != '') {
            var maxDurationNode = doc.createElement('maximumDuration');
            maxDurationNode.appendChild(doc.createTextNode(maxDur.value));
            suspendOnFailureNode.appendChild(maxDurationNode);
        }
        childEndpointNode.appendChild(suspendOnFailureNode);


    }
    // retry out
    if (timeoutErrCodes.value != '' || retry.value != '' || retryDelay.value != '') {
        if (childEndpointNode.getElementsByTagName('markForSuspension').length > 0) {
            childEndpointNode.removeChild(childEndpointNode.getElementsByTagName('markForSuspension')[0]);
        }
        var onTimeOutNode = doc.createElement('markForSuspension');
        if (timeoutErrCodes.value != '') {
            var suspendTimeoutErrorNode = doc.createElement('errorCodes');
            suspendTimeoutErrorNode.appendChild(doc.createTextNode(timeoutErrCodes.value));
            onTimeOutNode.appendChild(suspendTimeoutErrorNode);
        }
        if (retry.value != '') {
            var retryNode = doc.createElement('retriesBeforeSuspension');
            retryNode.appendChild(doc.createTextNode(retry.value));
            onTimeOutNode.appendChild(retryNode);
        }
        if (retryDelay.value != '') {
            var retryDelayNode = doc.createElement('retryDelay');
            retryDelayNode.appendChild(doc.createTextNode(retryDelay.value));
            onTimeOutNode.appendChild(retryDelayNode);
        }
        childEndpointNode.appendChild(onTimeOutNode);
    }

    // Time out
    if (timeoutAction.childNodes[timeoutAction.selectedIndex].value != 'SelectAValue') {
        if (childEndpointNode.getElementsByTagName('timeout').length > 0) {
            childEndpointNode.removeChild(childEndpointNode.getElementsByTagName('timeout')[0]);
        }
        var timeOutNode = doc.createElement('timeout');
        var timeOutActionNode = doc.createElement('action');
        var timeOutDurationNode = doc.createElement('duration');

        if (timeoutAction.childNodes[timeoutAction.selectedIndex].value != '') {
            timeOutActionNode.appendChild(doc.createTextNode(timeoutAction.childNodes[timeoutAction.selectedIndex].value));
            timeOutNode.appendChild(timeOutActionNode);
        }
        if (duration.value != '') {
            timeOutDurationNode.appendChild(doc.createTextNode(duration.value));
            timeOutNode.appendChild(timeOutDurationNode);
        }
        childEndpointNode.appendChild(timeOutNode);
    } else {
        if (childEndpointNode.getElementsByTagName('timeout').length > 0) {
            childEndpointNode.removeChild(childEndpointNode.getElementsByTagName('timeout')[0]);
        }
    }

    // Qos
    if (wsAddressing.checked) {
        if (childEndpointNode.getElementsByTagName('enableAddressing').length > 0) {
            childEndpointNode.removeChild(childEndpointNode.getElementsByTagName('enableAddressing')[0]);
        }
        var wsAddressingNode = doc.createElement('enableAddressing');
        if (useSeperatelistener.checked) {
            wsAddressingNode.setAttribute('separateListener', 'true');
        }
        childEndpointNode.appendChild(wsAddressingNode);
    } else {
        if (childEndpointNode.getElementsByTagName('enableAddressing').length > 0) {
            childEndpointNode.removeChild(childEndpointNode.getElementsByTagName('enableAddressing')[0]);
        }
    }

    if (WSSecurity.checked) {
        if (childEndpointNode.getElementsByTagName('enableSec').length > 0) {
            childEndpointNode.removeChild(childEndpointNode.getElementsByTagName('enableSec')[0]);
        }
        var wsSecurityNode = doc.createElement('enableSec');
        if (securityPolicy.value != '') {
            wsSecurityNode.setAttribute('policy', securityPolicy.value);
        }
        childEndpointNode.appendChild(wsSecurityNode);
    } else {
        if (childEndpointNode.getElementsByTagName('enableSec').length > 0) {
            childEndpointNode.removeChild(childEndpointNode.getElementsByTagName('enableSec')[0]);
        }
    }

    if (WSRM.checked) {
        if (childEndpointNode.getElementsByTagName('enableRM').length > 0) {
            childEndpointNode.removeChild(childEndpointNode.getElementsByTagName('enableRM')[0]);
        }
        var wsRMNode = doc.createElement('enableRM');
        if (WSRMPolicy.value != '') {
            wsRMNode.setAttribute('policy', WSRMPolicy.value);
        }
        childEndpointNode.appendChild(wsRMNode);
    } else {
        if (childEndpointNode.getElementsByTagName('enableRM').length > 0) {
            childEndpointNode.removeChild(childEndpointNode.getElementsByTagName('enableRM')[0]);
        }
    }

    var propertyCount = 0;
    for (var i=0 ; i < endpointNode.childNodes.length; ++i) {
        var child = endpointNode.childNodes[i];
        if (child.nodeName == 'property') {
            ++propertyCount;
        }
    }

    var properties = new Array(propertyCount);
    var j = 0;
    for (var i = 0; i < endpointNode.childNodes.length; ++i) {
        var child = endpointNode.childNodes[i];
        if (child.nodeName == 'property') {
            properties[j] = child;
            ++j;
        }
    }

    for (var i=0; i<properties.length;++i){
        endpointNode.removeChild(properties[i]);
    }

//
//        var child = endpointNode.childNodes[1];
//        if (child.nodeName == 'property') {
//            child.parent.removeChild(child);
//        }


//
//    while (endpointNode.childNodes.length > 1) {
//        var child = endpointNode.childNodes[1];
//        if (child.nodeName == 'property') {
//            child.parent.removeChild(child);
//        }
//    }

    var headerTable = document.getElementById(formId+"_headerTable");
    for (var j = 1; j < headerTable.rows.length; j++) {
        var propertyName = headerTable.rows[j].getElementsByTagName("input")[0].value;
        var propertyValue = headerTable.rows[j].getElementsByTagName("input")[1].value;
        var propertyScope = headerTable.rows[j].getElementsByTagName("select")[0].value;
        var propNode = doc.createElement('property');
        propNode.setAttribute('name',propertyName);
        propNode.setAttribute('value',propertyValue);
        propNode.setAttribute('scope',propertyScope);
        endpointNode.appendChild(propNode);
    }
}

// update the 'doc' configuration for load balacne endpoint
function updateLoadBalanceEndpoint(formID, endpointNode) {
    // TODO
    // according to the configuration of loadbalance there can be more than 1 session elements
    // and since the UI only gives chance to have one session element we'll assume it!
    var sessionNode = doc.createElement('session');
    var sessionTimeOutNode = doc.createElement('sessionTimeout');
    var isHasSessionNode = false;

    var i = 0;
    var selectedSessionOption = document.getElementById(formID + '_sesOptions').value;
    var sessionTimeOut = document.getElementById(formID + '_sessionTimeOut').value;
    if (isNaN(sessionTimeOut)) {
        CARBON.showWarningDialog(jsi18n['please.enter.a.valid.session.time.out.number']);
        return false;
    }
    var algoClassName = document.getElementById(formID + '_algoClassName').value;

    // TODO - get this option from the UI
    var propertyCount = 0;
    for (i = 0; i < endpointNode.childNodes.length; i++) {
        if (endpointNode.childNodes[i].nodeName == 'loadbalance') {
            //endpointNode.childNodes[i].setAttribute('algorithm', 'org.apache.synapse.endpoints.algorithms.RoundRobin');
            endpointNode.childNodes[i].setAttribute('algorithm', algoClassName);
        } else if (endpointNode.childNodes[i].nodeName == 'session') {
            sessionNode = endpointNode.childNodes[i];
            isHasSessionNode = true;
        }

        if (endpointNode.childNodes[i].nodeName == 'property') {
            ++propertyCount;
        }
    }

    for (i = 0; i < propertyCount; i++) {
        for (j = 0; j < endpointNode.childNodes.length; j++) {
            if (endpointNode.childNodes[j].nodeName == 'property') {
                var n = endpointNode.childNodes[j];
                endpointNode.removeChild(n);
            }
        }
    }


    if (selectedSessionOption == 'SelectAValue') {
        if (isHasSessionNode) {
            endpointNode.removeChild(sessionNode);
        }
    } else {
        if (isHasSessionNode) {
            endpointNode.removeChild(sessionNode);
            sessionNode = doc.createElement('session');
            sessionTimeOutNode = doc.createElement('sessionTimeout');
        }
        sessionNode.setAttribute('type', selectedSessionOption);
        sessionTimeOutNode.appendChild(doc.createTextNode(sessionTimeOut));
        sessionNode.appendChild(sessionTimeOutNode);
        endpointNode.appendChild(sessionNode);
    }

    var headerTable = document.getElementById("headerTable");
    for (var j = 1; j < headerTable.rows.length; j++) {
        var propertyName = headerTable.rows[j].getElementsByTagName("input")[0].value;
        var propertyValue = headerTable.rows[j].getElementsByTagName("input")[1].value;
        var propertyScope = headerTable.rows[j].getElementsByTagName("select")[0].value;
        var propNode = doc.createElement('property');
        propNode.setAttribute('name', propertyName);
        propNode.setAttribute('value', propertyValue);
        propNode.setAttribute('scope', propertyScope);
        endpointNode.appendChild(propNode);
    }
    return true;
}

function showHideAlgoInputDiv(selectNode) {
    var selectOption = selectNode.options[selectNode.selectedIndex].value;
    var algoInputDiv = document.getElementById('_algoInputDiv');
    if (selectOption != null && selectOption != undefined) {
        if (selectOption == 'other') { // show algoInputDiv
            if (algoInputDiv != null && algoInputDiv != undefined) {
                document.getElementById('_algoClassName').value = '';
                algoInputDiv.style.display = '';
            }
        } else { // hide algoInputDiv
            document.getElementById('_algoClassName').value = 'org.apache.synapse.endpoints.algorithms.RoundRobin';
            if (algoInputDiv != null && algoInputDiv != undefined) {
                algoInputDiv.style.display = 'none';
            }
        }
    }
}

function activateManagementField(selectNode) {
    var selectOption = selectNode.options[selectNode.selectedIndex].value;
    var sessionTimeOut = document.getElementById('_sessionTimeOut');
    if (selectOption != null && selectOption != undefined) {
        if (selectOption == 'SelectAValue') {
            if (sessionTimeOut != null && sessionTimeOut != undefined) {
                sessionTimeOut.disabled = 'disabled';
                sessionTimeOut.value = 0;
            }
        } else {
            if (sessionTimeOut != null && sessionTimeOut != undefined) {
                sessionTimeOut.disabled = '';
            }
        }
    }
}

function activateManagement(selectNode, formID) {
    var selectOption = selectNode.options[selectNode.selectedIndex].value;
    var sessionTimeOut = document.getElementById(formID + '_sessionTimeOut');
    if (selectOption != null && selectOption != undefined) {
        if (selectOption == 'SelectAValue') {
            if (sessionTimeOut != null && sessionTimeOut != undefined) {
                sessionTimeOut.disabled = 'disabled';
                sessionTimeOut.value = 0;
            }
        } else {
            if (sessionTimeOut != null && sessionTimeOut != undefined) {
                sessionTimeOut.disabled = '';
            }
        }
    }
}