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
package org.wso2.carbon.business.messaging.paypal.mediator.ui.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wso2.carbon.business.messaging.paypal.mediator.ui.Input;
import org.wso2.carbon.business.messaging.paypal.mediator.ui.Operation;

/**
 * This is a singleton class that builds the payload of the operation to be
 * invoked in the Paypal WS API.
 */
public class OperationInputFactory {

    /**
     * Reference of the singleton instance.
     */
    private static OperationInputFactory operationInputFactory;

    /**
     * The map containing all the inputs from the UI.
     */
    private Map<String, Input> inputsMap;

    /**
     * private constructor to disable the creation of the OperationInputFactory
     * instances
     */
    private OperationInputFactory() {
        inputsMap = new HashMap<String, Input>();
    }

    /**
     * Returns the singleton instance. This method returns the singleton
     * instance of the OperationPayloadFactory.
     *
     * @return the singleton instance of this class.
     */
    public static OperationInputFactory getInstance() {
        if (null == operationInputFactory) {
            operationInputFactory = new OperationInputFactory();
        }
        return operationInputFactory;
    }

    /**
     * Creates an OMElement instance to represent the operation.
     *
     * @param operation - the operation.
     * @return the OMElement payload representing the operation instance.
     */
    public void populateInputs(Operation operation, List<Input> inputList) {

        handle(inputList);
        populateInputs(operation.getInputs());
    }

    /**
     * Populates the inputs of the operation with the list of inputs.
     *
     * @param inputList the inputs of the operation.
     */
    private void populateInputs(List<Input> inputList) {
        for (Input input : inputList) {
            populateInput(input, null);
        }
    }

    /**
     * Populates the inputs of the operation with the list of inputs.
     *
     * @param input      the input to be populated.
     * @param parentType the type of the input.
     */
    private void populateInput(Input input, String parentType) {

        if (null != input.getName()) {
            String key = input.getName();
            if (null != parentType) {
                key = parentType + "_" + key;
            }
            if (inputsMap.containsKey(key)) {
                Input fetchedInput = inputsMap.get(key);
                input.setSourceValue(fetchedInput.getSourceValue());
                input.setSourceXPath(fetchedInput.getSourceXPath());
                System.out.println(key + "--> " + input.getName());
            }
        } else {
            for (Input subInput : input.getSubInputs()) {
                populateInput(subInput, input.getType());
            }
        }
    }

    /**
     * Populates the inputs map with the list of inputs.
     *
     * @param inputList contains the list of inputs to be manipulated.
     */
    private void handle(List<Input> inputList) {
        for (Input input : inputList) {
            if (null == input.getType()) {
                inputsMap.put(input.getName(), input);
            } else {
                inputsMap.put(input.getType() + "_" + input.getName(), input);
			}
		}
	}

}
