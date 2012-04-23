
function getUser() {
    //TODO : implement getUser

    return {
        username:session.get("username"),
        cookie:session.get("cookie")
    };

}

