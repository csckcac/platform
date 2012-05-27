package org.wso2.carbon.endpoint.uep;

import javax.xml.namespace.QName;

import org.apache.axis2.addressing.AddressingConstants;

/**
 * Constant class contains all constants needed by unified endpoint.
 * 
 */
public class UEndpointConstants {

	/** Message send out constants(nhttp) **/
	public static final String NHTTP_REST_REQUEST_CONTENT_TYPE =
	                                                             "synapse.internal.rest.contentType";
	public static final String NHTTP_REST_URL_POSTFIX = "REST_URL_POSTFIX";
	public static final String NHTTP_ENDPOINT_PREFIX = "ENDPOINT_PREFIX";
	public static final String NHTTP_FORCE_HTTP_1_0 = "FORCE_HTTP_1.0";

	// UnifiedEndpointModule
	public static final String UNIFIED_ENDPOINT_MODULE = "UEPModule";

	public static final String WSDL11_NS = "http://schemas.xmlsoap.org/wsdl/";
	public static final String WSDL11_NS_PREFIX = "wsdl11";

	/** factory constants **/
	public static final String SYNAPSE_NAMESPACE = "http://ws.apache.org/ns/synapse";
	public static final String SYNAPSE_NULL_NAMESPACE = "";

	public static final String TIMEOUT = "timeout";
	public static final String TIMEOUT_DURATION = "duration";
	public static final String TIMEOUT_ACTION = "action";

	public static final String METADATA = "Metadata";
	public static final String ADDRESS = "Address";
	public static final String WSDL11_DEFINITIONS = "definitions";
	public static final String METADATA_ID = "id";

	/** QNames ***/

	public final static QName ADDRESS_Q = new QName(AddressingConstants.Final.WSA_NAMESPACE,
	                                                ADDRESS);
	public final static QName METADATA_WSDL11_DEFINITIONS_Q = new QName(WSDL11_NS,
	                                                                    WSDL11_DEFINITIONS);
	public final static QName METADATA_Q = new QName(AddressingConstants.Final.WSA_NAMESPACE,
	                                                 METADATA);
	public final static QName METADATA_ID_Q = new QName(null, METADATA_ID);
	public static final QName TIMEOUT_Q = new QName(null, TIMEOUT);
	public static final QName DURATION_Q = new QName(null, TIMEOUT_DURATION);
	public static final QName ACTION_Q = new QName(null, TIMEOUT_ACTION);

}
