package org.wso2.carbon.bam.lwevent.core;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * Created by IntelliJ IDEA.
 * User: sinthuja
 * Date: 1/11/12
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 */
public interface LightWeightEventBrokerInterface {

    public String subscribe(Subscription subscription) throws RegistryException;

    public void unsubscribe(Subscription subscription) throws RegistryException;

    public void publish(String topicName, OMElement event) throws AxisFault, RegistryException;


}
