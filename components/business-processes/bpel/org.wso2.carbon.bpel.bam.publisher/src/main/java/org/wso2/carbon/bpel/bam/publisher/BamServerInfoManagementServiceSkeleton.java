package org.wso2.carbon.bpel.bam.publisher;

import org.apache.commons.logging.Log;

import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.agent.publish.EventReceiver;
import org.wso2.carbon.bpel.bam.publisher.internal.BamPublisherServiceComponent;
import org.wso2.carbon.bpel.bam.publisher.skeleton.BAMServerInfoManagementServiceSkeletonInterface;
import org.wso2.carbon.bpel.bam.publisher.skeleton.BamServerInformation;
import org.wso2.carbon.bpel.bam.publisher.skeleton.Fault;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.Tenant;

public class BamServerInfoManagementServiceSkeleton extends AbstractAdmin implements
                                  BAMServerInfoManagementServiceSkeletonInterface{
    Log log = LogFactory.getLog(BamServerInfoManagementServiceSkeleton.class);

    public BamServerInformation getBamServerInformation() throws Fault {
        int tenantId = SuperTenantCarbonContext.getCurrentContext().getTenantId();
        UserRegistry configSystemRegistry = null;
        try {
            configSystemRegistry = BamPublisherServiceComponent.getRegistryService().
                       getConfigSystemRegistry(tenantId);
        } catch (RegistryException e) {
            String msg = "Obtaining registry failed for tenant id " + tenantId;
            log.error(msg, e);
            throw  new Fault(msg);
        }
        BamServerInformation bamServerDataFromRegistry = BamPublisherUtils.
                getBamServerDataFromRegistry(configSystemRegistry, tenantId);
       return bamServerDataFromRegistry;
    }

    public String updateBamServerInformation(String serverURL, String username, String password,
                    int thriftPort, boolean enableSocketTransport, boolean enableHTTPTransport)
                        throws Fault {
        int tenantId = SuperTenantCarbonContext.getCurrentContext().getTenantId();
        UserRegistry configSystemRegistry = null;
        try {
            configSystemRegistry = BamPublisherServiceComponent.getRegistryService().
                       getConfigSystemRegistry(tenantId);
        } catch (RegistryException e) {
            String msg = "Obtaining registry failed for tenant id " + tenantId;
            log.error(msg, e);
            throw  new Fault(msg);
        }

        BamServerInformation bamServerInformation = new BamServerInformation();
        bamServerInformation.setServerURL(serverURL);
        bamServerInformation.setUsername(username);
        bamServerInformation.setPassword(password);
        bamServerInformation.setEnableSocketTransport(enableSocketTransport);
        bamServerInformation.setEnableHTTPTransport(enableHTTPTransport);
        bamServerInformation.setThriftPort(thriftPort);
        BamPublisherUtils.addBamServerDataToRegistry(configSystemRegistry, tenantId,
                                                     bamServerInformation);
        EventReceiver eventReceiver = TenantBamAgentHolder.getInstance().getEventReceiver(tenantId);

        if(null == eventReceiver){
            eventReceiver = BamPublisherUtils.createBamEventReceiver(bamServerInformation);
            TenantBamAgentHolder.getInstance().addEventReceiver(tenantId, eventReceiver);
        }else {
            BamPublisherUtils.configureEventReceiver(eventReceiver, bamServerInformation);
        }
        return "Server Data Updated to the registry" ;
    }
}
