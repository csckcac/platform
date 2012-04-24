var updateAPI = function(apiData) {
    var log = new Log();
    var apiProvider = require("/core/greg/greg.js").getAPIProviderObj();
    var user = require("/core/users/users.js").getUser();
    apiData.provider = user.username;
    apiData.request = request;
    try {
        var success = apiProvider.updateAPI(apiData);
        if (log.isDebugEnabled()) {
            log.debug("updateAPI : " + apiData.apiName + "-" + apiData.version);
        }
        if (success) {
            return {
                error:false
            };
        } else {
            return {
                error:true

            };
        }
    } catch (e) {
        log.error(e.message);
        return {
            error:e

        };
    }

};
