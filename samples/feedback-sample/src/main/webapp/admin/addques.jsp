<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2con.feedback.db.SessionDO"%>
<%@ page import="org.wso2con.feedback.db.FeedbackAppDAO"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<fmt:bundle basename="org.wso2con.feedback.form.i18n.Resources">

<html>
<head>
  
</head>
<%				String propertyValue="";
				int numberOfQuestions = 48;
				boolean isUserFilled = false;
				ArrayList<String> answerList=null;
				int userId=-1;
				SessionDO[] sessioDoObjs = null;
				ServletContext context = getServletContext();

				 String jdbcURL = context.getInitParameter("jdbcURL");
				   String username = context.getInitParameter("username");
				    String password = context.getInitParameter("password");
				try {
					FeedbackAppDAO.initialize(jdbcURL,username, password);
					FeedbackAppDAO dao = FeedbackAppDAO.getInstance();
					sessioDoObjs =  dao.getSessions();
					
				} catch (Exception e) {
			%>
			<script type="text/javascript">
			    location.href = "timeout_error.jsp";
			</script>
			<%
				}
			%>
<body>

       <form method="POST" action="../ProcessQuestions">
       	<table align="right">
					<tbody>
					<tr>
					    <td align="left"></td>
					    <td align="right"><a href="logout.jsp">Logout</a></td>
					</tr>
					</tbody>
			  </table><br>
       <input type="hidden" id="action" name="action" value="add" />
            <table border="0" cellspacing="5">
                <tr>
                    <th align="right">Question Id:</th>
                    <td align="left"><input id="id" type="text" name="id"/>
                    </td>
                </tr>
                <tr>
                    <th align="right">Session Name:</th>
                    <td align="left">
                     <select id="sesson" name="sesson">
                     <% for (SessionDO sessiondo :sessioDoObjs) {
                    	 int sessionId = sessiondo.getSessionId();
                    	 String sessionDetails = sessiondo.getText();
                    	  %>
                    	  <option value="<%=sessionId%>"><%=sessionDetails%></option>
                    	  <%
                     }
						 
				  %>
                   </select></td>
                </tr>
                <tr>
                    <th align="right">Question :</th>
                    <td align="left">	
                     <textarea cols="50" rows="6" id="question" name="question">
                    </textarea>
				</td>
                </tr>
                <tr>
                    <th align="right">Question Type :</th>
                    <td align="left">
                     <select id="qtype" name="qtype">
					<option value="1">Comment</option>
					<option value="2">Rating</option>
					<option value="0">Yes/No</option>
					<option value="3">Comment/Rating</option>
				</select></td>
                </tr>
                <tr>
                    <td align="right">&nbsp;</td>
                    <td align="left"><input  type="submit" value="add"><input type="reset" value="Cancel">
                    </td>
                </tr>
            </table>
        </form>
</body>
</html>
</fmt:bundle>