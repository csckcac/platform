package org.wso2.carbon.appfactory.project.mgt.service;

/**
 * Created by IntelliJ IDEA.
 * User: aja
 * Date: 4/30/12
 * Time: 12:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProjectInfoBean {
    private String name;
    private String description;
    private String projectKey;
    private String ownerUserName;

    public String getOwnerUserName() {
        return ownerUserName;
    }
    public void setOwnerUserName(String ownerUserName) {
        this.ownerUserName = ownerUserName;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getProjectKey() {
        return projectKey;
    }
    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }
}
