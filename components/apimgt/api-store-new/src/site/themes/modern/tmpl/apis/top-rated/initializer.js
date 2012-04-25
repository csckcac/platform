var getTemplateFile = function() {
    return "tmpl/apis/top-rated/template.jag";
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