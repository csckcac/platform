package org.wso2.carbon.messagebox;

public class MessageBoxException extends Exception {
    private String faultCode;
    private String faultString;
    public MessageBoxException() {
        super();
    }

    public MessageBoxException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getFaultCode() {
        return faultCode;
    }

    public String getFaultString() {
        return faultString;
    }

    public MessageBoxException(String message) {
        super(message);
    }

    public MessageBoxException(String faultString, String faultCode) {
        this.faultCode = faultCode;
        this.faultString = faultString;
    }

    public MessageBoxException(Throwable cause) {
        super(cause);
    }
}