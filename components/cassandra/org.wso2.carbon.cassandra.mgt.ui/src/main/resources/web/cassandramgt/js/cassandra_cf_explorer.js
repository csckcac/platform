function loadNext(rowID, keySpace, columnFamily) {

    var startKey = document.getElementById("hfEndKey_" + rowID).value;
    var endKey = "";
    var isReversed = "false";

    loadWithAJAX(keySpace, columnFamily, rowID, startKey, endKey, isReversed);
}

function loadPrevious(rowID, keySpace, columnFamily) {

    var startKey = "";
    var endKey = document.getElementById("hfStartKey_" + rowID).value;
    var isReversed = "false";

    loadWithAJAX(keySpace, columnFamily, rowID, startKey, endKey, isReversed);
}

function getXMLHttpRequestObject() {

    var xmlhttp;
    if (!xmlhttp && typeof XMLHttpRequest != 'undefined') {
        try {
            xmlhttp = new XMLHttpRequest();
        } catch (e) {
            xmlhttp = false;
        }
    }
    return xmlhttp;
}

function loadWithAJAX(keySpace, columnFamily, rowId, startKey, endKey, isReversed) {

    var http = new getXMLHttpRequestObject();

    var url = "caller-ajaxprocessor.jsp";
    var parameters = "keySpace=" + keySpace + "&columnFamily=" + columnFamily + "&rowId=" + rowId +
        "&startKey=" + startKey + "&endKey=" + endKey + "&isReversed=" + isReversed;
    http.open("POST", url, false);

    //Send the proper header information along with the request
    http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    http.setRequestHeader("Content-length", parameters.length);
    http.setRequestHeader("Connection", "close");

    http.onreadystatechange = function () {//Handler function for call back on state change.
        if (http.readyState == 4) {
            var responseText = "" + http.responseText + "";
            var responseJSON = JSON.parse(responseText.split("\n")[0]);
            updateRowTable(rowId, responseJSON);
        }
    }
    http.send(parameters);
}

function updateNextButtonStatusWithAJAX(keySpace, columnFamily, rowId, startKey, endKey, isReversed) {
    var http = new getXMLHttpRequestObject();

    var url = "caller-ajaxprocessor.jsp";
    var parameters = "keySpace=" + keySpace + "&columnFamily=" + columnFamily + "&rowId=" + rowId +
        "&startKey=" + startKey + "&endKey=" + endKey + "&isReversed=" + isReversed;
    http.open("POST", url, false);

    //Send the proper header information along with the request
    http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    http.setRequestHeader("Content-length", parameters.length);
    http.setRequestHeader("Connection", "close");

    http.onreadystatechange = function () {//Handler function for call back on state change.
        if (http.readyState == 4) {
            var responseText = "" + http.responseText + "";
            var responseJSON = JSON.parse(responseText.split("\n")[0]);
            if (responseJSON.length < 2) { // Disable Next button
                document.getElementById("CfExplorerTable_" + rowId).getElementsByTagName("a")[2].className = "disabled";
                document.getElementById("CfExplorerTable_" + rowId).getElementsByTagName("a")[2].setAttribute("onclick", "");
            }
            else { // Enable Next button
                document.getElementById("CfExplorerTable_" + rowId).getElementsByTagName("a")[2].className = "enabled";
                document.getElementById("CfExplorerTable_" + rowId).getElementsByTagName("a")[2].setAttribute("onclick", "loadNext('" + rowId + "','" + keySpace + "','" + columnFamily + "')");
            }
        }
    }
    http.send(parameters);
}

function updatePrevButtonStatusWithAJAX(keySpace, columnFamily, rowId, startKey, endKey, isReversed) {
    var http = new getXMLHttpRequestObject();

    var url = "caller-ajaxprocessor.jsp";
    var parameters = "keySpace=" + keySpace + "&columnFamily=" + columnFamily + "&rowId=" + rowId +
        "&startKey=" + startKey + "&endKey=" + endKey + "&isReversed=" + isReversed;
    http.open("POST", url, false);

    //Send the proper header information along with the request
    http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    http.setRequestHeader("Content-length", parameters.length);
    http.setRequestHeader("Connection", "close");

    http.onreadystatechange = function () {//Handler function for call back on state change.
        if (http.readyState == 4) {
            var responseText = "" + http.responseText + "";
            var responseJSON = JSON.parse(responseText.split("\n")[0]);
            if (responseJSON.length < 2) { // Disable Prev button
                document.getElementById("CfExplorerTable_" + rowId).getElementsByTagName("a")[1].className = "disabled";
                document.getElementById("CfExplorerTable_" + rowId).getElementsByTagName("a")[1].setAttribute("onclick", "");
            }
            else { // Enable Prev button
                document.getElementById("CfExplorerTable_" + rowId).getElementsByTagName("a")[1].className = "enabled";
                document.getElementById("CfExplorerTable_" + rowId).getElementsByTagName("a")[1].setAttribute("onclick", "loadPrevious('" + rowId + "','" + keySpace + "','" + columnFamily + "')");
            }
        }
    }
    http.send(parameters);
}

function updateRowTable(rowId, responseJSON) {

    var tableId = "CfExplorerTable_" + rowId;
    var table = document.getElementById(tableId);

    var tableHeader = table.rows[0]; // For filling the header of the table
    var tableBody = table.rows[1]; // For filling the body of the table

    // Fill data from response
    for (var i = 0; i < responseJSON.length; i++) {
        tableHeader.cells[i + 1].firstChild.data = responseJSON[i].name;
        tableBody.cells[i + 1].firstChild.data = responseJSON[i].value;
    }

    // Fill empty strings
    for (var i = responseJSON.length; i < tableHeader.cells.length - 2; i++) {
        // 2 is used, as there is an extra column for buttons
        tableHeader.cells[i + 1].firstChild.data = "";
        tableBody.cells[i + 1].firstChild.data = "";
    }

    updateNewVariables(rowId, responseJSON[0].name, responseJSON[responseJSON.length - 1].name);
    updateButtonStatus(rowId);
}

function updateNewVariables(rowId, firstKey, lastKey) {
    document.getElementById("hfStartKey_" + rowId).value = firstKey;
    document.getElementById("hfEndKey_" + rowId).value = lastKey;
}

function loadPreviousRows(keySpace, columnFamily) {

    var startRowKey = document.getElementById("hfStartRowKey").value;
    var navigationDirection = "prev";
    var pageSize = document.getElementById("ddlPageSize").value;

    location.href = 'cf_explorer.jsp?keyspace=' + keySpace + "&columnFamily=" + columnFamily +
        "&startRowKey=" + startRowKey + "&naviDirection=" + navigationDirection +
        "&pageSize=" + pageSize;
}

function loadNextRows(keySpace, columnFamily) {

    var startRowKey = document.getElementById("hfEndRowKey").value;
    var navigationDirection = "next";
    var pageSize = document.getElementById("ddlPageSize").value;

    location.href = 'cf_explorer.jsp?keyspace=' + keySpace + "&columnFamily=" + columnFamily +
        "&startRowKey=" + startRowKey + "&naviDirection=" + navigationDirection +
        "&pageSize=" + pageSize;
}

function loadPageSize(keySpace, columnFamily) {

    var pageSize = document.getElementById("ddlPageSize").value;

    location.href = 'cf_explorer.jsp?keyspace=' + keySpace + "&columnFamily=" + columnFamily +
        "&pageSize=" + pageSize;
}

function viewExplorer(keyspace, index) {
    location.href = 'cf_explorer.jsp?keyspace=' + keyspace + "&columnFamily=" +
        document.getElementById("cfName" + index).value;
}

function getDataForRow(keyspace, cf, rowID) {
    location.href = 'cf_explorer.jsp?keyspace=' + keyspace + "&columnFamily=" + cf + "&rowID=" + rowID;
}

function getDataForColumn(keyspace, cf, rowID, columnKey) {
    location.href = 'row_explorer.jsp?keyspace=' + keyspace + "&columnFamily=" + cf + "&rowID=" + rowID +
        "&columnKey=" + columnKey;
}

function getDataPageForRow(keyspace, cf, rowID) {
    location.href = 'row_explorer.jsp?keyspace=' + keyspace + "&columnFamily=" + cf + "&rowID=" + rowID;
}

function reloadDataTable(keyspace, cf) {
    location.href = 'cf_explorer.jsp?keyspace=' + keyspace + "&columnFamily=" + cf;

}