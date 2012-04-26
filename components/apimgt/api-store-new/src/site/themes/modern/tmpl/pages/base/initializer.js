var getTemplateFile = function () {
    return "tmpl/pages/base/template.jag";
};

var initialize = function(jagg) {
    var jaggi = {
        context : site.context,
        ajaxPath : "/site/ajax/"
    };
    jagg.addHeaderJS("pages/base", "jagg", "tmpl/pages/base/js/jagg.js");
    jagg.addHeaderJSCode("pages/base", "jagg.site", 'jagg.site = ' + stringify(jaggi) + ';');
};

var getParams = function () {
    return {
        title:null,
        body:null
    };
};

var getTemplates = function () {
    return [
        "header/top-bar"
    ];
};

var getParamTemplates = function () {
    return ["body"];
};
