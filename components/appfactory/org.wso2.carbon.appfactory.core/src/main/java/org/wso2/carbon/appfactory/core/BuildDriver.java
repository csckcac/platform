package org.wso2.carbon.appfactory.core;

public interface BuildDriver {
    
    public void buildArtifact(String applicationId, String version, String revision);

}
