var cookie_date = new Date();  // current date & time
cookie_date.setTime(cookie_date.getTime() + oneDay);

/*Getting a cookie value from it's name*/
function get_cookie(check_name) {
    // first we'll split this cookie up into name/value pairs
    // note: document.cookie only returns name=value, not the other components
    var a_all_cookies = document.cookie.split(';');
    var a_temp_cookie = '';
    var cookie_name = '';
    var cookie_value = '';
    var b_cookie_found = false; // set boolean t/f default f

    for (i = 0; i < a_all_cookies.length; i++)
    {
        a_temp_cookie = a_all_cookies[i].split('=');
        cookie_name = a_temp_cookie[0].replace(/^\s+|\s+$/g, '');
        if (cookie_name == check_name)
        {
            b_cookie_found = true;
            if (a_temp_cookie.length > 1)
            {
                cookie_value = unescape(a_temp_cookie[1].replace(/^\s+|\s+$/g, ''));
            }
            return cookie_value;
            break;
        }
        a_temp_cookie = null;
        cookie_name = '';
    }
    if (!b_cookie_found)
    {
        return null;
    }
}
function set_cookie(check_name,value){
    document.cookie = check_name + "="+value+";path=/;expires=" + cookie_date.toGMTString();
}
function delete_cookie(check_name){
    var now = new Date();
    now.setTime(now.getTime() - oneDay);
    document.cookie = check_name + "=;path=/;expires=" + now.toGMTString();
}
function get_date_cookies(){
    //assigning cookie values to global variables
    if(get_cookie(startHr_cookie) != null){
        startHr = parseFloat(get_cookie(startHr_cookie));
    }
    if(get_cookie(endHr_cookie) != null){
        endHr = parseFloat(get_cookie(endHr_cookie));
    }
    if(get_cookie(startMonth_cookie) != null){
        startMonth = parseFloat(get_cookie(startMonth_cookie));
    }
    if(get_cookie(endMonth_cookie) != null){
        endMonth = parseFloat(get_cookie(endMonth_cookie));
    }
    if(get_cookie(startDay_cookie) != null){
        startDay = parseFloat(get_cookie(startDay_cookie));
    }
    if(get_cookie(endDay_cookie) != null){
        endDay = parseFloat(get_cookie(endDay_cookie));
    }
    if(get_cookie(endDay_cookie) != null){
        pageMode = parseFloat(get_cookie(pageMode_cookie));
    }
}