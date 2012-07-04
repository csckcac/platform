package org.wso2.carbon.appfactory.core;

import java.io.File;

import org.wso2.carbon.appfactory.common.AppFactoryException;

public interface BuildDriverListener {
    
    public void onBuildCompleted(String applicationId, String version,
            String revision, File file)  throws AppFactoryException;

}
