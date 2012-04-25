var getAPI = function (name, version, provider, username) {
    var log = new Log();
    var store = require("/core/greg/greg.js").getAPIStoreObj();

    try {
        var obj;
        var api = store.getAPI(provider, name, version, username);
        if (log.isDebugEnabled()) {
            log.debug("getAPIDescription : " + stringify(description));
        }
        api = api[0];
        obj = {
            name:api.name,
            provider:api.provider,
            version:api.version,
            description:api.description,
            rating:api.rates,
            subscribed:api.subscribed,
            thumbURL:api.thumbnailurl,
            tier:api.tier,
            context:api.context
        };
        return {
            error:false,
            api:obj
        };
    } catch (e) {
        log.error(e.message);
        return {
            error:e,
            api:null
        };
    }
};