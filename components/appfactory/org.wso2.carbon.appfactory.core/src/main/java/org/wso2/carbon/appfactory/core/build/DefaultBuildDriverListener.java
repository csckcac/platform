package org.wso2.carbon.appfactory.core.build;

import java.io.File;

import org.wso2.carbon.appfactory.core.ArtifactStorage;
import org.wso2.carbon.appfactory.core.BuildDriverListener;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;

public class DefaultBuildDriverListener implements BuildDriverListener {

	public void onBuildCompleted(String applicationId, String version, String revision, File file) {
		ArtifactStorage storage = ServiceHolder.getArtifactStorage();
		storage.storeArtifact(applicationId, version, revision, file);
		// TODO call the bpel back
	}

}
