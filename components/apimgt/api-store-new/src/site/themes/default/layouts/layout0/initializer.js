var initialize = function(data) {
    include("/site/themes/utils.jag");

    initTemplate(data, "recently-added");
    initTemplate(data, "top-rated");
    initTemplate(data, "tag-cloud");
};