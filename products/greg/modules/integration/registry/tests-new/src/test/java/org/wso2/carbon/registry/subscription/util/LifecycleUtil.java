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

import java.io.File;
import java.rmi.RemoteException;

import org.wso2.carbon.automation.api.clients.governance.HumanTaskAdminClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.governance.WorkItem;
import org.wso2.carbon.automation.api.clients.registry.InfoServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.PropertiesAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.governance.utils.FileReader;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.registry.subscription.util.WorkItemClient;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.info.stub.beans.xsd.SubscriptionBean;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

public class LifecycleUtil {

	private static UserInfo userInfo;
	private static ManageEnvironment environment;
	private static WSRegistryServiceClient wsRegistryServiceClient;
	private static RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
	private static LifeCycleAdminServiceClient lifeCycleAdminServiceClient;
	
	private static final String ASPECT_NAME = "StateDemoteLC";
	private static final String ACTION_DEMOTE = "Demote";
	private static final String LC_STATE2 ="Development";
	private static final String ACTION_ITEM_CLICK = "itemClick";
	
	
	public static boolean init(String path, ManageEnvironment env,UserInfo userInf) throws Exception{
		environment = env;
		userInfo = userInf;
		wsRegistryServiceClient = registryProviderUtil.getWSRegistry(Integer.parseInt(userInfo.getUserId()), ProductConstant.GREG_SERVER_NAME);
		lifeCycleAdminServiceClient = new LifeCycleAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),userInfo.getUserName(),userInfo.getPassword());
		addRole();
		boolean result1 =  consoleSubscribe(path,"LifeCycleCreated") && addLifeCycle(path) && getNotification("The LifeCycle was created") && mangementUnsubscription(path);
		boolean result2 =  consoleSubscribe(path,"CheckListItemChecked") && checkItem(path) && getNotification("The CheckList item 'Effective Inspection Completed' of LifeCycle State 'Tested' was Checked.")&& mangementUnsubscription(path);
		boolean result3 =  consoleSubscribe(path,"CheckListItemUnchecked") && unCheckItem(path) && getNotification("The CheckList item 'Effective Inspection Completed' of LifeCycle State 'Tested' was Unchecked.")&& mangementUnsubscription(path);
		boolean result4 =  consoleSubscribe(path,"LifeCycleStateChanged") && changeState(path) && getNotification("The LifeCycle State Changed from 'Tested' to 'Development'") && mangementUnsubscription(path);
		boolean result5 =  consoleSubscribe(path,"ResourceUpdated") && update(path) && getNotification("at path "+path+" was updated.") && mangementUnsubscription(path);
		boolean result6 =  consoleSubscribe(path,"ResourceDeleted") && delete(path) && getNotification("at path "+path+" was deleted.") ;
		
		clean(path);
		return result1 && result2 && result3 && result4 && result5 && result6;
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
	
	private static boolean getNotification(String type) throws RemoteException, IllegalStateFault, IllegalAccessFault, IllegalArgumentFault, InterruptedException  {
		boolean success = false;
		HumanTaskAdminClient humanTaskAdminClient = new HumanTaskAdminClient(environment.getGreg().getBackEndUrl(), userInfo.getUserName(),userInfo.getPassword());
		Thread.sleep(1000);
		WorkItem[] workItems = WorkItemClient.getWorkItems(humanTaskAdminClient);
		 
		for (WorkItem workItem : workItems) {
			if((workItem.getPresentationSubject().toString()).contains(type))
    		 {
				success = true;
    			break;
    		 }
		}
		return success;
	}
	
	private static boolean addLifeCycle(String path) throws Exception {
		 LifeCycleManagementClient lifeCycleManagementClient = new LifeCycleManagementClient(environment.getGreg().getBackEndUrl(), userInfo.getUserName(), userInfo.getPassword());
	     String filePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" + File.separator + "lifecycle" + File.separator + "StateDemoteLifeCycle.xml";
	     String lifeCycleConfiguration = FileReader.readFile(filePath);
	     lifeCycleManagementClient.addLifeCycle(lifeCycleConfiguration);
	     wsRegistryServiceClient.associateAspect(path, ASPECT_NAME);
	     return (lifeCycleAdminServiceClient.getLifecycleBean(path) != null);
	  }
	 
	 
	
	private static boolean changeState(String path) throws Exception{
		 lifeCycleAdminServiceClient.invokeAspect(path, ASPECT_NAME, ACTION_DEMOTE,null);
         LifecycleBean lifeCycle = lifeCycleAdminServiceClient.getLifecycleBean(path);
         boolean success = false;
         for (Property prop : lifeCycle.getLifecycleProperties()) {
        	 if (("registry.lifecycle."+ASPECT_NAME+".state").equalsIgnoreCase(prop.getKey())) {
        		 success = prop.getValues()[0].equalsIgnoreCase(LC_STATE2);   
            }
         }
        return success;              
	 }
	 
	 
	private static boolean checkItem(String path) throws RemoteException, CustomLifecyclesChecklistAdminServiceExceptionException{
		 lifeCycleAdminServiceClient.invokeAspect(path, ASPECT_NAME, ACTION_ITEM_CLICK, new String[]{"true", "true", "true"});
		 LifecycleBean lifeCycle= lifeCycleAdminServiceClient.getLifecycleBean(path);
		 Property[] p =lifeCycle.getLifecycleProperties();
		 boolean result= true;
         for (Property prop : p) {
        	 if (("registry.custom_lifecycle.checklist.option.0.item").equalsIgnoreCase(prop.getKey())) {
                  result = result && (prop.getValues()[3].equals("value:true")); 
             }
             if (("registry.custom_lifecycle.checklist.option.1.item").equalsIgnoreCase(prop.getKey())) {
            	 result = result && (prop.getValues()[3].equals("value:true")); 
             }
             if (("registry.custom_lifecycle.checklist.option.2.item").equalsIgnoreCase(prop.getKey())) {
            	 result = result && (prop.getValues()[3].equals("value:true")); 
             }
         }
         return result;
	 }
	 
	private static boolean unCheckItem(String path) throws RemoteException, CustomLifecyclesChecklistAdminServiceExceptionException{
		 lifeCycleAdminServiceClient.invokeAspect(path, ASPECT_NAME, ACTION_ITEM_CLICK, new String[]{"false", "false", "false"});
		 LifecycleBean lifeCycle= lifeCycleAdminServiceClient.getLifecycleBean(path);
		 Property[] p =lifeCycle.getLifecycleProperties();
		 boolean resultProp1= false;
		 boolean resultProp2= false;
		 boolean resultProp3= false;
         for (Property prop : p) {
        	 if (("registry.custom_lifecycle.checklist.option.0.item").equalsIgnoreCase(prop.getKey())) {
                  resultProp1 =  (prop.getValues()[3].equals("value:false")); 
             }
             if (("registry.custom_lifecycle.checklist.option.1.item").equalsIgnoreCase(prop.getKey())) {
            	  resultProp2 =  (prop.getValues()[3].equals("value:false")); 
             }
             if (("registry.custom_lifecycle.checklist.option.2.item").equalsIgnoreCase(prop.getKey())) {
            	  resultProp3 = (prop.getValues()[3].equals("value:false")); 
             }
             
         }
         return resultProp1 && resultProp2 && resultProp3;
	 }
	 
	private static boolean update(String path) throws Exception{
    	PropertiesAdminServiceClient propertiesAdminServiceClient = new PropertiesAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(),userInfo.getPassword());
		propertiesAdminServiceClient.setProperty(path, "TestProperty", "TestValue");
		ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(),userInfo.getPassword());
		return resourceAdminServiceClient.getProperty(path, "TestProperty").equals("TestValue");
	}
	
	private static boolean delete(String path) throws Exception{
    	ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(),userInfo.getPassword());
		return resourceAdminServiceClient.deleteResource(path);
	}
	
	public static boolean mangementUnsubscription(String path) throws Exception   {
		InfoServiceAdminClient infoServiceAdminClient = new InfoServiceAdminClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(),userInfo.getPassword());
        String sessionID = environment.getGreg().getSessionCookie();
		SubscriptionBean sBean = infoServiceAdminClient.getSubscriptions(path, sessionID);
        infoServiceAdminClient.unsubscribe(path, sBean.getSubscriptionInstances()[0].getId(), sessionID);
        sBean = infoServiceAdminClient.getSubscriptions(path, sessionID);
        return (sBean.getSubscriptionInstances() == null);
   }
	

	
	private static void clean(String path) throws Exception{
	  	UserManagementClient userManagementClient = new UserManagementClient(environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(),userInfo.getPassword());
		userManagementClient.deleteRole("RoleSubscriptionTest");
	
		
  }
}
