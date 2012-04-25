var getTemplateFile = function () {
    return "tmpl/apis/provider-more/template.jag";
};

var getData = function (params) {
    var providerId = params.providerId;
    // TODO : implement the apis of the provider and remove following lines
    var recent = require("/core/apis/recently-added.js");
    var result = recent.getRecentlyAddedAPIs(params.count);
    return {
        "apis":result.apis
    };
};

var getParams = function () {
    return {
        "providerId":null,
        "count":5
    };
};