var logout = function () {
    jagg.post("/site/blocks/user/login/ajax/login.jag", {action:"logout"}, function (result) {
        if (!result.error) {
            var current = window.location.pathname;
            if (current.indexOf(".jag") >= 0) {
                location.href = "login.jag";
            } else {
                location.href = 'site/pages/login.jag';
            }
        } else {
            jagg.message(result.message);
        }
    }, "json");
};


