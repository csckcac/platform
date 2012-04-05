/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.receiver.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.bam.receiver.ReceiverConstants;
import org.wso2.carbon.bam.receiver.ReceiverUtils;
import org.wso2.carbon.bam.receiver.authentication.ThriftAuthenticator;
import org.wso2.carbon.bam.receiver.persistence.PersistenceManager;
import org.wso2.carbon.bam.receiver.service.AuthenticatorServiceImpl;
import org.wso2.carbon.bam.receiver.service.AuthenticatorServlet;
import org.wso2.carbon.bam.receiver.service.ReceiverServiceImpl;
import org.wso2.carbon.bam.receiver.service.ReceiverServlet;
import org.wso2.carbon.bam.service.AuthenticatorService;
import org.wso2.carbon.bam.service.ReceiverService;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.identity.authentication.AuthenticationService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @scr.component name="bam.receiver.component" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="config.context.service" interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic" bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="realm.service" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="cassandra.service" interface="org.wso2.carbon.cassandra.dataaccess.DataAccessService"
 * cardinality="1..1" policy="dynamic" bind="setDataAccessService" unbind="unsetDataAccessService"
 * @scr.reference name="http.service" interface="org.osgi.service.http.HttpService"
 * cardinality="1..1" policy="dynamic"  bind="setHttpService" unbind="unsetHttpService"
 * @scr.reference name="org.wso2.carbon.identity.authentication.internal.AuthenticationServiceComponent"
 * interface="org.wso2.carbon.identity.authentication.AuthenticationService"
 * cardinality="1..1" policy="dynamic" bind="setAuthenticationService"  unbind="unsetAuthenticationService"
 * @scr.reference name="server.configuration" interface="org.wso2.carbon.base.ServerConfiguration"
 * cardinality="1..1" policy="dynamic" bind="setServerConfiguration" unbind="unsetServerConfiguration"
 */

public class BAMReceiverServiceComponent {

    private static Log log = LogFactory.getLog(BAMReceiverServiceComponent.class);

    private static HttpService httpServiceInstance;

    private AuthenticationService authenticationService;

    private ExecutorService executor = Executors.newFixedThreadPool(2);

    protected void activate(ComponentContext ctx) {
        try {

            ReceiverUtils.setQueue(new EventQueue());
            //ReceiverUtils.setPersistentManager(PersistenceManager.getManager());
            if (log.isDebugEnabled()) {
                log.debug("BAM Core bundle is activated");
            }

            ThriftAuthenticator.getInstance().init(authenticationService);

            //ReceiverServer.start(new ReceiverServiceImpl());

            ReceiverService.Processor processor = new ReceiverService.Processor(
                    new ReceiverServiceImpl());
            TCompactProtocol.Factory inProtFactory = new TCompactProtocol.Factory();
            TCompactProtocol.Factory outProtFactory = new TCompactProtocol.Factory();

            httpServiceInstance.registerServlet("/thriftReceiver",
                                                new ReceiverServlet(processor, inProtFactory,
                                                                    outProtFactory),
                                                new Hashtable(),
                                                httpServiceInstance.createDefaultHttpContext());

            AuthenticatorService.Processor authProcessor = new AuthenticatorService.Processor(
                    new AuthenticatorServiceImpl());
            httpServiceInstance.registerServlet("/thriftAuthenticator",
                                                new AuthenticatorServlet(authProcessor, inProtFactory,
                                                                         outProtFactory),
                                                new Hashtable(),
                                                httpServiceInstance.createDefaultHttpContext());

            startThriftServices();

        } catch (Throwable e) {
            log.fatal("Unable to start receiver..", e);
        }
    }

    private void startThriftServices() {
        // Authenticator service should be exposed over SSL. Since Thrift 0.5 doesn't have support
        // for SSL transport this is commented out for now until later Thrift version is used. Using
        // servlet based authenticator service for authentication for now.

        //startThriftAuthenticatorService();

        int receiverPort = getReceiverPort();
        startThriftReceiverService(receiverPort);
    }

/*    private void startThriftAuthenticatorService() {
        int port = 7920;
        log.info("Starting authenticator service..");
        try {
            TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(port);
            AuthenticatorService.Processor authProcessor = new AuthenticatorService.Processor(
                    new AuthenticatorServiceImpl());
            TServer server = new TNonblockingServer(authProcessor, serverTransport);
            Runnable serverRunnable = new ServerRunnable(server);
            executor.submit(serverRunnable);

        } catch (TTransportException e) {

        }

    }*/

    private int getReceiverPort() {
        int port = readReceiverPortFromConfig();
        int offset = getPortOffset();

        return (port + offset);
    }

    private int getPortOffset() {
        String portOffset = ReceiverUtils.getCarbonConfiguration().getFirstProperty(
                ReceiverConstants.CARBON_CONFIG_PORT_OFFSET_NODE);

        try {
            return ((portOffset != null) ? Integer.parseInt(portOffset.trim()) : ReceiverConstants.
                    CARBON_DEFAULT_PORT_OFFSET);
        } catch (Exception e) {
            return ReceiverConstants.CARBON_DEFAULT_PORT_OFFSET;
        }
    }

    private int readReceiverPortFromConfig() {

        int port = ReceiverConstants.DEFAULT_RECEIVER_PORT;

        String bamConfig = CarbonUtils.getCarbonConfigDirPath() + "/" + ReceiverConstants.
                BAM_CONFIGURATION_FILE;

        try {

            OMElement root = new StAXOMBuilder(new FileInputStream(bamConfig)).
                    getDocumentElement();
            OMElement portElement = root.getFirstChildWithName(new QName(ReceiverConstants.
                    RECEIVER_PORT_ELEMENT));
            String portStr = portElement.getText();

            try {
                if (portStr != null) {
                    port = Integer.parseInt(portStr);
                }
            } catch (Exception ignored) {
                // Ignore. Default port will be used.
            }
        } catch (FileNotFoundException e) {
            log.error(bamConfig + " not found. Using default receiver port : " + port);
        } catch (XMLStreamException e) {
            log.error("Invalid configuration for receiver port in " + bamConfig + ". Using default " +
                      "receiver port : " + port);
        }

        return port;

    }

    private void startThriftReceiverService(int receiverPort) {
        try {
            TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(receiverPort);
            ReceiverService.Processor processor = new ReceiverService.Processor(
                    new ReceiverServiceImpl());
            TServer server = new THsHaServer(processor,
                                             serverTransport, new TCompactProtocol.Factory());
            Runnable serRunnable = new ServerRunnable(server);
            executor.submit(serRunnable);

            log.info("Started Thrift receiver service at port : " + receiverPort);
            
        } catch (TTransportException e) {
            log.error("Unable to start Thrift receiver service at port : " + receiverPort);
        }

    }

    private class ServerRunnable implements Runnable {

        private TServer server;

        public ServerRunnable(TServer server) {
            this.server = server;
        }

        public void run() {
            server.serve();
        }

    }

    protected void deactivate(ComponentContext ctx) {
        ReceiverUtils.setQueue(null);
        if (log.isDebugEnabled()) {
            log.debug("BAM Core bundle is deactivated");
        }
    }

    protected void setDataAccessService(DataAccessService dataAccessService) {
        ReceiverUtils.setDataAccessService(dataAccessService);
    }

    protected void unsetDataAccessService(DataAccessService dataAccessService) {
        ReceiverUtils.setDataAccessService(null);
    }

    protected void setRegistryService(RegistryService registryService) throws RegistryException {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService set in BAM bundle");
        }

        ReceiverUtils.setRegistry(registryService.getConfigSystemRegistry());
    }

    protected void unsetRegistryService(RegistryService registryService) {
        ReceiverUtils.setRegistry(null);
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unset in BAM bundle");
        }
    }

    protected void setConfigurationContextService(ConfigurationContextService ccService) {

//        ConfigurationContext serverCtx = ccService.getServerConfigContext();
//        AxisConfiguration serverConfig = serverCtx.getAxisConfiguration();
//        LocalTransportReceiver.CONFIG_CONTEXT = new ConfigurationContext(serverConfig);
//        LocalTransportReceiver.CONFIG_CONTEXT.setServicePath("services");
//        LocalTransportReceiver.CONFIG_CONTEXT.setContextRoot("local:/");

        ReceiverUtils.setConfigurationContextService(ccService);
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService set in BAM bundle");
        }
    }

    protected void unsetConfigurationContextService(ConfigurationContextService ccService) {
        ReceiverUtils.setConfigurationContextService(null);
        if (log.isDebugEnabled()) {
            log.debug("ConfigurationContextService unset in BAM bundle");
        }
    }

    protected void setRealmService(RealmService realmService) {
        ReceiverUtils.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        ReceiverUtils.setRealmService(null);
    }

    protected void setServerConfiguration(ServerConfiguration serverConfiguration) {
        ReceiverUtils.setCarbonConfiguration(serverConfiguration);
    }

    protected void unsetServerConfiguration(ServerConfiguration serverConfiguration) {
        ReceiverUtils.setCarbonConfiguration(null);
    }

    protected void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    protected void unsetAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = null;
    }

    protected void setHttpService(HttpService httpService) {
        httpServiceInstance = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        httpServiceInstance = null;
    }

}
