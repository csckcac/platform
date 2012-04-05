package org.wso2.carbon.cep.core.internal.config;

import org.wso2.carbon.broker.core.BrokerConfiguration;
import org.wso2.carbon.cep.core.internal.ds.CEPServiceValueHolder;

/**
 * This class is used to  adapt the Broker Manager's Broker configuration object in to
 * Core Broker Configuration object
 */
public class BrokerConfigurationHelper {
    public BrokerConfiguration getBrokerConfiguration(String brokerName, int tenantId) {
        BrokerConfiguration brokerConfiguration = new BrokerConfiguration();
        org.wso2.carbon.brokermanager.core.BrokerConfiguration managerBrokerConfiguration
                = CEPServiceValueHolder.getInstance().getBrokerManagerService()
                .getBrokerConfiguration(brokerName, tenantId);

        brokerConfiguration.setName(managerBrokerConfiguration.getName());
        brokerConfiguration.setType(managerBrokerConfiguration.getType());
        brokerConfiguration.setProperties(managerBrokerConfiguration.getProperties());

        return brokerConfiguration;
    }

}
