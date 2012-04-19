
var test0 = function() {
    var name = "getRecentlyAddedAPIs";
    var utils = require("/tests/utils.js");
    var listing = require("/core/listing/listing.js");
    var result = listing.getRecentlyAddedAPIs(5);
    if(result.error) {
        utils.failure(name, result);
        return;
    }
    utils.success(name, result);
};