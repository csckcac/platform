$(document).ready(function () {
    $("select[name='tiers-list']").change(function() {
        var selectedIndex = document.getElementById('tiers-list').selectedIndex;
        var api = jagg.api;
        var tierDescription = api.tierDescription;
        var tierDescList = tierDescription.split(",");
        for (var i = 0; i < tierDescList.length; i++) {
            var tierDesc = tierDescList[i];
            if (selectedIndex == i) {
                if (tierDesc != "null") {
                    $("#tierDesc").text(tierDesc);
                }
            }
        }

    });
    $("#subscribe-button").click(function () {
        if (!jagg.loggedIn) {
            return;
        }
        var applicationId = $("#application-list").val();
        if (applicationId == "-") {
            jagg.message("Please select an application before subscribing");
            return;
        }
        var api = jagg.api;

        $(this).html('Please wait...').attr('disabled', 'disabled');

        jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
            action:"addSubscription",
            applicationId:applicationId,
            name:api.name,
            version:api.version,
            provider:api.provider,
            tier:api.tier
        }, function (result) {
            $("#subscribe-button").html('Subscribe');
            $("#subscribe-button").removeAttr('disabled');
            if (result.error == false) {
                $('#messageModal').html($('#confirmation-data').html());
                $('#messageModal h3.modal-title').html('API Provider');
                $('#messageModal div.modal-body').html('\n\nCongratulations! You have successfully subscribed to the API. Please go to \'My Subscriptions\' page to review your subscription and generate API keys.');
                $('#messageModal a.btn-primary').html('Go to My Subscriptions');
                $('#messageModal a.btn-other').html('Stay on this page');
                $('#messageModal a.btn-other').click(function() {
                    window.location.reload();
                });
                $('#messageModal a.btn-primary').click(function() {
                    location.href = "../site/pages/subscriptions.jag";
                });
                $('#messageModal').modal();


            } else {
                jagg.message(result.message);


                //$('#messageModal').html($('#confirmation-data').html());
                /*$('#messageModal h3.modal-title').html('API Provider');
                 $('#messageModal div.modal-body').html('\n\nSuccessfully subscribed to the API.\n Do you want to go to the subscription page?');
                 $('#messageModal a.btn-primary').html('Yes');
                 $('#messageModal a.btn-other').html('No');
                 */
                /*$('#messageModal a.btn-other').click(function(){
                 v.resetForm();
                 });*/
                /*
                 $('#messageModal a.btn-primary').click(function() {
                 var current = window.location.pathname;
                 if (current.indexOf(".jag") >= 0) {
                 location.href = "index.jag";
                 } else {
                 location.href = 'site/pages/index.jag';
                 }
                 });*/
//                        $('#messageModal').modal();


            }
        }, "json");

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