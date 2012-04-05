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

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.wso2.carbon.ec2.client.stub.*;
import org.wso2.carbon.ec2client.data.Address;
import org.wso2.carbon.ec2client.data.AvailabilityZone;
import org.wso2.carbon.ec2client.data.Image;
import org.wso2.carbon.ec2client.data.Instance;
import org.wso2.carbon.ec2client.data.InstanceStateFactory;
import org.wso2.carbon.ec2client.data.InstanceType;
import org.wso2.carbon.ec2client.data.KeyPair;
import org.wso2.carbon.ec2client.data.SecurityGroup;
import org.wso2.carbon.ec2client.data.UserData;
import org.wso2.carbon.ec2client.utils.KeyImporter;
import org.wso2.carbon.ec2client.utils.PKCS1;
import org.wso2.carbon.ec2client.utils.PWCBHandler;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 *
 */
public class EC2Client {

    private static final String EC2_PRIVATE_KEY = "EC2_PRIVATE_KEY";
    private static final String EC2_CERT = "EC2_CERT";
    private static final String EC2_JKS = System.getProperty("java.io.tmpdir") + File.separator +
                                          "ec2.jks";
    private static final String EC2_KS_PASSWORD =
            Math.random() + "ec2kspwd" + System.currentTimeMillis();
    private static final String EC2_USER = "ec2user";

    private static final Log log = LogFactory.getLog(EC2Client.class);

    private AmazonEC2Stub stub;
    private String ec2PrivateKey;
    private String ec2Cert;

    private static final String POLICY =
            "<wsp:Policy wsu:Id=\"SigOnly\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" " +
            "xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">" +
            " <wsp:ExactlyOne>" +
            "  <wsp:All>" +
            "   <sp:AsymmetricBinding xmlns:sp=\"http://schemas.xmlsoap.org/ws/2005/07/securitypolicy\">" +
            "    <wsp:Policy>" +
            "     <sp:InitiatorToken>" +
            "      <wsp:Policy>" +
            "       <sp:X509Token sp:IncludeToken=\"http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/AlwaysToRecipient\">" +
            "        <wsp:Policy>" +
            "         <sp:WssX509V3Token10/>" +
            "        </wsp:Policy>" +
            "       </sp:X509Token>" +
            "      </wsp:Policy>" +
            "     </sp:InitiatorToken>" +
            "     <sp:RecipientToken>" +
            "      <wsp:Policy>" +
            "       <sp:X509Token sp:IncludeToken=\"http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/Never\">" +
            "        <wsp:Policy>" +
            "         <sp:WssX509V3Token10/>" +
            "        </wsp:Policy>" +
            "       </sp:X509Token>" +
            "      </wsp:Policy>" +
            "     </sp:RecipientToken>" +
            "     <sp:AlgorithmSuite>" +
            "      <wsp:Policy>" +
            "       <sp:TripleDesRsa15/>" +
            "      </wsp:Policy>" +
            "     </sp:AlgorithmSuite>" +
            "     <sp:Layout>" +
            "      <wsp:Policy>" +
            "       <sp:Strict/>" +
            "      </wsp:Policy>" +
            "     </sp:Layout>" +
            "     <sp:IncludeTimestamp/>" +
            "     <sp:OnlySignEntireHeadersAndBody/>" +
            "    </wsp:Policy>" +
            "   </sp:AsymmetricBinding>" +
            "   <sp:Wss10 xmlns:sp=\"http://schemas.xmlsoap.org/ws/2005/07/securitypolicy\">" +
            "    <wsp:Policy>" +
            "     <sp:MustSupportRefKeyIdentifier/>" +
            "     <sp:MustSupportRefIssuerSerial/>" +
            "    </wsp:Policy>" +
            "   </sp:Wss10>" +
            "   <sp:SignedParts xmlns:sp=\"http://schemas.xmlsoap.org/ws/2005/07/securitypolicy\">" +
            "    <sp:Body/>" +
            "   </sp:SignedParts>" +
            "   <ramp:RampartConfig xmlns:ramp=\"http://ws.apache.org/rampart/policy\"> " +
            "    <ramp:user>" + EC2_USER + "</ramp:user>" +
            "    <ramp:encryptionUser>service</ramp:encryptionUser>" +
            "    <ramp:passwordCallbackClass>org.wso2.carbon.ec2client.utils.PWCBHandler</ramp:passwordCallbackClass>" +
            "    <ramp:signatureCrypto>" +
            "     <ramp:crypto provider=\"org.apache.ws.security.components.crypto.Merlin\">" +
            "      <ramp:property name=\"org.apache.ws.security.crypto.merlin.keystore.type\">JKS</ramp:property>" +
            "      <ramp:property name=\"org.apache.ws.security.crypto.merlin.file\">" + EC2_JKS + "</ramp:property>" +
            "      <ramp:property name=\"org.apache.ws.security.crypto.merlin.keystore.password\">" + EC2_KS_PASSWORD + "</ramp:property>" +
            "     </ramp:crypto>" +
            "    </ramp:signatureCrypto>" +
            "   </ramp:RampartConfig>" +
            "  </wsp:All>" +
            " </wsp:ExactlyOne>" +
            "</wsp:Policy>";


    public EC2Client(String ec2PrivateKey, String ec2Cert) throws EC2Exception {
        
        this.ec2PrivateKey = ec2PrivateKey;
        this.ec2Cert = ec2Cert;
        try {
            stub = new AmazonEC2Stub();
            stub._getServiceClient().getOptions().
                    setProperty(AddressingConstants.INCLUDE_OPTIONAL_HEADERS, Constants.VALUE_TRUE);
            stub._getServiceClient().getOptions().
                    setReplyTo(new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL));
        } catch (AxisFault e) {
            String msg = "Cannot create stub";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }
        init();
    }

    public EC2Client(ConfigurationContext configurationContext,
                     String targetEndpoint,
                     String ec2PrivateKey,
                     String ec2Cert) throws EC2Exception {
        this.ec2PrivateKey = ec2PrivateKey;
        this.ec2Cert = ec2Cert;
        try {
            stub = new AmazonEC2Stub(configurationContext, targetEndpoint);
            stub._getServiceClient().getOptions().
                    setProperty(AddressingConstants.INCLUDE_OPTIONAL_HEADERS, Constants.VALUE_TRUE);
            stub._getServiceClient().getOptions().
                    setReplyTo(new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL));
        } catch (AxisFault e) {
            String msg = "Cannot create stub";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }
        init();
    }

    public EC2Client(ConfigurationContext configurationContext,
                     String ec2PrivateKey,
                     String ec2Cert) throws EC2Exception {
        this.ec2PrivateKey = ec2PrivateKey;
        this.ec2Cert = ec2Cert;
        try {
            stub = new AmazonEC2Stub(configurationContext);
            stub._getServiceClient().getOptions().
                    setProperty(AddressingConstants.INCLUDE_OPTIONAL_HEADERS, Constants.VALUE_TRUE);
            stub._getServiceClient().getOptions().
                    setReplyTo(new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL));
        } catch (AxisFault e) {
            String msg = "Cannot create stub";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }
        init();
    }

    public EC2Client() throws EC2Exception {
//        stub = new AmazonEC2Stub("http://localhost:8080/");
        try {
            stub = new AmazonEC2Stub();
        } catch (AxisFault e) {
            String msg = "Cannot create stub";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }
        init();
    }

    private void init() throws EC2Exception {
        if (ec2Cert == null) {
            ec2Cert = System.getenv(EC2_CERT);
            if (ec2Cert == null) {
                ec2Cert = System.getProperty("ec2Cert");
            }
        }
        if (ec2PrivateKey == null) {
            ec2PrivateKey = System.getenv(EC2_PRIVATE_KEY);
            if (ec2PrivateKey == null) {
                ec2PrivateKey = System.getProperty("ec2PrivateKey");
            }
        }

        if (ec2Cert == null || ec2PrivateKey == null) {
            throw new IllegalArgumentException("The" + EC2_CERT + " and/or " + EC2_PRIVATE_KEY +
                                               " variables have not been set.");
        }

        // Convert PEM to DER
        ec2PrivateKey = pem2der(ec2PrivateKey);
        ec2Cert = pem2der(ec2Cert);

        File jks = new File(EC2_JKS);
        if (jks.exists()) {
            jks.delete();
        }
        PWCBHandler.password = EC2_KS_PASSWORD;
        KeyImporter.doImport(EC2_JKS, ec2PrivateKey, ec2Cert, EC2_USER, EC2_KS_PASSWORD);
    }

    private String pem2der(String fileName) throws EC2Exception {
        String derFile;
        byte[] bytes;
        try {
            bytes = new PKCS1().readDecodedBytes(fileName);
        } catch (IOException e) {
            String msg = "Cannot read decoded bytes";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }
        int indexOfExt = fileName.indexOf(".pem");
        int indexOfSeparator = fileName.lastIndexOf(File.separator);
        if (indexOfSeparator != -1) {
            if (indexOfExt != -1) {
                derFile = System.getProperty("java.io.tmpdir") + File.separator +
                          fileName.substring(indexOfSeparator + 1, indexOfExt) + ".der";
            } else {
                derFile = System.getProperty("java.io.tmpdir") + File.separator +
                          fileName.substring(indexOfSeparator + 1) + ".der";
            }
        } else {
            if (indexOfExt != -1) {
                derFile = System.getProperty("java.io.tmpdir") + File.separator +
                          fileName.substring(0, indexOfExt) + ".der";
            } else {
                derFile = System.getProperty("java.io.tmpdir") + File.separator +
                          fileName + ".der";
            }
        }
        File f = new File(derFile);
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fop = new FileOutputStream(f);
            fop.write(bytes);
            fop.flush();
            fop.close();
        } catch (IOException e) {
            String msg = "Cannot write to file " + derFile;
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }
        return derFile;
    }

    public List<Image> describeImages() throws EC2Exception {
        DescribeImages describeImages = new DescribeImages();
        DescribeImagesType describeImagesType = new DescribeImagesType();
        describeImagesType.setImagesSet(new DescribeImagesInfoType());
        describeImagesType.setOwnersSet(new DescribeImagesOwnersType());
        describeImagesType.setExecutableBySet(new DescribeImagesExecutableBySetType());
        describeImages.setDescribeImages(describeImagesType);

        enableSecurity("describeImages");

        DescribeImagesResponse response;
        try {
            response = stub.describeImages(describeImages);
        } catch (RemoteException e) {
            String msg = "Cannot invoke AWS";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }
        DescribeImagesResponseItemType[] items =
                response.getDescribeImagesResponse().getImagesSet().getItem();
        List<Image> images = new ArrayList<Image>();
        for (DescribeImagesResponseItemType item : items) {
            Image image = new Image(item.getImageId());
            image.setImageId(item.getImageLocation());
            image.setState(item.getImageState());
            image.setType(item.getImageType());
            image.setOwnerId(item.getImageOwnerId());
            image.setPublic(item.getIsPublic());
            image.setKernelId(item.getKernelId());
            image.setArchitecture(item.getArchitecture());
            image.setRamDiskId(item.getRamdiskId());
            images.add(image);
        }
        return images;
    }

    public List<Instance> runInstances(String amiID,
                                       int numberOfInstances,
                                       InstanceType instanceType,
                                       String keyName,
                                       String [] groupIds,
                                       String additionalInfo,
                                       UserData userData,
                                       AvailabilityZone zone) throws EC2Exception {
        log.debug("Running instances...");
        if (instanceType == null) {
            instanceType = InstanceType.SMALL;
        }
        RunInstances instances = new RunInstances();
        RunInstancesType instancesType = new RunInstancesType();
        instancesType.setImageId(amiID);
        if (userData != null) {
            UserDataType userDataType = new UserDataType();

            userDataType.setVersion(userData.getVersion());
            userDataType.setEncoding(userData.getEncoding());
            userDataType.setData(userData.getData());
            instancesType.setUserData(userDataType);
        }
        if (additionalInfo != null) {
            instancesType.setAdditionalInfo(additionalInfo);
        }

        // Set the availability zone
        if (zone != null) {
            PlacementRequestType placement = new PlacementRequestType();
            placement.setAvailabilityZone(zone.getZoneName());
            instancesType.setPlacement(placement);
        }

        instancesType.setKeyName(keyName);
        instancesType.setMinCount(numberOfInstances);
        instancesType.setMaxCount(numberOfInstances);
        instancesType.setInstanceType(instanceType.getType());

        GroupItemType[] groupItemTypes = new GroupItemType[groupIds.length];
        for(int i = 0; i < groupIds.length; i++){
            GroupItemType groupItemType = new GroupItemType();
            groupItemType.setGroupId(groupIds[i]);
            groupItemTypes[i] = groupItemType;    
        }
        
        GroupSetType groupSetType = new GroupSetType();
        groupSetType.setItem(groupItemTypes);
        instancesType.setGroupSet(groupSetType);     

        instances.setRunInstances(instancesType);
        if (keyName != null) {
            instancesType.setKeyName(keyName);
        }
        enableSecurity("runInstances");
        RunInstancesResponse response;
        try {
            response = stub.runInstances(instances);
        } catch (RemoteException e) {
            String msg = "Cannot invoke AWS";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }

        RunningInstancesItemType[] runningInstancesItemTypes =
                response.getRunInstancesResponse().getInstancesSet().getItem();
        List<Instance> list = new ArrayList<Instance>();
        for (RunningInstancesItemType itemType : runningInstancesItemTypes) {
            Instance instance = new Instance();
            instance.setInstanceId(itemType.getInstanceId());
            instance.setInternalName(itemType.getPrivateDnsName());
            instance.setExternalName(itemType.getDnsName());
            instance.setLaunchTime(itemType.getLaunchTime());
            instance.setInstanceType(itemType.getInstanceType());

            String availabilityZone = itemType.getPlacement().getAvailabilityZone();
            if (availabilityZone != null) {
                AvailabilityZone z = describeAvailabilityZones(new String[]{availabilityZone}).get(0);
                instance.setAvailabilityZone(z);
            }

            instance.setCurrentState(InstanceStateFactory.get(itemType.getInstanceState().getCode()));
            Image image = new Image(itemType.getImageId());
            instance.setImage(image);

            list.add(instance);
        }
        return list;
    }

    public List<Instance> describeInstances() throws EC2Exception {
        DescribeInstances describeInstances = new DescribeInstances();
        DescribeInstancesType instancesType = new DescribeInstancesType();
        DescribeInstancesInfoType type = new DescribeInstancesInfoType();
        instancesType.setInstancesSet(type);
        describeInstances.setDescribeInstances(instancesType);

        enableSecurity("describeInstances");

        DescribeInstancesResponse response;
        try {
            response = stub.describeInstances(describeInstances);
        } catch (RemoteException e) {
            String msg = "Cannot invoke AWS";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }

        ReservationSetType reservationSet =
                response.getDescribeInstancesResponse().getReservationSet();

        List<Instance> list = new ArrayList<Instance>();
        int i = 0;
        for (ReservationInfoType infoType : reservationSet.getItem()) {
            for (RunningInstancesItemType instanceType : infoType.getInstancesSet().getItem()) {
//            for (AmazonEC2Stub.GroupItemType groupType : infoType.getGroupSet().getItem()) {
                i++;
                Instance instance = new Instance();
                instance.setOwnerId(infoType.getOwnerId());
                instance.setReservationId(infoType.getReservationId());
                 

                instance.setInstanceId(instanceType.getInstanceId());
                instance.setInternalName(instanceType.getPrivateDnsName());
                instance.setExternalName(instanceType.getDnsName());
                instance.setLaunchTime(instanceType.getLaunchTime());
                instance.setInstanceType(instanceType.getInstanceType());

                instance.setCurrentState(InstanceStateFactory.get(instanceType.getInstanceState().getCode()));

//            System.out.println("State code=" + infoType.getInstancesSet().getItem()[0].getInstanceState().getCode());
//            System.out.println("State name=" + infoType.getInstancesSet().getItem()[0].getInstanceState().getName());

                String availabilityZone = instanceType.getPlacement().getAvailabilityZone();
                if (availabilityZone != null && availabilityZone.length() > 0) {
                    AvailabilityZone z = describeAvailabilityZones(new String[]{availabilityZone}).get(0); // TODO: WIll need to set multiple av zonez
                    instance.setAvailabilityZone(z);
                }

                setGroupIDs(instance, infoType.getGroupSet().getItem());
                Image image = new Image(instanceType.getImageId());
                instance.setImage(image);


                list.add(instance);
            }
        }
        return list;
    }

    public Instance describeInstance(String instanceId) throws EC2Exception {
        DescribeInstances describeInstances = new DescribeInstances();
        DescribeInstancesType instancesType = new DescribeInstancesType();
        DescribeInstancesInfoType type = new DescribeInstancesInfoType();
        DescribeInstancesItemType instancesItemType = new DescribeInstancesItemType();
        instancesItemType.setInstanceId(instanceId);
        type.setItem(new DescribeInstancesItemType[]{instancesItemType});
        instancesType.setInstancesSet(type);
        describeInstances.setDescribeInstances(instancesType);

        enableSecurity("describeInstances");

        DescribeInstancesResponse response;
        try {
            response = stub.describeInstances(describeInstances);
        } catch (RemoteException e) {
            String msg = "Cannot invoke AWS";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }

        ReservationSetType reservationSet =
                response.getDescribeInstancesResponse().getReservationSet();
        if (reservationSet == null || reservationSet.getItem() == null ||
            reservationSet.getItem().length == 0) {
            return null;
        }
        ReservationInfoType infoType = reservationSet.getItem()[0];
        if (infoType != null) {
            Instance instance = new Instance();
            instance.setOwnerId(infoType.getOwnerId());
            instance.setReservationId(infoType.getReservationId());
            instance.setInstanceId(infoType.getInstancesSet().getItem()[0].getInstanceId());
            instance.setInternalName(infoType.getInstancesSet().getItem()[0].getPrivateDnsName());
            instance.setExternalName(infoType.getInstancesSet().getItem()[0].getDnsName());
            instance.setLaunchTime(infoType.getInstancesSet().getItem()[0].getLaunchTime());
            instance.setInstanceType(infoType.getInstancesSet().getItem()[0].getInstanceType());

            instance.setCurrentState(InstanceStateFactory.get(infoType.getInstancesSet().getItem()[0].getInstanceState().getCode()));

//            System.out.println("State code=" + infoType.getInstancesSet().getItem()[0].getInstanceState().getCode());
//            System.out.println("State name=" + infoType.getInstancesSet().getItem()[0].getInstanceState().getName());

            setGroupIDs(instance, infoType.getGroupSet().getItem());
            Image image = new Image(infoType.getInstancesSet().getItem()[0].getImageId());
            instance.setImage(image);

            return instance;
        }
        return null;
    }

    public List<Instance> terminateInstances(String[] instanceIDs) throws EC2Exception {
        log.debug("Terminating instances...");
        TerminateInstances instances = new TerminateInstances();
        TerminateInstancesType instancesType = new TerminateInstancesType();
        TerminateInstancesInfoType terminateInstancesInfoType = new TerminateInstancesInfoType();

        List<TerminateInstancesItemType> types =
                new ArrayList<TerminateInstancesItemType>();
        for (String instanceID : instanceIDs) {
            TerminateInstancesItemType type = new TerminateInstancesItemType();
            type.setInstanceId(instanceID);
            types.add(type);
        }

        terminateInstancesInfoType.setItem(types.toArray(new TerminateInstancesItemType[types.size()]));
        instancesType.setInstancesSet(terminateInstancesInfoType);
        instances.setTerminateInstances(instancesType);
        enableSecurity("terminateInstances");
        TerminateInstancesResponse response;
        try {
            response = stub.terminateInstances(instances);
        } catch (RemoteException e) {
            String msg = "Cannot invoke AWS";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }
        TerminateInstancesResponseItemType[] itemTypes =
                response.getTerminateInstancesResponse().getInstancesSet().getItem();
        List<Instance> list = new ArrayList<Instance>();
        for (TerminateInstancesResponseItemType itemType : itemTypes) {
            Instance instance = new Instance();
            instance.setInstanceId(itemType.getInstanceId());
            instance.setCurrentState(InstanceStateFactory.get(itemType.getShutdownState().getCode()));
            instance.setPreviousState(InstanceStateFactory.get(itemType.getPreviousState().getCode()));
            list.add(instance);            
        }
        return list;
    }

    public boolean associateAddress(String instanceId, String publicIp) throws EC2Exception {
        AssociateAddress address = new AssociateAddress();
        AssociateAddressType associateAddressType = new AssociateAddressType();
        associateAddressType.setInstanceId(instanceId);
        associateAddressType.setPublicIp(publicIp);
        address.setAssociateAddress(associateAddressType);

        enableSecurity("associateAddress");
        AssociateAddressResponse response;
        try {
            response = stub.associateAddress(address);
        } catch (RemoteException e) {
            String msg = "Cannot invoke AWS";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }
        AssociateAddressResponseType responseType =
                response.getAssociateAddressResponse();
        return responseType.get_return();
    }

    public boolean disassociateAddress(String publicIp) throws EC2Exception {
        DisassociateAddress disassociateAddress = new DisassociateAddress();
        DisassociateAddressType disassociateAddressType = new DisassociateAddressType();
        disassociateAddressType.setPublicIp(publicIp);
        disassociateAddress.setDisassociateAddress(disassociateAddressType);
        enableSecurity("disassociateAddress");
        DisassociateAddressResponse response;
        try {
            response = stub.disassociateAddress(disassociateAddress);
        } catch (RemoteException e) {
            String msg = "Cannot invoke AWS";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }
        return response.getDisassociateAddressResponse().get_return();
    }

    public Address allocateAddress() throws EC2Exception {
        AllocateAddress address = new AllocateAddress();
        AllocateAddressType allocateAddressType = new AllocateAddressType();
        address.setAllocateAddress(allocateAddressType);
        enableSecurity("allocateAddress");
        AllocateAddressResponse response;
        try {
            response = stub.allocateAddress(address);
        } catch (RemoteException e) {
            String msg = "Cannot invoke AWS";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }
        Address addrs = new Address(response.getAllocateAddressResponse().getPublicIp());
        log.info("Allocated public IP address " + addrs.getPublicIp());
        return addrs;
    }

    public boolean releaseAddress(String publicIp) throws EC2Exception {
        log.debug("Releasing address " + publicIp + "...");
        ReleaseAddress address = new ReleaseAddress();
        ReleaseAddressType releaseAddressType = new ReleaseAddressType();
        releaseAddressType.setPublicIp(publicIp);
        address.setReleaseAddress(releaseAddressType);
        enableSecurity("releaseAddress");
        ReleaseAddressResponse response;
        try {
            response = stub.releaseAddress(address);
        } catch (RemoteException e) {
            String msg = "Cannot invoke AWS";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }
        boolean result = response.getReleaseAddressResponse().get_return();
        if (result) {
            log.info("Released public IP address " + publicIp);
        }
        return result;
    }

    public Image registerImage(String imageLocation) throws EC2Exception {
        RegisterImage image = new RegisterImage();
        RegisterImageType type = new RegisterImageType();
        type.setImageLocation(imageLocation);
        image.setRegisterImage(type);
        enableSecurity("registerImage");
        RegisterImageResponse response;
        try {
            response = stub.registerImage(image);
        } catch (RemoteException e) {
            String msg = "Cannot invoke AWS";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }
        return new Image(response.getRegisterImageResponse().getImageId());
    }

    public boolean deregisterImage(String imageId) throws EC2Exception {
        DeregisterImage deregisterImage = new DeregisterImage();
        DeregisterImageType deregisterImageType = new DeregisterImageType();
        deregisterImageType.setImageId(imageId);
        deregisterImage.setDeregisterImage(deregisterImageType);
        enableSecurity("deregisterImage");
        DeregisterImageResponse response;
        try {
            response = stub.deregisterImage(deregisterImage);
        } catch (RemoteException e) {
            String msg = "Cannot invoke AWS";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }
        return response.getDeregisterImageResponse().get_return();
    }

    public List<Address> describeAddresses(String[] publicIPs) throws EC2Exception {
        log.debug("Describing addresses...");
        DescribeAddresses addresses = new DescribeAddresses();
        DescribeAddressesType addressesType = new DescribeAddressesType();
        DescribeAddressesInfoType describeAddressesInfoType = new DescribeAddressesInfoType();

        if (publicIPs != null && publicIPs.length > 0) {
            List<DescribeAddressesItemType> list = new ArrayList<DescribeAddressesItemType>();
            for (String publicIP : publicIPs) {
                DescribeAddressesItemType item = new DescribeAddressesItemType();
                item.setPublicIp(publicIP);
                list.add(item);
            }
            describeAddressesInfoType.setItem(list.toArray(new DescribeAddressesItemType[list.size()]));
        }
        addressesType.setPublicIpsSet(describeAddressesInfoType);
        addresses.setDescribeAddresses(addressesType);
        enableSecurity("describeAddresses");
        DescribeAddressesResponse response;
        try {
            response = stub.describeAddresses(addresses);
        } catch (RemoteException e) {
            String msg = "Cannot invoke AWS";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }
        DescribeAddressesResponseItemType[] items = response.getDescribeAddressesResponse().getAddressesSet().getItem();
        List<Address> addressList = new ArrayList<Address>();
        for (DescribeAddressesResponseItemType item : items) {
            String id = item.getInstanceId();
            Instance instance = null;
            if (id != null && id.length() > 0) {
                instance = describeInstance(id);
            }
            addressList.add(new Address(instance, item.getPublicIp()));
        }
        return addressList;
    }

    public List<KeyPair> describeKeyPairs(String[] keyNames) throws EC2Exception {
        log.debug("Describing key pairs...");
        DescribeKeyPairs pairs = new DescribeKeyPairs();
        DescribeKeyPairsType describeKeyPairsType = new DescribeKeyPairsType();
        DescribeKeyPairsInfoType pairsInfoType = new DescribeKeyPairsInfoType();

        if (keyNames != null && keyNames.length > 0) {
            List<DescribeKeyPairsItemType> types = new ArrayList<DescribeKeyPairsItemType>();
            for (String keyName : keyNames) {
                DescribeKeyPairsItemType type = new DescribeKeyPairsItemType();
                type.setKeyName(keyName);
                types.add(type);
            }
            pairsInfoType.setItem(types.toArray(new DescribeKeyPairsItemType[types.size()]));
        }
        describeKeyPairsType.setKeySet(pairsInfoType);
        pairs.setDescribeKeyPairs(describeKeyPairsType);
        enableSecurity("describeKeyPairs");

        DescribeKeyPairsResponse response;
        try {
            response = stub.describeKeyPairs(pairs);
        } catch (RemoteException e) {
            String msg = "Cannot invoke AWS";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }

        List<KeyPair> keyPairs = new ArrayList<KeyPair>();
        DescribeKeyPairsResponseItemType[] items =
                response.getDescribeKeyPairsResponse().getKeySet().getItem();
        for (DescribeKeyPairsResponseItemType item : items) {
            KeyPair keyPair = new KeyPair(item.getKeyName(), item.getKeyFingerprint());
            keyPairs.add(keyPair);
        }
        return keyPairs;
    }

    public List<AvailabilityZone> describeAvailabilityZones(String[] availabilityZones)
            throws EC2Exception {
        log.debug("Describing availability zones...");
        DescribeAvailabilityZones zones = new DescribeAvailabilityZones();
        DescribeAvailabilityZonesType describeAvailabilityZonesType = new DescribeAvailabilityZonesType();
        DescribeAvailabilityZonesSetType type = new DescribeAvailabilityZonesSetType();

        if (availabilityZones != null && availabilityZones.length > 0) {
            List<DescribeAvailabilityZonesSetItemType> items = new ArrayList<DescribeAvailabilityZonesSetItemType>();
            for (String availabilityZone : availabilityZones) {
                DescribeAvailabilityZonesSetItemType item = new DescribeAvailabilityZonesSetItemType();
                item.setZoneName(availabilityZone);
                items.add(item);
            }
            type.setItem(items.toArray(new DescribeAvailabilityZonesSetItemType[items.size()]));
        }

        describeAvailabilityZonesType.setAvailabilityZoneSet(type);
        zones.setDescribeAvailabilityZones(describeAvailabilityZonesType);
        enableSecurity("describeAvailabilityZones");
        DescribeAvailabilityZonesResponse response;
        try {
            response = stub.describeAvailabilityZones(zones);
        } catch (RemoteException e) {
            String msg = "Cannot invoke AWS";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }
        List<AvailabilityZone> zoneList = new ArrayList<AvailabilityZone>();
        AvailabilityZoneItemType[] zoneItemTypes =
                response.getDescribeAvailabilityZonesResponse().getAvailabilityZoneInfo().getItem();
        for (AvailabilityZoneItemType zoneItemType : zoneItemTypes) {
            AvailabilityZone zone = new AvailabilityZone(zoneItemType.getZoneName(),
                                                         zoneItemType.getZoneState());
            zoneList.add(zone);
        }
        return zoneList;
    }

    public List<SecurityGroup> describeSecurityGroups(String[] securityGroupNames)
            throws EC2Exception {
        log.debug("Describing security groups...");
        DescribeSecurityGroups groups = new DescribeSecurityGroups();
        DescribeSecurityGroupsType describeSecurityGroupsType = new DescribeSecurityGroupsType();
        DescribeSecurityGroupsSetType securityGroupsSetType = new DescribeSecurityGroupsSetType();
        describeSecurityGroupsType.setSecurityGroupSet(securityGroupsSetType);
        DescribeSecurityGroupsSetType describeSecurityGroupsSetType = new DescribeSecurityGroupsSetType();

        if (securityGroupNames != null && securityGroupNames.length > 0) {
            List<DescribeSecurityGroupsSetItemType> items = new ArrayList<DescribeSecurityGroupsSetItemType>();
            for (String sg : securityGroupNames) {
                DescribeSecurityGroupsSetItemType item = new DescribeSecurityGroupsSetItemType();
                item.setGroupName(sg);
                items.add(item);
            }
            describeSecurityGroupsSetType.setItem(items.toArray(new DescribeSecurityGroupsSetItemType[items.size()]));
        }
        describeSecurityGroupsType.setSecurityGroupSet(describeSecurityGroupsSetType);
        groups.setDescribeSecurityGroups(describeSecurityGroupsType);
        enableSecurity("describeSecurityGroups");
        DescribeSecurityGroupsResponse response;
        try {
            response = stub.describeSecurityGroups(groups);
        } catch (RemoteException e) {
            String msg = "Cannot invoke AWS";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }
        List<SecurityGroup> sgList = new ArrayList<SecurityGroup>();
        SecurityGroupItemType[] securityGroupItemTypes =
                response.getDescribeSecurityGroupsResponse().getSecurityGroupInfo().getItem();
        for (SecurityGroupItemType sgItemType : securityGroupItemTypes) {
            SecurityGroup sg = new SecurityGroup(sgItemType.getGroupName());
            sg.setDescription(sgItemType.getGroupDescription());
            sg.setOwnerId(sgItemType.getOwnerId());
            /*
            TODO: Set the permissions
            AmazonEC2Stub.IpPermissionSetType ipPermissions = sgItemType.getIpPermissions();
            AmazonEC2Stub.IpPermissionType[] permissionTypes = ipPermissions.getItem();
            for (AmazonEC2Stub.IpPermissionType permissionType : permissionTypes) {
            permissionType.getFromPort();
                permissionType.getToPort();
                permissionType.getIpProtocol();
            }
            sg.setIpPermissions(ipPermissions)*/
            sgList.add(sg);
        }
        return sgList;
    }

    public boolean rebootInstances(String[] instanceIDs) throws EC2Exception {
        log.debug("Rebooting instances...");
        RebootInstances instances = new RebootInstances();
        RebootInstancesType instancesType = new RebootInstancesType();
        RebootInstancesInfoType rebootInstancesInfoType = new RebootInstancesInfoType();

        List<RebootInstancesItemType> list = new ArrayList<RebootInstancesItemType>();
        for (String instanceID : instanceIDs) {
            RebootInstancesItemType type = new RebootInstancesItemType();
            type.setInstanceId(instanceID);
            list.add(type);
        }
        rebootInstancesInfoType.setItem(list.toArray(new RebootInstancesItemType[list.size()]));
        instancesType.setInstancesSet(rebootInstancesInfoType);
        instances.setRebootInstances(instancesType);
        enableSecurity("rebootInstances");
        RebootInstancesResponse response;
        try {
            response = stub.rebootInstances(instances);
        } catch (RemoteException e) {
            String msg = "Cannot invoke AWS";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }
        return response.getRebootInstancesResponse().get_return();
    }

    private void enableSecurity(String operation) throws EC2Exception {
        ServiceClient serviceClient = stub._getServiceClient();
        AxisService axisService = serviceClient.getAxisService();
        AxisOperation axisOperation = axisService.getOperation(new QName(operation));
        axisOperation.getMessage("Out").getPolicySubject().attachPolicy(loadPolicy());

        try {
            serviceClient.engageModule("rampart");
        } catch (AxisFault e) {
            String msg = "Cannot engage Rampart module";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }
    }

    private Policy loadPolicy() throws EC2Exception {
        StAXOMBuilder builder;
        try {
            builder = new StAXOMBuilder(new ByteArrayInputStream(POLICY.getBytes()));
        } catch (XMLStreamException e) {
            String msg = "Cannot load security policy";
            log.error(msg, e);
            throw new EC2Exception(msg, e);
        }
        return PolicyEngine.getPolicy(builder.getDocumentElement());
    }

    private void setGroupIDs(Instance instance, GroupItemType[] groupItemTypes) {
        if (groupItemTypes != null) {
            int groupIdCount = groupItemTypes.length;
            String[] groupIds = new String[groupIdCount];
            for (int index = 0; index < groupIdCount; index++) {
                groupIds[index] = groupItemTypes[index].getGroupId();
            }
            instance.setGroupIds(groupIds);
        } else {
            instance.setGroupIds(new String[0]);
        }
    }

}
