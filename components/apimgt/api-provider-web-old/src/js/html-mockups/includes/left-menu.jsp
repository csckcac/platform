<%
    String place = request.getParameter("place");
    if(place == null){
        place = "";
    }
%>

<div class="sidebar-nav well">
    <ul class="nav nav-list">
        <li class="nav-header">APIs</li>
        <li <% if(place.equals("") || place.equals("api-details")){%>class="active"<% } %>><a href="?place=">Browse</a></li>
        <li <% if(place.equals("add")){%>class="active"<% } %>><a href="?place=add" >Add</a></li>
        <li class="nav-header">Manage</li>
        <li <% if(place.equals("users_keys")|| place.equals("user")){%>class="active"<% } %>><a href="?place=users_keys">Users/Keys</a></li>
    </ul>
</div>