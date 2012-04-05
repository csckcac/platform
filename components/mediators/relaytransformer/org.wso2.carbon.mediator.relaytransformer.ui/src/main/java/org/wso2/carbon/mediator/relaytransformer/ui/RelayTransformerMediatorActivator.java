package org.wso2.carbon.mediator.relaytransformer.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.mediator.service.MediatorService;

import java.util.Properties;

public class RelayTransformerMediatorActivator implements BundleActivator {
    private static final Log log = LogFactory.getLog(RelayTransformerMediatorActivator.class);

    public void start(BundleContext bundleContext) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Starting the RelayTransformer mediator component ...");
        }

        Properties props = new Properties();
        bundleContext.registerService(
                RelayTransformerMediatorService.class.getName(),
                new RelayTransformerMediatorService(), props);

        if (log.isDebugEnabled()) {
            log.debug("Successfully registered the RelayTransformer mediator service");
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Stopped the RelayTransformer mediator component ...");
        }
    }
}
