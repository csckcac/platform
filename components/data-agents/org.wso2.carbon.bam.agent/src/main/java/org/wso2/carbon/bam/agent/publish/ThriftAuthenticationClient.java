/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.bam.agent.publish;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.bam.service.AuthenticationException;
import org.wso2.carbon.bam.service.AuthenticatorService;

import java.util.Map;
import java.util.concurrent.TimeUnit;


public class ThriftAuthenticationClient {

    private static Log log = LogFactory.getLog(ThriftAuthenticationClient.class);

    //    private Map<EventReceiver, String> sessionIdCache = new ConcurrentHashMap<EventReceiver, String>();
    private Map<EventReceiver, String> sessionIdCache = new MapMaker().expiration(30, TimeUnit.MINUTES).
            makeComputingMap(new Function<EventReceiver, String>() {

                @Override
                public String apply(EventReceiver eventReceiver) {
                    THttpClient client = null;
                    String sessionId = null;
                    try {

                        client = getClient(eventReceiver);
                        TProtocol protocol = new TCompactProtocol(client);
                        AuthenticatorService.Client authClient = new AuthenticatorService.Client(protocol);
                        client.open();
                        sessionId = authClient.authenticate(eventReceiver.getUserName(),
                                eventReceiver.getPassword());
                        client.close();

                        if (log.isDebugEnabled()) {
                            log.debug("new session id retrieved for username: " + eventReceiver.getUserName() +
                                    " event url : " + eventReceiver.getUrl());
                        }

                    } catch (TTransportException e) {
                        log.error("Transport Exception for user : " + eventReceiver.getUserName() + " for url : " + eventReceiver.getUrl(), e);
                    } catch (TException e) {
                        log.error("Thrift Exception for user : " + eventReceiver.getUserName() + " for url : " + eventReceiver.getUrl(), e);
                    } catch (AuthenticationException e) {
                        log.error("Authentication failed for user : " + eventReceiver.getUserName() + " for url : " + eventReceiver.getUrl(), e);
                    }
                    return sessionId;
                }
            });

    public String getSessionId(EventReceiver eventReceiver) {
        return sessionIdCache.get(eventReceiver);
    }

    public THttpClient getClient(EventReceiver eventReceiver) {
        THttpClient client = null;
        try {

//            String trustStore = CarbonUtils.getCarbonHome() + "/repository/resources/security";
//            System.setProperty("javax.net.ssl.trustStore", trustStore + "/client-truststore.jks");
//            System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
            client = new THttpClient(eventReceiver.getUrl() + "thriftAuthenticator");
        } catch (TTransportException e) {
            log.error("Transport Exception for user : " + eventReceiver.getUserName() + " for url : " + eventReceiver.getUrl(), e);
        }
//        } catch (Exception e) {
//            log.error("Authentication failed for user : " + eventReceiver.getUserName(), e);
//        }
        return client;
    }

    public void removeSessionId(EventReceiver eventReceiver) {
        sessionIdCache.remove(eventReceiver);
    }

}
