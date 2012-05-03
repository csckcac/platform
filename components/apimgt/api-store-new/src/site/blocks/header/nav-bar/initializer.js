var getTemplateFile = function () {
    return "tmpl/header/nav-bar/template.jag";
};

var initialize = function (jagg) {
    jagg.addHeaderCSSCode("header/nav-bar", "body.padding", "body{padding-top:60px;padding-bottom:40px;}");
};
