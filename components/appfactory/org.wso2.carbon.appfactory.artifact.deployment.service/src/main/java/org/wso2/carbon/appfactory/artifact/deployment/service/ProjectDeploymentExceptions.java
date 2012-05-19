package org.wso2.carbon.appfactory.artifact.deployment.service;

public class ProjectDeploymentExceptions extends Exception {
    
    private static final long serialVersionUID = 527953914068285778L;

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