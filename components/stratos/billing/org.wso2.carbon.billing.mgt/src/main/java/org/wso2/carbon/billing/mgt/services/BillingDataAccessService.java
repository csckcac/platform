/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.billing.mgt.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.billing.core.DataAccessManager;
import org.wso2.carbon.billing.core.dataobjects.Customer;
import org.wso2.carbon.billing.core.dataobjects.Subscription;
import org.wso2.carbon.billing.mgt.util.Util;
import org.wso2.carbon.core.AbstractAdmin;

import java.util.List;

/**
 * This service allows other components to access billing related data
 * without going through a billing manager
 */
public class BillingDataAccessService extends AbstractAdmin {
    private static Log log = LogFactory.getLog(BillingDataAccessService.class);

    /**
     * Add a new subscription to the BC_SUBSCRIPTION table
     * @param subscription  object with subscription info
     * @return
     * @throws Exception
     */
    public int addSubscription(Subscription subscription) throws Exception {
        DataAccessManager dataAccessManager = Util.getDataAccessManager();
        return dataAccessManager.addSubscription(subscription);
    }

    /**
     * Finds the customer with a given tenant domain
     * @param customerName  is the tenant domain
     * @return  a customer object
     * @throws Exception
     */
    public Customer getCustomerWithName(String customerName) throws Exception {
        DataAccessManager dataAccessManager = Util.getDataAccessManager();
        Customer customer = null;
        List<Customer> customers = dataAccessManager.getCustomersWithName(customerName);
        if (customers.size() > 0) {
            customer = customers.get(0);
        }
        return customer;
    }

    /**
     * Get a subscription with a given id
     * @param subscriptionId
     * @return a subscription object
     * @throws Exception
     */
    public Subscription getSubscription(int subscriptionId) throws Exception {
        DataAccessManager dataAccessManager = Util.getDataAccessManager();
        return dataAccessManager.getSubscription(subscriptionId);
    }

    /**
     * Gets the active subscription of a customer. There can be only one active
     * subscription for a customer at a given time
     * @param customerId
     * @return  a subscription object
     * @throws Exception
     */
    public Subscription getActiveSubscriptionOfCustomer(int customerId) throws Exception {
        DataAccessManager dataAccessManager = Util.getDataAccessManager();
        return dataAccessManager.getActiveSubscriptionOfCustomer(customerId);
    }

    /**
     * Gets the item id for a given item name and a parent id
     * For example "subscription" item id of multitenancy-small subscription
     * @param name  e.g. "subscription", "bwOveruse", "storageOveruse"
     * @param parentId
     * @return  the item id from the BC_ITEM table
     * @throws Exception
     */
    public int getItemIdWithName(String name, int parentId) throws Exception {
        DataAccessManager dataAccessManager = Util.getDataAccessManager();
        return dataAccessManager.getItemIdWithName(name, parentId);
    }

    /**
     * @param customerId       is same as tenantId for tenant unique id use to identify tenant
     * @param subscriptionPlan new Usage plan name that user expect to go
     * @return
     * @throws Exception in error in change subscription or
     */
    public boolean changeSubscription(int customerId, String subscriptionPlan) throws Exception {
        DataAccessManager dataAccessManager = Util.getDataAccessManager();
        if (dataAccessManager.changeSubscription(customerId, subscriptionPlan)) {
            boolean isExecutedSuccessfully = false;
            try {
                Util.executeThrottlingRules(customerId);
                isExecutedSuccessfully = true;
            }
            catch (Exception e) {
                log.error("Error occured executing throttling rules at billing mgt" + e.toString());
            }
            //send mail
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the inactive subscriptions of a customer ordered by ACTIVE_SINCE time
     * in the descending order (i.e. latest ones first)
     * @param customerId
     * @return  an array of subscriptions
     * @throws Exception
     */
    public Subscription[] getInactiveSubscriptionsOfCustomer(int customerId) throws Exception {
        DataAccessManager dataAccessManager = Util.getDataAccessManager();
        List<Subscription> subscriptions = dataAccessManager.getInactiveSubscriptionsOfCustomer(customerId);
        Subscription[] subscriptionArray;
        if (subscriptions != null && subscriptions.size() > 0) {
            subscriptionArray = subscriptions.toArray(new Subscription[subscriptions.size()]);
        } else {
            subscriptionArray = new Subscription[0];
        }
        return subscriptionArray;
    }

    /**
     * Activate a subscription with a given id
     * @param subscriptionId is the id of subscription which needs to be activated
     * @return  true or false based on whether the operation was successful or not
     * @throws Exception
     */
    public boolean activateSubscription(int subscriptionId) throws Exception {
        DataAccessManager dataAccessManager = Util.getDataAccessManager();
        return dataAccessManager.activateSubscription(subscriptionId);
    }

    /**
     * Deactivates the active subscription of a customer
     * @param tenantId is the customer id (both have the same meaning)
     * @return true or false based on whether the operation was successful or not 
     * @throws Exception
     */
    public boolean deactivateActiveSubscription(int tenantId) throws Exception {
        DataAccessManager dataAccessManager = Util.getDataAccessManager();
        return dataAccessManager.deactivateActiveSubscription(tenantId);
    }
}
