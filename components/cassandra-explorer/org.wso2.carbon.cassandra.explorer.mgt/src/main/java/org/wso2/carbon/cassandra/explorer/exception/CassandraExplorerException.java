package org.wso2.carbon.cassandra.explorer.exception;

/**
 * Created with IntelliJ IDEA.
 * User: shelan
 * Date: 5/28/12
 * Time: 5:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class CassandraExplorerException extends Exception {
    public CassandraExplorerException(String message) {
        super(message);
    }

    public CassandraExplorerException(String message, Throwable t) {
        super(message, t);
    }
}
