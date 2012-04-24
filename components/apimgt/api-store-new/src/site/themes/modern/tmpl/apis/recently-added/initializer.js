var getTemplateFile = function() {
    return "tmpl/apis/recently-added/template.jag";
};

var initialize = function (global) {

};

var getData = function (params) {
    var recent = require("/core/apis/recently-added.js");
    var result = recent.getRecentlyAddedAPIs(params.count);
    return {
        "apis":result.apis
    };
};

var getParams = function () {
    return {
        "count" : 5
    };
};

var getTemplates = function () {
    return [];
};

var getTemplateParams = function () {
    return [];
};