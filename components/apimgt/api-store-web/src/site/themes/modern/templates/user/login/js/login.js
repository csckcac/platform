var login = login || {};
(function () {
    var loginbox = login.loginbox || (login.loginbox = {});

    loginbox.login = function (username, password, url) {
        jagg.post("/site/blocks/user/login/ajax/login.jag", { action:"login", username:username, password:password },
                 function (result) {
                     if (result.error == false) {
                         if (url && url != undefined) {
                             window.location.href = url;
                         } else {
                             window.location.reload();
                         }
                     } else {
                         $('#login-form').modal('hide');
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
    $('#mainLoginForm input').keydown(function(event) {
        if (event.which == 13) {
            var goto_url = $('#loginBtn').data("goto_url");
            event.preventDefault();
            login.loginbox.login($("#username").val(), $("#password").val(), goto_url);

        }
    });
    $("#logout-link").click(function () {
        login.loginbox.logout();
    });

    $(".need-login").click(function() {
        $('#login-form').modal('show');
        $('#loginBtn').data("goto_url", this.href);


        $("#login-form").dialog("open").data("url", $(this).attr("href"));
        return false;
    });

    $('#loginBtn').click(
                        function() {
                            var goto_url = $('#loginBtn').data("goto_url");
                            login.loginbox.login($("#username").val(), $("#password").val(), goto_url);
                        }
            );
});