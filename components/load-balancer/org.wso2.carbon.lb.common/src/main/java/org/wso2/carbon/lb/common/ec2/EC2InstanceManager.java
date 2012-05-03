/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.lb.common.ec2;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EC2InstanceManager {
    private static final Log log = LogFactory.getLog(EC2InstanceManager.class);
    private AmazonEC2 ec2;

    public EC2InstanceManager(AmazonEC2 ec2) {
        this.ec2 = ec2;
    }

    public void runInstances(RunInstancesRequest request) {
        try {
            RunInstancesResult result = ec2.runInstances(request);
            Reservation reservation = result.getReservation();
            log.info("Started instances. ReservationID:" + reservation.getReservationId());
            List<Instance> instances = reservation.getInstances();
            for (Instance instance : instances) {
                log.info("Starting instance " + instance.getInstanceId());
            }
        } catch (AmazonClientException e) {
            handleException(e);
        }
    }

    public void terminateInstances(List<String> instanceIds) throws AmazonClientException {
        try {
            TerminateInstancesRequest request = new TerminateInstancesRequest(instanceIds);
            TerminateInstancesResult result = ec2.terminateInstances(request);
            List<InstanceStateChange> terminatingInstances = result.getTerminatingInstances();
            for (InstanceStateChange terminatingInstance : terminatingInstances) {
                log.info("Terminated instance " + terminatingInstance.getInstanceId());
            }
        } catch (AmazonClientException e) {
            handleException(e);
        }
    }

    public List<Reservation> describeInstances() throws AmazonClientException {
        try {
            DescribeInstancesResult result = ec2.describeInstances();
            return result.getReservations();
        } catch (AmazonClientException e) {
            handleException(e);
        }
        return new ArrayList<Reservation>();
    }

    public List<Address> describeAddresses(List<String> elasticIps) throws AmazonClientException {
        try {
            DescribeAddressesRequest request = new DescribeAddressesRequest();
            request.setPublicIps(elasticIps);
            DescribeAddressesResult result = ec2.describeAddresses(request);
            return result.getAddresses();
        } catch (AmazonClientException e) {
            handleException(e);
        }
        return new ArrayList<Address>();
    }

    public Address describeAddress(String elasticIp) throws AmazonClientException {
        try {
            DescribeAddressesRequest request = new DescribeAddressesRequest();
            request.setPublicIps(Arrays.asList(elasticIp));
            DescribeAddressesResult result = ec2.describeAddresses(request);
            return result.getAddresses().get(0);
        } catch (AmazonClientException e) {
            handleException(e);
        }
        return null;
    }

    public void associateAddress(String instanceId, String elasticIP) throws AmazonClientException {
        try {
            AssociateAddressRequest request = new AssociateAddressRequest();
            request.setInstanceId(instanceId);
            request.setPublicIp(elasticIP);
            AssociateAddressResult result = ec2.associateAddress(request);
            log.info("Associated instance " + instanceId + " with elastic IP " + elasticIP +
                     ". AssociationID:" + result.getAssociationId());
        } catch (AmazonClientException e) {
            handleException(e);
        }
    }

    public Instance describeInstance(String instanceId) {
        if (instanceId == null || instanceId.isEmpty()) {
            return null;
        }
        try {
            DescribeInstancesRequest request = new DescribeInstancesRequest();
            request.withInstanceIds(instanceId);
            DescribeInstancesResult result = ec2.describeInstances(request);
            List<Reservation> reservations = result.getReservations();
            if (reservations != null && !reservations.isEmpty()) {
                return reservations.get(0).getInstances().get(0);
            }
        } catch (AmazonClientException e) {
            handleException(e);
        }
        return null;
    }

    public void setInstanceAttribute(String instanceId, String name, String value) {
        ModifyInstanceAttributeRequest request = new ModifyInstanceAttributeRequest();
        request.setAttribute(name);
        request.setValue(value);
        request.setInstanceId(instanceId);
        try {
            ec2.modifyInstanceAttribute(request);
        } catch (AmazonClientException e) {
            handleException(e);
        }
    }

    public void enableApiTermination(String instanceId) {
        setInstanceAttribute(instanceId, "disable_api_termination", "false");
    }

    private void handleException(AmazonClientException e) throws AmazonClientException {
        if (e.getMessage().toLowerCase().contains("Request Limit exceeded")) {
            log.debug("EC2 Request limit exceeded", e);
        } else {
            throw e;
        }
    }
}
