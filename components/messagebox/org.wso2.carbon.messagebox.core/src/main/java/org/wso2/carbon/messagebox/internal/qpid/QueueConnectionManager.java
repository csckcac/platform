/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.messagebox.internal.qpid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.messagebox.internal.ds.MessageBoxServiceValueHolder;
import org.wso2.carbon.qpid.service.QpidService;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a singleton JMS connection manager that takes care of creating and caching connections.
 * Connections are cached against username. A connection is kept open until it's reference count is non-zero.
 */
public class QueueConnectionManager {
    private static final Log log = LogFactory.getLog(QueueConnectionManager.class);

    private static final String QPID_ICF = "org.apache.qpid.jndi.PropertiesFileInitialContextFactory";
    private static final String CF_NAME_PREFIX = "connectionfactory.";
    private static final String CF_NAME = "qpidConnectionfactory";


    public static final String MB_TYPE_LOCAL = "local";
    public static final String MB_TYPE_REMOTE = "remote";

    private String type;
    private String hostName;
    private String qpidPort;
    private String clientID;
    private String virtualHostName;
    private String accessKey;

    private ConcurrentHashMap<String, SharedConnection> connectionCache =
            new ConcurrentHashMap<String, SharedConnection>();

    private static final QueueConnectionManager instance = new QueueConnectionManager();

    private QueueConnectionManager() {
    }

    public static QueueConnectionManager getInstance() {
        return instance;
    }

    /**
     * Get a queue connection.
     * <p/>
     * It first looks at the cache for an existing connections and creates a new connection if not found in the cache.
     *
     * @param username Name of the user whom the connection is created for
     * @return New or cached connection
     * @throws QueueConnectionManagerException
     *
     */
    public QueueConnection getConnection(String username) throws QueueConnectionManagerException {
        try {
            return createNewConnection(username).getConnection();
        } catch (NamingException e) {
            throw new QueueConnectionManagerException(e);
        } catch (JMSException e) {
            throw new QueueConnectionManagerException(e);
        }
    }

    /**
     * Release a used connection.
     * .
     * The connection will be retained in the cache if the ref count is non-zero or closed otherwise.
     *
     * @param connection JMS queue connection that needs to be released
     * @throws QueueConnectionManagerException
     *
     */
    public void releaseConnection(QueueConnection connection)
            throws QueueConnectionManagerException {

        try {
            connection.stop();
            connection.close();
        } catch (JMSException e) {
            throw new QueueConnectionManagerException(e);
        }
    }

    /**
     * Create a new JMS connection
     *
     * @param userName Name of the user whom the connection is created for
     * @return Newly created queue connection
     * @throws NamingException
     * @throws JMSException
     */
    private SharedConnection createNewConnection(String userName)
            throws NamingException, JMSException {
        // Create IC
        QpidService qpidService = MessageBoxServiceValueHolder.getInstance().getQpidService();

        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, QPID_ICF);
        properties.put(CarbonConstants.REQUEST_BASE_CONTEXT, "true");
        String connectionURL = null;

        if (MB_TYPE_LOCAL.equals(this.type)) {
            connectionURL = qpidService.getInternalTCPConnectionURL(userName, qpidService.getAccessKey());
        } else {
            connectionURL = "amqp://" + userName + ":" + this.accessKey
                            + "@" + clientID + "/" + this.virtualHostName
                            + "?brokerlist='tcp://" + this.hostName + ":" + this.qpidPort + "'";
        }
        properties.put(CF_NAME_PREFIX + CF_NAME, connectionURL);

        InitialContext ctx = new InitialContext(properties);
        // Lookup connection factory
        QueueConnectionFactory connFactory = (QueueConnectionFactory) ctx.lookup(CF_NAME);

        // Create new connection
        return new SharedConnection(connFactory.createQueueConnection());
    }

    /**
     * Close all open connections
     *
     * @throws QueueConnectionManagerException
     *
     */
    public void closeAllConnections() throws QueueConnectionManagerException {
        try {
            for (SharedConnection sharedConnection : connectionCache.values()) {
                sharedConnection.getConnection().close();
            }
        } catch (JMSException e) {
            throw new QueueConnectionManagerException(e);
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setQpidPort(String qpidPort) {
        this.qpidPort = qpidPort;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public void setVirtualHostName(String virtualHostName) {
        this.virtualHostName = virtualHostName;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }
}
