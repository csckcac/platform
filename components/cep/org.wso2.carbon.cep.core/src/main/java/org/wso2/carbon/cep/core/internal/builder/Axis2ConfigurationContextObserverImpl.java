package org.wso2.carbon.cep.core.internal.builder;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

public class Axis2ConfigurationContextObserverImpl extends AbstractAxis2ConfigurationContextObserver {
    private static Log log = LogFactory.getLog(Axis2ConfigurationContextObserverImpl.class);

    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        String tenantDomain = SuperTenantCarbonContext.getCurrentContext(
                configurationContext).getTenantDomain();
        log.info("Loading Buckets Specific to tenant when the tenant logged in");
        CEPServiceBuilder.loadBucketsFromRegistry(configurationContext.getAxisConfiguration());
    }

}
