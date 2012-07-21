package org.wso2.carbon.cep.admin.internal;

/**
 * This class contains properties of inputs and outputs
 */
public class MapPropertyDTO {

    /**
     * Boolean to identify the property
     * if:
     * true - InputDTO MapPropertyDTO
     * false - OutputDTO MapPropertyDTO
     */
    private boolean isInputProperty;
    /**
     * Name of the property
     */
    private String name;

    /**
     * Type of the property
     */
    private String type;


    public boolean isInputProperty() {
        return isInputProperty;
    }

    public void setInputProperty(boolean inputProperty) {
        isInputProperty = inputProperty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
