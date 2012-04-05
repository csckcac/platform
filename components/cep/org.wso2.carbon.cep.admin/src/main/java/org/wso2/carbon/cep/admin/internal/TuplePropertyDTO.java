package org.wso2.carbon.cep.admin.internal;

/**
 * This class contains properties of inputs and outputs
 */
public class TuplePropertyDTO {

    /**
     * Boolean to identify the property
     * if:
     * true - InputDTO TuplePropertyDTO
     * false - OutputDTO TuplePropertyDTO
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

    /**
     * Type of the type of the data mataData, correlationData, payloadData field
     */
    private String dataType;

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

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
