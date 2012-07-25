package org.wso2.carbon.hosting.mgt.scheduler;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hosting.mgt.clients.AutoscaleServiceClient;
import org.wso2.carbon.hosting.mgt.internal.Store;
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
		List<String> privateIps = openstackDAO.readInstancesToDelete();
		
		if (privateIps != null && !privateIps.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug(" Terminating instances, having IP s " + privateIps);
				log.debug(" AutoscalerService url :" + System.getProperty(PHPCartridgeConstants.AUTOSCALER_SERVICE_URL));
			}				
				
			try {				
				AutoscaleServiceClient autoscaleServiceClient = new AutoscaleServiceClient(
						System.getProperty(PHPCartridgeConstants.AUTOSCALER_SERVICE_URL));
				autoscaleServiceClient.init(true);

				for (String privateIp : privateIps) {

					if (log.isDebugEnabled()) {
						log.debug(" Terminating instance. IP :" + privateIp);
					}
					autoscaleServiceClient.terminateSpiInstance(privateIp);
                    if(Store.tenantToPrivateIpMap.containsValue(privateIp)
                       && Store.privateIpToTenantMap.containsKey(privateIp)){

                        Store.tenantToPublicIpMap.remove(Store.tenantToPrivateIpMap.get(privateIp));
                        Store.tenantToPrivateIpMap.remove(Store.privateIpToTenantMap.get(privateIp));
                        Store.privateIpToTenantMap.remove(privateIp);
                    }
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
