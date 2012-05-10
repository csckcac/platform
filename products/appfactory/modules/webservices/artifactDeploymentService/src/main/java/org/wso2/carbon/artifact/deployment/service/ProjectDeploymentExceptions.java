package org.wso2.carbon.artifact.deployment.service;

public class ProjectDeploymentExceptions extends Exception{
    public ProjectDeploymentExceptions() {
    }

    public ProjectDeploymentExceptions(String message) {
        super(message);
    }

    public ProjectDeploymentExceptions(String message, Throwable cause) {
        super(message, cause);
    }

    public ProjectDeploymentExceptions(Throwable cause) {
        super(cause);
    }
}