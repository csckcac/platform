//overrides goes here
var initialize = function(global) {
    addHeaderCSS(global, "layouts/layout0", "layout0", "tmpl/layouts/layout0/css/layout0.css");
    //removeHeaderCSS("layout/default");
    //removeHeaderJS("layout/default");
};

var getParams = function(data) {
    //var apis = getTopRatedAPIs(data.count);
    return {
        "count" : 5
    };
};

var getData = function () {
    return {
        left:function (args) {
            return "default-title";
        },
        right:function (args) {
            var name = args.name;
            var version = args.version;
            var provider = args.provider;
            var listing = require("/core/listing/listing.js");
            return listing.getAllPublishedAPIs(name, version, provider);
        },
        apis2:function (args) {
            return args.apis2;
        }

    }
};

var getTemplateVars = function() {
    return [];
};

var getTemplates = function () {
    return [
        "ratings/top-rated",
        "recent/recently-added",
        "tags/tag-cloud"
    ];
};