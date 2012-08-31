/**
 *  Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.ntask.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.wso2.carbon.coordination.core.services.CoordinationService;
import org.wso2.carbon.core.ServerStartupHandler;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.ntask.core.TaskStartupHandler;
import org.wso2.carbon.ntask.core.impl.TaskAxis2ConfigurationContextObserver;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.ntask.core.service.impl.TaskServiceImpl;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * This class represents the Tasks declarative service component.
 * @scr.component name="tasks.component" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="coordination.service" interface="org.wso2.carbon.coordination.core.services.CoordinationService"
 * cardinality="1..1" policy="dynamic"  bind="setCoordinationService" unbind="unsetCoordinationService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1" policy="dynamic"
 * bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1" policy="dynamic" 
 * bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class TasksDSComponent {
	
	private final Log log = LogFactory.getLog(TasksDSComponent.class);
	
	private static RegistryService registryService;
	
	private static CoordinationService coodinationService;
	
	private static RealmService realmService;
	
	private static Scheduler scheduler;
	
	private static ConfigurationContextService configCtxService;
	
	private TaskService taskService;
		
	protected void activate(ComponentContext ctx) {
		try {
			TasksDSComponent.scheduler = new StdSchedulerFactory().getScheduler();
			TasksDSComponent.getScheduler().start();
			if (this.getTaskService() == null) {
				this.taskService = new TaskServiceImpl();
			}
			BundleContext bundleContext = ctx.getBundleContext();
			bundleContext.registerService(ServerStartupHandler.class.getName(),
                    new TaskStartupHandler(this.taskService), null);
			bundleContext.registerService(TaskService.class.getName(), 
					this.getTaskService(), null);
			bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
                    new TaskAxis2ConfigurationContextObserver(this.getTaskService()), null);
			if (log.isDebugEnabled()) {
				log.debug("Task service started");
			}
		} catch (Exception e) {
			log.error("Error in intializing Tasks component", e);
		}
	}
	
	protected void deactivate(ComponentContext ctx) {
		this.taskService = null;
		if (TasksDSComponent.getScheduler() != null) {
			try {
			    TasksDSComponent.getScheduler().shutdown();
			} catch (Exception e) {
				log.error(e);
			}
		}
	}
	
	public TaskService getTaskService() {
		return taskService;
	}
	
	public static Scheduler getScheduler() {
		return scheduler;
	}
	
    protected void setRegistryService(RegistryService registryService) {
        TasksDSComponent.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
        TasksDSComponent.registryService = null;
    }

    public static RegistryService getRegistryService() {
    	return TasksDSComponent.registryService;
    }
    
    protected void setCoordinationService(CoordinationService coordinationService) {
    	TasksDSComponent.coodinationService = coordinationService;
    }
    
    protected void unsetCoordinationService(CoordinationService coordinationService) {
    	TasksDSComponent.coodinationService = null;
    }
    
    public static CoordinationService getCoordinationService() {
    	return coodinationService;
    }
    
    protected void setRealmService(RealmService realmService) {
    	TasksDSComponent.realmService = realmService;
    }
    
    protected void unsetRealmService(RealmService realmService) {
    	TasksDSComponent.realmService = null;
    }
	
    public static RealmService getRealmService() {
    	return TasksDSComponent.realmService;
    }
    
    protected void setConfigurationContextService(ConfigurationContextService configCtxService) {
    	TasksDSComponent.configCtxService = configCtxService;
    }
    
    protected void unsetConfigurationContextService(ConfigurationContextService configCtxService) {
    	TasksDSComponent.configCtxService = null;
    }
	
    public static ConfigurationContextService getConfigurationContextService() {
    	return TasksDSComponent.configCtxService;
    }
    
}
