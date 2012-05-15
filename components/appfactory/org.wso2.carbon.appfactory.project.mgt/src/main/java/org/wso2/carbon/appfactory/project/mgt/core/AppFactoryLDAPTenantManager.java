package org.wso2.carbon.appfactory.project.mgt.core;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.TenantMgtConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.ldap.LDAPConnectionContext;
import org.wso2.carbon.user.core.ldap.LDAPConstants;
import org.wso2.carbon.user.core.tenant.JDBCTenantManager;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.sql.DataSource;
import java.util.Map;

/**
 * .
 */
public class AppFactoryLDAPTenantManager extends JDBCTenantManager{
    private static final Log logger= LogFactory.getLog(AppFactoryLDAPTenantManager.class);
    private LDAPConnectionContext ldapConnectionSource;
    private TenantMgtConfiguration tenantMgtConfig = null;
    private RealmConfiguration realmConfig = null;
    public AppFactoryLDAPTenantManager(OMElement omElement, Map<String, Object> properties)
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

    public AppFactoryLDAPTenantManager(DataSource dataSource, String superTenantDomain) {
        super(dataSource, superTenantDomain);
    }

    @Override
    public int addTenant(Tenant tenant) throws UserStoreException {
        int tenantId= super.addTenant(tenant);
        tenant.setId(tenantId);
        //search the admin userName in OU=peoples
        //get the DN of the user
        //create admin group and add the tenant
        DirContext initialDirContext = null;
        try {
            initialDirContext = this.ldapConnectionSource.getContext();
            //create per tenant context and its user store and group store with admin related entries.
            createOrganizationalUnit(tenant.getDomain(), (org.wso2.carbon.user.core.tenant.Tenant) tenant, initialDirContext);
        } finally {
            closeContext(initialDirContext);
        }


        return tenantId;
    }

    /**
     * Create a space for tenant in LDAP.
     *
     * @param orgName  Organization name.
     * @param tenant The tenant
     * @param initialDirContext The directory connection.
     * @throws UserStoreException If an error occurred while creating.
     */
    private void createOrganizationalUnit(String orgName, org.wso2.carbon.user.core.tenant.Tenant tenant, DirContext initialDirContext)
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
       // createOrganizationalSubContext(dnOfOrganizationalContext,
       //                                LDAPConstants.USER_CONTEXT_NAME, initialDirContext);

        //create group store
        createOrganizationalSubContext(dnOfOrganizationalContext,
                                       LDAPConstants.GROUP_CONTEXT_NAME, initialDirContext);

        //create admin entry
        String orgSubContextAttribute = tenantMgtConfig.getTenantStoreProperties().get(
                UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_ATTRIBUTE);
        //eg: ou=users,o=cse.org,dc=wso2,dc=com
        //String dnOfUserContext = orgSubContextAttribute + "=" +realmConfig.getUserStoreProperty(LDAPConstants.USER_CONTEXT_NAME)
         //                        + "," + partitionDN;
        String dnOfUserEntry = getDNOfTheTenantAdmin(realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE), tenant, initialDirContext);

        //create admin group

            //construct dn of group context: eg:ou=groups,o=cse.org,dc=wso2,dc=com
            String dnOfGroupContext = orgSubContextAttribute + "=" +
                                      LDAPConstants.GROUP_CONTEXT_NAME + "," +
                                      dnOfOrganizationalContext;
            createAdminGroup(dnOfGroupContext, dnOfUserEntry, initialDirContext);

    }

    /**
     * Create main context corresponding to tenant.
     *
     * @param rootDN   Root domain name.
     * @param orgName Organization name
     * @param initialDirContext The directory connection.
     * @throws UserStoreException If an error occurred while creating context.
     */
    private void createOrganizationalContext(String rootDN, String orgName, DirContext initialDirContext)
            throws UserStoreException {

        DirContext subContext = null;
        DirContext organizationalContext = null;
        try {

            //get the connection context for rootDN
            subContext = (DirContext) initialDirContext.lookup(rootDN);

            Attributes contextAttributes = new BasicAttributes(true);
            //create organizational object class attribute
            Attribute objectClass = new BasicAttribute(LDAPConstants.OBJECT_CLASS_NAME);
            objectClass.add(
                    tenantMgtConfig.getTenantStoreProperties().get(
                            UserCoreConstants.TenantMgtConfig.PROPERTY_ORGANIZATIONAL_OBJECT_CLASS));
            contextAttributes.put(objectClass);
            //create organizational name attribute
            String organizationalNameAttribute = tenantMgtConfig.getTenantStoreProperties().get(
                    UserCoreConstants.TenantMgtConfig.PROPERTY_ORGANIZATIONAL_ATTRIBUTE);
            Attribute organization =
                    new BasicAttribute(organizationalNameAttribute);
            organization.add(orgName);
            contextAttributes.put(organization);
            //construct organization rdn.
            String rdnOfOrganizationalContext = organizationalNameAttribute + "=" + orgName;
            if (logger.isDebugEnabled()) {
                logger.debug("Adding sub context: " + rdnOfOrganizationalContext + " under " +
                             rootDN + " ...");
            }
            //create organization sub context
            organizationalContext = subContext.createSubcontext(rdnOfOrganizationalContext, contextAttributes);
            if (logger.isDebugEnabled()) {
                logger.debug("Sub context: " + rdnOfOrganizationalContext + " was added under "
                             + rootDN + " successfully.");
            }

        } catch (NamingException e) {
            String errorMsg = "Error occurred while adding the organizational unit " +
                              "sub context.";
            logger.error(errorMsg, e);
            throw new UserStoreException(errorMsg, e);
        } finally {
            closeContext(organizationalContext);
            closeContext(subContext);
        }
    }

    private void closeContext(DirContext ldapContext) {
        if (ldapContext != null) {
            try {
                ldapContext.close();
            } catch (NamingException e) {
                logger.error("Error closing sub context.", e);
            }
        }
    }

    /**
     * Create sub contexts under the tenant's main context.
     *
     * @param dnOfParentContext domain name of the parent context.
     * @param nameOfCurrentContext name of the current context.
     * @param initialDirContext The directory connection.
     * @throws UserStoreException if an error occurs while creating context.
     */
    private void createOrganizationalSubContext(String dnOfParentContext,
                                                String nameOfCurrentContext, DirContext initialDirContext)
            throws UserStoreException {

        DirContext subContext = null;
        DirContext organizationalContext = null;

        try {
            //get the connection for tenant's main context
            subContext = (DirContext) initialDirContext.lookup(dnOfParentContext);

            Attributes contextAttributes = new BasicAttributes(true);
            //create sub unit object class attribute
            Attribute objectClass = new BasicAttribute(LDAPConstants.OBJECT_CLASS_NAME);
            objectClass.add(tenantMgtConfig.getTenantStoreProperties().get(
                    UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_OBJ_CLASS));
            contextAttributes.put(objectClass);

            //create org sub unit name attribute
            String orgSubUnitAttributeName = tenantMgtConfig.getTenantStoreProperties().get(
                    UserCoreConstants.TenantMgtConfig.PROPERTY_ORG_SUB_CONTEXT_ATTRIBUTE);
            Attribute organizationSubUnit = new BasicAttribute(orgSubUnitAttributeName);
            organizationSubUnit.add(nameOfCurrentContext);
            contextAttributes.put(organizationSubUnit);

            //construct the rdn of org sub context
            String rdnOfOrganizationalContext = orgSubUnitAttributeName + "=" +
                                                nameOfCurrentContext;
            if (logger.isDebugEnabled()) {
                logger.debug("Adding sub context: " + rdnOfOrganizationalContext + " under " +
                             dnOfParentContext + " ...");
            }
            //create sub context
            organizationalContext = subContext.createSubcontext(rdnOfOrganizationalContext, contextAttributes);
            if (logger.isDebugEnabled()) {
                logger.debug("Sub context: " + rdnOfOrganizationalContext + " was added under "
                             + dnOfParentContext + " successfully.");
            }

        } catch (NamingException e) {
            String errorMsg = "Error occurred while adding the organizational unit " +
                              "sub context.";
            logger.error(errorMsg, e);
            throw new UserStoreException(errorMsg, e);
        } finally {
            closeContext(organizationalContext);
            closeContext(subContext);
        }
    }

    private String getDNOfTheTenantAdmin(String dnOfUserContext, org.wso2.carbon.user.core.tenant.Tenant tenant, DirContext initialDirContext)
            throws UserStoreException {
        String userDN = null;
        DirContext organizationalUsersContext = null;
        try {
            //get connection to tenant's user context
            organizationalUsersContext = (DirContext) initialDirContext.lookup(
                    dnOfUserContext);



            //return (userRDN + dnOfUserContext);
            NamingEnumeration<SearchResult> results;
            String searchFilter = realmConfig.getUserStoreProperty(
                    LDAPConstants.USER_NAME_FILTER);
            searchFilter = searchFilter.replace("?",tenant.getAdminName());
            results =
                    searchInUserBase(searchFilter, new String[]{},
                                     SearchControls.SUBTREE_SCOPE,initialDirContext);
            //we assume only one user with the given user name under user search base.
            SearchResult userResult;
            if (results.hasMore()) {
                userResult = results.next();
            } else {
                String errorMsg = "There is no user with the user name: " +
                                  tenant.getAdminName();
                logger.error(errorMsg);
                throw new UserStoreException(errorMsg);
            }
            //get his DN
            userDN= userResult.getNameInNamespace();
        } catch (NamingException e) {
            String errorMsg = "Error occurred while creating Admin entry";
            logger.error(errorMsg, e);
            throw new UserStoreException(errorMsg, e);
        } finally {
            closeContext(organizationalUsersContext);
        }

        return userDN;
    }

    private void createAdminGroup(String dnOfGroupContext, String adminUserDN, DirContext initialDirContext)
            throws UserStoreException {
        //create set of attributes required to create admin group
        Attributes adminGroupAttributes = new BasicAttributes(true);
        //admin entry object class
        Attribute objectClassAttribute = new BasicAttribute(LDAPConstants.OBJECT_CLASS_NAME);
        objectClassAttribute.add(realmConfig.getUserStoreProperty(
                LDAPConstants.GROUP_ENTRY_OBJECT_CLASS));
        adminGroupAttributes.put(objectClassAttribute);

        //group name attribute
        String groupNameAttributeName = realmConfig.getUserStoreProperty(
                LDAPConstants.ROLE_NAME_ATTRIBUTE_NAME);
        Attribute groupNameAttribute = new BasicAttribute(groupNameAttributeName);
        String adminRoleName = realmConfig.getAdminRoleName();
        groupNameAttribute.add(adminRoleName);
        adminGroupAttributes.put(groupNameAttribute);

        //membership attribute
        Attribute membershipAttribute = new BasicAttribute(realmConfig.getUserStoreProperty(
                LDAPConstants.MEMBERSHIP_ATTRIBUTE));
        membershipAttribute.add(adminUserDN);
        adminGroupAttributes.put(membershipAttribute);

        DirContext groupContext = null;
        try {
            groupContext = (DirContext) initialDirContext.lookup(dnOfGroupContext);
            String rdnOfAdminGroup = groupNameAttributeName + "=" + adminRoleName;
            groupContext.bind(rdnOfAdminGroup, null, adminGroupAttributes);

        } catch (NamingException e) {
            String errorMessage = "Error occurred while creating the admin group.";
            logger.error(errorMessage);
            throw new UserStoreException(errorMessage, e);
        } finally {
            closeContext(groupContext);
        }
    }
    private NamingEnumeration<SearchResult> searchInUserBase(String searchFilter,
                                                             String[] returningAttributes,
                                                             int searchScope, DirContext rootContext)
            throws UserStoreException {
        String userBase = realmConfig.getUserStoreProperty(LDAPConstants.USER_SEARCH_BASE);
        SearchControls userSearchControl = new SearchControls();
        userSearchControl.setReturningAttributes(returningAttributes);
        userSearchControl.setSearchScope(searchScope);
        NamingEnumeration<SearchResult> userSearchResults;

        try {
            userSearchResults = rootContext.search(
                    userBase, searchFilter, userSearchControl);
        } catch (NamingException e) {
            String errorMessage = "Error occurred while searching in user base.";
            logger.error(errorMessage, e);
            throw new UserStoreException(errorMessage, e);
        }

        return userSearchResults;

    }
}
