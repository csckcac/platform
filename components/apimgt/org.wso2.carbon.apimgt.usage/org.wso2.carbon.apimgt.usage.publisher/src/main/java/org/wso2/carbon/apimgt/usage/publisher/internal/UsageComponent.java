package org.wso2.carbon.apimgt.usage.publisher.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.usage.publisher.service.APIMGTConfigReaderService;

/**
 * @scr.component name="api.mgt.usage.component" immediate="true"
 * @scr.reference name="api.manager.config.service"
 * interface="org.wso2.carbon.apimgt.impl.APIManagerConfigurationService" cardinality="1..1"
 * policy="dynamic" bind="setAPIManagerConfigurationService" unbind="unsetAPIManagerConfigurationService"
 */
public class UsageComponent {

    private static final Log log = LogFactory.getLog(UsageComponent.class);

    private static APIMGTConfigReaderService apimgtConfigReaderService;
    private static APIManagerConfigurationService amConfigService;

    protected void activate(ComponentContext ctx) {
        try {
            apimgtConfigReaderService = new APIMGTConfigReaderService(amConfigService.getAPIManagerConfiguration());
            BundleContext bundleContext = ctx.getBundleContext();
            bundleContext.registerService(APIMGTConfigReaderService.class.getName(),
                                          apimgtConfigReaderService, null);
            log.debug("API Management Usage Publisher bundle is activated ");
        } catch (Throwable e) {
            log.error("API Management Usage Publisher bundle ", e);
        }
    }

    protected void deactivate(ComponentContext ctx) {

    }

    protected void setAPIManagerConfigurationService(APIManagerConfigurationService service) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service bound to the API usage handler");
        }
        amConfigService = service;
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService service) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service unbound from the API usage handler");
        }
        amConfigService = null;
    }

    public static APIMGTConfigReaderService getApiMgtConfigReaderService() {
        return apimgtConfigReaderService;
    }
}
