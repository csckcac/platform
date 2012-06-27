var i18n = {
    localeResourcesBasePath:'',

    init:function (req) {
        locale = req.getLocale();
    },

    getLocalString:function (key, fallback) {
        var localizations = require(this.localeResourcesBasePath + 'locale_' + locale + '.json');
        if (localizations[key]) {
            return localizations[key]
        } else {
            return  key;
        }

    },

    localize:function (key, fallback) {
        var localized = this.getLocalString(key);
        if (localized !== key) {
            return localized;
        } else {
            return fallback;
        }
    }
}