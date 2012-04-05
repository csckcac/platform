package org.wso2.carbon.cep.core.mapping.output;

import org.wso2.carbon.cep.core.mapping.output.mapping.OutputMapping;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * This class used to configure the out put from the CEP Engine
 * */
public class Output {
    /**
     * Output Topic which is used to publish processed data from CEP
     * */
    private String topic;

    /**
     * Name of the broker used
     * */
    private String brokerName;

    private OutputMapping outputMapping;

    /**
     * Method mapping cache of the output events
     */
    private Map<Class,Map<String,Method>> methodCache;

    public Output() {
        this.methodCache=new HashMap<Class, Map<String, Method>>();
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public OutputMapping getOutputMapping() {
        return outputMapping;
    }

    public void setOutputMapping(OutputMapping outputMapping) {
        this.outputMapping = outputMapping;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public Map<Class, Map<String, Method>> getMethodCache() {
        return methodCache;
    }

    public void setMethodCache(Map<Class, Map<String, Method>> methodCache) {
        this.methodCache = methodCache;
    }
}
