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
package org.wso2.carbon.billing.core.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.billing.core.BillingEngineContext;
import org.wso2.carbon.billing.core.BillingException;
import org.wso2.carbon.billing.core.BillingHandler;
import org.wso2.carbon.billing.core.dataobjects.Customer;
import org.wso2.carbon.billing.core.dataobjects.Invoice;
import org.wso2.carbon.billing.core.dataobjects.Item;
import org.wso2.carbon.billing.core.dataobjects.Payment;
import org.wso2.carbon.billing.core.dataobjects.Subscription;
import org.wso2.carbon.billing.core.internal.Util;
import org.wso2.carbon.rule.core.Session;
import org.wso2.carbon.rule.server.RuleEngine;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.SessionDescription;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Runs the billing rules against each subscription
 * At the moment only the subscription fee is calculated by
 * these rules. Overusage charges are calculated by InvoiceCalculationHandler
 */
public class RuleHandler implements BillingHandler {

    private static Log log = LogFactory.getLog(RuleHandler.class);
    RuleEngine ruleEngine;
    Session session;

    public void init(Map<String, String> handlerConfig) throws BillingException {
        ruleEngine = Util.getRuleServerManagerService().createRuleEngine(
                        Thread.currentThread().getContextClassLoader());
        
        String ruleFile = handlerConfig.get("file");
        ruleFile = CarbonUtils.getCarbonConfigDirPath() + File.separator + ruleFile;
        RuleSetDescription ruleSetDescription = new RuleSetDescription();
        
        try {
            ruleSetDescription.setRuleSource(new FileInputStream(ruleFile));
        } catch (FileNotFoundException e) {
            String msg = "file not found. file name: " + ruleFile + ".";
            throw new BillingException(msg, e);
        }
        
        // ruleSetDescription.setBindURI("file:" + ruleFile);
        String uri = ruleEngine.addRuleSet(ruleSetDescription);
        SessionDescription sessionDescription = new SessionDescription();
        sessionDescription.setSessionType(SessionDescription.STATELESS_SESSION);
        sessionDescription.setRuleSetURI(uri);

        session = ruleEngine.createSession(sessionDescription);
    }

    public void execute(BillingEngineContext handlerContext) throws BillingException {
        List<Subscription> subscriptions = handlerContext.getSubscriptions();

        List<Object> rulesInput = new ArrayList<Object>();
        Set<Integer> customerSet = new HashSet<Integer>();

        for (Subscription subscription : subscriptions) {
            // add the subscriptions
            rulesInput.add(subscription);

            // add the customers
            Customer customer = subscription.getCustomer();
            if (!customerSet.contains(customer.getId())) {
                customerSet.add(customer.getId());
                rulesInput.add(customer);

                // add the invoice too
                Invoice invoice = customer.getActiveInvoice();
                rulesInput.add(invoice);

                // add each purchases
                List<Payment> payments = invoice.getPayments();
                if (payments != null) {
                    for (Payment payment : payments) {
                        rulesInput.add(payment);
                    }
                }
            }

            // add the items
            Item item = subscription.getItem();
            rulesInput.add(item);

            List<? extends Item> children = item.getChildren();
            if (children != null) {
                for (Item subItem : item.getChildren()) {
                    rulesInput.add(subItem);
                }
            }
        }

        session.execute(rulesInput);
        log.info("Rule execution phase completed.");
    }
}
