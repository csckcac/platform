package org.wso2.carbon.hadoop.security;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.Krb5TicketCacheFinder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hadoop.security.HadoopCarbonMessageContext;
import org.wso2.carbon.identity.authenticator.krb5.stub.types.Krb5AuthenticatorStub;

public class Krb5TicketCacheFinderImpl implements Krb5TicketCacheFinder {
    private Log log = LogFactory.getLog(Krb5TicketCacheFinderImpl.class);
    private Configuration conf;
	@Override
	public String getTenantTicketCache() {
		String ticketCachePath = null;
		try {
			HadoopCarbonMessageContext msgCtx = HadoopCarbonMessageContext.get();
			String cookie = msgCtx.getCookie();
			ConfigurationContext configCtx = msgCtx.getConfigurationContext();
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
			log.warn("AxisFault: "+e.getMessage());
		} catch (RemoteException e) { 
			log.warn("RemoteException: "+e.getMessage());
		} catch (Exception e) {
			log.warn("Exception: "+e.getMessage());
			e.printStackTrace();
		}
		return ticketCachePath;
	}
	@Override
	public Configuration getConf() {
		return this.conf;
	}
	@Override
	public void setConf(Configuration conf) {
		this.conf = conf;
	}
}
