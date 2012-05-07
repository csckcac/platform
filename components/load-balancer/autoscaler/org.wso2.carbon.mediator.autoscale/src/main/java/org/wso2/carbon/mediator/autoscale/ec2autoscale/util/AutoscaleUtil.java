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
package org.wso2.carbon.mediator.autoscale.ec2autoscale.util;

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.lb.common.conf.LoadBalancerConfiguration;
import org.wso2.carbon.mediator.autoscale.ec2autoscale.context.AppDomainContext;
//import sun.misc.BASE64Encoder;
import org.apache.axiom.om.util.Base64;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods for Autoscale mediator
 */
public final class AutoscaleUtil {

    private static final Log log = LogFactory.getLog(AutoscaleUtil.class);

    private AutoscaleUtil() {
    }

    /**
     * handles the exception
     *
     * @param msg exception message
     */
    public static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    /**
     * handles the exception
     *
     * @param msg exception message
     * @param e   exception
     */
    public static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

    /**
     * Returns the contents of the file in a byte array
     *
     * @param file - Input File
     * @return Bytes from the file
     * @throws java.io.IOException, if retrieving the file contents failed.
     */
    public static byte[] getBytesFromFile(File file) throws IOException {
        if (!file.exists()) {
            log.error("Payload file " + file.getAbsolutePath() + " does not exist");
            return null;
        }
        InputStream is = new FileInputStream(file);
        byte[] bytes;

        try {
            // Get the size of the file
            long length = file.length();

            // You cannot create an array using a long type.
            // It needs to be an int type.
            // Before converting to an int type, check
            // to ensure that file is not larger than Integer.MAX_VALUE.
            if (length > Integer.MAX_VALUE) {
                if (log.isDebugEnabled()) {
                    log.debug("File is too large");
                }
            }

            // Create the byte array to hold the data
            bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead;
            while (offset < bytes.length
                   && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }
        } finally {
            // Close the input stream and return bytes
            is.close();
        }

        return bytes;
    }

    /**
     * Replaces the variables
     *
     * @param text input string
     * @return output String
     */
    public static String replaceVariables(String text) {
        int indexOfStartingChars;
        int indexOfClosingBrace;

        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        if ((indexOfStartingChars = text.indexOf("${")) != -1 &&
            (indexOfClosingBrace = text.indexOf("}")) != -1) { // Is a property used?
            String var = text.substring(indexOfStartingChars + 2,
                                        indexOfClosingBrace);

            String propValue = System.getProperty(var);
            if (propValue == null) {
                propValue = System.getenv(var);
            }
            if (propValue != null) {
                text = text.substring(0, indexOfStartingChars) + propValue +
                       text.substring(indexOfClosingBrace + 1);
            }
        }
        return text;
    }

/*    public static InstanceManager createEC2InstanceManager(String accessKey,
                                                              String secretKey,
                                                              String instanceMgtEPR) {
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonEC2Client ec2Client = new AmazonEC2Client(awsCredentials);
        ec2Client.setEndpoint(instanceMgtEPR);
        return new InstanceManager(ec2Client);
    }*/

    public static String getUserData(String payloadFileName) {
        String userData = null;
        try {
            File file = new File(payloadFileName);
            if (!file.exists()) {
                handleException("Payload file " + payloadFileName + " does not exist");
            }
            if (!file.canRead()) {
                handleException("Payload file " + payloadFileName + " does cannot be read");
            }
            byte[] bytes = AutoscaleUtil.getBytesFromFile(file);
            if (bytes != null) {
//                /BASE64.e encoder = new BASE64Encoder();
                userData = Base64.encode(bytes);
            }
        } catch (IOException e) {
            AutoscaleUtil.handleException("Cannot read data from payload file " + payloadFileName,
                                          e);
        }
        return userData;
    }

    /*public static boolean areEqual(List<GroupIdentifier> securityGroups1, String[] sourceGroups2) {
        for (String sourceGroup : sourceGroups2) {
            boolean isSourceGroupFound = false;
            for (GroupIdentifier securityGroup : securityGroups1) {
                if (securityGroup.getGroupName().equals(sourceGroup)) {
                    isSourceGroupFound = true;
                }
            }
            if (!isSourceGroupFound) {
                return false;
            }
        }
        return true;
    } */
    public static int getTenantId(String url) {
        String address = url;
        String servicesPrefix = "/t/";
        if (address != null && address.contains(servicesPrefix)) {
            int domainNameStartIndex =
                    address.indexOf(servicesPrefix) + servicesPrefix.length();
            int domainNameEndIndex = address.indexOf('/', domainNameStartIndex);
            String domainName = address.substring(domainNameStartIndex,
                    domainNameEndIndex == -1 ? address.length() : domainNameEndIndex);
            // return tenant id if domain name is not null
            if (domainName != null) {
                try {
                    return ConfigHolder.getRealmService().getTenantManager().getTenantId(domainName);
                } catch (org.wso2.carbon.user.api.UserStoreException e) {
                    log.error("An error occurred while obtaining the tenant id.", e);
                }
            }
        }
        // return 0 if the domain name is null
        return 0;
    }

    public static String getTargetHost(MessageContext synCtx) {
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        Map<String, String> headers =
                (Map<String, String>) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String address = headers.get(HTTP.TARGET_HOST);
        if (address.contains(":")) {
            address = address.substring(0, address.indexOf(":"));
        }
        return address;
    }

    public static Map<String, AppDomainContext> getAppDomainContexts(ConfigurationContext configCtx,
                                                                     LoadBalancerConfiguration lbConfig) {
        Map<String, AppDomainContext> appDomainContexts =
                (Map<String, AppDomainContext>) configCtx.getPropertyNonReplicable(AutoscaleConstants.APP_DOMAIN_CONTEXTS);
        if (appDomainContexts == null) {
            appDomainContexts = new HashMap<String, AppDomainContext>();
            ClusteringAgent clusteringAgent = configCtx.getAxisConfiguration().getClusteringAgent();

            for (String domain :lbConfig.getServiceDomains() ) {
                if (clusteringAgent.getGroupManagementAgent(domain) == null) {
                    throw new SynapseException("Axis2 clustering GroupManagementAgent for domain " +
                                               domain + " has not been defined");
                }
                    appDomainContexts.put(domain, new AppDomainContext(lbConfig.getServiceConfig(domain)));
                    
                }
            }
            configCtx.setNonReplicableProperty(AutoscaleConstants.APP_DOMAIN_CONTEXTS,
                                               appDomainContexts);
        
        return appDomainContexts;
    }
}