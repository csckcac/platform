$(document).ready(function() {
    $.validator.addMethod("matchPasswords", function(value) {
		return value == $("#password").val();
	}, "The passwords you entered do not match.");

    $("#sign-up").validate({
     submitHandler: function(form) {
       form.submit();
     }
    });
    $("#password").keyup(function() {
        $(this).valid();
    });
    $('#password').focus(function(){
        $('#password-help').show();
    });
    $('#password').blur(function(){
        $('#password-help').hide();
    });
});