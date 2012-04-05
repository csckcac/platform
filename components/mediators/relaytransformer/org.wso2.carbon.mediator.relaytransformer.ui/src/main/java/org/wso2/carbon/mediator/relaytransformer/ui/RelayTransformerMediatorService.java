package org.wso2.carbon.mediator.relaytransformer.ui;


import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.mediator.service.AbstractMediatorService;
import org.wso2.carbon.mediator.service.ui.Mediator;

public class RelayTransformerMediatorService extends AbstractMediatorService {

    public String getTagLocalName() {
        return "relayTransformer";
    }

    public String getDisplayName() {
        return "RelayTransformer";
    }

    public String getLogicalName() {
        return "RelayTransformerMediator";
    }

    public String getGroupName() {
        return "Transform";
    }

    public Mediator getMediator() {
        return new RelayTransformerMediator();
    }
}
