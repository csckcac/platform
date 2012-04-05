/**
 * 
 */
package org.wso2.carbon.hosting.mgt.service;

import org.wso2.carbon.hosting.mgt.HostingResources;
import org.wso2.carbon.hosting.mgt.ResourcesException;

/**
 * @author wso2
 *
 */
public class ContainerManagementService {

	private HostingResources hostingRes;
	
	public ContainerManagementService() {
		hostingRes = new HostingResources();
	}

    /**
     * Create the container. Note that at this stage container will not be physically created in the worker node, but
     * only registered in the registry.
     * @param tenantName
     * @param tenantPassword
     * @param zone
     * @param template
     */
	public void createContainer(String tenantName, String tenantPassword, String zone, String template) {
		
		try {
            hostingRes.registerContainer(tenantName, zone, template);
		} catch (ResourcesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    /**
     * Start the container
     * @param tenantName
     * @param containerRoot
     */
	public void startContainer(String tenantName, String containerRoot) {

        // TODO Call the agent service to start the container
	}

    /**
     * Stop the container
     * @param tenantName
     * @param containerRoot
     */
	public void stopContainer(String tenantName, String containerRoot) {
        // TODO Call the agent service to stop the container
	}

}
