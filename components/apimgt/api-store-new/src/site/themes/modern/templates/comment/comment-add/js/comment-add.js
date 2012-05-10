$(document).ready(function () {
    $("#comment-add-button").click(function () {
        var comment = $("#comment-text").val();
        var api = jagg.api;
        jagg.post("/site/blocks/comment/comment-add/ajax/comment-add.jag", {
            action:"addComment",
            comment:comment,
            name:api.name,
            version:api.version,
            provider:api.provider
        }, function (result) {
            if (result.error == false) {
                window.location.reload();
            } else {
                jagg.message(result.message);
            }
        }, "json");
    });
});