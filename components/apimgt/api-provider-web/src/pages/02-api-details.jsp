<h2>API1 - v1.0.0</h2>

<ul id="tab" class="nav nav-tabs tabs">
    <li class="active"><a href="#view" data-toggle="tab" id="viewLink"><i class="icon-file"></i>Overview</a></li>
    <li><a href="#edit" data-toggle="tab" id="editLink"><i class="icon-edit"></i>Edit</a></li>
    <li><a href="#versions" data-toggle="tab" id="versionsLink"><i class="icon-th-list"></i>Versions</a></li>
    <li><a href="#docs" data-toggle="tab" id="docsLink"><i class="icon-file"></i>Docs</a></li>
    <li><a href="#users" data-toggle="tab" id="usersLink"><i class="icon-user"></i>API Users</a></li>
</ul>

<div id="myTabContent" class="tab-content">
    <div class="tab-pane fade active in" id="view">
        <jsp:include page="02-api-details-view.jsp" />
    </div>
    <div class="tab-pane fade" id="edit">
        <jsp:include page="02-api-details-edit.jsp" />
    </div>
    <div class="tab-pane fade" id="versions">
        <jsp:include page="02-api-details-versions.jsp" />
    </div>
    <div class="tab-pane fade" id="docs">
        <jsp:include page="02-api-details-docs.jsp" />
    </div>
    <div class="tab-pane fade" id="users">
        <jsp:include page="02-api-details-users.jsp" />
    </div>
</div>

<script src="js/02-api-details.js"></script>

<%--Simple tempory effect to simulate doc select on page load--%>
<script type="text/javascript">
    $(document).ready(
            function(){
                <% if(request.getParameter("tab")!= null && request.getParameter("tab").equals("users")){ %>
                       showTab('usersLink');
                <% } %>
            }
            );
</script>