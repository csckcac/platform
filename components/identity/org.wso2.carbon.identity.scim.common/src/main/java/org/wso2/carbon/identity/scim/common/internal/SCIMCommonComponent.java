package org.wso2.carbon.identity.scim.common.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.scim.common.config.SCIMProvisioningConfigManager;
import org.wso2.carbon.identity.scim.common.listener.SCIMUserOperationListener;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.charon.core.config.SCIMConfig;
import org.wso2.charon.core.config.SCIMConfigConstants;
import org.wso2.charon.core.config.SCIMConfigProcessor;
import org.wso2.charon.core.exceptions.CharonException;

import java.io.File;

/**
 * @scr.component name="identity.scim.common" immediate="true"
 */
public class SCIMCommonComponent {
    private static Log logger = LogFactory.getLog(SCIMCommonComponent.class);

    protected void activate(ComponentContext ctx) {
        try {
            String filePath = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                              SCIMConfigConstants.PROVISIONING_CONFIG_NAME;

            SCIMConfigProcessor scimConfigProcessor = new SCIMConfigProcessor();
            SCIMConfig scimConfig = scimConfigProcessor.buildConfigFromFile(filePath);

            SCIMProvisioningConfigManager configManager = SCIMProvisioningConfigManager.getInstance();

            configManager.setSCIMConfig(scimConfig);

            //register UserOperationEventListener implementation
            SCIMUserOperationListener scimUserOperationListener = new SCIMUserOperationListener();
            ctx.getBundleContext().registerService(UserOperationEventListener.class.getName(),
                                                   scimUserOperationListener, null);
            if (logger.isDebugEnabled()) {
                logger.debug("SCIM Common component activated successfully.");
            }
        }
        catch (CharonException error) {
            logger.error(error);
        }
    }
}