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

function calloutMediatorValidate() {
    var target;
    var source
    var serviceUrl = document.getElementById("mediator.callout.svcURL");
    if (serviceUrl && serviceUrl.value == "") {
        CARBON.showErrorDialog(calloutMediatorJsi18n["callout.service.required"]);
        return false;
    }
    var sourceGroup = document.getElementById("sourceGroupXPath");
    if (sourceGroup && sourceGroup.checked) {
        source = document.getElementById("mediator.callout.source.xpath_val");
    } else {
        source = document.getElementById("mediator.callout.source.key_val");
    }
    if (source && source.value == "") {
        CARBON.showErrorDialog(calloutMediatorJsi18n["callout.source.required"]);
        return false;
    }

    var targetGroup = document.getElementById("targetGroupXPath");
    if (targetGroup && targetGroup.checked) {
        target = document.getElementById("mediator.callout.target.xpath_val");
    } else {
        target = document.getElementById("mediator.callout.target.key_val");
    }
    if (target && target.value == "") {
        CARBON.showErrorDialog(calloutMediatorJsi18n["callout.target.required"]);
        return false;
    }
    return true;
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
            ele.innerHTML = '<div id=\"' + id + '\">' +
                            '<table><tbody><tr><td>Prefix</td><td><input width="80" type="text" id=\"'+ prefix + '\"+ ' +
                            'name=\"'+ prefix + '\" value=""/></td></tr><tr><td>URI</td><td><input width="80" ' +
                            'type="text" id=\"'+ uri + '\"+ name=\"'+ uri + '\"+ value=""/></td></tr></tbody></table></div>';
        }
    }
}

