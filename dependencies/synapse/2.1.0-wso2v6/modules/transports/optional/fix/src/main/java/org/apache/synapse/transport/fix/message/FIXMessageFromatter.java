/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.synapse.transport.fix.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Reads the incoming message context and convert them back to the fix raw
 * message
 * 
 */
public class FIXMessageFromatter implements MessageFormatter {

	private Log log = LogFactory.getLog(FIXMessageFromatter.class);

	public String formatSOAPAction(MessageContext arg0, OMOutputFormat arg1, String arg2) {
		return null;
	}

	public byte[] getBytes(MessageContext arg0, OMOutputFormat arg1) throws AxisFault {
	
		return null;
	}

	public String getContentType(MessageContext msgCtxt, OMOutputFormat format, String soapActionString) {
		String contentType = (String) msgCtxt.getProperty(Constants.Configuration.CONTENT_TYPE);
		String encoding = format.getCharSetEncoding();
		if (contentType == null) {
			contentType = (String) msgCtxt.getProperty(Constants.Configuration.MESSAGE_TYPE);
		}
		if (encoding != null) {
			contentType += "; charset=" + encoding;
		}
		return contentType;
	}

	public URL getTargetAddress(MessageContext arg0, OMOutputFormat arg1, URL arg2) throws AxisFault {
		return null;
	}

	/**
	 * Read the FIX message payload and identify the namespace if exists
	 * 
	 * @param fixBody
	 *            FIX message payload
	 * @return namespace as a OMNamespace
	 */
	public static OMNamespace getNamespaceOfFIXPayload(SOAPBody fixBody) {
		return fixBody.getFirstElementNS();
	}

	private void generateFIXBody(OMElement node, Message message, MessageContext msgCtx, boolean withNs, String nsURI, String nsPrefix)
	                                                                                                                                   throws IOException {

		Iterator bodyElements = node.getChildElements();
		while (bodyElements.hasNext()) {
			OMElement bodyNode = (OMElement) bodyElements.next();
			String nodeLocalName = bodyNode.getLocalName();

			// handle repeating groups
			if (nodeLocalName.equals(FIXConstants.FIX_GROUPS)) {
				int groupsKey = Integer.parseInt(bodyNode.getAttributeValue(new QName(FIXConstants.FIX_FIELD_ID)));
				// Group group;
				Iterator groupElements = bodyNode.getChildElements();
				while (groupElements.hasNext()) {
					OMElement groupNode = (OMElement) groupElements.next();
					Iterator groupFields = groupNode.getChildrenWithName(new QName(FIXConstants.FIX_FIELD));
					List<Integer> idList = new ArrayList<Integer>();
					while (groupFields.hasNext()) {
						OMElement fieldNode = (OMElement) groupFields.next();
						idList.add(Integer.parseInt(fieldNode.getAttributeValue(new QName(FIXConstants.FIX_FIELD_ID))));
					}

					int[] order = new int[idList.size()];
					for (int i = 0; i < order.length; i++) {
						order[i] = idList.get(i);
					}

				}

			} else {
				String tag;
				if (withNs) {
					tag = bodyNode.getAttributeValue(new QName(nsURI, FIXConstants.FIX_FIELD_ID, nsPrefix));
				} else {
					tag = bodyNode.getAttributeValue(new QName(FIXConstants.FIX_FIELD_ID));
				}

				String value = null;
				OMElement child = bodyNode.getFirstElement();
				if (child != null) {
					String href;
					if (withNs) {
						href = bodyNode.getFirstElement().getAttributeValue(new QName(nsURI, FIXConstants.FIX_FIELD_ID, nsPrefix));
					} else {
						href = bodyNode.getFirstElement().getAttributeValue(new QName(FIXConstants.FIX_MESSAGE_REFERENCE));
					}

					if (href != null) {
						DataHandler binaryDataHandler = msgCtx.getAttachment(href.substring(4));
						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						binaryDataHandler.writeTo(outputStream);
						value = new String(outputStream.toByteArray());
					}
				} else {
					value = bodyNode.getText();
				}

				if (value != null) {
					message.getBodyList().add(new Field(tag, value));
				}
			}
		}
	}

	public void writeTo(MessageContext msgCtx, OMOutputFormat format, OutputStream out, boolean arg3) throws AxisFault {

		if (log.isDebugEnabled()) {
			log.debug("Extracting FIX message from the message context (Message ID: " + msgCtx.getMessageID() + ")");
		}

		boolean withNs = false;
		String nsPrefix = null;
		String nsURI = null;

		Message message = new Message();
		SOAPBody soapBody = msgCtx.getEnvelope().getBody();

		// find namespace information embedded in the FIX payload
		OMNamespace ns = getNamespaceOfFIXPayload(soapBody);
		if (ns != null) {
			withNs = true;
			nsPrefix = ns.getPrefix();
			nsURI = ns.getNamespaceURI();
		}

		OMElement messageNode;
		if (withNs) {
			messageNode = soapBody.getFirstChildWithName(new QName(nsURI, FIXConstants.FIX_MESSAGE, nsPrefix));
		} else {
			messageNode = soapBody.getFirstChildWithName(new QName(FIXConstants.FIX_MESSAGE));
		}

		Iterator messageElements = messageNode.getChildElements();

		while (messageElements.hasNext()) {
			OMElement node = (OMElement) messageElements.next();
			// create FIX header
			if (node.getQName().getLocalPart().equals(FIXConstants.FIX_HEADER)) {
				Iterator headerElements = node.getChildElements();
				while (headerElements.hasNext()) {
					OMElement headerNode = (OMElement) headerElements.next();
					String tag;
					if (withNs) {
						tag = headerNode.getAttributeValue(new QName(nsURI, FIXConstants.FIX_FIELD_ID, nsPrefix));
					} else {
						tag = headerNode.getAttributeValue(new QName(FIXConstants.FIX_FIELD_ID));
					}
					String value = null;

					OMElement child = headerNode.getFirstElement();
					if (child != null) {
						String href;
						if (withNs) {
							href =
							       headerNode.getFirstElement().getAttributeValue(new QName(nsURI, FIXConstants.FIX_MESSAGE_REFERENCE,
							                                                                nsPrefix));
						} else {
							href = headerNode.getFirstElement().getAttributeValue(new QName(FIXConstants.FIX_MESSAGE_REFERENCE));
						}

						if (href != null) {
							DataHandler binaryDataHandler = msgCtx.getAttachment(href.substring(4));
							ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
							try {
								binaryDataHandler.writeTo(outputStream);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							value = new String(outputStream.toByteArray());
						}
					} else {
						value = headerNode.getText();
					}

					if (value != null) {
						// message.getHeader().setString(Integer.parseInt(tag),
						// value);
						message.getHeaderList().add(new Field(tag, value));
					}
				}

			} else if (node.getQName().getLocalPart().equals(FIXConstants.FIX_BODY)) {
				// create FIX body
				try {
					generateFIXBody(node, message, msgCtx, withNs, nsURI, nsPrefix);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (node.getQName().getLocalPart().equals(FIXConstants.FIX_TRAILER)) {
				// create FIX trailer
				Iterator trailerElements = node.getChildElements();
				while (trailerElements.hasNext()) {
					OMElement trailerNode = (OMElement) trailerElements.next();
					String tag;
					if (withNs) {
						tag = trailerNode.getAttributeValue(new QName(nsURI, FIXConstants.FIX_FIELD_ID, nsPrefix));
					} else {
						tag = trailerNode.getAttributeValue(new QName(FIXConstants.FIX_FIELD_ID));
					}
					String value = null;

					OMElement child = trailerNode.getFirstElement();
					if (child != null) {
						String href;
						if (withNs) {
							href = trailerNode.getFirstElement().getAttributeValue(new QName(nsURI, FIXConstants.FIX_FIELD_ID, nsPrefix));
						} else {
							href = trailerNode.getFirstElement().getAttributeValue(new QName(FIXConstants.FIX_MESSAGE_REFERENCE));
						}
						if (href != null) {
							DataHandler binaryDataHandler = msgCtx.getAttachment(href.substring(4));
							ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
							try {
								binaryDataHandler.writeTo(outputStream);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							value = new String(outputStream.toByteArray());
						}
					} else {
						value = trailerNode.getText();
					}

					if (value != null) {
						// message.getTrailer().setString(Integer.parseInt(tag),
						// value);
						message.getTailerList().add(new Field(tag, value));
					}
				}
			}
		}

		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < message.getHeaderList().size(); i++) {
			if (i != 0 && i != message.getHeaderList().size()) {
				builder.append(FIXConstants.SOH);
			}
			builder.append(message.getHeaderList().get(i).getId() + "=" + message.getHeaderList().get(i).getTag());

		}

		for (int i = 0; i < message.getBodyList().size(); i++) {
			if (i != message.getBodyList().size()) {
				builder.append(FIXConstants.SOH);
			}
			builder.append(message.getBodyList().get(i).getId() + "=" + message.getBodyList().get(i).getTag());

		}

		for (int i = 0; i < message.getTailerList().size(); i++) {
			if (i != message.getTailerList().size()) {
				builder.append(FIXConstants.SOH);
			}
			builder.append(message.getTailerList().get(i).getId() + "=" + message.getTailerList().get(i).getTag());

		}

		builder.append(FIXConstants.SOH);

		try {
			out.write(builder.toString().getBytes());
		} catch (IOException e) {
			log.error("Error while formatting FIX SOAP message", e);
			throw new AxisFault(e.getMessage());
		}

	}

}
