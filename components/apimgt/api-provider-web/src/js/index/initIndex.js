$(document).ready(function () {
    if (apiProviderApp.isUserLoggedIn()) {
        $('#userNameShow').html(apiProviderApp.loggedUser);
        $('#apiDeatils').html(apiProviderApp.loggedUser);
        getAPIsByProvider(renderAPIs);

    } else {
        location.href = "login.jag";
    }
});
