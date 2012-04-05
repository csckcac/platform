   var row = 0;
    //add a new row to the table
    function addRow() {

        if (row == 0) {
            row = document.getElementById('eventingTbl').rows.length- 4;
        }

        row++;

        //add a row to the rows collection and get a reference to the newly added row
        var newRow = document.getElementById("eventingTbl").insertRow(4);
        newRow.id = 'file' + row;

        var oCell = newRow.insertCell(-1);
        oCell.innerHTML = "<td>"+jsi18n["js.subscription.epr"]+"</td>";

        oCell = newRow.insertCell(-1);
        var sub="<td class='leftCol-small'>"+jsi18n["js.subscription.epr"]+"<span class='required'>*</span></td>";
        oCell.innerHTML ="<input  style='width:250px;' id='tmp' type='text' value='' name='subscriptionEpr' class='eprClass'/><input id='bt' type='button' width='20px' class='button' value=' - ' onclick=\"deleteRow('file" + row + "');\" />";

        var input = document.getElementById('tmp');
        input.name = "subscriptionEpr" + row;
        input.id = 'id' + row;

        var btInput = document.getElementById('bt');
        btInput.id = 'bt' + row;

        alternateTableRows('eventingTbl', 'tableEvenRow', 'tableOddRow');

    }

    function deleteRow(rowId) {
        var tableRow = document.getElementById(rowId);
        //compactRows(rowId);
        tableRow.parentNode.deleteRow(tableRow.rowIndex);
        alternateTableRows('eventingTbl', 'tableEvenRow', 'tableOddRow');
    }

function registerCount() {
        var field = document.getElementById('eprCount');
        field.value = document.getElementById('eventingTbl').rows.length - 4;
        if(validate()){
            document.configForm.submit();
        }else{
            CARBON.showErrorDialog("Empty EPR not allowed.");
        }
    }

    function validateEprField(){
        var valid = true;
        var elms = YAHOO.util.Dom.getElementsByClassName('eprClass', 'input',document.getElementById('eventingTbl'));
        for(var i=0;i<elms.length;i++){
            if(elms[i].value == ""){
                valid = false;
            }
        }
        return valid;
    }


