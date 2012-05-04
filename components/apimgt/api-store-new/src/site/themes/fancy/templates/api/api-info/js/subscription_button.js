$(document).ready(function(){
    $('#apiSubscribeButton').click(function(){
        $('#apiSubscribeButton').html('Please wait...').removeClass('green').addClass('disabled').attr('disabled','disabled');
        //Use the following if you want to set this the other way round
        //$('#apiSubscribeButton').html('Subscribe').addClass('green').removeClass('disabled').removeAttr('disabled');
    });
});