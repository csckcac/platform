$(document).ready(function () {
    if (apiProviderApp.isUserLoggedIn()) {    	
        	$('#apiDeatils').empty().html('<p><h1> '+apiProviderApp.currentDocName+ '</h1></p>');
       
    } else {
        location.href = "login.jag";
    }
});