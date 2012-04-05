/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.tenant.reg.agent.client.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.activation.stub.ActivationServiceStub;
import org.wso2.carbon.authenticator.proxy.AuthenticationAdminClient;
import org.wso2.carbon.tenant.reg.agent.client.internal.DataHolder;
import org.wso2.carbon.tenant.reg.agent.stub.TenantRegAgentServiceStub;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Util {
    private static final Log log = LogFactory.getLog(Util.class);

    private static String CONF_FILENAME = "tenant-reg-agent.xml";

    private static List<TenantRegListenerServer> listenerServers =
            new ArrayList<TenantRegListenerServer>();

    public static TenantRegAgentServiceStub getTenantRegAgentServiceStub(
            String serverUrl,
            String userName,
            String password) throws Exception {
        TenantRegAgentServiceStub stub =
                new TenantRegAgentServiceStub(DataHolder.getInstance().getClientConfigContext(),
                                              serverUrl + "TenantRegAgentService");
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        String cookie = login(serverUrl, userName, password);
        option.setProperty(
                org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                cookie);
        return stub;
    }

    public static ActivationServiceStub getActivationServiceStub(
            String serverUrl,
            String userName,
            String password) throws Exception {
        ActivationServiceStub stub =
                new ActivationServiceStub(DataHolder.getInstance().getClientConfigContext(),
                                          serverUrl + "ActivationService");
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        String cookie = login(serverUrl, userName, password);
        option.setProperty(
                org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                cookie);
        return stub;
    }


    public static String login(String serverUrl, String userName, String password) throws
                                                                                   UserStoreException {
        String sessionCookie = null;
        try {
            AuthenticationAdminClient client =
                    new AuthenticationAdminClient(DataHolder.getInstance().getClientConfigContext(),
                                                  serverUrl, null, null, false);
            //TODO : get the correct IP
            boolean isLogin = client.login(userName, password, "127.0.0.1");
            if (isLogin) {
                sessionCookie = client.getAdminCookie();
            }
        } catch (Exception e) {
            throw new UserStoreException("Error in login to the server server: " + serverUrl +
                                         "username: " + userName + ".", e);
        }
        return sessionCookie;
    }

    public static void loadConfig() throws Exception {
        String configFilename = CarbonUtils.getCarbonConfigDirPath() + "/" + CONF_FILENAME;
        OMElement agentConfigEle = buildOMElement(new FileInputStream(configFilename));

        Iterator configChildren = agentConfigEle.getChildElements();
        while (configChildren.hasNext()) {
            Object configObj = configChildren.next();
            if (!(configObj instanceof OMElement)) {
                continue;
            }
            OMElement serverConfigEle = (OMElement) configObj;

            Iterator serverConfigChildren = serverConfigEle.getChildElements();
            TenantRegListenerServer server = new TenantRegListenerServer();
            while (serverConfigChildren.hasNext()) {
                Object childObj = serverConfigChildren.next();
                if (!(childObj instanceof OMElement)) {
                    continue;
                }

                OMElement childEle = (OMElement) childObj;

                if (new QName(null, "serverUrl").equals(childEle.getQName())) {
                    String serverUrl = childEle.getText();
                    server.setServerUrl(serverUrl);
                } else if (new QName(null, "userName").equals(childEle.getQName())) {
                    String userName = childEle.getText();
                    server.setUserName(userName);
                } else if (new QName(null, "password").equals(childEle.getQName())) {
                    String password = childEle.getText();
                    server.setPassword(password);
                }
            }
            listenerServers.add(server);
        }
    }

    public static void triggerTenantRegistration(final int tenantId) throws Exception {
        new Thread() {
            public void run() {
                for (TenantRegListenerServer server : listenerServers) {
                    final String serverUrl = server.getServerUrl();
                    final String userName = server.getUserName();
                    final String password = server.getPassword();
                    try {
                        TenantRegAgentServiceStub stub =
                                getTenantRegAgentServiceStub(serverUrl, userName, password);

                        stub.addTenant(tenantId);
                    } catch (Exception e) {
                        String msg = "Error in triggering the tenant update, tenant id: "
                                     + tenantId + ".";
                        log.error(msg, e);
                    }
                }
            }
        }.start();
    }

    public static void triggerTenantUpdate(final int tenantId) throws Exception {
        new Thread() {
            public void run() {
                for (TenantRegListenerServer server : listenerServers) {

                    final String serverUrl = server.getServerUrl();
                    final String userName = server.getUserName();
                    final String password = server.getPassword();
                    try {
                        TenantRegAgentServiceStub stub =
                                getTenantRegAgentServiceStub(serverUrl, userName, password);

                        stub.updateTenant(tenantId);
                    } catch (Exception e) {
                        String msg = "Error in triggering the tenant update, tenant id: "
                                     + tenantId + ".";
                        log.error(msg, e);
                    }
                }
            }
        }.start();
    }

    public static void triggerTenantRename(final int tenantId, final String oldName,
                                           final String newName) throws Exception {
        new Thread() {
            public void run() {
                for (TenantRegListenerServer server : listenerServers) {

                    final String serverUrl = server.getServerUrl();
                    final String userName = server.getUserName();
                    final String password = server.getPassword();
                    try {
                        TenantRegAgentServiceStub stub =
                                getTenantRegAgentServiceStub(serverUrl, userName, password);

                        stub.renameTenant(tenantId, oldName, newName);
                    } catch (Exception e) {
                        String msg = "Error in triggering the tenant update, tenant id: "
                                     + tenantId + ".";
                        log.error(msg, e);
                    }
                }
            }
        }.start();
    }

    public static void triggerTenantActivation(final int tenantId) throws Exception {
        new Thread() {
            public void run() {
                for (TenantRegListenerServer server : listenerServers) {
                    final String serverUrl = server.getServerUrl();
                    final String userName = server.getUserName();
                    final String password = server.getPassword();
                    try {
                        ActivationServiceStub stub =
                                getActivationServiceStub(serverUrl, userName, password);

                        stub.updateActivation(tenantId);
                    } catch (Exception e) {
                        String msg = "Error in triggering the tenant activate, tenant id: "
                                     + tenantId + ".";
                        log.error(msg, e);
                    }
                }
            }
        }.start();
    }


    public static OMElement buildOMElement(InputStream inputStream) throws Exception {
        XMLStreamReader parser;
        try {
            parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        } catch (XMLStreamException e) {
            String msg = "Error in initializing the parser to build the OMElement.";
            log.error(msg, e);
            throw new Exception(msg, e);
        }

        //create the builder
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        //get the root element (in this case the envelope)

        return builder.getDocumentElement();
    }

    public static TenantRegListenerServer[] getListenerServers() {
        return listenerServers.toArray(new TenantRegListenerServer[listenerServers.size()]);
    }
}
