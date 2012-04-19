var initialize = function(data) {
    include("/site/themes/utils.jag");

    var site = require("/site/conf/site.json");
    var ratings = require("/core/ratings/ratings.js");

    var result = ratings.getTopRatedAPIs(site.topRatedCount);

    addTemplateData(data, "top-rated", "apis", result.apis);
};