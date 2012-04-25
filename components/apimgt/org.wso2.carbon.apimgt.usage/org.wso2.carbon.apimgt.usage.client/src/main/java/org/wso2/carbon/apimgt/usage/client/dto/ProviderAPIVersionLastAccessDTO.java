package org.wso2.carbon.apimgt.usage.client.dto;

public class ProviderAPIVersionLastAccessDTO {
    String api;
    String version;
    String lastAccess;

    public String getLastAccess(){
        return lastAccess;
    }

    public String getVersion(){
        return version;
    }

    public String getApi(){
        return api;
    }

    public void setLastAccess(String lastAccess){
        this.lastAccess = lastAccess;
    }

    public void setVersion(String version){
        this.version = version;
    }

    public void setApi(String api){
        this.api = api;
    }

    public ProviderAPIVersionLastAccessDTO(String api, String version, String lastAccess){
        this.api = api;
        this.version = version;
        this.lastAccess = lastAccess;
    }
}
