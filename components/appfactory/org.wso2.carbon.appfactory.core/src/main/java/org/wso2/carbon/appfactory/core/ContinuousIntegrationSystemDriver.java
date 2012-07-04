package org.wso2.carbon.appfactory.core;

import org.wso2.carbon.appfactory.common.AppFactoryException;

/**
 * Continuous build system driver 
 *
 */
public interface ContinuousIntegrationSystemDriver {

	/**
	 * Trigger the build.
	 * 
	 * @param applicationId
	 * @param version
	 * @param revision
	 */
	public void buildArtifact(String applicationId, String version, String revision)
	                                                                                throws AppFactoryException;

}
