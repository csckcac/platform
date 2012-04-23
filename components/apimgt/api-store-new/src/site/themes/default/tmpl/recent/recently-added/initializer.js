//overrides goes here
var initialize = function (global) {
    //removeHeaderCSS("layout/default");
    //removeHeaderJS("layout/default");
};

var getParams = function (data) {
    var recent = require("/core/recent/recent.js");
    var result = recent.getRecentlyAddedAPIs(data.count);
    return {
        "apis":result.apis
    };
};

var getData = function () {
    return {
        "count":10
    };
};

var getTemplates = function () {
    return [];
};

var getTemplateVars = function () {
    return [];
};