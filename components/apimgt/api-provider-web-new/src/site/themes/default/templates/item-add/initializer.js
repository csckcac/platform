var getTemplateFile = function() {
    return "tmpl/item-add/template.jag";
};

var initialize = function (jagg) {
  addHeaderJS(global, "add", "add", "tmpl/item-add/js/validate.js");
  addHeaderJS(global, "add-resource", "add-resource", "tmpl/item-add/js/resource-template.js");
};

var getData = function (params) {

};

var getParams = function () {

};

var getTemplates = function () {
    return [];
};

var getTemplateParams = function () {
    return [];
};