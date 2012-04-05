/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.upgrade.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.billing.core.BillingEngine;
import org.wso2.carbon.billing.core.BillingManager;
import org.wso2.carbon.billing.core.dataobjects.Customer;
import org.wso2.carbon.billing.core.dataobjects.Item;
import org.wso2.carbon.billing.core.dataobjects.Subscription;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.billing.mgt.api.MultitenancyBillingInfo;
import org.wso2.carbon.billing.mgt.dataobjects.MultitenancyPackage;
import org.wso2.carbon.stratos.common.constants.StratosConstants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class Util {

    private static final Log log = LogFactory.getLog(Util.class);

    private static RegistryService registryService;
    private static RealmService realmService;
    private static BillingManager billingManager;
    private static MultitenancyBillingInfo mtBillingInfo;

    public static synchronized void setRegistryService(RegistryService service) {
        if (registryService == null) {
            registryService = service;
        }
    }

    public static synchronized void setRealmService(RealmService service) {
        if (realmService == null) {
            realmService = service;
        }
    }

    public static BillingManager getBillingManager() {
        return billingManager;
    }

    public static void setBillingManager(BillingManager billingManager) {
        Util.billingManager = billingManager;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static PackageInfoBean[] getPackageInfo(UserRegistry userRegistry) throws Exception {
        if (mtBillingInfo == null) {
            String msg = "Error in retrieving the current billing package. The package info is null.";
            log.error(msg);
            throw new Exception(msg);
        }
        List<MultitenancyPackage> multitenancyPackages = mtBillingInfo.getMultitenancyPackages();

        Subscription subscription = getCurrentSubscription(userRegistry);
        Item currentPackage;
        if (subscription == null) {
            // so the active until is gone, hence i'm marking it as inactive
            currentPackage = null;
        } else {
            currentPackage = subscription.getItem();
        }

        List<PackageInfoBean> packageInfoBeans = new ArrayList<PackageInfoBean>();
        for (MultitenancyPackage multitenancyPackage: multitenancyPackages) {
            PackageInfoBean packageInfoBean = new PackageInfoBean();
            packageInfoBean.setName(multitenancyPackage.getName());
            if (multitenancyPackage.getName().toLowerCase().contains("free") &&
                    currentPackage == null) {
                packageInfoBean.setCurrentPackage(true);
            }
            else if (currentPackage != null &&
                    multitenancyPackage.getName().equals(currentPackage.getName())) {
                packageInfoBean.setCurrentPackage(true);
            }
            packageInfoBean.setSubscriptionPerUserFee(multitenancyPackage.getChargePerUser().serializeToString());
            packageInfoBean.setBandwidthLimit(multitenancyPackage.getBandwidthLimit());
            packageInfoBean.setOveruseCharge(multitenancyPackage.getBandwidthOveruseCharge().serializeToString());
            packageInfoBean.setResourceVolumeLimit(multitenancyPackage.getResourceVolumeLimit());
            packageInfoBean.setUserLimit(multitenancyPackage.getUsersLimit());

            packageInfoBeans.add(packageInfoBean);
        }
        return packageInfoBeans.toArray(new PackageInfoBean[packageInfoBeans.size()]);
    }

    public static SubscriptionInfoBean getCurrentSubscriptionInfo(UserRegistry userRegistry) throws Exception {
        Subscription subscription = getCurrentSubscription(userRegistry);
        if (subscription == null) {
            return null;
        }
        SubscriptionInfoBean subscriptionInfoBean = new SubscriptionInfoBean();
        subscriptionInfoBean.setPackageName(subscription.getItem().getName());
        subscriptionInfoBean.setActiveSince(subscription.getActiveSince());
        subscriptionInfoBean.setActiveUntil(subscription.getActiveUntil());

        return subscriptionInfoBean;
    }

    public static void cancelSubscriptionInfo(UserRegistry userRegistry) throws Exception {
        BillingEngine billingEngine = billingManager.getBillingEngine(StratosConstants.MULTITENANCY_SCHEDULED_TASK_ID);

        Subscription subscription = getCurrentSubscription(userRegistry);
        if (subscription == null) {
            // nothing to un-subscribe
            return;
        }
        // what we are doing here is, set the activeUntil to today and save it
        subscription.setActiveUntil(new Date());
        //billingEngine.updateSubscription(subscription);
    }

    private static final String notNumbersRegEx = "[^0-9]";
    private static final Pattern notNumbersPattern = Pattern.compile(notNumbersRegEx);

    public static void updateSubscriptionInfo(String packageName,
                                              String durationInMonth,
                                              UserRegistry userRegistry) throws Exception {
        // we have to get the current subscription info, cancel it if it is different and add a new one
        Calendar activeUntilCalendar = Calendar.getInstance();

        if (notNumbersPattern.matcher(durationInMonth).find()) {
            String msg = "The duration in month expected to have only 0-9 characters.: " +
                    durationInMonth + " is not a number. ";
            throw new Exception(msg);
        }
        int durationInMonthNum = Integer.parseInt(durationInMonth);
        if (durationInMonthNum == 0) {
            cancelSubscriptionInfo(userRegistry);
            return;
        }
        activeUntilCalendar.add(Calendar.MONTH, durationInMonthNum);
        Date activeUntilDate = activeUntilCalendar.getTime();

        Subscription subscription = getCurrentSubscription(userRegistry);

        if (subscription != null && subscription.getItem() != null && 
                subscription.getItem().getName().equals(packageName)) {
            // then we are just extending (or just shortning) the subscription
            subscription.setActiveUntil(activeUntilDate);
            BillingEngine billingEngine =
                    billingManager.getBillingEngine(StratosConstants.MULTITENANCY_SCHEDULED_TASK_ID);

            //billingEngine.updateSubscription(subscription);
        }
        else {
            cancelSubscriptionInfo(userRegistry);
            SubscriptionInfoBean subscriptionInfoBean = new SubscriptionInfoBean();
            subscriptionInfoBean.setActiveSince(new Date());
            subscriptionInfoBean.setActiveUntil(activeUntilDate);
            subscriptionInfoBean.setPackageName(packageName);
            addSubscriptionInfo(subscriptionInfoBean, userRegistry);
        }
    }

    public static void addSubscriptionInfo(SubscriptionInfoBean subscriptionInfoBean,
                                           UserRegistry userRegistry) throws Exception {
        BillingEngine billingEngine = billingManager.getBillingEngine(StratosConstants.MULTITENANCY_SCHEDULED_TASK_ID);

        Customer customer = getCurrentCustomer(userRegistry);
        // if customer doesn't exist, we are making a one
        if (customer == null) {
            int currentTenantId = userRegistry.getTenantId();
            TenantManager tenantManger = getRealmService().getTenantManager();
            Tenant currentTenant = (Tenant) tenantManger.getTenant(currentTenantId);
            if (currentTenant == null || currentTenant.getDomain() == null) {
                String msg = "Error in getting the customer information.";
                throw new Exception(msg);
            }
            customer = new Customer();
            customer.setName(currentTenant.getDomain());
            customer.setEmail(currentTenant.getEmail());
            customer.setStartedDate(new Date());

            //billingEngine.addCustomer(customer);
        }

        String itemName = subscriptionInfoBean.getPackageName();
        if (itemName.toLowerCase().contains("free")) {
            return; //nothing to upgrade in a free package
        }
        List<Item> items = billingManager.getBillingEngine(StratosConstants.MULTITENANCY_SCHEDULED_TASK_ID).
                getItemsWithName(itemName);
        if (items == null || items.size() == 0) {
            String msg = "Invalid item name: " + itemName + ".";
            throw new Exception(msg);
        }
        Item item = items.get(0);

        // adding the subscription
        Subscription subscription = new Subscription();
        subscription.setItem(item);
        subscription.setCustomer(customer);
        subscription.setActive(true);
        subscription.setActiveSince(subscriptionInfoBean.getActiveSince());
        subscription.setActiveUntil(subscriptionInfoBean.getActiveUntil());

        billingEngine.addSubscription(subscription);
    }

    public static Subscription getCurrentSubscription(UserRegistry userRegistry) throws Exception {
        BillingEngine billingEngine = billingManager.getBillingEngine(StratosConstants.MULTITENANCY_SCHEDULED_TASK_ID);

        Customer customer = getCurrentCustomer(userRegistry);
        if (customer == null) {
            return null;
        }
        List<Subscription> subscriptions = billingEngine.getActiveSubscriptions(customer);
        if (subscriptions == null || subscriptions.size() == 0) {
            return null;
        }
        Subscription subscription = subscriptions.get(0);
        if (subscription.getActiveUntil().getTime() <= System.currentTimeMillis()) {
            return null;
        }
        int itemId = subscription.getItem().getId();
        // fill with a correct item
        Item item =  billingEngine.getItem(itemId);
        subscription.setItem(item);
        return subscription;
    }

    public static Customer getCurrentCustomer(UserRegistry userRegistry) throws Exception {
        int currentTenantId = userRegistry.getTenantId();
        TenantManager tenantManger = getRealmService().getTenantManager();
        Tenant currentTenant = (Tenant) tenantManger.getTenant(currentTenantId);
        BillingEngine billingEngine =
                billingManager.getBillingEngine(StratosConstants.MULTITENANCY_SCHEDULED_TASK_ID);
        List<Customer> customers = billingEngine.getCustomersWithName(currentTenant.getDomain());
        if (customers == null || customers.size() == 0) {
            return null;
        }
        return customers.get(0);
    }


    public static void setMultitenancyBillingInfo(MultitenancyBillingInfo mtBillingInfo) {
        Util.mtBillingInfo = mtBillingInfo;
    }
}
