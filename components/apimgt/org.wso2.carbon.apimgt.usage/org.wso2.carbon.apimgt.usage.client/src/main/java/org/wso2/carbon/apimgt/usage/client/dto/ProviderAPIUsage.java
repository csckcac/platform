package org.wso2.carbon.apimgt.usage.client.dto;


public class ProviderAPIUsage {
    
    String apiName;
    double count;

    public ProviderAPIUsage(String apiName, double count) {
        this.apiName = apiName;
        this.count = count;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }
}
