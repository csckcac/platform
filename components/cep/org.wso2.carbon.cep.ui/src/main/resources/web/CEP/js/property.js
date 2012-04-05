 
function isRemainPropertyExpressions() {
    var nsCount = document.getElementById("propertyCount");
    var i = nsCount.value;

    var currentCount = parseInt(i);

    if (currentCount >= 1) {
        for (var k = 0; k < currentCount; k++) {
            var propertyType = getSelectedValue('propertyTypeSelection' + k);
            if ("expression" == propertyType) {
                return true;
            }
        }
    }
    return false;
}
 function resetDisplayStyle(displayStyle) {
     document.getElementById('ns-edior-th').style.display = displayStyle;
     var nsCount = document.getElementById("propertyCount");
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
 function addproperty(name,nameemptymsg, valueemptymsg) {

     if (!isValidProperties(nameemptymsg, valueemptymsg)) {
         return false;
     }
     var propertyCount = document.getElementById("propertyCount");
     var i = propertyCount.value;

     var currentCount = parseInt(i);
     currentCount = currentCount + 1;

     propertyCount.value = currentCount;

     var propertytable = document.getElementById("propertytable");
     propertytable.style.display = "";
     var propertytbody = document.getElementById("propertytbody");

     var propertyRaw = document.createElement("tr");
     propertyRaw.setAttribute("id", "propertyRaw" + i);

     var nameTD = document.createElement("td");
     nameTD.innerHTML = "<input type='text' name='propertyName" + i + "' id='propertyName" + i + "'" +
                        " />";

     var valueTD = document.createElement("td");
     valueTD.innerHTML = "<input type='text' name='propertyValue" + i + "' id='propertyValue" + i + "'" +
                         " class='esb-edit small_textbox' />";

     var deleteTD = document.createElement("td");
     deleteTD.innerHTML =  "<a href='#' class='delete-icon-link' onclick='deleteproperty(" + i + ")'>Delete</a>";

     propertyRaw.appendChild(nameTD);
     propertyRaw.appendChild(valueTD);
     propertyRaw.appendChild(deleteTD);

     propertytbody.appendChild(propertyRaw);
     return true;
 }

 function isValidProperties(nameemptymsg, valueemptymsg) {

     var nsCount = document.getElementById("propertyCount");
     var i = nsCount.value;

     var currentCount = parseInt(i);

     if (currentCount >= 1) {
         for (var k = 0; k < currentCount; k++) {
             var prefix = document.getElementById("propertyName" + k);
             if (prefix != null && prefix != undefined) {
                 if (prefix.value == "") {
                     CARBON.showWarningDialog(nameemptymsg)
                     return false;
                 }
             }
             var uri = document.getElementById("propertyValue" + k);
             if (uri != null && uri != undefined) {
                 if (uri.value == "") {
                     CARBON.showWarningDialog(valueemptymsg)
                     return false;
                 }
             }
         }
     }
     return true;
 }
 function deleteproperty(i) {
    var propRow = document.getElementById("propertyRaw" + i);
    if (propRow != undefined && propRow != null) {
        var parentTBody = propRow.parentNode;
        if (parentTBody != undefined && parentTBody != null) {
            parentTBody.removeChild(propRow);
            if (!isContainRaw(parentTBody)) {
                var propertyTable = document.getElementById("propertytable");
                propertyTable.style.display = "none";
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


 


 