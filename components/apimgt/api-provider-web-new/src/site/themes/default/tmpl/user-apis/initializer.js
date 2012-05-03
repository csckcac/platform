var getTemplateFile = function() {
    return "tmpl/user-apis/template.jag";
};

var initialize = function (jagg) {
    addHeaderJS(global, "userAPIs", "userAPIs", "tmpl/user-apis/js/lineCharts.js");
};

var getData = function (params) {
    return {
        "apis":params.apis
    };
};

var getParams = function () {
    return {
        "apis" : null
    };
};
var getTemplates = function () {
    return [];
};

var getTemplateParams = function () {
    return [];
};