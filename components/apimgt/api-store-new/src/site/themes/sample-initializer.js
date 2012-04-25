var getTemplateFile = function () {
    return "tmpl/apis/api-info/template.jag";
};

var initialize = function (jagg) {

};

var getData = function (params) {
    return {
        "api":params.api,
        "tabs":{
            "tabs":[
                {
                    "title":"Overview",
                    "body":[
                        {
                            "name":"apis/overview",
                            "params":{
                                api:params.api
                            }
                        }
                    ]
                },
                {
                    "title":"Documentation",
                    "body":[
                        {
                            "name":"apis/documentation",
                            "params":params.api
                        }
                    ]
                }
            ]
        }
    };
};

var getParams = function () {
    return {
        "api":null
    }
};

var getTemplates = function () {
    return [
        "tabs/basic"
    ];
};

var getDataTemplates = function () {
    return [
        ["tabs", "tabs", "body"]
    ];
};

var getParamTemplates = function () {
    return [];
};