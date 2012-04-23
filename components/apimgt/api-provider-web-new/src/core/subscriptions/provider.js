var getSubscribersOfProvider = function () {
    var subscribersOut = [];
    var subscribersArray = [];
    var apiProvider = require("/core/greg/greg.js").getAPIProviderObj();
    var user = require("/core/user/user.js").getUser();
    var provider = user.username;
    try {
        subscribersArray = apiProvider.getAllAPIUsageByProvider(provider);
        if (log.isDebugEnabled()) {
            log.debug("getSubscribersOfProvider : " + stringify(subscribersArray));
        }
        if (subscribersArray == null) {
            return {
                error:true
            };

        } else {
            for (var k = 0; k < subscribersArray.length; k++) {
                var elem = {
                    userName:subscribersArray[k].userName,
                    application:subscribersArray[k].application,
                    apis:subscribersArray[k].apis
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

