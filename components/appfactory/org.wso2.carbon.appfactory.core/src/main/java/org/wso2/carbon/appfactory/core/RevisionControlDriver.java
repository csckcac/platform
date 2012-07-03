package org.wso2.carbon.appfactory.core;

/**
 * Implementations of this will communicate with the revision control driver
 */
public interface RevisionControlDriver {
    
    /**
     * TODO
     * 
     * @param applicationId
     * @param version
     * @param revision
     */
    public void getSource(String applicationId, String version, String revision);

}
