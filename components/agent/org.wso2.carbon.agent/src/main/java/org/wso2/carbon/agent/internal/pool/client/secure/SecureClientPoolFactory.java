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


package org.wso2.carbon.agent.internal.pool.client.secure;


import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.agent.commons.thrift.service.secure.ThriftSecureEventTransmissionService;
import org.wso2.carbon.agent.exception.AgentSecurityException;
import org.wso2.carbon.agent.internal.utils.AgentConstants;

public class SecureClientPoolFactory extends BaseKeyedPoolableObjectFactory {

    private TSSLTransportFactory.TSSLTransportParameters params;
    private String trustStorePassword;
    private String trustStore;

    public SecureClientPoolFactory(String trustStorePassword, String trustStore) {
        this.trustStorePassword = trustStorePassword;
        this.trustStore = trustStore;
    }

    @Override
    public ThriftSecureEventTransmissionService.Client makeObject(Object key)
            throws AgentSecurityException, TTransportException {

        if (params == null) {
            if (trustStore == null) {
                trustStore = System.getProperty("javax.net.ssl.trustStore");
                if (trustStore == null) {
                    throw new AgentSecurityException("No trustStore found");
                }
                // trustStore = "/home/suho/projects/wso2/trunk/carbon/distribution/product/modules/distribution/target/wso2carbon-4.0.0-SNAPSHOT/repository/resources/security/client-truststore.jks";
            }

            if (trustStorePassword == null) {
                trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
                if (trustStorePassword == null) {
                    throw new AgentSecurityException("No trustStore password found");
                }
                //trustStorePassword = "wso2carbon";
            }

            params = new TSSLTransportFactory.TSSLTransportParameters();
            params.setTrustStore(trustStore, trustStorePassword);
        }

        String[] hostNameAndPort = key.toString().split(AgentConstants.ENDPOINT_SEPARATOR)[1].split(AgentConstants.HOSTNAME_AND_PORT_SEPARATOR);

        TTransport receiverTransport = TSSLTransportFactory.
                getClientSocket(hostNameAndPort[0], Integer.parseInt(hostNameAndPort[1]), 0, params);

        TProtocol protocol = new TBinaryProtocol(receiverTransport);
        return new ThriftSecureEventTransmissionService.Client(protocol);
    }

    @Override
    public boolean validateObject(Object key, Object obj) {
        ThriftSecureEventTransmissionService.Client client = (ThriftSecureEventTransmissionService.Client) obj;
        return client.getOutputProtocol().getTransport().isOpen();
    }


}
