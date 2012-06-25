$(document).ready(function () {

    $(".key-generate-button").click(function () {
        var elem = $(this);
        jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
            action:"generateAPIKey",
            name:elem.attr("data-name"),
            version:elem.attr("data-version"),
            provider:elem.attr("data-provider"),
            context:elem.attr("data-context"),
            application:elem.attr("data-application"),
            keytype:elem.attr("data-keytype")
        }, function (result) {
            if (result.error == false) {
                window.location.reload();
            } else {
                $("#subscribe-button").html('Subscribe').addClass('green').removeClass('disabled').removeAttr('disabled');
                jagg.message({content:result.message,type:"error"});
            }
        }, "json");

        $(this).html('Please wait...').removeClass('green').addClass('disabled').attr('disabled', 'disabled');
    });
    $('.app-key-generate-button').click(function () {
        var elem = $(this);
        jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
            action:"generateApplicationKey",
            application:elem.attr("data-application"),
            keytype:elem.attr("data-keytype")
        }, function (result) {
            if (!result.error) {
                window.location.reload();
            } else {
                $("#subscribe-button").html('Subscribe').addClass('green').removeClass('disabled').removeAttr('disabled');
                jagg.message({content:result.message,type:"error"});
            }
        }, "json");

        $(this).html('Please wait...').removeClass('green').addClass('disabled').attr('disabled', 'disabled');
    });
});

function toggleKey(toggleButton){
    if($(toggleButton).next().is(":visible")){
        $(toggleButton).next().hide();
        $(toggleButton).html('<span class="icon gray small" data-icon="a"></span> Show Key');
    }else{
        $(toggleButton).next().show();
        $(toggleButton).html('<span class="icon gray small" data-icon="O"></span> Hide Key');
    }
}
function collapseKeys(index,type,link){

    if(type == 'super'){
        if($('#appDetails'+index+'_super').is(":visible")){
            $('i',link).removeClass('icon-minus').addClass('icon-plus');
        }else{
            $('i',link).removeClass('icon-plus').addClass('icon-minus');
        }
        $('#appDetails'+index+'_super').toggle();
    }else{
        if($('#appDetails'+index).is(":visible")){
            $('i',link).removeClass('icon-minus').addClass('icon-plus');
        }else{
            $('i',link).removeClass('icon-plus').addClass('icon-minus');
        }

        $('#appDetails'+index).toggle();
    }
}

function removeSubscription(apiName, version, provider, applicationId) {
    $('#messageModal').html($('#confirmation-data').html());
    $('#messageModal h3.modal-title').html('Confirm Delete');
    $('#messageModal div.modal-body').html('\n\nAre you sure you want to delete the subscription of <b>"' + apiName+'-'+version + '</b>"?');
    $('#messageModal a.btn-primary').html('Yes');
    $('#messageModal a.btn-other').html('No');
    $('#messageModal a.btn-primary').click(function() {
    jagg.post("/site/blocks/subscription/subscription-remove/ajax/subscription-remove.jag", {
        action:"removeSubscription",
        name:apiName,
        version:version,
        provider:provider,
        applicationId:applicationId
    }, function (result) {
        if (!result.error) {
            window.location.reload();
        } else {

            jagg.message({content:result.message,type:"error"});
        }
    }, "json"); });
    $('#messageModal a.btn-other').click(function() {
        return;
    });
    $('#messageModal').modal();
}