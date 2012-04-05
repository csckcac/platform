package org.wso2.carbon.bam.clustermonitor.ui.data;


public class OperationData {

    private String operationName;
    private String faultCount;
    private String responseTime;
    private String responseCount;
    private String requestCount;

    public String getFaultCount() {
        return faultCount;
    }

    public void setFaultCount(String faultCount) {
        this.faultCount = faultCount;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(String responseTime) {
        this.responseTime = responseTime;
    }

    public String getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(String responseCount) {
        this.responseCount = responseCount;
    }

    public String getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(String requestCount) {
        this.requestCount = requestCount;
    }
}
