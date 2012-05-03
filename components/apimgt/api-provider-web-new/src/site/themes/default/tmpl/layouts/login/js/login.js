//the login operation
this.login = function () {
    var name = $("#username").val();
    var pass = $("#pass").val();


    var result = APIProviderAppUtil.makeSyncRequest(apiProviderApp.url, "action=login&username=" + name + "&password=" + pass);
    //debugger;
    if (result.error == "true") {
        $('#loginError').show('fast');
        $('#loginErrorSpan').html('<strong>Unable to log you in!</strong><br />'+result.message);
        return false;
    }
    else {
        apiProviderApp.loggedUser = name;
        location.href = 'index.jag';
    }
};
