package org.wso2.carbon.bam.clustermonitor.ui.data;


public class ProxyServiceData {

    private String faultCount;
    private String count;
    private String direction;
    private String responseTime;


    public String getFaultCount() {
        return faultCount;
    }

    public void setFaultCount(String faultCount) {
        this.faultCount = faultCount;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(String responseTime) {
        this.responseTime = responseTime;
    }
}
