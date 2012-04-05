package org.wso2.carbon.registry.activities.ui.report.beans;

/**
 * Created by IntelliJ IDEA.
 * User: aravinda
 * Date: 6/23/11
 * Time: 10:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class ActivityReportBean {

    private String userName;
    private String activity;
    private String resourcePath;
    private String accessedTime;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getAccessedTime() {
        return accessedTime;
    }

    public void setAccessedTime(String accessedTime) {
        this.accessedTime = accessedTime;
    }
}
