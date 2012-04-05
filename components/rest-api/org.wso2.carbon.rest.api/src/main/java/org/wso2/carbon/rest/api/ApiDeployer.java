package org.wso2.carbon.rest.api;

import java.util.Properties;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.synapse.deployers.APIDeployer;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;

public class ApiDeployer extends APIDeployer {
	
	MediationPersistenceManager mpm;

    @Override
    public void init(ConfigurationContext configCtx) {
        super.init(configCtx);
        mpm = ServiceBusUtils.getMediationPersistenceManager(configCtx.getAxisConfiguration());
    }

    @Override
    public String deploySynapseArtifact(OMElement artifactConfig, String fileName,
                                        Properties properties) {
        String proxyName = super.deploySynapseArtifact(artifactConfig, fileName, properties);
        mpm.saveItemToRegistry(proxyName, ServiceBusConstants.ITEM_TYPE_REST_API);
        return proxyName;
    }

    @Override
    public String updateSynapseArtifact(OMElement artifactConfig, String fileName,
                                        String existingArtifactName, Properties properties) {
        String proxyName = super.updateSynapseArtifact(
                artifactConfig, fileName, existingArtifactName, properties);
        mpm.saveItemToRegistry(proxyName, ServiceBusConstants.ITEM_TYPE_REST_API);
        return proxyName;
    }

    @Override
    public void undeploySynapseArtifact(String artifactName) {
        super.undeploySynapseArtifact(artifactName);
        mpm.deleteItemFromRegistry(artifactName, ServiceBusConstants.ITEM_TYPE_REST_API);
    }

    @Override
    public void restoreSynapseArtifact(String artifactName) {
        super.restoreSynapseArtifact(artifactName);
        mpm.saveItemToRegistry(artifactName, ServiceBusConstants.ITEM_TYPE_REST_API);
    }

}
