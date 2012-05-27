package org.wso2.carbon.endpoint.uep.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.endpoint.EndpointAdminException;
import org.wso2.carbon.endpoint.util.ConfigHolder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointFactory;

public class UEndpointUtil {
	/** Reference to UnifiedEndpoint object **/
	private UnifiedEndpoint unifiedEndpoint;
	private OMElement unifiedEndpointElement;
	private static final Log log = LogFactory.getLog(UEndpointUtil.class);

	/**
	 * Retrieve the UnifedEndpoint out of the uepconfiguartion
	 * 
	 * @return
	 * @throws EndpointAdminException
	 */
	public UnifiedEndpoint getUnifiedEndpoint(String uepConfigurationPath)
	                                                                      throws EndpointAdminException {

		if (uepConfigurationPath != null) {
			UnifiedEndpointFactory uepFac = new UnifiedEndpointFactory();
			String prefix;
			String resourceName;
			try {
				if (uepConfigurationPath.startsWith("gov:")) {
					prefix = "gov:/";
					resourceName = uepConfigurationPath.replace(prefix, "");
					Registry govRegistry = ConfigHolder.getInstance().getGovernanceRegistry();
					Resource resource = govRegistry.get(resourceName);
					String resourceStr = new String((byte[]) resource.getContent());
					unifiedEndpointElement = AXIOMUtil.stringToOM(resourceStr);

				} else if (uepConfigurationPath.startsWith("conf:")) {
					prefix = "conf:/";
					resourceName = uepConfigurationPath.replace(prefix, "");
					Registry confRegistry = ConfigHolder.getInstance().getConfigRegistry();
					Resource resource = confRegistry.get(resourceName);

					String resourceStr = new String((byte[]) resource.getContent());
					unifiedEndpointElement = AXIOMUtil.stringToOM(resourceStr);
				}
				unifiedEndpoint = uepFac.createEndpoint(unifiedEndpointElement);
			} catch (RegistryException e) {
				String msg = "Could not retrive the resource from the registry";
				log.error(msg, e);
				throw new EndpointAdminException(msg, e);
			} catch (EndpointAdminException e) {
				String msg = "Could not retrive the config registry";
				log.error(msg, e);
				throw new EndpointAdminException(msg, e);
			} catch (Exception e) {
				String msg = "Could not construct the unifiedEndpoint";
				log.error(msg, e);
				throw new EndpointAdminException(msg, e);
			}

		}
		return unifiedEndpoint;
	}
}
