package org.wso2.carbon.autoscaler.service.adapters;

import com.amazonaws.services.ec2.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.lb.common.ec2.EC2InstanceManager;
import org.wso2.carbon.lb.common.ec2.EC2Util;
import org.wso2.carbon.lb.common.conf.EC2Configuration;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;

public class EC2Adapter extends Adapter {

    private static final Log log = LogFactory.getLog(EC2Adapter.class);

    private static final String ADAPTER_NAME = "ec2";

    // EC2Instance Manager will handle all the EC2 API level invocations
    private EC2InstanceManager ec2InstanceManager;

    //private EC2Config;
    private EC2Configuration ec2Configuration = new EC2Configuration();


    public EC2Adapter(){
        log.info("Constructing EC2 Adapter...");

        //Need to create ec2Configuration here
        String ec2ConfigFile =
                CarbonUtils.getCarbonConfigDirPath() + File.separator + "ec2.conf";
        ec2Configuration.init(ec2ConfigFile);

        ec2InstanceManager = EC2Util.createEC2InstanceManager(ec2Configuration.getEc2AccessKey(),
                ec2Configuration.getEc2PrivateKey(),
                ec2Configuration.getInstanceMgtEPR());

        log.info("Initialized LoadBalancerConfiguration for EC2 configuration...");


        //  ec2InstanceManager =
    }

    @Override
    public String getName() {
        return ADAPTER_NAME;
    }

    @Override
    public boolean spawnInstance(String domainName, String instanceId) throws ClassNotFoundException, SQLException {
        log.info("Spawning EC2 Instance...");

        EC2Configuration.ServiceConfiguration serviceConfig = ec2Configuration.getServiceConfig(domainName);
       // int instancesPerScaleUp = serviceConfig.getInstancesPerScaleUp();
        log.info("Domain: " + domainName + " Going to start instance " + 1 +
                ". Running instances:" + "runningInstances ??? ");
        runInstances(serviceConfig, 1);
        log.info("Started " + 1 + " new app instances in domain " +
                domainName);

        return true;
    }

    @Override
    public boolean terminateInstance(String instanceId) {

        // Here we have decided to terminate the given instance
        // This decision should make in a intelligent way for better use of ec2 resources.
            if (ec2Configuration.getDisableApiTermination()) {
                ec2InstanceManager.enableApiTermination(instanceId);
            }
            log.info("Going to terminate instance " + instanceId + ".");
            ec2InstanceManager.terminateInstances(Arrays.asList(instanceId));
            log.info("Terminated instance: " + instanceId + ".");
        return true;
    }

    @Override
    public int getRunningInstanceCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getRunningInstanceCount(String domainName) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getPendingInstanceCount(String domainName) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean sanityCheck() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }


    private void runInstances(EC2Configuration.Configuration configuration, int diff) {
        RunInstancesRequest request = new RunInstancesRequest(configuration.getImageId(),
                                                              diff, diff);
        request.setInstanceType(configuration.getInstanceType());
        request.setKeyName(ec2Configuration.getSshKey());
        request.setSecurityGroups(Arrays.asList(configuration.getSecurityGroups()));
        request.setAdditionalInfo(configuration.getAdditionalInfo());
        request.setUserData(configuration.getUserData());
        request.setPlacement(new Placement(configuration.getAvailability_zone()));
        request.setDisableApiTermination(ec2Configuration.getDisableApiTermination());

        ec2InstanceManager.runInstances(request);
    }

//    public static void main(String args[]) {
//        System.out.println("Inside EC2 Adaptor");
//
//        EC2Adapter ec2Adapter = new EC2Adapter();
//        String domainName = "wso2.as.domain";
//        String instanceId = "1";
//
//        // Need to read the policy  file
//
//        try {
//            ec2Adapter.spawnInstance(domainName, instanceId);
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (SQLException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//
//    }

}
