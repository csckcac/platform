var test0 = function() {
    var name = "saveAPI";
    var utils = require("/tests/utils.js");
    var add = require("/core/apis/add.js");

    var uriTemplateArr = [];
    var uriMethodArr = [];

    uriTemplateArr.push("/*");
    uriMethodArr.push("GET");


    var result = add[name]("apiSample", "1.2.3", "hello", "http://ab", "http://ab.wsdl", "jaggery,mashup", "silver", "", "api1", uriTemplateArr, uriMethodArr);
    if (result.error) {
        utils.failure(name, result);
        return;
    }
    utils.success(name, result);
};