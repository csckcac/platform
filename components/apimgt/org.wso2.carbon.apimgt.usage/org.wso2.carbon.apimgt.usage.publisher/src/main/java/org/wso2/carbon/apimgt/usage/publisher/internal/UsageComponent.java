package org.wso2.carbon.apimgt.usage.publisher.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.usage.publisher.service.APIMGTConfigReaderService;

/**
 * @scr.component name="api.mgt.usage.component" immediate="true"
 * */
public class UsageComponent {

    private static Log log = LogFactory.getLog(UsageComponent.class);
    public static APIMGTConfigReaderService apimgtConfigReaderService;


    protected void activate(ComponentContext ctx) {
        try {
            apimgtConfigReaderService = new APIMGTConfigReaderService();
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
}
