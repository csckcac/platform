package org.wso2con.feedback.form;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2con.feedback.db.FeedbackAppDAO;
import org.wso2con.feedback.db.FeedbackDO;
import org.wso2con.feedback.db.QuestionDO;

public class ProcessQuestions extends HttpServlet {
	private static Log log = LogFactory.getLog(ProcessFeedbackServlet.class);
	private String jdbcURL;
	private String username;
	private String password;
	private String path;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ServletContext context = getServletContext();
		jdbcURL = context.getInitParameter("jdbcURL");
		username = context.getInitParameter("username");
		password = context.getInitParameter("password");
		path = context.getInitParameter("path");
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		/*
		 * Get the value of form parameter
		 */
		String action = request.getParameter("action");
		String id = request.getParameter("id");
		String session = request.getParameter("sesson");
		String question = request.getParameter("question");
		int questionId = Integer.parseInt(id);
		int sessionId = Integer.parseInt(session);
		String questionType = request.getParameter("qtype");
		String status = "success";

		FeedbackAppDAO.initialize(jdbcURL, username, password);
		FeedbackAppDAO dao = FeedbackAppDAO.getInstance();
		if (action.equals("add")) {
			try {
				int type = Integer.parseInt(questionType);
				QuestionDO[] questions = new QuestionDO[1];
				questions[0] = new QuestionDO(questionId, sessionId, type,
						question);
				dao.addQuestions(questions);
			} catch (Exception e1) {
				log.info("Insert process failed " + e1.getStackTrace());
				e1.printStackTrace();
				status = "error";
			}
		}
		if (action.equals("delete")) {
			try {
				dao.deleteQuestion(questionId, sessionId);
			} catch (Exception e1) {
				log.info("Delete process failed " + e1.getStackTrace());
				e1.printStackTrace();
				status = "error";
			}
		}
		if (action.equals("edit")) {
			try {
				int type = Integer.parseInt(questionType);
				QuestionDO[] questions = new QuestionDO[1];
				questions[0] = new QuestionDO(questionId, sessionId, type,
						question);
				dao.updateQuestions(questions);

			} catch (Exception e1) {
				log.info("Insert process failed " + e1.getStackTrace());
				e1.printStackTrace();
				status = "error";
			}
		}

		if (status.equals("error")) {
			response.sendRedirect(path + "/admin/success.jsp?message=" + action);

		} else {
			response.sendRedirect(path + "/admin/error.jsp?message=" + action);

		}

	}

}
