var test0 = function() {
    var name = "updateAPI";
    var utils = require("/tests/utils.js");
    var update = require("/core/apis/update.js");

    var uriTemplateArr = [];
    var uriMethodArr = [];

    uriTemplateArr.push("/*");
    uriMethodArr.push("GET");


    var result = update[name]("apiSample", "1.2.3", "hello", "","http://sample", "http://sample.wsdl", "jaggery,mashup,sample","silver", "CREATED", "api1", uriTemplateArr, uriMethodArr);
    if (result.error) {
        utils.failure(name, result);
        return;
    }
    utils.success(name, result);
};