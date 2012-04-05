<%@ page import="org.wso2con.feedback.db.FeedbackAppDAO" %>
<%@ page import="org.wso2con.feedback.db.FeedbackResultStat"%>
<%@ page import="java.util.HashMap"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<link rel="stylesheet" href="../css/stat.css">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>WSO2Con 2011 - Feedback Statistics</title>
</head>
<body>

 <% 
 ServletContext context = getServletContext();
 String jdbcURL = context.getInitParameter("jdbcURL");
   String username = context.getInitParameter("username");
    String password = context.getInitParameter("password");
try {
	FeedbackAppDAO.initialize(jdbcURL,username, password);
	FeedbackAppDAO dao = FeedbackAppDAO.getInstance();
	HashMap<Integer, FeedbackResultStat> results = dao.calculateOverallResult();
	context.setAttribute("feedbackMap", results);
	%>
	
		<div id="middle">

		<table align="center" class="tableStyle" border="1">
			<tr>
				<td colspan="8">Feedback Rating statistics</td>
			</tr>
			<tr>
				<td width="50%"><b>Question </b>
				</td>
			
				<td><b>Not Answered</b></td>
				<td><b>Very Poor</b></td>
				<td><b>Poor</b></td>
				<td><b>Good</b></td>
				<td><b>Very good</b></td>
				<td><b>Excellent</b></td>
			</tr>
			<%
			for (int i= 0;i<results.size()-1;i++) {
	    		 FeedbackResultStat answerCounts = results.get(i);
	       		 if (answerCounts != null) {
	            		 HashMap<Integer, Integer> answerMap = answerCounts.getAnswerMap(); 
	            		 %>
	            		 	<tr>
							<td width="50%"><b><%=answerCounts.getQuestion()%></b></td> 
	             		<%int count =0;
	            		 for (int x=0;x<answerMap.size();x++) {
	            			 %>
								<td><%=answerMap.get(x) %></td> 
		             		<%
	            			 count = count+answerMap.get(x);
	            		 }
	       
	       		 }
			}
			%>
			

		</table>

		<form method="post" action="../SaveFeedbackServlet">
			<table>
				<tr>
					<td colspan="2"><input type="submit" value="Save To Excel" /></td>
				</tr>
			</table>
		</form>
	</div>
</body>
</html>
	<%
} catch (Exception e) {
%>
			<script type="text/javascript">
location.href = "timeout_error.jsp";
</script>
			<%
}
%>


