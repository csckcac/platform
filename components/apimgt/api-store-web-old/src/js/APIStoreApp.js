var APIStoreApp = new function () {

    this.url = "/apistore/services/subscriber.jag";

/**
     * main operation to call the external WS
     * @param data
     * @param callback
     */
    this.call = function (data, callback) {
        var path = $("#path").val();
        var htmlResult;
        if (ServiceStoreAppUtil.isUndefined(callback) || callback == null) {
            callback = function (result) {
                console.log(JSON.stringify(result));
            };
        }
        ServiceStoreAppUtil.makeJsonRequest(APIStoreApp.url, data, callback);
    };
    this.listPurchasedServices = function () {
        APIStoreApp.call('action=getPurchases', APIStoreApp.drawServicesList);
    };


    this.drawServicesList = function (result) {
        if (result.error == "false") {
            var items = "";
            for (var i = 0; i < result.data.services.length; i++) {
                var installText = 'install';
                if (result.data.services[i].purchased == "true") {
                    installText = 'Uninstall';
                }
                var serviceName = result.data.services[i].name;
                var servicePath = result.data.services[i].path;
                var serviceAuthor = result.data.services[i].author;
                var serviceForumURL = result.data.services[i].supportForumURL;

                if (!ServiceStoreAppUtil.isUndefined(serviceName) && serviceName.length >= 15) {
                    serviceName = '<a id="name"  href="serviceInfo.jag?path=' + servicePath + '&name=' + serviceName + '" class="service-name" title="' + serviceName + '">' + serviceName.substring(0, 15) + '..' + '</a>';
                } else if (!ServiceStoreAppUtil.isUndefined(serviceName)) {
                    serviceName = '<a id="name" href="serviceInfo.jag?path=' + servicePath + '&name=' + serviceName + '" class="service-name">' + serviceName.substring(0, 15) + '</a>';
                }
                if (!ServiceStoreAppUtil.isUndefined(serviceAuthor) && serviceAuthor.length >= 20) {
                    serviceAuthor = '<div id="path" class="company-name" title="' + serviceAuthor + '">' + serviceAuthor.substring(0, 20) + '..' + '</div>';
                } else if (!ServiceStoreAppUtil.isUndefined(serviceAuthor)) {
                    serviceAuthor = '<div id="path" class="company-name">' + serviceAuthor.substring(0, 20) + '</div>';
                }
                if (!ServiceStoreAppUtil.isUndefined(serviceForumURL) && serviceForumURL.length >= 20) {
                    serviceForumURL = '<div id="forum" class="forum" title="' + serviceForumURL + '">' + serviceForumURL.substring(0, 20) + '..' + '</div>';
                } else if (!ServiceStoreAppUtil.isUndefined(serviceForumURL)) {
                    serviceForumURL = '<div id="forum" class="forum">' + serviceForumURL.substring(0, 20) + '&nbsp;</div>';
                }
                var serviceThumbView = result.data.services[i].thumbURL;
                if (result.data.services[i].thumbURL == "") {
                    serviceThumbView = 'images/service-default.png';
                }
                items += '<div id="list-item-' + i + '" class="list-item">' +
                    '<img src="' + serviceThumbView + '" />' +
                    serviceName +
                    serviceAuthor +
                    serviceForumURL +
                    '<div id="rating" class="rating">' +
                    '<img src="images/start0.png" />' +
                    '<img src="images/start0.png" />' +
                    '<img src="images/start0.png" />' +
                    '<img src="images/start0.png" />' +
                    '</div>' +
                    '<div style="clear:both"></div>' +
                    '<a id="buy" class="buy-button" onclick="ServiceStoreApp.purchase(' + i + ')">' + installText + '</a>' +
                    '</div>';
            }
            $('#elementList').html(items);
        }
    };
};