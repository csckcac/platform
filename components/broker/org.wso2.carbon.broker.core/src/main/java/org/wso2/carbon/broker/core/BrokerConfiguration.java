package org.wso2.carbon.broker.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class contain the configuration details of the broker
 */

public class BrokerConfiguration {
    /**
     * logical name use to identify this configuration
     */
    private String name;

    /**
     * broker  type for this configuration
     */
    private String type;

    /**
     * property values - these properties are depends on the properties defined in the
     * broker type. there must be a property here for each property defined in broker type
     */
    private Map<String, String> properties;

    public BrokerConfiguration() {
        this.properties = new ConcurrentHashMap<String, String>();
    }

    public void addProperty(String name, String value) {
        this.properties.put(name, value);
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

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
