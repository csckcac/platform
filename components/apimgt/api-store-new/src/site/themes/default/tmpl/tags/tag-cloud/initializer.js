var initialize = function (global) {
    addHeaderJS(global, "tags/tag-cloud", "tag-cloud-events", "tmpl/tags/tag-cloud/js/tag-cloud.js");
};

var getParams = function (data) {
    var tags = require("/core/tags/tags.js");
    var result = tags.getAllTags();
    return {
        "tags":result.tags
    };
};

var getData = function (args) {
    return {};
};

var getTemplates = function () {
    return [];
};

var getTemplateVars = function () {
    return [];
};