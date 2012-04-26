var jagg = jagg || {};

(function () {
    jagg.post = function () {
        var args = Array.prototype.slice.call(arguments);
        args[0] = this.site.context + this.site.ajaxPath + args[0];
        $.post.apply(this, args);
    };

    jagg.message = function (content) {
        $("#message-box:ui-dialog").dialog("destroy");
        var dialog = $("#message-box");
        $(".message", dialog).text(content);
        dialog.dialog({
            modal:true,
            dialogClass: "alert"
        });
    };
}());