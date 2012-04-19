$(document).ready(function() {
    $("#service-info").hide();
    $("#purchased-service-list").hide();
    if (!ServiceStoreApp.isUserLoggedIn()) ServiceStoreApp.loginDefault();
    ServiceStoreApp.getSearchKeys(ServiceStoreAppSearchBar.initSearchAutoComplete(services));
    ServiceStoreAppSearchBar.initCategories();


    if (!ServiceStoreApp.loggedUser || ServiceStoreApp.loggedUser == defaultUser) {
        $('#logout-links').hide();
        $('#login-links').show();
    } else {
        $("#userName").text(ServiceStoreApp.loggedUser);
        $('#logout-links').show();
        $('#login-links').hide();
    }
});