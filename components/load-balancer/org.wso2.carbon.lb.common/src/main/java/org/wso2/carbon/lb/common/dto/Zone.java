package org.wso2.carbon.lb.common.dto;

public class Zone {

    private boolean available;

    private String name;

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
