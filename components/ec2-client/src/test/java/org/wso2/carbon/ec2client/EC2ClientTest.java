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
package org.wso2.carbon.ec2client;

import junit.framework.TestCase;
import org.wso2.carbon.ec2client.data.Instance;
import org.wso2.carbon.ec2client.data.InstanceState;
import org.wso2.carbon.ec2client.data.Address;
import org.wso2.carbon.ec2client.data.AvailabilityZone;
import org.wso2.carbon.ec2client.data.SecurityGroup;
import org.wso2.carbon.ec2client.data.Image;
import org.wso2.carbon.ec2client.data.KeyPair;
import org.wso2.carbon.ec2client.data.UserData;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

import sun.misc.BASE64Encoder;

/**
 *
 */
public class EC2ClientTest extends TestCase {

    private EC2Client client;
    private String amiID;
    private String amiInstanceID;

    public static final String AMI_ID = "ami_id";
    public static final String AMI_INSTANCE_ID = "ami_instance_id";
    public static final String PK_FILE = "pk_file";
    public static final String CERT_FILE = "cert_file";

    protected void setUp() throws Exception {
        super.setUp();
        amiID = System.getProperty(AMI_ID);
        if (amiID == null) {
            throw new Exception("AMI id has not been given. please specify it with -Dami_id");
        }
        amiInstanceID = System.getProperty(AMI_INSTANCE_ID);
        if (amiInstanceID == null) {
            throw new Exception("AMI instance id has been not given. please specify it with -Dami_instance_id");
        }

        String pkFile = System.getProperty(PK_FILE);
        if (pkFile == null) {
            throw new Exception("pk file has not been given. please specify it with -Dpk_file");
        }

        String certFile = System.getProperty(CERT_FILE);
        if (certFile == null) {
            throw new Exception("cert file has not been given. please specify it with -Dcert_file");
        }
        client = new EC2Client(pkFile, certFile);
    }

    public void testDescribeImages() throws EC2Exception {
        List<Image> images = client.describeImages();
        assertNotNull(images);
        assertTrue(images.size() > 0);
        for (Image img : images) {
            System.out.println(img);
        }
    }

    public void _testRunInstance() throws EC2Exception {
        UserData data = new UserData();

        //TODO: Move this to the autoscale project
        File f = new File("/home/azeez/Desktop/axis2/payload.zip");
        try {
            byte[] bytes = getBytesFromFile(f);
            BASE64Encoder encoder = new BASE64Encoder();
            String userData = encoder.encode(bytes);
            data.setData(userData);
            System.out.println("Data=" + userData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Instance> ec2Instances = client.runInstances(amiID, 1, null, null, null, null, data, null);
        assertNotNull(ec2Instances);
        assertTrue(ec2Instances.size() > 0);
    }

    // Returns the contents of the file in a byte array.
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    public void testRunAndTerminateInstances() throws EC2Exception {
        List<String> instanceIDs = new ArrayList<String>();
        try {
            List<Instance> ec2Instances = client.runInstances(amiID, 5, null, null, null, null, null, null);
            assertNotNull(ec2Instances);
            assertTrue(ec2Instances.size() > 0);

            for (Instance instance : ec2Instances) {
                instanceIDs.add(instance.getInstanceId());
            }
        } finally {
            List<Instance> instances =
                    client.terminateInstances(instanceIDs.toArray(new String[instanceIDs.size()]));
            assertEquals(instances.size(), 5);
            assertEquals(instances.get(0).getCurrentState(), InstanceState.SHUTTING_DOWN);
        }
    }

    public void testAssociateAddress() throws EC2Exception {

        // Run an instance
        List<Instance> ec2Instances = client.runInstances(amiID, 1, null, null, null, null, null, null);
        assertNotNull(ec2Instances);
        assertEquals(ec2Instances.size(), 1);

        // Allocate an address
        Address address = client.allocateAddress();
        assertNotNull(address);

        // Describe all addresses
        client.describeAddresses(null);

        // Describe addresses.
        List<Address> addressList = client.describeAddresses(new String[]{address.getPublicIp()});
        assertNotNull(addressList);
        assertTrue(addressList.size() > 0);
        for (Address addrs : addressList) {
            System.out.println("Address: " + addrs.getPublicIp() +
                               ", instance=" + addrs.getInstance());
            assertEquals(addrs, address);
            assertNull(addrs.getInstance());
        }

        // Associate the allocated address to the instance we started above
        boolean result = client.associateAddress(ec2Instances.get(0).getInstanceId(), address.getPublicIp());
        assertTrue(result);

        // Check the association
        addressList = client.describeAddresses(new String[]{address.getPublicIp()});
        assertNotNull(addressList);
        assertTrue(addressList.size() > 0);
        for (Address addrs : addressList) {
            System.out.println("Address: " + addrs.getPublicIp() +
                               ", instance=" + addrs.getInstance());
            assertEquals(addrs.getInstance(), ec2Instances.get(0));
            assertEquals(addrs, address);
        }

        // Disassociate the address
        result = client.disassociateAddress(address.getPublicIp());
        assertTrue(result);

        // release the address
        result = client.releaseAddress(address.getPublicIp());
        assertTrue(result);
        try {
            client.describeAddresses(new String[]{address.getPublicIp()});
        } catch (EC2Exception e) {
            assertTrue(e.getCause().getMessage().indexOf("Address '" + address.getPublicIp() + "' not found.") == 0);
        }

        List<Instance> instances =
                client.terminateInstances(new String[]{ec2Instances.get(0).getInstanceId()});
        assertEquals(instances.size(), 1);
        assertEquals(instances.get(0).getCurrentState(), InstanceState.SHUTTING_DOWN);
    }

    public void testDescribeInstances() throws EC2Exception {
        List<Instance> instances = client.describeInstances();
        for (Instance instance : instances) {
            System.out.println(instance);
            client.describeInstance(instance.getInstanceId());
        }
    }

    public void testDescribeRunningAndPendingInstances() throws EC2Exception {
        List<Instance> instances = client.describeInstances();
        System.out.println("Instances: " + instances.size());
        int i = 0;
//        for (Instance instance : instances) {
//            if (instance.getImage().getImageId().equals(amiID) &&
//                instance.getGroupId().equals("autoscale-app") &&
//                (instance.getCurrentState().equals(InstanceState.PENDING) || instance.getCurrentState().equals(InstanceState.RUNNING))) {
//                System.out.println(instance);
//                i++;
//            }
//        }
        System.out.println("Running & pending instances: " + i);
    }

    public void testDescribeNonExistentInstance() {
        try {
            client.describeInstance("i-s1w233333");
        } catch (EC2Exception e) {
            assertTrue(e.getCause().getMessage().indexOf("Invalid id:") == 0);
        }
    }

    public void testDescribeAvailabilityZones() throws EC2Exception {
        List<AvailabilityZone> list = client.describeAvailabilityZones(null);
        assertNotNull(list);
        assertTrue(list.size() > 0);
        for (AvailabilityZone zone : list) {
            assertNotNull(zone);
            System.out.println("Name: " + zone.getZoneName());
            System.out.println("State: " + zone.getZoneState());
            assertNotNull(client.describeAvailabilityZones(new String[]{zone.getZoneName()}));
        }
    }

    public void testDescribeSecurityGroups() throws EC2Exception {
        List<SecurityGroup> list = client.describeSecurityGroups(null);
        for (SecurityGroup sg : list) {
            System.out.println("Name: " + sg.getName());
            System.out.println("Description: " + sg.getDescription());
            System.out.println("Owner ID: " + sg.getOwnerId());
        }
    }

    public void testRebootInstance() throws EC2Exception {
        List<Instance> ec2Instances = client.runInstances(amiID, 1, null, null, null, null, null, null);
        Instance instance = ec2Instances.get(0);

        // Wait till the instance is running
        while (!instance.getCurrentState().equals(InstanceState.RUNNING)) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            instance = client.describeInstance(instance.getInstanceId());
        }

        // Now reboot it
        boolean result = client.rebootInstances(new String[]{instance.getInstanceId()});
        assertTrue(result);

        instance = client.describeInstance(instance.getInstanceId());
        System.out.println("STATE=" + instance.getCurrentState());

        // Finally shut it down
        List<Instance> instances =
                client.terminateInstances(new String[]{instance.getInstanceId()});
        assertEquals(instances.size(), 1);
        assertEquals(instances.get(0).getCurrentState(), InstanceState.SHUTTING_DOWN);
    }

    public void testDescribeInstance() throws EC2Exception {
        assertNotNull(client.describeInstance(amiInstanceID));
    }

    public void testDescribeKeyPairs() throws EC2Exception {
        List<KeyPair> keyPairList = client.describeKeyPairs(null);
        assertNotNull(keyPairList);
        assertTrue(keyPairList.size() > 0);
        for (KeyPair keyPair : keyPairList) {
            System.out.println(keyPair);
        }
    }
}
