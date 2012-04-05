<%@ page import="org.wso2.carbon.cep.stub.admin.internal.xsd.InputDTO" %>
<%@ page import="java.util.LinkedList" %>
<%
    String[] inputs = request.getParameterValues("inputs");
    String pageNumber = request.getParameter("pageNumber");
    String deleteAllInputs = request.getParameter("deleteAllinputs");
    String bucketName = request.getParameter("bucketName");
    int pageNumberInt = 0;
    String message = "";

    if (pageNumber != null) {
        pageNumberInt = Integer.parseInt(pageNumber);
    }

    if (deleteAllInputs != null) {
        session.setAttribute("inputs", new LinkedList<InputDTO>());
        message = "All inputs removed succesfully";
    } else {
        if (inputs != null) {
            LinkedList<InputDTO> currentInputs = (LinkedList<InputDTO>) session.getAttribute("inputs");
            if (currentInputs != null) {
                int i = inputs.length - 1;
                while (i >= 0) {
                    currentInputs.remove(Integer.parseInt(inputs[i]));
                    i--;
                }
                message = "Specified inputs removed successfully";
            }
        }
    }
%>
<%=message%>
<script type="text/javascript">
    location.href = 'cep_buckets.jsp?inputsPageNumber=<%=pageNumberInt%>'
</script>