package org.wso2.carbon.hosting.mgt.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hosting.mgt.utils.PHPCartridgeConstants;

public class InstanceCleanupScheduler {

	private static final Log log = LogFactory.getLog(InstanceCleanupScheduler.class);
	private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, new InstanceCheckThreadFactory());	
	
	public static void schedule() {
		
		scheduler.scheduleWithFixedDelay(new InstanceCleanupJob(), 
										Long.valueOf(System.getProperty(PHPCartridgeConstants.OPENSTACK_INSTANCE_CHECK_INITIAL_DELAY)),
										Long.valueOf(System.getProperty(PHPCartridgeConstants.OPENSTACK_INSTANCE_CHECK_DELAY)),
										TimeUnit.HOURS);
		if (log.isInfoEnabled()) {
			log.info("Instance Clean up task successfully scheduled");
		}		
	}	
	
	public static void shutdown() {
		if(!scheduler.isShutdown()){
		scheduler.shutdown();
		}
	}
}
