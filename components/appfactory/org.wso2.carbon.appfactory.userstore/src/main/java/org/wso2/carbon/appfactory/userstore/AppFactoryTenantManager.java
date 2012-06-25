/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.appfactory.userstore;

import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.sql.DataSource;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.api.TenantMgtConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.ldap.LDAPConnectionContext;
import org.wso2.carbon.user.core.ldap.LDAPConstants;
import org.wso2.carbon.user.core.tenant.CommonHybridLDAPTenantManager;

/**
 * This class is the tenant manager for any external LDAP and based on the "ou" partitioning
 * per tenant under one DIT.
 */
public class AppFactoryTenantManager extends CommonHybridLDAPTenantManager {
    private static Log logger = LogFactory.getLog(AppFactoryTenantManager.class);
    private LDAPConnectionContext ldapConnectionSource;

    private TenantMgtConfiguration tenantMgtConfig = null;
    private RealmConfiguration realmConfig = null;

    public AppFactoryTenantManager(OMElement omElement, Map<String, Object> properties)
            throws Exception {
        super(omElement, properties);

        this.ldapConnectionSource = (LDAPConnectionContext) properties.get(
                UserCoreConstants.LDAP_CONNECTION_SOURCE);

        if (ldapConnectionSource == null) {
            throw new UserStoreException("LDAP connection context is not set in properties with key - "
                    + UserCoreConstants.LDAP_CONNECTION_SOURCE);
        }

        tenantMgtConfig = (TenantMgtConfiguration) properties.get(
                UserCoreConstants.TENANT_MGT_CONFIGURATION);

        realmConfig = (RealmConfiguration) properties.get(UserCoreConstants.REALM_CONFIGURATION);
    }

    public AppFactoryTenantManager(DataSource dataSource, String superTenantDomain) {
        super(dataSource, superTenantDomain);
    }

    /**
     * Create a space for tenant in LDAP.
     *
     * @param orgName  Organization name.
     * @param tenant The tenant
     * @param initialDirContext The directory connection.
     * @throws UserStoreException If an error occurred while creating.
     */
    @Override
    protected void createOrganizationalUnit(String orgName, Tenant tenant, DirContext initialDirContext)
            throws UserStoreException {
        
        //e.g: ou=wso2.com
        String partitionDN = tenantMgtConfig.getTenantStoreProperties().get(
                UserCoreConstants.TenantMgtConfig.PROPERTY_ROOT_PARTITION);
        createOrganizationalContext(partitionDN, orgName, initialDirContext);

        //create user store
        String organizationNameAttribute = tenantMgtConfig.getTenantStoreProperties().get(
                UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_ATTRIBUTE);
        //eg:o=cse.org,dc=wso2,dc=com
        String dnOfOrganizationalContext = organizationNameAttribute + "=" + orgName + "," +
                partitionDN;
//        createOrganizationalSubContext(dnOfOrganizationalContext,
//                LDAPConstants.USER_CONTEXT_NAME, initialDirContext);

        //create group store
        createOrganizationalSubContext(dnOfOrganizationalContext,
                LDAPConstants.GROUP_CONTEXT_NAME, initialDirContext);

        //create admin entry
        String orgSubContextAttribute = tenantMgtConfig.getTenantStoreProperties().get(
                UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_ATTRIBUTE);
        //eg: ou=users,dc=wso2,dc=com
        String dnOfUserContext = orgSubContextAttribute + "=" + LDAPConstants.USER_CONTEXT_NAME
                + "," + partitionDN;
        String dnOfUserEntry = getAdminEntryDN(dnOfUserContext, tenant, initialDirContext);

        //create admin group if write ldap group is enabled
        if (("true").equals(realmConfig.getUserStoreProperty(
                LDAPConstants.WRITE_EXTERNAL_ROLES))) {
            //construct dn of group context: eg:ou=groups,o=cse.org,dc=wso2,dc=com
            String dnOfGroupContext = orgSubContextAttribute + "=" +
                    LDAPConstants.GROUP_CONTEXT_NAME + "," +
                    dnOfOrganizationalContext;
            
            createAdminGroup(dnOfGroupContext, dnOfUserEntry, initialDirContext);
        }
    }


    private String getAdminEntryDN(String dnOfUserContext, Tenant tenant, DirContext initialDirContext)
            throws UserStoreException {
        String userDN = null;
        DirContext organizationalUsersContext = null;
        try {
            //get connection to tenant's user context
            organizationalUsersContext = (DirContext) initialDirContext.lookup(
                    dnOfUserContext);
            //read user name attribute in user-mgt.xml
            String userNameAttribute = realmConfig.getUserStoreProperty(
                    LDAPConstants.USER_NAME_ATTRIBUTE);

            String userRDN = userNameAttribute + "=" + tenant.getAdminName();
            //organizationalUsersContext.bind(userRDN, null, userAttributes);
            userDN = userRDN + "," + dnOfUserContext;
            //return (userRDN + dnOfUserContext);
        } catch (NamingException e) {
            String errorMsg = "Error occurred while creating Admin entry";
            logger.error(errorMsg, e);
            throw new UserStoreException(errorMsg, e);
        } finally {
            closeContext(organizationalUsersContext);
        }

        return userDN;
    }



}
