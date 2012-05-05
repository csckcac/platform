$(document).ready(function () {
    $("#subscribe-button").click(function () {
        var applicationId = $("#application-list").val();
        var api = jagg.api;
        jagg.post("/site/blocks/api/api-info/ajax/subscribe.jag", {
            applicationId:applicationId,
            name:api.name,
            version:api.version,
            provider:api.provider,
            tier:api.tier
        }, function (result) {
            if (result.error == false) {
                var elem = $("#application-list");
                $("option[value=" + applicationId + "]", elem).remove();
                if ($("option", elem).length() == 0) {
                    //elem.attr("disabled");
                    $("#subscribe-button").attr("disabled");
                }
            } else {
                jagg.message(result.message);
            }
        }, "json");
    });
});