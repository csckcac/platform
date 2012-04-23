//overrides goes here
var initialize = function (global) {
    //removeHeaderCSS("layout/default");
    //removeHeaderJS("layout/default");
};

var getParams = function (data) {
    var ratings = require("/core/ratings/ratings.js");
    var result = ratings.getTopRatedAPIs(data.count);
    return {
        "apis":result.apis
    };
};

var getData = function (args) {
    return {
        "count":5
    };
};

var getTemplates = function () {
    return [];
};

var getTemplateVars = function () {
    return [];
};