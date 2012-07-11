var itemsGlobal;
function addAspect() {
    if (lifecyleOperationStarted) {
        CARBON.showWarningDialog(org_wso2_carbon_governance_custom_lifecycles_checklist_ui_jsi18n["lifecycle.operation.in.progress"]);
        return;
    }
    lifecyleOperationStarted = true;
	var path = document.getElementById('aspectResourcePath').value;
	var aspect = document.getElementById('aspect').value;
    sessionAwareFunction(function() {
        new Ajax.Request(
                '../lifecycles/add_aspect_ajaxprocessor.jsp',
        {
            method : 'post',
            parameters : {
                path : path,
                aspect : aspect
            },

            onSuccess : function() {
                lifecyleOperationStarted = false;
                refreshLifecyclesSection(path);
                refreshPropertiesSection(path);
                alternateTableRows('myTable', 'tableEvenRow', 'tableOddRow');
            },
            onFailure : function(transport) {
                lifecyleOperationStarted = false;
                showRegistryError(org_wso2_carbon_governance_custom_lifecycles_checklist_ui_jsi18n["failed.to.add.aspect"] + ' ' + transport.responseText);
            }
        });
    }, org_wso2_carbon_governance_custom_lifecycles_checklist_ui_jsi18n["session.timed.out"]);
}

function removeAspect() {
    if (lifecyleOperationStarted) {
        CARBON.showWarningDialog(org_wso2_carbon_governance_custom_lifecycles_checklist_ui_jsi18n["lifecycle.operation.in.progress"]);
        return;
    }
    lifecyleOperationStarted = true;

    var path = document.getElementById('resPath').value;
	var aspect = document.getElementById('aspectName').value;
    sessionAwareFunction(function() {
        CARBON.showConfirmationDialog(org_wso2_carbon_governance_custom_lifecycles_checklist_ui_jsi18n["are.you.sure.you.want.to.delete"] + "<strong>'" + aspect + "'</strong> " + org_wso2_carbon_governance_custom_lifecycles_checklist_ui_jsi18n["permanently"], function() {
            new Ajax.Request('../lifecycles/remove_aspect_ajaxprocessor.jsp',
            {
                method : 'post',
                parameters : {
                    path : path,
                    aspect : aspect
                },
                onSuccess: function(transport) {
                    lifecyleOperationStarted = false;
                    if (transport) {
                        refreshLifecyclesSection(path);
                    }
                },
                onFailure: function(transport) {
                    CARBON.showErrorDialog(transport.responseText);
                    lifecyleOperationStarted = false;
                }
            });
        }, null);
    }, org_wso2_carbon_governance_custom_lifecycles_checklist_ui_jsi18n["session.timed.out"]);
}

var lifecyleOperationStarted = false;


function loadCustomUI(path, aspect, action, mediaType, customUI, callBack) {
    sessionAwareFunction(function() {
        if (lifecyleOperationStarted) {
            CARBON.showWarningDialog(org_wso2_carbon_governance_custom_lifecycles_checklist_ui_jsi18n["lifecycle.operation.in.progress"]);
            return;
        }

        if (action != "itemClick") {
            document.getElementById(action).disabled = true;
        }

        lifecyleOperationStarted = true;
        var items = [];
        if (document.getElementById('itemcount') != null) {
            var itemcount = document.getElementById('itemcount').value;
            for (var i = 0; i < itemcount; i++) {
                if (document.getElementById('option' + i.toString()).checked) {
                    items[i] = 'true';
                } else {
                    items[i] = 'false';
                }
            }
        }

        itemsGlobal = items;

        if (jQuery.trim(customUI) != "") {

            var parameterMap = new Array();

            if(customUI.indexOf("?") > 1){
                var customUIParameters = customUI.split("?")[1];
                customUI = customUI.split("?")[0];
                if(customUIParameters.indexOf("&")){
                    var parameterSegments = customUIParameters.split("&");
                    for (var i = 0; i < parameterSegments.length; i++) {
                        var obj = parameterSegments[i];
                        parameterMap[i] = obj;
                    }
                }
            }
            jQuery.ajax({
                traditional: true,
                url: customUI,
                type:"POST",
                success: function(data) {
                    jQuery('#customUIDiv').html(data);
                },
                error:function(){
                },
                data : {
                    path : path,
                    aspect : aspect,
                    action : action,
                    mediaType : mediaType,
                    callBack : callBack,
                    parameterMap : parameterMap
                }
            });
        }
        else {
            invokeAspect(path,aspect,action,callBack,"");
        }
    }, org_wso2_carbon_governance_custom_lifecycles_checklist_ui_jsi18n["session.timed.out"]);
}

function invokeAspect(path, aspect, action, callBack, parameterString,customUIAction) {
    //            We are passing an empty array with the default call
    if(customUIAction == null){
        customUIAction = "";
    }

    if (parameterString == null || parameterString == "") {
        parameterString = new Array();
    }
    new Ajax.Request('../lifecycles/invoke_aspect_ajaxprocessor.jsp',
        {
            method : 'post',
            parameters : {
                path : path,
                aspect : aspect,
                action : action,
                items : itemsGlobal,
                parameterString : parameterString
            },
            onSuccess : function() {
                 lifecyleOperationStarted = false;
                if (action != "itemClick") {
                    document.getElementById(action).disabled = false;
                }
                var message = org_wso2_carbon_governance_custom_lifecycles_checklist_ui_jsi18n["lifecycle.operation.successful"]
                     + " : " + action;
                if (callBack && typeof callBack == "function") {
                    CARBON.showInfoDialog(message, function () {
                        callBack();
                        refreshLifecyclesSection(path, action);
                    }, function () {
                        callBack();
                        refreshLifecyclesSection(path, action);
                    });
                } else {
                    CARBON.showInfoDialog(message, function () {
                        refreshLifecyclesSection(path, action);
                    }, function () {
                        refreshLifecyclesSection(path, action);
                    });
                }
            },
            onFailure : function(transport) {
                document.getElementById(action).disabled = false;
                document.getElementById(customUIAction).disabled = false;
                showRegistryError(org_wso2_carbon_governance_custom_lifecycles_checklist_ui_jsi18n["failed.to.invoke.aspect"] + ' ' + transport.responseText);
                lifecyleOperationStarted = false;
            }
        });
}
function refreshLifecyclesSection(path) {
    sessionAwareFunction(function() {
        new Ajax.Updater(
                'lifecyclesDiv',
                '../lifecycles/lifecycles_ajaxprocessor.jsp',
        {
            method : 'post',
            parameters : {
                path : path
            },
            evalScripts : true,

            onSuccess : function(transport) {
                var newPath = transport.responseText;

                newPath = removeCarriageReturns(newPath);

                if (newPath.indexOf("/") > 0) {
                    refreshPropertiesSection(path);
                } else {
                    newPath = newPath.substring(0, newPath.indexOf("<"));
                    refreshPropertiesSection(newPath);

                }

                alternateTableRows('myTable', 'tableEvenRow', 'tableOddRow');

            },

            onFailure : function(transport) {
                showRegistryError(transport.responseText);
            }

        });
    }, org_wso2_carbon_governance_custom_lifecycles_checklist_ui_jsi18n["session.timed.out"]);
}

/*function refreshLifecyclesSection(path) {
    new Ajax.Request('../lifecycles/lifecycles_ajaxprocessor.jsp',
    {
        method: 'get',
        parameters: {path : path},

        onSuccess : function(transport) {
            $('lifecyclesDiv').innerHTML = transport.responseText;
            alternateTableRows('myTable', 'tableEvenRow', 'tableOddRow');

        },

        onFailure : function(transport) {
            showRegistryError(transport.responseText);
        }

    });
}*/

function refreshPropertiesSection(path) {
    sessionAwareFunction(function() {
        if (document.getElementById('propertiesDiv') != null) {
            new Ajax.Updater('propertiesDiv',
                    '../properties/properties-main-ajaxprocessor.jsp', {
                method : 'post',
                parameters : {
                    path : path
                }
            });
        }
    }, org_wso2_carbon_governance_custom_lifecycles_checklist_ui_jsi18n["session.timed.out"]);
}

function removeCarriageReturns(newPath) {
	return newPath.replace('\n', '', 'g');
}