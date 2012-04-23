var getSubscribersOfAPI = function (apiName, version) {
    var apiProvider = require("/core/greg/greg.js").getAPIProviderObj();
    var user = require("/core/user/user.js").getUser();
    var providerName = user.username;
    var subscribersOut = [];
    var subscribersArray = [];
    try {
        subscribersArray = apiProvider.getSubscribersOfAPI(providerName, apiName, version);
        if (log.isDebugEnabled()) {
            log.debug("getSubscribersOfAPI : " + stringify(subscribersArray));
        }
        if (subscribersArray == null) {
            return {
                error:true
            };

        } else {
            for (var k = 0; k < subscribersArray.length; k++) {
                var elem = {
                    userName:subscribersArray[k].userName,
                    subscribedDate:subscribersArray[k].subscribedDate
                };
                subscribersOut.push(elem);
            }
            return {
                error:false,
                subscribers:subscribersOut
            };
        }
    } catch (e) {
        log.error(e.message);
        return {
            error:e,
            subscribers:null
        };
    }

};
