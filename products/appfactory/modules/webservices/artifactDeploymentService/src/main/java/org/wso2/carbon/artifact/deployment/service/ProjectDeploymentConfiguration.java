package org.wso2.carbon.artifact.deployment.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectDeploymentConfiguration {
    private String svnBaseURL;
    private Map<String, List<String>> deploymentServerLocations = new HashMap<String, List<String>>();

    public String getSvnBaseURL() {
        return svnBaseURL;
    }

    public void setSvnBaseURL(String svnBaseURL) {
        this.svnBaseURL = svnBaseURL;
    }

    public void addDeploymentServerLocations(String stage, List<String> locations) {
        deploymentServerLocations.put(stage, locations);
    }

    public List<String> getDeploymentServerLocations(String stage) {
        return deploymentServerLocations.get(stage);
    }
}