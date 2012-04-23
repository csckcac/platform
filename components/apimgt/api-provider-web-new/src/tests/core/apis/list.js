var test0 = function() {
    var name = "getAPI";
    var utils = require("/tests/utils.js");
    var get = require("/core/apis/list.js");


    var result = get[name]("api2", "1.2.3");
    if (result.error) {
        utils.failure(name, result);
        return;
    }
    utils.success(name, result);
};