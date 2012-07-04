package org.wso2.carbon.appfactory.core;

import org.wso2.carbon.appfactory.common.AppFactoryException;

/**
 * This will drive the build.
 * Maven2, Maven3 and Ant are some possible implementations of this
 */
public interface BuildDriver {

	/**
	 * Trigger the build.
	 * 
	 * @param applicationId
	 * @param version
	 * @param revision
	 */
	public void buildArtifact(String applicationId, String version, String revision,
	                          BuildDriverListener listener) throws AppFactoryException;

}
