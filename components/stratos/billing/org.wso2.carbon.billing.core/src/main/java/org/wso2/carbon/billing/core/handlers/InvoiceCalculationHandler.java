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
import org.wso2.carbon.billing.core.BillingConstants;
import org.wso2.carbon.billing.core.BillingEngineContext;
import org.wso2.carbon.billing.core.BillingException;
import org.wso2.carbon.billing.core.BillingHandler;
import org.wso2.carbon.billing.core.dataobjects.*;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates overusage charges, CF value, total payments etc.
 */
public class InvoiceCalculationHandler implements BillingHandler {

    Log log = LogFactory.getLog(InvoiceCalculationHandler.class);

    public void init(Map<String, String> handlerConfig) throws BillingException {
        // nothing to initialize
    }

    public void execute(BillingEngineContext handlerContext) throws BillingException {
        // calculate the bill
        calculateInvoice(handlerContext);
    }

    private void calculateInvoice(BillingEngineContext handlerContext) throws BillingException {
        List<Subscription> subscriptions = handlerContext.getSubscriptions();
        Map<Integer, Invoice> invoiceMap = new HashMap<Integer, Invoice>();
       
        for (Subscription subscription : subscriptions) {
            Customer customer = subscription.getCustomer();

            // invoice should be already set..
            Invoice invoice = customer.getActiveInvoice();
            Cash totalCost = invoice.getTotalCost();
            if (totalCost == null) {
                totalCost = new Cash("$0");
            }
        
            Item item = subscription.getItem();
            //prorate the subscription cost
            //prorateItemCosts(item, invoice, subscription);
            calculateItemCost(item, invoice, subscription);
            Cash itemCost = getItemCost(item);
            totalCost = Cash.add(totalCost, itemCost);
            invoice.setTotalCost(totalCost);
            if (invoiceMap.get(customer.getId()) == null) {
                invoiceMap.put(customer.getId(), invoice);
            }
        }

        // from the invoice set we are calculating the payments       purchase orders
        for (Invoice invoice : invoiceMap.values()) {
            Cash totalPayment = invoice.getTotalPayment();
            if (totalPayment == null) {
                totalPayment = new Cash("$0");
            }
            List<Payment> payments = invoice.getPayments();
            if (payments != null) {
                for (Payment payment : payments) {
                    Cash paymentCash = payment.getAmount();
                    totalPayment = Cash.add(paymentCash, totalPayment);
                }
            }
            invoice.setTotalPayment(totalPayment);

            // setting the carried forward
            Cash boughtForward = invoice.getBoughtForward();
            if (boughtForward == null) {
                boughtForward = new Cash("$0");
            }
            Cash totalCost = invoice.getTotalCost();
            Cash carriedForward = Cash.subtract(Cash.add(boughtForward, totalCost), totalPayment);
            invoice.setCarriedForward(carriedForward);
        }

        log.info("Invoice calculation phase completed. " + invoiceMap.size() + " invoices were calculated");
    }

    private Cash getItemCost(Item item) throws BillingException {
        Cash itemCost = item.getCost();
        if (itemCost == null) {
            itemCost = new Cash("$0");
        }
        if (item.getChildren() != null) {
            // and iterate through all the item children
            for (Item subItem : item.getChildren()) {
                Cash subItemCost = subItem.getCost();
                if (subItemCost != null) {
                    itemCost = Cash.add(itemCost, subItemCost);
                }
            }
        }
        return itemCost;
    }

    private void calculateItemCost(Item item, Invoice invoice, Subscription subscription) throws BillingException {
        if(item.getChildren()!=null){
            for(Item subItem : item.getChildren()){
                if((BillingConstants.BANDWIDTH_SUBITEM.equals(subItem.getName()) ||
                    BillingConstants.STORAGE_SUBITEM.equals(subItem.getName())) && subscription.isActive()){
                    calculateOverUseCharges(item, subItem, subscription);
                }else if(BillingConstants.SUBSCRIPTION_SUBITEM.equals(subItem.getName())){
                    prorateItemCosts(subItem, invoice, subscription);
                }
            }
        }
    }

    private void calculateOverUseCharges(Item item, Item subItem, Subscription subscription) throws BillingException {
        //calculating cost for bandwidth overuse
        if(BillingConstants.BANDWIDTH_SUBITEM.equals(subItem.getName())){
            long bandwidthUsage = subscription.getCustomer().getTotalBandwidth()/(1024 * 1024L);
            long bandwidthOveruse = 0;
            if(bandwidthUsage > item.getBandwidthLimit()){
                bandwidthOveruse = bandwidthUsage - item.getBandwidthLimit();
                subItem.setCost(item.getBandwidthOveruseCharge().multiply(bandwidthOveruse));
            }
            StringBuffer description = new StringBuffer();
            description.append(subItem.getDescription());
            description.append(": ").append(bandwidthOveruse).append("MB");
            subItem.setDescription(description.toString());
        //calculating cost for storage overuse    
        }else if(BillingConstants.STORAGE_SUBITEM.equals(subItem.getName())){
            long storageUsage = subscription.getCustomer().getTotalStorage()/(1024 * 1024L);
            long storageOveruse = 0;
            if(storageUsage > item.getResourceVolumeLimit()){
                storageOveruse = storageUsage - item.getResourceVolumeLimit();
                subItem.setCost(item.getResourceVolumeOveruseCharge().multiply(storageOveruse));
            }
            StringBuffer description = new StringBuffer();
            description.append(subItem.getDescription());
            description.append(": ").append(storageOveruse).append("MB");
            subItem.setDescription(description.toString());
        }

    }

    //by looking at the start and end dates of the invoice, subscription item's cost is interpolated
    private void prorateItemCosts(Item subItem, Invoice invoice, Subscription subscription) throws BillingException {
        long milisecondsPerDay = 24*60*60*1000L;
        NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);

        long period;
        long days;

        if(subscription.isActive()){
            if(subscription.getActiveSince().before(invoice.getStartDate())){
                period = invoice.getEndDate().getTime() - invoice.getStartDate().getTime();
            }else{
                period = invoice.getEndDate().getTime() - subscription.getActiveSince().getTime();
            }
        }else{ 
            if(subscription.getActiveSince().before(invoice.getStartDate())){
                period = subscription.getActiveUntil().getTime() - invoice.getStartDate().getTime();
            }else{
                period = subscription.getActiveUntil().getTime() - subscription.getActiveSince().getTime();
            }
        }

        //I am considering 28 days or more as a complete month
        days = period/milisecondsPerDay;
        if(days<28){
            double multiplyingFactor = (double)days/30;
            multiplyingFactor = Double.parseDouble(nf.format(multiplyingFactor));

            if(subItem.getCost()!=null){
                subItem.setCost(subItem.getCost().multiply(multiplyingFactor));
            }
        }


    }
}
