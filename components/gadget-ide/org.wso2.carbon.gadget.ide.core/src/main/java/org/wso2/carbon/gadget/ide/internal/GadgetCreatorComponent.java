package org.wso2.carbon.gadget.ide.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.datasource.DataSourceInformationRepositoryService;
import org.wso2.carbon.gadget.ide.util.Utils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

/**
 * @scr.component name="gadgetide.component" immediate="true"
 * @scr.reference name="org.wso2.carbon.datasource.DataSourceInformationRepositoryService" interface="org.wso2.carbon.datasource.DataSourceInformationRepositoryService"
 * cardinality="0..1" policy="dynamic" bind="setCarbonDataSourceService" unbind="unsetCarbonDataSourceService"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */public class GadgetCreatorComponent {
    private static Log log = LogFactory.getLog(GadgetCreatorComponent.class);
    private static Object componentLock = new Object(); /* class level lock for controlling synchronized access to static variables */


    protected void setCarbonDataSourceService(
            DataSourceInformationRepositoryService dataSourceService) {
        synchronized (componentLock) {
            log.debug("Setting the Carbon Data Sources Service");
            Utils.setCarbonDataSourceService(dataSourceService);
        }
    }

    protected void unsetCarbonDataSourceService(
            DataSourceInformationRepositoryService dataSourceService) {
        synchronized (componentLock) {
            log.debug("Unsetting the Carbon Data Sources Service");
            Utils.setCarbonDataSourceService(null);
        }
    }


    protected void setRegistryService(RegistryService registryService) throws RegistryException {
        synchronized (componentLock) {
            if (log.isDebugEnabled()) {
                log.debug("Setting the Carbon RegistryService");
            }
            Utils.setRegistryService(registryService);
        }

    }

    protected void unsetRegistryService(RegistryService registryService) {
        synchronized (componentLock) {
            if (log.isDebugEnabled()) {
                log.debug("Unsetting the Carbon RegistryService");
            }
            Utils.setRegistryService(null);
        }
    }
}
