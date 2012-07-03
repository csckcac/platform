package org.wso2.carbon.appfactory.core;

public interface BuildDriverListener {
    
    public void onBuildCompleted(String applicationId, String version,
            String revision);

}
