var login = login || {};
(function () {
    var loginbox = login.loginbox || (login.loginbox = {});

    loginbox.login = function (username, password, url) {
        jagg.post("/site/blocks/user/login/ajax/login.jag", { action:"login", username:username, password:password },
                function (result) {
                    if (result.error == false) {
                        if(url) {
                            window.location.href = url;
                        } else {
                            window.location.reload();
                        }
                    } else {
                        jagg.message(result.message);
                    }
                }, "json");
    };

    loginbox.logout = function () {
        jagg.post("/site/blocks/user/login/ajax/login.jag", {action:"logout"}, function (result) {
            if (result.error == false) {
                window.location.reload();
            } else {
                jagg.message(result.message);
            }
        }, "json");
    };
}());

$(document).ready(function () {
    $("#login-form").dialog({
        autoOpen:false,
        height:270,
        width:350,
        modal:true,
        buttons:{
            "Login":function () {
                login.loginbox.login($("#username").val(), $("#password").val(), $(this).dialog("close").data("url"));
            },
            "Cancel":function () {
                $(this).data("url", null).dialog("close");
            }
        },
        close:function () {

        }
    });

    $("#logout-link").click(function () {
        login.loginbox.logout();
    });

    $(".need-login").click(function() {
        $("#login-form").data("url", $(this).attr("href")).dialog("open");
        return false;
    });
});