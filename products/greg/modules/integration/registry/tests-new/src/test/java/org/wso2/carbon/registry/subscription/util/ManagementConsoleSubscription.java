/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.registry.subscription.util;



import java.rmi.RemoteException;

import org.wso2.carbon.automation.api.clients.governance.HumanTaskAdminClient;
import org.wso2.carbon.automation.api.clients.governance.WorkItem;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.PropertiesAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.subscription.util.WorkItemClient;

public class ManagementConsoleSubscription {
	
	private static UserInfo userInfo;
	private static ManageEnvironment environment;
	
	public static boolean init(String path,String eventType,ManageEnvironment env,UserInfo userInf) throws Exception{
		environment = env;
		userInfo = userInf;
		boolean result = (addRole() && consoleSubscribe(path,eventType) && update(path) && getNotification(path));
		clean(path);
		return result;
	}
	
	private static boolean addRole() throws Exception {
		UserManagementClient userManagementClient = new UserManagementClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(),userInfo.getPassword());
		userManagementClient.addRole("RoleSubscriptionTest",new String[]{userInfo.getUserName()} , new String[]{""});
		return userManagementClient.roleNameExists("RoleSubscriptionTest");
	}
	

	private static boolean consoleSubscribe(String path,String eventType) throws RemoteException, RegistryException{
		InfoServiceAdminClient infoServiceAdminClient = new InfoServiceAdminClient(environment.getGreg().getProductVariables().getBackendUrl(),userInfo.getUserName(),userInfo.getPassword());
		SubscriptionBean bean = infoServiceAdminClient.subscribe(path, "work://RoleSubscriptionTest", eventType, environment.getGreg().getSessionCookie());
		return bean.getSubscriptionInstances() != null;
		
	}
	
	private static boolean update(String path) throws Exception{
	    	PropertiesAdminServiceClient propertiesAdminServiceClient = new PropertiesAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(),userInfo.getPassword());
			propertiesAdminServiceClient.setProperty(path, "TestProperty", "TestValue");
			ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(),userInfo.getPassword());
			return resourceAdminServiceClient.getProperty(path, "TestProperty").equals("TestValue");
		}
	  
	private static boolean getNotification(String path) throws RemoteException, IllegalStateFault, IllegalAccessFault, IllegalArgumentFault, InterruptedException  {
			boolean success = false;
			HumanTaskAdminClient humanTaskAdminClient = new HumanTaskAdminClient(environment.getGreg().getBackEndUrl(), userInfo.getUserName(),userInfo.getPassword());
			Thread.sleep(1000);
			WorkItem[] workItems = WorkItemClient.getWorkItems(humanTaskAdminClient);
			 
			for (WorkItem workItem : workItems) {
				if((workItem.getPresentationSubject().toString()).contains("at path "+path+" was updated."))
	    		 {
					success = true;
	    			break;
	    		 }
			}
			return success;
		}
	  
	private static void clean(String path) throws Exception{
		  	UserManagementClient userManagementClient = new UserManagementClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(),userInfo.getPassword());
			userManagementClient.deleteRole("RoleSubscriptionTest");
			PropertiesAdminServiceClient propertiesAdminServiceClient = new PropertiesAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(),userInfo.getPassword());
			propertiesAdminServiceClient.removeProperty(path, "TestProperty");
	  }
}
