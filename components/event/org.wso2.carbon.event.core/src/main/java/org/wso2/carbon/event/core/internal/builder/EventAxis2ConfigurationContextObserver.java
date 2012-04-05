package org.wso2.carbon.event.core.internal.builder;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

import java.util.ArrayList;
import java.util.List;

public class EventAxis2ConfigurationContextObserver extends AbstractAxis2ConfigurationContextObserver {
    private static Log log = LogFactory.getLog(EventAxis2ConfigurationContextObserver.class);

    private EventBroker eventBroker;

    private List<String> loadedTenants;

    public EventAxis2ConfigurationContextObserver() {
        this.loadedTenants = new ArrayList<String>();
    }

    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        String tenantDomain = SuperTenantCarbonContext.getCurrentContext(
                configurationContext).getTenantDomain();
        int tenantID = SuperTenantCarbonContext.getCurrentContext(configurationContext).getTenantId();
        try {
            SuperTenantCarbonContext.startTenantFlow();
            SuperTenantCarbonContext.getCurrentContext().setTenantId(tenantID);
            SuperTenantCarbonContext.getCurrentContext().getTenantDomain(true);

            if (!loadedTenants.contains(tenantDomain.trim())) {
                eventBroker.initializeTenant();
                loadedTenants.add(tenantDomain.trim());
            }

        } catch (Exception e) {
            log.error("Error in setting tenant information", e);
        } finally {
            SuperTenantCarbonContext.endTenantFlow();
        }

    }

    public void setEventBroker(EventBroker eventBroker) {
        this.eventBroker = eventBroker;
    }
}
