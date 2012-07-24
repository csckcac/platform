$(document).ready(function () {
    var application = $("#application-name").val("");
    $("#appAddForm").validate({
        submitHandler: function(form) {
            applicationAdd();
        }
    });
    var applicationAdd = function(){
        var application = $("#application-name").val();
        var apiPath = $("#apiPath").val();
        var goBack = $("#goBack").val();
        jagg.post("/site/blocks/application/application-add/ajax/application-add.jag", {
            action:"addApplication",
            application:application
        }, function (result) {
            if (result.error == false) {
                $.cookie('highlight','true');
                $.cookie('lastAppName',application);
                if(goBack == "yes"){
                    jagg.message({content:'Return back to API detail page?',type:'confirm',okCallback:function(){
                         window.location.href = apiViewUrl + "?" +  apiPath;
                    },cancelCallback:function(){
                        window.location.href= appAddUrl;
                    }});
                } else{
                    window.location.reload();
                }

            } else {
                jagg.message({content:result.error,type:"error"});
            }
        }, "json");
    };


    $("#application-name").charCount({
			allowed: 70,
			warning: 50,
			counterText: 'Characters left: '
		});
    $("#application-name").val('');

    /*$('#application-name').keydown(function(event) {
         if (event.which == 13) {
               applicationAdd();
            }
        });*/
});

