/**
 * 
 */
package org.wso2.carbon.hosting.mgt.service;


/**
 * @author wso2
 *
 */
public class ApplicationManagementService {


    /**
     * Upload the applications that will be deployed in the container
     * @param tenantName
     * @param password
     * @param appName
     */

	public void  uploadApplication(String tenantName, String password, String appName) {
		
//		try {
//            //Container container =  hostingRes.retrieveContainer(tenantName);
//            // TODO call the agent to create the container
//		} catch (ResourcesException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}


    /**
     * Retrieve and display the applications deployed in the container
     * @param username
     * @param containerRoot
     */
	public void listApplications(String username, String containerRoot) {
        // TODO list applications
	}

}
