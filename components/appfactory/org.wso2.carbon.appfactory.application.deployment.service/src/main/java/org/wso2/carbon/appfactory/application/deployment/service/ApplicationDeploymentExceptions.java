package org.wso2.carbon.appfactory.application.deployment.service;

public class ApplicationDeploymentExceptions extends Exception {
    
    private static final long serialVersionUID = 527953914068285778L;

    public ApplicationDeploymentExceptions() {
    }

    public ApplicationDeploymentExceptions(String message) {
        super(message);
    }

    public ApplicationDeploymentExceptions(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationDeploymentExceptions(Throwable cause) {
        super(cause);
    }
}