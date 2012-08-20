package org.wso2.carbon.identity.scim.common.config;

import org.wso2.charon.core.config.SCIMConfig;
import org.wso2.charon.core.config.SCIMConfigConstants;
import org.wso2.charon.core.config.SCIMConsumer;

public class SCIMProvisioningConfigManager {
    private static SCIMProvisioningConfigManager configManager = null;
    private static SCIMConfig scimConfig;

    public static SCIMProvisioningConfigManager getInstance() {
        if (configManager == null) {
            synchronized (SCIMProvisioningConfigManager.class) {
                if (configManager == null) {
                    configManager = new SCIMProvisioningConfigManager();
                    return configManager;
                }
                return configManager;
            }
        }

        return configManager;
    }

    public static void setSCIMConfig(SCIMConfig scimConfiguration) {
        scimConfig = scimConfiguration;
    }

    public static SCIMConfig getSCIMConfig() {
        return scimConfig;
    }

    public static boolean isAppliedToPrivilegedActions(String consumerName) {
        SCIMConsumer consumer = scimConfig.getConsumersMap().get(consumerName);
        String propertyValue = consumer.getPropertiesMap().get(
                SCIMConfigConstants.ELEMENT_NAME_APPLIED_TO_PRIVILEGED_ACTIONS);

        if (propertyValue != null) {
            return Boolean.parseBoolean(propertyValue);
        }
        return false;
    }

    public static boolean isAppliedToSCIMOperation(String consumerName) {
        SCIMConsumer consumer = scimConfig.getConsumersMap().get(consumerName);
        String propertyValue = consumer.getPropertiesMap().get(
                SCIMConfigConstants.ELEMENT_NAME_APPLIED_TO_SCIM_OPERATIONS);

        if (propertyValue != null) {
            return Boolean.parseBoolean(propertyValue);
        }
        return false;
    }

    public static boolean isConsumerRegistered(String consumerName) {
        return scimConfig.getConsumersMap().containsKey(consumerName);
    }
}