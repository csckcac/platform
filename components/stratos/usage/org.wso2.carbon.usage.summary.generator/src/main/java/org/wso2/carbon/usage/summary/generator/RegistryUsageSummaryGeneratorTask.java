/**
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.usage.summary.generator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.usage.summary.generator.client.UsageSummaryGeneratorClient;

import java.util.TimerTask;

/**
 * Runs timer task to go through reg_content and reg_content_history table and generate the registry
 * bandwidth usage
 *
 */
public class RegistryUsageSummaryGeneratorTask extends TimerTask {
    private UsageSummaryGeneratorClient client;
    private RealmService realmService;
    private static final Log log = LogFactory.getLog(RegistryUsageSummaryGeneratorTask.class);

    public RegistryUsageSummaryGeneratorTask(
            UsageSummaryGeneratorClient client, RealmService realmService){
        this.client = client;
        this.realmService = realmService;
    }
    
    @Override
    public void run() {
        TenantManager tenantManager = realmService.getTenantManager();
        Tenant[] tenants;
        try {
            tenants = (Tenant[]) tenantManager.getAllTenants();
            for (Tenant tenant : tenants) {
                //Run Daily generator 
                RegistryUsageSummaryGenerator generator = new RegistryUsageSummaryGenerator(
                        tenant, BAMCalendar.DAY_OF_MONTH, client);
                try{
                    generator.generateSummary();
                }catch (BAMException e) {
                    String msg = "Unable to generate Daily Registry Usage Summary for tenant "
                            + tenant.getDomain();
                    log.error(msg, e);
                }
                
              //Run Monthly generator 
                generator = new RegistryUsageSummaryGenerator(tenant, BAMCalendar.MONTH, client);
                try{
                    generator.generateSummary();
                }catch (BAMException e) {
                    String msg = "Unable to generate Monthly Registry Usage Summary for tenant "
                        + tenant.getDomain();
                    log.error(msg, e);
                }
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String msg = "Unable to get tenants from realmService. "
                    + "Failed to generate Registry Usage Summary";
            log.error(msg, e);
        } 
    }
}
