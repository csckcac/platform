var getTemplateFile = function () {
    return "tmpl/apis/top-rated/template.jag";
};

var getData = function (params) {
    var result, apis, ratings = require("/core/apis/top-rated.js");
    result = ratings.getTopRatedAPIs(params.count);
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