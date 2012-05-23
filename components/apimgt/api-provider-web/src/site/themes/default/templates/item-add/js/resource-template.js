
var rowNums = new Array();
rowNums.push(0);
var addResourcesToApi = function () {
    var isChecked=$('#resource-get').is(":checked")&&$('#resource-put').is(":checked")&&$('#resource-post').is(":checked")
    &&$('#resource-delete').is(":checked");

    if ($('#uriTemplate').val() == "" || !isChecked) {
        $('#resourceTableError').show('fast');
        $('#resourceTableError').html('Sorry. The new row can not be added.Please enter a value for URI Template and Resource Method.<br />');
        return;
    }
    var resourcesCount=$('#resourceTable tr').length-2;
    var countLength=$('#resourceCount').length;
    $('#resourceTableError').hide('fast');
    $('#resourceRow').clone(true).attr('id', 'item-' + resourcesCount).insertAfter($('#resourceRow'));
    var resourceGet,resourcePut,resourcePost,resourceDelete;
    if($('#resource-get').is(":checked")){
    resourceGet=$('#resource-get').val();
    $('#item-' + resourcesCount + ' #resource-get').val(resourceGet);
    }if($('#resource-put').is(":checked")){
    resourcePut=$('#resource-put').val();
    $('#item-' + resourcesCount + ' #resource-put').val(resourcePut);
    }if($('#resource-post').is(":checked")){
    resourcePost=$('#resource-post').val();
    $('#item-' + resourcesCount + ' #resource-post').val(resourcePost);
    }if($('#resource-delete').is(":checked")){
    resourceDelete=$('#resource-delete').val();
    $('#item-' + resourcesCount + ' #resource-delete').val(resourceDelete);
    }
    $('#item-' + resourcesCount + ' #uriTemplate').attr('disabled', 'disabled');
    $('#item-' + resourcesCount + ' #resource-get').attr('disabled', 'disabled');
    $('#item-' + resourcesCount + ' #resource-put').attr('disabled', 'disabled');
    $('#item-' + resourcesCount + ' #resource-post').attr('disabled', 'disabled');
    $('#item-' + resourcesCount + ' #resource-delete').attr('disabled', 'disabled');


    $('#resource-get').attr('checked', false);
    $('#resource-put').attr('checked', false);
    $('#resource-post').attr('checked', false);
    $('#resource-delete').attr('checked', false);


    $('#item-' + resourcesCount + ' td#buttons a#resourceAddBtn').remove();
    if($('#item-' + resourcesCount + ' td#buttons a#resourceDelBtn').length>0){
    $('#item-' + resourcesCount + ' td#buttons a#resourceDelBtn').remove();
    }
    $('#item-' + resourcesCount + ' td#buttons').append("<a class=\"btn btn-danger\" onclick=\"deleteResource(" + resourcesCount + ")\"><i class=\"icon-trash icon-white\"></i> Delete</a>");

    var resourceMethodValues=new Array();
    if(resourceGet!=null){
    resourceMethodValues.push(resourceGet);
    }if(resourcePut!=null){
    resourceMethodValues.push(resourcePut);
    }if(resourcePost!=null){
    resourceMethodValues.push(resourcePost);
    }if(resourceDelete!=null){
    resourceMethodValues.push(resourceDelete);
    }

    $('<input>').attr('type', 'hidden')
        .attr('name', 'resourceMethod-' + resourcesCount).attr('value', resourceMethodValues)
        .appendTo('#addAPIForm');

    $('<input>').attr('type', 'hidden')
        .attr('name', 'uriTemplate-' + resourcesCount).attr('value', $('#uriTemplate').val())
        .appendTo('#addAPIForm');
    rowNums.push(resourcesCount);
    resourcesCount++;

    if (countLength == 0) {
        $('<input>').attr('type', 'hidden')
            .attr('name', 'resourceCount')
            .attr('id', 'resourceCount')
            .attr('value', rowNums)
            .appendTo('#addAPIForm');
    } else {
        $('#resourceCount').attr('value', rowNums);
    }

    $('#uriTemplate').val('');

};


var deleteResource = function (id) {
    var count=$('#resourceTable tr').length;
    //Check whether only one defined resource remains before delete operation
    if(count==3){
        $('#resourceTableError').show('fast');
        $('#resourceTableError').html('Sorry. This row can not be deleted. Atleast one resource entry has to be available.<br />');
        return;
    }
    $('#resourceTableError').hide('fast');
    $('#item-' + id).remove();
    $('[name=resourceMethod-' + id + ']').remove();
    $('[name=uriTemplate-' + id + ']').remove();
    rowNums.splice(rowNums.indexOf(id),1);
    $('#resourceCount').val(rowNums);

};
