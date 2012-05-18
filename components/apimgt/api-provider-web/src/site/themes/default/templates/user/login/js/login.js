var login = function () {
    var name = $("#username").val();
    var pass = $("#pass").val();
    jagg.post("/site/blocks/user/login/ajax/login.jag", { action:"login", username:name, password:pass },
              function (result) {
                  if (!result.error) {
                      var current = window.location.pathname;
                      if (current.indexOf(".jag") >= 0) {
                          location.href = "index.jag";
                      } else {
                          location.href = 'site/pages/index.jag';
                      }

                  } else {
                      $('#loginError').show('fast');
                      $('#loginErrorSpan').html('<strong>Unable to log you in!</strong><br />' + result.message);
                  }
              }, "json");


};

$(document).ready(
        function() {
            $('#username').focus();
            $('#username').keydown(function(event) {
                if (event.which == 13) {
                    event.preventDefault();
                    login();
                }
            });
            $('#pass').keydown(function(event) {
                if (event.which == 13) {
                    event.preventDefault();
                    login();
                }
            });
        }
        );


