/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.ui.bpel2svg.latest.wso2.svg.javascript;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class encapsulate a javaScript Function.
 * So it consists of a method name and a sequence of input parameters
 */
public class JSFunction {

    private final String functionName;

    private final List<String> inputParameters;

    public JSFunction(String functionName) {
        this.inputParameters = new ArrayList<String>();
        this.functionName = functionName;
    }

    public void addInputParameters(String[] inputParameters) {
        Collections.addAll(this.inputParameters, inputParameters);
    }

//    public String getFunctionName() {
//        return functionName;
//    }

//    public String[] getInputParameters() {
//        return (String[]) inputParameters.toArray();
//    }

    public String generateJSFunctionSignature () {
        StringBuilder buf = new StringBuilder();

        for(int i = 0; i < inputParameters.size(); i++) {
            if (i != (inputParameters.size() -1)) {
                buf.append(inputParameters.get(i)).append(", ");
            } else {
                buf.append(inputParameters.get(i));
            }
        }
        return functionName + "(" + buf.toString() + ")";
    }

}
