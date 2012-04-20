package org.wso2.carbon.humantask.core.integration;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.humantask.core.dao.GenericHumanRoleDAO;
import org.wso2.carbon.humantask.core.dao.HumanTaskDAOConnection;
import org.wso2.carbon.humantask.core.dao.OrganizationalEntityDAO;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PeopleQuery evaluator is used to get the set of users from user manager giving set
 * of arguments as defined in human interaction file's logical people groups
 */
public class UserManagerBasedPeopleQueryEvaluator implements PeopleQueryEvaluator {

    private static Log log = LogFactory.getLog(UserManagerBasedPeopleQueryEvaluator.class);

    RegistryService registryService;

    public UserManagerBasedPeopleQueryEvaluator() {
        this.registryService = HumanTaskServiceComponent.getRegistryService();
    }

    public boolean isExistingUser(String userName) {
        try {
            return getUserRealm().getUserStoreManager().isExistingUser(userName);
        } catch (UserStoreException e) {
            throw new HumanTaskRuntimeException("Error occurred while calling to realm service", e);
        }
    }

    /**
     * Return true if the provided role name exist.
     *
     * @param roleName :  The role name to check.
     * @return : True is the role exists, false otherwise.
     */
    @Override
    public boolean isExistingRole(String roleName) {
        try {
            return getUserRealm().getUserStoreManager().isExistingRole(roleName);
        } catch (UserStoreException e) {
            throw new HumanTaskRuntimeException("Error occurred while calling to realm service " +
                                                "for operation isExistingRole", e);
        }
    }

    @Override
    public boolean hasUsersForRole(String roleName) {
        throw new UnsupportedOperationException("TODO Implement me");
    }

    @Override
    public List<String> getUserNameListForRole(String roleName) {
        if (isExistingRole(roleName)) {
            try {
                return new ArrayList<String>(Arrays.asList(getUserRealm().
                        getUserStoreManager().getUserListOfRole(roleName)));
            } catch (UserStoreException e) {
                throw new HumanTaskRuntimeException("Error occurred while calling" +
                                                    " to realm service for operation isExistingRole", e);
            }
        } else {
            throw new HumanTaskRuntimeException(String.format("The role name[%s] does not exist.", roleName));
        }
    }

    @Override
    public List<String> getRoleNameListForUser(String userName) {
        List<String> matchingRoleNames = new ArrayList<String>();
        if (StringUtils.isNotEmpty(userName)) {
            userName = userName.trim();
            if (isExistingUser(userName)) {
                try {
                    matchingRoleNames.addAll(
                            Arrays.asList(
                                    getUserRealm().getUserStoreManager().
                                            getRoleListOfUser(userName)));
                } catch (UserStoreException ex) {
                    throw new HumanTaskRuntimeException("Error occurred while calling" +
                                                        " to realm service for operation isExistingRole", ex);
                }
            }
        }
        return matchingRoleNames;
    }

    @Override
    public OrganizationalEntityDAO createGroupOrgEntityForRole(String roleName) {
        roleName = roleName.trim();
        if (isExistingRole(roleName)) {
            return getConnection().createNewOrgEntityObject(roleName, OrganizationalEntityDAO.OrganizationalEntityType.GROUP);
        } else {
            throw new HumanTaskRuntimeException(String.format("The role name[%s] does not exist.", roleName));
        }
    }

    @Override
    public OrganizationalEntityDAO createUserOrgEntityForName(String userName) {
        if (isExistingUser(userName)) {
            return getConnection().createNewOrgEntityObject(userName, OrganizationalEntityDAO.OrganizationalEntityType.USER);
        } else {
            throw new HumanTaskRuntimeException(String.format("The user name[%s] does not exist.", userName));
        }
    }

    @Override
    public GenericHumanRoleDAO createGHRForRoleName(String roleName,
                                                    GenericHumanRoleDAO.GenericHumanRoleType type) {
        if (isExistingRole(roleName)) {

            List<String> userNames = getUserNameListForRole(roleName);
            GenericHumanRoleDAO ghr = getConnection().createNewGHRObject(type);
            List<OrganizationalEntityDAO> orgEntities = new ArrayList<OrganizationalEntityDAO>();
            for (String userName : userNames) {
                OrganizationalEntityDAO orgEntity =
                        getConnection().createNewOrgEntityObject(userName,
                                                                 OrganizationalEntityDAO.OrganizationalEntityType.USER);
                orgEntity.addGenericHumanRole(ghr);
                orgEntities.add(orgEntity);
            }

            ghr.setOrgEntities(orgEntities);

            return ghr;

        } else {
            throw new HumanTaskRuntimeException(String.format("The role name[%s] does not exist.", roleName));
        }
    }

    @Override
    public void checkOrgEntitiesExist(List<OrganizationalEntityDAO> orgEntities) {
        if (orgEntities != null) {
            for (OrganizationalEntityDAO orgEntity : orgEntities) {
                checkOrgEntityExists(orgEntity);
            }
        }
    }

    @Override
    public void checkOrgEntityExists(OrganizationalEntityDAO orgEntity) {
        if (orgEntity != null) {
            if (OrganizationalEntityDAO.OrganizationalEntityType.USER.equals(orgEntity.getOrgEntityType())) {
                if (!isExistingUser(orgEntity.getName())) {
                    throw new HumanTaskRuntimeException(String.format("The user name:[%s] " +
                                                                      "does not exist in the user store!",
                                                                      orgEntity.getName()));
                }
            } else if (OrganizationalEntityDAO.OrganizationalEntityType.GROUP.equals(orgEntity.getOrgEntityType())) {
                if (!isExistingRole(orgEntity.getName())) {
                    throw new HumanTaskRuntimeException(String.format("The group name:[%s] " +
                                                                      "does not exist in the user store!",
                                                                      orgEntity.getName()));
                }
            }
        }
    }

    @Override
    public boolean isOrgEntityInRole(OrganizationalEntityDAO entity, GenericHumanRoleDAO role) {
        boolean isOrgEntityInRole = false;

        for (OrganizationalEntityDAO orgEntity : role.getOrgEntities()) {
            if (OrganizationalEntityDAO.OrganizationalEntityType.USER.equals(orgEntity.getOrgEntityType())) {
                if (orgEntity.getName().equals(entity.getName())) {
                    isOrgEntityInRole = true;
                }
            } else if (OrganizationalEntityDAO.OrganizationalEntityType.GROUP.equals(orgEntity.getOrgEntityType())) {
                if (getUserNameListForRole(orgEntity.getName()).contains(entity.getName())) {
                    isOrgEntityInRole = true;
                }
            }

            if (isOrgEntityInRole) {
                break;
            }
        }

        return isOrgEntityInRole;
    }

    @Override
    public String getLoggedInUser() {
        String userName = null;
        if (StringUtils.isNotEmpty(CarbonContext.getCurrentContext().getUsername())) {
            userName = CarbonContext.getCurrentContext().getUsername();
        }
        return userName;
    }

    private HumanTaskDAOConnection getConnection() {
        return HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine().
                getDaoConnectionFactory().getConnection();
    }

    private UserRealm getUserRealm() {
        Integer tenantId = CarbonContextHolder.getThreadLocalCarbonContextHolder().getTenantId();

        if (tenantId < 0) {
            log.warn("Invalid Tenant Id " + tenantId);
            return null;
        }

        try {
            // TODO - add null check for the user realm.
            return this.registryService.getUserRealm(tenantId);
        } catch (RegistryException e) {
            throw new HumanTaskRuntimeException("Error occurred while retrieving " +
                                                "User Realm for tenant :" + tenantId);
        }

    }
}
