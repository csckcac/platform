function saveGet(data){
    var configNode = data.configNode;
    var xmlNode = data.xmlNode;
    var tree = data.tree;
    var allChildren = tree.config.removeCluter(xmlNode.childNodes);
    for(var i=0;i<allChildren.length;i++){
        xmlNode.removeChild(allChildren[i]);
    }
    //update main attributes
    updateAttributesFromUI(xmlNode,$('#mainAttributeTable'));

    //Add the granularity or groupBy node base on the check boxes
    var granularityForm  = $('#granularityForm');
    var groupByForm  = $('#groupByForm');

    if($('#granularityForm').is(':visible')){
        var xmlElement = tree.xml.createElement('granularity');
        updateAttributesFromUI(xmlElement,$('#granularityForm'));
        xmlNode.appendChild(xmlElement);
    }else if($('#groupByForm').is(':visible')){
         xmlElement = tree.xml.createElement('groupBy');
        updateAttributesFromUI(xmlElement,$('#groupByForm'));
        xmlNode.appendChild(xmlElement);
    }

    //Adding index elements if they exists
    $('.indexElements').each(
            function(){
                var xmlElement = tree.xml.createElement('index');
                updateAttributesFromUI(xmlElement,this);
                xmlNode.appendChild(xmlElement);
            }
     );
}
function saveDrop(data){
     var configNode = data.configNode;
    var xmlNode = data.xmlNode;
    var tree = data.tree;

    var allChildren = tree.config.removeCluter(xmlNode.childNodes);
    for(var i=0;i<allChildren.length;i++){
        xmlNode.removeChild(allChildren[i]);
    }

    //update main attributes
    var dropTypeSelect = document.getElementById('dropTypeSelect');
    var type = dropTypeSelect.options[dropTypeSelect.selectedIndex].value;
    xmlNode.setAttribute('type',type);

    if(type == 'group'){
        //create the groupSet element
        var groupSet = tree.xml.createElement('groupSet');
        var groupTable = document.getElementById('dropGroupTable');

        for(var i=0;i<groupTable.rows.length;i++){
            var groupElement = tree.xml.createElement('group');
            groupElement.setAttribute('regex',$('input',groupTable.rows[i])[0].value);
            groupSet.appendChild(groupElement);
        }
        xmlNode.appendChild(groupSet);
    }else if(type == 'row'){
        //create the groupSet element
        var fieldSet = tree.xml.createElement('fieldSet');
        var matchUsingField = document.getElementById('matchUsingSelector');
        var matchUsing =  matchUsingField.options[matchUsingField.selectedIndex].value;
        fieldSet.setAttribute('matchUsing',matchUsing);

        var fieldTable = document.getElementById('dropFieldTable');

        for(var i=1;i<fieldTable.rows.length;i++){
            var groupElement = tree.xml.createElement('field');
            groupElement.setAttribute('name',$('input',fieldTable.rows[i])[0].value);
            if($('input',fieldTable.rows[i])[1].value != ""){
                groupElement.setAttribute('regex',$('input',fieldTable.rows[i])[1].value);                     
            }
            fieldSet.appendChild(groupElement);
        }
        xmlNode.appendChild(fieldSet);
    }
    else if(type == 'column'){
        var fieldSet = tree.xml.createElement('fieldSet');

        var fieldTable = document.getElementById('dropFieldTableColumn');

        for(var i=1;i<fieldTable.rows.length;i++){
            var groupElement = tree.xml.createElement('field');
            groupElement.setAttribute('name',$('input',fieldTable.rows[i])[0].value);
            if($('input',fieldTable.rows[i])[1].value != ""){
                groupElement.setAttribute('regex',$('input',fieldTable.rows[i])[1].value);
            }
            fieldSet.appendChild(groupElement);
        }
        xmlNode.appendChild(fieldSet);
    } else{
        var fieldSet = tree.xml.createElement('fieldSet');
        var matchUsingField = document.getElementById('matchUsingSelector');
        var matchUsing =  matchUsingField.options[matchUsingField.selectedIndex].value;
        fieldSet.setAttribute('matchUsing',matchUsing);

        var fieldTable = document.getElementById('dropFieldTable');

        for(var i=1;i<fieldTable.rows.length;i++){
            var groupElement = tree.xml.createElement('field');
            groupElement.setAttribute('name',$('input',fieldTable.rows[i])[0].value);
            if($('input',fieldTable.rows[i])[1].value != ""){
                groupElement.setAttribute('regex',$('input',fieldTable.rows[i])[1].value);
            }
            fieldSet.appendChild(groupElement);
        }
        xmlNode.appendChild(fieldSet);
    }
}
function updateAttributesFromUI(xmlNode,ui){
     var mainAttributeInputs = $('input',ui);
    for(i=0;i<mainAttributeInputs.length;i++){
        xmlNode.setAttribute(mainAttributeInputs[i].name, mainAttributeInputs[i].value);
    }
}
