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
package org.wso2.carbon.eventing.eventsource.ui;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.eventing.eventsource.stub.EventSourceAdminServiceStub;
import org.wso2.carbon.eventing.eventsource.stub.types.carbon.EventSourceDTO;

public class EventingSourceAdminClient {

	private static final Log log = LogFactory.getLog(EventingSourceAdminClient.class);

	private EventSourceAdminServiceStub stub;

	/**
	 * 
	 * @param cookie
	 * @param backendServerURL
	 * @param configCtx
	 * @throws AxisFault
	 */
	public EventingSourceAdminClient(String cookie, String backendServerURL,
			ConfigurationContext configCtx) throws AxisFault {
		String serviceURL = backendServerURL + "EventSourceAdminService";
		stub = new EventSourceAdminServiceStub(configCtx, serviceURL);
		ServiceClient client = stub._getServiceClient();
		Options option = client.getOptions();
		option.setManageSession(true);
		option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
	}

	/**
	 * 
	 * @return
	 * @throws AxisFault
	 */
	public EventSourceDTO[] getEventSources() throws AxisFault {

		try {
			return stub.getEventSources();
		} catch (RemoteException e) {
			handleException("Error while retreiving the eventsources", e);
		}

		return null;
	}
	
	/**
	 * 
	 * @return
	 * @throws AxisFault
	 */
	public String getEventSourceNames() throws AxisFault {

		EventSourceDTO[] eventsources = null;
		String names = null;

		try {
			eventsources = stub.getEventSources();

			if (eventsources != null) {
				for (int i = 0; i < eventsources.length; i++) {
					if (names != null) {
						names = names + eventsources[i].getName() + "%";
					} else {
						names = eventsources[i].getName() + "%";
					}
				}
			}

		} catch (RemoteException e) {
			handleException("Error while retreiving the eventsources", e);
		}

		return names;
	}

	/**
	 * Get EventSource by name
	 * 
	 * @param eventSourceName
	 * @return
	 * @throws AxisFault
	 */
	public EventSourceDTO getEventSource(String eventSourceName) throws AxisFault {

		try {
			return stub.getEventSource(eventSourceName);
		} catch (RemoteException e) {
			handleException("Error while retreiving the eventsource " + eventSourceName, e);
		}

		return null;
	}

	/**
	 * 
	 * @param eventSourceName
	 * @return
	 */
	public boolean isEventSourceExisting(String eventSourceName) {
		EventSourceDTO eventsource = null;
		try {
			eventsource = stub.getEventSource(eventSourceName);
			if (eventsource != null)
				return true;
		} catch (RemoteException e) {
			return false;
		}

		return false;
	}

	/**
	 * 
	 * @param eventsource
	 * @throws AxisFault
	 */
	public void addEventSource(EventSourceDTO eventsource) throws AxisFault {
		try {
			stub.addEventSource(eventsource);
		} catch (RemoteException e) {
			handleException("Error while adding the eventsource " + eventsource.getName(), e);
		}
	}

	/**
	 * 
	 * @param eventsource
	 * @throws AxisFault
	 */
	public void saveEventSource(EventSourceDTO eventsource) throws AxisFault {
		try {
			stub.saveEventSource(eventsource);
		} catch (RemoteException e) {
			handleException("Error while adding the eventsource " + eventsource.getName(), e);
		}
	}

	/**
	 * 
	 * @throws AxisFault
	 */
	public void removeEventSource(String name) throws AxisFault {
		try {
			stub.removeEventSource(name);
		} catch (RemoteException e) {
			handleException("Error while removing the eventsource " + name, e);
		}
	}

	/**
	 * 
	 * @param msg
	 * @param e
	 * @throws AxisFault
	 */
	private void handleException(String msg, Exception e) throws AxisFault {
		log.error(msg, e);
		throw new AxisFault(msg, e);
	}
}