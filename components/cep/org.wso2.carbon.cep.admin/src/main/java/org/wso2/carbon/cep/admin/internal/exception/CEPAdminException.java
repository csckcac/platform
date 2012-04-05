package org.wso2.carbon.cep.admin.internal.exception;

public class CEPAdminException extends Exception {
    public String errorMessage;

    public CEPAdminException() {
    }

    public CEPAdminException(String message) {
        super(message);
        errorMessage = message;
    }

    public CEPAdminException(String message, Throwable cause) {
        super(message, cause);
        errorMessage = message;
    }

    public CEPAdminException(Throwable cause) {
        super(cause);
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
