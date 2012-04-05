package org.wso2.carbon.cep.core.mapping.property;

/**
 * This class contains properties of inputs and outputs
 */
public abstract class Property {

    /**
     * Boolean to identify the property
     * if:
     * true - Input Property
     * false - Output Property
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
