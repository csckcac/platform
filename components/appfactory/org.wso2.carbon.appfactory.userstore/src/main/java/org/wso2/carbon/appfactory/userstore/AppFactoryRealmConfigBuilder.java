package org.wso2.carbon.appfactory.userstore;

import java.util.Map;

import org.apache.axiom.util.UIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.TenantMgtConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.config.multitenancy.MultiTenantRealmConfigBuilder;
import org.wso2.carbon.user.core.ldap.LDAPConstants;
import org.wso2.carbon.user.core.tenant.Tenant;

public class AppFactoryRealmConfigBuilder implements MultiTenantRealmConfigBuilder  {

    private static Log logger = LogFactory.getLog(AppFactoryRealmConfigBuilder.class);
    
    @Override
    public RealmConfiguration getRealmConfigForTenantToCreateRealm(RealmConfiguration bootStrapConfig, 
                                                                   RealmConfiguration persistedConfig, 
                                                                   int tenantId) throws UserStoreException {
        return persistedConfig;
    }

    @Override
    public RealmConfiguration getRealmConfigForTenantToPersist(RealmConfiguration bootStrapConfig,
                                                               TenantMgtConfiguration tenantMgtConfig, Tenant tenantInfo,
                                                               int tenantId) throws UserStoreException {

        try {
            RealmConfiguration ldapRealmConfig = bootStrapConfig
                    .cloneRealmConfiguration();
            ldapRealmConfig.setAdminPassword(UIDGenerator.generateUID());
            ldapRealmConfig.setAdminUserName(tenantInfo.getAdminName());
            ldapRealmConfig.setTenantId(tenantId);

            Map<String, String> authz = ldapRealmConfig.getAuthzProperties();
            authz.put(
                    UserCoreConstants.RealmConfig.PROPERTY_ADMINROLE_AUTHORIZATION,
                    CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION);

            Map<String, String> userStoreProperties = ldapRealmConfig
                    .getUserStoreProperties();

            String partitionDN = tenantMgtConfig
                    .getTenantStoreProperties()
                    .get(UserCoreConstants.TenantMgtConfig.PROPERTY_ROOT_PARTITION);
            String organizationName = tenantInfo.getDomain();
            // eg: o=cse.rog
            String organizationRDN = tenantMgtConfig
                    .getTenantStoreProperties()
                    .get(UserCoreConstants.TenantMgtConfig.PROPERTY_ORGANIZATIONAL_ATTRIBUTE)
                    + "=" + organizationName;
            // eg: ou=users
            String orgSubContextAttribute = tenantMgtConfig
                    .getTenantStoreProperties()
                    .get(UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_ATTRIBUTE);
            
            // if read ldap group is enabled, set the tenant specific group
            // search base
            if (("true").equals(bootStrapConfig
                    .getUserStoreProperty(LDAPConstants.READ_LDAP_GROUPS))) {
                // eg: ou=groups
                String groupContextRDN = orgSubContextAttribute + "="
                        + LDAPConstants.GROUP_CONTEXT_NAME;
                // eg: ou=users,o=cse.org, dc=cloud, dc=com
                String groupSearchBase = groupContextRDN + ","
                        + organizationRDN + "," + partitionDN;

                userStoreProperties.put(LDAPConstants.GROUP_SEARCH_BASE,
                        groupSearchBase);
            }

            return ldapRealmConfig;

        } catch (Exception e) {
            String errorMessage = "Error while building tenant specific Realm Configuration.";
            logger.error(errorMessage, e);
            throw new UserStoreException(errorMessage, e);
        }
    }

    @Override
    public RealmConfiguration getRealmConfigForTenantToCreateRealmOnTenantCreation(
                              RealmConfiguration bootStrapConfig, 
                              RealmConfiguration persistedConfig, int tenantId) throws UserStoreException {
        return persistedConfig;
    }
    
    

}
