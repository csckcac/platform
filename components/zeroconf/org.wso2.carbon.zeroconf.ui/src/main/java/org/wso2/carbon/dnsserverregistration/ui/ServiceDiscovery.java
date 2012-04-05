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
package org.wso2.carbon.dnsserverregistration.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dnsserverregistration.ui.bean.ServiceDiscoveryBean;

public class ServiceDiscovery {

	private static Log log = LogFactory.getLog(ServiceDiscovery.class);

	/* discover the services which are registered in a network */
	class serviceListener implements ServiceListener {
		public void serviceAdded(ServiceEvent event) {
			event.getDNS().requestServiceInfo(event.getType(), event.getName());

			System.out.println("Service added   : " + event.getName() + "." + event.getType());
		}

		public void serviceRemoved(ServiceEvent event) {
			System.out.println("Service removed : " + event.getName() + "." + event.getType());
		}

		public void serviceResolved(ServiceEvent event) {
			System.out.println("Service resolved: " + event.getInfo());
		}
	}

	public void discoverServices() {
		/* to see log messages of JmDNS */
		Logger logger = Logger.getLogger(JmDNS.class.getName());
		ConsoleHandler handler = new ConsoleHandler();
		logger.addHandler(handler);
		logger.setLevel(Level.FINER);
		handler.setLevel(Level.FINER);

		try {
			JmDNS jmdns = JmDNS.create();
			jmdns.addServiceListener(ServerTypeConstants.SERVICE_TYPE, new serviceListener());
		} catch (Exception e) {
			log.error("Could not discover the services",e);
		}

	}

	/* list the services available in the network */
	public ServiceDiscoveryBean listwso2Services() {
		List<String> serviceList = new ArrayList<String>();
		List<String> urlList = new ArrayList<String>();
		try {
			JmDNS jmdns = JmDNS.create();
			ServiceDiscoveryBean jmdnsBean = new ServiceDiscoveryBean();
			while (true) {
				ServiceInfo[] infos = jmdns.list(ServerTypeConstants.SERVICE_TYPE);

				while (infos.length == 0) {
					try {
						infos = jmdns.list(ServerTypeConstants.SERVICE_TYPE);
						Thread.sleep(10);
					} catch (InterruptedException e) {
						log.error("Could not retrive server list",e);
					}
				}
				for (int i = 0; i < infos.length; i++) {
					serviceList.add(infos[i].getName());
					urlList.add(infos[i].getURL(ServerTypeConstants.SERVICE_PROTOCOL));
				}
				jmdnsBean.setServicesToAdd(serviceList);
				jmdnsBean.setURLOfTheServer(urlList);
				return jmdnsBean;
			}
		} catch (IOException e) {
			log.error("Could not list the available servers",e);
		}
		return null;
	}
}
