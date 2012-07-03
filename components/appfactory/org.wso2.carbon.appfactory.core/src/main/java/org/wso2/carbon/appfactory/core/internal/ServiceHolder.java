package org.wso2.carbon.appfactory.core.internal;

import org.wso2.carbon.appfactory.core.ArtifactStorage;
import org.wso2.carbon.appfactory.core.BuildDriver;
import org.wso2.carbon.appfactory.core.ContinuousIntegrationSystemDriver;
import org.wso2.carbon.appfactory.core.RevisionControlDriver;

public class ServiceHolder {

	public static BuildDriver getBuildDriver() {
		return null;
	}

	public static RevisionControlDriver getRevisionControlDriver() {
		return null;
	}

	public static ContinuousIntegrationSystemDriver getContinuousIntegrationSystemDriver() {
		return null;
	}

	public static ArtifactStorage getArtifactStorage() {
		return null;
	}

}
