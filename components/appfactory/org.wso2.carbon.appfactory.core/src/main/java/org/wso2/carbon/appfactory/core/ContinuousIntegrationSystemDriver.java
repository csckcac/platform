package org.wso2.carbon.appfactory.core;

public interface ContinuousIntegrationSystemDriver {

    /**
     * Trigger the build. 
     * 
     * @param applicationId
     * @param version
     * @param revision
     */
    public void buildArtifact(String applicationId, String version, String revision);

    
}
