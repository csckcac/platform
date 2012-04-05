package org.wso2.carbon.cep.core.mapping.input;

import org.wso2.carbon.cep.core.mapping.input.mapping.InputMapping;

/**
 * this class is used to define input to CEP Engine
 */
public class Input {

    /**
     * Topic which we need to subscribe
     */
    private String topic;

    /**
     * Name of the broker used for Input
     */
    private String brokerName;

    /**
     * CEP input mapping for incoming XML
     */
    private InputMapping inputMapping;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public InputMapping getInputMapping() {
        return inputMapping;
    }

    public void setInputMapping(InputMapping inputMapping) {
        this.inputMapping = inputMapping;
    }
}
