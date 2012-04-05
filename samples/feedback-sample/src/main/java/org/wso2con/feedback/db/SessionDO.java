package org.wso2con.feedback.db;

public class SessionDO {

    private int sessionId;
    private String text;
    private String date;
    private String duration;
    
    public SessionDO(int sessionId, String text, String date, String duration) {
        super();
        this.sessionId = sessionId;
        this.text = text;
        this.date = date;
        this.duration = duration;
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
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getDuration() {
        return duration;
    }
    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    
    
    
}
