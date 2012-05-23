function changeAppNameMode(linkObj){
    var theTr = $(linkObj).parent().parent();
    var appName = $(theTr).attr('data-value');
    $('td:first',theTr).html('<div class="row-fluid"><div class="span6"> <input class="app_name_new" value="'+theTr.attr('data-value')+'" type="text" /> </div><div class="span6"><button class="btn btn-primary" onclick="updateApplication(this)">Save</button> <button class="btn" onclick="updateApplication_reset(this)">Cancel</button></div></div> ');
}
function updateApplication_reset(linkObj){
      var appNameCell = $(linkObj).parent();
        appNameCell.html(appNameCell.val());
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