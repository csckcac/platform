 var logout = function () {
    jagg.post("/site/blocks/user/login/ajax/login.jag", {action:"logout"}, function (result) {
        if (!result.error) {
            location.href = 'login.jag';
        } else {
            jagg.message(result.message);
        }
    }, "json");
};


