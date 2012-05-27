package org.wso2.carbon.endpoint.uep;

import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.aspects.AspectConfiguration;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint;

/****
 * <default [format="soap11|soap12|pox|get"] [optimize="mtom|swa"]
 * [encoding="charset encoding"]
 * [statistics="enable|disable"] [trace="enable|disable"]>
 * 
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
 * </default>
 *****/

/**
 * UDefaultEndpointDefinition class to construct <code>EndpointDefinition</code> from
 * unifiedendpoint 
 * 
 */
public class UDefaultEndpointDefinition {

	private static final Log log = LogFactory.getLog(UDefaultEndpointDefinition.class);
	private UnifiedEndpoint unifiedEndpoint;

	public UDefaultEndpointDefinition() {

	}

	protected void setEndpointDefinition(UnifiedEndpoint unifiedEndpoint) {
		this.unifiedEndpoint = unifiedEndpoint;
		
		 setUDefaultEndpointDefinition();
	}

	/**
	 * Get the DefaultEndpoint from the specified unified endpoint
	 * 
	 * @return definition
	 */
	private void setUDefaultEndpointDefinition() {

		EndpointDefinition definition = null;	
		UdefaultEndpoint uDefaultEndpoint = null;

		if (unifiedEndpoint != null) {
			
			definition = new EndpointDefinition();
			uDefaultEndpoint = new UdefaultEndpoint();
			definition.setAddressingOn(unifiedEndpoint.isAddressingEnabled());
			definition.setSecurityOn(unifiedEndpoint.isSecurityEnabled());
			definition.setReliableMessagingOn(unifiedEndpoint.isRMEnabled());
			definition.setUseSeparateListener(unifiedEndpoint.isSeparateListener());

			

			if (unifiedEndpoint.getUepId() != null) {
				uDefaultEndpoint.setName(unifiedEndpoint.getUepId());
			}
			getMonitoringConfig(definition, unifiedEndpoint.getUepId(), unifiedEndpoint);
			getQoSConfig(definition, unifiedEndpoint);
			getMsgFormatConfig(definition, unifiedEndpoint);
			getTimeoutConfig(definition, unifiedEndpoint);
			getMarkForSuspensionConfig(definition, unifiedEndpoint);
			
			// set the endpoint definition
			uDefaultEndpoint.setDefinition(definition);
		}
		
	}

	// Statictics, Trace config
	protected void getMonitoringConfig(EndpointDefinition endpointDefinition, String epName,
	                                 UnifiedEndpoint unifiedEndpoint) {

		processAuditStatus(endpointDefinition, epName, unifiedEndpoint);
		if (unifiedEndpoint.isTraceEnabled()) {
			endpointDefinition.setTraceState(1); // trace is set
		} else {
			endpointDefinition.setTraceState(2); // trace is unset
		}
	}

	// QOS config
	protected void getQoSConfig(EndpointDefinition endpointDefinition, UnifiedEndpoint unifiedEndpoint) {

		if (unifiedEndpoint.isRMEnabled()) {
			endpointDefinition.setReliableMessagingOn(true);
		}
		if (unifiedEndpoint.getWsRMPolicyKey() != null) {
			endpointDefinition.setWsRMPolicyKey(unifiedEndpoint.getWsRMPolicyKey());
		}

		if (unifiedEndpoint.isSecurityEnabled()) {
			endpointDefinition.setSecurityOn(true);
		}
		if (unifiedEndpoint.getWsSecPolicyKey() != null) {
			endpointDefinition.setWsRMPolicyKey(unifiedEndpoint.getWsSecPolicyKey());
		}

		if (unifiedEndpoint.isAddressingEnabled()) {
			endpointDefinition.setAddressingOn(true);
		}
		if (unifiedEndpoint.getAddressingVersion() != null) {
			endpointDefinition.setAddressingVersion(unifiedEndpoint.getAddressingVersion());
		}
		if (unifiedEndpoint.isSeparateListener()) {
			endpointDefinition.setUseSeparateListener(true);
		}

	}

	// Timeout configuration from unified endpoint
	protected void getTimeoutConfig(EndpointDefinition endpointDefinition,
	                              UnifiedEndpoint unifiedEndpoint) {

		if (unifiedEndpoint.getTimeout() != null) {
			Map timeoutPropertiesMap = unifiedEndpoint.getTimeout().getTimeOutProperties();

			if (timeoutPropertiesMap.size() > 0) {
				for (int i = 0; i < timeoutPropertiesMap.size(); i++) {
					if (unifiedEndpoint.getTimeout()
					                   .getTimeOutPropertyValue(UEndpointConstants.TIMEOUT_DURATION) != null) {
						String timeout =
						                 unifiedEndpoint.getTimeout()
						                                .getTimeOutPropertyValue(UEndpointConstants.TIMEOUT_DURATION);
						endpointDefinition.setTimeoutDuration(Long.parseLong(timeout));
					}
					if (unifiedEndpoint.getTimeout()
					                   .getTimeOutPropertyValue(UEndpointConstants.TIMEOUT_ACTION) != null) {
						String action =
						                unifiedEndpoint.getTimeout()
						                               .getTimeOutPropertyValue(UEndpointConstants.TIMEOUT_ACTION);
						endpointDefinition.setTimeoutAction(Integer.parseInt(action));
					}
				}
			}
		}
	}

	// Message format configurations from unified endpoint
	protected void getMsgFormatConfig(EndpointDefinition endpointDefinition,
	                                UnifiedEndpoint unifiedEndpoint) {

		if (unifiedEndpoint.getMessageOutput() != null) {
			if (unifiedEndpoint.getMessageOutput().getFormat() != null) {
				endpointDefinition.setFormat(unifiedEndpoint.getMessageOutput().getFormat());
			}

			if (unifiedEndpoint.getMessageOutput().getOptimize() != null) {
				if ("mtom".equals(unifiedEndpoint.getMessageOutput().getOptimize())) {
					endpointDefinition.setUseMTOM(true);
				} else {
					endpointDefinition.setUseSwa(true);
				}
			}

			if (unifiedEndpoint.getMessageOutput().getCharSetEncoding() != null) {
				endpointDefinition.setCharSetEncoding(unifiedEndpoint.getMessageOutput()
				                                                     .getCharSetEncoding());
			}
		}
	}

	// Message format configurations from unified endpoint
	protected void getMarkForSuspensionConfig(EndpointDefinition endpointDefinition,
	                                        UnifiedEndpoint unifiedEndpoint) {

		endpointDefinition.setRetriesOnTimeoutBeforeSuspend(unifiedEndpoint.getRetriesBeforeSuspension());
		endpointDefinition.setRetryDurationOnTimeout((int) (unifiedEndpoint.getRetryDelay()));

		List errorCodes = unifiedEndpoint.getTimeoutErrorCodes();
		if (errorCodes.size() > 0) {
			for (int i = 0; i < errorCodes.size(); i++) {
				endpointDefinition.addTimeoutErrorCode(Integer.parseInt((String) errorCodes.get(i)));
			}
		}

		endpointDefinition.setInitialSuspendDuration(unifiedEndpoint.getInitialDuration());
		endpointDefinition.setSuspendProgressionFactor((float) unifiedEndpoint.getProgressionFactor());
		endpointDefinition.setSuspendMaximumDuration(unifiedEndpoint.getMaximumDuration());

		List suspensionErrorCodes = unifiedEndpoint.getSuspendErrorCodes();
		if (suspensionErrorCodes.size() > 0) {
			for (int i = 0; i < suspensionErrorCodes.size(); i++) {
				endpointDefinition.addSuspendErrorCode(Integer.parseInt((String) suspensionErrorCodes.get(i)));
			}
		}

	}

	/**
	 * Check endpoint statistics
	 * 
	 * @param definition
	 * @param name
	 * @param epOmElement
	 */
	protected void processAuditStatus(EndpointDefinition definition, String name,
	                                  UnifiedEndpoint unifiedEndpoint) {

		if (name == null || "".equals(name)) {
			name = SynapseConstants.ANONYMOUS_ENDPOINT;
		}
		AspectConfiguration aspectConfiguration = new AspectConfiguration(name);
		definition.configure(aspectConfiguration);

		if (unifiedEndpoint.isStatisticEnabled()) {
			aspectConfiguration.enableStatistics();
		}

	}



}
