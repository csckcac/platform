var getTemplateFile = function () {
    return "tmpl/apis/recently-added/template.jag";
};

var getData = function (params) {
    var result, apis, recent = require("/core/apis/recently-added.js");
    result = recent.getRecentlyAddedAPIs(params.count);
    apis = result.apis;
    return {
        "apis":apis
    };
};

var getParams = function () {
    return {
        "count":5
    };
};