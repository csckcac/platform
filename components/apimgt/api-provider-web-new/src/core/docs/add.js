var apiProvider = require("/core/greg/greg.js").getAPIProviderObj();
var user = require("/core/users/users.js").getUser();
var addDocumentation = function(apiName, version, docName, docType, summary, sourceType,
                                sourceUrl) {


    var log = new Log();
    var providerName = user.username;
    try {
        var success = apiProvider.addDocumentation(providerName, apiName, version, docName, docType, summary, sourceType, sourceUrl);
        if (log.isDebugEnabled()) {
            log.debug("addNewDocumentation : " + sourceType);
        }
        if (sourceType == "inline") {
            var inlineContent = addInlineContent(apiName, version, docName, "");

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
            error:e,
            docs:null
        };
    }


};


var addInlineContent = function(apiName, version, docName, docContent) {

    var providerName = user.username;
    var docOut = [];
    var doc = [];
    try {
        apiProvider.addInlineContent(providerName, apiName, version, docName, docContent);
        if (log.isDebugEnabled()) {
            log.debug("addInlineContent for : " + docName + "with the content : " + docContent);
        }
        return {
            error:false,
            message:"success"
        };
    } catch (e) {
        log.error(e.message);
        return {
            error:e,
            doc:null
        };
    }

};
