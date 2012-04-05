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
package org.wso2.carbon.eventing.subscription;

import java.util.*;

import org.apache.savan.eventing.subscribers.EventingSubscriber;
import org.wso2.eventing.Subscription;
import org.wso2.eventing.SubscriptionManager;
import org.wso2.eventing.Event;
import org.wso2.eventing.exceptions.EventException;

/**
 * 
 * 
 */
public class SubscriptionManagerAdapter extends RegistryBasedSubscriberStore
		implements
			SubscriptionManager {

	public SubscriptionManagerAdapter() {
		super();
	}

	public SubscriptionManagerAdapter(String serviceName) {
		setServiceName(serviceName);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Subscription> getAllSubscribers() throws EventException {
		return getSubscribers();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Subscription> getSubscribers() throws EventException {
		List<Subscription> subscriptions = null;

		subscriptions = new ArrayList<Subscription>();

		for (Iterator iter = retrieveAllSubscribers(); iter.hasNext();) {
			EventingSubscriber subscriber = (EventingSubscriber) iter.next();
			if (subscriber != null && subscriber.getId() != null) {
				subscriptions.add(getSubscription(subscriber));
			}
		}
		return subscriptions;
	}

	/**
	 * {@inheritDoc}
	 */
	public Subscription getSubscription(String subscriptionID) throws EventException {
		return getSubscription((EventingSubscriber) retrieve(subscriptionID));
	}

    public Subscription getStatus(String s) throws EventException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addProperty(String s, String s1) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getPropertyValue(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection getPropertyNames() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List getStaticSubscriptions() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void init() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
	 * {@inheritDoc}
	 */
	public boolean renew(Subscription subscription) throws EventException {
		// TODO Auto-generated method stub
		return true;
	}

    public List getSubscriptions() throws EventException {
        return getSubscribers();
    }

    public List getAllSubscriptions() throws EventException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List getMatchingSubscriptions(Event event) throws EventException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
	 * {@inheritDoc}
	 */
	public String subscribe(Subscription subscription) throws EventException {
		// TODO Auto-generated method stub
		return null;
	}

    public boolean unsubscribe(String s) throws EventException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
	 * {@inheritDoc}
	 */
	public boolean unsubscribe(Subscription subscription) throws EventException {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Subscription getStatus(Subscription subscription) throws EventException {
		return null;
	}

	/**
	 * 
	 * @param subscriber
	 * @return
	 */
	private Subscription getSubscription(EventingSubscriber subscriber) {
		Subscription subscription = null;
		Calendar calender = null;

		subscription = new Subscription();
		subscription.setId(subscriber.getId().toASCIIString());

		if (subscriber.getDelivery() != null) {
			subscription.setDeliveryMode(subscriber.getDelivery().getDeliveryMode());
			if (subscriber.getDelivery().getDeliveryEPR() != null) {
				subscription.setAddressUrl(subscriber.getDelivery().getDeliveryEPR().getAddress());
				subscription.setEndpointUrl(subscriber.getDelivery().getDeliveryEPR().getAddress());
			}
		}

		if (subscriber.getSubscriptionEndingTime() != null) {
			calender = Calendar.getInstance();
			calender.setTime(subscriber.getSubscriptionEndingTime());
			subscription.setExpires(calender);
		}

		return subscription;
	}

}