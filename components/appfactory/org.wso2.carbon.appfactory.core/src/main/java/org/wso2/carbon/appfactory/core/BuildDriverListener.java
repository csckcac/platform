package org.wso2.carbon.appfactory.core;

import java.io.File;

public interface BuildDriverListener {
    
    public void onBuildCompleted(String applicationId, String version,
            String revision, File file);

}
