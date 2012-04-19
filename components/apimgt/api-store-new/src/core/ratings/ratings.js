var getTopRatedAPIs = function (limit) {
    var log = new Log();
    var store = require("/core/greg/greg.js").getAPIStoreObj();

    try {
        var list = [];
        var apis = store.getTopRatedAPIs(limit);
        if (log.isDebugEnabled()) {
            log.debug("getTopRatedAPIs : " + stringify(apis));
        }
        var i, length = apis.length;
        for (i = 0; i < length; i++) {
            var api = apis[i];
            list.push({
                name:api.name,
                provider:api.provider,
                version:api.version,
                description:api.description,
                rates:api.rates,
                purchased:"false",
                thumbURL:api.thumbnailurl
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