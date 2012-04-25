var getTemplateFile = function () {
    return "tmpl/layouts/base/template.jag";
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

var getParamTemplates = function () {
    return [
        "top",
        "left",
        "middle",
        "right"
    ];
};