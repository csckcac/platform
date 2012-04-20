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


package org.wso2.carbon.agent.internal.pool.authenticator;


import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.agent.commons.exception.AuthenticationException;
import org.wso2.carbon.agent.commons.thrift.authentication.service.ThriftAuthenticatorService;
import org.wso2.carbon.agent.internal.utils.AgentConstants;

public class AuthenticatorClientPoolFactory extends BaseKeyedPoolableObjectFactory {

    private String trustStore;
    private String trustStorePassword;

    public AuthenticatorClientPoolFactory(String trustStore, String trustStorePassword) {
        this.trustStore = trustStore;
        this.trustStorePassword = trustStorePassword;
    }

    @Override
    public ThriftAuthenticatorService.Client makeObject(Object key)
            throws AuthenticationException, TTransportException {

        String[] hostNameAndPort = key.toString().split(AgentConstants.HOSTNAME_AND_PORT_SEPARATOR);

        if (trustStore == null) {
            trustStore = System.getProperty("javax.net.ssl.trustStore");
            if (trustStore == null) {
                throw new AuthenticationException("No trustStore found");
            }
            // trustStore = "/home/suho/projects/wso2/trunk/carbon/distribution/product/modules/distribution/target/wso2carbon-4.0.0-SNAPSHOT/repository/resources/security/client-truststore.jks";
        }

        if (trustStorePassword == null) {
            trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
            if (trustStorePassword == null) {
                throw new AuthenticationException("No trustStore password found");
            }
            //trustStorePassword = "wso2carbon";
        }

        TSSLTransportFactory.TSSLTransportParameters params = new TSSLTransportFactory.TSSLTransportParameters();
        params.setTrustStore(trustStore, trustStorePassword);

        TTransport receiverTransport = TSSLTransportFactory.
                getClientSocket(hostNameAndPort[0], Integer.parseInt(hostNameAndPort[1]), 0, params);

        TProtocol protocol = new TBinaryProtocol(receiverTransport);
        return new ThriftAuthenticatorService.Client(protocol);
    }

    @Override
    public boolean validateObject(Object key, Object obj) {
        ThriftAuthenticatorService.Client client = (ThriftAuthenticatorService.Client) obj;
        return client.getOutputProtocol().getTransport().isOpen();
    }


}
