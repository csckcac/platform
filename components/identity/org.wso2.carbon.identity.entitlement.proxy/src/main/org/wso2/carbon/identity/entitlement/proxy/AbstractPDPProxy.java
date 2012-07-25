package org.wso2.carbon.identity.entitlement.proxy;

import java.util.List;

public abstract class AbstractPDPProxy {

	public abstract void setPDPConfig(PDPConfig config);

	public abstract boolean getDecision(Attribute[] subjectAttrs, Attribute[] rescAttrs,
			Attribute[] actionAttrs, Attribute[] envAttrs, String domainId, String appId)
			throws Exception;

	public abstract boolean subjectCanActOnResource(String subjectType, String alias,
			String actionId, String resourceId, String domainId, String appId) throws Exception;

	public abstract boolean subjectCanActOnResource(String subjectType, String alias,
			String actionId, String resourceId, Attribute[] attributes, String domainId,
			String appId) throws Exception;

	public abstract List<String> getResourcesForAlias(String alias, String appId) throws Exception;

	public abstract List<String> getActionableResourcesForAlias(String alias, String appId)
			throws Exception;

	public abstract List<String> getActionableChidResourcesForAlias(String alias,
			String parentResource, String action, String appId) throws Exception;

	public abstract List<String> getActionsForResource(String alias, String resources, String appId)
			throws Exception;
}
