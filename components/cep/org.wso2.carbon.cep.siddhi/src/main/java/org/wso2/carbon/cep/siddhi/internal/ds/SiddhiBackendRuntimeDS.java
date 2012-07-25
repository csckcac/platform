/*
 * Copyright 2004,2012 The Apache Software Foundation.
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

package org.wso2.carbon.cep.siddhi.internal.ds;

import me.prettyprint.hector.api.Cluster;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.cep.core.CEPServiceInterface;
import org.wso2.carbon.cep.core.backend.CEPEngineProvider;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.siddhi.backend.SiddhiBackEndRuntimeFactory;
import org.wso2.carbon.cep.siddhi.persistence.CasandraPersistenceStore;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.siddhi.core.persistence.PersistenceStore;

import java.util.ArrayList;
import java.util.List;

/**
 * @scr.component name="siddhibackend.component" immediate="true"
 * @scr.reference name="cep.service"
 * interface="org.wso2.carbon.cep.core.CEPServiceInterface" cardinality="1..1"
 * policy="dynamic" bind="setCEPService" unbind="unSetCEPService"
 * @scr.reference name="user.realm.delegating" interface="org.wso2.carbon.user.core.UserRealm"
 * cardinality="1..1" policy="dynamic" bind="setUserRealm" unbind="unsetUserRealm"
 * @scr.reference name="dataaccess.service" interface="org.wso2.carbon.cassandra.dataaccess.DataAccessService"
 * cardinality="1..1" policy="dynamic" bind="setDataAccessService" unbind="unsetDataAccessService"
 */

public class SiddhiBackendRuntimeDS {

    private static final Log log = LogFactory.getLog(SiddhiBackendRuntimeDS.class);

    private PersistenceStore casandraPersistenceStore = null;
    private UserRealm userRealm;
    private DataAccessService dataAccessService;
    private String clusterName = null;

    protected void activate(ComponentContext context) {
        if (SiddhiBackendRuntimeValueHolder.getInstance().getCEPEngineProvider() == null) {
            // registers with the cep service
            try {
                CEPEngineProvider cepEngineProvider = new CEPEngineProvider();
                cepEngineProvider.setName("SiddhiCEPRuntime");
                cepEngineProvider.setProviderClass(SiddhiBackEndRuntimeFactory.class);
                SiddhiBackendRuntimeValueHolder.getInstance().setCEPEngineProvider(cepEngineProvider);
                String adminPassword = userRealm.getRealmConfiguration().getAdminPassword();
                String adminUserName = userRealm.getRealmConfiguration().getAdminUserName();
//           int tenantId =userRealm.getRealmConfiguration().getTenantId();
                List<String> configPropertyNames=new ArrayList<String>();
                configPropertyNames.add(SiddhiBackEndRuntimeFactory.PERSISTENCE_SNAPSHOT_TIME_INTERVAL_MINUTES);
                cepEngineProvider.setConfigurationPropertyNames(configPropertyNames);

                ClusterInformation clusterInformation = new ClusterInformation(adminUserName,
                                                                               adminPassword);
                clusterInformation.setClusterName("SiddhiPersistenceCluster");
                Cluster cluster = dataAccessService.getCluster(clusterInformation);
                clusterName = cluster.getName();
                casandraPersistenceStore = new CasandraPersistenceStore(cluster);
                SiddhiBackendRuntimeValueHolder.getInstance().setPersistenceStore(casandraPersistenceStore);

                SiddhiBackendRuntimeValueHolder.getInstance().getCEPService()
                        .registerCEPEngineProvider(cepEngineProvider);
            } catch (CEPConfigurationException e) {
                log.error("Can not register Siddhi back end runtime with the cep service ");
            } catch (UserStoreException e) {
                log.error("Error in accessing user store ", e);
            } catch (Throwable e){
                log.error("Error in registering Siddhi back end runtime with the cep service ",e);
            }
        }

    }

    protected void deactivate(ComponentContext context) {
        if (dataAccessService != null && clusterName != null) {
            dataAccessService.destroyCluster(clusterName);
            clusterName = null;
        }
    }

    protected void setCEPService(CEPServiceInterface cepService) {
        SiddhiBackendRuntimeValueHolder.getInstance().registerCEPService(cepService);
    }

    protected void unSetCEPService(CEPServiceInterface cepService) {

    }

    protected void setUserRealm(UserRealm userRealm) {
        this.userRealm = userRealm;
    }

    protected void unsetUserRealm(UserRealm userRealm) {
        this.userRealm = null;

    }

    protected void setDataAccessService(DataAccessService dataAccessService) {
        this.dataAccessService = dataAccessService;
    }

    protected void unsetDataAccessService(DataAccessService dataAccessService) {
        this.dataAccessService = null;
    }


}
