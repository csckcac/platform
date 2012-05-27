package org.wso2.carbon.endpoint.uep;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.endpoints.AbstractEndpoint;
import org.wso2.carbon.endpoint.uep.util.UEndpointUtil;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint;

/**
 * ESB specific UAddressEndpoint class,extended from synapse Abstractendpoint.
 * 
 */
public class UAddressEndpoint extends AbstractEndpoint {

	/**
	 * The registry location of the uep configuration,where it is stored
	 * Eg:conf:/repositoryuepTestEP.xml
	 */
	private String uepConfigurationPath;

	private static final Log log = LogFactory.getLog(UAddressEndpoint.class);

	public UAddressEndpoint() {

	}

	/**
	 * Getter and Setter of the parameter which we define at the classendpoint
	 * configuration.
	 * eg:<parameter
	 * name="uepConfigurationPath">conf:/repositoryuepTestEP.xml</parameter>
	 * 
	 * @param uepConfiguration
	 */
	public void setUepConfigurationPath(String uepConfigurationPath) {
		this.uepConfigurationPath = uepConfigurationPath;
	}

	public String getUepConfigurationPath() {
		return uepConfigurationPath;
	}

	/**
	 * Override the <code>AbstractEndpoint.send()</code>. Use the UEP handler to
	 * get endpoint specific
	 * Configurations
	 * 
	 */
	public void send(MessageContext synMessageContext) {
		if (log.isDebugEnabled()) {
			log.debug("Executing UAddressEndpoint Sender ");
		}

		try {
			UAddressEndpointDefinition addressEPDefinition = new UAddressEndpointDefinition();
			UEndpointUtil uepUtil = new UEndpointUtil();

			UnifiedEndpoint unifiedEndpoint = uepUtil.getUnifiedEndpoint(uepConfigurationPath);
			addressEPDefinition.setEndpointDefinition(unifiedEndpoint);
			UEndpointAxis2MEPClient.send(unifiedEndpoint, getDefinition(), synMessageContext);
		} catch (AxisFault e) {
			log.error("Error in executing the message", e);
		}
	}

}
