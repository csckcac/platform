package org.wso2.carbon.identity.entitlement.proxy;

import java.util.List;

public class PDPProxy {

	public static PDPProxy pdpProxy = new PDPProxy();

	private String messageFormat;
	private String appId;
	private AbstractPDPProxy proxy;
	private PDPConfig config;
    private static final String DEFAULT_DATA_TYPE = "http://www.w3.org/2001/XMLSchema#string";

	private PDPProxy() {
	}

	public static PDPProxy getInstance() {
		return pdpProxy;
	}

	public static PDPProxy getInstance(PDPConfig pdpConfig) throws Exception {
		pdpProxy.init(pdpConfig);
		return pdpProxy;
	}

	public void init(PDPConfig pdpConfig) throws Exception {
		pdpProxy.validatePDPConfig(pdpConfig);
		proxy = PDPFactory.getPDPProxy(config);
	}

	public boolean getDecision(Attribute[] subjectAttrs, Attribute[] rescAttrs,
			Attribute[] actionAttrs, Attribute[] envAttrs, String domainId) throws Exception {
		return getDecision(subjectAttrs, rescAttrs, actionAttrs, envAttrs, domainId, appId);
	}

	public boolean getDecision(String subject, String resource, String action, String environment,
                                String domainId) throws Exception {


        Attribute subjectAttribute = new Attribute();
        subjectAttribute.setId("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
        subjectAttribute.setType(DEFAULT_DATA_TYPE);
        subjectAttribute.setValue(subject);

        Attribute actionAttribute = new Attribute();
        actionAttribute.setId("urn:oasis:names:tc:xacml:1.0:action:action-id");
        actionAttribute.setType(DEFAULT_DATA_TYPE);
        actionAttribute.setValue(action);

        Attribute resourceAttribute = new Attribute();
        resourceAttribute.setId("urn:oasis:names:tc:xacml:1.0:resource:resource-id");
        resourceAttribute.setType(DEFAULT_DATA_TYPE);
        resourceAttribute.setValue(resource);

        Attribute environmentAttribute = new Attribute();
        environmentAttribute.setId("urn:oasis:names:tc:xacml:1.0:environment:environment-id");
        environmentAttribute.setType(DEFAULT_DATA_TYPE);
        environmentAttribute.setValue(environment);

		return getDecision(new Attribute[]{subjectAttribute}, new Attribute[]{resourceAttribute},
                new Attribute[]{actionAttribute}, new Attribute[]{environmentAttribute}, domainId, appId);
	}

	public boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
			String resourceId, String domainId) throws Exception {
		return subjectCanActOnResource(subjectType, alias, actionId, resourceId, domainId, appId);
	}

	public boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
			String resourceId, Attribute[] attributes, String domainId) throws Exception {
		return subjectCanActOnResource(subjectType, alias, actionId, resourceId, attributes,
				domainId, appId);
	}

	public List<String> getActionableChidResourcesForAlias(String alias, String parentResource,
			String action) throws Exception {
		return getActionableChidResourcesForAlias(alias, parentResource, action, appId);
	}

	public List<String> getResourcesForAlias(String alias) throws Exception {
		return getResourcesForAlias(alias, appId);
	}

	public List<String> getActionableResourcesForAlias(String alias) throws Exception {
		return getActionableResourcesForAlias(alias, appId);
	}

	public List<String> getActionsForResource(String alias, String resources) throws Exception {
		return getActionsForResource(alias, resources, appId);
	}

	public boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
			String resourceId, String domainId, String appId) throws Exception {
		return proxy.subjectCanActOnResource(subjectType, alias, actionId, resourceId, domainId,
				appId);
	}

	public boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
			String resourceId, Attribute[] attributes, String domainId, String appId)
			throws Exception {
		return proxy.subjectCanActOnResource(subjectType, alias, actionId, resourceId, attributes,
				domainId, appId);
	}

	public List<String> getResourcesForAlias(String alias, String appId) throws Exception {
		return proxy.getResourcesForAlias(alias, appId);
	}

	public List<String> getActionableResourcesForAlias(String alias, String appId) throws Exception {
		return proxy.getActionableResourcesForAlias(alias, appId);
	}

	public List<String> getActionsForResource(String alias, String resources, String appId)
			throws Exception {
		return proxy.getActionsForResource(alias, resources, appId);
	}

	public boolean getDecision(Attribute[] subjectAttrs, Attribute[] rescAttrs,
			Attribute[] actionAttrs, Attribute[] envAttrs, String domainId, String appid)
			throws Exception {
		return proxy.getDecision(subjectAttrs, rescAttrs, actionAttrs, envAttrs, domainId, appid);
	}

	public List<String> getActionableChidResourcesForAlias(String alias, String parentResource,
			String action, String appId) throws Exception {
		return proxy.getActionableChidResourcesForAlias(alias, parentResource, action, appId);
	}

	private void validatePDPConfig(PDPConfig pdpConfig) {
		pdpProxy.messageFormat = pdpConfig.getMessageFormat();
		pdpProxy.appId = pdpConfig.getAppId();

		if (pdpProxy.messageFormat == null || pdpProxy.messageFormat.trim().length() == 0) {
			throw new IllegalArgumentException("Message format cannot be null or empty");
		}
		if (!ProxyConstants.JSON.equals(pdpProxy.messageFormat)
				&& !ProxyConstants.SOAP.equals(pdpProxy.messageFormat)
				&& !ProxyConstants.THRIFT.equals(pdpProxy.messageFormat)) {
			throw new IllegalArgumentException(
					"Invalid message format. Should be json, soap or thrift");
		}
		this.config = pdpConfig;
	}

}
