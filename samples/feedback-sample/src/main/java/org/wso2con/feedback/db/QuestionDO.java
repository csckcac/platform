package org.wso2con.feedback.db;

public class QuestionDO {

    private int questionId; 
    private int sessionId; 
    private String text;
    private int type;
    
    public QuestionDO(int questionId, int sessionId, int type, String text) {
        super();
        this.questionId = questionId;
        this.sessionId = sessionId;
        this.text = text;
        this.type = type;
    }
    public int getQuestionId() {
        return questionId;
    }
    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }
    public int getSessionId() {
        return sessionId;
    }
    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    
    
    
}
