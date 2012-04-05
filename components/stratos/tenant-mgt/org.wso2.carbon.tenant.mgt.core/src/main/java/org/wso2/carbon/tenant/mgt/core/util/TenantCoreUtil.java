package org.wso2.carbon.tenant.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.AuthenticationObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.stratos.common.constants.StratosConstants;
import org.wso2.carbon.tenant.mgt.core.internal.TenantMgtCoreServiceComponent;

/**
 * Tenant Core Util class - used by any service that needs to create a tenant.
 */
public class TenantCoreUtil {
    
    private static final Log log = LogFactory.getLog(TenantCoreUtil.class);

    /**
     * Check whether a tenant exist with the given tenantInfoBean and
     * TenantManager.
     * 
     * @param tenant tenant Information
     * @return true, if the chosen name is available to register
     * @throws Exception
     *             if unable to get the tenant id or if a tenant with same
     *             domain exists.
     */
    public static boolean isDomainNameAvailable(Tenant tenant) throws Exception {
        TenantManager tenantManager = TenantMgtCoreServiceComponent.getTenantManager();
        String tenantDomain = tenant.getDomain();
    
        // The registry reserved words are checked first.
        if (tenantDomain.equals("atom") || tenantDomain.equals("registry") ||
            tenantDomain.equals("resource")) {
            String msg = "You can not use a registry reserved word:" + tenantDomain +
                         ":as a tenant domain. Please choose a different one.";
            log.error(msg);
            throw new Exception(msg);
        }
    
        int tenantId;
        try {
            tenantId = tenantManager.getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            String msg = "Error in getting the tenant id for the given domain  " +
                         tenant.getDomain() + ".";
            log.error(msg);
            throw new Exception(msg, e);
        }
    
        // check a tenant with same domain exist.
        if (tenantId > 0 || tenant.getDomain().equals(MultitenantConstants.SUPER_TENANT_NAME)) {
            String msg =
                         "A tenant with same domain already exist. " +
                                 "Please use a different domain name. tenant domain: " +
                                 tenant.getDomain() + ".";
            log.info(msg);
            return false;
        }
        return true;
    }

    /**
     * Initializes the registry for the tenant.
     * 
     * @param tenantId
     *            tenant id.
     */
    public static void initializeRegistry(int tenantId) {
        BundleContext bundleContext = TenantMgtCoreServiceComponent.getBundleContext();
        if (bundleContext != null) {
            ServiceTracker tracker =
                                     new ServiceTracker(bundleContext,
                                                        AuthenticationObserver.class.getName(),
                                                        null);
            tracker.open();
            Object[] services = tracker.getServices();
            if (services != null) {
                for (Object service : services) {
                    ((AuthenticationObserver) service).startedAuthentication(tenantId);
                }
            }
            tracker.close();
        }
    }

    /**
     * Setting the Originated
     * @param tenantId - tenant Id
     * @param originatedService - The Service from where the tenant registration was originated.
     * @throws Exception, Registry Exception, if error in putting the originated Service resource
     * to the governance registry.
     */
    public static void setOriginatedService(int tenantId,
                                            String originatedService) throws Exception {
        if (originatedService != null) { 
            String originatedServicePath =
                                           StratosConstants.ORIGINATED_SERVICE_PATH +
                                                   StratosConstants.PATH_SEPARATOR +
                                                   StratosConstants.ORIGINATED_SERVICE +
                                                   StratosConstants.PATH_SEPARATOR + tenantId;
            try {
                Resource origServiceRes = TenantMgtCoreServiceComponent.
                        getGovernanceSystemRegistry(0).newResource();
                origServiceRes.setContent(originatedService);
                TenantMgtCoreServiceComponent.getGovernanceSystemRegistry(0).
                        put(originatedServicePath, origServiceRes);
            } catch (RegistryException e) {
                String msg = "Error in putting the originated service resource " +
                             "to the governance registry";
                log.error(msg, e);
                throw new RegistryException(msg, e);
            }
        }
    }

}
