package org.wso2.carbon.cep.admin.internal;

/**
 * this class is used to define input to CEP Engine
 */
public class InputDTO {
    /**
     * Topic which we need to subscribe
     */
    private String topic;

    /**
     * CEP input XML Mapping DTO for incoming XML
     */
    private InputXMLMappingDTO inputXMLMappingDTO;

    /**
     * CEP input Tuple Mapping DTO for incoming XML
     */
    private InputTupleMappingDTO inputTupleMappingDTO;

    /**
     * CEP input Map Mapping DTO for incoming XML
     */
    private InputMapMappingDTO inputMapMappingDTO;

    /**
     * Name of the broker to be used
     */
    private String brokerName;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public InputXMLMappingDTO getInputXMLMappingDTO() {
        return inputXMLMappingDTO;
    }

    public void setInputXMLMappingDTO(InputXMLMappingDTO inputXMLMappingDTO) {
        this.inputXMLMappingDTO = inputXMLMappingDTO;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public InputTupleMappingDTO getInputTupleMappingDTO() {
        return inputTupleMappingDTO;
    }

    public void setInputTupleMappingDTO(InputTupleMappingDTO inputTupleMappingDTO) {
        this.inputTupleMappingDTO = inputTupleMappingDTO;
    }

    public InputMapMappingDTO getInputMapMappingDTO() {
        return inputMapMappingDTO;
    }

    public void setInputMapMappingDTO(InputMapMappingDTO inputMapMappingDTO) {
        this.inputMapMappingDTO = inputMapMappingDTO;
    }
}

