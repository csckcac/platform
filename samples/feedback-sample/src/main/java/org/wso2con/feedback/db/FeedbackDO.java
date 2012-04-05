package org.wso2con.feedback.db;

/**
 * Feedback data object. A place holder for one feedback question.
 */
public class FeedbackDO {

    private int qId = -1;
    private String userId = "";
    private int sessionId = -1;
    private int rating = -1;
    private String comment = "";

    public FeedbackDO(int qId, int sessionId, int rating, String comment) {
        super();
        this.qId = qId;
        this.sessionId = sessionId;
        this.rating = rating;
        this.comment = comment;
    }

    public int getqId() {
        return qId;
    }

    public void setqId(int qId) {
        this.qId = qId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
