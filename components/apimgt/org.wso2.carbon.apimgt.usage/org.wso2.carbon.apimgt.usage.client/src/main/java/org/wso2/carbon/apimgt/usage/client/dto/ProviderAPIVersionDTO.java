package org.wso2.carbon.apimgt.usage.client.dto;

public class ProviderAPIVersionDTO {

    String version;
    String count;

    public String getCount(){
        return count;
    }

    public String getVersion(){
        return version;
    }

    public void setCount(String count){
        this.count = count;
    }

    public void setVersion(String version){
        this.version = version;
    }

    public ProviderAPIVersionDTO(String version,String count){
        this.version = version;
        this.count = count;
    }
}
