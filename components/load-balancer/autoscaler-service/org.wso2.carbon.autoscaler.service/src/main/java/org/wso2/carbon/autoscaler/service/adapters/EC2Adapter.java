package org.wso2.carbon.autoscaler.service.adapters;

import com.amazonaws.services.ec2.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.autoscaler.service.IAutoscalerService;
import org.wso2.carbon.autoscaler.service.internal.AutoscalerServiceDSHolder;
import org.wso2.carbon.lb.common.ec2.EC2InstanceManager;
import org.wso2.carbon.lb.common.ec2.EC2Util;
import org.wso2.carbon.lb.common.conf.EC2Configuration;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EC2 specific Adapter, handles spawning / terminating instances in EC2
 *
 */
public class EC2Adapter extends Adapter {

    private static final Log log = LogFactory.getLog(EC2Adapter.class);

    // Name of the Adapter
    private static final String ADAPTER_NAME = "ec2";

    // EC2Instance Manager will handle all the EC2 API level invocations
    private EC2InstanceManager ec2InstanceManager;

    // EC2Configuration which holds ec2.conf configuration;
    private EC2Configuration ec2Configuration = new EC2Configuration();

    // instanceIdToEC2IdMap will keep a map between started instance Id and EC2 generated ID
    private Map<String, String> instanceIdToEC2IdMap = new HashMap<String, String>();

    private IAutoscalerService autoscalerService = AutoscalerServiceDSHolder.getInstance().getAutoscalerService();

    /**
     * Default constructor which creates EC2 Configuration and EC2 Instance Manager
     */
    public EC2Adapter(){
        log.info("Constructing EC2 Adapter...");

        // Create ec2Configuration from given file

        String ec2ConfigFile =
                CarbonUtils.getCarbonConfigDirPath() + File.separator + "ec2.conf";
        ec2Configuration.init(ec2ConfigFile);

        // Create EC2 Instance Manager

        ec2InstanceManager = EC2Util.createEC2InstanceManager(ec2Configuration.getEc2_access_key(),
                ec2Configuration.getEc2_private_key(),
                ec2Configuration.getInstance_mgt_epr());

        if (log.isDebugEnabled()) {
            log.debug("EC2 Adaptor constructed with EC2 configuration");
        }
    }

    @Override
    public String getName() {
        return ADAPTER_NAME;
    }

    @Override
    public boolean spawnInstance(final String domainName, String instanceId) throws ClassNotFoundException, SQLException {

        log.info("Trying to spawn instance in EC2 Adapter");

        EC2Configuration.ServiceConfiguration serviceConfig = ec2Configuration.getServiceConfig(domainName);

        // Spawns only a single instance at a time with given instance ID
        List<String> ec2InstanceIdList = runInstances(serviceConfig, 1);

        // Store the returned EC2 instance ID. That will useful when terminating EC2 instances
        if(ec2InstanceIdList != null && ec2InstanceIdList.size() == 1){
            instanceIdToEC2IdMap.put(instanceId,ec2InstanceIdList.get(0));

        }

        // At this point the the server will starts after some time (Sleep for 3 minutes)
        // start a new thread to reduce the pending instance count after sometime

        new Thread(
                new Runnable() {
                    public void run() {
                        try {
                            log.info("Starting a new Thread to handle pending instance count");
                            try {
                                log.info("Thread Sleeping for 4 minutes...");
                                Thread.sleep(240000);
                            } catch (InterruptedException e) {
                                log.error("InterruptedException while waiting for new instance startup", e);
                            }

                            // Reducing the pending instance count by one in AutoscalerService
                            // Here we assumes that the server instance is successfully started

                            log.info("Sleeep finished and decrementing pending count");
                            autoscalerService.addPendingInstanceCount(domainName, -1);

                        } catch (Exception e) {
                            log.error("Error occurred inside pending instance count management Thread ", e);
                        }
                    }
                }).start();


        // Non blocking return
        log.info("Started " + 1 + " new instances in domain " +
                domainName);
        return true;

    }

    @Override
    public boolean terminateInstance(String instanceId) {

        String ec2InstanceId = instanceIdToEC2IdMap.get(instanceId);

        log.info("Trying to terminate EC2 instance with EC2InstanceId " + ec2InstanceId + ".");


        if (ec2InstanceId != null) {

            if (ec2Configuration.getDisable_api_termination()) {
                ec2InstanceManager.enableApiTermination(ec2InstanceId);
            }

            ec2InstanceManager.terminateInstances(Arrays.asList(ec2InstanceId));

            return true;

        } else {
            log.error("Can not find EC2InstanceId entry for the given InstanceId " + instanceId + ".");
            return false;
        }

    }

    @Override
    public int getRunningInstanceCount() {
        return 0;
    }

    @Override
    public int getRunningInstanceCount(String domainName) {
        return 0;
    }

    @Override
    public int getPendingInstanceCount(String domainName) {
        return 0;
    }

    @Override
    public boolean sanityCheck() {
        return false;
    }

    /**
     *  method which calls EC2 Instance Manager when starting new EC2 Instances
     * @param configuration : EC2 Configuration
     * @param diff : Minimum and maximum number of instances to spawn
     * @return  List of instance IDs which are spawned
     */
    private List<String> runInstances(EC2Configuration.Configuration configuration, int diff) {
        RunInstancesRequest request = new RunInstancesRequest(configuration.getImageId(),
                                                              diff, diff);
        request.setInstanceType(configuration.getInstanceType());
        request.setKeyName(ec2Configuration.getEc2_ssh_key());
        request.setSecurityGroups(Arrays.asList(configuration.getSecurityGroups()));
        request.setAdditionalInfo(configuration.getAdditionalInfo());
        request.setUserData(configuration.getUserData());
        request.setPlacement(new Placement(configuration.getAvailability_zone()));
        request.setDisableApiTermination(ec2Configuration.getDisable_api_termination());

        log.info("Calling EC2InstanceManager to spawn new instance");

        return ec2InstanceManager.runInstances(request);

    }

}
