var getTemplateFile = function () {
    return "tmpl/tags/tag-cloud/template.jag";
};

var getData = function (params) {
    var result, tags, cloud = require("/core/tags/cloud.js");
    result = cloud.getAllTags();
    tags = result.tags;
    return {
        "tags":tags
    };
};