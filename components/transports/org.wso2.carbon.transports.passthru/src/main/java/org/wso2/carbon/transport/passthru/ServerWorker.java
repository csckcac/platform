/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.transport.passthru;

import java.net.InetAddress;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpInetConnection;
import org.apache.http.protocol.HTTP;
import org.wso2.carbon.transport.passthru.config.SourceConfiguration;

/**
 * This is a worker thread for executing an incoming request in to the transport.
 */
public class ServerWorker implements Runnable {
    private static final Log log = LogFactory.getLog(ServerWorker.class);
    /** the incoming message to be processed */
    private org.apache.axis2.context.MessageContext msgContext = null;
    /** the http request */
    private SourceRequest request = null;
    /** The configuration of the receiver */
    private SourceConfiguration sourceConfiguration = null;

    private static final String SOAP_ACTION_HEADER = "SOAPAction";

    public ServerWorker(final SourceRequest request,
                        final SourceConfiguration sourceConfiguration) {
        this.request = request;
        this.sourceConfiguration = sourceConfiguration;

        this.msgContext = createMessageContext(request);

        // set these properties to be accessed by the engine
        msgContext.setProperty(
                PassThroughConstants.PASS_THROUGH_SOURCE_REQUEST, request);
        msgContext.setProperty(
                PassThroughConstants.PASS_THROUGH_SOURCE_CONFIGURATION, sourceConfiguration);
        msgContext.setProperty(PassThroughConstants.PASS_THROUGH_SOURCE_CONNECTION,
                request.getConnection());
    }

    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("Starting a new Server Worker instance");
        }
        String method = request.getRequest().getRequestLine().getMethod();
        ConfigurationContext cfgCtx = sourceConfiguration.getConfigurationContext();        
        msgContext.setProperty(Constants.Configuration.HTTP_METHOD, request.getMethod());

        String uri = request.getUri();
        String oriUri = uri;

        if (uri.indexOf(cfgCtx.getServicePath()) != -1) {
            // discard upto servicePath
            uri = uri.substring(uri.indexOf(cfgCtx.getServicePath()) +
                    cfgCtx.getServicePath().length());
            // discard [proxy] service name if any
            int pos = uri.indexOf("/", 1);
            if (pos > 0) {
                uri = uri.substring(pos);
            } else {
                pos = uri.indexOf("?");
                if (pos != -1) {
                    uri = uri.substring(pos);
                } else {
                    uri = "";
                }
            }
        } else {
            // remove any absolute prefix if any
            int pos = uri.indexOf("://");
            if (pos != -1) {
                uri = uri.substring(pos + 3);
                pos = uri.indexOf("/");
                if (pos != -1) {
                    uri = uri.substring(pos + 1);
                }
            }
        }
        
        String servicePrefix = oriUri.substring(0, oriUri.indexOf(uri));
        if (servicePrefix.indexOf("://") == -1) {
            HttpInetConnection inetConn = (HttpInetConnection) request.getConnection();
            InetAddress localAddr = inetConn.getLocalAddress();
            if (localAddr != null) {
                servicePrefix = (sourceConfiguration.isSsl() ? "https://" : "http://") +
                        localAddr.getHostAddress() + ":" + inetConn.getLocalPort() + servicePrefix;
            }
        }
       
        msgContext.setProperty(PassThroughConstants.SERVICE_PREFIX, servicePrefix);


        msgContext.setTo(new EndpointReference(uri));
        msgContext.setProperty(PassThroughConstants.REST_URL_POSTFIX, uri);

        if (request.isEntityEnclosing()) {
            processEntityEnclosingRequest();
        } else {
            processNonEntityEnclosingMethod();
        }
    }

    private void processNonEntityEnclosingMethod() {
        String soapAction = request.getHeaders().get(SOAP_ACTION_HEADER);
        if ((soapAction != null) && soapAction.startsWith("\"") && soapAction.endsWith("\"")) {
            soapAction = soapAction.substring(1, soapAction.length() - 1);
        }

        msgContext.setSoapAction(soapAction);
        msgContext.setTo(new EndpointReference(request.getUri()));
        msgContext.setServerSide(true);
        msgContext.setDoingREST(true);
        msgContext.setProperty(PassThroughConstants.NO_ENTITY_BODY, Boolean.TRUE);

        try {
            msgContext.setEnvelope(new SOAP11Factory().getDefaultEnvelope());

            AxisEngine.receive(msgContext);
        } catch (AxisFault axisFault) {
            handleException("Error processing " + request.getMethod() +
                " request for : " + request.getUri(), axisFault);
        }
    }

    private void processEntityEnclosingRequest() {
        try {
            String contentTypeHeader = request.getHeaders().get(HTTP.CONTENT_TYPE);
            contentTypeHeader = contentTypeHeader != null ? contentTypeHeader : inferContentType();

            String charSetEncoding = BuilderUtil.getCharSetEncoding(contentTypeHeader);
            String contentType = TransportUtils.getContentType(contentTypeHeader, msgContext);

            // get the contentType of char encoding
            if (charSetEncoding == null) {
                charSetEncoding = MessageContext.DEFAULT_CHAR_SET_ENCODING;
            }

            msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEncoding);
            msgContext.setProperty(Constants.Configuration.CONTENT_TYPE, contentType);
            msgContext.setProperty(Constants.Configuration.MESSAGE_TYPE, contentType);

            msgContext.setTo(new EndpointReference(request.getUri()));
            msgContext.setProperty(HTTPConstants.HTTP_METHOD, request.getMethod());
            msgContext.setServerSide(true);

            if (HTTPTransportUtils.isRESTRequest(contentTypeHeader)) {

                msgContext.setProperty(PassThroughConstants.REST_REQUEST_CONTENT_TYPE, contentType);
                msgContext.setDoingREST(true);

                String messageType =
                        (String) msgContext.getProperty(Constants.Configuration.MESSAGE_TYPE);
                if (HTTPConstants.MEDIA_TYPE_X_WWW_FORM.equals(messageType) ||
                        HTTPConstants.MEDIA_TYPE_MULTIPART_FORM_DATA.equals(messageType)) {
                    msgContext.setProperty(Constants.Configuration.MESSAGE_TYPE,
                            HTTPConstants.MEDIA_TYPE_APPLICATION_XML);
                }

                SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
                SOAPEnvelope envelope = fac.getDefaultEnvelope();

                msgContext.setEnvelope(envelope);
            } else {
                String soapAction = request.getHeaders().get(SOAP_ACTION_HEADER);

                int soapVersion = HTTPTransportUtils.
                        initializeMessageContext(msgContext, soapAction,
                                request.getUri(), contentTypeHeader);
                SOAPEnvelope envelope;

                if (soapVersion == 1) {
                    SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
                    envelope = fac.getDefaultEnvelope();
                } else if (soapVersion == 2) {
                    SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
                    envelope = fac.getDefaultEnvelope();
                } else {
                    SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();
                    envelope = fac.getDefaultEnvelope();
                }

                msgContext.setEnvelope(envelope);
            }

            msgContext.setProperty(PassThroughConstants.PASS_THROUGH_PIPE, request.getPipe());

            AxisEngine.receive(msgContext);
        } catch (AxisFault axisFault) {
            handleException("Error processing " + request.getMethod() +
                " request for : " + request.getUri(), axisFault);
        }
    }


    /**
     * Create an Axis2 message context for the given http request. The request may be in the
     * process of being streamed
     *
     * @param request the http request to be used to create the corresponding Axis2 message context
     * @return the Axis2 message context created
     */
    private MessageContext createMessageContext(SourceRequest request) {
        ConfigurationContext cfgCtx = sourceConfiguration.getConfigurationContext();
        MessageContext msgContext =
                new MessageContext();
        msgContext.setMessageID(UUIDGenerator.getUUID());

        // Axis2 spawns a new threads to send a message if this is TRUE - and it has to
        // be the other way
        msgContext.setProperty(MessageContext.CLIENT_API_NON_BLOCKING,
                Boolean.FALSE);
        msgContext.setConfigurationContext(cfgCtx);

        /** Only passthru transport sender is capable of sending a message received by passthru
         * transport receiver. Hence, explicitly set tTRANSPORT_OUT_DESCRIPTION.**/
        TransportInDescription trpInDesc = sourceConfiguration.getInDescription();
        TransportOutDescription trpOutDesc = cfgCtx.getAxisConfiguration().getTransportOut(
                                                        trpInDesc.getName());

        msgContext.setTransportIn(trpInDesc);
        msgContext.setIncomingTransportName(trpInDesc.getName());

        msgContext.setTransportOut(trpOutDesc);
        msgContext.setProperty("TRANSPORT_OUT_DESCRIPTION", trpOutDesc);

        msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, this);

        msgContext.setServerSide(true);
        msgContext.setProperty(
                Constants.Configuration.TRANSPORT_IN_URL, request.getUri());

        // http transport header names are case insensitive
        Map<String, String> headers = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });

        Set<Map.Entry<String, String>> entries = request.getHeaders().entrySet();
        for (Map.Entry<String, String> entry : entries) {
            headers.put(entry.getKey(), entry.getValue());
        }
        msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, headers);

        return msgContext;
    }

    private void handleException(String msg, Exception e) {
        if (e == null) {
            log.error(msg);
        } else {
            log.error(msg, e);
        }

        if (e == null) {
            e = new Exception(msg);
        }

        try {
            MessageContext faultContext =
                    MessageContextBuilder.createFaultMessageContext(
                    msgContext, e);
            AxisEngine.sendFault(faultContext);
        } catch (Exception ignored) {}
    }

    private String inferContentType() {
        Parameter param = sourceConfiguration.getConfigurationContext().getAxisConfiguration().
                getParameter(PassThroughConstants.REQUEST_CONTENT_TYPE);
        if (param != null) {
            return param.getValue().toString();
        }
        return null;
    }
}