wso2.ui.dashboard.Util = new function() {
    this.makeRequest = function(reqUrl, content, sucessFunc) {
        $.ajax({
            url: reqUrl,
            data: content,
            type: 'POST',
            success: sucessFunc
        });
    }
}

