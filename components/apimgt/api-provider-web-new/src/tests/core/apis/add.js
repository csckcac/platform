var test0 = function() {
    var name = "saveAPI";
    var utils = require("/tests/utils.js");
    var add = require("/core/apis/add.js");

    var uriTemplateArr = [];
    var uriMethodArr = [];

    uriTemplateArr.push("api1");
    uriMethodArr.push("GET");


    var result = add[name]("admin", "api1", "1.2.3", "hello", "http://ab", "http://ab.wsdl", "jaggery,mashup", "silver", "", "api1", request, uriTemplateArr, uriMethodArr);
    if (result.error) {
        utils.failure(name, result);
        return;
    }
    utils.success(name, result);
};