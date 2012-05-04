var getTemplateFile = function () {
    return "tmpl/users-keys/template.jag";
};

var initialize = function (jagg) {

};

var getData = function (params) {
    var subs = require("/core/subscriptions/subscriptions.js");
    var allSubs = subs.getSubscribersOfProvider();
    return {
        "subscribers":allSubs.subscribers
    };
};

var getParams = function () {
};

var getTemplates = function () {
    return [];
};

var getTemplateParams = function () {
    return [];
};