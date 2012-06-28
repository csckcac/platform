var i18n = {
    localeResourcesBasePath:'',
    localizations:{},

    init:function (req) {
        locale = req.getLocale();
        this.localizations = require(this.localeResourcesBasePath + 'locale_' + locale + '.json');
    },

    getLocalString:function (key, fallback) {
        if (this.localizations[key]) {
            return this.localizations[key]
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