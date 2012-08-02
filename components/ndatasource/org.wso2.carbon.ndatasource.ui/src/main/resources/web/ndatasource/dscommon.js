var perviousICFac = '';
var perviousProviderURL = '';
var perviousProviderPort = '';


function addServiceParamRow(key, value, table, delFunction) {
    addRowForSP(key, value, table, delFunction);
 }

function addRowForSP(prop1, prop2, table, delFunction) {
    var tableElement = document.getElementById(table);
    var param1Cell = document.createElement('td');
    var inputElem = document.createElement('input');
    inputElem.type = "text";
    inputElem.name = "spName";
    inputElem.value = prop1;
    param1Cell.appendChild(inputElem); //'<input type="text" name="spName" value="'+prop1+' />';


    var param2Cell = document.createElement('td');
    inputElem = document.createElement('input');
    inputElem.type = "text";
    inputElem.name = "spValue";
    inputElem.value = prop2;
    param2Cell.appendChild(inputElem);

    var delCell = document.createElement('td');
    delCell.innerHTML='<a id="deleteLink" href="#" onClick="' + delFunction + '(this.parentNode.parentNode.rowIndex)" alt="Delete" class="icon-link" style="background-image:url(../admin/images/delete.gif);">Delete</a>';

    var rowtoAdd = document.createElement('tr');
    rowtoAdd.appendChild(param1Cell);
    rowtoAdd.appendChild(param2Cell);
    rowtoAdd.appendChild(delCell);

    tableElement.tBodies[0].appendChild(rowtoAdd);
    tableElement.style.display = "";

    alternateTableRows(tableElement, 'tableEvenRow', 'tableOddRow');
}

function showPropConfigurations() {
	  var pwdMngrSymbolMax =  document.getElementById('dsProperties');
	  var configFields = document.getElementById('dsPropFields');
	  if(configFields.style.display == 'none') {
	    pwdMngrSymbolMax.setAttribute('style','background-image:url(images/minus.gif);');
	    configFields.style.display = '';
	  } else {
	      pwdMngrSymbolMax.setAttribute('style','background-image:url(images/plus.gif);');
	      configFields.style.display = 'none';
	  }
}

function showJNDIConfigurations() {
	var pwdMngrSymbolMax =  document.getElementById('jndiconfigheader');
	var configFields = document.getElementById('jndiconfig');
	if(configFields.style.display == 'none') {
	   pwdMngrSymbolMax.setAttribute('style','background-image:url(images/minus.gif);');
	   configFields.style.display = '';
	} else {
	   pwdMngrSymbolMax.setAttribute('style','background-image:url(images/plus.gif);');
	   configFields.style.display = 'none';
	}
}

function isDSValid(namemsg, invalidnamemsg, drivermsg, urlmsg) {

    var name = document.getElementById('dsName').value;
    if (name == null || name == '') {
        CARBON.showWarningDialog(namemsg);
        return false;
    }

    var iChars = "!@#$%^&*()+=[]\\\';,/{}|\":<>?";
    for (var i = 0; i < name.length; i++) {
        if (iChars.indexOf(name.charAt(i)) != -1) {
            CARBON.showWarningDialog(invalidnamemsg);
            return false;
        }
    }

    return true;
}

function clearStatus(id) {
    var textbox = document.getElementById(id);
    var textValue = textbox.value;
    if (textValue.indexOf('int') >= 0 || textValue.indexOf('long') >= 0){
      textbox.value = '';
    } 
    return true;
}

function forward(destinationJSP) {
    location.href = destinationJSP;
}

function deleteRow(name, msg) {
    CARBON.showConfirmationDialog(msg + "' " + name + " ' ?", function() {
        document.location.href = "deletedatasource.jsp?" + "name=" + name;
    });
}

function editRow(name) {

    document.location.href = "newdatasource.jsp?" + "dsName=" + name + "&edit=true";
}

function goBackOnePage(){
     history.go(-1);
}

function testConnection(namemsg, invalidnamemsg, drivermsg, urlmsg, validquerymsg, succcessmsg) {

    if (!isDSValid(namemsg, invalidnamemsg, drivermsg, urlmsg)) {
        return false;
    }

    if (trim(document.getElementById('validationquery').value) == '') {
        CARBON.showConfirmationDialog(validquerymsg, function () {
            doTestConnection(succcessmsg);
        });
    } else {
        doTestConnection(succcessmsg);
    }
    return false;
}

function doTestConnection(successmsg) {
	if (document.getElementById("jndiPropertyTable") != null) {
    	extractJndiProps();
    } 
    if (document.getElementById("dsPropertyTable") != null) {
    	extractDataSourceProps();
    }
	var query = document.getElementById('dsName').value;
	var dsProvider = document.getElementById('dsProviderType').value;
	if (dsProvider == 'default') {
		var driver = document.getElementById('driver').value;
		var url = document.getElementById('url').value;
		var username = document.getElementById('username').value;
		var password = document.getElementById('password').value;
	} else {
		var dsclassname = document.getElementById('dsclassname').value;
		var dsproviderProperties = document.getElementById('dsproviderProperties').value;
	}
	var requestUrl = '../ndatasource/validateconnection-ajaxprocessor.jsp?&dsName=' + document.getElementById('dsName').value+'&dsProviderType='+dsProvider+
    	'&dsclassname='+dsclassname+'&dsclassname='+dsclassname+'&dsproviderProperties='+dsproviderProperties+'&driver='+driver+
    	'&url='+url+'&username='+username+'&password='+password;
    jQuery.post(requestUrl, ({}),
            function(data, status) {
                if (status != "success") {
                    CARBON.showWarningDialog("Error Occurred!");
                } else {
                    var returnValue = trim(data);
                    if (returnValue != null && returnValue != undefined && returnValue != "" && returnValue != "true") {
                        CARBON.showErrorDialog(returnValue);
                        return false;
                    } else {
                       CARBON.showInfoDialog(successmsg);
                        return false;
                    }
                }
            });
}

function trim(stringValue) {
      return stringValue.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
}

function ValidateProperties() {
	if (document.getElementById("maxActive").value < 0) {
		CARBON.showErrorDialog("Please enter a positive value for maxActive");
		return false;
	}
	if (document.getElementById("maxIdle").value < 0) {
		CARBON.showErrorDialog("Please enter a positive value for maxIdle");
		return false;
	}
	if (document.getElementById("minIdle").value < 0) {
		CARBON.showErrorDialog("Please enter a positive value for minIdle");
		return false;
	}
	if (document.getElementById("initialSize").value < 0) {
		CARBON.showErrorDialog("Please enter a positive value for initialSize");
		return false;
	}
	if (document.getElementById("maxWait").value < 0) {
		CARBON.showErrorDialog("Please enter a positive value for maxWait");
		return false;
	}
	if (document.getElementById("timeBetweenEvictionRunsMillis").value < 0) {
		CARBON.showErrorDialog("Please enter a positive value for timeBetweenEvictionRunsMillis");
		return false;
	}
	if (document.getElementById("numTestsPerEvictionRun").value < 0) {
		CARBON.showErrorDialog("Please enter a positive value for numTestsPerEvictionRun");
		return false;
	}
	if (document.getElementById("minEvictableIdleTimeMillis").value < 0) {
		CARBON.showErrorDialog("Please enter a positive value for minEvictableIdleTimeMillis");
		return false;
	}
	if (document.getElementById("removeAbandonedTimeout").value < 0) {
		CARBON.showErrorDialog("Please enter a positive value for removeAbandonedTimeout");
		return false;
	}
	if (document.getElementById("validationInterval").value < 0) {
		CARBON.showErrorDialog("Please enter a positive value for validationInterval");
		return false;
	}
	if (document.getElementById("abandonWhenPercentageFull").value < 0) {
		CARBON.showErrorDialog("Please enter a positive value for abandonWhenPercentageFull");
		return false;
	}
	if (document.getElementById("maxAge").value < 0) {
		CARBON.showErrorDialog("Please enter a positive value for maxAge");
		return false;
	}
	if (document.getElementById("suspectTimeout").value < 0) {
		CARBON.showErrorDialog("Please enter a positive value for suspectTimeout");
		return false;
	}
	return true;
}

