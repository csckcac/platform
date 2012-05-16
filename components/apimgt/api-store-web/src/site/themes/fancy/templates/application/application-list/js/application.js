function changeAppNameMode(linkObj){
    var appNameCell = $(linkObj).parent().prev();
    appNameCell.html('<input value="'+appNameCell.val()+'" type="text" /> <button class="blue" onclick="updateApplication(this)">Save</button> <button class="" onclick="updateApplication_reset(this)">Cancel</button> ');
}
function updateApplication_reset(linkObj){
      var appNameCell = $(linkObj).parent();
        appNameCell.html(appNameCell.val());
}
function updateApplication(saveButton){
    var applicationOld = $(saveButton).parent().val();
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