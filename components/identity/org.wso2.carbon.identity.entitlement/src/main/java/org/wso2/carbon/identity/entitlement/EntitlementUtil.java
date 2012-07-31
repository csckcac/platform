/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.entitlement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wso2.balana.ParsingException;
import org.wso2.balana.attr.BooleanAttribute;
import org.wso2.balana.attr.DateAttribute;
import org.wso2.balana.attr.DateTimeAttribute;
import org.wso2.balana.attr.DoubleAttribute;
import org.wso2.balana.attr.HexBinaryAttribute;
import org.wso2.balana.attr.IntegerAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.attr.TimeAttribute;
import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.balana.attr.AttributeValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.ctx.Attribute;
import org.wso2.balana.ctx.xacml2.RequestCtx;
import org.wso2.balana.ctx.xacml2.Subject;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.entitlement.dto.AttributeValueDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

/**
 * 
 * Provides utility functionalities used across different classes.
 * 
 */
public class EntitlementUtil {

	private static Log log = LogFactory.getLog(EntitlementUtil.class);

	/**
	 * Creates XACML RequestCtx using given attribute set. Here assumptions are done for the action,
	 * resource and environment attribute ids.
	 * 
	 * @param subjectName user or role
	 * @param subjectId subject attribute id
	 * @param resource resource name
	 * @param action action name
	 * @param environment environment names as array
	 * @return RequestCtx
	 * @throws Exception throws
	 */
	public static RequestCtx createXACMLRequestFromAttributes(String subjectName, String subjectId,
			String resource, String action, String environment) throws Exception {
        
		Set<Subject> subjects = null;
		Set<Attribute> resources = null;
		Set<Attribute> actions = null;
		Set<Attribute> environments = null;

		try {
			subjects = new HashSet<Subject>();
			resources = new HashSet<Attribute>();
			actions = new HashSet<Attribute>();
			environments = new HashSet<Attribute>();

			if (subjectName != null) {
				if (subjectId != null && !"".equals(subjectId)) {
					subjects.add(new Subject(getAttributes(subjectId,
                                           EntitlementConstants.STRING_DATA_TYPE, subjectName)));
				} else {
					subjects.add(new Subject(getAttributes(EntitlementConstants.SUBJECT_ID_DEFAULT,
                                            EntitlementConstants.STRING_DATA_TYPE, subjectName)));
				}
			}

			if (resource != null) {
				resources.add(getAttribute(EntitlementConstants.RESOURCE_ID_DEFAULT,
                                           EntitlementConstants.STRING_DATA_TYPE, resource));
			}
			if (action != null) {
				actions.add(getAttribute(EntitlementConstants.ACTION_ID_DEFAULT,
                                            EntitlementConstants.STRING_DATA_TYPE, action));
			}
			if (environment != null) {
                environments.add(getAttribute(EntitlementConstants.ENVIRONMENT_ID_DEFAULT,
                                            EntitlementConstants.STRING_DATA_TYPE, environment));
			}

			return new org.wso2.balana.ctx.xacml2.RequestCtx(subjects, resources, actions, environments);

		} catch (Exception e) {
			log.error("Error occurred while building XACML request", e);
			throw new Exception("Error occurred while building XACML request");
		}
	}

	/**
	 * Creates XACML request as a String object using given attribute set
	 * 
	 * @param subjectName user or role
	 * @param subjectId subject attribute id
	 * @param resource resource name
	 * @param action action name
	 * @param environment environment names as array
	 * @return request as a String
	 * @throws Exception throws
	 */
	public static String createXACMLRequestAsString(String subjectName, String subjectId,
			String resource, String action, String environment) throws Exception {

		try {
			RequestCtx request = createXACMLRequestFromAttributes(subjectName, subjectId, resource,
					action, environment);
			ByteArrayOutputStream requestOut = new ByteArrayOutputStream();
			request.encode(requestOut);
			return requestOut.toString();
		} catch (Exception e) {
			log.error("Error occurred while building XACML request", e);
			throw new Exception("Error occurred while building XACML request");
		}

	}


	/**
	 * @param uri
	 * @param value
	 * @return
	 * @throws URISyntaxException
	 */
	private static Set<Attribute> getAttributes(String uri, String type, final String value)
			throws URISyntaxException {
		Set<Attribute> attrs = new HashSet<Attribute>();

		AttributeValue attrValues = new AttributeValue(new URI(type)) {
			@Override
			public String encode() {
				return value;
			}
		};
		Attribute attribute = new Attribute(new URI(uri), null, null, attrValues, 1);
		attrs.add(attribute);
		return attrs;
	}

	/**
	 * @param uri
	 * @param value
	 * @return
	 * @throws URISyntaxException
	 */
	private static Attribute getAttribute(String uri, String type, final String value)
			throws URISyntaxException {

		AttributeValue attrValues = new AttributeValue(new URI(type)) {
			@Override
			public String encode() {
				return value;
			}
		};
		return new Attribute(new URI(uri), null, null, attrValues, 1);
	}

	/**
	 * Return an instance of a named cache that is common to all tenants.
	 * 
	 * @param name the name of the cache.
	 * 
	 * @return the named cache instance.
	 */
	public static Cache getCommonCache(String name) {
		// We create a single cache for all tenants. It is not a good choice to create per-tenant
		// caches in this case. We qualify tenants by adding the tenant identifier in the cache key.
		CarbonContextHolder currentContext = CarbonContextHolder.getCurrentCarbonContextHolder();
		currentContext.startTenantFlow();
		try {
			currentContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
			return CacheManager.getInstance().getCache(name);
		} finally {
			currentContext.endTenantFlow();
		}
	}

    /**
     * Return the Attribute Value Object for given string value and data type
     * @param value attribute value as a String object
     * @param type  attribute data type name as String object
     * @return Attribute Value Object
     * @throws IdentityException throws
     */
	public static AttributeValue getAttributeValue(final String value, String type)
                        throws IdentityException {

        try {
            if (StringAttribute.identifier.equals(type)) {
                return new StringAttribute(value);
            }
            if (IntegerAttribute.identifier.equals(type)) {
                return new IntegerAttribute(Long.parseLong(value));
            }
            if (BooleanAttribute.identifier.equals(type)) {
                return BooleanAttribute.getInstance(value);
            }
            if (DoubleAttribute.identifier.equals(type)) {
                return new DoubleAttribute(Double.parseDouble(value));
            }
            if (DateAttribute.identifier.equals(type)) {
                return new DateAttribute(DateFormat.getDateInstance().parse(value));
            }
            if (DateTimeAttribute.identifier.equals(type)) {
                return new DateTimeAttribute(DateFormat.getDateInstance().parse(value));
            }
            if (TimeAttribute.identifier.equals(type)) {
                return TimeAttribute.getInstance(value);
            }
            if (HexBinaryAttribute.identifier.equals(type)) {
                return new HexBinaryAttribute(value.getBytes());
            }

            return new AttributeValue(new URI(type)) {
                @Override
                public String encode() {
                    return value;
                }
            };

        } catch (ParsingException e) {
            throw new IdentityException("Error while creating AttributeValue object for given " +
                                        "string value and data type");
        } catch (ParseException e) {
            throw new IdentityException("Error while creating AttributeValue object for given " +
                                        "string value and data type");
        } catch (URISyntaxException e) {
            throw new IdentityException("Error while creating AttributeValue object for given " +
                                        "string value and data type");
        }
	}


    /**
     * This creates the XACML 2.0 Request element from AttributeValueDTO object model
     * @param attributeValueDTOs  AttributeValueDTO objects as List
     * @return DOM element as XACML request
     * @throws IdentityException  throws, if fails
     */
    public static Document createRequestElement(List<AttributeValueDTO> attributeValueDTOs)
                                        throws IdentityException {

        Document doc = createNewDocument();
        Element requestElement = doc.createElement(EntitlementConstants.REQUEST_ELEMENT);
        requestElement.setAttribute("xmlns", EntitlementConstants.REQ_RES_CONTEXT);
        requestElement.setAttribute("xmlns:xsi", EntitlementConstants.REQ_SCHEME);
        Element resourceElement = doc.createElement(EntitlementConstants.RESOURCE_ELEMENT);
        Element subjectElement = doc.createElement(EntitlementConstants.SUBJECT_ELEMENT);
        Element actionElement = doc.createElement(EntitlementConstants.ACTION_ELEMENT);
        Element enviornementElement = doc.createElement(EntitlementConstants.ENVIRONMENT_ELEMENT);

        if(attributeValueDTOs != null){
            for(AttributeValueDTO attributeValueDTO : attributeValueDTOs){
                if(EntitlementConstants.RESOURCE_ELEMENT.equals(attributeValueDTO.getAttributeType())){
                    resourceElement.appendChild(createRequestAttributeElement(attributeValueDTO, doc));
                }
                if(EntitlementConstants.ACTION_ELEMENT.equals(attributeValueDTO.getAttributeType())){
                    actionElement.appendChild(createRequestAttributeElement(attributeValueDTO, doc));
                }
                if(EntitlementConstants.SUBJECT_ELEMENT.equals(attributeValueDTO.getAttributeType())){
                    subjectElement.appendChild(createRequestAttributeElement(attributeValueDTO, doc));
                }
                if(EntitlementConstants.ENVIRONMENT_ELEMENT.equals(attributeValueDTO.getAttributeType())){
                    enviornementElement.appendChild(createRequestAttributeElement(attributeValueDTO, doc));
                }
            }
        }

        requestElement.appendChild(resourceElement);
        requestElement.appendChild(subjectElement);
        requestElement.appendChild(actionElement);
        requestElement.appendChild(enviornementElement);

        doc.appendChild(requestElement);

        return doc;
    }

    /**
     * This creates the attribute Element of the XACML request for AttributeValueDTO object.
     * This is a helper method for createRequestElement ()
     * @param attributeValueDTO  AttributeValueDTO object
     * @param doc Document element
     * @return DOM element
     */
    public static Element createRequestAttributeElement(AttributeValueDTO attributeValueDTO,
                                                        Document doc){

        // TODO Fix for issue in sunxacml that more than one resource-id can not be in the request
        Element attributeElement = doc.createElement(EntitlementConstants.ATTRIBUTE);
        String attributeValue = attributeValueDTO.getAttribute();
        if(attributeValue != null) {
            attributeElement.setAttribute(EntitlementConstants.ATTRIBUTE_ID,
                                          attributeValueDTO.getAttributeId());
            attributeElement.setAttribute(EntitlementConstants.DATA_TYPE,
                 attributeValueDTO.getAttributeDataType());
            Element attributeValueElement = doc.createElement(EntitlementConstants.
                    ATTRIBUTE_VALUE);
            attributeValueElement.setTextContent(attributeValue.trim());
            attributeElement.appendChild(attributeValueElement);
        }
        return attributeElement;
    }

    /**
     * Creates Document Object. This is a helper method
     * @return Document Object
     * @throws IdentityException  throws, if fails
     */
    private static Document createNewDocument() throws IdentityException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        Document doc = null;

        try {
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new IdentityException("While creating Document Object", e);
        }

        return doc;
    }

    /**
     * Convert XACML policy Document element to a String object
     * @param doc Document element
     * @return String XACML policy
     * @throws IdentityException throws when transform fails
     */
    public static String getStringFromDocument(Document doc) throws IdentityException {
        try {

            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString().substring(writer.toString().indexOf('>') + 1);

        } catch(TransformerException e){
            throw new IdentityException("While transforming policy element to String", e);
        }
    }

    /**
     * Validates the given policy XML files against the standard XACML policies.
     * @param policy Policy to validate
     * @throws IdentityException If validation failed or XML parsing failed or any IOException occurs
     */
    public static void validatePolicy(PolicyDTO policy) throws IdentityException {
        try {

            // there may be cases where you only updated the policy meta data in PolicyDTO not the
            // actual XACML policy String
            if(policy.getPolicy() == null || policy.getPolicy().trim().length() < 1){
                return;
            }
            //build XML document
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputSource inputSource = new InputSource();
            inputSource.setCharacterStream(new StringReader(policy.getPolicy()));
            Document doc = documentBuilder.parse(inputSource);

            //get policy version
            Element policyElement = doc.getDocumentElement();
            String policyXMLNS = policyElement.getNamespaceURI();

            Map<String, Schema> schemaMap = EntitlementServiceComponent.
                                                    getEntitlementConfig().getPolicySchemaMap();
            //load correct schema by version
            Schema schema = schemaMap.get(policyXMLNS);

            if(schema != null){
                //Do the DOM validation
                DOMSource domSource = new DOMSource(doc);
                DOMResult domResult = new DOMResult();
                Validator validator = schema.newValidator();
                validator.validate(domSource,domResult);
                if(log.isDebugEnabled()){
                    log.debug("XACML Policy validation succeeded with the Schema");
                }
            } else {
                log.error("Invalid Namespace in policy");
                throw new IdentityException("Invalid Namespace in policy");                
            }
        } catch (SAXException e) {
            log.error("XACML Policy validation failed :" + e.getMessage());
            throw new IdentityException("Invalid policy : "+ e.getMessage());
        } catch (IOException e) {
            throw new IdentityException(e.getMessage());
        } catch (ParserConfigurationException e) {
            throw new IdentityException(e.getMessage());
        }
    }

	/**
	 * Creates Simple XACML request using given attribute value.Here category, attribute ids and datatypes are
     * taken as default values.
	 *
	 * @param subject user or role
	 * @param resource resource name
	 * @param action action name
	 * @return String
	 * @throws Exception throws
	 */
	public static String createSimpleXACMLRequest(String subject,
			String resource, String action) throws Exception {

        return "<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" " +
        "ReturnPolicyIdList=\"false\" CombinedDecision=\"false\"><Attributes " +
        "Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\" >" +
        "<Attribute IncludeInResult=\"false\" AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\">" +
        "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">"+ subject +
        "</AttributeValue></Attribute></Attributes><Attributes " +
        "Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\">" +
        "<Attribute IncludeInResult=\"false\" AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\">" +
        "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + resource +
        "</AttributeValue></Attribute></Attributes><Attributes " +
        "Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\">" +
        "<Attribute IncludeInResult=\"false\" AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\">" +
        "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">" + action + "</AttributeValue>" +
        "</Attribute></Attributes></Request>";
	}

}
