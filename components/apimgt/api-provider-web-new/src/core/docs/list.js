var apiProvider = require("/core/greg/greg.js").getAPIProviderObj();
var user = require("/core/users/users.js").getUser();
var log = new Log();

var getAllDocumentation = function(apiName, version) {

    var providerName = user.username;
    var docsOut = [];
    var docs = [];
    try {
        docs = apiProvider.getAllDocumentation(providerName, apiName, version);
        if (log.isDebugEnabled()) {
            log.debug("getAllDocumentation : " + stringify(docs));
        }
        if (docs == null) {
            return {
                error:true
            };

        } else {
            for (var k = 0; k < docs.length; k++) {

                var elem = {
                    docName:docs[k].docName,
                    docType:docs[k].docType,
                    sourceType:docs[k].sourceType,
                    summary:docs[k].summary,
                    lastUpdated:docs[k].docLastUpdated
                };
                if (docs[k].sourceUrl != null) {
                    elem.docUrl = docs[k].sourceUrl;
                }
                docsOut.push(elem);
            }
            return {
                error:false,
                docs:docsOut
            };
        }
    } catch (e) {
        log.error(e.message);
        return {
            error:e,
            docs:null
        };
    }

};

var getInlineContent = function(apiName, version, docName) {
    var providerName = user.username;
    var docOut = [];
    var doc = [];
    try {
        doc = apiProvider.getInlineContent(providerName, apiName, version, docName);
        if (log.isDebugEnabled()) {
            log.debug("getInlineContent for : " + docName);
        }
        var k = 0;
        var elem = {
            apiProvider:providerName,
            apiName:apiName,
            apiVersion:version,
            docName:doc[k].docName,
            docContent:doc[k].content
        };

        docOut.push(elem);

        return {
            error:false,
            doc:docOut
        };
    } catch (e) {
        log.error(e.message);
        return {
            error:e,
            doc:null
        };
    }

};