//overrides goes here
var initialize = function (global) {
    //removeHeaderCSS("layout/default");
    //removeHeaderJS("layout/default");
};

var getParams = function (data) {
    //var apis = getTopRatedAPIs(data.count);
    return {
        "param1":{
            "left":"Left1",
            "right":"Right1",
            "middle":"Middle1"
        },
        "param2":{
            "left":"Left2",
            "right":"Right2",
            "middle":"Middle2"
        }
    };
};

var getData = function (args) {
    return {
        title:"default-title",
        footer:[
            {
                name:"login/box",
                data:null
            }
        ]
    };
};

var getTemplates = function () {
    return [
        "layouts/layout0"
    ];
};

var getTemplateVars = function () {
    return [
        "footer"
    ];
};