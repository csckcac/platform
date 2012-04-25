var getRecentlyAddedAPIs = function (limit) {
    var log = new Log();
    var store = require("/core/greg/greg.js").getAPIStoreObj();

    try {
        var list = [];
        var apis = store.getRecentlyAddedAPIs(limit);
        if (log.isDebugEnabled()) {
            log.debug("getRecentlyAddedAPIs : " + stringify(apis));
        }
        var i, length = apis.length;
        for (i = 0; i < length; i++) {
            var api = apis[i];
            list.push({
                name:api.name,
                provider:api.provider,
                version:api.version,
                description:api.description,
                rating:api.rates,
                subscribed:api.subscribed,
                thumbURL:api.thumbnailurl,
                tier:api.tier,
                context:api.context
            });
        }
        return {
            error:false,
            apis:list
        };
    } catch (e) {
        log.error(e.message);
        return {
            error:e,
            apis:null
        };
    }
};