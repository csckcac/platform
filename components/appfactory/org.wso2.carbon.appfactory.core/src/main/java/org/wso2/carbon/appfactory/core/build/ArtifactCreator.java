package org.wso2.carbon.appfactory.core.build;

import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.appfactory.core.RevisionControlDriver;
import org.wso2.carbon.core.AbstractAdmin;

/**
 * Basic artifact creator uses
 * 
 */
public class ArtifactCreator extends AbstractAdmin {

	public void createArtifact(String applicationId, String version, String revision) throws AppFactoryException{
		if (ServiceHolder.getContinuousIntegrationSystemDriver() != null) {
			// TODO : we are yet to define a continuous build system driver
		} else {
			DefaultRevisionControlDriverListener listener =
			                                                new DefaultRevisionControlDriverListener();
			RevisionControlDriver revisionControlDriver = ServiceHolder.getRevisionControlDriver();
			revisionControlDriver.getSource(applicationId, version, revision, listener);
		}
	}
}
