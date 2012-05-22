function changeAppNameMode(linkObj){
    var theTr = $(linkObj).parent().parent().attr('data-value');
    $('td:first',appName).html('<div class="row-fluid"><div class="span6"> <input class="app_name_new" value="'+theTr.attr('data-value')+'" type="text" /> </div><div class="span6"><button class="btn btn-primary" onclick="updateApplication(this)">Save</button> <button class="btn" onclick="updateApplication_reset(this)">Cancel</button></div></div> ');
}
function updateApplication_reset(linkObj){
      var appNameCell = $(linkObj).parent();
        appNameCell.html(appNameCell.val());
}
function updateApplication(saveButton){
    var applicationOld = $(saveButton).parent().parent().attr('data-value');
    var applicationNew = $(saveButton).prev().val();
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