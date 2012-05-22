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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Input input = (Input) o;

        if (brokerName != null ? !brokerName.equals(input.brokerName) : input.brokerName != null) return false;
        if (topic != null ? !topic.equals(input.topic) : input.topic != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = topic != null ? topic.hashCode() : 0;
        result = 31 * result + (brokerName != null ? brokerName.hashCode() : 0);
        result = 31 * result + (inputMapping != null ? inputMapping.hashCode() : 0);
        return result;
    }

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
