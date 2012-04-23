var getAllTags = function() {
    var log = new Log();
    var store = require("/core/greg/greg.js").getAPIStoreObj();

    try {
        var list = [];
        var tags = store.getAllTags();
        if (log.isDebugEnabled()) {
            log.debug("getAllTags : " + stringify(tags));
        }
        var i, j, tagsLength = tags.length;
        for (i = 0; i < tagsLength; i++) {
            var tag = tags[i];
            var count = 1;
            var toSave = true;
            var listLength = list.length;
            for (j = 0; j < listLength; j++) {
                if (list[j].name == tag.name) {
                    list[j].count = parseFloat(list[j].count) + 1;
                    toSave = false;
                }
            }
            if (toSave) {
                list.push({
                    name:tag.name,
                    count:count
                });
            }
        }
        return {
            error:false,
            tags:list
        };
    } catch (e) {
        log.error(e.message);
        return {
            error:e,
            tags:null
        };
    }
};