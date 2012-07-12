function addArgument(name) {

    if (!isValidArguments()) {
        return false;
    }

    var argCountElem = document.getElementById("argCount");
    var newIndex = parseInt(argCountElem.value);
    argCountElem.value = newIndex + 1;

    document.getElementById("argumentTable").style.display = "";
    var argTableBody = document.getElementById("argumentTableBody");

    var argRaw = document.createElement("tr");
    argRaw.setAttribute("id", "argRaw" + newIndex);

    var indexTD = document.createElement("td");
    indexTD.innerHTML = newIndex;
    indexTD.setAttribute("id", "argIndex" + newIndex);

    var typeTD = document.createElement("td");
    typeTD.appendChild(createArgTypeCombo('argType' + newIndex, newIndex, name));

    var valueTD = document.createElement("td");
    valueTD.innerHTML = "<input type='text' name='payloadFactory.argValue" + newIndex + "' id='payloadFactory.argValue" + newIndex + "'" +
                        " class='esb-edit small_textbox' />";

    var nsTD = document.createElement("td");
    nsTD.setAttribute("id", "nsEditorButtonTD" + newIndex);
    nsTD.style.display = document.getElementById('ns-edior-th').style.display;

    var deleteTD = document.createElement("td");
    deleteTD.innerHTML = "<a href='#' class='delete-icon-link' onclick='deleteArg(" + newIndex + ");return false;'>" +
            payloadfactory_i18n["mediator.payloadFactory.delete"] + "</a>";

    argRaw.appendChild(indexTD);
    argRaw.appendChild(typeTD);
    argRaw.appendChild(valueTD);
    argRaw.appendChild(nsTD);
    argRaw.appendChild(deleteTD);

    argTableBody.appendChild(argRaw);
    refreshIndexLabels();
    return true;
}

function isValidArguments() {
    var i = document.getElementById("argCount").value;
    var currentCount = parseInt(i);
    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var valueElem = document.getElementById("payloadFactory.argValue" + k);
            if (valueElem != null && valueElem != undefined) {
                if (valueElem.value == "") {
                    CARBON.showWarningDialog(payloadfactory_i18n["mediator.payloadFactory.empty.args"]);
                    return false;
                }
            }
        }
    }
    return formatXMLValidate();
}

function formatXMLValidate() {
    //XML Validation
    var payloadFormatXML = document.getElementById("payloadFactory.format").value;

    if (window.ActiveXObject) {
        try {
            var doc = new ActiveXObject("Microsoft.XMLDOM");
            doc.async = "false";
            var hasParse = doc.loadXML(payloadFormatXML);
            if (!hasParse) {
                CARBON.showErrorDialog(payloadfactory_i18n["invalid.inline.xml"]);
                form.Value.focus();
                return false;
            }
        } catch (e) {
            CARBON.showErrorDialog(payloadfactory_i18n["invalid.inline.xml"]);
            form.Value.focus();
            return false;
        }
    } else {
        var parser = new DOMParser();
        var dom = parser.parseFromString(payloadFormatXML, "text/xml");
        if (dom.documentElement.nodeName == "parsererror" || dom.getElementsByTagName("parsererror").length > 0) {
            CARBON.showErrorDialog(payloadfactory_i18n["invalid.inline.xml"]);
            form.Value.focus();
            return false;
        }
    }
    return true;
}


function createArgTypeCombo(id, i, name) {
    var combo_box = document.createElement('select');
    combo_box.name = id;
    combo_box.setAttribute("id", id);
    combo_box.onchange = function () {
        onArgTypeChange(i);
    };
    var choice = document.createElement('option');
    choice.value = 'value';
    choice.appendChild(document.createTextNode(payloadfactory_i18n["mediator.payloadFactory.value"])); //TODO: i18n
    combo_box.appendChild(choice);

    choice = document.createElement('option');
    choice.value = 'expression';
    choice.appendChild(document.createTextNode(payloadfactory_i18n["mediator.payloadFactory.expression"]));
    combo_box.appendChild(choice);

    return combo_box;
}

function deleteArg(i) {
    var index = parseInt(document.getElementById("argIndex" + i).innerHTML);
    var tblArguments = document.getElementById("argumentTable");
    tblArguments.deleteRow(index);
    if (tblArguments.rows.length <= 1) {
        tblArguments.style.display = "none";
    }
    refreshIndexLabels();
    if (!isRemainPropertyExpressions()) {
        resetDisplayStyle("none");
    }
}

function onArgTypeChange(i) {
    var argType = document.getElementById('argType' + i).value;
    var nsEditorButtonTD = document.getElementById("nsEditorButtonTD" + i);
    if ('expression' == argType) {
        resetDisplayStyle("");
        nsEditorButtonTD.innerHTML = "<a href='#nsEditorLink' class='nseditor-icon-link' style='padding-left:40px' onclick=\"showNameSpaceEditor('payloadFactory.argValue" + i + "')\">" +
                payloadfactory_i18n["mediator.payloadFactory.namespaces"] + "</a>";
        nsEditorButtonTD.style.display = "";
        document.getElementById('ns-edior-th').style.display = "";
    } else {
        nsEditorButtonTD.innerHTML = "";
        nsEditorButtonTD.style.display = document.getElementById('ns-edior-th').style.display;
        if (!isRemainPropertyExpressions()) {
            resetDisplayStyle("none");
        }
    }
}

function refreshIndexLabels() {
    var tblArgs = document.getElementById("argumentTable");
    var rows = tblArgs.rows;
    var i = 0;
    for (i = 1; i < rows.length; ++i) {
        var currentRow = rows[i];
        currentRow.cells[0].innerHTML = i;
    }
}

function payloadfactoryMediatorValidate() {
    return isValidArguments();
}

function isRemainPropertyExpressions() {
    var nsCount = document.getElementById("argCount");
    var i = nsCount.value;
    var currentCount = parseInt(i);
    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var propertyType = getSelectedValue('argType' + k);
            if ("expression" == propertyType) {
                return true;
            }
        }
    }
    return false;
}


function resetDisplayStyle(displayStyle) {
    document.getElementById('ns-edior-th').style.display = displayStyle;
    var nsCount = document.getElementById("argCount");
    var i = nsCount.value;
    var currentCount = parseInt(i);
    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var nsEditorButtonTD = document.getElementById("nsEditorButtonTD" + k);
            if (nsEditorButtonTD != undefined && nsEditorButtonTD != null) {
                nsEditorButtonTD.style.display = displayStyle;
            }
        }
    }
}

function getSelectedValue(id) {
    var propertyType = document.getElementById(id);
    var propertyType_indexstr = null;
    var propertyType_value = null;
    if (propertyType != null) {
        propertyType_indexstr = propertyType.selectedIndex;
        if (propertyType_indexstr != null) {
            propertyType_value = propertyType.options[propertyType_indexstr].value;
        }
    }
    return propertyType_value;
}