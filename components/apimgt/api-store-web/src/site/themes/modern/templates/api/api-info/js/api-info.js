$(document).ready(function () {
    $("#subscribe-button").click(function () {
        if (!jagg.loggedIn) {
            $("#login-form").dialog("open");
            return;
        }
        var applicationId = $("#application-list").val();
        if (applicationId == "-") {
            jagg.message("Please select an application before subscribing");
            return;
        }
        var api = jagg.api;
        jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
            action:"addSubscription",
            applicationId:applicationId,
            name:api.name,
            version:api.version,
            provider:api.provider,
            tier:api.tier
        }, function (result) {
            if (result.error == false) {
                window.location.reload();
            } else {
                $("#subscribe-button").html('Subscribe').addClass('green').removeClass('disabled').removeAttr('disabled');
                jagg.message(result.message);
            }
        }, "json");

        $(this).html('Please wait...').removeClass('green').addClass('disabled').attr('disabled', 'disabled');
    });
    $('#application-list').change(
            function(){
                if($(this).val() == "createNewApp"){
                    window.location.href = '../site/pages/applications.jag';
                }
            }
            );
    jagg.initStars($(".api-info"), function (rating, api) {
        jagg.post("/site/blocks/api/api-info/ajax/api-info.jag", {
            action:"addRating",
            name:api.name,
            version:api.version,
            provider:api.provider,
            rating:rating
        }, function (result) {
            if (result.error == false) {
                window.location.reload();
            } else {
                jagg.message(result.message);
            }
        }, "json");
    }, function (api) {

    }, jagg.api);
});