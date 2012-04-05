wso2.ui.dashboard.IndexView = new function() {

    this.showGadgetUrlsForTab = function(tabId, divId) {
        gadgetUrls = wso2.ui.dashboard.GadgetController.getGadgetsForTab(tabId, function (data) {
            var html = '';
            for (var i = 0; i < data.split(',').length; i++) {
                html += '<p>' + data.split(',')[i] + '</p><br/>';
            }
            $(divId).html(html)
        })
    }
}