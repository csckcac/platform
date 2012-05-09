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
    public boolean spawnInstance(String domainName, String instanceId) throws ClassNotFoundException, SQLException {

        log.info("Trying to spawn instance in EC2 Adapter");

        EC2Configuration.ServiceConfiguration serviceConfig = ec2Configuration.getServiceConfig(domainName);

        runInstances(serviceConfig, 1);

        log.info("Started " + 1 + " new instances in domain " +
                domainName);

        // At this point the the server will starts after some time (Sleep for 2 minutes)
        try {
            Thread.sleep(120000);
        } catch (InterruptedException e) {
            log.error("InterruptedException while waiting for new instance startup" , e);
        }

        // Reducing the pending instance count by one in AutoscalerService
        // Here we assumes that the server instance is successfully started
        autoscalerService.addPendingInstanceCount(domainName, -1);

        return true;
    }

    @Override
    public boolean terminateInstance(String instanceId) {

        log.info("Trying to terminate EC2 instance " + instanceId + ".");

        // Here we have decided to terminate the given instance
        // This decision should make in a intelligent way for better use of ec2 resources.
        if (ec2Configuration.getDisable_api_termination()) {
            ec2InstanceManager.enableApiTermination(instanceId);
        }

        ec2InstanceManager.terminateInstances(Arrays.asList(instanceId));

        return true;
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
     * method which calls EC2 Instance Manager when starting new EC2 Instances
     * @param configuration : EC2 Configuration
     * @param diff : Minimum and maximum number of instances to spawn
     */
    private void runInstances(EC2Configuration.Configuration configuration, int diff) {
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
        ec2InstanceManager.runInstances(request);
    }

}
