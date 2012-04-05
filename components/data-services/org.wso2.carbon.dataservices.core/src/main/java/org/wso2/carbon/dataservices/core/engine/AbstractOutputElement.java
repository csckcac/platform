/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.dataservices.core.engine;

import org.wso2.carbon.dataservices.core.DataServiceFault;

import javax.xml.stream.XMLStreamWriter;

/**
 * Abstract output element class
 */
public abstract class AbstractOutputElement extends XMLWriterHelper implements OutputElement {

    private String arrayName;

    public AbstractOutputElement(String namespace) {
        super(namespace);
    }

    public String getArrayName() {
        return arrayName;
    }

    @Override
    public void execute(XMLStreamWriter xmlWriter, ExternalParamCollection params,
                        int queryLevel) throws DataServiceFault {
        if (this.getArrayName() == null) {
            this.executeElement(xmlWriter, params, queryLevel);
        } else {
            ExternalParam exParam = params.getParam(this.getArrayName().toLowerCase());
            String name = exParam.getName();
            if (name == null) {
                throw new DataServiceFault("Parameter is null");
            }
            String type = exParam.getType();
            ParamValue arrayValue = exParam.getValue();
            if (arrayValue.getValueType() == ParamValue.PARAM_VALUE_ARRAY) {
                ExternalParamCollection tmpParams;
                for (ParamValue value : arrayValue.getArrayValue()) {
                    tmpParams = new ExternalParamCollection();
                    tmpParams.addParam(new ExternalParam(name, value, type));
                    this.executeElement(xmlWriter, tmpParams, queryLevel);
                }
            } else {
                this.executeElement(xmlWriter, params, queryLevel);
            }
        }
    }

    protected abstract void executeElement(XMLStreamWriter xmlWriter, ExternalParamCollection params,
                        int queryLevel) throws DataServiceFault;

}
