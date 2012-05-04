var getTemplateFile = function () {
    return "tmpl/layouts/login/template.jag";
};

var initialize = function (jagg) {
    //addHeaderCSS(global, "layouts/layout0", "layout0", "tmpl/layouts/layout0/css/layout0.css");
};

var getData = function (params) {
    return {
        rBottom:params.rBottom
    };
};

var getParams = function () {
    return {

        rBottom:null
    };
};

var getTemplates = function () {
    return [];
};

var getTemplateParams = function () {
    return [

        "rBottom"
    ];
};