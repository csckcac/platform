var getTemplateFile = function() {
    return "tmpl/overview/template.jag";
};

var initialize = function (jagg) {
};

var getData = function (params) {
    var api = require("/core/apis/list.js");
    var result = api.getAPI(apiProviderApp.currentAPIName, apiProviderApp.currentVersion);
    return {
        "api":result.api
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