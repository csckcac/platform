package org.wso2.carbon.appfactory.core;

import org.wso2.carbon.appfactory.common.AppFactoryException;

/**
 * Listens to the events of RevisionControlDriver
 */
public interface RevisionControlDriverListener {

	/**
	 * This even will be called when the source code is checked o ut
	 * 
	 * @param applicationId
	 * @param version
	 * @param revision
	 * @throws AppFactoryException
	 */
	public void onGetSourceCompleted(String applicationId, String version, String revision)
	                                                                                       throws AppFactoryException;
}
