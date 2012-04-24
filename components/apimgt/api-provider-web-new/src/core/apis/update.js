var updateAPI = function(apiName, version, description, imageUrl, endpoint, wsdl, tags, tier,
                         status, context,
                         uriTemplateArr, uriMethodArr) {
    var log = new Log();
    var apiProvider = require("/core/greg/greg.js").getAPIProviderObj();
    var user = require("/core/users/users.js").getUser();
    var providerName = user.username;
    try {
        var success = apiProvider.updateAPI(providerName, apiName, version, description, imageUrl, endpoint, wsdl, tags, tier, status, context, request, uriTemplateArr, uriMethodArr);
        if (log.isDebugEnabled()) {
            log.debug("updateAPI : " + apiName + "-" + version);
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
