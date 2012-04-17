var apiProviderApp = new function () {

    this.url = "/apiprovider/services/provider.jag";
    this.storeUrl = "/apistore/services/registry.jag";
    this.loggedUser = null;
    this.currentProviderName = '';
    this.currentAPIName = '';
    this.currentVersion = '';
    this.currentDocName = '';
    this.currentAPI = {};
    /**
     *
     * @param data
     * @param callback
     */
    this.call = function (data, callback) {
        var path = $("#path").val();
        var htmlResult;
        if (APIProviderAppUtil.isUndefined(callback) || callback == null) {
            callback = function (result) {
                    //console.log(JSON.stringify(result));
            };
        }
        APIProviderAppUtil.makeJsonRequest(apiProviderApp.url, data, callback);
    };
    
    this.saveDocContent = function (apiName, version,docName,docContent) {
        apiProviderApp.call("action=addInlineContent&apiName=" + apiName + "&version=" + version+ "&docName=" + docName+ "&docContent=" + encodeURI(docContent), function (json) {
            if (json.error == "true") {
                alert(json.message);
            }
            else {
            	 alert('Saved '+json.data.message);
            }
        });

    };
    /**
     *
     * @param data
     * @param callback
     */
    this.callStore = function (data, callback) {
        var path = $("#path").val();
        var htmlResult;
        if (APIProviderAppUtil.isUndefined(callback) || callback == null) {
            callback = function (result) {
                    //console.log(JSON.stringify(result));
            };
        }
        APIProviderAppUtil.makeJsonRequest(apiProviderApp.storeUrl, data, callback);
    };

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


    //logout operation
    this.logout = function () {
        var loginCallback = function (result) {
            if (result.error == "false") {
                apiProviderApp.loggedUser = null;
                //$('#userNameShow').html('');
                location.href = "login.jag";
            }
        };

        apiProviderApp.call("action=logout", loginCallback);
    };

    //Check if user is logged in
    this.isUserLoggedIn = function () {
        if (apiProviderApp.loggedUser == null || apiProviderApp.loggedUser == '') {
            var result = APIProviderAppUtil.makeSyncRequest(apiProviderApp.url, 'action=getUser');
            if (!jQuery.isEmptyObject(result.data.username) || typeof(result.data.username) == "string") {
                apiProviderApp.loggedUser = result.data.username;
                return true;
            } else {
                return false;
            }

        } else {
            return true;
        }
    };

    this.searchAPIs = function () {
        var searchQ = $('#apiSearch').val();
        getAPIsByProvider(function (jsonRes) {
            var searchObj;

            var rec = function (a) {
                var b;
                for (var i = 0; i < a.length; i++) {
                    if (a[i].name.toUpperCase().search(searchQ.toUpperCase()) < 0) {
                        a.splice(i, 1);
                        rec(a);
                    }
                }
                return a;
            }

            jsonRes.data.apis = rec(jsonRes.data.apis);

            renderAPIs(jsonRes);
        });
    }


}
