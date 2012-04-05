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
package org.wso2.carbon.bam.activity.mediation.data.publisher.publish;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.bam.activity.mediation.data.publisher.conf.ActivityConfigData;
import org.wso2.carbon.bam.service.AuthenticationException;
import org.wso2.carbon.bam.service.AuthenticatorService;
import org.wso2.carbon.utils.CarbonUtils;


public class ThriftUtil {

    private static volatile String sessionId = null;

    public static String getSessionId(ActivityConfigData activityConfigData) {
        THttpClient client = null;
        try {
            if (sessionId == null) {
                synchronized (ThriftUtil.class) {
                    if (sessionId == null) {
                        client = getClient(activityConfigData);
                        TProtocol protocol = new TCompactProtocol(client);
                        AuthenticatorService.Client authClient = new AuthenticatorService.Client(protocol);
                        client.open();
                        sessionId = authClient.authenticate(activityConfigData.getUserName(),
                                                            activityConfigData.getPassword());
                        client.close();
                    }
                }
            }
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        return sessionId;
    }

    public static THttpClient getClient(ActivityConfigData activityConfigData) {
        THttpClient client = null;
        try {

            String trustStore = CarbonUtils.getCarbonHome() + "/repository/resources/security";
            System.setProperty("javax.net.ssl.trustStore", trustStore + "/client-truststore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
            client = new THttpClient(activityConfigData.getUrl() + "thriftAuthenticator");
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return client;
    }


    public static void setSessionId(String sessionID) {
        sessionId = sessionID;
    }
}
