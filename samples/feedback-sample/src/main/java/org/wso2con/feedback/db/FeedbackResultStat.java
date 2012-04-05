package org.wso2con.feedback.db;

import java.util.ArrayList;
import java.util.HashMap;

public class FeedbackResultStat {

	private int sessionId;
	private String question;
	private HashMap<Integer, Integer> answerMap;
	private String session;
	private ArrayList<String> comments;
	

	public FeedbackResultStat() {
		answerMap = new HashMap<Integer, Integer>();
		answerMap.put(0, 0);
		answerMap.put(1, 0);
		answerMap.put(2, 0);
		answerMap.put(3, 0);
		answerMap.put(4, 0);
		answerMap.put(5, 0);
	}

	public int getSessionId() {
		return sessionId;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public HashMap<Integer, Integer> getAnswerMap() {
		return answerMap;
	}

	public void setAnswerMap(HashMap<Integer, Integer> answerMap) {
		this.answerMap = answerMap;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}

	public ArrayList<String> getComments() {
		return comments;
	}

	public void setComments(ArrayList<String> comments) {
		this.comments = comments;
	}

	public int getCountForAnswer(int answerId) {
		return answerMap.get(answerId);
	}

	public void incrementAnswerCount(int answerId) {
		int currVal = answerMap.get(answerId);
		answerMap.put(answerId, currVal + 1);
	}
	
	public void addComment(String comment) {
		if (comments == null) {
			comments = new ArrayList<String>();
			comments.add(comment);
		} else {
			comments.add(comment);
		}
	}

}
