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

package org.wso2.carbon.identity.entitlement.mediator.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.wso2.carbon.identity.entitlement.mediator.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.mediator.EntitlementMediatorUtils;
import org.wso2.carbon.identity.entitlement.mediator.generatedCode.AuthenticatorService;
import org.wso2.carbon.identity.entitlement.mediator.generatedCode.EntitlementThriftClient;

import java.util.Properties;

/**
 *  Implementation of Entitlement Service Client that is using thrift as transport protocol
 *
 */
public class ThriftServiceClient extends EntitlementServiceClient {

    private String sessionId;

    private String userName;

    private String password;

    private String backEndServerURL;

    private String thriftHost;

    private int thriftPort;

    private String trustStore;

    private String trustStorePass;

    private static final Log log = LogFactory.getLog(ThriftServiceClient.class);

    @Override
    public void init(Properties properties) {

        password = properties.getProperty(EntitlementConstants.PASSWORD);
        userName = properties.getProperty(EntitlementConstants.USER);
        thriftHost = properties.getProperty(EntitlementConstants.THRIFT_HOST);
        String port = properties.getProperty(EntitlementConstants.THRIFT_PORT);
        if(port != null){
            thriftPort = Integer.parseInt(port.trim());    
        } else {
            thriftPort = 10500;
        }
        //configCtx = (ConfigurationContext)properties.get(EntitlementConstants.CONTEXT);
        backEndServerURL = properties.getProperty(EntitlementConstants.SERVICE_EPR);

        if(backEndServerURL != null){
            backEndServerURL = backEndServerURL.trim();
            if (!backEndServerURL.endsWith("/")) {
                backEndServerURL += "/";
            }
        }
        
        trustStore = System.getProperty("javax.net.ssl.trustStore");
        trustStorePass = System.getProperty("javax.net.ssl.trustStorePassword");

    }
    

    /**
     * gets session id from thrift authentication
     * @return session id
     * @throws Exception throws, if fails
     */
    public boolean authenticate() throws Exception {
        THttpClient client;
        try {
            client = new THttpClient(backEndServerURL + "thriftAuthenticator");
            TProtocol protocol = new TCompactProtocol(client);
            AuthenticatorService.Client authClient = new AuthenticatorService.Client(protocol);
            client.open();
            sessionId = authClient.authenticate(userName,password);
            client.close();
        } catch (Exception e) {
            log.error("Error while authenticating with Identity Server using thrift authenticator", e);
            return false;
        }
        return true;
    }

    @Override
    public String getDecision(String userName, String resource, String action, String[] env)
            throws Exception {

        String decision;

        if(authenticate()){
            TSSLTransportFactory.TSSLTransportParameters param =
                    new TSSLTransportFactory.TSSLTransportParameters();

            try{
                param.setTrustStore(trustStore, trustStorePass);
                TTransport transport = TSSLTransportFactory.getClientSocket(thriftHost, thriftPort,
                        EntitlementConstants.THRIFT_TIME_OUT, param);
                TProtocol protocol = new TBinaryProtocol(transport);

                EntitlementThriftClient.Client client = new EntitlementThriftClient.Client(protocol);
                String xacml2Request = EntitlementMediatorUtils.createXACML2Request(userName, resource,
                                                                                    action);
                decision = getStatus(client.getDecision(xacml2Request, sessionId));
            } catch (Exception e) {
                log.error("Error occurred while policy evaluation", e);
                throw e;
            }
        } else {
            log.error("User can not be authenticated to evaluate the entitlement query" );
            throw new Exception("User can not be authenticated to evaluate the entitlement query");
        }
        return decision;
    }
}
