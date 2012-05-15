package org.wso2.carbon.appfactory.artifact.deployment.service;

public interface ProjectDeployingService {
    /**
     * Deploy project artifacts after checkout, build
     *
     * @param projectSvnUrl - project svn location to checkout
     * @param projectId     - name of the project
     * @param artifactType  - artifact type extension
     * @param stage         - stage name(Development,QA,LIVE)
     * @return artifacts contains file name and size in bytes
     * @throws ProjectDeploymentExceptions
     */
    Artifact[] deployProject(String projectSvnUrl, String projectId, String artifactType,
                             String stage)
            throws ProjectDeploymentExceptions;

}
