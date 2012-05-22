<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.QueryDTO" %>
<%@ page import="java.util.LinkedList" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
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
        Set<QueryDTO> queriesToRemove = new HashSet<QueryDTO>();
        if (queries != null) {
            LinkedList<QueryDTO> currentQueries = (LinkedList<QueryDTO>) session.getAttribute("queries");
            if (currentQueries != null) {
                for (QueryDTO query : currentQueries){
                    for (String removeQuery : queries){
                        if (query.getName().equals(removeQuery)){
                            queriesToRemove.add(query);
                        }
                    }
                }

                for (QueryDTO queryToRemove : queriesToRemove) {
                    currentQueries.remove(queryToRemove);
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