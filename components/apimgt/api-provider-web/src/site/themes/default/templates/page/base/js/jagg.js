var jagg = jagg || {};

(function () {
    jagg.post = function () {
        var args = Array.prototype.slice.call(arguments);
        args[0] = this.site.context + args[0];
        $.post.apply(this, args);
    };

    jagg.syncPost = function(url, data, callback, type) {
        url = this.site.context + url;
        return jQuery.ajax({
                               type: "POST",
                               url: url,
                               data: data,
                               async:false,
                               success: callback,
                               dataType:"json"
                           });
    },

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