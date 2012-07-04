package org.wso2.carbon.appfactory.core;

import java.io.File;

import org.wso2.carbon.appfactory.common.AppFactoryException;

/**
 * Listens to the events of the BuildDriver
 */
public interface BuildDriverListener {
    
	/**
	 * Called upon successful build
	 * 
	 * @param applicationId
	 * @param version
	 * @param revision
	 * @param file
	 * @throws AppFactoryException
	 */
    public void onBuildSuccessful(String applicationId, String version,
            String revision, File file)  throws AppFactoryException;
    
    /**
     * Called upon build failure
     * 
     * @param applicationId
     * @param version
     * @param revision
     * @param file
     * @throws AppFactoryException
     */
    public void onBuildFailure(String applicationId, String version,
                                  String revision, File file)  throws AppFactoryException;

}
