/*
log.js contains scripts need to handle log information.
*/

function viewSingleLogLevel() {
    var loglevel = document.getElementById("logLevelID");
	var tenantDomain = document.getElementById("tenantDomain").value;
	var serviceName = document.getElementById("serviceName").value;
	var logFile = document.getElementById("logFile").value;
    var loglevel_index = null;
    var loglevel_value = null;
    if (loglevel != null)
    {
        loglevel_index = loglevel.selectedIndex;
        if (loglevel_index != null) {
            loglevel_value = loglevel.options[loglevel_index].value;
        }
    }
    if (loglevel_value != null && loglevel_value != "") {
        location.href = "syslog_index.jsp?type=" + loglevel_value+"&logFile="+logFile+"&tenantDomain="+tenantDomain+"&serviceName="+serviceName;
    } else {
        return;
    }

}

function getTenantSpecificIndex () {
	var tenantDomain = document.getElementById("tenantDomain").value;
	var serviceName = document.getElementById("serviceName").value;
	location.href = "syslog_index.jsp?tenantDomain="+tenantDomain+"&serviceName="+serviceName;
}

function getProductTenantSpecificIndex() {
	var tenantDomain = document.getElementById("tenantDomain").value;
	var serviceName = document.getElementById("serviceName").value;
	location.href = "syslog_index.jsp?tenantDomain="+tenantDomain+"&serviceName="+serviceName;
}

function submitenter(e)
{
    var keycode;
    if (window.event) {
        keycode = window.event.keyCode;
    }
    else if (e) {
        keycode = e.which;
    }
    if (keycode == 13)
    {
    	searchTenantLog();
        return true;
    }
    else {
        return true;
    }
}

function submitenterbottomUp(e)
{
    var keycode;
    if (window.event) {
        keycode = window.event.keyCode;
    }
    else if (e) {
        keycode = e.which;
    }
    if (keycode == 13)
    {
    	searchLogBottomLogs();
        return true;
    }
    else {
        return true;
    }
}

function isNumeric(str)
{
	var validChars = "0123456789";
	var isNumber = true;
	var char;
	for (i = 0; i < str.length && isNumber == true; i++) {
		char = str.charAt(i);
		if (validChars.indexOf(char) == -1) {
			isNumber = false;
		}
	}
	return isNumber;
}


function searchLogBottomLogs() {
	var logFile = document.getElementById("logFile").value;
	var log_index = document.getElementById("logIndex").value;
	var loglevel = document.getElementById("logLevelID");
	var serviceName = document.getElementById("serviceName").value;
	var loglevel_index = null;
	var loglevel_value = null;
	var tenantDomain = document.getElementById("tenantDomain").value;
    if(log_index == ''){
        CARBON.showWarningDialog('Head index cannot be empty');
        return false;
    }
    if(! isNumeric(log_index)) {
    	 CARBON.showWarningDialog('Enter non negative numeric values for log head');
         return false;
    }
    if(log_index < 1){
    	CARBON.showWarningDialog('Log index should be between 1 and 10000000');
        return false;
    }
    if(log_index > 10000000){
        CARBON.showWarningDialog('Log index should be between 1 and 10000000');
        return false;
    }
	if (loglevel != null) {
		loglevel_index = loglevel.selectedIndex;
		if (loglevel_index != null) {
			loglevel_value = loglevel.options[loglevel_index].value;
			if (loglevel_value == "Custom") {
				loglevel_value = "ALL";
			}
		}
	}
	var keyword = document.getElementById("keyword");
	if (keyword != null && keyword != undefined && keyword.value != null
			&& keyword.value != undefined) {
		if (keyword.value == "") {
			location.href = "view.jsp?type=ALL&logIndex=" + log_index+"&logFile="+logFile+"&tenantDomain="+tenantDomain+"&serviceName="+serviceName;
		} else {
			location.href = "view.jsp?type=" + loglevel_value + "&keyword="
					+ keyword.value+"&logIndex=" + log_index+"&logFile="+logFile+"&tenantDomain="+tenantDomain+"&serviceName="+serviceName;
		}
	} else {
		return;
	}
}

function searchLog() {
    var loglevel = document.getElementById("logLevelID");
	var logFile = document.getElementById("logFile").value;
	var serviceName = document.getElementById("serviceName").value;
    var loglevel_index = null;
    var loglevel_value = null;
	var tenantDomain = document.getElementById("tenantDomain").value;
    if (loglevel != null)
    {
        loglevel_index = loglevel.selectedIndex;
        if (loglevel_index != null) {
            loglevel_value = loglevel.options[loglevel_index].value;
        }
    }
    var keyword = document.getElementById("keyword");
    if (keyword != null && keyword != undefined && keyword.value != null && keyword.value != undefined) {
        if (keyword.value == "") {
            location.href = "syslog_index.jsp?type=ALL"+"&logFile="+logFile+"&tenantDomain="+tenantDomain;
        } else {
            location.href = "syslog_index.jsp?type=" + loglevel_value + "&keyword=" + keyword.value+"&logFile="+logFile+"&tenantDomain="+tenantDomain+"&serviceName="+serviceName;
        }
    } else {
        return;
    }
}

function clearLogEntries(message) {
    CARBON.showConfirmationDialog(message, function() {
        location.href = "syslog_index.jsp?action=clear-logs";
    });
}

function viewSingleSysLogLevel() {
	var loglevel = document.getElementById("logLevelID");
	var serviceName = document.getElementById("serviceName").value;
	var logFile = document.getElementById("logFile").value;
	var tenantDomain = document.getElementById("tenantDomain").value;
	var loglevel_index = null;
	var loglevel_value = null;
	if (loglevel != null) {
		loglevel_index = loglevel.selectedIndex;
		if (loglevel_index != null) {
			loglevel_value = loglevel.options[loglevel_index].value;
		}
	}
	if (loglevel_value != null && loglevel_value != "") {
		location.href = "view.jsp?type=" + loglevel_value+"&logFile="+logFile+"&tenantDomain="+tenantDomain+"&serviceName="+serviceName;
	} else {
		return;
	}
}

function clearProperties() {
	document.getElementById("logIndex").value = "";
	document.getElementById("type").value="ALL";
	document.getElementById("keyword").value="";
}

function searchTenantLog() {
	var loglevel = document.getElementById("logLevelID");
	var serviceName = document.getElementById("serviceName").value;
	var logFile = document.getElementById("logFile").value;
	var tenantDomain = document.getElementById("tenantDomain").value;
	var loglevel_index = null;
	var loglevel_value = null;
	if (loglevel != null) {
		loglevel_index = loglevel.selectedIndex;
		if (loglevel_index != null) {
			loglevel_value = loglevel.options[loglevel_index].value;
			if (loglevel_value == "Custom") {
				loglevel_value = "ALL";
			}
		}
	}
	var keyword = document.getElementById("keyword");
	if (keyword != null && keyword != undefined && keyword.value != null
			&& keyword.value != undefined) {
		if (keyword.value == "") {
			location.href = "view.jsp?type=ALL"+"&logFile="+logFile+"&tenantDomain="+tenantDomain+"&serviceName="+serviceName;
		} else {
			location.href = "view.jsp?type=" + loglevel_value
					+ "&keyword=" + keyword.value+"&logFile="+logFile+"&tenantDomain="+tenantDomain+"&serviceName="+serviceName;
		}
	} else {
		return;
	}
}
    
function getFilteredLogs() {
	
	var loglevel = document.getElementById("logLevelID");
	var log_index = document.getElementById("logIndex").value;

	var time = document.getElementById("time").value;
	var date = document.getElementById("date").value;

	var serviceName = null;
	var tenantDomain = null;
	 // regular expression to match required date format 
	var reTdate = /^\d{1,2}\/\d{1,2}\/\d{4}$/; 
	if(date != '' && !date.match(reTdate)) {  
		CARBON.showWarningDialog('Invalid Date Format');
    	return false; 
    } 
	// regular expression to match required time format 
	var reTime = /^\d{1,2}:\d{2}([ap]m)?$/; 
	if(time != '' && !time.match(reTime)) {  
		CARBON.showWarningDialog('Invalid Time Format');
    	return false; 
    } 
	if (document.getElementById("serviceName") != null) {
		serviceName = document.getElementById("serviceName").value
	}
	if (document.getElementById("tenantDomain") != null) {
		tenantDomain = document.getElementById("tenantDomain").value
	}
	var loglevel_index = null;
	var loglevel_value = null;
	
	if (loglevel != null) {
		loglevel_index = loglevel.selectedIndex;
		if (loglevel_index != null) {
			loglevel_value = loglevel.options[loglevel_index].value;
			if (loglevel_value == "Custom") {
				loglevel_value = "ALL";
			}
		}
	}
	var keyword = document.getElementById("keyword").value;
	var logger = document.getElementById("logger").value;


	location.href = "cassandra_log_viewer.jsp?priority=" + loglevel_value + "&keyword="
	+ keyword+"&logIndex=" + log_index+"&tenantDomain="+tenantDomain+"&serviceName="+serviceName+"&logger="+logger+"&time="+time+"&date="+date;
}

function showQueryProperties() {
    var propertyTab = document.getElementById('propertyTable');
    var propertySymbolMax =  document.getElementById('propertySymbolMax');
    if(propertyTab.style.display == 'none') {
        propertyTab.style.display = '';
        propertySymbolMax.setAttribute('style','background-image:url(images/minus.gif);');
    } else {
        propertyTab.style.display = 'none';
        propertySymbolMax.setAttribute('style','background-image:url(images/plus.gif);');
    }
}
function showTrace(obj) {
    var traceTab = document.getElementById('traceTable'+obj);
    var traceSymbolMax =  document.getElementById('traceSymbolMax'+obj);
    if(traceTab.style.display == 'none') {
        traceTab.style.display = '';
        traceSymbolMax.setAttribute('style','background-image:url(images/minus.gif);');
    } else {
        traceTab.style.display = 'none';
        traceSymbolMax.setAttribute('style','background-image:url(images/plus.gif);');
    }
}
