package org.wso2.carbon.appfactory.project.mgt.core;

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

import java.util.Map;

/**
 *
 */
public class AppFactoryLDAPRealmBuilder implements MultiTenantRealmConfigBuilder {
    private static final Log logger = LogFactory.getLog(AppFactoryLDAPRealmBuilder.class);

    @Override
    public RealmConfiguration getRealmConfigForTenantToCreateRealm(
            RealmConfiguration
                    bootStrapConfig, RealmConfiguration persistedConfig, int tenantId)
            throws UserStoreException {
        return persistedConfig;
    }

    @Override
    public RealmConfiguration getRealmConfigForTenantToPersist(
            RealmConfiguration bootStrapConfig, TenantMgtConfiguration tenantMgtConfig,
            Tenant tenantInfo, int tenantId) throws UserStoreException {
        try {
            RealmConfiguration ldapRealmConfig = bootStrapConfig.cloneRealmConfiguration();
            ldapRealmConfig.setAdminPassword(UIDGenerator.generateUID());
            ldapRealmConfig.setAdminUserName(tenantInfo.getAdminName());
            ldapRealmConfig.setTenantId(tenantId);

            Map<String, String> authz = ldapRealmConfig.getAuthzProperties();
            authz.put(UserCoreConstants.RealmConfig.PROPERTY_ADMINROLE_AUTHORIZATION,
                      CarbonConstants.UI_ADMIN_PERMISSION_COLLECTION);

            Map<String, String> userStoreProperties = ldapRealmConfig.getUserStoreProperties();

            String partitionDN = tenantMgtConfig.getTenantStoreProperties().get(
                    UserCoreConstants.TenantMgtConfig.PROPERTY_ROOT_PARTITION);
            String organizationName = tenantInfo.getDomain();
            //eg: o=cse.rog
            String organizationRDN = tenantMgtConfig.getTenantStoreProperties().get(
                    UserCoreConstants.TenantMgtConfig.PROPERTY_ORGANIZATIONAL_ATTRIBUTE) + "=" +
                                     organizationName;
            //eg: ou=users
            String orgSubContextAttribute = tenantMgtConfig.getTenantStoreProperties().get(
                    UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_ATTRIBUTE);
            String userContextRDN = orgSubContextAttribute + "=" +
                                    LDAPConstants.USER_CONTEXT_NAME;
            //eg: ou=groups,o=cse.org, dc=cloud, dc=com
            String userSearchBase = userContextRDN + "," +
                                    partitionDN;
            //replace the tenant specific user search base.
            userStoreProperties.put(LDAPConstants.USER_SEARCH_BASE, bootStrapConfig.
                    getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE));

            //if read ldap group is enabled, set the tenant specific group search base
            if (("true").equals(bootStrapConfig.getUserStoreProperty
                    (LDAPConstants.READ_EXTERNAL_ROLES))) {
                //eg: ou=groups
                String groupContextRDN = orgSubContextAttribute + "=" +
                                         LDAPConstants.GROUP_CONTEXT_NAME;
                //eg: ou=users,o=cse.org, dc=cloud, dc=com
                String groupSearchBase = groupContextRDN + "," + organizationRDN + "," + partitionDN;

                userStoreProperties.put(LDAPConstants.GROUP_SEARCH_BASE, groupSearchBase);
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
            RealmConfiguration realmConfiguration, RealmConfiguration realmConfiguration1, int i)
            throws UserStoreException {
        return realmConfiguration;
    }
}
