var initialize = function(data) {
    include("/site/themes/utils.jag");

    var tags = require("/core/tags/tags.js");

    var result = tags.getAllTags();
    addTemplateData(data, "tag-cloud", "tags", result.tags);
    addHeaderJS(data, "tmpl/tag-cloud/js/tag-cloud.js");
};