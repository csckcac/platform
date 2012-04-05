package org.wso2.carbon.dashboard;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.services.utils.GetDownloadContentUtil;
import org.wso2.carbon.registry.resource.beans.ContentDownloadBean;
import org.wso2.carbon.registry.app.Utils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * The service pick gadget specs from the registry and returns them to the FE
 */
public class GadgetContentDownloadService extends AbstractAdmin {
    public Registry getRootRegistry() throws Exception {
        if (getHttpSession() != null) {
            Registry registry =
                    (Registry) getHttpSession().getAttribute(
                            RegistryConstants.ROOT_REGISTRY_INSTANCE);
            if (registry != null) {
                return registry;
            } else {
                registry = Utils.getEmbeddedRegistryService().getRegistry(
                        CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME, RegistryContext.getBaseInstance().getRealmService().
                                getTenantManager().getTenantId(getTenantDomain()));
                return registry;
            }
        }
        return null;
    }

    public ContentDownloadBean getContentDownloadBean(String path) throws Exception {
        UserRegistry userRegistry = (UserRegistry) getRootRegistry();
        return GetDownloadContentUtil.getContentDownloadBean(path, userRegistry);
    }
}
