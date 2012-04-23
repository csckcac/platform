//overrides goes here
var initialize = function(data) {
    addHeaderCSS(data, "layouts/layout0", "layout0", "tmpl/layouts/layout0/css/layout0.css");
    //removeHeaderCSS("layout/default");
    //removeHeaderJS("layout/default");
};

var getParams = function(data) {
    //var apis = getTopRatedAPIs(data.count);
    return {
        header : {
            apis : null
        }
    };
};

var getData = function (args) {
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
    return [];
};

var getHeaderCSS = function() {
    return [];
};

var getHeaderJS = function() {
    return [];
};

var getHeaderCode = function() {
    return [];
};
