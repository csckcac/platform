$(document).ready(function() {
    $("#myform").validate({
     submitHandler: function(form) {
       form.submit();
     }
    });
    $("#sign-up").validate();
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