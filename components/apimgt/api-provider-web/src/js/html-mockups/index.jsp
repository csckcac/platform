<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>API Store Providers</title>
    <jsp:include page="includes/header-includes.jsp" />


  </head>

  <body>
    <jsp:include page="includes/header.jsp" />

    <div class="container-fluid content-section">
      <div class="row-fluid">
        <div class="span2">
             <jsp:include page="includes/left-menu.jsp" />
        </div><!--/span-->
        <div class="span10">
            <%
                String place = request.getParameter("place");
            %>
            <ul class="breadcrumb">
                <% if(place == null || place.equals("")){%>
                    <li>
                    APIs <span class="divider">/</span>
                    </li>
                    <li>
                        <a href="?place=">My APIs</a>
                    </li>
                <% } %>
                <% if(place != null && place.equals("api-details")){%>
                <li>
                    APIs <span class="divider">/</span>
                </li>
                <li>
                    <a href="?place=">My APIs</a>
                </li>
                <li>
                    <span class="divider">/</span>
                </li>
                <li>
                    <a href="?place=api-details">API1 - v1.0.0</a>
                </li>
                <% } %>


                <% if(place != null && place.equals("add")){%>
                <li>
                    APIs <span class="divider">/</span>
                </li>
                <li>
                    <a href="?place=">My APIs</a>
                </li>
                <li>
                    <span class="divider">/</span>
                </li>
                <li>
                    <a href="?place=add">Add New API</a>
                </li>
                <% } %>



                <% if(place != null && place.equals("user")){%>
                <li>
                    Manage <span class="divider">/</span>
                </li>
                <li>
                    <a href="?place=users_keys">Users-Keys</a>
                </li>
                <li>
                    <span class="divider">/</span>
                </li>
                <li>
                    <a href="?place=user">User - Chamith</a>
                </li>
                <% } %>


                <% if(place != null && place.equals("users_keys")){%>
                <li>
                    Manage <span class="divider">/</span>
                </li>
                <li>
                    <a href="?place=users_keys">Users-Keys</a>
                </li>
                <% } %>
            </ul>

            <% if(place == null || place.equals("")){%><jsp:include page="pages/01-home.jsp" /><% } %>
            <% if(place != null && place.equals("api-details")){%><jsp:include page="pages/02-api-details.jsp" /><% } %>
            <% if(place != null && place.equals("add")){%><jsp:include page="pages/03-add.jsp" /><% } %>
            <% if(place != null && place.equals("user")){%><jsp:include page="pages/05-user.jsp" /><% } %>
            <% if(place != null && place.equals("users_keys")){%><jsp:include page="pages/06-users_keys.jsp" /><% } %>
        </div><!--/span-->
      </div><!--/row-->

      <hr>

      <footer>
        <jsp:include page="includes/footer.jsp" />
      </footer>

    </div><!--/.fluid-container-->
     <jsp:include page="includes/js-imports.jsp" />

  </body>
</html>
