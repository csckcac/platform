function logout() {
    session.put("username", null);
    session.put("cookie", null);
    return {
        error:false
    };
}