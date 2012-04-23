var saveAPI = function(apiName, version, description, endpoint, wsdl, tags, tier, thumbUrl, context,
                       uriTemplateArr, uriMethodArr) {
    var log = new Log();
    var apiProvider = require("/core/greg/greg.js").getAPIProviderObj();
    var user = require("/core/users/users.js").getUser();
    var provider = user.username;
    try {
        var success = apiProvider.addAPI(provider, apiName, version, description, endpoint, wsdl, tags, tier, thumbUrl, context, request, uriTemplateArr, uriMethodArr);
        if (log.isDebugEnabled()) {
            log.debug("addAPI : " + apiName + "-" + version);
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

var isContextExist = function(context) {
    try {
        var contextExist = apiProvider.isContextExist(context);
        if (log.isDebugEnabled()) {
            log.debug("isContext exist for : " + context + " : " + contextExist);
        }
        return {
            error:false,
            contextExist:contextExist
        };
    } catch (e) {
        log.error(e.message);
        return {
            error:e

        };
    }

};
