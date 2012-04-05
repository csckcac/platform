package org.wso2con.feedback.db;

public class FeedbackResult {


	public FeedbackResult(String userId, String session, int rating,
			String question, String comment) {
		super();
		this.userId = userId;
		this.session = session;
		this.rating = rating;
		this.question = question;
		this.comment = comment;
	}

	private String userId;
	private String session;
	private int rating;
	private String question;
	private String comment;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
