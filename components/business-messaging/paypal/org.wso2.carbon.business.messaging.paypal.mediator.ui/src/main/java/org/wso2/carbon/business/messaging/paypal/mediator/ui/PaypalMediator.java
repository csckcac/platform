/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.business.messaging.paypal.mediator.ui;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;

/**
 * <p>
 * Mediates the requests which are extracted from the
 * <code>MessageContext</code> using the specified particular source inputs and
 * maps the response back to the <code>MessageContext</code> using the outputs
 * specified.
 * </p>
 *
 * @see org.wso2.carbon.business.messaging.paypal.mediator.ui.Operation
 * @see org.apache.synapse.Mediator
 * @see org.apache.synapse.mediators.AbstractMediator
 */
public class PaypalMediator extends AbstractMediator {

    /**
     * Holds the location of the client repository configuration.
     */
    private String clientRepository;

    /**
     * Holds the location of the axis2 configuration.
     */
    private String axis2xml;

    /**
     * The credential required for the <code>Operation</code> to be invoked.
     *
     * @see org.wso2.carbon.business.messaging.paypal.mediator.ui.Operation
     */
    private RequestCredential requestCredential;
    /**
     * The <code>Operation</code> to be invoked.
     *
     * @see org.wso2.carbon.business.messaging.paypal.mediator.ui.Operation
     */
    private Operation operation;

    private PaypalCompactSerializer serializer;

    private PaypalCompactBuilder builder;

    public OMElement serialize(OMElement parent) {
        if (null == serializer) {
            /*serializer = new PaypalMediatorSerializer(fac, synNS, nullNS);*/
            serializer = new PaypalCompactSerializer(fac, SynapseConstants.SYNAPSE_OMNAMESPACE, nullNS);
        }
        OMElement paypal = serializer.serializeMediator(parent, this);

        if (parent != null) {
            parent.addChild(paypal);
        }
        System.out.println("The Serialized output " + paypal);
        return paypal;
    }

    public void build(OMElement elem) {
        if (null == builder) {
            /*builder = new PaypalMediatorBuilder();*/
            builder = new PaypalCompactBuilder();
        }

        builder.buildMediator(elem, this);

        System.out.println("The Build output " + elem);
    }

    /**
     * Getter method for <code>clientRepository</code>
     *
     * @return the client repository location.
     */
    public String getClientRepository() {
        return clientRepository;
    }

    /**
     * Setter method for <code>clientRepository</code>
     *
     * @param clientRepository the client repository location.
     */
    public void setClientRepository(String clientRepository) {
        this.clientRepository = clientRepository;
    }

    /**
     * Getter method for <code>axis2xml</code>
     *
     * @return the axis2 xml configuration location.
     */
    public String getAxis2xml() {
        return axis2xml;
    }

    /**
     * Setter method for <code>axis2xml</code>
     *
     * @param axis2xml the axis2 xml configuration location.
     */
    public void setAxis2xml(String axis2xml) {
        this.axis2xml = axis2xml;
    }

    /**
     * @return the requestCredential
     */
    public RequestCredential getRequestCredential() {
        return requestCredential;
    }

    /**
     * @param requestCredential the requestCredential to set
     */
    public void setRequestCredential(RequestCredential requestCredential) {
        this.requestCredential = requestCredential;
    }

    /**
     * Getter method for <code>operation</code>
     *
     * @return the operation the operation to invoke.
     */
    public Operation getOperation() {
        return operation;
    }

    /**
     * Setter method for <code>operation</code>
     *
     * @param operation the operation to invoke
     */
    public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public String getTagLocalName() {
		return "paypal";
	}
}
