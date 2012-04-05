package org.wso2.carbon.messagebox.sqs.internal.ds;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.messagebox.MessageBoxService;
import org.wso2.carbon.messagebox.sqs.internal.util.MessageBoxHolder;
import org.wso2.carbon.registry.core.service.RegistryService;


/**
 * this class is used to get the Messagebox service.
 *
 * @scr.component name="org.wso2.carbon.messagebox.sqs" immediate="true"
 * @scr.reference name="org.wso2.carbon.messagebox"
 * interface="org.wso2.carbon.messagebox.MessageBoxService" cardinality="1..1"
 * policy="dynamic" bind="setMessageBoxService" unbind="unSetMessageBoxService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
public class MessageBoxServiceSqsDS {
    protected void activate(ComponentContext context) {

    }

    protected void setMessageBoxService(MessageBoxService messageboxService) {
        MessageBoxHolder.getInstance().registerMessageboxService(messageboxService);
    }

    protected void unSetMessageBoxService(MessageBoxService messageboxService) {
        MessageBoxHolder.getInstance().unRegisterMessageboxService(messageboxService);
    }

    protected void setRegistryService(RegistryService registryService) {
        MessageBoxHolder.getInstance().registerRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {

    }
}

