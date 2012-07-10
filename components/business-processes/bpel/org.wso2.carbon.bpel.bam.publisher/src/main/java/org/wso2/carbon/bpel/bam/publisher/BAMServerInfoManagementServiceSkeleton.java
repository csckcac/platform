package org.wso2.carbon.bpel.bam.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.bam.publisher.internal.BAMPublisherServiceComponent;
import org.wso2.carbon.bpel.bam.publisher.skeleton.BAMServerInfoManagementServiceSkeletonInterface;
import org.wso2.carbon.bpel.bam.publisher.skeleton.BamServerInformation;
import org.wso2.carbon.bpel.bam.publisher.skeleton.Fault;
import org.wso2.carbon.bpel.bam.publisher.util.BamPublisherUtils;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

public class BAMServerInfoManagementServiceSkeleton extends AbstractAdmin implements
                                  BAMServerInfoManagementServiceSkeletonInterface{
    Log log = LogFactory.getLog(BAMServerInfoManagementServiceSkeleton.class);

    public BamServerInformation getBamServerInformation() throws Fault {



        int tenantId = SuperTenantCarbonContext.getCurrentContext().getTenantId();
        UserRegistry configSystemRegistry = null;
        try {
            configSystemRegistry = BAMPublisherServiceComponent.getRegistryService().
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

    public String updateBamServerInformation(String serverURL, String username, String password)
            throws Fault {
        int tenantId = SuperTenantCarbonContext.getCurrentContext().getTenantId();
        UserRegistry configSystemRegistry = null;
        try {
            configSystemRegistry = BAMPublisherServiceComponent.getRegistryService().
                       getConfigSystemRegistry(tenantId);
        } catch (RegistryException e) {
            String msg = "Obtaining registry failed for tenant id " + tenantId;
            log.error(msg, e);
            throw  new Fault(msg);
        }

        BamServerInformation bamServerInformation = new BamServerInformation();
        bamServerInformation.setReceiverURL(serverURL);
        bamServerInformation.setUsername(username);
        bamServerInformation.setPassword(password);

        BamPublisherUtils.addBamServerDataToRegistry(configSystemRegistry, tenantId,
                                                     bamServerInformation);
        DataPublisher dataPublisher = TenantBamAgentHolder.getInstance().getDataPublisher(tenantId);

        if(null == dataPublisher){
            dataPublisher = BamPublisherUtils.createBamDataPublisher(bamServerInformation);
            TenantBamAgentHolder.getInstance().addDataPublisher(tenantId, dataPublisher);
        }else {
            BamPublisherUtils.configureBamDataPublisher(dataPublisher, bamServerInformation);
        }
        return "Server Data Updated to the registry" ;
    }
}
