package org.wso2.carbon.appfactory.core;

public interface RevisionControlDriver {
    
    public void copySource(String applicationId, String version, String revision);

}
