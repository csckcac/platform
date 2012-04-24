var getTemplateFile = function() {
    return "tmpl/apis/top-rated/template.jag";
};
//overrides goes here
var initialize = function (global) {
    //removeHeaderCSS("layout/default");
    //removeHeaderJS("layout/default");
};

var getData = function (params) {
    var ratings = require("/core/apis/top-rated.js");
    var result = ratings.getTopRatedAPIs(params.count);
    return {
        "apis":result.apis
    };
};

var getParams = function () {
    return {
        "count":5
    };
};

var getTemplates = function () {
    return [];
};

var getTemplateParams = function () {
    return [];
};