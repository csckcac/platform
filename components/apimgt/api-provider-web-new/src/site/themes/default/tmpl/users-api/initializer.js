var getTemplateFile = function() {
    return "tmpl/users-api/template.jag";
};

var initialize = function (jagg) {
};

var getData = function (params) {
    var subs = require("/core/subscriptions/subscriptions.js");
    var subsOfAPIs = subs.getSubscribersOfAPI(apiProviderApp.currentAPIName, apiProviderApp.currentVersion);
    return {
        "subscribers":subsOfAPIs.subscribers
    };
};

var getParams = function () {
};

var getTemplates = function () {
    return [];
};

var getTemplateParams = function () {
    return [];
};