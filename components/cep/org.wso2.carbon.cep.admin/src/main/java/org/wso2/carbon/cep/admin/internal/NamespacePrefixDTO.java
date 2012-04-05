package org.wso2.carbon.cep.admin.internal;

/**
     *  This class used to keep the namespace prefix to namespace mappings in xpath expessions.
     */
public class NamespacePrefixDTO {
   /**
    * Prefix for the Namespace
    * */
    String prefix;

    /**
     * Namespace
     * */
    String namespace;

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
