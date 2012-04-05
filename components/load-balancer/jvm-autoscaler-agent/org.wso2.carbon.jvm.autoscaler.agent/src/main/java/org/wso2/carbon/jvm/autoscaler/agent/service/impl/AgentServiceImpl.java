/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.jvm.autoscaler.agent.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.jvm.autoscaler.agent.agentmgt.clients.AgentManagementServiceClient;
import org.wso2.carbon.jvm.autoscaler.agent.data.Instance;
import org.wso2.carbon.jvm.autoscaler.agent.data.StreamPrinter;
import org.wso2.carbon.autoscaler.agent.exception.ImageNotFoundException;
import org.wso2.carbon.autoscaler.agent.exception.InstanceInitiationFailureException;
import org.wso2.carbon.autoscaler.agent.exception.InstanceTerminationFailureException;
import org.wso2.carbon.autoscaler.agent.service.IAgentService;
import org.wso2.carbon.jvm.autoscaler.agent.xml.DefaultPortReader;
import org.wso2.carbon.jvm.autoscaler.agent.xml.AgentConfigFileReader;
import org.wso2.carbon.utils.ArchiveManipulator;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Implements the IAgentService interface.
 * 
 * @scr.component name="org.wso2.carbon.autoscaler.agent.service"
 * @scr.service value="org.wso2.carbon.autoscaler.agent.service.IAgentService"
 * 
 */
public class AgentServiceImpl implements IAgentService {

    /**
     * Working directory of this <i>Agent</i> ( <i>CARBON_HOME<i>/tmp/instances/ )
     */
    private static final String WORKING_DIR = CarbonUtils.getTmpDir() + File.separator +
        "instances" + File.separator;

    /**
     * <i>Agent</i>'s End Point Reference (<i>EPR</i>)
     */
    private String agentEpr;

    private static final Log log = LogFactory.getLog(AgentServiceImpl.class);

    private AgentManagementServiceClient agentMgtClient = null;

    private String baseURL = null;

    /**
     * Keep track of the spawned server instance's {@link #portOffset}.
     * 
     * Key - Instance Id
     * Value - {@link #portOffset}
     */
    private Map<String, Integer> instanceIdToPortOffset = new HashMap<String, Integer>();

    /**
     * For configuration file reading purpose
     */
    private AgentConfigFileReader configReader;

    /**
     * To read the default port
     */
    private DefaultPortReader portReader;

    /**
     * Maps a domain to an image path
     * key: domain name
     * value: path to the image
     */
    private Map<String, String> domainToImagePathMap;

    /**
     * Each time an instance spawned this will increment by one.
     * Maximum value of this is {@link #PORT_OFFSET_LIMIT}.
     */
    private int portOffset = 0;

    /**
     * {@link #portOffset} will not exceed this limit.
     */
    private static final int PORT_OFFSET_LIMIT = 1000;

    private MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    /**
     * default value of the {@link #maxInstanceCount}
     */
    private static final int DEFAULT_MAX_INSTANCE_COUNT = 6;

    /**
     * .
     * <i>Bytes</i> for a <i>MegaByte</i>
     */
    public final static int MB_BASE = 1024 * 1024;

    /**
     * Maximum number of instances spawnable in this <i>Agent</i>.
     */
    private int maxInstanceCount;

    /**
     * This value will be read from the <i>Agent</i>'s config file
     */
    private long maxMemoryPerInstance;

    /**
     * ============================== Operations ===============================
     */

    public boolean registerInAgentManagementService() throws Exception {

        // EPR of this Service
        agentEpr = getBaseURL();

        // Each time this Agent get registered it reads the configuration file and retrieve
        // the available domain-image map
        configReader = new AgentConfigFileReader();
        
        domainToImagePathMap = configReader.getAvailableInstanceImages();

        maxMemoryPerInstance = configReader.getMaxMemoryPerInstance();

        // Get a connection to AgentManagementService via a client
        agentMgtClient =
            new AgentManagementServiceClient(configReader.getHostUrlOfDependentServices());

        // extract all the image Zip files
        domainToImagePathMap = extractZipFilesAndUpdateMap(domainToImagePathMap);

        // calculate max instance count
        maxInstanceCount = calculateMaxInstanceCount();

        // get registered!
        return agentMgtClient.registerAgent(agentEpr, maxInstanceCount);
    }

    
    public boolean unregisterInAgentManagementService() throws Exception {

        // EPR of this Service
        agentEpr = getBaseURL();

        return agentMgtClient.unregisterAgent(agentEpr, maxInstanceCount);
    }

    
    public boolean startInstance(String domainName, String instanceId) 
            throws ImageNotFoundException, InstanceInitiationFailureException, Exception {

        agentEpr = getBaseURL();

        // first check whether the agent has already spawned its maxInstanceCount
        if (instanceIdToPortOffset.size() > maxInstanceCount - 1) {
            // if so throw an error
            String msg =
                "Agent (" + agentEpr + ") has reached the maximum number of instances (" +
                    maxInstanceCount + ") that it can spawn.";
            log.error(msg);
            throw new InstanceInitiationFailureException(msg);
        }

        // get the image for the requested domain if available
        String pathToImage = getPathToInstanceImage(domainName);

        // if requested image not found
        if (pathToImage == null) {
            String msg =
                "No matching image found in Agent (" + agentEpr + "), for domain (" + domainName +
                    ").";
            // this is a warning, since the instance will be tried to spawn in a different Agent.
            log.warn(msg);
            throw new ImageNotFoundException(msg);
        }

        // create a copy of the retrieved image and build an Instance
        Instance anInstance = createANewInstance(pathToImage, instanceId);

        // we try to start the process only if an instance found
        // FIXME: this check can be removed since anInstance will never get null?
        if (anInstance != null) {

            Process proc;
            // set carbon home
            String carbonHome = anInstance.getPathToInstance();

            portReader = new DefaultPortReader(carbonHome);

            int defaultHttpsPort = portReader.getHttpsPort();
            int defaultHttpPort = portReader.getHttpPort();

            // increment the portOffset till we find an available port
            do {
                ++portOffset;

                // if limit exceeds
                if (portOffset > PORT_OFFSET_LIMIT) {
                    // starts from the beginning
                    portOffset = 0;
                }

            }
            // checks whether the ports are available
            while (!isPortAvailable(defaultHttpsPort + portOffset) &&
                !isPortAvailable(defaultHttpPort + portOffset));

            // set the available port's offset in the instance
            anInstance.setPortOffset(portOffset);

            File commandDir = new File(carbonHome);
            try {
                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    commandDir = new File(carbonHome + File.separator + "bin");

                    proc =
                        Runtime.getRuntime()
                               .exec(new String[] { "cmd.exe", "/c", "wso2server.bat",
                                         "-DportOffset=" + String.valueOf(portOffset) }, null,
                                     commandDir);

                } else {
                    proc =
                        Runtime.getRuntime()
                               .exec(new String[] { "sh", "bin/wso2server.sh",
                                         "-DportOffset=" + String.valueOf(portOffset) }, null,
                                     commandDir);
                }

                // add this process to the instanceIdToProcess map
                instanceIdToPortOffset.put(anInstance.getInstanceId(), portOffset);

                // any error message?
                StreamPrinter errorReader = new StreamPrinter(proc.getErrorStream(), "ERROR ");

                // any output?
                StreamPrinter outputReader = new StreamPrinter(proc.getInputStream(), "OUTPUT ");

                // starts printing the streams
                errorReader.start();
                outputReader.start();
                
                return true;

            } catch (IOException e) {
                String msg =
                    "Spawning the requested instance for domain (" + domainName + ") failed.";
                log.error(msg, e);
                throw new InstanceInitiationFailureException(msg, e);
            }

        }

        return false;
    }
    
    
    public boolean terminateInstance(String instanceId) throws InstanceTerminationFailureException, Exception {

        Process proc;
        StreamPrinter errorReader;

        // TODO do a check on CPU usage of this particular instance, if not 0% we should not
        // terminate this instance. May be wait for some time and retry? if still not, should
        // throw an InstanceTerminationFailureException

        // else proceed

        if (instanceIdToPortOffset.containsKey(instanceId)) {

            int portOffsetValue = instanceIdToPortOffset.get(instanceId);

            try {
                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    //FIXME this will kill all processes who has DportOffset=x
                    //if x=2, this will kill eg: DportOffset=2 and DportOffset=25
                    //find a wmic command
                    proc =
                        Runtime.getRuntime()
                               .exec(new String[] {
                                         "wmic",
                                         "process where (CommandLine like \"%DportOffset=" +
                                             portOffsetValue + "%\")", "delete" }, null);

                } else {

                    proc =
                        Runtime.getRuntime().exec(new String[] {
                                                      "sh",
                                                      "-c",
                                                      "kill -9 `ps -ef | grep -w 'DportOffset=" +
                                                          portOffsetValue + "$' " +
                                                          "| awk '{print $2}'`" }, null);
                }

                // any error message?
                errorReader = new StreamPrinter(proc.getErrorStream(), "ERROR ");

                // any output?
                StreamPrinter outputReader = new StreamPrinter(proc.getInputStream(), "OUTPUT ");

                // starts printing the streams
                errorReader.start();
                outputReader.start();

                proc.waitFor();
                proc.destroy();
                
                log.info("Instance ("+instanceId+") is killed. PortOffset of the instance was "+
                                portOffsetValue+".");

            } catch (IOException e) {
                String msg =
                    "Terminating the requested instance with portOffset of " + portOffsetValue +
                        " failed.";
                log.error(msg, e);
                throw new InstanceTerminationFailureException(msg, e);
                
            }
            
            // successfully terminated, hence remove from the Map
            instanceIdToPortOffset.remove(instanceId);

            return true;
        } else {
            return false;
        }

    }
    
    
    
    /**
     * Calculates number of instances that this Agent can spawn. If failed when calculating
     * this will return the {@value #DEFAULT_MAX_INSTANCE_COUNT}.
     * 
     * @return calculated value or {@link #DEFAULT_MAX_INSTANCE_COUNT}
     */
    private int calculateMaxInstanceCount() {
        try {
            ObjectName osBean = new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
            // gets the free memory of the OS
            String freeMemory =
                mBeanServer.getAttribute(osBean, "FreePhysicalMemorySize").toString();

            // debugging purposes
            log.debug("FreeMemory: " + (Long.parseLong(freeMemory) / MB_BASE) +
                " (MB) **** MaxMemoryPerInstance: " + maxMemoryPerInstance);

            // calculate max instance count and return
            return (int) ((Long.parseLong(freeMemory) / MB_BASE) / maxMemoryPerInstance);

        } catch (Exception e) {
            // if an exception occur, we are using the default value
            log.error("Failed to calculate maximum instance count, hence using default value" +
                " " + DEFAULT_MAX_INSTANCE_COUNT + ".", e);

            return DEFAULT_MAX_INSTANCE_COUNT;
        }
    }

    /**
     * Extract the Zip files of the images to the same root folder as the images.
     * 
     * @param originalInstanceMap
     *            domain to pathToImageZip map
     * @return a map which maps domain to pathToExtractedImage
     */
    private Map<String, String> extractZipFilesAndUpdateMap(
                                       Map<String, String> originalInstanceMap) {

        Map<String, String> newInstanceMap = new HashMap<String, String>();

        // iterate through entries
        for (Map.Entry<String, String> entry : originalInstanceMap.entrySet()) {

            String domain = entry.getKey();
            // locate the path of an image
            String carbonServerZipFile = entry.getValue();

            // carbonServerZipFile can never be null, since we've checked for null before it
            // is added.
            int indexOfZip = carbonServerZipFile.lastIndexOf(".zip");
            if (indexOfZip == -1) {
                log.error(carbonServerZipFile + " is not a zip file.");
                // logging the error is enough. We're not going to add this to the map.
            }

            String fileSeparator = (File.separator.equals("\\")) ? "\\" : "/";

            if (fileSeparator.equals("\\")) {
                carbonServerZipFile = carbonServerZipFile.replace("/", "\\");
            }

            // this is the root directory where the zip will be extracted to.
            String pathToExtractedCarbonRootDir =
                carbonServerZipFile.substring(0, carbonServerZipFile.lastIndexOf(fileSeparator));

            try {
                new ArchiveManipulator().extract(carbonServerZipFile, pathToExtractedCarbonRootDir);
                // what we add to the map is the path to carbon home
                newInstanceMap.put(domain, carbonServerZipFile.substring(0, indexOfZip));

            } catch (IOException e) {
                // we don't need to throw this error since it doesn't matter!
                // TODO: should this be a warning?
                log.error("Failed when extracting the " + carbonServerZipFile + " to " +
                    pathToExtractedCarbonRootDir + ". Hence this image of '" + domain +
                    "' domain is not spawnable.");

            }

        }

        return newInstanceMap;
    }

    /**
     * Checks whether the given port is available
     * 
     * @param port
     *            port to be checked
     * @return available or not.
     */
    private boolean isPortAvailable(int port) {

        ServerSocket ss = null;

        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            return true;

        } catch (IOException e) {
        } finally {

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;

    }

    /**
     * Get a copy of the instance from the working directory and create a new instance
     * which is points to the new location.
     * 
     * @param pathToImage
     * @param instanceId
     * @return an Instance which points to the location of the instance
     * @throws Exception
     *             when error in reading the source folder
     */
    private Instance createANewInstance(String pathToImage, String instanceId) 
            throws ImageNotFoundException, Exception {

        File sourceDir = new File(pathToImage);

        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            String msg =
                "Image of the instance does not exist or not a directory! Path: " + pathToImage;
            log.error(msg);
            throw new ImageNotFoundException(msg);

        }

        // adds a random value as a suffix to the image folder name at the working directory.
        File destDir = new File(WORKING_DIR + sourceDir.getName() + instanceId);

        // Copying
        copyDirectory(sourceDir, destDir);

        // creating a new instance
        Instance anInstance = new Instance();
        anInstance.setPathToInstance(destDir.getAbsolutePath());
        anInstance.setInstanceId(instanceId);

        return anInstance;

    }

    /**
     * Copy a directory. If targetLocation does not exist, it will be created.
     * 
     * @param sourceLocation
     * @param targetLocation
     * @throws Exception
     */
    private void copyDirectory(File sourceLocation, File targetLocation) throws Exception {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                if (!targetLocation.mkdirs()) {
                    String msg =
                        "Error while creating the target directory at " + targetLocation + " !";
                    log.error(msg);
                    throw new Exception(msg);
                }
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation,
                                                                              children[i]));
            }
        } else {

            try {
                InputStream in = new FileInputStream(sourceLocation);
                OutputStream out = new FileOutputStream(targetLocation);

                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (IOException ex) {
                String msg =
                    "Error while copying the file from " + sourceLocation + " to " +
                        targetLocation + " !";
                log.error(msg, ex);
                throw new Exception(msg, ex);
            }
        }

    }

    /**
     * Finds an instance from the requested domain.
     * 
     * @param domainName
     *            name of the domain. (eg: wso2.as.domain)
     * @return the found instance. If no matching domain is found or
     *         no available instance is found for the requested domain this will
     *         return null.
     */
    private String getPathToInstanceImage(String domainName) {

        // for each domain in instance map
        for (String domain : domainToImagePathMap.keySet()) {

            // is matching domain found?
            if (domain.equals(domainName)) {
                // returns the path to image
                return domainToImagePathMap.get(domain);
            }
        }

        // if no matching domain is found
        return null;
    }

    public int getNumberOfInstances() {
        return instanceIdToPortOffset.size();
    }

    // This method attempts to get the Base URI for this service. This will be used to construct
    // the URIs for the various details returned.
    private String getBaseURL() {
        if (baseURL == null) {
            MessageContext messageContext = MessageContext.getCurrentMessageContext();
            AxisConfiguration configuration =
                messageContext.getConfigurationContext().getAxisConfiguration();
            TransportInDescription inDescription = configuration.getTransportIn("http");

            try {
                EndpointReference[] eprs =
                    inDescription.getReceiver().getEPRsForService(messageContext.getAxisService()
                                                                                .getName(), null);
                baseURL = eprs[0].getAddress();
            } catch (AxisFault axisFault) {
            }
        }
        return baseURL;
    }

}
