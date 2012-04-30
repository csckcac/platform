package org.wso2.carbon.hadoop.security;

import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.security.Krb5TicketCacheFinder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.authenticator.krb5.stub.types.Krb5AuthenticatorStub;

public class Krb5TicketCacheFinderImpl implements Krb5TicketCacheFinder {
    private Log log = LogFactory.getLog(Krb5TicketCacheFinderImpl.class);
	@Override
	public String getTenantTicketCache() {
		String ticketCachePath = null;
		try {
			MessageContext msgCtx = MessageContext.getCurrentMessageContext();
			ConfigurationContext configCtx = msgCtx.getConfigurationContext();
			HttpServletRequest request = (HttpServletRequest) msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
			String cookie = request.getHeader(HTTPConstants.COOKIE_STRING);
			String serviceEPR = "https://localhost:9443/services/" + "Krb5Authenticator";
			Krb5AuthenticatorStub stub = new Krb5AuthenticatorStub(configCtx, serviceEPR);
			ServiceClient client = stub._getServiceClient();
			Options options = client.getOptions();
			options.setManageSession(true);
			if (cookie != null) {
				options.setProperty(HTTPConstants.COOKIE_STRING, cookie);
			}
			ticketCachePath = stub.getTicketCache();
		} catch (AxisFault e) {
			log.warn("AxisFault: "+e.getStackTrace());
		} catch (RemoteException e) { 
			log.warn("RemoteException: "+e.getStackTrace());
		} catch (Exception e) {
			log.warn("Exception: "+e.getStackTrace());
		}
		return ticketCachePath;
	}
}
