package org.wso2.carbon.priority.executors;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.synapse.deployers.PriorityExecutorDeployer;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;

import java.util.Properties;

public class ExecutorDeployer extends PriorityExecutorDeployer {
    MediationPersistenceManager mpm;
    @Override
    public void init(ConfigurationContext configCtx) {
        super.init(configCtx);
        this.mpm = ServiceBusUtils.getMediationPersistenceManager(configCtx.getAxisConfiguration());
    }

    @Override
    public String deploySynapseArtifact(OMElement artifactConfig,
                                        String fileName, Properties properties) {

        String name = super.deploySynapseArtifact(artifactConfig, fileName, properties);
        mpm.saveItemToRegistry(name, ServiceBusConstants.ITEM_TYPE_EXECUTOR);
        return name;
    }

    @Override
    public String updateSynapseArtifact(OMElement artifactConfig, String fileName,
                                        String existingArtifactName, Properties properties) {
        String epName = super.updateSynapseArtifact(artifactConfig, fileName,
                existingArtifactName, properties);
        mpm.saveItemToRegistry(epName, ServiceBusConstants.ITEM_TYPE_EXECUTOR);
        return epName;
    }

    @Override
    public void undeploySynapseArtifact(String artifactName) {
        super.undeploySynapseArtifact(artifactName);
        mpm.deleteItemFromRegistry(artifactName, ServiceBusConstants.ITEM_TYPE_EXECUTOR);
    }

    @Override
    public void restoreSynapseArtifact(String artifactName) {
        super.restoreSynapseArtifact(artifactName);
        mpm.saveItemToRegistry(artifactName, ServiceBusConstants.ITEM_TYPE_EXECUTOR);
    }
}
