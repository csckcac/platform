<%@ page import="org.wso2.carbon.mapred.mgt.stub.HadoopJobRunnerStub" %>
<%@ page import="org.wso2.carbon.mapred.mgt.ui.*" %>
<%@ page import="org.apache.axis2.*" %>
<%@ page import="org.apache.axis2.context.MessageContext" %>
<%@ page import="javax.servlet.http.Cookie" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%
    // Set standard HTTP/1.1 no-cache headers.
    response.setHeader("Cache-Control", "no-store, max-age=0, no-cache, must-revalidate");
    // Set IE extended HTTP/1.1 no-cache headers.
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");
%>

<form method="post">
	<table border="0" cellpadding="0"  cellspacing="0">
		<tr>
			<td>Hadoop Jar: </td>
			<td>
				<input type="text" name="hadoopJarPath"/>
			</td>
		</tr>
		<tr>
			<td>Class Name: </td>
			<td>
				<input type="text" name="hadoopClassName"/>
			</td>
		</tr>
		<tr>
			<td>Input File: </td>
			<td>
				<input type="text" name="hadoopInFile"/>
			</td>
		</tr>
		<tr>
			<td>Output File: </td>
			<td>
				<input type="text" name="hadoopOutFile"/>
			</td>
		</tr>
		<tr>
			<td>
				<input type="submit" value="Submit"/>
			</td>
		</tr>
	</table>
</form>	

<%
    String jarPath = request.getParameter("hadoopJarPath");
    String className = request.getParameter("hadoopClassName");
    String inFile = request.getParameter("hadoopInFile");
    String outFile = request.getParameter("hadoopOutFile");
    Cookie[] cookies = request.getCookies();
    String sessionID = null;
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_AUTH_TOKEN);
	if (jarPath != null) {
      HadoopJobRunnerProxy proxy = new HadoopJobRunnerProxy(request);
	  proxy.submitJob(jarPath, className, inFile+" "+outFile);
	}
%>
