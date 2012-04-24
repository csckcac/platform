var getTemplateFile = function() {
    return "tmpl/tabs/basic/template.jag";
};

var initialize = function (global) {
    //addHeaderCSS(global, "layouts/layout0", "layout0", "tmpl/layouts/layout0/css/layout0.css");
};

var getData = function (params) {
    return {
        tabs : params.tabs
    };
};

var getParams = function () {
    return {
        tabs:null
    };
};

var getTemplates = function () {
    return [];
};

var getTemplateParams = function () {
    return [
        ["tabs", "body"]
    ];
};