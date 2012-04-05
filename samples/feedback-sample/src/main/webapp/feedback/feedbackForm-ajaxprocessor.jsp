<%@ page import="org.wso2con.feedback.db.FeedbackAppDAO" %>
<%@ page import="org.wso2con.feedback.db.QuestionDO" %>
<%@ page import="org.wso2con.feedback.db.FeedbackDO" %>
<%
    /*
          * Get the value of form parameter
          */


    String user = (String) session.getAttribute("user");
    ServletContext context = getServletContext();
    String jdbcURL = context.getInitParameter("jdbcURL");
    String username = context.getInitParameter("username");
    String password = context.getInitParameter("password");
    String result = "success";

    FeedbackAppDAO.initialize(jdbcURL, username, password);
    FeedbackAppDAO dao = FeedbackAppDAO.getInstance();
    int userId = -1;

    try {
        userId = dao.getUser(user);
        QuestionDO[] quests = dao.getQuestions();
        FeedbackDO[] dos = new FeedbackDO[quests.length];
        int noOfQuestions = quests.length;

        if (userId == -1) {
            //Inserting feedback for the user
            for (int i = 0; i < noOfQuestions; i++) {
                String rating = null;
                rating = (rating == null) ? "0" : rating;
                rating = (rating.equals("")) ? "0" : rating;
                int ratingVal = Integer.parseInt(rating);
                String comment = null;
                comment = (comment == null) ? "N/A" : comment;
                comment = comment.equals("") ? "N/A" : comment;
                dos[i] = new FeedbackDO(quests[i].getQuestionId(),
                        quests[i].getSessionId(), ratingVal, comment);
            }
            dao.addFeedbacks(user, dos);
        }
        // Updating feedback for the user
        FeedbackDO[] dos1 = new FeedbackDO[1];
        String rating = request.getParameter("rating");
        String comment = request.getParameter("comment");
        int ratingVal = Integer.parseInt(rating);

        String buttonId = request.getParameter("buttonId");

        String[] ids = buttonId.split("s");

        int questionId = Integer.parseInt(ids[0]);
        int sessionId = Integer.parseInt(ids[1]);

        rating = (rating == null) ? "0" : rating;
        rating = (rating.equals("")) ? "0" : rating;

        ratingVal = Integer.parseInt(rating);
        comment = (comment == null) ? "N/A" : comment;
        comment = comment.equals("") ? "N/A" : comment;

        dos1[0] = new FeedbackDO(questionId, sessionId, ratingVal, comment);
        dao.updateFeedbacks(user, dos1);


    } catch (Exception e1) {
        e1.printStackTrace();
    }
    if (result.equals("error")) {
        session.setAttribute("result", "error");
    } else {
        session.setAttribute("result", "success");
    }


%>
