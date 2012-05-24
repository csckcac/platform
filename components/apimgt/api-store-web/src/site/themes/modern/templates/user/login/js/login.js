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
                         $('#messageModal').modal('hide');
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
    var registerEventsForLogin = function(){
        $('#mainLoginForm input').die();
         $('#mainLoginForm input').keydown(function(event) {
         if (event.which == 13) {
                var goto_url = $('#loginBtn').data("goto_url");
                event.preventDefault();
                login.loginbox.login($("#username").val(), $("#password").val(), goto_url);

            }
        });

        $('#loginBtn').die();
         $('#loginBtn').click(
            function() {
                var goto_url = $('#messageModal').data("goto_url");
                login.loginbox.login($("#username").val(), $("#password").val(), goto_url);
            }
         );
    };
    var showLoginForm = function(){
        $('#messageModal').html($('#login-data').html());
        $('#messageModal').modal('show').data("goto_url", null);
        $('#username').focus();

         registerEventsForLogin();
    };



    $("#logout-link").click(function () {
        login.loginbox.logout();
    });

    $(".need-login").click(function() {
        $('#messageModal').html($('#login-data').html());
        $('#messageModal').modal('show').data("goto_url", $(this).attr("href"));
        $('#username').focus();

        registerEventsForLogin();
        return false;
    });


    $('#login-link').click(showLoginForm);


});
