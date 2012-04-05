/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.mediationstats.data.publisher.publish;


import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.bam.mediationstats.data.publisher.conf.MediationStatConfig;
import org.wso2.carbon.bam.service.AuthenticationException;
import org.wso2.carbon.bam.service.AuthenticatorService;
import org.wso2.carbon.utils.CarbonUtils;

public class ThriftUtil {
    private static volatile TTransport client = null;
    private static volatile String sessionId = null;

    public static String getSessionId(MediationStatConfig mediationStatConfig) {

        try {
            if (sessionId == null) {
                synchronized (ThriftUtil.class) {
                    if (sessionId == null) {
                        TTransport client = getClient(mediationStatConfig);
                        TProtocol protocol = new TCompactProtocol(client);
                        AuthenticatorService.Client authClient = new AuthenticatorService.Client(protocol);
                        client.open();
                        sessionId = authClient.authenticate(mediationStatConfig.getUserName(),
                                                            mediationStatConfig.getPassword());
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

    public static TTransport getClient(MediationStatConfig mediationStatConfig) {
        try {
            if (client == null) {
                synchronized (ThriftUtil.class) {
                    if (client == null) {
                        String trustStore = CarbonUtils.getCarbonHome() + "/repository/resources/security";
                        System.setProperty("javax.net.ssl.trustStore", trustStore + "/client-truststore.jks");
                        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
                        client = new THttpClient(mediationStatConfig.getUrl() + "/thriftAuthenticator");
                    }
                }
            }
        } catch (TTransportException e) {
            e.printStackTrace();
        }
        return client;
    }

    public static void setSessionId(String sessionID) {
        sessionId = sessionID;
    }

}
