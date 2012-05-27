package org.wso2.carbon.endpoint.uep;

import org.apache.axis2.AxisFault;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.endpoints.AbstractEndpoint;
import org.wso2.carbon.endpoint.EndpointAdminException;
import org.wso2.carbon.endpoint.uep.util.UEndpointUtil;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint;

/**
 * ESB specific UDefaultEndpoint class,extended from synapse Abstractendpoint.
 * 
 */
public class UdefaultEndpoint extends AbstractEndpoint {

	/**
	 * The registry location of the uep configuration,where it is stored
	 * Eg:conf:/repositoryuepTestEP.xml
	 */
	private String uepConfigurationPath;

	public UdefaultEndpoint() {

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
			log.debug("Executing UdefaultEndpoint Sender ");
		}
		if (getParentEndpoint() == null && !readyToSend()) {
			informFailure(synMessageContext, SynapseConstants.ENDPOINT_ADDRESS_NONE_READY,
			              "Currently , UdefaultEndpoint : " + getContext());
		} else {
			try {
				UDefaultEndpointDefinition defaultEPDefinition = new UDefaultEndpointDefinition();
				UEndpointUtil uepUtil = new UEndpointUtil();

				UnifiedEndpoint unifiedEndpoint = uepUtil.getUnifiedEndpoint(uepConfigurationPath);
				defaultEPDefinition.setEndpointDefinition(unifiedEndpoint);

				UEndpointAxis2MEPClient.send(unifiedEndpoint, getDefinition(), synMessageContext);
			} catch (AxisFault e) {
				log.error("Error in executing the message", e);
			}
		}

	}
}
