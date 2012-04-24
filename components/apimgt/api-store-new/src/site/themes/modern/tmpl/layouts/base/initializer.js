var getTemplateFile = function () {
    return "tmpl/layouts/base/template.jag";
};

var initialize = function (jagg) {
    //addHeaderCSS(global, "layouts/layout0", "layout0", "tmpl/layouts/layout0/css/layout0.css");
};

var getData = function (params) {
    return {
        top:params.top,
        left:params.left,
        middle:params.middle,
        right:params.right
    };
};

var getParams = function () {
    return {
        top:null,
        left:null,
        middle:null,
        right:null
    };
};

var getTemplates = function () {
    return [];
};

var getTemplateParams = function () {
    return [
        "top",
        "left",
        "middle",
        "right"
    ];
};