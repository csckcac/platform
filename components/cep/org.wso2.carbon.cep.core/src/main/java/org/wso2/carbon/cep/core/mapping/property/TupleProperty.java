package org.wso2.carbon.cep.core.mapping.property;

/**
 * This class contains properties of inputs and outputs
 */
public class TupleProperty extends Property {

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

}
