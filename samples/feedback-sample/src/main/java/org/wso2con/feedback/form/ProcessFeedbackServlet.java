package org.wso2con.feedback.form;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2con.feedback.db.FeedbackAppDAO;
import org.wso2con.feedback.db.FeedbackDO;
import org.wso2con.feedback.db.QuestionDO;

public class ProcessFeedbackServlet extends HttpServlet {

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
	
		String user = request.getParameter("user");
		FeedbackAppDAO.initialize(jdbcURL, username, password);

		FeedbackAppDAO dao = FeedbackAppDAO.getInstance();
		int userId = -1;
		String result = "success";

		try {
			
			userId = dao.getUser(user);
			QuestionDO[] quests = dao.getQuestions();
			FeedbackDO[] dos = new FeedbackDO[quests.length];
			int noOfQuestions = quests.length;

			for (int i = 0; i < noOfQuestions; i++) {
				String rating = request.getParameter("qr"
						+ quests[i].getQuestionId() + "s"
						+ quests[i].getSessionId());
				rating = (rating == null) ? "0" : rating;
				rating = (rating.equals("")) ? "0" : rating;
				int ratingVal = Integer.parseInt(rating);
				String comment = request.getParameter("qc"
						+ quests[i].getQuestionId() + "s"
						+ quests[i].getSessionId());
				comment = (comment == null) ? "N/A" : comment;
				comment = comment.equals("") ? "N/A" : comment;
				dos[i] = new FeedbackDO(quests[i].getQuestionId(),
						quests[i].getSessionId(), ratingVal, comment);
			}
			if (userId == -1) {
				log.info("Inserting feedback for the user " + user);
				dao.addFeedbacks(user, dos);
			} else {
				log.info("Updating feedback for the user " + user);
				dao.updateFeedbacks(user, dos);
			}

		} catch (Exception e1) {
			log.info("Process feedback failed " + e1.getStackTrace());
			e1.printStackTrace();
			result = "error";
		}
		if (result.equals("error")) {
			response.sendRedirect(path + "/feedback/response.jsp?message=" + result);
		} else  {
			response.sendRedirect(path + "/feedback/feedbackForm.jsp?updated=" + true);
		}
		
		
		
	}

}
