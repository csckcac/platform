function displayGet(data){
    var xmlNode = data.xmlNode;
    var element = document.createElement("div");
    element.className = "customForms";
    $.ajax({
        async:false,
        url:'../analyzer/override-uis/get-ui_ajaxprocessor.jsp',
        success:function(data) {
            element.innerHTML = data;
        }
    });

    if(xmlNode.getAttribute('name') != undefined && xmlNode.getAttribute('name') != null){
        $('#getName',element)[0].value = xmlNode.getAttribute('name');
    }
    if(xmlNode.getAttribute('batchSize') != undefined && xmlNode.getAttribute('batchSize') != null){
        $('#batchSize',element)[0].value = xmlNode.getAttribute('batchSize');
    }

    var graCheck = $('#granularity_checkbox',element)[0];
    var graForm = $('#granularityForm',element)[0];
    var groupCheck = $('#groupBy_checkbox',element)[0];
    var groupForm = $('#groupByForm',element)[0];

    if(xmlNode.getElementsByTagName('granularity').length > 0){
        graCheck.checked = true;
        $(graForm).show();
        var graNode = xmlNode.getElementsByTagName('granularity')[0];
        $('#granularityIndex',element)[0].value = graNode.getAttribute('index');
        $('#granularityType',element)[0].value = graNode.getAttribute('type');

    }else if(xmlNode.getElementsByTagName('groupBy').length > 0){
        groupCheck.checked = true;
        $(groupForm).show();        
         var gbNode = xmlNode.getElementsByTagName('granularity')[0];
        $('#granularityIndex',element)[0].value = gbNode.getAttribute('index');
    }


    var toggle_Optional_Forms = function(){
        if(graCheck.checked){
            if(groupCheck.checked){
                if(this.id == graCheck.id){
                    groupCheck.checked = false;
                    $(graForm).show();
                    $(groupForm).hide();
                }else{
                    graCheck.checked = false;
                    $(graForm).hide();
                    $(groupForm).show();
                }
            }else{
                $(graForm).show();
                $(groupForm).hide();
            }
            //show the granularity form
        }else{
            if(groupCheck.checked){
                //show the groupby form
                $(graForm).hide();
                $(groupForm).show();
            }else{
                //hide all forms
                $(graForm).hide();
                $(groupForm).hide();
            }
        }
    };

    $(graCheck).click(toggle_Optional_Forms);
    $(groupCheck).click(toggle_Optional_Forms);



    var addIndexBtn = $('#addIndexBtn',element)[0];
    var indexFormTable = $('#indexFormTable',element)[0];
    var indexForm = $('#indexForm',element)[0];

    //Handling the index add event
    $(addIndexBtn).click(
            function(){
                $(indexForm).show();                
                if(indexFormTable.rows.length == 0){
                    var newTr = document.createElement("tr");
                    newTr.innerHTML = '<td>name</td><td>start</td><td>end</td>';
                    indexFormTable.appendChild(newTr);

                    newTr = document.createElement("tr");
                    newTr.innerHTML += '<td><input type="text" name="name" value="" /></td>';
                    newTr.innerHTML += '<td><input type="text" name="start" value="" /></td>';
                    newTr.innerHTML += '<td><input type="text" name="end" value="" /></td>';
                    indexFormTable.appendChild(newTr);
                }
            }
    );

    var indexElements = xmlNode.getElementsByTagName('index');
    if(indexElements.length > 0){
        $(indexForm).show(); 
        var newTr = document.createElement("tr");
        newTr.innerHTML = '<td>name</td><td>start</td><td>end</td>';
        indexFormTable.appendChild(newTr);
        for(var i = 0;i<indexElements.length;i++){
            newTr = document.createElement("tr");
            newTr.className = 'indexElements';
            newTr.innerHTML += '<td><input type="text" name="name" value="'+indexElements[i].getAttribute('name')+'" /></td>';
            newTr.innerHTML += '<td><input type="text" name="start" value="'+indexElements[i].getAttribute('start')+'" /></td>';
            newTr.innerHTML += '<td><input type="text" name="end" value="'+indexElements[i].getAttribute('end')+'" /></td>';
            indexFormTable.appendChild(newTr);
        }
    }

    return element;
}
function displayDrop(data){
    var xmlNode = data.xmlNode;
    var element = document.createElement("div");
    element.className = "customForms";
    $.ajax({
        async:false,
        url:'../analyzer/override-uis/drop-ui_ajaxprocessor.jsp',
        success:function(data) {
            element.innerHTML = data;            
        }
    });
    var dropTypeSelect = $('#dropTypeSelect',element)[0];
    var type = xmlNode.getAttribute('type');

    for(var i=0;i<dropTypeSelect.options.length;i++){
        if(xmlNode.getAttribute('type') == dropTypeSelect.options[i].value){
            dropTypeSelect.options[i].selected = true;
        }
    }

     if(type == 'group'){
        $('#groupSetForm',element).show();
        $('#fieldSetFormRow',element).hide();
         $('#fieldSetFormColumn',element).hide();
         var dropGroupTable = $('#dropGroupTable',element)[0];
         dropGroupTable.innerHTML = "";
         if(xmlNode.getElementsByTagName("groupSet")[0] != null){
             var groups = xmlNode.getElementsByTagName("group");
             for(var i=0;i<groups.length;i++){
                var newRow = document.createElement('tr');
                var td1 = document.createElement('td');
                var td2 = document.createElement('td');
                td1.innerHTML = 'RegEx';
                 if(groups[i].getAttribute("regex")!=null){
                    td2.innerHTML = '<input type="text" name="regex" value="'+groups[i].getAttribute("regex")+'">';
                 }

                newRow.appendChild(td1);
                newRow.appendChild(td2);
                dropGroupTable.appendChild(newRow);
             }
         }
    }else if(type == 'row'){
         $('#groupSetForm',element).hide();
         $('#fieldSetFormRow',element).show();
           $('#fieldSetFormColumn',element).hide();
         var dropFieldTable = $('#dropFieldTable', element)[0];
         dropFieldTable.deleteRow(1);
         if (xmlNode.getElementsByTagName("fieldSet")[0] != null) {
             var matchUsing = xmlNode.getElementsByTagName("fieldSet")[0].getAttribute('matchUsing');
             var matchUsingSelector = $('#matchUsingSelector',element)[0];
            for(var i=0;i<matchUsingSelector.options.length;i++){
                if(matchUsing == matchUsingSelector.options[i].value){
                    matchUsingSelector.options[i].selected = true;
                }
            }
             var groups = xmlNode.getElementsByTagName("field");
             for (var i = 0; i < groups.length; i++) {
                 var newRow = document.createElement('tr');
                 var td1 = document.createElement('td');
                 var td2 = document.createElement('td');
                 td1.innerHTML = '<input type="text" class="required" name="name" value="'+groups[i].getAttribute("name")+'">';
                 td2.innerHTML = '<input type="text" name="regex" value="'+groups[i].getAttribute("regex")+'">';

                 newRow.appendChild(td1);
                 newRow.appendChild(td2);
                 dropFieldTable.appendChild(newRow);
             }
         }

    }else if(type == 'column') {
         $('#groupSetForm',element).hide();
         $('#fieldSetFormColumn',element).show();
         $('#fieldSetFormRow',element).hide;
         var dropFieldTable = $('#dropFieldTableColumn', element)[0];
         dropFieldTable.deleteRow(1);
         if (xmlNode.getElementsByTagName("fieldSet")[0] != null) {
             var groups = xmlNode.getElementsByTagName("field");
             for (var i = 0; i < groups.length; i++) {
                 var newRow = document.createElement('tr');
                 var td1 = document.createElement('td');
                 var td2 = document.createElement('td');
                 td1.innerHTML = '<input type="text" class="required" name="name" value="'+groups[i].getAttribute("name")+'">';
                 td2.innerHTML = '<input type="text" name="regex" value="'+groups[i].getAttribute("regex")+'">';

                 newRow.appendChild(td1);
                 newRow.appendChild(td2);
                 dropFieldTable.appendChild(newRow);
             }
         }
     }
     else{
        $('#groupSetForm',element).show();
        $('#fieldSetFormColumn',element).hide();
         $('#fieldSetFormRow',element).hide();
     }

    return element;
}
function updateDropUI(selectElement){
    var type = selectElement.options[selectElement.selectedIndex].value;
    if(type == 'group'){
        $('#groupSetForm').show();
        $('#fieldSetFormRow').hide();
         $('#fieldSetFormColumn').hide();
    }else if(type == 'row'){
        $('#groupSetForm').hide();
        $('#fieldSetFormRow').show();
         $('#fieldSetFormColumn').hide();
    }else if (type == 'column'){
        $('#groupSetForm').hide();
        $('#fieldSetFormRow').hide();
         $('#fieldSetFormColumn').show();
    }
}
function addGroupElement(){
    var table = document.getElementById('dropGroupTable');
    var newRow = document.createElement('tr');
    var td1 = document.createElement('td');
    var td2 = document.createElement('td');
    td1.innerHTML = 'RegEx';
    td2.innerHTML = '<input type="text" name="regex">';

    newRow.appendChild(td1);
    newRow.appendChild(td2);
    table.appendChild(newRow);
}
function addFieldElementRow(){
    var table = document.getElementById('dropFieldTable');
    var newRow = document.createElement('tr');
    var td1 = document.createElement('td');
    var td2 = document.createElement('td');
    td1.innerHTML = '<input type="text" class="required" name="name">';
    td2.innerHTML = '<input type="text" name="regex">';

    newRow.appendChild(td1);
    newRow.appendChild(td2);
    table.appendChild(newRow);
}

function addFieldElementColumn(){
    var table = document.getElementById('dropFieldTableColumn');
    var newRow = document.createElement('tr');
    var td1 = document.createElement('td');
    var td2 = document.createElement('td');
    td1.innerHTML = '<input type="text" class="required" name="name">';
    td2.innerHTML = '<input type="text" name="regex">';

    newRow.appendChild(td1);
    newRow.appendChild(td2);
    table.appendChild(newRow);
}