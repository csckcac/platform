package org.wso2.carbon.registry.indexing.solr;

public class IndexDocument {
	
	private String path;
	private String contentAsText;
	private String rawContent;
	private int tenantId;
	
	public IndexDocument(String path, String rawContent, String contentAsText) {
		this.path = path;
	    this.contentAsText = contentAsText;
	    this.rawContent = rawContent;
	}

	public IndexDocument(String path, String contentAsText, String rawContent, int tenantId) {
	    this.path = path;
	    this.contentAsText = contentAsText;
	    this.rawContent = rawContent;
	    this.tenantId = tenantId;
    }

	public String getPath() {
    	return path;
    }

	public void setPath(String path) {
    	this.path = path;
    }

	public String getContentAsText() {
    	return contentAsText;
    }

	public void setContentAsText(String contentAsText) {
    	this.contentAsText = contentAsText;
    }

	public String getRawContent() {
    	return rawContent;
    }

	public void setRawContent(String rawContent) {
    	this.rawContent = rawContent;
    }

	public int getTenantId() {
    	return tenantId;
    }

	public void setTenantId(int tenantId) {
    	this.tenantId = tenantId;
    }
	
	
}
