package org.wso2.carbon.cep.admin.internal;

/**
 * used to keep the source related details
 */
public class SourceDTO {

    /**
     * type of the query source. it can be online|registryKey|fileName|inputstream
     */
    private String type;

    /**
     * source according to the above given type
     */
    private String text;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}