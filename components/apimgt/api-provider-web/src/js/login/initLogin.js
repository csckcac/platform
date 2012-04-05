var result = APIProviderAppUtil.makeSyncRequest(apiProviderApp.url, 'action=getUser');
if (apiProviderApp.isUserLoggedIn()) {
    location.href = "index.jag";
}
