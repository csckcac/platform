package org.wso2.carbon.endpoint.uep;

import java.util.Map;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.w3c.dom.Document;
import org.wso2.carbon.endpoint.EndpointAdminException;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint;

/*****
 * <wsdl [uri="wsdl-uri"] service="qname" port/endpoint="qname">
 * <wsdl:definition>...</wsdl:definition>?
 * <wsdl20:description>...</wsdl20:description>?
 * <enableRM [policy="key"]/>?
 * <enableSec [policy="key"]/>?
 * <enableAddressing/>?
 * 
 * <timeout>
 * <duration>timeout duration in milliseconds</duration>
 * <action>discard|fault</action>
 * </timeout>?
 * 
 * <markForSuspension>
 * [<errorCodes>xxx,yyy</errorCodes>]
 * <retriesBeforeSuspension>m</retriesBeforeSuspension>
 * <retryDelay>d</retryDelay>
 * </markForSuspension>
 * 
 * <suspendOnFailure>
 * [<errorCodes>xxx,yyy</errorCodes>]
 * <initialDuration>n</initialDuration>
 * <progressionFactor>r</progressionFactor>
 * <maximumDuration>l</maximumDuration>
 * </suspendOnFailure>
 * </wsdl>
 *****/

/**
 * WSDLEndpointFactory class to construct wsdl endpoint from unifiedendpoint
 * definition
 * 
 * @param uep
 * @return UEndpoint
 */
public class UWSDLEndpointDefinition extends UDefaultEndpointDefinition {
	private UnifiedEndpoint unifiedEndpoint;

	private static final Log log = LogFactory.getLog(UWSDLEndpointDefinition.class);

	public UWSDLEndpointDefinition() {

	}

	@Override
	protected  void setEndpointDefinition(UnifiedEndpoint unifiedEndpoint) {
		this.unifiedEndpoint = unifiedEndpoint;	
		setUWSDLEndpointDefinition();
	}

	/**
	 * Get the WSDLEndpoint from the specified unified endpoint
	 * 
	 * @return UEndpoint
	 * @throws EndpointAdminException 
	 */

	public  void setUWSDLEndpointDefinition() {

		EndpointDefinition endpointDefinition = null;
		UWSDLEndpoint uWSDLEndpoint = null;

		if (unifiedEndpoint != null) {
	
			uWSDLEndpoint= new UWSDLEndpoint();
		
			/** get WSDL Definitions */
			if (unifiedEndpoint.getWsdl11Definitions() != null) {
				endpointDefinition =
				                     populateEndpointDefinitionFromInlineWSDL(uWSDLEndpoint,
				                                                              unifiedEndpoint);

		

				if (unifiedEndpoint.getUepId() != null) {
					uWSDLEndpoint.setName(unifiedEndpoint.getUepId());
				}
				getMonitoringConfig(endpointDefinition, unifiedEndpoint.getUepId(), unifiedEndpoint);
				getQoSConfig(endpointDefinition, unifiedEndpoint);
				getMsgFormatConfig(endpointDefinition, unifiedEndpoint);
				getTimeoutConfig(endpointDefinition, unifiedEndpoint);
				getMarkForSuspensionConfig(endpointDefinition, unifiedEndpoint);
				
				// set the endpoint definition
				uWSDLEndpoint.setDefinition(endpointDefinition);
			}
		}
	
	}

	private EndpointDefinition populateEndpointDefinitionFromInlineWSDL(UWSDLEndpoint uEndpoint,
	                                                                    UnifiedEndpoint unifiedEndpoint)  {

		WSDLFactory fac;
		Definition wsdlDefinition = null;
		OMElement wsdl11Doc = unifiedEndpoint.getWsdl11Definitions();
		try {
			OMFactory doomFactory = DOOMAbstractFactory.getOMFactory();
			StAXOMBuilder doomBuilder =
			                            new StAXOMBuilder(doomFactory,
			                                              wsdl11Doc.getXMLStreamReader());
			Document domDoc = (org.w3c.dom.Document) doomBuilder.getDocument();

			fac = WSDLFactory.newInstance();
			WSDLReader reader = fac.newWSDLReader();
			wsdlDefinition = reader.readWSDL(null, domDoc);
		} catch (WSDLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return createEndpointDefinitionFromInlineWSDL(uEndpoint, unifiedEndpoint, wsdlDefinition);

	}

	private EndpointDefinition createEndpointDefinitionFromInlineWSDL(UWSDLEndpoint uEndpoint,
	                                                                  UnifiedEndpoint unifiedEndpoint,
	                                                                  Definition wsdlDefinition) {
		EndpointDefinition endpointDefinition = new EndpointDefinition();

		endpointDefinition.setAddressingOn(unifiedEndpoint.isAddressingEnabled());
		endpointDefinition.setSecurityOn(unifiedEndpoint.isSecurityEnabled());
		endpointDefinition.setReliableMessagingOn(unifiedEndpoint.isRMEnabled());
		endpointDefinition.setUseSeparateListener(unifiedEndpoint.isSeparateListener());

		String serviceURL = null;
		// get soap version from wsdl port and update endpoint definition below
		// so that correct soap version is used when endpoint is called
		String format = null;
		assert wsdlDefinition != null;

		// get the service name and port name, GET ONLY VERY FIST
		// SERVICE AND IT"S PORT NUMBER..THIS IS BECAUSE OF THE ISSUE AT
		// UEP SCHEMA. THERE IS NO ANY SCHEMA DEFINITION TO TAKE INPUT
		// FROM THE USER TO IDENTIFY THE SERVICE+PORT NUMBER
		Map serviceMaps = wsdlDefinition.getServices();
		if (serviceMaps.size() > 0) {
			Service service = (Service) serviceMaps.get(1); // take the
			                                                // first
			                                                // service
			                                                // defined
			QName serviceQName = service.getQName();
			String serviceName = serviceQName.getLocalPart();

			uEndpoint.setServiceName(serviceName);

			Map portMaps = service.getPorts();
			// check this..do we need this, this will be handled by handler?
			if (portMaps.size() > 0) {
				Port port = (Port) portMaps.get(1); // take the first
				uEndpoint.setPortName(port.getName()); // port
				if (port != null) {
					for (Object obj : port.getExtensibilityElements()) {
						if (obj instanceof SOAPAddress) {
							SOAPAddress address = (SOAPAddress) obj;
							serviceURL = address.getLocationURI();
							format = SynapseConstants.FORMAT_SOAP11;
							break;
						} else if (obj instanceof SOAP12Address) {
							SOAP12Address address = (SOAP12Address) obj;
							serviceURL = address.getLocationURI();
							format = SynapseConstants.FORMAT_SOAP12;
							break;
						} else if (obj instanceof HTTPAddress) {
							HTTPAddress address = (HTTPAddress) obj;
							serviceURL = address.getLocationURI();
							format = SynapseConstants.FORMAT_REST;
							Binding binding = port.getBinding();
							if (binding == null) {
								continue;
							}
							for (Object portElement : binding.getExtensibilityElements()) {
								if (portElement instanceof HTTPBinding) {
									HTTPBinding httpBinding = (HTTPBinding) portElement;
									String verb = httpBinding.getVerb();
									if (verb == null || "".equals(verb)) {
										continue;
									}
									if ("POST".equals(verb.toUpperCase())) {
										format = SynapseConstants.FORMAT_REST;
									} else if ("GET".equals(verb.toUpperCase())) {
										format = SynapseConstants.FORMAT_GET;
									}
								}
							}
						}
					}
				}
			}

		}

		if (serviceURL != null) {
			endpointDefinition.setAddress(serviceURL);
			if (SynapseConstants.FORMAT_SOAP11.equals(format)) {
				endpointDefinition.setForceSOAP11(true);
			} else if (SynapseConstants.FORMAT_SOAP12.equals(format)) {
				endpointDefinition.setForceSOAP12(true);
			} else if (SynapseConstants.FORMAT_REST.equals(format)) {
				endpointDefinition.setForceREST(true);
			} else if (SynapseConstants.FORMAT_GET.equals(format)) {
				endpointDefinition.setForceGET(true);
			} else {
				log.error("format value '" + format + "' not yet implemented");		
				
			}
			endpointDefinition.setFormat(format);
			return endpointDefinition;
		} else {
			log.error("Couldn't retrieve endpoint information from the WSDL.");			
		}
		return endpointDefinition;

	}
}
