/**
 *
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.agent.internal.publisher.authenticator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.wso2.carbon.agent.commons.exception.AuthenticationException;
import org.wso2.carbon.agent.conf.ReceiverConfiguration;
import org.wso2.carbon.agent.exception.AgentAuthenticatorException;
import org.wso2.carbon.agent.exception.AgentException;
import org.wso2.carbon.agent.exception.TransportException;
import org.wso2.carbon.agent.internal.pool.authenticator.AuthenticatorClientPool;
import org.wso2.carbon.agent.internal.utils.AgentConstants;

/**
 * Authenticates all data publishers
 */
public abstract class AgentAuthenticator {

    private GenericKeyedObjectPool threadPool;

    public AgentAuthenticator(KeyedPoolableObjectFactory factory,
                              int maxActive,
                              int maxIdle,
                              boolean testOnBorrow,
                              long timeBetweenEvictionRunsMillis,
                              long minEvictableIdleTimeMillis) {
        threadPool = new AuthenticatorClientPool().
                getClientPool(factory, maxActive, maxIdle, testOnBorrow,
                              timeBetweenEvictionRunsMillis, minEvictableIdleTimeMillis);
    }

    private static Log log = LogFactory.getLog(AgentAuthenticator.class);

    public String connect(ReceiverConfiguration receiverConfiguration)
            throws AuthenticationException, TransportException, AgentException {
        Object client = null;
        String key = receiverConfiguration.getAuthenticatorIp() +
                     AgentConstants.HOSTNAME_AND_PORT_SEPARATOR +
                     receiverConfiguration.getAuthenticatorPort();
        try {
            client = threadPool.borrowObject(key);
            return connect(client, receiverConfiguration.getUserName(),
                           receiverConfiguration.getPassword());
        } catch (AgentAuthenticatorException e) {
            throw new AuthenticationException("Access denied for user " +
                                              receiverConfiguration.getUserName() + " to login " +
                                              key, e);
        } catch (Exception e) {
            throw new AgentException("Cannot borrow client for " + key, e);
        } finally {
            try {
                threadPool.returnObject(key, client);
            } catch (Exception e) {
                threadPool.clear(key);
            }
        }


    }

    protected abstract String connect(Object client, String userName, String password)
            throws AuthenticationException, AgentAuthenticatorException;


    public void disconnect(String sessionId, ReceiverConfiguration receiverConfiguration) {
        Object client = null;
        String key = receiverConfiguration.getAuthenticatorIp() +
                     AgentConstants.HOSTNAME_AND_PORT_SEPARATOR +
                     receiverConfiguration.getAuthenticatorPort();
        try {
            client = threadPool.borrowObject(key);
            disconnect(client, sessionId);
        } catch (Exception e) {
            log.error("Cannot connect to the server at " + key + "Authenticator", e);
        } finally {
            try {
                threadPool.returnObject(key, client);
            } catch (Exception e) {
                threadPool.clear(key);
            }
        }

    }

    protected abstract void disconnect(Object client, String sessionId)
            throws AgentAuthenticatorException;

}
