var getTemplateFile = function () {
    return "tmpl/layouts/base/template.jag";
};

var getParent = function () {
    return "layouts/base";
};

var getParams = function () {
    return {
        "top":[
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
        "middle":null,
        "right":[
            {
                "name":"tags/tag-cloud",
                "params":null
            }
        ]
    };
};