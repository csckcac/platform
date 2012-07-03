package org.wso2.carbon.appfactory.core;

import java.io.File;

public interface ArtifactStorage {
    
    public File retrieveArtifact(String applicationId, String version, String revision);
    
    public void storeArtifact(String applicationId, String version, String revision, String file);

}
