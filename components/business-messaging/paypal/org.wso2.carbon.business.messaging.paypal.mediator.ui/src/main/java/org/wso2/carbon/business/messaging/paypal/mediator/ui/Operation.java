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

import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * Specifies the <code>Operation</code> including the name and the expression of
 * a particular parameter. This stores the properties of a particular input or
 * output and will be used by the <code>PaypalMedaitor</code>.
 * </p>
 *
 * @see org.wso2.carbon.business.messaging.paypal.mediator.ui.Input
 * @see org.wso2.carbon.business.messaging.paypal.mediator.OutputF
 */
public class Operation {

    /**
     * <p>
     * Specifies the name of the operation.
     * </p>
     */
    private String name;

    /**
     * <p>
     * Specifies the SOAP action of the operation.
     * </p>
     */
    private String action;

    /**
     * <p>
     * List of inputs to be accepted by this operation on arrival of a message.
     * </p>
     *
     * @see org.wso2.carbon.business.messaging.paypal.mediator.ui.Input
     * @see java.util.ArrayList
     */
    private List<Input> inputs = new ArrayList<Input>();

    /**
     * <p>
     * List of inputs to be accepted by this operation on arrival of a message.
     * </p>
     *
     * @see org.wso2.carbon.business.messaging.paypal.mediator.ui.Input
     * @see java.util.ArrayList
     */
    private List<Output> outputs = new ArrayList<Output>();

    private String version;

    private String currency;

    public void addInput(Input input) {
        inputs.add(input);
    }

    public void addOutput(Output output) {
        outputs.add(output);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * @return the inputs
     */
    public List<Input> getInputs() {
        return inputs;
    }

    /**
     * @param inputs the inputs to set
     */
    public void setInputs(List<Input> inputs) {
        this.inputs = inputs;
    }

    /**
     * @return the outputs
     */
    public List<Output> getOutputs() {
        return outputs;
    }

    /**
     * @param outputs the outputs to set
     */
    public void setOutputs(List<Output> outputs) {
        this.outputs = outputs;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * @param currency the currency to set
     */
    public void setCurrency(String currency) {
		this.currency = currency;
	}

}
