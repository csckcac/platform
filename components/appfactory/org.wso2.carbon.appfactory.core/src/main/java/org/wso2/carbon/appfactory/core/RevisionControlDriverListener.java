package org.wso2.carbon.appfactory.core;

import org.wso2.carbon.appfactory.common.AppFactoryException;

public interface RevisionControlDriverListener {

	public void onGetSourceCompleted(String applicationId, String version, String revision)
	                                                                                       throws AppFactoryException;
}
