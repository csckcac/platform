var initialize = function(data) {
    include("/site/themes/utils.jag");

    var site = require("/site/conf/site.json");

    var recent = require("/core/recent/recent.js");
    var result = recent.getRecentlyAddedAPIs(site.recentAPIsCount);
    addTemplateData(data, "recently-added", "apis", result.apis);
    initTemplate(data, "recently-added");

    var ratings = require("/core/ratings/ratings.js");
    result = ratings.getTopRatedAPIs(site.topRatedCount);
    addTemplateData(data, "top-rated", "apis", result.apis);
    initTemplate(data, "top-rated");

    var tags = require("/core/tags/tags.js");
    result = tags.getAllTags();
    addTemplateData(data, "tag-cloud", "tags", result.tags);
    initTemplate(data, "tag-cloud");
};