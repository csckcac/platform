package org.wso2.carbon.appfactory.core;

public interface RevisionControlDriverListener {

    public void onGetSourceCompleted(String applicationId, String version, String revision);
}
