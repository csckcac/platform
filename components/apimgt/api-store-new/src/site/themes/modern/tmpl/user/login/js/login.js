var login = login || {};
(function () {
    var loginbox = login.loginbox || (login.loginbox = {});

    loginbox.login = function (username, password) {
        jagg.post("user.jag", { action:"login", username:username, password:password }, function (result) {
            if (result.error == "false" && result.data.success == "true") {
                window.location.reload();
            } else {
                jagg.message(result.message);
            }
        }, "json");
    };

    loginbox.logout = function () {
        jagg.post("user.jag", {action:"logout"}, function (result) {
            if (result.error == "false" && result.data.success == "true") {
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
                login.loginbox.login($("#username").val(), $("#password").val());
                $(this).dialog("close");
            },
            "Cancel":function () {
                $(this).dialog("close");
            }
        },
        close:function () {

        }
    });

    $("#login-link").click(function () {
        $("#login-form").dialog("open");
    });

    $("#logout-link").click(function () {
        login.loginbox.logout();
    });
});