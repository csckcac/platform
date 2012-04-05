package org.wso2con.feedback.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Data access object of feedback application.
 */
public class FeedbackAppDAO {

    private DataSource dataSource = null;
    private static Log log = LogFactory.getLog(FeedbackAppDAO.class);

    /**
     * JDK5 and later extends the semantics for volatile so that the
     * system will not allow a write of a volatile to be reordered with respect
     * to any previous read or write, and a read of a volatile cannot be
     * reordered with respect to any following read or write.
     */
    private volatile static FeedbackAppDAO feedbackAppDAO = null;

    private FeedbackAppDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static FeedbackAppDAO getInstance() {
        if (feedbackAppDAO == null) {
            throw new RuntimeException("Please initialize the application first");
        }
        return feedbackAppDAO;
    }

    /**
     * The initialization with volatile double locking
     * 
     * @param url
     *            - The URL of the RSS
     * @param userName
     *            - The username of RSS
     * @param password
     *            - The password of RSS
     */
    public static void initialize(String url, String userName, String password) {
        if (feedbackAppDAO == null) {
            synchronized (FeedbackAppDAO.class) {
                if (feedbackAppDAO == null) {
                    BasicDataSource ds = new BasicDataSource();
                    ds.setDriverClassName("com.mysql.jdbc.Driver");
                    ds.setUrl(url);
                    ds.setUsername(userName);
                    ds.setPassword(password);
                    ds.setValidationQuery("SELECT 1");
                    feedbackAppDAO = new FeedbackAppDAO(ds);
                }
            }
        }
    }
    
    public String getUserName(int userId) {
    	   Connection dbConnection = null;
           PreparedStatement prepStmt = null;
           ResultSet rs = null;
           String userName = "";
           try {
               dbConnection = getDBConnection();
               prepStmt = dbConnection.prepareStatement(SQLConstants.GET_USER_NAME);
               prepStmt.setInt(1, userId);
               rs = prepStmt.executeQuery();
               if (rs.next()) {
            	   userName = rs.getString(1);
               }
           } catch (SQLException e) {
               log.error(e.getMessage(), e);
               rollBack(dbConnection);
           } finally {
               closeAll(dbConnection, rs, prepStmt);
           }
           return userName;
    	
    }

    public int getUser(String userName) throws Exception {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        int id = -1;
        try {
            dbConnection = getDBConnection();
            prepStmt = dbConnection.prepareStatement(SQLConstants.GET_USER);
            prepStmt.setString(1, userName);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            rollBack(dbConnection);
        } finally {
            closeAll(dbConnection, rs, prepStmt);
        }
        return id;
    }

    public void updateFeedbacks(String userName, FeedbackDO[] feedbacks) throws Exception {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            prepStmt = dbConnection.prepareStatement(SQLConstants.UPDATE_FEEDBAK);
            for (FeedbackDO feedbak : feedbacks) {
                prepStmt.setInt(1, feedbak.getRating());
                prepStmt.setString(2, feedbak.getComment());
                prepStmt.setInt(3, feedbak.getqId());
                prepStmt.setInt(4, feedbak.getSessionId());
                prepStmt.setString(5, userName);
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
            dbConnection.commit();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            rollBack(dbConnection);
        } finally {
            closeAll(dbConnection, null, prepStmt);
        }
    }

    public void addFeedbacks(String userName, FeedbackDO[] feedbacks) throws Exception {
        Connection dbConnection = null;
        PreparedStatement prepStmt2 = null;
        PreparedStatement prepStmt1 = null;
        ResultSet rs = null;
        try {
            dbConnection = getDBConnection();
            prepStmt1 = dbConnection.prepareStatement(SQLConstants.INSERT_USER);
            prepStmt1.setString(1, userName);
            prepStmt1.executeUpdate();
            prepStmt2 = dbConnection.prepareStatement(SQLConstants.INSERT_FEEDBAK);
            for (FeedbackDO feedbak : feedbacks) {
                prepStmt2.setInt(1, feedbak.getqId());
                prepStmt2.setString(2, userName);
                prepStmt2.setInt(3, feedbak.getSessionId());
                prepStmt2.setInt(4, feedbak.getRating());
                prepStmt2.setString(5, feedbak.getComment());
                prepStmt2.addBatch();
            }
            prepStmt2.executeBatch();
            dbConnection.commit();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            rollBack(dbConnection);
        } finally {
            closeAll(dbConnection, rs, prepStmt1, prepStmt2);
        }
    }

    public FeedbackDO[] getFeedbacks(String userName) throws SQLException {
        FeedbackDO[] feedbacks = new FeedbackDO[0];
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            dbConnection = getDBConnection();
            prepStmt = dbConnection.prepareStatement(SQLConstants.GET_FEEDBAK);
            prepStmt.setString(1, userName);
            rs = prepStmt.executeQuery();
            List<FeedbackDO> lst = new ArrayList<FeedbackDO>();
            while (rs.next()) {
                int qId = rs.getInt("WC_Q_ID");
                int sessionId = rs.getInt("WC_SESSION_ID");
                int rating = rs.getInt("WC_RATING");
                String comment = rs.getString("WC_COMMENT");
                FeedbackDO feedback = new FeedbackDO(qId, sessionId, rating, comment);
                lst.add(feedback);
            }
            feedbacks = (FeedbackDO[]) lst.toArray(new FeedbackDO[lst.size()]);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            rollBack(dbConnection);
        } finally {
            closeAll(dbConnection, rs, prepStmt);
        }
        return feedbacks;
    }
    
    
    public FeedbackResult[] getFeedbacks(int sessionId,int type) throws SQLException {
    	FeedbackResult[] feedbacks = new FeedbackResult[0];
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            dbConnection = getDBConnection();
            if (type == -1) { //overall 
            	 prepStmt = dbConnection.prepareStatement(SQLConstants.GET_OVERALL_FEEDBACK_RESULTS);
            } else {
            	prepStmt = dbConnection.prepareStatement(SQLConstants.GET_FEEDBACK_RESULTS);
            }
            
           
            prepStmt.setInt(1, sessionId);
            rs = prepStmt.executeQuery();
            List<FeedbackResult> lst = new ArrayList<FeedbackResult>();
            while (rs.next()) {
                String usrId = rs.getString("WC_USER_NAME");
                String session = rs.getString("SESSION");
                String question = rs.getString("QUESTION");
                int rating = rs.getInt("WC_RATING");
                String comment = rs.getString("WC_COMMENT");
                
                FeedbackResult feedback = new FeedbackResult(usrId,session,rating,question,comment);
                lst.add(feedback);
            }
            feedbacks = (FeedbackResult[]) lst.toArray(new FeedbackResult[lst.size()]);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            rollBack(dbConnection);
        } finally {
            closeAll(dbConnection, rs, prepStmt);
        }
        return feedbacks;
    }
    
 
	public static HashMap<Integer, FeedbackResultStat> calculateOverallResult()
			throws SQLException {
		HashMap<Integer, FeedbackResultStat> results = new HashMap<Integer, FeedbackResultStat>();
		HashMap<Integer, FeedbackResultStat> contentResults = getInstance()
				.calculateOverallResults("Content");
		HashMap<Integer, FeedbackResultStat> locationResults = getInstance()
				.calculateOverallResults("Location and food");
		HashMap<Integer, FeedbackResultStat> experianceResults = getInstance()
				.calculateOverallResults("Conference experience");
		HashMap<Integer, FeedbackResultStat> commentResults = getInstance()
		.calculateOverallResults("Any Other Comment");
		FeedbackResultStat answerCountsCont = contentResults.get(1);
		FeedbackResultStat answerCountsLoc = locationResults.get(1);
		FeedbackResultStat answerCountsExp = experianceResults.get(1);
		FeedbackResultStat answerCountsComment = commentResults.get(1);
		results.put(0, answerCountsCont);
		results.put(1, answerCountsLoc);
		results.put(2, answerCountsExp);
		results.put(3, answerCountsComment);
		return results;
	}
    
	
    
    public HashMap<Integer, FeedbackResultStat> calculateOverallResults(String questionText) throws SQLException {
    	FeedbackResult fbresults[] = getInstance().getFeedbacks(1,-1);
    	HashMap<Integer, FeedbackResultStat> feedbackMap = new HashMap<Integer, FeedbackResultStat>();
    	String session = fbresults[0].getSession();
		FeedbackResultStat fbresultStat = new FeedbackResultStat();
		ArrayList<String> comments = new ArrayList<String>();
		String question = "";
    	for (int x = 0; x < fbresults.length; x++) {
    		question = fbresults[x].getQuestion();
    		if (questionText.equals(question)) {
    			fbresultStat.setQuestion(question);
    			fbresultStat.setSession(session);
    			fbresultStat.incrementAnswerCount(fbresults[x].getRating());
    			String comment = fbresults[x].getComment();
    			String userId = fbresults[x].getUserId();
    			if (comment != null && !comment.trim().equals("") && !comment.equals("N/A") ) {
    				comments.add(comment+" :- "+userId);
    			}
    		}
			
		}
		fbresultStat.setComments(comments);
		feedbackMap.put(1, fbresultStat);
		return feedbackMap;
    }
    
    public HashMap<Integer, FeedbackResultStat> calculateSessionResults() throws SQLException {

    	HashMap<Integer, FeedbackResultStat> feedbackMap = new HashMap<Integer, FeedbackResultStat>();
    	for (int i=2;i<40;i++) {
    		FeedbackResult fbresults[] = getInstance().getFeedbacks(i,0);
    		String question = fbresults[0].getQuestion();
    		String session = fbresults[0].getSession();
    		FeedbackResultStat fbresultStat = new FeedbackResultStat();
    		
    		ArrayList<String> comments = new ArrayList<String>();
			for (int x = 0; x < fbresults.length; x++) {
				fbresultStat.incrementAnswerCount(fbresults[x].getRating());
				String comment = fbresults[x].getComment();
				String userId = fbresults[x].getUserId();
				if (comment != null && !comment.trim().equals("") && !comment.equals("N/A") ) {
    				comments.add(comment+" :- "+userId);
				}
			}
		
    		fbresultStat.setQuestion(question);
    		fbresultStat.setSession(session);
    		fbresultStat.setComments(comments);
    		feedbackMap.put(i, fbresultStat);
    	}
    	return feedbackMap;
    }

    public void addQuestions(QuestionDO[] questions) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            prepStmt = dbConnection.prepareStatement(SQLConstants.ADD_QUESTION);
            for (QuestionDO question : questions) {
                prepStmt.setInt(1, question.getQuestionId());
                prepStmt.setInt(2, question.getSessionId());
                prepStmt.setString(3, question.getText());
                prepStmt.setInt(4, question.getType());
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
            dbConnection.commit();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            rollBack(dbConnection);
        } finally {
            closeAll(dbConnection, null, prepStmt);
        }
    }

    public void updateQuestions(QuestionDO[] questions) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            prepStmt = dbConnection.prepareStatement(SQLConstants.UPDATE_QUESTION);
            for (QuestionDO question : questions) {
                prepStmt.setString(1, question.getText());
                prepStmt.setInt(2, question.getType());
                prepStmt.setInt(3, question.getQuestionId());
                prepStmt.setInt(4, question.getSessionId());
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
            dbConnection.commit();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            rollBack(dbConnection);
        } finally {
            closeAll(dbConnection, null, prepStmt);
        }
    }

    public void deleteQuestion(int questionId, int sessionId) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        try {
            dbConnection = getDBConnection();
            prepStmt = dbConnection.prepareStatement(SQLConstants.DELETE_QUESTION);
            prepStmt.setInt(1, questionId);
            prepStmt.setInt(2, sessionId);
            prepStmt.executeUpdate();
            dbConnection.commit();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            rollBack(dbConnection);
        } finally {
            closeAll(dbConnection, null, prepStmt);
        }
    }
    
    public SessionDO[] getSessions() {
        SessionDO[] sessions = new SessionDO[0];
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            dbConnection = getDBConnection();
            prepStmt = dbConnection.prepareStatement(SQLConstants.GET_SESSIONS);
            rs = prepStmt.executeQuery();
            List<SessionDO> lst = new ArrayList<SessionDO>();
            while (rs.next()) {
                int sessionId = rs.getInt("WC_ID"); 
                String text = rs.getString("WC_TEXT");
                String date = rs.getDate("WC_DATE").toString();
                String duration = rs.getString("WC_DURATION");
                SessionDO session = new SessionDO(sessionId, text, date, duration);
                lst.add(session);
            }
            sessions = (SessionDO[]) lst.toArray(new SessionDO[lst.size()]);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            rollBack(dbConnection);
        } finally {
            closeAll(dbConnection, rs, prepStmt);
        }
        return sessions;
    }
       
    public QuestionDO[] getQuestions() throws SQLException {
        QuestionDO[] questions = new QuestionDO[0];
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            dbConnection = getDBConnection();
            prepStmt = dbConnection.prepareStatement(SQLConstants.GET_QUESTIONS);
            rs = prepStmt.executeQuery();
            List<QuestionDO> lst = new ArrayList<QuestionDO>();
            while (rs.next()) {
            	int qId = rs.getInt("WC_Q_ID");
            	int sessionId = rs.getInt("WC_SESSION_ID");
                String text = rs.getString("WC_TEXT");
                int type = rs.getInt("WC_TYPE");
                QuestionDO question = new QuestionDO(qId, sessionId, type, text);
                lst.add(question);
            }
            questions = (QuestionDO[]) lst.toArray(new QuestionDO[lst.size()]);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            rollBack(dbConnection);
        } finally {
            closeAll(dbConnection, rs, prepStmt);
        }
        return questions;
    }
    
    
    
    public QuestionDO[] getQuestions(int sessionId) throws SQLException {
        QuestionDO[] questions = new QuestionDO[0];
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            dbConnection = getDBConnection();
            prepStmt = dbConnection.prepareStatement(SQLConstants.GET_QUESTIONS_BY_SESSION);
            prepStmt.setInt(1, sessionId);
            rs = prepStmt.executeQuery();
            List<QuestionDO> lst = new ArrayList<QuestionDO>();
            while (rs.next()) {
                int qId = rs.getInt("WC_Q_ID");
                String text = rs.getString("WC_TEXT");
                int type = rs.getInt("WC_TYPE");
                QuestionDO question = new QuestionDO(qId, sessionId, type, text);
                lst.add(question);
            }
            questions = (QuestionDO[]) lst.toArray(new QuestionDO[lst.size()]);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            rollBack(dbConnection);
        } finally {
            closeAll(dbConnection, rs, prepStmt);
        }
        return questions;
    }
    
    private Connection getDBConnection() throws SQLException {
        Connection dbConnection = dataSource.getConnection();
        dbConnection.setAutoCommit(false);
        dbConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        return dbConnection;
    }

    private void rollBack(Connection dbConnection) {
        try {
            if (dbConnection != null) {
                dbConnection.rollback();
            }
        } catch (SQLException e) {
            log.error("Error rolling back transaction" + e.getMessage(), e);
        }
    }

    private void closeAll(Connection dbConnection, ResultSet rs, PreparedStatement... prepStmts) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (prepStmts != null && prepStmts.length > 0) {
                for (PreparedStatement stmt : prepStmts) {
                    stmt.close();
                }
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        } catch (SQLException e) {
            log.error("Error closing connections " + e.getMessage(), e);
        }
    }
}
