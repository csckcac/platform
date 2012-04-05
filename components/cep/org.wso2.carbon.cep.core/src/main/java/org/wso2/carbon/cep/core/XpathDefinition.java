package org.wso2.carbon.cep.core;

/**
 * this class contains the xpath namespace mapping of the input XML
 * */
public class XpathDefinition {
    /**
     * Prefix to be mapped
     * */
    private String prefix;

    /**
     * Namespace to be mapped
     * */
    private String namespace;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
