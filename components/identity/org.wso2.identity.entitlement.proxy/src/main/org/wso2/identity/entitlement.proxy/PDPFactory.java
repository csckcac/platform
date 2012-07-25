package org.wso2.identity.entitlement.proxy;

import org.wso2.identity.entitlement.pdp.proxy.soap.SOAPProxy;

public class PDPFactory {

	public static AbstractPDPProxy getPDPProxy(PDPConfig config) throws Exception {

		String messageFormat = config.getMessageFormat();
		AbstractPDPProxy proxy = null;

		if (ProxyConstants.SOAP.equals(messageFormat)) {
			proxy = new SOAPProxy();
			proxy.setPDPConfig(config);
		} else if (ProxyConstants.JSON.equals(messageFormat)) {

		} else if (ProxyConstants.THRIFT.equals(messageFormat)) {

		}

		return proxy;
	}

}
