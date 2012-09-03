package org.wso2.carbon.hosting.mgt.cli;


public class CliToolException extends Exception {

    public CliToolException(String message) {
        super(message);
    }

    public CliToolException(String message, Throwable t) {
        super(message, t);
    }

    public CliToolException(Throwable t) {
        super(t);
    }
}
