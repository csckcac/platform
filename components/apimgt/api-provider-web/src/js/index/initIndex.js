$(document).ready(function () {
    if (apiProviderApp.isUserLoggedIn()) {
        $('#apiDeatils').html(apiProviderApp.loggedUser);
        getAPIsByProvider(renderAPIs);

    } else {
        location.href = "login.jag";
    }
});
