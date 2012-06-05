$(document).ready(function() {
    $.validator.addMethod("matchPasswords", function(value) {
		return value == $("#newPassword").val();
	}, "The passwords you entered do not match.");

    $("#sign-up").validate({
     submitHandler: function(form) {
       jagg.post("/site/blocks/user/sign-up/ajax/user-add.jag", {
            action:"addUser",
            username:$('#newUsername').val(),
            password:$('#newPassword').val()
        }, function (result) {
            if (result.error == false) {
                jagg.message('User added success');
                location.href = context;
            } else {
                jagg.message(result.message);
            }
        }, "json");
     }
    });
    $("#newPassword").keyup(function() {
        $(this).valid();
    });
    $('#newPassword').focus(function(){
        $('#password-help').show();
    });
    $('#newPassword').blur(function(){
        $('#password-help').hide();
    });
});