var removeDocumentation = function(apiName, version, docName, docType) {
    var apiProvider = require("/core/greg/greg.js").getAPIProviderObj();
    var user = require("/core/users/users.js").getUser();
    var log = new Log();
    var providerName = user.username;
    try {
        var success = apiProvider.removeDocumentation(providerName, apiName, version, docName, docType);
        if (log.isDebugEnabled()) {
            log.debug("removeDocumentation : " + docName);
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