
var user = require("/core/users/logout.js");

//logout operation
    this.logout = function () {
        user.logout();
        location.href = "login.jag";
    };