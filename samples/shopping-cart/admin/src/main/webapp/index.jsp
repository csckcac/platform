<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <jsp:include page="admin_header_includes.jsp"/>
    <%--Additional Includes--%>
</head>
<body>
<div class="pageSizer">
    <jsp:include page="menu.jsp"/>
    <div class="clear"></div>
    <div class="content">
        <div class="home-top-msg">
            <table>
                <tbody>
                <tr>
                    <td align="left">ACME Administration Panel</td>
                    <td align="right"><a href="logout.jsp">Logout</a></td>
                </tr>
                </tbody>
            </table>
        </div>
        <table class="contentTable">
            <tr>
                <td class="leftCol">
                    <div class="leftMenu">
                        <div class="leftMenu-inside">
                            <h2 class="menuHeader">Categories</h2>
                            <div class="catagories-block" id="categories_list"></div>
                        </div>
                    </div>
                </td>
                <td class="contentCol" id="admin_content">
                </td>
            </tr>
        </table>
    </div>
</div>

<jsp:include page="footer.jsp"/>

</body>
</html>