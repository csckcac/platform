/*
 * Copyright 2012 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.bam.eventreceiver.service;

import org.wso2.carbon.bam.eventreceiver.BAMEventReceiverComponentManager;
import org.wso2.carbon.bam.eventreceiver.exception.ConfigurationException;
import org.wso2.carbon.bam.eventreceiver.internal.util.EventReceiverConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * BAM Event Receiver backend Cassandra cluster information manager.
 */
public class CassandraClusterMgtService extends AbstractAdmin{

    public void setClusterInformation(ClusterInformation clusterInformation) throws ConfigurationException {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        if(MultitenantConstants.SUPER_TENANT_ID == tenantId){
            try {
                UserRegistry configSystemRegistry = BAMEventReceiverComponentManager.getInstance().getRegistryService().
                        getConfigSystemRegistry(tenantId);

                String cassandraStreamDefHostpoolPath = EventReceiverConstants.CASSANDRA_STREAM_DEF_HOSTPOOL_PATH;
                Resource cassandraAuthResource;
                if (configSystemRegistry.resourceExists(cassandraStreamDefHostpoolPath)) {
                    cassandraAuthResource = configSystemRegistry.get(cassandraStreamDefHostpoolPath);
                } else {
                    cassandraAuthResource = configSystemRegistry.newResource();
                    configSystemRegistry.put(cassandraStreamDefHostpoolPath, cassandraAuthResource);
                    cassandraAuthResource = configSystemRegistry.get(cassandraStreamDefHostpoolPath);
                }

                cassandraAuthResource.setProperty(EventReceiverConstants.HOSTPOOL_PROPERTY,
                        clusterInformation.getHostPool().toString());

                configSystemRegistry.put(cassandraStreamDefHostpoolPath, cassandraAuthResource);

            } catch (RegistryException e) {
                throw new ConfigurationException("Failed to store connection parameters for tenant : " +
                        tenantId);
            }
        }

    }


    public ClusterInformation getClusterInformation(){
        return null;
    }
}
