$(document).ready(function () {
    $("#comment-add-button").click(function () {
        var comment = $("#comment-text").val();
        if(comment.length > 450){
            $('#commentAdd-error').show();
            return;
        }
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
                jagg.message({content:result.message,type:"error"});
            }
        }, "json");
    });

    $("#comment-text").charCount({
			allowed: 450,
			warning: 420,
			counterText: 'Characters left: '
		});
    $("#comment-text").val('');
});