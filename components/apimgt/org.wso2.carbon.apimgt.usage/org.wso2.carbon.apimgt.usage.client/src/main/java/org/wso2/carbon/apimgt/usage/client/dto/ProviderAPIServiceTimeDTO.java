package org.wso2.carbon.apimgt.usage.client.dto;

public class ProviderAPIServiceTimeDTO {
    String apiName;
    String serviceTime;

    public ProviderAPIServiceTimeDTO(String apiName, String serviceTime) {
        this.apiName = apiName;
        this.serviceTime = serviceTime;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(String serviceTime) {
        this.serviceTime = serviceTime;
    }
}
