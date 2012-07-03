package org.wso2.carbon.appfactory.core;

import java.io.File;

/**
 * This is the artifact storage.
 * The built artifacts are stored in this storage for retrieval by the deployment service
 */
public interface ArtifactStorage {
    
    /**
     * This will retrieve the artifact as a file.
     * This is a blocking call.
     * 
     * @param applicationId Application Id
     * @param version Version of the artifact
     * @param revision The revision of the artifacct
     * @return
     */
    public File retrieveArtifact(String applicationId, String version, String revision);
    
    /**
     * This will store the artifact in the artifact storage.
     * 
     * @param applicationId Application Id
     * @param version  Version of the artifact
     * @param revision The revision of the artifact
     * @param file The artifact
     */
    public void storeArtifact(String applicationId, String version, String revision, File file);

}
