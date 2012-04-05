<%--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 --%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Facebook un-friend application.</title>
    <script src="js/jquery/jquery-1.5.1.min.js" type="text/javascript"></script>
    <link href="css/styles.css" rel="stylesheet" type="text/css"/>
</head>

<body>

<table class="page-table">
    <tr>
        <td class="header">FACEBOOK Un-FRIEND FINDER</td>
    </tr>
    <tr>
        <td class="content">
            <div class="login-box">
                <h2 id="prompt">Login to facebook</h2>
                <div class="loading" id="loading-div"><p><img src="images/loading.gif"></p></div>
                <fb:login-button auto-logout-link="true" perms="read_friendlists">
                </fb:login-button>
            </div>
        </td>
    </tr>
    <tr>
        <td>
            <form name="freindDetails" method="post" action="list.jsp">
                <input type="hidden" name="friendsList"/>
                <input type="hidden" name="userDetails"/>
            </form>
        </td>
    </tr>
    <tr>
        <td class="footer"></td>
    </tr>
</table>

<div id="fb-root"></div>
<script type="text/javascript" src="http://connect.facebook.net/en_US/all.js"></script>
<script>
    window.fbAsyncInit = function() {
        FB.init({
            //appId  : '133951150008558',
            appId  : '184088328310515',
            status : true, // check login status
            cookie : true, // enable cookies to allow the server to access the session
            xfbml  : true  // parse XFBML
        });

        /* All the events registered */
        FB.Event.subscribe('auth.login', function(response) {
            // get id of current user
            var user_id = response.session.uid;

            // get the current logged in users details and save it 
            FB.api('/me?fields=name,picture,link', function(response) {
                document.forms["freindDetails"].elements["userDetails"].value = JSON.stringify(response);
            });

            // get the friends list
            FB.api('/me/friends?fields=name,picture', function(response) {
                // save the friends list for current user
                var friends = JSON.stringify(response.data);
                persistFriendList(user_id, friends);
            });
        });
        
        FB.Event.subscribe('auth.logout', function(response) {
            // logout handler 
        });

        FB.getLoginStatus(function(response) {
            if (response.session) {
                // current user id
                var user_id = response.session.uid;

                // get the current logged in users details and save it
                FB.api('/me?fields=name,picture,link', function(response) {
                    document.forms["freindDetails"].elements["userDetails"].value = JSON.stringify(response);
                });

                FB.api('/me/friends?fields=name,picture', function(response) {
                    // save the friends list for current user
                    var friends = JSON.stringify(response.data);
                    persistFriendList(user_id, friends);
                });
                //showProfileDetails();
            } else {
            }
        });
    };

    // fb async init
    (function() {
        var e = document.createElement('script');
        e.src = document.location.protocol + '//connect.facebook.net/en_US/all.js';
        e.async = true;
        document.getElementById('fb-root').appendChild(e);
    }());

    function persistFriendList(id, friends) {
        $('#prompt').hide();
        $('#loading-div').show();
        $.post("friendListServlet", {friendList: friends, uid: id}, function(response) {
            // alert(response);
            document.forms["freindDetails"].elements["friendsList"].value = response;
            document.forms["freindDetails"].submit();
        });
    }


</script>

</body>
</html>