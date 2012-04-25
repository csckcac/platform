package org.wso2.carbon.apimgt.usage.client.dto;

public class ProviderAPIVersionUsageDTO {

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

    public ProviderAPIVersionUsageDTO(String version, String count){
        this.version = version;
        this.count = count;
    }
}
