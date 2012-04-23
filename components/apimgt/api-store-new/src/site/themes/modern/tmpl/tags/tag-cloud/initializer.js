var initialize = function (global) {
    //addHeaderJS(global, "tags/tag-cloud", "tag-cloud-events", "tmpl/tags/tag-cloud/js/tag-cloud.js");
};

var getData = function (params) {
    var tags = require("/core/tags/cloud.js");
    var result = tags.getAllTags();
    return {
        "tags":result.tags
    };
};

var getParams = function () {
    return {};
};

var getTemplates = function () {
    return [];
};

var getTemplateParams = function () {
    return [];
};