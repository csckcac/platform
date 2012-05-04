var getTemplateFile = function () {
    return "tmpl/pages/base/template.jag";
};

var initialize = function (jagg) {
    //addHeaderCSS(global, "layouts/layout0", "layout0", "tmpl/layouts/layout0/css/layout0.css");
};

var getData = function (params) {
    return {
        title:params.title,
        body:params.body
    };
};

var getParams = function () {
    //return { top:null, left:null, middle:null, right:null };
    return {
        title:null,
        body:null
    };
};

var getTemplates = function () {
    return [
        "header"
    ];

};

var getTemplateParams = function () {
    return ["body"];
};
