var getAllPublishedAPIs = function () {
    var log = new Log();
    var store = require("/core/greg/greg.js").getAPIStoreObj();

    try {
        var list = [];
        var apis = store.getAllPublishedAPIs();
        if (log.isDebugEnabled()) {
            log.debug("getAllPublishedAPIs : " + stringify(apis));
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