	/**
	 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.dnsserverregistration;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Inet6Address;
import java.util.Enumeration;
import java.util.Stack;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dnsserverregistration.internal.ServerRegistrationComponent;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.base.ServerConfiguration;

/*
 * This class is used to register the carbon server with JmDNS(DNS-SD server).It will query via multicast
 * on the network for available services. Server Registration happens using serviceType(wso2server),
 * serverName(productName), port, text
 * eg:-{product}._wso2server._tcp.local
 *
 */

public class ServerRegistration {




		private static Log log = LogFactory.getLog(ServerRegistration.class);

	    private Stack<JmDNSRegistration> registrations = new Stack<JmDNSRegistration>();

		public String getServerConfigurationProperty() {
			try {
				ServerConfiguration serverConfig = ServerRegistrationComponent
						.getServerConfiguration();
				return serverConfig.getFirstProperty("Name");
			} catch (Exception e) {
				String msg = "ServerConfiguration Service not available";
				log.error(msg, e);
			}

			return null;
		}

		private int getTransportPort() {
			ConfigurationContextService configurationContextService = ServerRegistrationComponent
					.getConfigurationContextService();
			return CarbonUtils.getTransportPort(configurationContextService,
					"https");
		}

		public void registerServer() {
			// see log messages of JmDNS
			Logger logger = Logger.getLogger(JmDNS.class.getName());
			ConsoleHandler handler = new ConsoleHandler();
			logger.addHandler(handler);
			logger.setLevel(Level.FINER);
			handler.setLevel(Level.FINER);

			String serverName = getServerConfigurationProperty();
			String serviceType = ServerRegistrationConstants.SERVICE_TYPE;
			int port = getTransportPort();

			try {

				JmDNS jmdns;

				/* To overcome interface 0.0.0.0 issue */
				Enumeration<NetworkInterface> e = NetworkInterface
						.getNetworkInterfaces();
				while (e.hasMoreElements()) {
					NetworkInterface i = e.nextElement();

					Enumeration<InetAddress> ie = i.getInetAddresses();
					while (ie.hasMoreElements()) {
						InetAddress addr = ie.nextElement();
	                    // We don't support IPv6 yet.
						if (addr.isAnyLocalAddress() || addr.isMulticastAddress()
								|| addr.isLoopbackAddress() || addr instanceof Inet6Address) {
							continue;
						}
						jmdns = JmDNS.create(addr);
						jmdns.requestServiceInfo(serviceType, serverName);
						String text = jmdns.getHostName();
						ServiceInfo info = ServiceInfo.create(serviceType,
								serverName, port, text);
						jmdns.registerService(info);
	                    registrations.push(new JmDNSRegistration(info, jmdns));
					}
				}

			} catch (Exception e) {
				String msg = "Failed to register the server";
				log.error(msg, e);
			}
	    }

	    public void unregisterServer() {
	        while (!registrations.isEmpty()) {
	            JmDNSRegistration registration = registrations.pop();
	            //TODO: Make DNS Server Registration optional and add the unregistration functionality.
	            //registration.getJmdns().unregisterService(registration.getInfo());
	        }
	    }

	    private final class JmDNSRegistration {

	        private ServiceInfo info;

	        private JmDNS jmdns;

	        public JmDNSRegistration(ServiceInfo info, JmDNS jmdns) {
	            this.info = info;
	            this.jmdns = jmdns;
	        }

	        public ServiceInfo getInfo() {
	            return info;
	        }

	        public JmDNS getJmdns() {
	            return jmdns;
	        }
	    }

	}


