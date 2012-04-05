package org.wso2.carbon.registry.search.ui.report.beans;

/**
 * Created by IntelliJ IDEA.
 * User: aravinda
 * Date: 6/23/11
 * Time: 11:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetaDataReportBean {

 private String resourcePath;
 private String createdDate;
 private String authorName;
 private String averageRating;

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(String averageRating) {
        this.averageRating = averageRating;
    }

}
