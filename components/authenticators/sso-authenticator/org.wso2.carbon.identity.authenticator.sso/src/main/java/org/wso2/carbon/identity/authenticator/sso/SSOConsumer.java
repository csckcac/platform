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
package org.wso2.carbon.identity.authenticator.sso;

import java.net.URL;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.util.Loader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.authenticator.sso.stub.SSOServiceStub;

public class SSOConsumer {

	private SSOServiceStub service = null;
	private static final Log log = LogFactory.getLog(SSOConsumer.class);

	public SSOConsumer(String epr) throws AxisFault {
		service = new SSOServiceStub(epr);
		Options options = null;
		TransportOutDescription transportOut = null;
		options = service._getServiceClient().getOptions();
		transportOut = getTranport(epr);
		if (transportOut != null) {
			options.setTransportOut(transportOut);
		}
	}

	/**
	 * 
	 * @param userName
	 * @param password
	 * @return
	 */
	public String autneticate(String userName, String password) {
		try {
			return service.authenticate(userName, password);
		} catch (Exception e) {
			// User should be able to proceed whether the SSO service failed or not.
			if (log.isDebugEnabled()) {
				log.debug("SSO enabled authentication failed.", e);
			}
			return null;
		}
	}

	/**
	 * 
	 * @param userName
	 * @param sessionId
	 * @return
	 */
	public boolean isAuthenticated(String userName, String sessionId) {
		try {
			return service.isAuthenticated(userName, sessionId);
		} catch (Exception e) {
			// User should be able to proceed whether the SSO service failed or not.
			if (log.isDebugEnabled()) {
				log.debug("SSO enabled authentication failed.", e);
			}
			return false;
		}
	}

	/**
	 * 
	 * @param userName
	 * @param sessionId
	 */
	public void signout(String userName, String sessionId) {
		try {
			service.signOut(userName, sessionId);
		} catch (Exception e) {
			// User should be able to proceed whether the SSO service failed or not.
			if (log.isDebugEnabled()) {
				log.debug("SSO enabled sign out failed.", e);
			}
		}
	}

	// TODO: Right now we have an issue with HttpCoreNIOSender - which is used in ESB.
	// This is a work-around for that.
	private TransportOutDescription getTranport(String epr) {
		TransportOutDescription transport = null;

		try {
			URL url = new URL(epr);
			String protocol = url.getProtocol();
			if ("http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol))  {
				transport = new TransportOutDescription(protocol);
				Class senderClass;
				senderClass = Loader
						.loadClass("org.apache.axis2.transport.http.CommonsHTTPTransportSender");
				TransportSender sender = (TransportSender) senderClass.newInstance();
				transport.setSender(sender);
				transport.addParameter(new Parameter("PROTOCOL", "HTTP/1.1"));
				transport.addParameter(new Parameter("Transfer-Encoding", "chunked"));
				transport.addParameter(new Parameter("OmitSOAP12Action", "true"));
			}
		} catch (Exception e) {
			return null;
		}
		return transport;
	}
}
