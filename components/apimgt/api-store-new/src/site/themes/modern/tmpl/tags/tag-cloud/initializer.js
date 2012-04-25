var getTemplateFile = function() {
    return "tmpl/tags/tag-cloud/template.jag";
};

var getData = function (params) {
    var tags = require("/core/tags/cloud.js");
    var result = tags.getAllTags();
    return {
        "tags":result.tags
    };
};