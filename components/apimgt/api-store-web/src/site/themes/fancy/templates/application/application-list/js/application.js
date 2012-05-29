function changeAppNameMode(linkObj){
    var theTr = $(linkObj).parent().parent();
    var appName = $(theTr).attr('data-value');
    $('td:first',theTr).html('<div class="row-fluid"><div class="span6"> <input class="app_name_new" value="'+theTr.attr('data-value')+'" type="text" /> </div><div class="span6"><button class="btn btn-primary" onclick="updateApplication(this)">Save</button> <button class="btn" onclick="updateApplication_reset(this)">Cancel</button></div></div> ');
    $('input.app_name_new',theTr).focus();
}
function updateApplication_reset(linkObj){
    var theTr = $(linkObj).parent().parent().parent().parent();
    var appName = $(theTr).attr('data-value');
    $('td:first',theTr).html(appName);
}
function updateApplication(linkObj){
    var theTr = $(linkObj).parent().parent().parent().parent();
    var applicationOld = $(theTr).attr('data-value');
    var applicationNew = $('input.app_name_new',theTr).val();
        jagg.post("/site/blocks/application/application-update/ajax/application-update.jag", {
            action:"updateApplication",
            applicationOld:applicationOld,
            applicationNew:applicationNew
        }, function (result) {
            if (result.error == false) {
                window.location.reload();
            } else {
                jagg.message(result.message);
            }
        }, "json");
}

function deleteApp(linkObj) {
    var theTr = $(linkObj).parent().parent();
    var appName = $(theTr).attr('data-value');
    $('#messageModal').html($('#confirmation-data').html());
    $('#messageModal h3.modal-title').html('API Provider');
    $('#messageModal div.modal-body').html('\n\nAre you sure you want to remove the application "' + appName + '"? This will cancel all the existing subscriptions and keys associated with the application.');
    $('#messageModal a.btn-primary').html('Yes');
    $('#messageModal a.btn-other').html('No');
    $('#messageModal a.btn-primary').click(function() {
        jagg.post("/site/blocks/application/application-remove/ajax/application-remove.jag", {
            action:"removeApplication",
            application:appName
        }, function (result) {
            if (!result.error) {
                window.location.reload();
            } else {
                jagg.message(result.message);
            }
        }, "json");
    });
    $('#messageModal a.btn-other').click(function() {
        window.location.reload();
    });
    $('#messageModal').modal();

}