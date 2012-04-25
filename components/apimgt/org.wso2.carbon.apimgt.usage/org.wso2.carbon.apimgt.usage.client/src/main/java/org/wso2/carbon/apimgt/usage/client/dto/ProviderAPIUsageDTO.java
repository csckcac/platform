package org.wso2.carbon.apimgt.usage.client.dto;


public class ProviderAPIUsageDTO {
    
    String apiName;
    String count;

    public ProviderAPIUsageDTO(String apiName, String count) {
        this.apiName = apiName;
        this.count = count;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
