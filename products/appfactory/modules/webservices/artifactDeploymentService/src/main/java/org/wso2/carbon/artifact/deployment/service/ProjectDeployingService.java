package org.wso2.carbon.artifact.deployment.service;

public interface ProjectDeployingService {
    /**
     * Deploy project artifacts after checkout, build
     * @param projectId - name of the project as in svn repository
     * @param artifactType - artifact type extension
     * @param stage - stage name(Development,QA,LIVE)
     * @return artifacts contains file name and size in bytes
     * @throws ProjectDeploymentExceptions
     */
    Artifact[] deployProject(String projectSvnUrl, String projectId, String artifactType, String stage)
            throws ProjectDeploymentExceptions;

}
