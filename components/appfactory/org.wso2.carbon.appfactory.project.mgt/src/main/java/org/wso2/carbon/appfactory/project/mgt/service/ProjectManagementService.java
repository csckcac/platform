package org.wso2.carbon.appfactory.project.mgt.service;

/**
 *Interface for project management web service
 */
public interface ProjectManagementService {
    /**
     * Create a project which will do following tasks
     * 1.Create a tenant(In Stratos POV)
     * //2.Create a SVN repository
     * 3.Add meta data to governance registry(later)
     * @param project
     * @return
     */
    String createProject(ProjectInfoBean project);

    /**
     * Add the user to project which actually
     * add a user to tenant
     * @param projectKey
     * @param userName
     * @return
     */
    boolean addUserToProject(String projectKey,String userName);

    /**
     *
     * @param projectKey
     * @param userName
     * @return
     */
    boolean removeUserFromProject(String projectKey,String userName);

    /**
     *
     * @param projectKey
     * @return
     */
    boolean revokeProject(String projectKey);

    /**
     *
     * @param projectKey
     * @return
     */
    boolean isProjectKeyAvailable(String projectKey);

    /**
     *
     * @param userName
     * @return
     */
    String getEmailOfUser(String userName);
    
}
