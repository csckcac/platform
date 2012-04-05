<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<%
    String deletedFriendsList = "";
    String userDetails = "";
    if (request.getParameter("friendsList") != null && request.getParameter("userDetails") != null) {
        deletedFriendsList = request.getParameter("friendsList");
        userDetails = request.getParameter("userDetails");
    } else {
%>
    <script type="text/javascript">
        location.href = "index.jsp";
    </script>
<%
    }
%>

<html>
<head>
    <title>Facebook friends</title>
    <link href="css/styles.css" rel="stylesheet" type="text/css"/>

    <script type="text/javascript">
        // get the deleted list of friends 
        var deletedFriendsList = <%= deletedFriendsList%>;
        var userDetails = <%= userDetails%>;

        function displayRemovedFriends() {
            // show user details
            var userDiv = document.getElementById("userDetails");
            userDiv.innerHTML = "";
            var username = document.createTextNode(userDetails.name);
            var userlink = document.createElement("a");
            userlink.setAttribute("href", userDetails.link);
            userlink.target = "_blank";
            userlink.appendChild(username);
            var userimage = document.createElement('img');
            userimage.setAttribute("src", userDetails.picture);
            userimage.setAttribute("align", "left");

            userDiv.appendChild(userimage);
            userDiv.appendChild(userlink);

            if (deletedFriendsList.length == 0) {
                var msg = document.createTextNode("No removed friends");
                var label = document.createElement("label").appendChild(msg);

                var detailDiv = document.createElement("div");
                detailDiv.className = "person-entry";
                detailDiv.appendChild(label);

                var container = document.getElementById("friendListDiv");
                container.appendChild(detailDiv);

            } else {
                // iterate through the removed friends and display them
                for (var i = 0; i < deletedFriendsList.length; i++) {
                    var detailDiv = document.createElement("div");
                    detailDiv.className = "person-entry";

                    var txt = document.createTextNode(deletedFriendsList[i].name);
                    var label = document.createElement("label").appendChild(txt);
                    var image = document.createElement('img');
                    image.setAttribute("src", deletedFriendsList[i].picture);
                    image.setAttribute("align", "left");

                    detailDiv.appendChild(image);
                    detailDiv.appendChild(label);

                    var container = document.getElementById("friendListDiv");
                    container.appendChild(detailDiv);
                }
            }
        }
    </script>

</head>
<body onload="displayRemovedFriends();">
<table class="page-table" id="unfreindList">
    <tr>
        <td class="header">FACEBOOK Un-FRIEND FINDER</td>
    </tr>
    <tr>
        <td class="content">
            <div class="login-box">
                <div class="person-entry" id="userDetails">
                </div>
                <fb:login-button auto-logout-link="true" perms="read_friendlists"></fb:login-button>
            </div>
            <div class="login-box" id="friendListDiv" style="margin-top:10px">
                <h2>Friends who have been removed</h2>
            </div>
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

        FB.Event.subscribe('auth.logout', function(response) {
            location.href = "index.jsp";
        });

    };
    // fb async init
    (function() {
        var e = document.createElement('script');
        e.src = document.location.protocol + '//connect.facebook.net/en_US/all.js';
        e.async = true;
        document.getElementById('fb-root').appendChild(e);
    }());
</script>

</body>
</html>