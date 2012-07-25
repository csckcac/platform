package org.wso2.carbon.identity.entitlement.proxy.thrift;

import java.util.List;

import org.wso2.carbon.identity.entitlement.proxy.AbstractPDPProxy;
import org.wso2.carbon.identity.entitlement.proxy.Attribute;
import org.wso2.carbon.identity.entitlement.proxy.PDPConfig;

public class ThriftProxy extends AbstractPDPProxy {

	@Override
	public boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
			String resourceId, String domainId, String appId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
			String resourceId, Attribute[] attributes, String domainId, String appId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> getResourcesForAlias(String alias, String appId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getActionableResourcesForAlias(String alias, String appId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getActionsForResource(String alias, String resources, String appId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPDPConfig(PDPConfig config) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getDecision(Attribute[] subjectAttrs, Attribute[] rescAttrs,
			Attribute[] actionAttrs, Attribute[] envAttrs, String domainId, String appId)
			throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> getActionableChidResourcesForAlias(String alias, String parentResource,
			String action, String appId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
