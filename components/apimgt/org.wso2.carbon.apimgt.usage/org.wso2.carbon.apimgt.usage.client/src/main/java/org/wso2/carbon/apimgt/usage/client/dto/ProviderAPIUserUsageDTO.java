package org.wso2.carbon.apimgt.usage.client.dto;

public class ProviderAPIUserUsageDTO {
    String user;
    String count;

    public String getCount(){
        return count;
    }

    public String getUser(){
        return user;
    }

    public void setCount(String count){
        this.count = count;
    }

    public void setVersion(String user){
        this.user = user;
    }

    public ProviderAPIUserUsageDTO(String user, String count){
        this.user = user;
        this.count = count;
    }
}
