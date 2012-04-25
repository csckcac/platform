var getParent = function () {
    return "layouts/base";
};

var getData = function (params) {
    return {
        "top":[
            {
                "name":"header/featured-apis",
                "params":null
            },
            {
                "name":"search/api-search",
                "params":null
            }
        ],
        "left":[
            {
                "name":"tabs/basic",
                "params":{
                    "classes":"cat_and_charts",
                    "tabs":[
                        {
                            "title":"Top Charts",
                            "body":[
                                {
                                    "name":"apis/recently-added",
                                    "params":null
                                },
                                {
                                    "name":"apis/top-rated",
                                    "params":null
                                }
                            ]
                        },
                        {
                            "title":"Categories",
                            "body":null
                        }
                    ]
                }
            }
        ],
        "middle":params.middle,
        "right":[
            {
                "name":"tags/tag-cloud",
                "params":null
            }
        ]
    };
};

var getParams = function () {
    return {
        "middle":null
    };
};


var getDataTemplates = function () {
    return [
        "top",
        "left",
        "right"
    ];
};

var getParamTemplates = function () {
    return [
        "middle"
    ];
};