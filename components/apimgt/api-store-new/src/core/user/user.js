var login = function (username, password) {
    var greg = require("/core/greg/greg.js");
    var gregClient = require("/core/greg/clients.js");
    var url = greg.getServerURL() + "services/AuthenticationAdmin";
    var remoteAddress = greg.getServer();
    var payload =
            <aut:login xmlns:aut="http://authentication.services.core.carbon.wso2.org">
                <aut:username>{username}</aut:username>
                <aut:password>{password}</aut:password>
                <aut:remoteAddress>{remoteAddress}</aut:remoteAddress>
            </aut:login>;
    var result = gregClient.invokeService(null, "AuthenticationAdmin", "urn:login", payload);
    if (result.error) {
        log("Error while authenticating user : " + username + " at " + remoteAddress, "error");
        return {
            error:result.error,
            cookie:null
        };
    }
    var xml = result.client.responseE4X;
    var ns = "http://authentication.services.core.carbon.wso2.org";
    var status = xml.ns::["return"].text().toString();
    if (status == "false") {
        log("Error verifying credentials for user : " + username + " at " + remoteAddress, "error");
        return {
            error:true,
            cookie:null
        };
    }
    /*var subscriber = apiStore.getSubscriber(username);
     if(!subscriber) {
     apiStore.addSubscriber(username);
     subscriber = apiStore.getSubscriber(username);
     }
     log(username);
     log(xml);*/
    var cookie = result.client.getResponseHeader("Set-Cookie");
    log(cookie);
    cookie = cookie.split(';')[0];
    /*session.put("username", username);
     session.put("userId", subscriber.id);
     session.put("cookie", cookie);*/
    return {
        error:false,
        cookie:cookie
    };
};

function logout() {
    //TODO : implement logout
    /*session.put("username", null);
     session.put("cookie", null);*/
    return {
        error:false
    };
}


