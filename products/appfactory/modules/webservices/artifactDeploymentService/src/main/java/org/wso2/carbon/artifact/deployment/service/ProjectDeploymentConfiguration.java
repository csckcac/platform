package org.wso2.carbon.artifact.deployment.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectDeploymentConfiguration {
    private Map<String, List<String>> deploymentServerUrls = new HashMap<String, List<String>>();

    public void addDeploymentServerUrls(String stage, List<String> locations) {
        deploymentServerUrls.put(stage, locations);
    }

    public List<String> getDeploymentServerUrls(String stage) {
        return deploymentServerUrls.get(stage);
    }
}