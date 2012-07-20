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

package org.wso2.carbon.identity.entitlement.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.entitlement.thrift.EntitlementService;
import org.wso2.carbon.identity.entitlement.thrift.ThriftConfigConstants;
import org.wso2.carbon.identity.entitlement.thrift.ThriftEntitlementServiceImpl;
import org.wso2.carbon.identity.thrift.authentication.ThriftAuthenticatorService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @scr.component name="identity.entitlement.component" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="org.wso2.carbon.identity.thrift.authentication.internal.ThriftAuthenticationServiceComponent"
 * interface="org.wso2.carbon.identity.thrift.authentication.ThriftAuthenticatorService"
 * cardinality="1..1" policy="dynamic" bind="setThriftAuthenticationService"  unbind="unsetThriftAuthenticationService"
 */
public class EntitlementServiceComponent {

    private static Log log = LogFactory.getLog(EntitlementServiceComponent.class);
    private static RegistryService registryService = null;
    private static EntitlementConfigHolder entitlementConfig = null;
    private static RealmService realmservice;

    private ThriftAuthenticatorService thriftAuthenticationService;

    private ExecutorService executor = Executors.newFixedThreadPool(2);

    /**
     *
     */
    public EntitlementServiceComponent() {
    }

    /**
     * @param ctxt
     */
    protected void activate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.info("Identity Entitlement bundle is activated");
        }

        try {
            EntitlementServiceInitializer entitlementServiceInitializer = new EntitlementServiceInitializer(
                    registryService);
            entitlementServiceInitializer.putEntitlementPolicyResourcesToRegistry();
            entitlementConfig = new EntitlementConfigHolder();
            EntitlementExtensionBuilder builder = new EntitlementExtensionBuilder();
            builder.setBundleContext(ctxt.getBundleContext());
            builder.buildPIPConfig(entitlementConfig);
            builder.buildCachingConfig(entitlementConfig);
            builder.buildPAPConfig(entitlementConfig);
            builder.buildPolicySchema(entitlementConfig);
            //TODO: Read from identit.xml, the configurations to be used in thrift based entitlement service.
            //initialize thrift authenticator
            ThriftEntitlementServiceImpl.init(thriftAuthenticationService);
            //initialize thrift based Entitlement Service.
            startThriftServices();

        } catch (Exception e) {
            log.error("Failed to initialize Entitlement Service", e);
        }
    }

    /**
     * @param ctxt
     */
    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Identity Entitlement bundle is deactivated");
        }
    }

    /**
     * sets registry service
     * @param registryService <code>RegistryService</code>
     */
    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService set in Entitlement bundle");
        }
        EntitlementServiceComponent.registryService = registryService;
    }

    /**
     * un-sets registry service  
     * @param registryService <code>RegistryService</code>
     */
    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unset in Entitlement bundle");
        }
        EntitlementServiceComponent.registryService = null;
    }

    /**
     * sets realm service
     * @param realmService <code>RealmService</code>
     */
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("DefaultUserRealm set in Entitlement bundle");
        }
        EntitlementServiceComponent.realmservice = realmService;
    }

    /**
     * un-sets realm service
     * @param realmService  <code>RealmService</code>
     */
    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("DefaultUserRealm unset in Entitlement bundle");
        }
        EntitlementServiceComponent.realmservice = null;
    }

    /**
     * @param httpService
     */
    /*protected void setHttpService(HttpService httpService) {
        httpServiceInstance = httpService;
    }

    *//**
     * @param httpService
     *//*
    protected void unsetHttpService(HttpService httpService) {
        httpServiceInstance = null;
    }*/

    /**
     * set Thrift authentication service
     * @param authenticationService  <code>ThriftAuthenticatorService</code>
     */
    protected void setThriftAuthenticationService(ThriftAuthenticatorService authenticationService) {
        if (log.isDebugEnabled()) {
            log.debug("ThriftAuthenticatorService set in Entitlement bundle");
        }
        this.thriftAuthenticationService = authenticationService;

    }

    /**
     * un-set Thrift authentication service
     * @param authenticationService  <code>ThriftAuthenticatorService</code>
     */
    protected void unsetThriftAuthenticationService(
            ThriftAuthenticatorService authenticationService) {
        if (log.isDebugEnabled()) {
            log.debug("ThriftAuthenticatorService unset in Entitlement bundle");
        }
        this.thriftAuthenticationService = null;
    }

    /**
     * @return
     */
    public static EntitlementConfigHolder getEntitlementConfig() {
        return entitlementConfig;
    }

    /**
     * @return
     */
    public static RealmService getRealmservice() {
        return realmservice;
    }

    /**
     * @param realmservice
     */
    public static void setRealmservice(RealmService realmservice) {
        EntitlementServiceComponent.realmservice = realmservice;
    }

    /**
     * Return registry service
     *
     * @return RegistryService
     */
    public static RegistryService getRegistryService() {
        return registryService;
    }

    private void startThriftServices() throws Exception {
        startThriftEntitlementService();
    }

    private void startThriftEntitlementService() throws Exception {
        try {
            //read identity.xml
            IdentityUtil.populateProperties();
            //if thrift based EntitlementService is enabled.
            String thriftEnabled = IdentityUtil.getProperty(ThriftConfigConstants.PARAM_ENABLE_THRIFT_SERVICE);

            if (thriftEnabled != null && Boolean.parseBoolean(thriftEnabled)) {

                TSSLTransportFactory.TSSLTransportParameters transportParam =
                        new TSSLTransportFactory.TSSLTransportParameters();

                //read the keystore and password used for ssl communication from config
                String keystorePath = IdentityUtil.getProperty(
                        ThriftConfigConstants.PARAM_KEYSTORE_LOCATION);
                String keystorePassword = IdentityUtil.getProperty(
                        ThriftConfigConstants.PARAM_KEYSTORE_PASSWORD);

                //set it in parameters
                transportParam.setKeyStore(keystorePath, keystorePassword);
                //int receivePort = 10395;
                int receivePort = readThriftReceivePort();
                //int clientTimeOut = 10000;
                int clientTimeOut = Integer.parseInt(IdentityUtil.getProperty(
                        ThriftConfigConstants.PARAM_CLIENT_TIMEOUT));
                //String ifAddress = "localhost";
                TServerSocket serverTransport =
                        TSSLTransportFactory.getServerSocket(receivePort,
                                                             clientTimeOut,
                                                             InetAddress.getLocalHost(),
                                                             transportParam);

                EntitlementService.Processor processor = new EntitlementService.Processor(
                        new ThriftEntitlementServiceImpl());

                //TODO: have to decide on the protocol.
                TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).
                        processor(processor));
                //TServer server = new TThreadPoolServer(new TThreadPoolServer.Args())
/*
                TServer server = new TThreadPoolServer(processor, serverTransport,
                                                   new TCompactProtocol.Factory());*/
                Runnable serverThread = new ServerRunnable(server);
                executor.submit(serverThread);

                log.info("Started thrift entitlement service at port:" + receivePort);
            }


        } catch (TTransportException e) {
            String transportErrorMsg = "Error in initializing thrift transport";
            log.error(transportErrorMsg, e);
            throw new Exception(transportErrorMsg);
        } catch (UnknownHostException e) {
            String hostErrorMsg = "Error in obtaining host name";
            log.error(hostErrorMsg, e);
            throw new Exception(hostErrorMsg);
        } catch (ServerConfigurationException e) {
            String configError = "Error in reading configuration.";
            log.error(configError, e);
            throw new Exception(configError);

        }


    }

    /**
     * Read the port from identity.xml which is overridden by carbon.xml to facilitating
     * multiple servers at a time.
     */
    private int readThriftReceivePort() {
        int port = -1;
        String portValue = IdentityUtil.getProperty(ThriftConfigConstants.PARAM_RECEIVE_PORT);
        //if the port contains a template string that refers to carbon.xml
        if ((portValue.contains("${")) && (portValue.contains("}"))) {
            port = (CarbonUtils.getPortFromServerConfig(portValue));
        } else { //if port directly mentioned in identity.xml
            port = Integer.parseInt(portValue);
        }
        return port;
    }

    /**
     * Thread that starts thrift server
     */
    private class ServerRunnable implements Runnable {
        TServer server;

        public ServerRunnable(TServer server) {
            this.server = server;
        }

        public void run() {
            server.serve();
        }
    }

}