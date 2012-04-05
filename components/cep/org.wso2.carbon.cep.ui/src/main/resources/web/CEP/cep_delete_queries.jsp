<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.QueryDTO" %>
<%@ page import="java.util.LinkedList" %>
<%
    String[] queries = request.getParameterValues("queries");
    String pageNumber = request.getParameter("pageNumber");
    String deleteAllQueries = request.getParameter("deleteAllqueries");
    String bucketName = request.getParameter("bucketName");
    int pageNumberInt = 0;
    String message = "";

    if (pageNumber != null) {
        pageNumberInt = Integer.parseInt(pageNumber);
    }

    if (deleteAllQueries != null) {
        session.setAttribute("queries", new LinkedList<QueryDTO>());
        message = "Successfully removed all queries";

    } else {
        if (queries != null) {
            LinkedList<QueryDTO> currentQueries = (LinkedList<QueryDTO>) session.getAttribute("queries");
            if (currentQueries != null) {
                int i = queries.length - 1;
                while (i >= 0) {
                    currentQueries.remove(Integer.parseInt(queries[i]));
                    i--;
                }
                message = "Successfully removed specified queries";
            }
        }
    }
%>
<%=message%>
<script type="text/javascript">
    location.href = "cep_buckets.jsp?pageNumber=<%=pageNumberInt%>";
</script>