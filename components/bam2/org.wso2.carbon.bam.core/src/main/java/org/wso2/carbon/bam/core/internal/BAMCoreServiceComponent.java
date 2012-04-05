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
package org.wso2.carbon.bam.core.internal;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bam.core.persistence.StoreFactory;
import org.wso2.carbon.bam.core.persistence.cassandra.CassandraStoreFactory;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="bam.utils.component" immediate="true"
 * @scr.reference name="cassandra.service" interface="org.wso2.carbon.cassandra.dataaccess.DataAccessService"
 * cardinality="1..1" policy="dynamic" bind="setDataAccessService" unbind="unsetDataAccessService"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"  unbind="unsetRealmService"
 * @scr.reference name="tenant.registryloader"
 * interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 * cardinality="1..1" policy="dynamic" bind="setTenantRegistryLoader"
 * unbind="unsetTenantRegistryLoader"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"
 * bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class BAMCoreServiceComponent {

    protected void activate(ComponentContext ctx) {

    }

    protected void setDataAccessService(DataAccessService dataAccessService) {
        //QueryUtils.setDataAccessService(dataAccessService);
    }

    protected void unsetDataAccessService(DataAccessService dataAccessService) {
        //QueryUtils.setDataAccessService(null);
    }

    protected static void setRealmService(RealmService realm) {
        ServiceHolder.setRealmService(realm);
    }

    protected static void unsetRealmService(RealmService realm) {
        ServiceHolder.setRealmService(null);
    }

    protected static RealmService getRealmService() {
        return ServiceHolder.getRealmService();
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        ServiceHolder.setConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        ServiceHolder.setConfigurationContextService(null);
    }

    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        ServiceHolder.setTenantRegistryLoader(tenantRegistryLoader);
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        ServiceHolder.setTenantRegistryLoader(null);
        // TODO: Add debug logs for each set and unset methods.
/*        if (log.isDebugEnabled()) {
            log.debug("TenantRegistryLoader unset in BAM bundle");
        }*/
    }

    protected void setRegistryService(RegistryService registryService) throws
                                                                       RegistryException {
        ServiceHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        ServiceHolder.setRegistryService(null);
    }

}
