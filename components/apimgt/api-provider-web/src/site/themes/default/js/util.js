APIProviderAppUtil = new function () {
    this.makeRequest = function (u, d, callback) {
        $.ajax({
            type:"GET",
            url:u,
            data:d,
            dataType:"text",
            async:false,
            success:callback
        });
    };

    this.makePost = function (u, d, callback) {
        $.ajax({
            type:"POST",
            url:u,
            data:d,
            dataType:"text",
            success:callback
        });
    };
    this.makeJsonRequest = function (u, d, callback) {
        $.ajax({
            type:"POST",
            url:u,
            data:d,
            dataType:"json",
            success:callback
        });
    };

    this.makeSyncRequest = function (u, d) {
        var result = $.ajax({
            type:"POST",
            url:u,
            data:d,
            async:false,
            dataType:"json"
        }).responseText;

        return jQuery.parseJSON(result);
    };

    this.isUndefined = function (prop) {
        if ((typeof prop == 'undefined') || prop == null) {
            return true;
        }
    };
}
