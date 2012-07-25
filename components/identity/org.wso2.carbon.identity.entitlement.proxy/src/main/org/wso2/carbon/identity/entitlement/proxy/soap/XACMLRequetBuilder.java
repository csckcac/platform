package org.wso2.carbon.identity.entitlement.proxy.soap;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.RequestCtx;
import com.sun.xacml.ctx.Subject;

public class XACMLRequetBuilder {

	private static Log log = LogFactory.getLog(XACMLRequetBuilder.class);

	/**
	 * 
	 * @param subject
	 * @param resource
	 * @param action
	 * @return
	 * @throws Exception
	 */
	public static String buildXACMLRequest(RequestAttribute[] subjectAttrs,
			RequestAttribute[] resourceAttrs, RequestAttribute[] actionAttrs,
			RequestAttribute[] envAttrs) throws Exception {

		Set<Subject> subjects = null;
		Set<Attribute> resources = null;
		Set<Attribute> actions = null;
		Set<Attribute> environment = null;

		try {
			subjects = new HashSet<Subject>();
			resources = new HashSet<Attribute>();
			actions = new HashSet<Attribute>();
			environment = new HashSet<Attribute>();

			if (subjectAttrs != null) {
				for (int i = 0; i < subjectAttrs.length; i++) {
					subjects.add(new Subject(getAttributes(subjectAttrs[i].getId(),
							subjectAttrs[i].getType(), subjectAttrs[i].getValue())));
				}
			}

			if (resourceAttrs != null) {
				for (int i = 0; i < resourceAttrs.length; i++) {
					resources.add(getAttribute(resourceAttrs[i].getId(), resourceAttrs[i].getType(),
							resourceAttrs[i].getValue()));
				}
			}

			if (actionAttrs != null) {
				for (int i = 0; i < actionAttrs.length; i++) {
					actions.add(getAttribute(actionAttrs[i].getId(), actionAttrs[i].getType(),
							actionAttrs[i].getValue()));
				}
			}

			if (envAttrs != null) {
				for (int i = 0; i < envAttrs.length; i++) {
					environment.add(getAttribute(envAttrs[i].getId(), envAttrs[i].getType(),
							envAttrs[i].getValue()));
				}
			}

			RequestCtx request = new RequestCtx(subjects, resources, actions, environment);
			ByteArrayOutputStream requestOut = new ByteArrayOutputStream();
			request.encode(requestOut);
			return requestOut.toString();
		} catch (Exception e) {
			log.error("Error occured while building XACML request", e);
			throw new Exception("Error occured while building XACML request");
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
		Attribute attribute = new Attribute(new URI(uri), null, null, attrValues);
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
		return new Attribute(new URI(uri), null, null, attrValues);
	}
}
