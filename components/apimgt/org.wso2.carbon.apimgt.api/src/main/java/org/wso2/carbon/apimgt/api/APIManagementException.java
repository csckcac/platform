package org.wso2.carbon.apimgt.api;

/**
 * This is the custom exception class for API management.
 */
public class APIManagementException extends Exception {

    public APIManagementException(String msg) {
        super(msg);
    }

    public APIManagementException(String msg, Throwable e) {
        super(msg, e);
    }
    public APIManagementException(Throwable throwable){
        super(throwable);
    }
}
