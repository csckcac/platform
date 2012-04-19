var initialize = function(data) {
    include("/site/themes/utils.jag");

    var site = require("/site/conf/site.json");
    var listing = require("/core/listing/listing.js");

    var result = listing.getRecentlyAddedAPIs(site.recentAPIsCount);

    addTemplateData(data, "recently-added", "apis", result.apis);
};