package org.wso2con.feedback.form;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProcessLogin extends HttpServlet {

	private static Log log = LogFactory.getLog(ProcessLogin.class);

	private String path;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ServletContext context = getServletContext();
		path = context.getInitParameter("path");

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		/*
		 * Get the value of form parameter
		 */
		String name = request.getParameter("name");
		String password = request.getParameter("password");
		String encoded_password = URLEncoder.encode(password, "UTF-8");
		String encoded_name = URLEncoder.encode(name, "UTF-8");
		
		HttpClient httpclient = new HttpClient();
		PostMethod post = new PostMethod(
				"https://teswso2.org/services/rest/ws/login.xml");
		post.setRequestEntity(new StringRequestEntity("mail=" + encoded_name
				+ "&password=" +encoded_password, "application/x-www-form-urlencoded", "utf-8"));
	
		int result = httpclient.executeMethod(post);
		 request.getSession().setAttribute("user", name);
		if (result == 200) {
			log.info(name + " logged Successfully");
			

			response.sendRedirect(path + "/feedback/feedbackForm.jsp");
		} else {
			log.warn(name + " login failed Return Code "+result);
			response.sendRedirect(path + "/feedback/login-error.jsp");

		}

	}
}
