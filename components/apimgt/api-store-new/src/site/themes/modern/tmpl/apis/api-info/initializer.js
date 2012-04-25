var getTemplateFile = function () {
    return "tmpl/apis/api-info/template.jag";
};

var initialize = function (jagg) {

};

var getData = function (params) {
    var t = params.tabs.tabs;
    t[0].body[0].params.api = params.api;
    return {
        "api":params.api,
        "tabs":params.tabs
    };
};

var getParams = function () {
    return {
        "api":null,
        "tabs":{
            "tabs":[
                {
                    "title":"Overview",
                    "body":[
                        {
                            "name":"apis/overview",
                            "params": {
                                api : null
                            }
                        }
                    ]
                },
                {
                    "title":"Documentation",
                    "body":[
                        {
                            "name":"apis/documentation",
                            "params":null
                        }
                    ]
                }
            ]
        }
    }
};

var getTemplates = function () {
    return [
        "tabs/basic"
    ];
};

var getTemplateParams = function () {
    return [
        ["tabs", "tabs", "body"]
    ];
};