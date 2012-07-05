package org.wso2.carbon.hadoop.hive.jdbc.storage.exception;


public class UnsupportedDatabaseException extends Exception{
    private String errorMessage;

    public UnsupportedDatabaseException() {
    }

    public UnsupportedDatabaseException(String message) {
        super(message);
        errorMessage = message;
    }

    public UnsupportedDatabaseException(String message, Throwable cause) {
        super(message, cause);
        errorMessage = message;
    }

    public UnsupportedDatabaseException(Throwable cause) {
        super(cause);
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
