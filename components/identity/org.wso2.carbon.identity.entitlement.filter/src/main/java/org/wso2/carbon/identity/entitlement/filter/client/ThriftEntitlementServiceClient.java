/*
 *  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.entitlement.filter.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.wso2.carbon.identity.entitlement.filter.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.filter.exception.EntitlementFilterException;
import org.wso2.carbon.identity.entitlement.filter.generatedcode.AuthenticationException;
import org.wso2.carbon.identity.entitlement.filter.generatedcode.AuthenticatorService;
import org.wso2.carbon.identity.entitlement.filter.generatedcode.EntitlementThriftClient;
import org.wso2.carbon.identity.entitlement.filter.util.EntitlementFilterUtils;

import java.util.Properties;

/**
 * Implementation of Entitlement Service Client that is using thrift as transport protocol
 */
public class ThriftEntitlementServiceClient extends AbstractEntitlementServiceClient {

    private String backEndServerURL;
    private String userName;
    private String password;
    private String thriftHost;
    private int thriftIntPort;
    private String sessionId;
    private String trustStore;
    private String trustStorePass;

    private static final Log log = LogFactory.getLog(ThriftEntitlementServiceClient.class);

    public void init(Properties properties) {

        password = properties.getProperty(EntitlementConstants.PASSWORD);
        userName = properties.getProperty(EntitlementConstants.USER);
        thriftHost = properties.getProperty(EntitlementConstants.THRIFT_HOST);
        String thriftPort = properties.getProperty(EntitlementConstants.THRIFT_PORT);
        if (thriftPort != null) {
            thriftIntPort = Integer.parseInt(thriftPort.trim());
        } else {
            thriftIntPort = EntitlementConstants.DEFAULT_THRIFT_PORT;
        }
        backEndServerURL = "https://" + properties.getProperty(EntitlementConstants.HOST) + ":"
                           + properties.getProperty(EntitlementConstants.HOST) + "/";

        trustStore = System.getProperty(EntitlementConstants.TRUST_STORE);
        trustStorePass = System.getProperty(EntitlementConstants.TRUST_STORE_PASSWORD);

    }

    public String getDecision(String userName, String resource, String action, String[] env)
            throws EntitlementFilterException {

        String decision = EntitlementConstants.DENY;
        try {
            if (authenticate()) {
                TSSLTransportFactory.TSSLTransportParameters param = new TSSLTransportFactory.TSSLTransportParameters();
                param.setTrustStore(trustStore, trustStorePass);
                TTransport transport;
                transport = TSSLTransportFactory.getClientSocket(thriftHost, thriftIntPort, EntitlementConstants.THRIFT_TIME_OUT, param);
                TProtocol protocol = new TBinaryProtocol(transport);
                EntitlementThriftClient.Client client = new EntitlementThriftClient.Client(protocol);
                String xacml2Request = EntitlementFilterUtils.createXACML2Request(userName, resource, action);
                decision = getStatus(client.getDecision(xacml2Request, sessionId));
                return decision;
            }
        } catch (Exception e) {
            throw new EntitlementFilterException("Error while getting decision from PDP using ThriftEntitlementServiceClient", e);
        }
        return decision;
    }

    /**
     * gets session id from thrift authentication
     *
     * @return session id
     */
    private boolean authenticate() throws EntitlementFilterException {

        boolean isAuthenticated = false;
        try {
            THttpClient client = new THttpClient(backEndServerURL + "thriftAuthenticator");
            TProtocol protocol = new TCompactProtocol(client);
            AuthenticatorService.Client authClient = new AuthenticatorService.Client(protocol);
            client.open();
            sessionId = authClient.authenticate(userName, password);
            client.close();
            isAuthenticated = true;
        } catch (AuthenticationException e) {
            log.info(userName + " not authenticated to perform entitlement query", e);
        } catch (TException e) {
            throw new EntitlementFilterException("Error while authenticating with ThriftAuthenticator", e);
        }
        return isAuthenticated;
    }
}
