package org.wso2.carbon.bam.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.common.dataobjects.common.EventingServerDO;
import org.wso2.carbon.bam.common.dataobjects.common.JMXServerDO;
import org.wso2.carbon.bam.common.dataobjects.service.PullServerDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;
import org.wso2.carbon.bam.core.persistence.BAMRegistryResources;
import org.wso2.carbon.bam.util.BAMConstants;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class BAMRegistryUtil {

//    private static Log log = LogFactory.getLog(BAMRegistryUtil.class);
//
//    public static synchronized void addMonitoredServer(Registry registry, ServerDO server)
//            throws BAMException {
//
//        if (server.getId() < 0) {
//            log.error("Internal Error: ServerID is always needed when persisting");
//            throw new BAMException("serverID in the provided serverDO is null");
//        }
//
//        try {
//            String serverPath = getRegistryMonitoredServerPath(server);
//            Collection collection = registry.newCollection();
//            collection.addProperty(BAMRegistryResources.SERVER_URL_PROPERTY, server.getServerURL());
//            collection.addProperty(BAMRegistryResources.USERNAME_PROPERTY, server.getUserName());
//            collection.addProperty(BAMRegistryResources.SERVER_ID_PROPERTY, String.valueOf(server.getId()));
//            collection.addProperty(BAMRegistryResources.SERVER_TYPE, server.getServerType());
//            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
//
//            if (server.getPassword() != null) {
//                try {
//                    String cipherTextPassword = cryptoUtil.
//                            encryptAndBase64Encode(server.getPassword().getBytes());
//                    collection.addProperty(BAMRegistryResources.PASSWORD_PROPERTY, cipherTextPassword);
//
//                } catch (CryptoException e) {
//                    throw new BAMException("failed to store monitored server credentials ", e);
//                }
//            }
//
//            collection.addProperty(BAMRegistryResources.CATEGORY_PROPERTY, String.valueOf(server.getCategory()));
//            collection.addProperty(BAMRegistryResources.DESCRIPTION_PROPERTY, server.getDescription());
//            collection.addProperty(BAMRegistryResources.ACTIVE_PROPERTY, "true"); // when a server is
//
//            if (server.getSubscriptionID() != null) {
//                collection.addProperty(BAMRegistryResources.SUBSCRIPTION_ID_PROPERTY, server.getSubscriptionID());
//            }
//
///*            if (server instanceof PullServerDO) {
//                collection.addProperty(BAMRegistryResources.POLLING_INTERVAL_PROPERTY, String
//                        .valueOf(((PullServerDO) server).getPollingInterval()));
//            }*/
//
//            int tenantID = server.getTenantID();
//            collection.addProperty(BAMRegistryResources.TENANT_ID_PROPERTY, String.valueOf(tenantID));
//            registry.put(serverPath, collection);
//            if (log.isDebugEnabled()) {
//                log.debug("New monitored server added to registry");
//            }
//
//        } catch (RegistryException e) {
//            log.error("Could not add new monitored server " + server.getServerURL() + " to Registry. ", e);
//            throw new BAMException("Could not add new monitored server " + server.getServerURL() + " to Registry.", e);
//        }
//
//    }
//
//    public static void removeMonitoredServer(Registry registry, int serverID) throws BAMException {
//        try {
//            if (registry.resourceExists(BAMRegistryResources.PULL_SERVERS_PATH + serverID)) {
//                registry.delete(BAMRegistryResources.PULL_SERVERS_PATH + serverID);
//            }
//            if (registry.resourceExists(BAMRegistryResources.EVENTING_SERVERS_PATH + serverID)) {
//                registry.delete(BAMRegistryResources.EVENTING_SERVERS_PATH + serverID);
//            }
//            if (registry.resourceExists(BAMRegistryResources.GENERIC_SERVERS_PATH + serverID)) {
//                registry.delete(BAMRegistryResources.GENERIC_SERVERS_PATH + serverID);
//            }
//            if (registry.resourceExists(BAMRegistryResources.JMX_SERVERS_PATH + serverID)) {
//                registry.delete(BAMRegistryResources.JMX_SERVERS_PATH + serverID);
//            }
//        } catch (RegistryException e) {
//            log.error("Could not delete the server [" + serverID + "] from Registry - " +
//                      e.getMessage());
//            throw new BAMException("Could not delete the server [" + serverID + "] from Registry ", e);
//        }
//    }
//
//    public static void updateMonitoredServer(Registry registry, ServerDO server)
//            throws BAMException {
//        try {
//            String serverPath = getRegistryMonitoredServerPath(server);
//            Collection serverCollection = null;
//            if (registry.resourceExists(serverPath)) {
//                serverCollection = (Collection) registry.get(serverPath);
//            }
//
//            if (serverCollection != null) {
//                serverCollection.setProperty(BAMRegistryResources.SERVER_ID_PROPERTY,
//                                             String.valueOf(server.getId()));
//                serverCollection.setProperty(BAMRegistryResources.SERVER_URL_PROPERTY,
//                                             server.getServerURL());
//
//                if (server.getSubscriptionEPR() != null) {
//                    serverCollection.setProperty(BAMRegistryResources.SUBSCRIPTION_EPR_PROPERTY,
//                                                 server.getSubscriptionEPR());
//                    serverCollection.setProperty(BAMRegistryResources.SUBSCRIPTION_ID_PROPERTY,
//                                                 server.getSubscriptionID());
//                }
//
//                if (server.getDescription() != null) {
//                    serverCollection.setProperty(BAMRegistryResources.DESCRIPTION_PROPERTY,
//                                                 server.getDescription());
//                }
//            }
//        } catch (RegistryException e) {
//            log.error("Could not update the server [" + server.getId() + "] in the Registry - " +
//                      e.getMessage());
//            throw new BAMException("Could not update the server [" + server.getId() +
//                                   "] in the Registry - ", e);
//        }
//    }
//
//    public static void deactivateServer(Registry registry, int serverID) throws BAMException {
//        Collection serverCollection = null;
//        String collectionPath = BAMRegistryResources.PULL_SERVERS_PATH + serverID;
//
//        try {
//            if (registry.resourceExists(collectionPath)) {
//                serverCollection = (Collection) registry.get(collectionPath);
//            }
//        } catch (Exception ignored) {
//        }
//
//        if (serverCollection == null) {
//            try {
//                collectionPath = BAMRegistryResources.EVENTING_SERVERS_PATH + serverID;
//                if (registry.resourceExists(collectionPath)) {
//                    serverCollection = (Collection) registry.get(collectionPath);
//                }
//            } catch (Exception ignored) {
//            }
//        }
//        if (serverCollection == null) {
//            try {
//                collectionPath = BAMRegistryResources.GENERIC_SERVERS_PATH + serverID;
//                if (registry.resourceExists(collectionPath)) {
//                    serverCollection = (Collection) registry.get(collectionPath);
//                }
//            } catch (Exception ignored) {
//            }
//        }
//        if (serverCollection == null) {
//            try {
//                collectionPath = BAMRegistryResources.JMX_SERVERS_PATH + serverID;
//                if (registry.resourceExists(collectionPath)) {
//                    serverCollection = (Collection) registry.get(collectionPath);
//                }
//            } catch (Exception ignored) {
//            }
//        }
//        if (serverCollection != null) {
//            try {
//                serverCollection.setProperty(BAMRegistryResources.ACTIVE_PROPERTY, "false");
//                registry.put(collectionPath, serverCollection);
//            } catch (Exception e) {
//                log.error("Could not deactivate the server [" + serverID + "] from Registry - " +
//                          e.getMessage());
//                throw new BAMException("Could not deactivate the server [" + serverID +
//                                       "] from Registry ", e);
//            }
//        }
//    }
//
//    public static void activateServer(Registry registry, int serverID, String subscriptionID)
//            throws BAMException {
//        Collection serverCollection = null;
//        String collectionPath = BAMRegistryResources.PULL_SERVERS_PATH + serverID;
//
//        try {
//            if (registry.resourceExists(collectionPath)) {
//                serverCollection = (Collection) registry.get(collectionPath);
//            }
//        } catch (Exception ignored) {
//
//        }
//
//        if (serverCollection == null) {
//            try {
//                collectionPath = BAMRegistryResources.EVENTING_SERVERS_PATH + serverID;
//                if (registry.resourceExists(collectionPath)) {
//                    serverCollection = (Collection) registry.get(collectionPath);
//                }
//            } catch (Exception ignored) {
//
//            }
//        }
//        if (serverCollection == null) {
//            try {
//                collectionPath = BAMRegistryResources.GENERIC_SERVERS_PATH + serverID;
//                if (registry.resourceExists(collectionPath)) {
//                    serverCollection = (Collection) registry.get(collectionPath);
//                }
//            } catch (Exception ignored) {
//
//            }
//        }
//
//        if (serverCollection != null) {
//            try {
//                serverCollection.setProperty(BAMRegistryResources.ACTIVE_PROPERTY, "true");
//                serverCollection.setProperty(BAMRegistryResources.SUBSCRIPTION_ID_PROPERTY, subscriptionID);
//                registry.put(collectionPath, serverCollection);
//            } catch (Exception e) {
//                log.error("Could not deactivate the server with ID [" + serverID + "] from Registry - " +
//                          e.getMessage());
//                throw new BAMException("Could not deactivate the server [" + serverID +
//                                       "] from Registry ", e);
//            }
//        } else {
//            log.error("Could not find registry collection corresponding to server ID : " + serverID);
//            throw new BAMException("Could not deactivate the server with ID " + serverID + ". Could not find registry collection corresponding to server ID");
//        }
//    }
//
///*
//    public static void updateMonitoredServer(Registry registry, ServerDO server)
//            throws BAMException {
//        removeMonitoredServer(registry, server.getId());
//        addMonitoredServer(registry, server);
//    }
//*/
//
//    public static ServerDO getMonitoringServerDetails(Registry registry, String serverType,
//                                                      String serverURL) throws BAMException {
//        Collection severDetailCollection;
//        ServerDO server = null;
//        try {
//            if (serverType.equals("EventingServer")) {
//
//                severDetailCollection = (Collection) registry.get(BAMRegistryResources.EVENTING_SERVERS_PATH);
//                server = new EventingServerDO();
//            } else if (serverType.equals("PullServer")) {
//
//                severDetailCollection = (Collection) registry.get(BAMRegistryResources.PULL_SERVERS_PATH);
//                server = new PullServerDO();
//
//            } else if (serverType.equals("GenericServer")) {
//                severDetailCollection = (Collection) registry.get(BAMRegistryResources.GENERIC_SERVERS_PATH);
//                server = new ServerDO();
//            } else if (serverType.equals("JMXServer")) {
//                severDetailCollection = (Collection) registry.get(BAMRegistryResources.JMX_SERVERS_PATH);
//                server = new JMXServerDO();
//            } else {
//                throw new BAMException("can not get " + serverType + " type of server details ");
//            }
//
//            if (severDetailCollection != null) {
//                if (severDetailCollection.getChildCount() > 0) {
//                    String[] children = severDetailCollection.getChildren();
//                    for (String child : children) {
//                        Collection regServer = (Collection) registry.get(child);
//                        if (serverURL.equals(regServer.getProperty(BAMRegistryResources.SERVER_URL_PROPERTY))) {
//                            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
//                            if (regServer.getProperty(BAMRegistryResources.PASSWORD_PROPERTY) != null) {
//                                try {
//                                    byte[] passwordBytes = cryptoUtil.
//                                            base64DecodeAndDecrypt(regServer.getProperty(BAMRegistryResources.PASSWORD_PROPERTY));
//                                    server.setPassword(new String(passwordBytes));
//                                    server.setUserName(regServer.getProperty(BAMRegistryResources.USERNAME_PROPERTY));
//                                } catch (CryptoException e) {
//                                    throw new BAMException("server credential getting failed", e);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//        } catch (RegistryException e) {
//            e.printStackTrace();
//        }
//        return server;
//    }
//
//
//    public static List<ServerDO> getMonitoredServers(Registry registry, int tenantID)
//            throws BAMException {
//        // if (BAMUtil.getServersListCache() == null) {
//        // MonitoredServerListCache cache = BAMUtil.initServerListCache();
//        List<ServerDO> monitoredServers = new ArrayList<ServerDO>();
//        Collection serverCollection = null;
//        try {
//            if (registry.resourceExists(BAMRegistryResources.PULL_SERVERS_PATH)) {
//                serverCollection = (Collection) registry.get(BAMRegistryResources.PULL_SERVERS_PATH);
//            }
//            if (serverCollection != null && serverCollection.getChildCount() > 0) {
//                String[] children = serverCollection.getChildren();
//                for (String child : children) {
//
//                    String serverString = child.substring(child.lastIndexOf('/') + 1);
//                    int serverID = Integer.parseInt(serverString);
//                    Collection regServer = (Collection) registry.get(child);
//                    int regServerTenantID = Integer.parseInt(regServer.getProperty(BAMRegistryResources.TENANT_ID_PROPERTY));
//                    PullServerDO server = new PullServerDO();
//                    server.setId(serverID);
//                    server.setServerURL(regServer.getProperty(BAMRegistryResources.SERVER_URL_PROPERTY));
//
//                    server.setServerType(regServer.getProperty(BAMRegistryResources.SERVER_TYPE));
//                    CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
//                    if (regServer.getProperty(BAMRegistryResources.PASSWORD_PROPERTY) != null) {
//                        try {
//                            byte[] passwordBytes = cryptoUtil.
//                                    base64DecodeAndDecrypt(regServer.getProperty(BAMRegistryResources.PASSWORD_PROPERTY));
//                            String value = new String(passwordBytes);
//                            server.setPassword(value);
//                            server.setUserName(regServer.getProperty(BAMRegistryResources.USERNAME_PROPERTY));
//
//                        } catch (CryptoException e) {
//                            throw new BAMException("server credential getting failed", e);
//                        }
//                    }
//
//                    server.setTenantID(regServerTenantID);
//                    String statType = regServer.getProperty(BAMRegistryResources.CATEGORY_PROPERTY);
//
//                    if (statType != null) {
//                        server.setCategory(Integer.parseInt(statType));
//                    }
//
//                    server.setDescription(regServer.getProperty(BAMRegistryResources.DESCRIPTION_PROPERTY));
//
//                    if ("true".equals(regServer.getProperty(BAMRegistryResources.ACTIVE_PROPERTY))) {
//                        server.setActive(BAMConstants.SERVER_ACTIVE_STATE);
//
//                    } else {
//                        server.setActive(BAMConstants.SERVER_INACTIVE_STATE);
//                    }
////                    server.setPollingInterval(Long.parseLong(regServer.getProperty(BAMRegistryResources.POLLING_INTERVAL_PROPERTY)));
//
//                    BAMUtil.getServersListCache().addServer(server);
//                    if ((tenantID == -1) || (regServerTenantID == tenantID)) {
//                        monitoredServers.add(server);
//                    }
//                }
//            }
//        } catch (ResourceNotFoundException e) {
//            // No pull servers found!
//        } catch (RegistryException e) {
//            String msg = "Error talking to registry";
//            log.debug(msg);
//            throw new BAMException(msg, e);
//        }
//
//        try {
//            if (registry.resourceExists(BAMRegistryResources.EVENTING_SERVERS_PATH)) {
//                serverCollection = (Collection) registry.get(BAMRegistryResources.EVENTING_SERVERS_PATH);
//            }
//            if (serverCollection != null && serverCollection.getChildCount() > 0) {
//                String[] children = serverCollection.getChildren();
//
//                for (String child : children) {
//
//                    String serverString = child.substring(child.lastIndexOf('/') + 1);
//                    int serverID = Integer.parseInt(serverString);
//                    Collection regServer = (Collection) registry.get(child);
//                    int regServerTenantID = Integer.parseInt(regServer.getProperty(BAMRegistryResources.TENANT_ID_PROPERTY));
//
//                    EventingServerDO server = new EventingServerDO();
//                    server.setId(serverID);
//                    server.setServerURL(regServer.getProperty(BAMRegistryResources.SERVER_URL_PROPERTY));
//                    server.setServerType(regServer.getProperty(BAMRegistryResources.SERVER_TYPE));
//                    CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
//
//                    if (regServer.getProperty(BAMRegistryResources.PASSWORD_PROPERTY) != null) {
//                        try {
//                            byte[] passwordBytes = cryptoUtil.base64DecodeAndDecrypt(regServer.getProperty(BAMRegistryResources.PASSWORD_PROPERTY));
//                            String value = new String(passwordBytes);
//                            server.setPassword(value);
//                            server.setUserName(regServer.getProperty(BAMRegistryResources.USERNAME_PROPERTY));
//
//                        } catch (CryptoException e) {
//                            throw new BAMException("server credential getting failed", e);
//                        }
//                    }
//                    server.setTenantID(regServerTenantID);
//                    String statType = regServer.getProperty(BAMRegistryResources.CATEGORY_PROPERTY);
//                    if (statType != null) {
//                        server.setCategory(Integer.parseInt(statType));
//                    }
//                    server.setDescription(regServer.getProperty(BAMRegistryResources.DESCRIPTION_PROPERTY));
//                    if ("true".equals(regServer.getProperty(BAMRegistryResources.ACTIVE_PROPERTY))) {
//                        server.setActive(BAMConstants.SERVER_ACTIVE_STATE);
//
//                    } else {
//                        server.setActive(BAMConstants.SERVER_INACTIVE_STATE);
//                    }
//                    server.setSubscriptionID(regServer.getProperty(BAMRegistryResources.SUBSCRIPTION_ID_PROPERTY));
//                    // server.setDataRetention(TimeRange.parseTimeRange(regServer.getProperty(DATA_RETENTION_PROPERTY)));
//                    // server.setSummaryInterval(TimeRange.parseTimeRange(regServer.getProperty(SUMMARY_INTERVAL_PROPERTY)));
//                    if ((tenantID == -1) || (regServerTenantID == tenantID)) {
//                        monitoredServers.add(server);
//                    }
//                    BAMUtil.getServersListCache().addServer(server);
//                }
//            }
//        } catch (ResourceNotFoundException e) {
//            // No eventing servers found!
//        } catch (RegistryException e) {
//            String msg = "Error talking to registry";
//            log.debug(msg);
//            throw new BAMException(msg, e);
//        }
//
//        try {
//
//            if (registry.resourceExists(BAMRegistryResources.GENERIC_SERVERS_PATH)) {
//                serverCollection = (Collection) registry.get(BAMRegistryResources.GENERIC_SERVERS_PATH);
//            }
//            if (serverCollection != null && serverCollection.getChildCount() > 0) {
//                String[] children = serverCollection.getChildren();
//
//                for (String child : children) {
//                    String serverString = child.substring(child.lastIndexOf('/') + 1);
//                    int serverID = Integer.parseInt(serverString);
//
//                    Collection regServer = (Collection) registry.get(child);
//                    int regServerTenantID = Integer.parseInt(regServer.getProperty(BAMRegistryResources.TENANT_ID_PROPERTY));
//                    ServerDO server = new ServerDO();
//                    server.setId(serverID);
//                    server.setServerURL(regServer.getProperty(BAMRegistryResources.SERVER_URL_PROPERTY));
//                    server.setServerType(regServer.getProperty(BAMRegistryResources.SERVER_TYPE));
//
//                    CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
//
//                    if (regServer.getProperty(BAMRegistryResources.PASSWORD_PROPERTY) != null) {
//                        try {
//                            byte[] passwordBytes = cryptoUtil.base64DecodeAndDecrypt(regServer
//                                    .getProperty(BAMRegistryResources.PASSWORD_PROPERTY));
//                            String value = new String(passwordBytes);
//                            server.setPassword(value);
//                            server.setUserName(regServer.getProperty(BAMRegistryResources.USERNAME_PROPERTY));
//
//                        } catch (CryptoException e) {
//                            throw new BAMException("server credential getting failed", e);
//                        }
//                    }
//                    server.setTenantID(regServerTenantID);
//                    String statType = regServer.getProperty(BAMRegistryResources.CATEGORY_PROPERTY);
//
//                    if (statType != null) {
//                        server.setCategory(Integer.parseInt(statType));
//                    }
//                    server.setDescription(regServer.getProperty(BAMRegistryResources.DESCRIPTION_PROPERTY));
//                    if ("true".equals(regServer.getProperty(BAMRegistryResources.ACTIVE_PROPERTY))) {
//                        server.setActive(BAMConstants.SERVER_ACTIVE_STATE);
//
//                    } else {
//                        server.setActive(BAMConstants.SERVER_INACTIVE_STATE);
//                    }                    // server.setDataRetention(TimeRange.parseTimeRange(regServer.getProperty(DATA_RETENTION_PROPERTY)));
//                    // server.setSummaryInterval(TimeRange.parseTimeRange(regServer.getProperty(SUMMARY_INTERVAL_PROPERTY)));
//                    if ((tenantID == -1) || (regServerTenantID == tenantID)) {
//                        monitoredServers.add(server);
//                    }
//                    BAMUtil.getServersListCache().addServer(server);
//                }
//            }
//        } catch (ResourceNotFoundException e) {
//            // No eventing servers found!
//        } catch (RegistryException e) {
//            String msg = "Error talking to registry";
//            log.debug(msg);
//            throw new BAMException(msg, e);
//        }
//        try {
//
//            if (registry.resourceExists(BAMRegistryResources.JMX_SERVERS_PATH)) {
//                serverCollection = (Collection) registry.get(BAMRegistryResources.JMX_SERVERS_PATH);
//            }
//            if (serverCollection != null && serverCollection.getChildCount() > 0) {
//
//                String[] children = serverCollection.getChildren();
//                for (String child : children) {
//
//                    String serverString = child.substring(child.lastIndexOf('/') + 1);
//                    int serverID = Integer.parseInt(serverString);
//
//                    Collection regServer = (Collection) registry.get(child);
//                    int regServerTenantID = Integer.parseInt(regServer
//                            .getProperty(BAMRegistryResources.TENANT_ID_PROPERTY));
//                    JMXServerDO server = new JMXServerDO();
//                    server.setId(serverID);
//                    server.setServerURL(regServer.getProperty(BAMRegistryResources.SERVER_URL_PROPERTY));
//                    server.setServerType(regServer.getProperty(BAMRegistryResources.SERVER_TYPE));
//
//                    CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
//                    if (regServer.getProperty(BAMRegistryResources.PASSWORD_PROPERTY) != null) {
//                        try {
//                            byte[] passwordBytes = cryptoUtil.base64DecodeAndDecrypt(regServer
//                                    .getProperty(BAMRegistryResources.PASSWORD_PROPERTY));
//                            String value = new String(passwordBytes);
//                            server.setPassword(value);
//                            server.setUserName(regServer.getProperty(BAMRegistryResources.USERNAME_PROPERTY));
//
//                        } catch (CryptoException e) {
//                            throw new BAMException("server credential getting failed", e);
//                        }
//                    }
//                    server.setTenantID(regServerTenantID);
//                    String statType = regServer.getProperty(BAMRegistryResources.CATEGORY_PROPERTY);
//
//                    if (statType != null) {
//                        server.setCategory(Integer.parseInt(statType));
//                    }
//
//                    server.setDescription(regServer.getProperty(BAMRegistryResources.DESCRIPTION_PROPERTY));
//                    if ("true".equals(regServer.getProperty(BAMRegistryResources.ACTIVE_PROPERTY))) {
//                        server.setActive(BAMConstants.SERVER_ACTIVE_STATE);
//
//                    } else {
//                        server.setActive(BAMConstants.SERVER_INACTIVE_STATE);
//                    }
//                    if ((tenantID == -1) || (regServerTenantID == tenantID)) {
//                        monitoredServers.add(server);
//                    }
//
//                    BAMUtil.getServersListCache().addServer(server);
//                }
//            }
//        } catch (ResourceNotFoundException e) {
//            // No JMX servers found!
//        } catch (RegistryException e) {
//            String msg = "Error talking to registry";
//            log.debug(msg);
//            throw new BAMException(msg, e);
//        }
//
//        return monitoredServers;
//    }
//
//    public static ServerDO populateServerCredentials(Registry registry, ServerDO server)
//            throws BAMException {
//        try {
//            String serverPath = getRegistryMonitoredServerPath(server);
//            Collection serverCollection = null;
//            if (registry.resourceExists(serverPath)) {
//                serverCollection = (Collection) registry.get(serverPath);
//            }
//
//            if (serverCollection != null) {
//                CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
//                if (serverCollection.getProperty(BAMRegistryResources.PASSWORD_PROPERTY) != null) {
//                    try {
//                        byte[] passwordBytes = cryptoUtil.
//                                base64DecodeAndDecrypt(serverCollection.
//                                        getProperty(BAMRegistryResources.PASSWORD_PROPERTY));
//                        String value = new String(passwordBytes);
//                        server.setPassword(value);
//                        server.setUserName(serverCollection.
//                                getProperty(BAMRegistryResources.USERNAME_PROPERTY));
//
//                    } catch (CryptoException e) {
//                        throw new BAMException("server credential getting failed", e);
//                    }
//                }
//            }
//        } catch (RegistryException e) {
//            log.error("Could not update the server [" + server.getId() + "] in the Registry - " +
//                      e.getMessage());
//            throw new BAMException("Could not update the server [" + server.getId() +
//                                   "] in the Registry - ", e);
//        }
//
//        return server;
//    }
//
//    private static Log log = LogFactory.getLog(BAMRegistryUtil.class);
//
//    public static synchronized void addMonitoredServer(Registry registry, ServerDO server)
//            throws BAMException {
//
//        if (server.getId() < 0) {
//            log.error("Internal Error: ServerID is always needed when persisting");
//            throw new BAMException("serverID in the provided serverDO is null");
//        }
//
//        try {
//            String serverPath = getRegistryMonitoredServerPath(server);
//            Collection collection = registry.newCollection();
//            collection.addProperty(BAMRegistryResources.SERVER_URL_PROPERTY, server.getServerURL());
//            collection.addProperty(BAMRegistryResources.USERNAME_PROPERTY, server.getUserName());
//            collection.addProperty(BAMRegistryResources.SERVER_ID_PROPERTY, String.valueOf(server.getId()));
//            collection.addProperty(BAMRegistryResources.SERVER_TYPE, server.getServerType());
//            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
//
//            if (server.getPassword() != null) {
//                try {
//                    String cipherTextPassword = cryptoUtil.
//                            encryptAndBase64Encode(server.getPassword().getBytes());
//                    collection.addProperty(BAMRegistryResources.PASSWORD_PROPERTY, cipherTextPassword);
//
//                } catch (CryptoException e) {
//                    throw new BAMException("failed to store monitored server credentials ", e);
//                }
//            }
//
//            collection.addProperty(BAMRegistryResources.CATEGORY_PROPERTY, String.valueOf(server.getCategory()));
//            collection.addProperty(BAMRegistryResources.DESCRIPTION_PROPERTY, server.getDescription());
//            collection.addProperty(BAMRegistryResources.ACTIVE_PROPERTY, "true"); // when a server is
//
//            if (server.getSubscriptionID() != null) {
//                collection.addProperty(BAMRegistryResources.SUBSCRIPTION_ID_PROPERTY, server.getSubscriptionID());
//            }
//
///*            if (server instanceof PullServerDO) {
//                collection.addProperty(BAMRegistryResources.POLLING_INTERVAL_PROPERTY, String
//                        .valueOf(((PullServerDO) server).getPollingInterval()));
//            }*/
//
//            int tenantID = server.getTenantID();
//            collection.addProperty(BAMRegistryResources.TENANT_ID_PROPERTY, String.valueOf(tenantID));
//            registry.put(serverPath, collection);
//            if (log.isDebugEnabled()) {
//                log.debug("New monitored server added to registry");
//            }
//
//        } catch (RegistryException e) {
//            log.error("Could not add new monitored server " + server.getServerURL() + " to Registry. ", e);
//            throw new BAMException("Could not add new monitored server " + server.getServerURL() + " to Registry.", e);
//        }
//
//    }
//
//    public static void removeMonitoredServer(Registry registry, int serverID) throws BAMException {
//        try {
//            if (registry.resourceExists(BAMRegistryResources.PULL_SERVERS_PATH + serverID)) {
//                registry.delete(BAMRegistryResources.PULL_SERVERS_PATH + serverID);
//            }
//            if (registry.resourceExists(BAMRegistryResources.EVENTING_SERVERS_PATH + serverID)) {
//                registry.delete(BAMRegistryResources.EVENTING_SERVERS_PATH + serverID);
//            }
//            if (registry.resourceExists(BAMRegistryResources.GENERIC_SERVERS_PATH + serverID)) {
//                registry.delete(BAMRegistryResources.GENERIC_SERVERS_PATH + serverID);
//            }
//            if (registry.resourceExists(BAMRegistryResources.JMX_SERVERS_PATH + serverID)) {
//                registry.delete(BAMRegistryResources.JMX_SERVERS_PATH + serverID);
//            }
//        } catch (RegistryException e) {
//            log.error("Could not delete the server [" + serverID + "] from Registry - " +
//                      e.getMessage());
//            throw new BAMException("Could not delete the server [" + serverID + "] from Registry ", e);
//        }
//    }
//
//    public static void updateMonitoredServer(Registry registry, ServerDO server)
//            throws BAMException {
//        try {
//            String serverPath = getRegistryMonitoredServerPath(server);
//            Collection serverCollection = null;
//            if (registry.resourceExists(serverPath)) {
//                serverCollection = (Collection) registry.get(serverPath);
//            }
//
//            if (serverCollection != null) {
//                serverCollection.setProperty(BAMRegistryResources.SERVER_ID_PROPERTY,
//                                             String.valueOf(server.getId()));
//                serverCollection.setProperty(BAMRegistryResources.SERVER_URL_PROPERTY,
//                                             server.getServerURL());
//
//                if (server.getSubscriptionEPR() != null) {
//                    serverCollection.setProperty(BAMRegistryResources.SUBSCRIPTION_EPR_PROPERTY,
//                                                 server.getSubscriptionEPR());
//                    serverCollection.setProperty(BAMRegistryResources.SUBSCRIPTION_ID_PROPERTY,
//                                                 server.getSubscriptionID());
//                }
//
//                if (server.getDescription() != null) {
//                    serverCollection.setProperty(BAMRegistryResources.DESCRIPTION_PROPERTY,
//                                                 server.getDescription());
//                }
//            }
//        } catch (RegistryException e) {
//            log.error("Could not update the server [" + server.getId() + "] in the Registry - " +
//                      e.getMessage());
//            throw new BAMException("Could not update the server [" + server.getId() +
//                                   "] in the Registry - ", e);
//        }
//    }
//
//    public static void deactivateServer(Registry registry, int serverID) throws BAMException {
//        Collection serverCollection = null;
//        String collectionPath = BAMRegistryResources.PULL_SERVERS_PATH + serverID;
//
//        try {
//            if (registry.resourceExists(collectionPath)) {
//                serverCollection = (Collection) registry.get(collectionPath);
//            }
//        } catch (Exception ignored) {
//        }
//
//        if (serverCollection == null) {
//            try {
//                collectionPath = BAMRegistryResources.EVENTING_SERVERS_PATH + serverID;
//                if (registry.resourceExists(collectionPath)) {
//                    serverCollection = (Collection) registry.get(collectionPath);
//                }
//            } catch (Exception ignored) {
//            }
//        }
//        if (serverCollection == null) {
//            try {
//                collectionPath = BAMRegistryResources.GENERIC_SERVERS_PATH + serverID;
//                if (registry.resourceExists(collectionPath)) {
//                    serverCollection = (Collection) registry.get(collectionPath);
//                }
//            } catch (Exception ignored) {
//            }
//        }
//        if (serverCollection == null) {
//            try {
//                collectionPath = BAMRegistryResources.JMX_SERVERS_PATH + serverID;
//                if (registry.resourceExists(collectionPath)) {
//                    serverCollection = (Collection) registry.get(collectionPath);
//                }
//            } catch (Exception ignored) {
//            }
//        }
//        if (serverCollection != null) {
//            try {
//                serverCollection.setProperty(BAMRegistryResources.ACTIVE_PROPERTY, "false");
//                registry.put(collectionPath, serverCollection);
//            } catch (Exception e) {
//                log.error("Could not deactivate the server [" + serverID + "] from Registry - " +
//                          e.getMessage());
//                throw new BAMException("Could not deactivate the server [" + serverID +
//                                       "] from Registry ", e);
//            }
//        }
//    }
//
//    public static void activateServer(Registry registry, int serverID, String subscriptionID)
//            throws BAMException {
//        Collection serverCollection = null;
//        String collectionPath = BAMRegistryResources.PULL_SERVERS_PATH + serverID;
//
//        try {
//            if (registry.resourceExists(collectionPath)) {
//                serverCollection = (Collection) registry.get(collectionPath);
//            }
//        } catch (Exception ignored) {
//
//        }
//
//        if (serverCollection == null) {
//            try {
//                collectionPath = BAMRegistryResources.EVENTING_SERVERS_PATH + serverID;
//                if (registry.resourceExists(collectionPath)) {
//                    serverCollection = (Collection) registry.get(collectionPath);
//                }
//            } catch (Exception ignored) {
//
//            }
//        }
//        if (serverCollection == null) {
//            try {
//                collectionPath = BAMRegistryResources.GENERIC_SERVERS_PATH + serverID;
//                if (registry.resourceExists(collectionPath)) {
//                    serverCollection = (Collection) registry.get(collectionPath);
//                }
//            } catch (Exception ignored) {
//
//            }
//        }
//
//        if (serverCollection != null) {
//            try {
//                serverCollection.setProperty(BAMRegistryResources.ACTIVE_PROPERTY, "true");
//                serverCollection.setProperty(BAMRegistryResources.SUBSCRIPTION_ID_PROPERTY, subscriptionID);
//                registry.put(collectionPath, serverCollection);
//            } catch (Exception e) {
//                log.error("Could not deactivate the server with ID [" + serverID + "] from Registry - " +
//                          e.getMessage());
//                throw new BAMException("Could not deactivate the server [" + serverID +
//                                       "] from Registry ", e);
//            }
//        } else {
//            log.error("Could not find registry collection corresponding to server ID : " + serverID);
//            throw new BAMException("Could not deactivate the server with ID " + serverID + ". Could not find registry collection corresponding to server ID");
//        }
//    }
//
///*
//    public static void updateMonitoredServer(Registry registry, ServerDO server)
//            throws BAMException {
//        removeMonitoredServer(registry, server.getId());
//        addMonitoredServer(registry, server);
//    }
//*/
//
//    public static ServerDO getMonitoringServerDetails(Registry registry, String serverType,
//                                                      String serverURL) throws BAMException {
//        Collection severDetailCollection;
//        ServerDO server = null;
//        try {
//            if (serverType.equals("EventingServer")) {
//
//                severDetailCollection = (Collection) registry.get(BAMRegistryResources.EVENTING_SERVERS_PATH);
//                server = new EventingServerDO();
//            } else if (serverType.equals("PullServer")) {
//
//                severDetailCollection = (Collection) registry.get(BAMRegistryResources.PULL_SERVERS_PATH);
//                server = new PullServerDO();
//
//            } else if (serverType.equals("GenericServer")) {
//                severDetailCollection = (Collection) registry.get(BAMRegistryResources.GENERIC_SERVERS_PATH);
//                server = new ServerDO();
//            } else if (serverType.equals("JMXServer")) {
//                severDetailCollection = (Collection) registry.get(BAMRegistryResources.JMX_SERVERS_PATH);
//                server = new JMXServerDO();
//            } else {
//                throw new BAMException("can not get " + serverType + " type of server details ");
//            }
//
//            if (severDetailCollection != null) {
//                if (severDetailCollection.getChildCount() > 0) {
//                    String[] children = severDetailCollection.getChildren();
//                    for (String child : children) {
//                        Collection regServer = (Collection) registry.get(child);
//                        if (serverURL.equals(regServer.getProperty(BAMRegistryResources.SERVER_URL_PROPERTY))) {
//                            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
//                            if (regServer.getProperty(BAMRegistryResources.PASSWORD_PROPERTY) != null) {
//                                try {
//                                    byte[] passwordBytes = cryptoUtil.
//                                            base64DecodeAndDecrypt(regServer.getProperty(BAMRegistryResources.PASSWORD_PROPERTY));
//                                    server.setPassword(new String(passwordBytes));
//                                    server.setUserName(regServer.getProperty(BAMRegistryResources.USERNAME_PROPERTY));
//                                } catch (CryptoException e) {
//                                    throw new BAMException("server credential getting failed", e);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//        } catch (RegistryException e) {
//            e.printStackTrace();
//        }
//        return server;
//    }
//
//
//    public static List<ServerDO> getMonitoredServers(Registry registry, int tenantID)
//            throws BAMException {
//        // if (BAMUtil.getServersListCache() == null) {
//        // MonitoredServerListCache cache = BAMUtil.initServerListCache();
//        List<ServerDO> monitoredServers = new ArrayList<ServerDO>();
//        Collection serverCollection = null;
//        try {
//            if (registry.resourceExists(BAMRegistryResources.PULL_SERVERS_PATH)) {
//                serverCollection = (Collection) registry.get(BAMRegistryResources.PULL_SERVERS_PATH);
//            }
//            if (serverCollection != null && serverCollection.getChildCount() > 0) {
//                String[] children = serverCollection.getChildren();
//                for (String child : children) {
//
//                    String serverString = child.substring(child.lastIndexOf('/') + 1);
//                    int serverID = Integer.parseInt(serverString);
//                    Collection regServer = (Collection) registry.get(child);
//                    int regServerTenantID = Integer.parseInt(regServer.getProperty(BAMRegistryResources.TENANT_ID_PROPERTY));
//                    PullServerDO server = new PullServerDO();
//                    server.setId(serverID);
//                    server.setServerURL(regServer.getProperty(BAMRegistryResources.SERVER_URL_PROPERTY));
//
//                    server.setServerType(regServer.getProperty(BAMRegistryResources.SERVER_TYPE));
//                    CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
//                    if (regServer.getProperty(BAMRegistryResources.PASSWORD_PROPERTY) != null) {
//                        try {
//                            byte[] passwordBytes = cryptoUtil.
//                                    base64DecodeAndDecrypt(regServer.getProperty(BAMRegistryResources.PASSWORD_PROPERTY));
//                            String value = new String(passwordBytes);
//                            server.setPassword(value);
//                            server.setUserName(regServer.getProperty(BAMRegistryResources.USERNAME_PROPERTY));
//
//                        } catch (CryptoException e) {
//                            throw new BAMException("server credential getting failed", e);
//                        }
//                    }
//
//                    server.setTenantID(regServerTenantID);
//                    String statType = regServer.getProperty(BAMRegistryResources.CATEGORY_PROPERTY);
//
//                    if (statType != null) {
//                        server.setCategory(Integer.parseInt(statType));
//                    }
//
//                    server.setDescription(regServer.getProperty(BAMRegistryResources.DESCRIPTION_PROPERTY));
//
//                    if ("true".equals(regServer.getProperty(BAMRegistryResources.ACTIVE_PROPERTY))) {
//                        server.setActive("1");
//
//                    } else {
//                        server.setActive("0");
//                    }
////                    server.setPollingInterval(Long.parseLong(regServer.getProperty(BAMRegistryResources.POLLING_INTERVAL_PROPERTY)));
//
//                    BAMUtil.getServersListCache().addServer(server);
//                    if ((tenantID == -1) || (regServerTenantID == tenantID)) {
//                        monitoredServers.add(server);
//                    }
//                }
//            }
//        } catch (ResourceNotFoundException e) {
//            // No pull servers found!
//        } catch (RegistryException e) {
//            String msg = "Error talking to registry";
//            log.debug(msg);
//            throw new BAMException(msg, e);
//        }
//
//        try {
//            if (registry.resourceExists(BAMRegistryResources.EVENTING_SERVERS_PATH)) {
//                serverCollection = (Collection) registry.get(BAMRegistryResources.EVENTING_SERVERS_PATH);
//            }
//            if (serverCollection != null && serverCollection.getChildCount() > 0) {
//                String[] children = serverCollection.getChildren();
//
//                for (String child : children) {
//
//                    String serverString = child.substring(child.lastIndexOf('/') + 1);
//                    int serverID = Integer.parseInt(serverString);
//                    Collection regServer = (Collection) registry.get(child);
//                    int regServerTenantID = Integer.parseInt(regServer.getProperty(BAMRegistryResources.TENANT_ID_PROPERTY));
//
//                    EventingServerDO server = new EventingServerDO();
//                    server.setId(serverID);
//                    server.setServerURL(regServer.getProperty(BAMRegistryResources.SERVER_URL_PROPERTY));
//                    server.setServerType(regServer.getProperty(BAMRegistryResources.SERVER_TYPE));
//                    CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
//
//                    if (regServer.getProperty(BAMRegistryResources.PASSWORD_PROPERTY) != null) {
//                        try {
//                            byte[] passwordBytes = cryptoUtil.base64DecodeAndDecrypt(regServer.getProperty(BAMRegistryResources.PASSWORD_PROPERTY));
//                            String value = new String(passwordBytes);
//                            server.setPassword(value);
//                            server.setUserName(regServer.getProperty(BAMRegistryResources.USERNAME_PROPERTY));
//
//                        } catch (CryptoException e) {
//                            throw new BAMException("server credential getting failed", e);
//                        }
//                    }
//                    server.setTenantID(regServerTenantID);
//                    String statType = regServer.getProperty(BAMRegistryResources.CATEGORY_PROPERTY);
//                    if (statType != null) {
//                        server.setCategory(Integer.parseInt(statType));
//                    }
//                    server.setDescription(regServer.getProperty(BAMRegistryResources.DESCRIPTION_PROPERTY));
//                    if ("true".equals(regServer.getProperty(BAMRegistryResources.ACTIVE_PROPERTY))) {
//                        server.setActive("1");
//
//                    } else {
//                        server.setActive("0");
//                    }
//                    server.setSubscriptionID(regServer.getProperty(BAMRegistryResources.SUBSCRIPTION_ID_PROPERTY));
//                    // server.setDataRetention(TimeRange.parseTimeRange(regServer.getProperty(DATA_RETENTION_PROPERTY)));
//                    // server.setSummaryInterval(TimeRange.parseTimeRange(regServer.getProperty(SUMMARY_INTERVAL_PROPERTY)));
//                    if ((tenantID == -1) || (regServerTenantID == tenantID)) {
//                        monitoredServers.add(server);
//                    }
//                    BAMUtil.getServersListCache().addServer(server);
//                }
//            }
//        } catch (ResourceNotFoundException e) {
//            // No eventing servers found!
//        } catch (RegistryException e) {
//            String msg = "Error talking to registry";
//            log.debug(msg);
//            throw new BAMException(msg, e);
//        }
//
//        try {
//
//            if (registry.resourceExists(BAMRegistryResources.GENERIC_SERVERS_PATH)) {
//                serverCollection = (Collection) registry.get(BAMRegistryResources.GENERIC_SERVERS_PATH);
//            }
//            if (serverCollection != null && serverCollection.getChildCount() > 0) {
//                String[] children = serverCollection.getChildren();
//
//                for (String child : children) {
//                    String serverString = child.substring(child.lastIndexOf('/') + 1);
//                    int serverID = Integer.parseInt(serverString);
//
//                    Collection regServer = (Collection) registry.get(child);
//                    int regServerTenantID = Integer.parseInt(regServer.getProperty(BAMRegistryResources.TENANT_ID_PROPERTY));
//                    ServerDO server = new ServerDO();
//                    server.setId(serverID);
//                    server.setServerURL(regServer.getProperty(BAMRegistryResources.SERVER_URL_PROPERTY));
//                    server.setServerType(regServer.getProperty(BAMRegistryResources.SERVER_TYPE));
//
//                    CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
//
//                    if (regServer.getProperty(BAMRegistryResources.PASSWORD_PROPERTY) != null) {
//                        try {
//                            byte[] passwordBytes = cryptoUtil.base64DecodeAndDecrypt(regServer
//                                    .getProperty(BAMRegistryResources.PASSWORD_PROPERTY));
//                            String value = new String(passwordBytes);
//                            server.setPassword(value);
//                            server.setUserName(regServer.getProperty(BAMRegistryResources.USERNAME_PROPERTY));
//
//                        } catch (CryptoException e) {
//                            throw new BAMException("server credential getting failed", e);
//                        }
//                    }
//                    server.setTenantID(regServerTenantID);
//                    String statType = regServer.getProperty(BAMRegistryResources.CATEGORY_PROPERTY);
//
//                    if (statType != null) {
//                        server.setCategory(Integer.parseInt(statType));
//                    }
//                    server.setDescription(regServer.getProperty(BAMRegistryResources.DESCRIPTION_PROPERTY));
//                    if ("true".equals(regServer.getProperty(BAMRegistryResources.ACTIVE_PROPERTY))) {
//                        server.setActive("1");
//
//                    } else {
//                        server.setActive("0");
//                    }                    // server.setDataRetention(TimeRange.parseTimeRange(regServer.getProperty(DATA_RETENTION_PROPERTY)));
//                    // server.setSummaryInterval(TimeRange.parseTimeRange(regServer.getProperty(SUMMARY_INTERVAL_PROPERTY)));
//                    if ((tenantID == -1) || (regServerTenantID == tenantID)) {
//                        monitoredServers.add(server);
//                    }
//                    BAMUtil.getServersListCache().addServer(server);
//                }
//            }
//        } catch (ResourceNotFoundException e) {
//            // No eventing servers found!
//        } catch (RegistryException e) {
//            String msg = "Error talking to registry";
//            log.debug(msg);
//            throw new BAMException(msg, e);
//        }
//        try {
//
//            if (registry.resourceExists(BAMRegistryResources.JMX_SERVERS_PATH)) {
//                serverCollection = (Collection) registry.get(BAMRegistryResources.JMX_SERVERS_PATH);
//            }
//            if (serverCollection != null && serverCollection.getChildCount() > 0) {
//
//                String[] children = serverCollection.getChildren();
//                for (String child : children) {
//
//                    String serverString = child.substring(child.lastIndexOf('/') + 1);
//                    int serverID = Integer.parseInt(serverString);
//
//                    Collection regServer = (Collection) registry.get(child);
//                    int regServerTenantID = Integer.parseInt(regServer
//                            .getProperty(BAMRegistryResources.TENANT_ID_PROPERTY));
//                    JMXServerDO server = new JMXServerDO();
//                    server.setId(serverID);
//                    server.setServerURL(regServer.getProperty(BAMRegistryResources.SERVER_URL_PROPERTY));
//                    server.setServerType(regServer.getProperty(BAMRegistryResources.SERVER_TYPE));
//
//                    CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
//                    if (regServer.getProperty(BAMRegistryResources.PASSWORD_PROPERTY) != null) {
//                        try {
//                            byte[] passwordBytes = cryptoUtil.base64DecodeAndDecrypt(regServer
//                                    .getProperty(BAMRegistryResources.PASSWORD_PROPERTY));
//                            String value = new String(passwordBytes);
//                            server.setPassword(value);
//                            server.setUserName(regServer.getProperty(BAMRegistryResources.USERNAME_PROPERTY));
//
//                        } catch (CryptoException e) {
//                            throw new BAMException("server credential getting failed", e);
//                        }
//                    }
//                    server.setTenantID(regServerTenantID);
//                    String statType = regServer.getProperty(BAMRegistryResources.CATEGORY_PROPERTY);
//
//                    if (statType != null) {
//                        server.setCategory(Integer.parseInt(statType));
//                    }
//
//                    server.setDescription(regServer.getProperty(BAMRegistryResources.DESCRIPTION_PROPERTY));
//                    if ("true".equals(regServer.getProperty(BAMRegistryResources.ACTIVE_PROPERTY))) {
//                        server.setActive("1");
//
//                    } else {
//                        server.setActive("0");
//                    }
//                    if ((tenantID == -1) || (regServerTenantID == tenantID)) {
//                        monitoredServers.add(server);
//                    }
//
//                    BAMUtil.getServersListCache().addServer(server);
//                }
//            }
//        } catch (ResourceNotFoundException e) {
//            // No JMX servers found!
//        } catch (RegistryException e) {
//            String msg = "Error talking to registry";
//            log.debug(msg);
//            throw new BAMException(msg, e);
//        }
//
//        return monitoredServers;
//    }
//
//    public static ServerDO populateServerCredentials(Registry registry, ServerDO server)
//            throws BAMException {
//        try {
//            String serverPath = getRegistryMonitoredServerPath(server);
//            Collection serverCollection = null;
//            if (registry.resourceExists(serverPath)) {
//                serverCollection = (Collection) registry.get(serverPath);
//            }
//
//            if (serverCollection != null) {
//                CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
//                if (serverCollection.getProperty(BAMRegistryResources.PASSWORD_PROPERTY) != null) {
//                    try {
//                        byte[] passwordBytes = cryptoUtil.
//                                base64DecodeAndDecrypt(serverCollection.
//                                        getProperty(BAMRegistryResources.PASSWORD_PROPERTY));
//                        String value = new String(passwordBytes);
//                        server.setPassword(value);
//                        server.setUserName(serverCollection.
//                                getProperty(BAMRegistryResources.USERNAME_PROPERTY));
//
//                    } catch (CryptoException e) {
//                        throw new BAMException("server credential getting failed", e);
//                    }
//                }
//            }
//        } catch (RegistryException e) {
//            log.error("Could not update the server [" + server.getId() + "] in the Registry - " +
//                      e.getMessage());
//            throw new BAMException("Could not update the server [" + server.getId() +
//                                   "] in the Registry - ", e);
//        }
//
//        return server;
//    }
////
////    public ServerDO getMonitoredServer(Registry registry, int serverID) throws BAMException {
////        getMonitoredServers(registry, -1);
////        return BAMUtil.getServersListCache().getServer(serverID);
////    }
////
////    public static ServerDO getMonitoredServer(Registry registry, String serverURL, int tenantID)
////            throws BAMException {
////        // TODO
////        throw new BAMException("Not implemented");
////    }
//
//    private static String getRegistryMonitoredServerPath(ServerDO server) {
//        String path;
//
//        if (server.getServerType() != null) {
//            if (server.getServerType().equals(BAMConstants.SERVER_TYPE_PULL)) {
//                path = BAMRegistryResources.PULL_SERVERS_PATH + server.getId();
//            } else if (server.getServerType().equals(BAMConstants.SERVER_TYPE_EVENTING)) {
//                path = BAMRegistryResources.EVENTING_SERVERS_PATH + server.getId();
//            } else {
//                path = BAMRegistryResources.GENERIC_SERVERS_PATH + server.getId();
//            }
//        } else { // If server type is not present we default it to pull server
//             path = BAMRegistryResources.PULL_SERVERS_PATH + server.getId();
//        }
//
//        return path;
//    }
}
