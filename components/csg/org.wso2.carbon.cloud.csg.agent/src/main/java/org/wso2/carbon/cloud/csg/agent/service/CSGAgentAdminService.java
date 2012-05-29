/*
 * Copyright WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.cloud.csg.agent.service;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.csg.agent.CSGAgentPollingTaskFlags;
import org.wso2.carbon.cloud.csg.agent.CSGAgentUtils;
import org.wso2.carbon.cloud.csg.agent.client.AuthenticationClient;
import org.wso2.carbon.cloud.csg.agent.client.CSGAdminClient;
import org.wso2.carbon.cloud.csg.agent.transport.CSGPollingTransportReceiver;
import org.wso2.carbon.cloud.csg.agent.transport.CSGPollingTransportSender;
import org.wso2.carbon.cloud.csg.common.CSGConstant;
import org.wso2.carbon.cloud.csg.common.CSGException;
import org.wso2.carbon.cloud.csg.common.CSGProxyToolsURLs;
import org.wso2.carbon.cloud.csg.common.CSGServerBean;
import org.wso2.carbon.cloud.csg.common.CSGUtils;
import org.wso2.carbon.cloud.csg.common.thrift.CSGThriftClient;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.service.mgt.ServiceAdmin;
import org.wso2.carbon.transport.mgt.TransportAdmin;

/**
 * The class <code>CSGAgentAdminService</code> provides the admin service to manipulate the
 * CSGAgent remotely
 */
public class CSGAgentAdminService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(CSGAgentAdminService.class);

    /**
     * Deploy the proxy service
     *
     * @param serviceName the service to deploy
     * @param serverName  the serverName to publish
     * @param isAutomatic the mode of service publishing
     * @throws CSGException throws in case of an error
     */
    public void publishService(String serviceName, String serverName, boolean isAutomatic) throws CSGException {
        if (serviceName == null) {
            handleException("Service name is null!");
        }
        try {
            CSGServerBean csgServer = getCSGServerBean(serverName);
            if (csgServer == null) {
                handleException("No persist information found for the server'" + serverName + "'");
            }

            AxisService service = getAxisConfig().getService(serviceName);
            if (service == null) {
                handleException("No service found with the name '" + serviceName + "'");
            }

            CSGAdminClient csgAdminClient = getCSGAdminClient(csgServer);
            if (csgAdminClient == null) {
                handleException("CSGAdminClient is null");
            }
            String domainName = csgServer.getDomainName();
            String passWord = csgServer.getPassWord();
            String userName = CSGUtils.getFullUserName(csgServer.getUserName(), domainName);

            org.wso2.carbon.cloud.csg.stub.types.common.CSGThriftServerBean bean =
                    csgAdminClient.getThriftServerConnectionBean();
            String hostName = bean.getHostName();
            int port = bean.getPort();
            int timeOut = bean.getTimeOut();

            String trustStorePath = CSGUtils.getWSO2TrustStoreFilePath();
            String trustStorePassword = CSGUtils.getWSO2TrustStorePassword();
            // get a token for this service
            CSGThriftClient csgThriftClient =
                    new CSGThriftClient(CSGUtils.getCSGThriftClient(
                            hostName, port, timeOut, trustStorePath, trustStorePassword));
            // we use the CSG EPR as the key of the buffer
            String queueName = CSGUtils.getCSGEPR(domainName, serverName, serviceName);
            String token = csgThriftClient.login(userName, passWord, queueName);

            // expose on csgthrift transport
            new TransportAdmin().addExposedTransports(serviceName,
                    CSGConstant.CSG_POLLING_TRANSPORT_NAME);

            // encrypt and embed, so nobody can steal
            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
            service.addParameter(CSGConstant.TOKEN, cryptoUtil.encryptAndBase64Encode(token.getBytes()));
            service.addParameter(CSGConstant.CSG_SERVER_BEAN, bean);

            CSGAgentPollingTaskFlags.flagForShutDown(serviceName, false);
            if (hasInOutOperations(service)) {
                // enable CSG Thrift transport sender as well
                // FIXME - need to persist the configured transport
                enableCSGPollingTransportSender(getAxisConfig());
            }

            // enable CSG transport receiver for this service
            // FIXME - need to persist the configured transport
            enableCSGPollingTransportReceiver(getAxisConfig());
            // finally deploy the proxy
            csgAdminClient.deployProxy(getCSGServiceMetaData(service, domainName, serverName));

            flagServiceStatus(serviceName, serverName, true, isAutomatic);
        } catch (Exception e) {
            handleException("Could not publish service '" + serviceName + "'. " + e.getMessage(), e);
        }
    }

    /**
     * Un-deploy the proxy service
     *
     * @param serviceName the service to un-deploy
     * @param serverName  the server name to publish
     * @throws CSGException throws in case of an error
     */
    public void unPublishService(String serviceName, String serverName) throws CSGException {
        if (serviceName == null) {
            handleException("The service name is not supplied for un-publishing");
        }
        try {
            // remove csg polling transport from exposed list
            new TransportAdmin().removeExposedTransports(serviceName,
                    CSGConstant.CSG_POLLING_TRANSPORT_NAME);
            AxisService service = getAxisConfig().getService(serviceName);
            if (service == null) {
                handleException("No service is found with the name '" + serviceName + "'");
            }
            CSGServerBean csgServer = getCSGServerBean(serverName);
            if (csgServer == null) {
                throw new CSGException("No CSG server information found with the name '" +
                        serverName + "'");
            }
            CSGAdminClient csgAdminClient = getCSGAdminClient(csgServer);
            if (csgAdminClient == null) {
                handleException("CSGAdminClient is null");
            }
            // flag this service's polling task for shutdown
            CSGAgentPollingTaskFlags.flagForShutDown(serviceName, true);
            csgAdminClient.unDeployProxy(serviceName);
            flagServiceStatus(serviceName, serverName, false, false);
        } catch (Exception e) {
            handleException("Cloud not un-publish the service '" + serviceName + "'", e);
        }
    }

    /**
     * Add a new CSG server and store it in registry
     *
     * @param csgServer new csg server instance
     * @throws CSGException throws in case of an error
     */
    public void addCSGServer(CSGServerBean csgServer) throws CSGException {
        try {
            // authenticate using provided credentials and if logged in persist the server
            loggingToRemoteCSGServer(csgServer);

            org.wso2.carbon.registry.core.Registry registry = getConfigSystemRegistry();
            CSGAgentUtils.persistServer(registry, csgServer);
        } catch (Exception e) {
            handleException("Could not add CSG server '" + csgServer.getName() + "'. Error is " +
                    e.getMessage(), e);
        }
    }

    /**
     * Get the CSG server given by the name
     *
     * @param csgServerName csg server name
     * @return the csg server instance
     * @throws CSGException throws in case of an error
     */
    public CSGServerBean getCSGServer(String csgServerName) throws CSGException {
        org.wso2.carbon.registry.core.Registry registry = getConfigSystemRegistry();
        String resourcePath = CSGConstant.REGISTRY_SERVER_RESOURCE_PATH + "/" + csgServerName;
        try {
            if (registry.resourceExists(resourcePath)) {
                Resource resource = registry.get(resourcePath);
                return CSGAgentUtils.getCSGServerBean(resource);
            }
        } catch (RegistryException e) {
            handleException("Could not read the registry resource '" + resourcePath + "'. Error is " +
                    e.getMessage(), e);
        }
        return null;
    }

    /**
     * Get the set of CSG servers
     *
     * @return the list of CSG_TRANSPORT_NAME servers
     * @throws CSGException throws in case of an error
     */
    public CSGServerBean[] getCSGServerList() throws CSGException {
        try {
            org.wso2.carbon.registry.core.Registry registry = getConfigSystemRegistry();
            if (registry.resourceExists(CSGConstant.REGISTRY_SERVER_RESOURCE_PATH)) {
                Resource resource = registry.get(CSGConstant.REGISTRY_SERVER_RESOURCE_PATH);
                if (resource instanceof Collection) {
                    Collection collection = (Collection) resource;
                    int size = collection.getChildCount();
                    CSGServerBean[] beanInfo = new CSGServerBean[size];
                    String[] child = collection.getChildren();
                    for (int i = 0; child.length > i; i++) {
                        String s = child[i]; // returns the set of path
                        Resource childResource = registry.get(s);
                        beanInfo[i] = CSGAgentUtils.getCSGServerBean(childResource);
                    }
                    return beanInfo;
                }
            }
        } catch (RegistryException e) {
            handleException("Could not retrieve the CSG server list. Error is " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Update the CSG server and persist new information into registry
     *
     * @param csgServer new csg server information
     * @throws CSGException throws in case of an error
     */
    public void updateCSGServer(CSGServerBean csgServer) throws CSGException {

        // check if the new user can log in
        loggingToRemoteCSGServer(csgServer);

        boolean isTransactionAlreadyStarted = Transaction.isStarted();
        boolean isTransactionSuccess = true;

        org.wso2.carbon.registry.core.Registry registry = getConfigSystemRegistry();
        String resource = CSGConstant.REGISTRY_SERVER_RESOURCE_PATH + "/" + csgServer.getName();
        try {
            if (!isTransactionAlreadyStarted) {
                // start a transaction only if we are not in one.
                registry.beginTransaction();
            }

            if (registry.resourceExists(resource)) {
                // delete the resource and add it again
                registry.delete(resource);
                CSGAgentUtils.persistServer(registry, csgServer);
            }

        } catch (RegistryException e) {
            isTransactionSuccess = false;
            handleException("Could not read the registry resource '" + resource + "'. Error is "
                    + e.getMessage(), e);
        } finally {
            if (!isTransactionAlreadyStarted) {
                // commit or rollback the transaction since we started it.
                try {
                    if (isTransactionSuccess) {
                        registry.commitTransaction();
                    } else {
                        registry.rollbackTransaction();
                    }

                } catch (RegistryException re) {
                    handleException("Error occurred while trying to rollback or commit the " +
                            "transaction", re);
                }
            }
        }
    }

    /**
     * Remove the CSG server given by the name
     *
     * @param csgServerName the csg server name
     * @throws CSGException throws in case of an error
     */
    public void removeCSGServer(String csgServerName) throws CSGException {

        boolean isTransactionAlreadyStarted = Transaction.isStarted();
        boolean isTransactionSuccess = true;

        org.wso2.carbon.registry.core.Registry registry = getConfigSystemRegistry();

        try {

            if (!isTransactionAlreadyStarted) {
                // start a transaction only if we are not in one.
                registry.beginTransaction();
            }

            String resource = CSGConstant.REGISTRY_SERVER_RESOURCE_PATH + "/" + csgServerName;
            if (registry.resourceExists(resource)) {
                if (!isHasPublishedServices(csgServerName)) {
                    registry.delete(resource);
                } else {
                    handleException(csgServerName + " has services published onto it.");
                }

            } else {
                log.error("The resource '" + resource + "' does not exist!");
            }

        } catch (Exception e) {
            isTransactionSuccess = false;
            handleException("Could not remove the CSG server: " + csgServerName + ". Error is " +
                    e.getMessage(), e);
        } finally {
            if (!isTransactionAlreadyStarted) {
                try {
                    if (isTransactionSuccess) {
                        // commit the transaction since we started it.
                        registry.commitTransaction();
                    } else {
                        registry.rollbackTransaction();
                    }
                } catch (RegistryException re) {
                    handleException("Error occurred while trying to rollback or commit " +
                            "the transaction", re);
                }
            }
        }
    }

    /**
     * Returns the status of the service
     *
     * @param serviceName service name
     * @return a string states representing the service status
     * @throws CSGException throws in case of an error
     */
    public String getServiceStatus(String serviceName) throws CSGException {
        try {
            org.wso2.carbon.registry.core.Registry registry = getConfigSystemRegistry();
            String resourcePath = CSGConstant.REGISTRY_FLAG_RESOURCE_PATH + "/" + serviceName +
                    ".flag";
            if (registry.resourceExists(resourcePath)) {
                Resource resource = registry.get(resourcePath);
                return new String((byte[]) resource.getContent());
            }
        } catch (Exception e) {
            handleException("Could not retrieve the service publish flag for service '" +
                    serviceName + "'", e);
        }
        return CSGConstant.CSG_SERVICE_STATUS_UNPUBLISHED;
    }

    public void setServiceStatus(String serviceName, String status) throws CSGException {
        boolean isTransactionAlreadyStarted = Transaction.isStarted();
        boolean isTransactionSuccess = true;

        org.wso2.carbon.registry.core.Registry registry = getConfigSystemRegistry();

        try {
            if (!isTransactionAlreadyStarted) {
                // start a new transaction if there exists none.
                registry.beginTransaction();
            }

            org.wso2.carbon.registry.core.Resource resource = registry.newResource();
            resource.setContent(status);
            registry.put(CSGConstant.REGISTRY_FLAG_RESOURCE_PATH + "/" + serviceName + ".flag",
                    resource);
        } catch (Exception e) {
            isTransactionSuccess = false;
            handleException("Could not retrieve the service publish flag for service '" +
                    serviceName + "'", e);
        } finally {
            if (!isTransactionAlreadyStarted) {
                try {
                    if (isTransactionSuccess) {
                        // commit the transaction since we started it.
                        registry.commitTransaction();
                    } else {
                        registry.rollbackTransaction();
                    }
                } catch (RegistryException re) {
                    handleException("Error occurred while trying to rollback or " +
                            "commit the transaction", re);
                }
            }
        }
    }

    public void doServiceUpdate(String serviceName, int eventType) throws CSGException {
        String publishedServer = getPublishedServer(serviceName);
        if (publishedServer != null) {
            CSGServerBean csgServer = getCSGServerBean(publishedServer);
            if (csgServer == null) {
                handleException("No persist information found for the server'" + publishedServer + "'");
            }
            try {
                CSGAdminClient csgAdminClient = getCSGAdminClient(csgServer);
                if (eventType == AxisEvent.SERVICE_REMOVE) {
                    flagServiceStatus(serviceName, csgServer.getName(), false, false);
                }
                csgAdminClient.updateProxy(serviceName, eventType);
            } catch (Exception e) {
                handleException("Cloud not update service the service '" + serviceName + "'");
            }
        }
    }

    /**
     * Get the server that this service has published to
     *
     * @param serviceName service name
     * @return the server that this service has published to
     * @throws CSGException throws in case of an error
     */
    public String getPublishedServer(String serviceName) throws CSGException {
        try {
            org.wso2.carbon.registry.core.Registry registry = getConfigSystemRegistry();
            String serverResourcePath = CSGConstant.REGISTRY_FLAG_RESOURCE_PATH + "/" + serviceName
                    + ".server";
            if (registry.resourceExists(serverResourcePath)) {
                Resource serverResource = registry.get(serverResourcePath);
                if (serverResource != null && serverResource.getContent() != null) {
                    return new String((byte[]) serverResource.getContent());
                }
            }
        } catch (RegistryException e) {
            handleException("Could not retrieve the published server list. Error is " +
                    e.getMessage(), e);
        }
        return null;
    }

    public CSGProxyToolsURLs getPublishedProxyToolsURLs(String serviceName) throws CSGException {
        try {
            CSGServerBean bean = getCSGServer(getPublishedServer(serviceName));
            if (bean == null) {
                handleException("No persist server information found for the published service '"
                        + serviceName + "'");
            }
            String domainName = bean.getDomainName();
            CSGAdminClient csgAdminClient = getCSGAdminClient(bean);
            if (csgAdminClient == null) {
                handleException("CSGAdminClient is null");
            }
            org.wso2.carbon.cloud.csg.stub.types.common.CSGProxyToolsURLs tools =
                    csgAdminClient.getPublishedProxyToolsURLs(serviceName, domainName);
            CSGProxyToolsURLs tempTools = new CSGProxyToolsURLs();
            if (tools != null) {
                tempTools.setTryItURL(tools.getTryItURL());
                tempTools.setWsdl11URL(tools.getWsdl11URL());
                tempTools.setWsdl2URL(tools.getWsdl2URL());
                tempTools.setEprArray(tools.getEprArray());

                return tempTools;
            }
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Determines whether at least a one service has been published to specified CSG server.
     *
     * @param csgServerName Name of the CSG sever
     * @return true if there is at least a one service published to the CSG server or false otherwise.
     * @throws CSGException in case of an error
     */
    public boolean isHasPublishedServices(String csgServerName) throws CSGException {

        boolean isHasServices = false;
        try {
            org.wso2.carbon.registry.core.Registry registry = getConfigSystemRegistry();
            if (registry.resourceExists(CSGConstant.REGISTRY_FLAG_RESOURCE_PATH)) {
                Resource resource = registry.get(CSGConstant.REGISTRY_FLAG_RESOURCE_PATH);

                if (resource instanceof Collection) {
                    Collection serviceFlagCollection = (Collection) resource;
                    String[] flags = serviceFlagCollection.getChildren();

                    List<String> serverFlagPaths = filterForServiceFlags(flags);

                    for (String serverFlagPath : serverFlagPaths) {

                        Resource serverFlag = registry.get(serverFlagPath);
                        String publishedServer =
                                serverFlag.getContent() != null ?
                                        new String((byte[]) serverFlag.getContent())
                                        : "";
                        if (csgServerName.equals(publishedServer)) {
                            // found at least one service, published to specified server
                            isHasServices = true;
                            break;
                        }
                    }

                }

            }

        } catch (Exception e) {
            handleException("Unable to retrieve CSG services configurations", e);
        }
        return isHasServices;
    }

    private void handleException(String msg) throws CSGException {
        log.error(msg);
        throw new CSGException(msg);
    }

    private void handleException(String msg, Throwable t) throws CSGException {
        log.error(msg, t);
        throw new CSGException(msg, t);
    }

    private CSGServerBean getCSGServerBean(String csgServerName) throws CSGException {
        CSGServerBean bean = null;
        try {
            org.wso2.carbon.registry.core.Registry registry = getConfigSystemRegistry();
            String resourceName = CSGConstant.REGISTRY_SERVER_RESOURCE_PATH + "/" + csgServerName;
            if (registry.resourceExists(resourceName)) {
                org.wso2.carbon.registry.core.Resource resource = registry.get(resourceName);
                try {
                    bean = new CSGServerBean();
                    bean.setHost(resource.getProperty(CSGConstant.CSG_SERVER_HOST));
                    bean.setName(resource.getProperty(CSGConstant.CSG_SERVER_NAME));
                    bean.setUserName(resource.getProperty(CSGConstant.CSG_SERVER_USER_NAME));
                    bean.setPort(resource.getProperty(CSGConstant.CSG_SERVER_PORT));
                    bean.setDomainName(resource.getProperty(CSGConstant.CSG_SERVER_DOMAIN_NAME));

                    CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();

                    bean.setPassWord(new String(cryptoUtil.base64DecodeAndDecrypt(
                            resource.getProperty("password"))));
                } catch (CryptoException e) {
                    handleException("Could not convert into an AXIOM element");
                }
            } else {
                throw new CSGException("Resource :" + resourceName + " does not exist");
            }
        } catch (org.wso2.carbon.registry.core.exceptions.RegistryException e) {
            handleException("Could not retrieve the server information for server: " +
                    csgServerName, e);
        }
        return bean;
    }

    private static String getAuthServiceURL(CSGServerBean csgServer) {
        return "https://" + csgServer.getHost() + ":" + csgServer.getPort() +
                "/services/AuthenticationAdmin";
    }

    private static String getProxyURL(CSGServerBean csgServer) {
        return "https://" + csgServer.getHost() + ":" + csgServer.getPort() + "/services/";
    }

    private static boolean hasInOutOperations(AxisService service) {
        for (Iterator<AxisOperation> axisOpItr = service.getOperations(); axisOpItr.hasNext(); ) {
            AxisOperation axisOp = axisOpItr.next();
            if (axisOp.getAxisSpecificMEPConstant() == WSDLConstants.MEP_CONSTANT_IN_OUT) {
                return true;
            }
        }
        return false;
    }

    private void flagServiceStatus(String serviceName, String serverName, boolean isPublished,
                                   boolean isAutoMatic)
            throws CSGException {
        boolean isTransactionAlreadyStarted = Transaction.isStarted();
        boolean isTransactionSuccess = true;
        Registry registry = getConfigSystemRegistry();
        try {

            if (!isTransactionAlreadyStarted) {
                registry.beginTransaction(); // start a transaction if none exists currently.
            }

            if (!registry.resourceExists(CSGConstant.REGISTRY_CSG_RESOURCE_PATH)) {
                org.wso2.carbon.registry.core.Collection collection = registry.newCollection();
                registry.put(CSGConstant.REGISTRY_CSG_RESOURCE_PATH, collection);
            }

            org.wso2.carbon.registry.core.Resource resource = registry.newResource();
            org.wso2.carbon.registry.core.Resource serverResource = registry.newResource();
            String serverResourcePath = CSGConstant.REGISTRY_FLAG_RESOURCE_PATH + "/" +
                    serviceName + ".server";
            if (isPublished) {
                if (isAutoMatic) {
                    resource.setContent(CSGConstant.CSG_SERVICE_STATUS_AUTO_MATIC);
                } else {
                    resource.setContent(CSGConstant.CSG_SERVICE_STATUS_PUBLISHED);
                }

                serverResource.setContent(serverName);
            } else {
                resource.setContent(CSGConstant.CSG_SERVICE_STATUS_UNPUBLISHED);
                // remove the published server from the list
                if (registry.resourceExists(serverResourcePath)) {
                    registry.delete(serverResourcePath);
                }
            }
            registry.put(CSGConstant.REGISTRY_FLAG_RESOURCE_PATH + "/" + serviceName + ".flag",
                    resource);
            registry.put(CSGConstant.REGISTRY_FLAG_RESOURCE_PATH + "/" + serviceName + ".server",
                    serverResource);

        } catch (org.wso2.carbon.registry.core.exceptions.RegistryException e) {
            isTransactionSuccess = false;
            handleException("Could not flag the service '" + serviceName + "'", e);
        } finally {
            if (!isTransactionAlreadyStarted) {
                try {
                    if (isTransactionSuccess) {
                        registry.commitTransaction();
                    } else {
                        registry.rollbackTransaction();
                    }
                } catch (Exception exception) {
                    handleException("Error occurred while trying to rollback or commit the " +
                            "transaction", exception);
                }
            }
        }
    }

    private org.wso2.carbon.cloud.csg.stub.types.common.CSGServiceMetaDataBean
    getCSGServiceMetaData(AxisService service, String tenantName, String serverName)
            throws CSGException {
        try {
            org.wso2.carbon.cloud.csg.stub.types.common.CSGServiceMetaDataBean privateServiceMetaData
                    = new org.wso2.carbon.cloud.csg.stub.types.common.CSGServiceMetaDataBean();
            privateServiceMetaData.setServiceName(service.getName());

            privateServiceMetaData.setEndpoint(
                    CSGUtils.getCSGEPR(tenantName, serverName, service.getName()));

            ServiceAdmin serviceAdmin = new ServiceAdmin(getAxisConfig());
            org.wso2.carbon.service.mgt.ServiceMetaData serviceAdminMetaData =
                    serviceAdmin.getServiceData(service.getName());

            if (serviceAdminMetaData.isActive()) {
                String wsdlLocation = serviceAdminMetaData.getWsdlURLs()[0];
                OMNode wsdNode =
                        CSGAgentUtils.getOMElementFromURI(wsdlLocation);
                OMElement wsdlElement;
                if (wsdNode instanceof OMElement) {
                    wsdlElement = (OMElement) wsdNode;
                } else {
                    throw new CSGException("Invalid instance type detected when parsing the WSDL '"
                            + wsdlLocation + "'. Required OMElement type!");
                }
                privateServiceMetaData.setInLineWSDL(wsdlElement.toStringWithConsume());
            }

            if (hasInOutOperations(service)) {
                privateServiceMetaData.setHasInOutMEP(true);
            }
            return privateServiceMetaData;
        } catch (Exception e) {
            handleException("Error while retrieving the meta data of the service '" +
                    service.getName() + "'", e);
        }
        return null;
    }

    private CSGAdminClient getCSGAdminClient(CSGServerBean bean) throws CSGException {
        try {
            String domainName = bean.getDomainName();
            String passWord = bean.getPassWord();
            String sessionCookie = CSGAgentUtils.getSessionCookie(getAuthServiceURL(bean),
                    bean.getUserName(), passWord, domainName, bean.getHost());
            CSGAdminClient csgAdminClient;
            if (CSGAgentUtils.isClientAxis2XMLExists()) {
                ConfigurationContext configCtx = ConfigurationContextFactory.
                        createConfigurationContextFromFileSystem(null, CSGConstant.CLIENT_AXIS2_XML);
                csgAdminClient = new CSGAdminClient(sessionCookie, getProxyURL(bean), configCtx);
            } else {
                csgAdminClient = new CSGAdminClient(sessionCookie, getProxyURL(bean));
            }
            return csgAdminClient;
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return null;
    }

    private void enableCSGPollingTransportSender(AxisConfiguration axisConfig) throws AxisFault {
        TransportOutDescription transportOut =
                new TransportOutDescription(CSGConstant.CSG_POLLING_TRANSPORT_NAME);
        CSGPollingTransportSender txSender = new CSGPollingTransportSender();
        transportOut.setSender(txSender);
        axisConfig.addTransportOut(transportOut);
        transportOut.getSender().init(getConfigContext(), transportOut);
    }

    private void enableCSGPollingTransportReceiver(AxisConfiguration axisConfig) throws AxisFault {
        TransportInDescription transportIn =
                new TransportInDescription(CSGConstant.CSG_POLLING_TRANSPORT_NAME);
        CSGPollingTransportReceiver receiver = new CSGPollingTransportReceiver();
        transportIn.setReceiver(receiver);
        axisConfig.addTransportIn(transportIn);
        transportIn.getReceiver().init(getConfigContext(), transportIn);
        transportIn.getReceiver().start();
    }

    private List<String> filterForServiceFlags(String[] flags) {
        List<String> filtered = new ArrayList<String>();
        for (String flag : flags) {
            if (flag.endsWith(".server")) {
                filtered.add(flag);
            }
        }
        return filtered;
    }


    private void loggingToRemoteCSGServer(CSGServerBean csgServer) throws CSGException {


        String authServerUrl = "https://" + csgServer.getHost() + ":" + csgServer.getPort() +
                "/services/AuthenticationAdmin";
        AuthenticationClient authClient = new AuthenticationClient();
        authClient.getLoggedAuthAdminStub(
                authServerUrl,
                csgServer.getUserName(),
                csgServer.getPassWord(),
                csgServer.getHost(),
                csgServer.getDomainName());

    }
}
