/*
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

function displaySetProperties(isDisply) {
	var toDisplayElement;
	displayElement("mediator.property.action_row", isDisply);
	displayElement("mediator.property.value_row", isDisply);
	toDisplayElement = document.getElementById("mediator.namespace.editor");
	if (toDisplayElement != null) {
		if (isDisply) {
			toDisplayElement.style.display = '';
		} else {
			toDisplayElement.style.display = 'none';
		}
	}
}

function displayElement(elementId, isDisplay) {
	var toDisplayElement = document.getElementById(elementId);
	if (toDisplayElement != null) {
		if (isDisplay) {
			toDisplayElement.style.display = '';
		} else {
			toDisplayElement.style.display = 'none';
		}
	}
}


function createNamespaceEditor(elementId, id, prefix, uri) {
	var ele = document.getElementById(elementId);
	if (ele != null) {
		var createEle = document.getElementById(id);
		if (createEle != null) {
			if (createEle.style.display == 'none') {
				createEle.style.display = '';
			} else {
				createEle.style.display = 'none';
			}
		} else {
			ele.innerHTML = '<div id=\"'
					+ id
					+ '\">'
					+ '<table><tbody><tr><td>Prefix</td><td><input width="80" type="text" id=\"'
					+ prefix
					+ '\"+ '
					+ 'name=\"'
					+ prefix
					+ '\" value=""/></td></tr><tr><td>URI</td><td><input width="80" '
					+ 'type="text" id=\"' + uri + '\"+ name=\"' + uri
					+ '\"+ value=""/></td></tr></tbody></table></div>';
		}
	}
}

function getSelectedOperation(obj) {
	return obj.options[obj.selectedIndex].value
}

/**/
function deleteInput(i) {
    var propRow = document.getElementById("inputRaw" + i);
    if (propRow != undefined && propRow != null) {
        var parentTBody = propRow.parentNode;
        if (parentTBody != undefined && parentTBody != null) {
            parentTBody.removeChild(propRow);
            if (!isContainRaw(parentTBody)) {
                var propertyTable = document.getElementById("inputstable");
                propertyTable.style.display = "none";
            }
        }
    }
}

function deleteOutput(i) {

	var propRow = document.getElementById("outputRaw" + i);
    if (propRow != undefined && propRow != null) {
        var parentTBody = propRow.parentNode;
        if (parentTBody != undefined && parentTBody != null) {
            parentTBody.removeChild(propRow);
            if (!isContainRaw(parentTBody)) {
                var propertyTable = document.getElementById("outputstable");
                propertyTable.style.display = "none";
            }
        }
    }

    if (!isRemainOutputExpressions()) {
        resetOutputDisplayStyle("none");
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

function onInputTypeSelectionChange(i, name) {
    var propertyType = getSelectedValue('inputTypeSelection' + i);
    if (propertyType != null) {
        settype(propertyType, i, name);
    }
}


function settype(type, i, name) {
    var nsEditorButtonTD = document.getElementById("nsEditorButtonTD" + i);
    if (nsEditorButtonTD == null || nsEditorButtonTD == undefined) {
        return;
    }
    if ("expression" == type) {
        resetDisplayStyle("");
        nsEditorButtonTD.innerHTML = "<a href='#nsEditorLink' class='nseditor-icon-link' style='padding-left:40px' onclick=\"showNameSpaceEditor('inputValue" + i + "')\">" + name + "</a>";
    } else {
        nsEditorButtonTD.innerHTML = "";
        if (!isRemainPropertyExpressions()) {
            resetDisplayStyle("none");
        }
    }
}

function onOutputTypeSelectionChange(i, name) {
	var propertyType = getSelectedValue('outputTypeSelection' + i);
	if (propertyType != null) {
		setOutputtype(propertyType, i, name);
	}
}

function setOutputtype(type, i, name) {
    var outputnsEditorButtonTD = document.getElementById("outputnsEditorButtonTD" + i);
    if (outputnsEditorButtonTD == null || outputnsEditorButtonTD == undefined) {
        return;
    }
    
    var outputSourceExpressionTD = document.getElementById("outputSourceExpressionTD" + i);

    if (outputSourceExpressionTD == null || outputSourceExpressionTD == undefined) {
        return;
    }
    
    var outputSourcensEditorButtonTD = document.getElementById("outputSourcensEditorButtonTD" + i);
    if (outputSourcensEditorButtonTD == null || outputSourcensEditorButtonTD == undefined) {
        return;
    }

    if ("expression" == type) {
        resetOutputDisplayStyle("");
        outputnsEditorButtonTD.innerHTML = "<a href='#nsoutputEditorLink' class='nseditor-icon-link' style='padding-left:40px' onclick=\"showNameSpaceEditor('outputValue" + i + "')\">" + name + "</a>";
        outputSourceExpressionTD.innerHTML = "<input type='text' name='outputSourceExpression" + i + "' id='outputSourceExpression" + i + "' value=''" + " class='esb-edit small_textbox' />";
        outputSourcensEditorButtonTD.innerHTML = "<a href='#nsoutputsourceEditorLink' class='nseditor-icon-link' style='padding-left:40px' onclick=\"showNameSpaceEditor('outputSourceExpression" + i + "')\">" + name + "</a>";
    } else {
        outputnsEditorButtonTD.innerHTML = "";
        outputSourceExpressionTD.innerHTML = "";
        outputSourcensEditorButtonTD.innerHTML = "";
        if (!isRemainOutputExpressions()) {
            resetOutputDisplayStyle("none");
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

function resetDisplayStyle(displayStyle) {
    document.getElementById('ns-edior-th').style.display = displayStyle;
    var nsCount = document.getElementById("inputCount");
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

function resetOutputDisplayStyle(displayStyle) {
    document.getElementById('output-ns-editor-th').style.display = displayStyle;
    document.getElementById('output-source-expression').style.display = displayStyle;
    document.getElementById('output-source-ns-editor-th').style.display = displayStyle;
    
    var nsCount = document.getElementById("outputCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var outputnsEditorButtonTD = document.getElementById("outputnsEditorButtonTD" + k);
            if (outputnsEditorButtonTD != undefined && outputnsEditorButtonTD != null) {
                outputnsEditorButtonTD.style.display = displayStyle;
            }
            
            var outputSourceExpressionTD = document.getElementById("outputSourceExpressionTD" + k);
            if (outputSourceExpressionTD != undefined && outputSourceExpressionTD != null) {
            	outputSourceExpressionTD.style.display = displayStyle;
            }
            
            var outputSourcensEditorButtonTD = document.getElementById("outputSourcensEditorButtonTD" + k);
            if (outputSourcensEditorButtonTD != undefined && outputSourcensEditorButtonTD != null) {
            	outputSourcensEditorButtonTD.style.display = displayStyle;
            }
        }
    }
}

function isRemainPropertyExpressions() {
    var nsCount = document.getElementById("inputCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var propertyType = getSelectedValue('inputTypeSelection' + k);
            if ("expression" == propertyType) {
                return true;
            }
        }
    }
    return false;
}

function isRemainOutputExpressions() {
    var nsCount = document.getElementById("outputCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var propertyType = getSelectedValue('outputTypeSelection' + k);
            if ("expression" == propertyType) {
                return true;
            }
        }
    }
    return false;
}

function loadConfigedInputs(val){
	 
	 jQuery.ajax({
         type:'POST',
         url: '../paypal-mediator/editInputs_ajaxprocessor.jsp',
         data:'operationName='+val  ,
         success: function(data){
		 	jQuery("#configInputs").html(data);                                                                                                    

         }
      });  
}

function loadConfigedOutputs(val){
	 
	 jQuery.ajax({
         type:'POST',
         url: '../paypal-mediator/editOutputs_ajaxprocessor.jsp',
         data:'operationName='+val  ,
         success: function(data){
		 	jQuery("#configOutputs").html(data);                                                                                                    

         }
      });  

}

//-----

function addOutput(name) {

    if (!isValidProperties()) {
        return false;
    }

    if(0 == document.getElementById('outputtbody'). rows.length) {
    	document.getElementById('output-ns-editor-th').style.display      = "none";
    	document.getElementById('output-source-expression').style.display = "none";
    	document.getElementById('output-source-ns-editor-th').style.display = "none";
    }

    var displayStyleOfNSEditor = document.getElementById('output-ns-editor-th').style.display;
    var displayStyleOfSourceExpression = document.getElementById('output-source-expression').style.display;
    var displayStyleOfSourceNSEditor = document.getElementById('output-source-ns-editor-th').style.display;
  
    
    var propertyCount = document.getElementById("outputCount");
    var i = propertyCount.value;

    var currentCount = parseInt(i);
    currentCount = currentCount + 1;

    propertyCount.value = currentCount;
    
    var propertytable = document.getElementById("outputstable");
    propertytable.style.display = "";
    
    var propertytbody = document.getElementById("outputtbody");
    
    var propertyRaw = document.createElement("tr");
    propertyRaw.setAttribute("id", "outputRaw" + i);

    var nameTD = document.createElement("td");
    nameTD.innerHTML = "<input type='text' name='outputName" + i + "' id='outputName" + i + "'" + "' value='outputName" + i + "'" + " />";
    nameTD.style.display = "none";
    
    var typeTD = document.createElement("td");
    typeTD.appendChild(createproperttypecombobox('outputTypeSelection' + i, i, name))

    var valueTD = document.createElement("td");
    valueTD.innerHTML = "<input type='text' name='outputValue" + i + "' id='outputValue" + i + "'" +
                        " class='esb-edit small_textbox' />";
    var nsTD = document.createElement("td");
    nsTD.setAttribute("id", "outputnsEditorButtonTD" + i);
    nsTD.style.display = displayStyleOfNSEditor;

    //
    var sourceExpressionValueTD = document.createElement("td");
    sourceExpressionValueTD.setAttribute("id", "outputSourceExpressionTD" + i);
    sourceExpressionValueTD.innerHTML = "";
    //sourceExpressionValueTD.innerHTML = "<input type='text' name='outputSourceExpression" + i + "' id='outputSourceExpression" + i + "' class='esb-edit small_textbox' />";
    
    sourceExpressionValueTD.style.display = displayStyleOfSourceExpression;
    
    var sourcensTD = document.createElement("td");
    sourcensTD.setAttribute("id", "outputSourcensEditorButtonTD" + i);
    sourcensTD.style.display = displayStyleOfSourceNSEditor;
    //
    var deleteTD = document.createElement("td");
    deleteTD.innerHTML =  "<a href='#' class='delete-icon-link' onclick='deleteOutput(" + i + ");return false;'>" + paypalMediatorJsi18n["mediator.paypal.action.delete"] + "</a>";

    propertyRaw.appendChild(nameTD);
    propertyRaw.appendChild(typeTD);
    propertyRaw.appendChild(valueTD);
    propertyRaw.appendChild(nsTD);
    propertyRaw.appendChild(sourceExpressionValueTD);
    propertyRaw.appendChild(sourcensTD);
    propertyRaw.appendChild(deleteTD);
    propertytbody.appendChild(propertyRaw);

    return true;
}

function isValidProperties() {

    var nsCount = document.getElementById("outputCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
/*            var prefix = document.getElementById("outputName" + k);
            if (prefix != null && prefix != undefined) {
                if (prefix.value == "") {
                    CARBON.showWarningDialog(paypalMediatorJsi18n["mediator.paypal.propemptyerror"])
                    return false;
                }
            }
*/    
            var uri = document.getElementById("outputValue" + k);
            if (uri != null && uri != undefined) {
                if (uri.value == "") {
                    CARBON.showWarningDialog(paypalMediatorJsi18n["mediator.paypal.valueemptyerror"])
                    return false;
                }
            }
            
            if( "expression" == getSelectedValue('outputTypeSelection' + k)) {
            	var uri = document.getElementById("outputSourceExpression" + k);
            	if (uri != null && uri != undefined) {
            		if (uri.value == "") {
            			CARBON.showWarningDialog(paypalMediatorJsi18n["mediator.paypal.sourcevalueemptyerror"])
            			return false;
            		}
            	}
            }
        }
    }
    return true;
}

function isValidInputs() {

    var nsCount = document.getElementById("inputCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    alert("Called");
    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {

            var uri = document.getElementById("inputValue" + k);
            var req = document.getElementById("inputRequired_hidden" + k);
            var name = document.getElementById("inputName_hidden" + k);
            alert('Value of ' + req);
            if (uri == null || uri == undefined || uri.value == "" && true == req) {
             
                    CARBON.showWarningDialog(name + ': ' + paypalMediatorJsi18n["mediator.paypal.inputvalueemptyerror"])
                    return false;
                
            }
        }
    }
    return true;
}

function createproperttypecombobox(id, i, name) {
    // Create the element:
    var combo_box = document.createElement('select');

    // Set some properties:
    combo_box.name = id;
    combo_box.setAttribute("id", id);
    combo_box.onchange = function () {
        onOutputTypeSelectionChange(i, name)
    };
    // Add some choices:
    var choice = document.createElement('option');
    choice.value = 'literal';
    choice.appendChild(document.createTextNode('Value'));
    combo_box.appendChild(choice);

    choice = document.createElement('option');
    choice.value = 'expression';
    choice.appendChild(document.createTextNode('Expression'));
    combo_box.appendChild(choice);

    return combo_box;
}