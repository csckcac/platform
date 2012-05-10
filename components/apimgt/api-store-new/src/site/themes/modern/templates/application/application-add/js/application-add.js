$(document).ready(function () {
    $("#application-add-button").click(function () {
        var application = $("#application-name").val();
        jagg.post("/site/blocks/application/application-add/ajax/application-add.jag", {
            action:"addApplication",
            application:application
        }, function (result) {
            if (result.error == false) {
                window.location.reload();
            } else {
                jagg.message(result.message);
            }
        }, "json");
    });
});