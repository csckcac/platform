var createNewAPIVersion = function(apiName, version, newVersion) {
    var apiProvider = require("/core/greg/greg.js").getAPIProviderObj();
    var user = require("/core/user/user.js").getUser();
    var providerName = user.username;
    try {
        var success = apiProvider.createNewAPIVersion(providerName, apiName, version, newVersion);
        if (log.isDebugEnabled()) {
            log.debug("createNewAPIVersion for : " + apiName + "-" + version + " as " + apiName + "-" + newVersion);
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
