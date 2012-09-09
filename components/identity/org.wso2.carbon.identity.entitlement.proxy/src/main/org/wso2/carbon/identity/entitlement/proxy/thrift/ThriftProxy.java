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
package org.wso2.carbon.identity.entitlement.proxy.thrift;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.wso2.carbon.identity.entitlement.proxy.*;
import org.wso2.carbon.identity.entitlement.proxy.exception.EntitlementProxyException;
import org.wso2.carbon.identity.entitlement.proxy.generatedCode.EntitlementThriftClient;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ThriftProxy extends AbstractPDPProxy {

    private PDPConfig config;
    private String backEndServerURL;
    private String trustStore;
    private String trustStorePass;

    private Map<String, Authenticator> authenticators = new ConcurrentHashMap<String, Authenticator>();

    @Override
    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
                                           String resourceId, String domainId, String appId) throws Exception {
        Attribute subjectAttribute = new Attribute("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject", subjectType, ProxyConstants.DEFAULT_DATA_TYPE, alias);
        Attribute actionAttribute = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:action", "urn:oasis:names:tc:xacml:1.0:action:action-id", ProxyConstants.DEFAULT_DATA_TYPE, actionId);
        Attribute resourceAttribute = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:resource", "urn:oasis:names:tc:xacml:1.0:resource:resource-id", ProxyConstants.DEFAULT_DATA_TYPE, resourceId);
        Attribute environmentAttribute = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:environment", "urn:oasis:names:tc:xacml:1.0:environment:environment-id", ProxyConstants.DEFAULT_DATA_TYPE, domainId);

        Attribute[] tempArr = {subjectAttribute, actionAttribute, resourceAttribute, environmentAttribute};

        String xacmlRequest = XACMLRequetBuilder.buildXACML3Request(tempArr);
        backEndServerURL = getServerUrl(appId);
        Credentials credentials = getCrdentials(appId);
        EntitlementThriftClient.Client client = getThriftClient(appId);
        Authenticator autheticator = getAuthenticator(backEndServerURL, credentials.userName,
                credentials.password);
        String result = getDecision(xacmlRequest, client, autheticator);

        return "permit".equalsIgnoreCase(result);
    }

    @Override
    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
                                           String resourceId, Attribute[] attributes, String domainId, String appId) throws Exception {
        Attribute[] attrs = new Attribute[attributes.length + 4];
        attrs[0] = new Attribute("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject", subjectType, ProxyConstants.DEFAULT_DATA_TYPE, alias);

        for (int i = 0; i < attributes.length; i++) {
            attrs[i + 1] = new Attribute("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject", attributes[i].getType(),
                    attributes[i].getId(), attributes[i].getValue());
        }

        attrs[attrs.length - 3] = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:action", "urn:oasis:names:tc:xacml:1.0:action:action-id", ProxyConstants.DEFAULT_DATA_TYPE, actionId);
        attrs[attrs.length - 2] = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:resource", "urn:oasis:names:tc:xacml:1.0:resource:resource-id", ProxyConstants.DEFAULT_DATA_TYPE, resourceId);
        attrs[attrs.length - 1] = new Attribute("urn:oasis:names:tc:xacml:3.0:attribute-category:environment", "urn:oasis:names:tc:xacml:1.0:environment:environment-id", ProxyConstants.DEFAULT_DATA_TYPE, domainId);

        String xacmlRequest = XACMLRequetBuilder.buildXACML3Request(attrs);

        backEndServerURL = getServerUrl(appId);
        Credentials credentials = getCrdentials(appId);
        EntitlementThriftClient.Client client = getThriftClient(appId);
        Authenticator autheticator = getAuthenticator(backEndServerURL, credentials.userName,
                credentials.password);
        String result = getDecision(xacmlRequest, client, autheticator);

        return "permit".equalsIgnoreCase(result);
    }

    @Override
    public List<String> getResourcesForAlias(String alias, String appId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getActionableResourcesForAlias(String alias, String appId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getActionsForResource(String alias, String resources, String appId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPDPConfig(PDPConfig config) {
        this.config = config;
        trustStore = System.getProperty(ProxyConstants.TRUST_STORE);
        trustStorePass = System.getProperty(ProxyConstants.TRUST_STORE_PASSWORD);
    }

    @Override
    public boolean getDecision(Attribute[] attributes, String appId) throws Exception {
        String xacmlRequest = XACMLRequetBuilder.buildXACML3Request(attributes);
        backEndServerURL = getServerUrl(appId);
        Credentials credentials = getCrdentials(appId);
        EntitlementThriftClient.Client client = getThriftClient(appId);
        Authenticator autheticator = getAuthenticator(backEndServerURL, credentials.userName,
                credentials.password);
        String result = getDecision(xacmlRequest, client, autheticator);

        return "permit".equalsIgnoreCase(result);
    }

    @Override
    public String getActualDecision(Attribute[] attributes, String appId) throws Exception {
        String xacmlRequest = XACMLRequetBuilder.buildXACML3Request(attributes);
        backEndServerURL = getServerUrl(appId);
        Credentials credentials = getCrdentials(appId);
        EntitlementThriftClient.Client client = getThriftClient(appId);
        Authenticator autheticator = getAuthenticator(backEndServerURL, credentials.userName,
                credentials.password);
        return getDecision(xacmlRequest, client, autheticator);
    }

    @Override
    public List<String> getActionableChidResourcesForAlias(String alias, String parentResource,
                                                           String action, String appId) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    private String getDecision(String request, EntitlementThriftClient.Client client,
                               Authenticator autheticator) throws Exception {
        try {
            return getStatus(client.getDecision(request, autheticator.getSessionId(false)));
        } catch (AxisFault e) {
            throw new EntitlementProxyException("Error while getting decision from PDP using ThriftEntitlementServiceClient", e);
        }
    }


    public String getStatus(String xmlString) throws EntitlementProxyException {
        OMElement decision;
        OMElement result;
        try {
            result = (AXIOMUtil.stringToOM(xmlString)).getFirstChildWithName(new QName("Result"));
            decision = result.getFirstChildWithName(new QName("Decision"));
            return decision.getText();
        } catch (Exception e) {
            throw new EntitlementProxyException("Unable to parse response string " + xmlString, e);
        }
    }

    private String getServerUrl(String appId) {
        if (config.getAppToPDPMap().containsKey(appId)) {
            String[] attributes = config.getAppToPDPMap().get(appId);
            if (attributes != null && attributes.length > 0) {
                return attributes[0];
            }
        }
        return null;
    }

    private int getThriftPort(String appId) {
        if (config.getAppToPDPMap().containsKey(appId)) {
            String[] attributes = config.getAppToPDPMap().get(appId);
            if (attributes != null && attributes.length > 4) {
                return Integer.parseInt(attributes[5]);
            }
        }
        return ProxyConstants.DEFAULT_THRIFT_PORT;
    }

    private String getThriftHost(String appId) {
        if (config.getAppToPDPMap().containsKey(appId)) {
            String[] attributes = config.getAppToPDPMap().get(appId);
            if (attributes != null && attributes.length > 3) {
                return attributes[4];
            }
        }
        return null;
    }

    private Credentials getCrdentials(String appId) throws EntitlementProxyException {
        if (config.getAppToPDPMap().containsKey(appId)) {
            Credentials credentials = new Credentials();
            String[] attributes = config.getAppToPDPMap().get(appId);
            if (attributes != null && attributes.length > 2) {
                credentials.userName = attributes[1];
                credentials.password = attributes[2];
            } else {
                throw new EntitlementProxyException("User Name and Password Arguments are not provided correctley in the appConfig Array per the appID :" + appId);
            }
            return credentials;
        }
        return null;
    }

    protected Authenticator getAuthenticator(String serverUrl, String userName, String password)
            throws Exception {

        if (authenticators.containsKey(serverUrl)) {
            return authenticators.get(serverUrl);
        }

        Authenticator authenticator;
        authenticator = new Authenticator(userName, password, serverUrl + "thriftAuthenticator");
        authenticators.put(serverUrl, authenticator);
        return authenticator;
    }

    protected EntitlementThriftClient.Client getThriftClient(String appId) throws Exception {

        TSSLTransportFactory.TSSLTransportParameters param = new TSSLTransportFactory.TSSLTransportParameters();
        param.setTrustStore(trustStore, trustStorePass);
        TTransport transport;
        transport = TSSLTransportFactory.getClientSocket(getThriftHost(appId), getThriftPort(appId), ProxyConstants.THRIFT_TIME_OUT, param);
        TProtocol protocol = new TBinaryProtocol(transport);
        EntitlementThriftClient.Client client = new EntitlementThriftClient.Client(protocol);

        return client;
    }

    static class Credentials {
        String userName;
        String password;
    }
}
