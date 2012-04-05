/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.tenant.mgt.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.stratos.common.util.ClaimsMgtUtil;
import org.wso2.carbon.stratos.common.util.CommonUtil;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.multitenancy.persistence.TenantPersistor;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.tenant.mgt.beans.PaginatedTenantInfoBean;
import org.wso2.carbon.tenant.mgt.beans.TenantInfoBean;
import org.wso2.carbon.tenant.mgt.internal.TenantMgtServiceComponent;
import org.wso2.carbon.tenant.mgt.internal.util.PasswordUtil;
import org.wso2.carbon.tenant.mgt.util.TenantMgtUtil;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.DataPaginator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the admin Web service which is used for managing tenants
 */
public class TenantMgtAdminService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(TenantMgtAdminService.class);

    /**
     * super admin adds a tenant
     *
     * @param tenantInfoBean tenant info bean
     * @return UUID
     * @throws Exception if error in adding new tenant.
     */
    public String addTenant(TenantInfoBean tenantInfoBean) throws Exception {
        try {
            CommonUtil.validateEmail(tenantInfoBean.getEmail());
        } catch (Exception e) {
            String msg = "Invalid email is provided.";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
        TenantMgtUtil.validateDomain(tenantInfoBean.getTenantDomain());
        UserRegistry userRegistry = (UserRegistry) getGovernanceRegistry();
        if (userRegistry == null) {
            log.error("Security Alert! User registry is null. A user is trying create a tenant "
                      + " without an authenticated session.");
            throw new Exception("Invalid data."); // obscure error message.
        }

        if (userRegistry.getTenantId() != 0) {
            log.error("Security Alert! Non super tenant trying to create a tenant.");
            throw new Exception("Invalid data."); // obscure error message.
        }
        Tenant tenant = TenantMgtUtil.initializeTenant(tenantInfoBean);
        TenantPersistor persistor = TenantMgtServiceComponent.getTenantPersistor();
        // not validating the domain ownership, since created by super tenant
        persistor.persistTenant(tenant, false, tenantInfoBean.getSuccessKey(),
                                tenantInfoBean.getOriginatedService());
        TenantMgtUtil.addClaimsToUserStoreManager(tenant);
        // For the registration validation - mail for the tenant email address
        TenantMgtUtil.sendEmail(tenant, tenantInfoBean.getOriginatedService());

        // Notifies the super admin about the new tenant creation
        TenantMgtUtil.notifyTenantCreationToSuperAdmin(
                tenantInfoBean.getTenantDomain(),tenantInfoBean.getAdmin(),
                tenantInfoBean.getEmail());

        //adding the subscription entry
        try{
            if(TenantMgtServiceComponent.getBillingService() != null){
                TenantMgtServiceComponent.getBillingService().
                        addUsagePlan(tenant, tenantInfoBean.getUsagePlan());
                if (log.isDebugEnabled()) {
                    log.debug("Subscription added successfully for the tenant: " +
                              tenantInfoBean.getTenantDomain());
                }
            }

        }catch(Exception e){
            log.error("Error occurred while adding the subscription for tenant: " +
                    tenantInfoBean.getTenantDomain() + " " + e.getMessage(), e);    
        }


        return TenantMgtUtil.prepareStringToShowThemeMgtPage(tenant.getId());
    }

    /**
     * Get the list of the tenants
     *
     * @return List<TenantInfoBean>
     * @throws Exception UserStorException
     */
    private List<TenantInfoBean> getAllTenants() throws Exception {
        TenantManager tenantManager = TenantMgtServiceComponent.getTenantManager();
        Tenant[] tenants;
        try {
            tenants = (Tenant[]) tenantManager.getAllTenants();
        } catch (UserStoreException e) {
            String msg = "Error in retrieving the tenant information.";
            log.error(msg, e);
            throw new Exception(msg, e);
        }

        List<TenantInfoBean> tenantList = new ArrayList<TenantInfoBean>();
        for (Tenant tenant : tenants) {
            TenantInfoBean bean = TenantMgtUtil.getTenantInfoBeanfromTenant(tenant.getId(), tenant);
            tenantList.add(bean);
        }
        return tenantList;
    }

    /**
     * Retrieve all the tenants
     *
     * @return tenantInfoBean[]
     * @throws Exception if failed to get Tenant Manager
     */
    public TenantInfoBean[] retrieveTenants() throws Exception {
        List<TenantInfoBean> tenantList = getAllTenants();
        return tenantList.toArray(new TenantInfoBean[tenantList.size()]);
    }

    /**
     * Method to retrieve all the tenants paginated
     *
     * @param pageNumber Number of the page.
     * @return PaginatedTenantInfoBean
     * @throws Exception if failed to getTenantManager;
     */
    public PaginatedTenantInfoBean retrievePaginatedTenants(int pageNumber) throws Exception {
        List<TenantInfoBean> tenantList = getAllTenants();

        // Pagination
        PaginatedTenantInfoBean paginatedTenantInfoBean = new PaginatedTenantInfoBean();
        DataPaginator.doPaging(pageNumber, tenantList, paginatedTenantInfoBean);
        return paginatedTenantInfoBean;
    }

    /**
     * Get a specific tenant
     *
     * @param tenantDomain tenant domain
     * @return tenantInfoBean
     * @throws Exception UserStoreException
     */
    public TenantInfoBean getTenant(String tenantDomain) throws Exception {
        TenantManager tenantManager = TenantMgtServiceComponent.getTenantManager();

        int tenantId;
        try {
            tenantId = tenantManager.getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            String msg = "Error in retrieving the tenant id for the tenant domain: " +
                         tenantDomain + ".";
            log.error(msg);
            throw new Exception(msg, e);
        }
        Tenant tenant;
        try {
            tenant = (Tenant) tenantManager.getTenant(tenantId);
        } catch (UserStoreException e) {
            String msg = "Error in retrieving the tenant from the tenant manager.";
            log.error(msg);
            throw new Exception(msg, e);
        }

        TenantInfoBean bean = TenantMgtUtil.initializeTenantInfoBean(tenantId, tenant);

        // retrieve first and last names from the UserStoreManager
        bean.setFirstname(ClaimsMgtUtil.getFirstNamefromUserStoreManager(
                TenantMgtServiceComponent.getRealmService(), tenant, tenantId));
        bean.setLastname(ClaimsMgtUtil.getLastNamefromUserStoreManager(
                TenantMgtServiceComponent.getRealmService(), tenant, tenantId));

        //getting the subscription plan
        String activePlan = "";
        if(TenantMgtServiceComponent.getBillingService() != null){
            activePlan = TenantMgtServiceComponent.getBillingService().
                    getActiveUsagePlan(tenantDomain);
        }

        if(activePlan != null && activePlan.trim().length() > 0){
            bean.setUsagePlan(activePlan);
        }else{
            bean.setUsagePlan("");
        }

        return bean;
    }

    /**
     * Updates a given tenant
     *
     * @param tenantInfoBean tenant information
     * @throws Exception UserStoreException
     */
    public void updateTenant(TenantInfoBean tenantInfoBean) throws Exception {
        TenantManager tenantManager = TenantMgtServiceComponent.getTenantManager();
        UserStoreManager userStoreManager;

        // filling the non-set admin and admin password first
        UserRegistry configSystemRegistry = TenantMgtServiceComponent.getConfigSystemRegistry(
                tenantInfoBean.getTenantId());

        String tenantDomain = tenantInfoBean.getTenantDomain();

        int tenantId;
        try {
            tenantId = tenantManager.getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            String msg = "Error in retrieving the tenant id for the tenant domain: " + tenantDomain
                         + ".";
            log.error(msg, e);
            throw new Exception(msg, e);
        }

        boolean updatePassword = false;
        boolean isPasswordChanged = false;
        if (tenantInfoBean.getAdminPassword() != null
            && !tenantInfoBean.getAdminPassword().equals("")) {
            updatePassword = true;
        }

        Tenant tenant;
        try {
            tenant = (Tenant) tenantManager.getTenant(tenantId);
        } catch (UserStoreException e) {
            String msg = "Error in retrieving the tenant id for the tenant domain: " +
                         tenantDomain + ".";
            log.error(msg, e);
            throw new Exception(msg, e);
        }

        // filling the first and last name values
        if (tenantInfoBean.getFirstname() != null &&
            !tenantInfoBean.getFirstname().trim().equals("")) {
            try {
                CommonUtil.validateName(tenantInfoBean.getFirstname(), "First Name");
            } catch (Exception e) {
                String msg = "Invalid first name is provided.";
                log.error(msg, e);
                throw new Exception(msg, e);
            }
        }
        if (tenantInfoBean.getLastname() != null &&
            !tenantInfoBean.getLastname().trim().equals("")) {
            try {
                CommonUtil.validateName(tenantInfoBean.getLastname(), "Last Name");
            } catch (Exception e) {
                String msg = "Invalid last name is provided.";
                log.error(msg, e);
                throw new Exception(msg, e);
            }
        }
        Map<String, String> claimsMap = new HashMap<String, String>(); // map of claims
        claimsMap.put(UserCoreConstants.ClaimTypeURIs.GIVEN_NAME, tenantInfoBean.getFirstname());
        claimsMap.put(UserCoreConstants.ClaimTypeURIs.SURNAME, tenantInfoBean.getLastname());

        userStoreManager = TenantMgtUtil.getUserStoreManager(tenant, tenant.getId());
        userStoreManager.setUserClaimValues(tenantInfoBean.getAdmin(), claimsMap,
                                            UserCoreConstants.DEFAULT_PROFILE);

        // filling the email value
        if (tenantInfoBean.getEmail() != null && !tenantInfoBean.getEmail().equals("")) {
            // validate the email
            try {
                CommonUtil.validateEmail(tenantInfoBean.getEmail());
            } catch (Exception e) {
                String msg = "Invalid email is provided.";
                log.error(msg, e);
                throw new Exception(msg, e);
            }
            tenant.setEmail(tenantInfoBean.getEmail());
        }

        UserRealm userRealm = configSystemRegistry.getUserRealm();
        try {
            userStoreManager = userRealm.getUserStoreManager();
        } catch (UserStoreException e) {
            String msg = "Error in getting the user store manager for tenant, tenant domain: " +
                         tenantDomain + ".";
            log.error(msg, e);
            throw new Exception(msg, e);
        }

        if (!userStoreManager.isReadOnly() && updatePassword) {
            // now we will update the tenant admin with the admin given
            // password.
            try {
                userStoreManager.updateCredentialByAdmin(tenantInfoBean.getAdmin(),
                                                         tenantInfoBean.getAdminPassword());
                isPasswordChanged = true;
            } catch (UserStoreException e) {
                String msg = "Error in changing the tenant admin password, tenant domain: " +
                             tenantInfoBean.getTenantDomain() + ". " + e.getMessage() + " for: " +
                             tenantInfoBean.getAdmin();
                log.error(msg, e);
                throw new Exception(msg, e);
            }
        }

        try {
            tenantManager.updateTenant(tenant);
        } catch (UserStoreException e) {
            String msg = "Error in updating the tenant for tenant domain: " + tenantDomain + ".";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
        try {
            TenantMgtUtil.triggerUpdateTenant(tenantInfoBean);
        } catch (UserStoreException e) {
            String msg = "Error in calling the callbacks for the add tenant.";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
        if (isPasswordChanged) { // email the tenant admin the new password.
            PasswordUtil.notifyResetPassword(tenantInfoBean);
        }

        //updating the usage plan
        try{
            if(TenantMgtServiceComponent.getBillingService() != null){
                TenantMgtServiceComponent.getBillingService().
                        updateUsagePlan(tenantInfoBean.getTenantDomain(), tenantInfoBean.getUsagePlan());
            }
        }catch(Exception e){
            String msg = "Error when updating the usage plan: " + e.getMessage();
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }

    /**
     * Activate the given tenant
     *
     * @param tenantDomain tenant domain
     * @throws Exception UserStoreException.
     */
    public void activateTenant(String tenantDomain) throws Exception {
        TenantManager tenantManager = TenantMgtServiceComponent.getTenantManager();
        int tenantId;
        try {
            tenantId = tenantManager.getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            String msg = "Error in retrieving the tenant id for the tenant domain: " + tenantDomain
                         + ".";
            log.error(msg, e);
            throw new Exception(msg, e);
        }

        try {
            tenantManager.activateTenant(tenantId);
        } catch (UserStoreException e) {
            String msg = "Error in activating the tenant for tenant domain: " + tenantDomain + ".";
            log.error(msg, e);
            throw new Exception(msg, e);
        }

        //activating the subscription
        try{
            if (TenantMgtServiceComponent.getBillingService() != null) {
                TenantMgtServiceComponent.getBillingService().activateUsagePlan(tenantDomain);
            }
        }catch(Exception e){
            String msg = "Error while activating subscription for domain: " + tenantDomain + ".";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }

    /**
     * Deactivate the given tenant
     *
     * @param tenantDomain tenant domain
     * @throws Exception UserStoreException
     */
    public void deactivateTenant(String tenantDomain) throws Exception {
        TenantManager tenantManager = TenantMgtServiceComponent.getTenantManager();
        int tenantId;
        try {
            tenantId = tenantManager.getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            String msg =
                    "Error in retrieving the tenant id for the tenant domain: " +
                    tenantDomain + ".";
            log.error(msg, e);
            throw new Exception(msg, e);
        }

        try {
            tenantManager.deactivateTenant(tenantId);
        } catch (UserStoreException e) {
            String msg =
                    "Error in deactivating the tenant for tenant domain: " + tenantDomain +
                    ".";
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }
}
