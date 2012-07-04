package org.wso2.carbon.appfactory.core.build;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.ArtifactStorage;
import org.wso2.carbon.appfactory.core.BuildDriverListener;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;


public class DefaultBuildDriverListener implements BuildDriverListener {

	private static final Log log = LogFactory.getLog(DefaultBuildDriverListener.class);
	
	@Override
    public void onBuildSuccessful(String applicationId, String version, String revision, File file) {
        ArtifactStorage storage = ServiceHolder.getArtifactStorage();
	    storage.storeArtifact(applicationId, version, revision, file);
	    // TODO call the bpel back
    }

	@Override
    public void onBuildFailure(String applicationId, String version, String revision, File file)
                                                                                                throws AppFactoryException {
		
	    
    }

}
