package org.wso2.carbon.endpoint.uep;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint;

/****
 * <address uri="endpoint address" [format="soap11|soap12|pox|get"]
 * [optimize="mtom|swa"]
 * [encoding="charset encoding"]
 * [statistics="enable|disable"] [trace="enable|disable"]>
 * 
 * <enableRM [policy="key"]/>?
 * <enableSec [policy="key"]/>?
 * <enableAddressing [version="final|submission"]
 * [separateListener="true|false"]/>?
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
 * </address>
 *****/

/**
 * AddressEndpointFactory class to construct address endpoint from
 * unifiedendpoint definition
 * 
 * @param uep
 * @return UEndpoint
 */
public class UAddressEndpointDefinition extends UDefaultEndpointDefinition {
	private UnifiedEndpoint unifiedEndpoint;
	private static final Log log = LogFactory.getLog(UAddressEndpointDefinition.class);

	public UAddressEndpointDefinition() {

	}

	@Override
	protected void setEndpointDefinition(UnifiedEndpoint unifiedEndpoint) {
		this.unifiedEndpoint = unifiedEndpoint;		
		setUAddressEndpointDefiniton();
	}

	/**
	 * Get the AddressEndpointdefinition from the specified unified endpoint
	 * 
	 * @return definition
	 */
	private void setUAddressEndpointDefiniton() {

		EndpointDefinition definition = null;	
		UAddressEndpoint uAddressEndpoint = null; 
		if (unifiedEndpoint != null) {		

			definition = new EndpointDefinition();
			uAddressEndpoint= new UAddressEndpoint();

			if (unifiedEndpoint.getAddress() != null) {				
				definition.setAddress(unifiedEndpoint.getAddress());
			}
			
			definition.setAddressingOn(unifiedEndpoint.isAddressingEnabled());
			definition.setSecurityOn(unifiedEndpoint.isSecurityEnabled());
			definition.setReliableMessagingOn(unifiedEndpoint.isRMEnabled());
			definition.setUseSeparateListener(unifiedEndpoint.isSeparateListener());		

			if (unifiedEndpoint.getUepId() != null) {
				uAddressEndpoint.setName(unifiedEndpoint.getUepId());
			}
			getMonitoringConfig(definition, unifiedEndpoint.getUepId(), unifiedEndpoint);
			getQoSConfig(definition, unifiedEndpoint);
			getMsgFormatConfig(definition, unifiedEndpoint);
			getTimeoutConfig(definition, unifiedEndpoint);
			getMarkForSuspensionConfig(definition, unifiedEndpoint);
			
			// set the endpoint definition
			uAddressEndpoint.setDefinition(definition);
		}
	}

}
