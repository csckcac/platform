/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bam.core.util;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.bam.common.clients.*;
import org.wso2.carbon.bam.core.cache.CacheData;
import org.wso2.carbon.bam.core.clients.BAMArchiverDSClient;
import org.wso2.carbon.bam.util.BAMConstants;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.event.client.broker.BrokerClient;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.NetworkUtils;

import java.net.SocketException;
import java.util.HashMap;

/**
 * Miscellaneous utilities.
 */
public class BAMUtil {
    //private static Log log = LogFactory.getLog(BAMUtil.class);
    private static Registry registry;
    private static ConfigurationContextService configurationContextService;
    private static RealmService realmService;

    private static HashMap<String,CacheData> bamCache;


    public static Registry getRegistry() {
        return registry;
    }

    public static void setRegistry(Registry registry) {
        BAMUtil.registry = registry;
    }

    public static String getBackendServerURLHTTPS() throws BAMException {
        //   return "local:/";
        try {
            return "https://" + NetworkUtils.getLocalHostname() + ":" +
                    CarbonUtils.getTransportPort(BAMUtil.getConfigurationContextService(), "https") +
                    BAMUtil.configurationContextService.getServerConfigContext().getContextRoot();
        } catch (SocketException e) {
            throw new BAMException("Cannot get back end https url" , e);
        }
    }

    public static String getBackendServerURLHTTP() throws SocketException {
        return "http://" + NetworkUtils.getLocalHostname() + ":" +
                CarbonUtils.getTransportPort(BAMUtil.getConfigurationContextService(), "http") +
                BAMUtil.configurationContextService.getServerConfigContext().getContextRoot();
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        BAMUtil.configurationContextService = configurationContextService;
    }

    public static void initBAMCache() {
        if (bamCache == null) {
            synchronized (BAMUtil.class){
                bamCache = new HashMap<String, CacheData>();
            }
        }
    }

    public static HashMap<String,CacheData> getBAMCache() {
        return bamCache;
    }

    public static BrokerClient getBrokerClient(String brokerURL, String userName, String password)
            throws Exception {
        return new BrokerClient(BAMUtil.getConfigurationContextService().getServerConfigContext(),
                brokerURL, userName, password);
    }

    public static BrokerClient getBrokerClient(String brokerURL, String cookie)
            throws Exception {
        return new BrokerClient(BAMUtil.getConfigurationContextService().getServerConfigContext(),
                brokerURL, cookie);
    }

    public static String generateURL(String[] components) {
        StringBuilder builder = new StringBuilder();
        if (components.length > 0) {
            builder.append(components[0]);
        }
        for (int i = 1; i < components.length; i++) {
            builder.append("/");
            builder.append(components[i]);
        }
        return builder.toString();
    }

    private static BAMSummaryGenerationDSClient summaryGenerationDSClient = null;
    public static synchronized BAMSummaryGenerationDSClient getBAMSummaryGenerationDSClient() throws BAMException {
        if (summaryGenerationDSClient == null) {
//            if (CarbonUtils.isRunningOnLocalTransportMode()) {
            summaryGenerationDSClient = new BAMSummaryGenerationDSClient(BAMConstants.LOCAL_TRANSPORT,
                    getConfigurationContextService().getServerConfigContext());
//            } else {
//                summaryGenerationDSClient = new BAMSummaryGenerationDSClient(BAMConstants.LOCAL_TRANSPORT,
//                        getConfigurationContextService().getClientConfigContext());
//            }
        }
        return summaryGenerationDSClient;
    }

    private static SummaryDimensionDSClient summaryDimensionDSClient = null;

    public static synchronized SummaryDimensionDSClient getSummaryDimensionDSClient() throws BAMException{
        if (summaryDimensionDSClient == null) {
            summaryDimensionDSClient = new SummaryDimensionDSClient(BAMConstants.LOCAL_TRANSPORT,
                    getConfigurationContextService().getServerConfigContext());

        }
        return summaryDimensionDSClient;
    }

    private static BAMServiceSummaryDSClient serviceSummaryDSClient = null;
    public static synchronized  BAMServiceSummaryDSClient getBAMServiceSummaryDSClient() throws BAMException {
        if (serviceSummaryDSClient == null) {
            serviceSummaryDSClient = new BAMServiceSummaryDSClient(BAMConstants.LOCAL_TRANSPORT,
                    getConfigurationContextService().getServerConfigContext());
        }
        return serviceSummaryDSClient;
    }


    public static BAMConfigurationDSClient getBAMConfigurationDSClient() throws BAMException {
        return new BAMConfigurationDSClient(BAMConstants.LOCAL_TRANSPORT,
                getConfigurationContextService().getServerConfigContext());
    }

    public static BAMDataCollectionDSClient getBAMDataCollectionDSClient() throws BAMException {
        return new BAMDataCollectionDSClient(BAMConstants.LOCAL_TRANSPORT,
                getConfigurationContextService().getServerConfigContext());
    }

    public static BAMArchiverDSClient getArchiverDSClient() throws BAMException {
        return new BAMArchiverDSClient(BAMConstants.LOCAL_TRANSPORT,
                getConfigurationContextService().getServerConfigContext());
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static void setRealmService(RealmService realmService) {
        BAMUtil.realmService = realmService;
    }

    /**
     * returns the tenent ID from the given tenant domain. null tenant domain assume as the suppoer
     * tenant and hence returns 0.
     * @param tenantDomain
     * @return
     * @throws BAMException
     */

    public static int getTenantID(String tenantDomain) throws BAMException {

        int tenantID = CarbonConstants.SUPER_TENANT_ID;
        if (tenantDomain != null) {
            RealmService realmService = BAMUtil.getRealmService();
            try {
                tenantID = realmService.getTenantManager().getTenantId(tenantDomain);
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                throw new BAMException("Can not tenant manager");
            }
        }
        return tenantID;
    }

}
