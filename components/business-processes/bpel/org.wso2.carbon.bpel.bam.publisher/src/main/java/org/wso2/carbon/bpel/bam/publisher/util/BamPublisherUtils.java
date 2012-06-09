package org.wso2.carbon.bpel.bam.publisher.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.agent.DataPublisher;
import org.wso2.carbon.agent.commons.exception.AuthenticationException;
import org.wso2.carbon.agent.exception.AgentException;
import org.wso2.carbon.agent.exception.TransportException;
import org.wso2.carbon.bpel.bam.publisher.BamPublisherConstants;
import org.wso2.carbon.bpel.bam.publisher.skeleton.BamServerInformation;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;

import java.net.MalformedURLException;


public class BamPublisherUtils {

    private static Log log = LogFactory.getLog(BamPublisherUtils.class);

    public static void addBamServerDataToRegistry(Registry registry, Integer tenantId,
                                                  BamServerInformation configData) {
        try {
            Resource resource = registry.newResource();
            //org.wso2.carbon.core.util.CryptoUtil.
            resource.addProperty(BamPublisherConstants.BAM_SERVER_URL, configData.getServerURL());
            resource.addProperty(BamPublisherConstants.BAM_SERVER_USERNAME, configData.getUsername());
            resource.addProperty(BamPublisherConstants.BAM_SERVER_PASSWORD, configData.getPassword());
            resource.addProperty(BamPublisherConstants.BAM_SERVER_PORT, Integer.toString(configData.getThriftPort()));
            resource.addProperty(BamPublisherConstants.BAM_SERVER_ENABLE_HTTP_TRANSPORT,
                                 Boolean.toString(configData.getEnableHTTPTransport()));
            resource.addProperty(BamPublisherConstants.BAM_SERVER_ENABLE_SOCKET_TRANSPORT,
                                 Boolean.toString(configData.getEnableSocketTransport()));
            registry.put(BamPublisherConstants.CONFIG_RESOURCE_PATH, resource);

        } catch (RegistryException e) {
            String msg = "Add Update bpel bam publisher resource failed for tenant Id" + tenantId;
            log.error(msg, e);
        }
    }

    public static BamServerInformation getBamServerDataFromRegistry(Registry registry,
                                                                    Integer tenantId) {
        try {
            if (registry.resourceExists(BamPublisherConstants.CONFIG_RESOURCE_PATH)) {
                BamServerInformation bamServerInformation = new BamServerInformation();
                Resource resource = registry.get(BamPublisherConstants.CONFIG_RESOURCE_PATH);
                bamServerInformation.setServerURL(
                        resource.getProperty(BamPublisherConstants.BAM_SERVER_URL));
                bamServerInformation.setUsername(
                        resource.getProperty(BamPublisherConstants.BAM_SERVER_USERNAME));
                bamServerInformation.setPassword(
                        resource.getProperty(BamPublisherConstants.BAM_SERVER_PASSWORD));
                bamServerInformation.setThriftPort(
                        Integer.parseInt(resource.getProperty(BamPublisherConstants.BAM_SERVER_PORT)));
                bamServerInformation.setEnableSocketTransport(
                        Boolean.parseBoolean(resource.getProperty(
                                BamPublisherConstants.BAM_SERVER_ENABLE_SOCKET_TRANSPORT)));
                return bamServerInformation;
            } else {
                log.debug("Bam Server Config resource does not exist in the registry for tenant");
            }
        } catch (RegistryException e) {
            String msg = "Error when getting bam config resource data from registry for tenant " +
                         tenantId;
            log.error(msg, e);
        }
        return null;
    }

    public static DataPublisher createBamDataPublisher(BamServerInformation configData) {
        DataPublisher publisher = null;
        try {
             publisher = new DataPublisher(
                                        configData.getServerURL(),
                                        configData.getUsername(),
                                        configData.getPassword());
        } catch (MalformedURLException e) {
            log.error("Event receiver URL provided is invalid" + e);
        } catch (AgentException e) {
            log.error("Error in creating data publisher" + e);
        } catch (AuthenticationException e) {
            log.error("Error in creating data publisher" + e);
        } catch (TransportException e) {
            log.error("Error in creating data publisher, Transport exception" + e);
        }
        return publisher;

    }

    public static void configureBamDataPublisher(DataPublisher publisher,
                                              BamServerInformation configData){
    }
}
