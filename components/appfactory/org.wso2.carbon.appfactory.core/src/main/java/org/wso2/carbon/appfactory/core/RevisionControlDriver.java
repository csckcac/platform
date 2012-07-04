package org.wso2.carbon.appfactory.core;

import org.wso2.carbon.appfactory.common.AppFactoryException;

/**
 * Implementations of this will communicate with the revision control driver
 */
public interface RevisionControlDriver {

	/**
	 * This method is responsible for copying the code from rvision control to the file system
	 * 
	 * @param applicationId
	 * @param version
	 * @param revision
	 */
	public void getSource(String applicationId, String version, String revision,
	                      RevisionControlDriverListener listener) throws AppFactoryException;

}
