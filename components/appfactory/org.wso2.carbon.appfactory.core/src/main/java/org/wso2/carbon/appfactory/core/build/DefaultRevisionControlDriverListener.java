package org.wso2.carbon.appfactory.core.build;

import org.wso2.carbon.appfactory.core.BuildDriver;
import org.wso2.carbon.appfactory.core.RevisionControlDriverListener;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;

public class DefaultRevisionControlDriverListener implements RevisionControlDriverListener {

	public void onGetSourceCompleted(String applicationId, String version, String revision) {
		BuildDriver buildDriver = ServiceHolder.getBuildDriver();
		DefaultBuildDriverListener listener = new DefaultBuildDriverListener();
		buildDriver.buildArtifact(applicationId, version, revision, listener);
	}

}
