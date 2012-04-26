var getTemplateFile = function () {
    return "tmpl/user/login/template.jag";
};

var initialize = function(jagg) {
    jagg.addHeaderJS("user/login", "login", "tmpl/user/login/js/login.js");
    jagg.addHeaderCSS("user/login", "login", "tmpl/user/login/css/login.css");
};

var getData = function (params) {
    return {
        "user" : session.get("logged.user")
    };
};