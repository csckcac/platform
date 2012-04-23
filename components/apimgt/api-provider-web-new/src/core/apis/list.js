var apiProvider = require("/core/greg/greg.js").getAPIProviderObj();
var user = require("/core/users/users.js").getUser();
var log = new Log();

var getAPI = function(apiName, version) {

    var providerName = user.username;
    var apiOut = [];
    var apii = [];
    try {
        apii = apiProvider.getAPI(providerName, apiName, version);
        if (log.isDebugEnabled()) {
            log.debug("getAPI : " + stringify(apii));
        }
        if (apii == null) {
            return {
                error:true
            };

        } else {
            var elem = {
                name:apii[0],
                description:apii[1],
                endpoint:apii[2],
                wsdl:apii[3],
                version:apii[4],
                tags:apii[5],
                availableTiers:apii[6],
                status:apii[7],
                thumb:apii[8],
                context:apii[9],
                lastUpdated:apii[10],
                subs:apii[11],
                templates:apii[12]

            };
            apiOut.push(elem);

            return {
                error:false,
                api:apiOut
            };
        }
    } catch (e) {
        log.error(e.message);
        return {
            error:e,
            api:null
        };
    }
};

var getAPIsByProvider = function() {
    var apisOut = [];
    var apis = [];

    var provider = user.username;
    try {
        apis = apiProvider.getAPIsByProvider(provider);
        if (log.isDebugEnabled()) {
            log.debug("getAPIsByProvider : " + stringify(apis));
        }
        if (apis == null) {
            return {
                error:true
            };
        }
        else {
            for (var k = 0; k < apis.length; k++) {
                var elem = {
                    name:apis[k].apiName,
                    version:apis[k].version,
                    status:apis[k].status,
                    thumb:apis[k].thumb,
                    subs:apis[k].subs

                };
                apisOut.push(elem);
            }
            return {
                error:false,
                apis:apisOut
            };
        }
    } catch (e) {
        log.error(e.message);
        return {
            error:e,
            apis:null
        };
    }

};
