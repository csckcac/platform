package org.wso2.carbon.endpoint.uep;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.endpoints.AbstractEndpoint;
import org.wso2.carbon.endpoint.EndpointAdminException;
import org.wso2.carbon.endpoint.uep.util.UEndpointUtil;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint;

/**
 * ESB specific UWSDLEndpoint class,extended from synapse Abstractendpoint.
 * 
 */
public class UWSDLEndpoint extends AbstractEndpoint {
	/**
	 * The registry location of the uep configuration,where it is stored
	 * Eg:conf:/repositoryuepTestEP.xml
	 */
	private String uepConfigurationPath;
	private OMElement wsdlDoc;
	private String serviceName;
	private String portName;

	private static final Log log = LogFactory.getLog(UWSDLEndpoint.class);

	public UWSDLEndpoint() {

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

	public void setWsdlDoc(OMElement wsdlDoc) {
		this.wsdlDoc = wsdlDoc;
	}

	public OMElement getWsdlDoc() {
		return wsdlDoc;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public String getPortName() {
		return portName;
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
			UWSDLEndpointDefinition wsdlEPDefinition = new UWSDLEndpointDefinition();
			UEndpointUtil uepUtil = new UEndpointUtil();

			UnifiedEndpoint unifiedEndpoint = uepUtil.getUnifiedEndpoint(uepConfigurationPath);
			wsdlEPDefinition.setEndpointDefinition(unifiedEndpoint);
			UEndpointAxis2MEPClient.send(unifiedEndpoint, getDefinition(), synMessageContext);
		} catch (AxisFault e) {
			log.error("Error in executing the message", e);
		}
	}
}
