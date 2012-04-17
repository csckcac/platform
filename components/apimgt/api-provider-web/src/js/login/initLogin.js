var result = APIProviderAppUtil.makeSyncRequest(apiProviderApp.url, 'action=getUser');
if (apiProviderApp.isUserLoggedIn()) {
    location.href = "index.jag";
}else{
    $(document).ready(
            function(){
                $('#username').focus();
                var loginForm = $("#loginForm");
                loginForm.validation();

                $('#username').keydown(function(event){
                    if (event.which == 13) {
                        event.preventDefault();
                        login();
                    }
                });
                $('#pass').keydown(function(event){
                    if (event.which == 13) {
                        event.preventDefault();
                        login();
                    }
                });
            }
    );
}
