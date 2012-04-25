var getTemplateFile = function () {
    return "tmpl/tabs/basic/template.jag";
};

var getData = function (params) {
    return {
        tabs:params.tabs,
        classes:params.classes
    };
};

var getParams = function () {
    return {
        tabs:null,
        classes:null
    };
};

var getParamTemplates = function () {
    return [
        ["tabs", "body"]
    ];
};