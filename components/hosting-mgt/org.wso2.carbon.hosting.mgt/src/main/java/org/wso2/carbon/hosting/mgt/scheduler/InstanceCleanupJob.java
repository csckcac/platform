package org.wso2.carbon.hosting.mgt.scheduler;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hosting.mgt.clients.AutoscaleServiceClient;
import org.wso2.carbon.hosting.mgt.openstack.db.OpenstackDAO;
import org.wso2.carbon.hosting.mgt.utils.PHPCartridgeConstants;

public class InstanceCleanupJob implements Runnable{

	private static final Log log = LogFactory.getLog(InstanceCleanupJob.class);

	@Override
	public void run() {		
		
		if(log.isInfoEnabled()) {
			log.info(" Instance Clean up task is started ");
		}
		
		OpenstackDAO openstackDAO = new OpenstackDAO();
		List<String> instanceIps = openstackDAO.readInstancesToDelete();
		
		if (instanceIps != null && !instanceIps.isEmpty()) {
			//if (log.isDebugEnabled()) {
				log.info(" Terminating instances, having IP s " + instanceIps);
			//}

				log.info(" AutoscalerService url :" + System.getProperty(PHPCartridgeConstants.AUTOSCALER_SERVICE_URL));
				
			try {
				AutoscaleServiceClient autoscaleServiceClient = new AutoscaleServiceClient(
						System.getProperty(PHPCartridgeConstants.AUTOSCALER_SERVICE_URL));
				autoscaleServiceClient.init(true);

				for (String instanceIp : instanceIps) {

					if (log.isDebugEnabled()) {
						log.debug(" Terminating instance. IP :" + instanceIp);
					}
					autoscaleServiceClient.terminateSpiInstance(instanceIp);
				}

			} catch (Exception e) {
				log.error(" Exception is occurred when trying to connet to AutoScalerService. Reason :" + e.getMessage());
			}
		} else {
			
			if(log.isInfoEnabled()) {
				log.info(" No instances found to be terminated. Exiting the task ");
			}
		}
		
	}
}
