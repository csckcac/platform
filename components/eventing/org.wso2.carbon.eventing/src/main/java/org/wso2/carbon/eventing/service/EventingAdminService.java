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
package org.wso2.carbon.eventing.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Calendar;

import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.savan.SavanConstants;
import org.wso2.carbon.eventing.service.dto.SubscriptionDTO;
import org.wso2.carbon.eventing.subscription.SubscriptionManagerAdapter;
import org.wso2.eventing.EventingConstants;
import org.wso2.eventing.Subscription;
import org.wso2.eventing.SubscriptionManager;
import org.wso2.eventing.exceptions.EventException;

public class EventingAdminService {

	private static final Log log = LogFactory.getLog(EventingAdminService.class);

	/**
	 * 
	 * @param serviceName
	 * @return
	 * @throws AxisFault
	 */
	public String[] getValidSubscriptions(String serviceName) throws AxisFault {
		AxisService service = null;
		SubscriptionManager manager = null;
		Parameter parameter = null;
		ArrayList<String> subscribers = null;

		service = getAxisService(serviceName);
		parameter = service.getParameter(SavanConstants.SUBSCRIBER_STORE);

		if (parameter == null) {
			parameter = service.getParameter(EventingConstants.SUBSCRIPTION_MANAGER);
		}

		subscribers = new ArrayList<String>();

		List<Subscription> list = null;
		Subscription subscription = null;

		if (parameter == null) {
			manager = new SubscriptionManagerAdapter(serviceName);
		} else {
			manager = (SubscriptionManager) parameter.getValue();
		}

		try {
			list = manager.getSubscriptions();
			for (Iterator<Subscription> iterator = list.iterator(); iterator.hasNext();) {
				subscription = iterator.next();
				if (!checkExpired(subscription)) {
					subscribers.add(subscription.getId());
				}
			}
		} catch (EventException e) {
			String message = "Error while retrieving valid subscriptions";
			log.error(message, e);
			throw new AxisFault(message, e);
		}

		return subscribers.toArray(new String[subscribers.size()]);
	}

	/**
	 * 
	 * @param serviceName
	 * @return
	 * @throws AxisFault
	 */
	public String[] getExpiredSubscriptions(String serviceName) throws AxisFault {
		AxisService service = null;
		SubscriptionManager manager = null;
		Parameter parameter = null;
		ArrayList<String> subscribers = null;

		service = getAxisService(serviceName);
		parameter = service.getParameter(SavanConstants.SUBSCRIBER_STORE);

		if (parameter == null) {
			parameter = service.getParameter(EventingConstants.SUBSCRIPTION_MANAGER);
		}

		subscribers = new ArrayList<String>();

		List<Subscription> list = null;
		Subscription subscription = null;

		if (parameter == null) {
			manager = new SubscriptionManagerAdapter(serviceName);
		} else {
			manager = (SubscriptionManager) parameter.getValue();
		}

		try {
			list = manager.getSubscriptions();
			for (Iterator<Subscription> iterator = list.iterator(); iterator.hasNext();) {
				subscription = iterator.next();
				if (checkExpired(subscription)) {
					subscribers.add(subscription.getId());
				}
			}
		} catch (EventException e) {
			String message = "Error while retrieving expired subscriptions";
			log.error(message, e);
			throw new AxisFault(message, e);
		}

		return subscribers.toArray(new String[subscribers.size()]);
	}

	/**
	 * 
	 * @param serviceName
	 * @param subscriberId
	 * @return
	 * @throws AxisFault
	 */
	public SubscriptionDTO getSubscriptionDetails(String serviceName, String subscriberId)
			throws AxisFault {
		AxisService service = null;
		Parameter parameter = null;
		SubscriptionDTO details = null;
		SubscriptionManager manager = null;

		service = getAxisService(serviceName);
		parameter = service.getParameter(SavanConstants.SUBSCRIBER_STORE);

		if (parameter == null) {
			parameter = service.getParameter(EventingConstants.SUBSCRIPTION_MANAGER);
		}

		Subscription subscription = null;

		if (parameter == null) {
			manager = new SubscriptionManagerAdapter(serviceName);
		} else {
			manager = (SubscriptionManager) parameter.getValue();
		}
		
		try {
			subscription = manager.getSubscription(subscriberId);
		} catch (EventException e) {
			String message = "Error while retrieving subscription details for " + subscriberId;
			log.error(message, e);
			throw new AxisFault(message, e);
		}

		details = new SubscriptionDTO();

		if (subscription.getExpires() != null) {
			details.setSubscriptionEndingTime(subscription.getExpires().getTime());
            details.setSubscriptionEndString(ConverterUtil.convertToString(subscription.getExpires()));
		}
		details.setEpr(subscription.getAddressUrl());
		details.setDiliveryMode(subscription.getDeliveryMode());
		if (subscription.getSubscriptionData() != null) {
			String filterVal = (String) subscription.getSubscriptionData().getProperty("filter");
			if (filterVal != null) {
				details.setFilterValue(filterVal);
			}
			String dialect = (String) subscription.getSubscriptionData().getProperty("dialect");
			if (dialect != null) {
				details.setDialect(dialect);
			}
		}
		return details;
	}

	/**
	 * 
	 * @param serviceName
	 * @return
	 * @throws AxisFault
	 */
	private AxisService getAxisService(String serviceName) throws AxisFault {
		AxisConfiguration axisConfig = MessageContext.getCurrentMessageContext()
				.getConfigurationContext().getAxisConfiguration();
		return axisConfig.getServiceForActivation(serviceName);
	}

	/**
	 * Check the expiration of the subscription by validating the expiration with current time
	 * 
	 * @param subscription
	 * @return
	 */
	private boolean checkExpired(Subscription subscription) {
		if (subscription.getExpires() != null) {
			Calendar calendarCurrent = Calendar.getInstance();
			Calendar calendarSubscription = subscription.getExpires();
			if (calendarCurrent.before(calendarSubscription)) {
				return false;
			} else {
				return true;
			}
		} else {
			return false; // never expire subscription
		}
	}
}