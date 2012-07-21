package org.wso2.carbon.cep.admin.internal;

/**
 * This class used to configure the out put from the CEP Engine
 * */
public class OutputDTO {
    /**
     * OutputDTO Topic which is used to publish processed data from CEP
     * */
    private String topic;

    /**
     * OutputXMLMappingDTO output from CEP Engine to the output XML
     **/
    private OutputElementMappingDTO outputElementMappingDTO;

    /**
     * XML OutputXMLMappingDTO document
     * */
    private OutputXMLMappingDTO outputXmlMappingDTO;

    /**
     * Tuple OutputTupleMappingDTO document
     * */
    private OutputTupleMappingDTO outputTupleMappingDTO;
 /**
     * Map OutputMapMappingDTO document
     * */
    private OutputMapMappingDTO outputMapMappingDTO;

    /**
     * Name of the broker to be used
     * */
    private String brokerName;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public OutputElementMappingDTO getOutputElementMapping() {
        return outputElementMappingDTO;
    }

    public void setOutputElementMapping(OutputElementMappingDTO outputElementMappingDTO) {
        this.outputElementMappingDTO = outputElementMappingDTO;
    }

    public OutputXMLMappingDTO getOutputXmlMapping() {
        return outputXmlMappingDTO;
    }

    public void setOutputXmlMapping(OutputXMLMappingDTO outputXmlMappingDTO) {
        this.outputXmlMappingDTO = outputXmlMappingDTO;
    }

    public OutputTupleMappingDTO getOutputTupleMapping() {
        return outputTupleMappingDTO;
    }

    public void setOutputTupleMapping(OutputTupleMappingDTO outputTupleMappingDTO) {
        this.outputTupleMappingDTO = outputTupleMappingDTO;
    }

    public OutputMapMappingDTO getOutputMapMapping() {
        return outputMapMappingDTO;
    }

    public void setOutputMapMapping(OutputMapMappingDTO outputMapMappingDTO) {
        this.outputMapMappingDTO = outputMapMappingDTO;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

}
