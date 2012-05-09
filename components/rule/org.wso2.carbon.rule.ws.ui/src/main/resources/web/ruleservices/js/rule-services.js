function setRuleScriptType(type) {
    var ruleScriptKeyTR = document.getElementById("ruleScriptKeyTR");
    var ruleScriptSourceTR = document.getElementById("ruleScriptSourceTR");
    var rulesetCreationTR = document.getElementById("rulesetCreationTR");
    var rulesetCreationUploadTR = document.getElementById("rulesetCreationUploadTR");
    var ruleScriptUploadTR = document.getElementById("ruleScriptUploadTR");
    var ruleScriptURLTR = document.getElementById("ruleScriptURLTR");
    if ('key' == type) {
        ruleScriptKeyTR.style.display = "";
        rulesetCreationTR.style.display = "";
        ruleScriptSourceTR.style.display = "none";
        ruleScriptUploadTR.style.display = "none";
        ruleScriptURLTR.style.display = "none";
    } else if ('upload' == type) {
        ruleScriptSourceTR.style.display = "none";
        ruleScriptKeyTR.style.display = "none";
        rulesetCreationTR.style.display = "";
        ruleScriptUploadTR.style.display = "";
        ruleScriptURLTR.style.display = "none";
    }
    else if ('url' == type) {
        ruleScriptSourceTR.style.display = "none";
        ruleScriptKeyTR.style.display = "none";
        rulesetCreationTR.style.display = "";
        ruleScriptURLTR.style.display = "";
        ruleScriptUploadTR.style.display = "none";
    }else {
        ruleScriptSourceTR.style.display = "";
        rulesetCreationTR.style.display = "";
        ruleScriptKeyTR.style.display = "none";
        ruleScriptUploadTR.style.display = "none";
        ruleScriptURLTR.style.display = "none";
    }

    return true;
}

function showFactEditor(category, i) {

    var suffix = "index=" + i + "&category=" + category;
    var typeInput = document.getElementById(category + "Type" + i);
    var type = null;
    if (typeInput != null && typeInput != undefined) {
        type = typeInput.value;
    }
    if (type != undefined && type != null && type != "null" && type != "") {
        suffix += "&type=" + type;
    }
    var url = 'fact_slector-ajaxprocessor.jsp?' + suffix;

    var loadingContent = "<div id='workArea' style='overflow-x:hidden;'><div id='popupContent'><div class='ajax-loading-message'> <img src='../resources/images/ajax-loader.gif' align='top'/> <span>" + "Wating ..." + "</span> </div></div></div>";
    CARBON.showPopupDialog(loadingContent, ruleservicejsi18n["rule." + category + ".editor"], 200, false, null, 550);

    jQuery("#popupContent").load(url, null,
            function(res, status, t) {
                if (status != "success") {
                    CARBON.showWarningDialog(ruleservicejsi18n["rule.facteditor.error"]);
                }
            });
    return false;
}

function showPropertyEditor() {

    var url = 'property_editor-ajaxprocessor.jsp';

    var loadingContent = "<div id='workArea' style='overflow-x:hidden;'><div id='popupContent'><div class='ajax-loading-message'> <img src='../resources/images/ajax-loader.gif' align='top'/> <span>" + "Wating ..." + "</span> </div></div></div>";
    CARBON.showPopupDialog(loadingContent, ruleservicejsi18n["property.editor"], 200, false, null, 550);

    jQuery("#popupContent").load(url, null,
            function(res, status, t) {
                if (status != "success") {
                    CARBON.showWarningDialog(ruleservicejsi18n["property.editor.error"]);
                }
            });
    return false;
}

function addProperty() {

    var nsCount = document.getElementById("propertyCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    currentCount = currentCount + 1;

    nsCount.value = currentCount;

    var nstable = document.getElementById("propertyTable");
    nstable.style.display = "";
    var nstbody = document.getElementById("propertyTBody");

    var nsRaw = document.createElement("tr");
    nsRaw.setAttribute("id", "propertyTR" + i);

    var prefixTD = document.createElement("td");
    prefixTD.appendChild(createNSEditorTextBox("name" + i, null));

    var uriTD = document.createElement("td");
    uriTD.appendChild(createNSEditorTextBox("value" + i, "longInput"));

    var actionTD = document.createElement("td");
    actionTD.appendChild(createPropertyDeleteLink(i));
    nsRaw.appendChild(prefixTD);
    nsRaw.appendChild(uriTD);
    nsRaw.appendChild(actionTD);
    nstbody.appendChild(nsRaw);
    return true;
}

function createPropertyDeleteLink(i) {
    // Create the element:
    var factDeleteLink = document.createElement('a');
    // Set some properties:
    factDeleteLink.setAttribute("href", "#");
    factDeleteLink.style.paddingLeft = '40px';
    factDeleteLink.className = "delete-icon-link";
    factDeleteLink.appendChild(document.createTextNode(ruleservicejsi18n["rule.action.delete"]));
    factDeleteLink.onclick = function () {
        deletePropertyRaw(i)
    };
    return factDeleteLink;
}

function deletePropertyRaw(i) {
    CARBON.showConfirmationDialog(ruleservicejsi18n["ns.editor.delete.confirmation"], function() {
        var propRow = document.getElementById("propertyTR" + i);
        if (propRow != undefined && propRow != null) {
            var parentTBody = propRow.parentNode;
            if (parentTBody != undefined && parentTBody != null) {
                parentTBody.removeChild(propRow);
            }
        }
    });
}

function saveProperties() {

    var nsCount = document.getElementById("propertyCount");
    var count = parseInt(nsCount.value);
    var referenceString = "";
    for (var i = 0; i < count; i++) {
        var nameTD = "name" + i;
        var name = document.getElementById(nameTD);
        var valueTD = "value" + i;
        var value = document.getElementById(valueTD);
        if (name != null && name != undefined && value != null && value != undefined) {
            var nameValue = name.value;
            var valueText = value.value;
            if (nameValue != undefined && valueText != undefined && valueText != "") {
                referenceString += "&" + nameTD + "=" + nameValue + "&" + valueTD + "=" + valueText;
            }
        }
    }
    var url = 'property_save-ajaxprocessor.jsp?propertyCount=' + count + referenceString;
    jQuery.post(url, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showWarningDialog(ruleservicejsi18n["property.save.error"]);
                }
            });
    hideEditor();
    CARBON.closeWindow();
    return false;
}

function deleteOperation(name, i) {
    var opCount = document.getElementById("opCount");
    opCount.value = parseInt(opCount.value) - 1;
    CARBON.showConfirmationDialog(ruleservicejsi18n["operation.delete.confirmation"], function() {
        deleteRaw('operation', i);
        var suffix = "index=" + i + "&opname=" + name;
        var url = 'op_delete-ajaxprocessor.jsp?' + suffix;
        jQuery.get(url, ({}),
                function(data, status) {
                    if (status != "success") {
                        CARBON.showWarningDialog(ruleservicejsi18n['error.occurred']);
                        return false;
                    }
                });
    });

    return false;
}

function deleteFactArchives(name) {
    CARBON.showConfirmationDialog(ruleservicejsi18n["operation.delete.confirmation"], function() {
       // deleteFactArchiveRaw(name);
        var suffix = "factArchiveName=" + name;
        var url = 'fact_archive_delete-ajaxprocessor.jsp?' + suffix;
        jQuery.get(url, ({}),
                function(data, status) {
                    location.reload();
                    if (status != "success") {
                        CARBON.showWarningDialog(ruleservicejsi18n['error.occurred']);
                        return false;
                    }
                });
    });

    return false;
}

//function deleteFactArchiveRaw(name) {
//    var propRow1 = document.getElementById(name);
//    if (propRow1 != undefined && propRow1 != null) {
//        var parentTBody1 = propRow1.parentNode;
//        if (parentTBody1 != undefined && parentTBody1 != null) {
//            parentTBody1.removeChild(propRow1);
//            if (!isContainRaw(parentTBody1)) {
//                var factTable1 = document.getElementById("factArchiveListTable");
//                factTable1.style.display = "none";
//            }
//        }
//    }
//}


function getSelectedValue(id) {
    var variableType = document.getElementById(id);
    var variableType_indexstr = null;
    var variableType_value = null;
    if (variableType != null) {
        variableType_indexstr = variableType.selectedIndex;
        if (variableType_indexstr != null) {
            variableType_value = variableType.options[variableType_indexstr].value;
        }
    }
    return variableType_value;
}

function addFact(category, opName) {

    if (!validateFacts(category)) {
        return false;
    }

    var factCount = document.getElementById(category + "Count");
    var i = factCount.value;

    var currentCount = parseInt(i);
    currentCount = currentCount + 1;

    factCount.value = currentCount;

    var facttable = document.getElementById(category + "table");
    facttable.style.display = "";
    var facttbody = document.getElementById(category + "tbody");

    var factRaw = document.createElement("tr");
    factRaw.setAttribute("id", category + "Raw" + i);

    var factSelectorTD = document.createElement("td");
    factSelectorTD.appendChild(createFactEditorLLink(category, i));
    var nameTD = document.createElement("td");
    nameTD.appendChild(createFactTextBox(category + "Name" + i));
    var factTypeTD = document.createElement("td");
    factTypeTD.appendChild(createFactTextBox(category + "Type" + i));
    var deleteTD = document.createElement("td");
    deleteTD.appendChild(createFactDeleteLink(category, i));

        var factNameSpace = document.createElement("td");
    factNameSpace.appendChild(createFactTextBox(category + "NameSpace" + i));
        var factXPath = document.createElement("td");
    factXPath.appendChild(createFactTextBox(category + "XPath" + i));


    var nsBrowserTD = document.createElement("td");
    nsBrowserTD.setAttribute("id", category + 'NsEditorButtonTD' + i);
    nsBrowserTD.appendChild(createNSEditorLink(category, i, opName));

    factRaw.appendChild(factTypeTD);
    factRaw.appendChild(factSelectorTD);
    factRaw.appendChild(nameTD);
    factRaw.appendChild(factNameSpace);
    factRaw.appendChild(factXPath);
    factRaw.appendChild(nsBrowserTD);
    factRaw.appendChild(deleteTD);
    facttbody.appendChild(factRaw);
    return true;
}

function createNSEditorLink(category, i, opName) {
    // Create the element:
    var factHref = document.createElement('a');

    // Set some properties:
    factHref.setAttribute("href", "#nsEditorLink");
    factHref.className = "nseditor-icon-link";
    factHref.appendChild(document.createTextNode(ruleservicejsi18n["rule.namespaces"]));
    factHref.style.paddingLeft = '40px';
    factHref.onclick = function () {
        showNameSpaceEditor(category + 'Value' + i, opName);
    };
    return factHref;
}

function createFactEditorLLink(category, i) {
    // Create the element:
    var factHref = document.createElement('a');

    // Set some properties:
    factHref.setAttribute("href", "#factEditorLink");
    factHref.style.paddingLeft = '40px';
    factHref.className = "fact-selector-icon-link";
    factHref.appendChild(document.createTextNode(ruleservicejsi18n["fact.type"]));
    factHref.onclick = function () {
        showFactEditor(category, i)
    };
    return factHref;
}
function createFactDeleteLink(category, i) {
    // Create the element:
    var factDeleteLink = document.createElement('a');

    // Set some properties:
    factDeleteLink.setAttribute("href", "#");
    factDeleteLink.style.paddingLeft = '40px';
    factDeleteLink.className = "delete-icon-link";
    factDeleteLink.appendChild(document.createTextNode(ruleservicejsi18n["rule.action.delete"]));
    factDeleteLink.onclick = function () {
        deleteFact(category, i)
    };
    return factDeleteLink;
}

function createFactTextBox(id) {
    // Create the element:
    var factInput = document.createElement('input');

    // Set some properties:
    factInput.setAttribute("type", 'text');
    factInput.setAttribute("id", id);
    factInput.setAttribute("name", id);
    factInput.className = "longInput";
    return factInput;
}

function deleteFact(category, i) {
    CARBON.showConfirmationDialog(ruleservicejsi18n[ "fact.delete.confirmation"], function() {
        deleteRaw(category, i)
    });
}

function deleteRaw(category, i) {
    var propRow = document.getElementById(category + "Raw" + i);
    if (propRow != undefined && propRow != null) {
        var parentTBody = propRow.parentNode;
        if (parentTBody != undefined && parentTBody != null) {
            parentTBody.removeChild(propRow);
            if (!isContainRaw(parentTBody)) {
                var factTable = document.getElementById(category + "table");
                factTable.style.display = "none";
            }
        }
    }
}


function isContainRaw(tbody) {
    if (tbody.childNodes == null || tbody.childNodes.length == 0) {
        return false;
    } else {
        for (var i = 0; i < tbody.childNodes.length; i++) {
            var child = tbody.childNodes[i];
            if (child != undefined && child != null) {
                if (child.nodeName == "tr" || child.nodeName == "TR") {
                    return true;
                }
            }
        }
    }
    return false;
}

function validateFacts(category) {

    var nsCount = document.getElementById(category + "Count");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var propRow = document.getElementById(category + "Raw" + k);
            if (propRow != null && propRow != undefined) {
                var type = document.getElementById(category + "Type" + k);
                if (type != null && type != undefined) {
                    if (type.value == "") {
                        CARBON.showWarningDialog(ruleservicejsi18n["invalid." + category]);
                        return false;
                    }
                }
            }
        }
    }
    return true;
}


