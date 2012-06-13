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

import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;

import javax.xml.stream.XMLStreamWriter;
import java.util.Set;

/**
 * Represents an entity which can yield a result, i.e. elements in a result section.
 */
public abstract class OutputElement extends XMLWriterHelper{

    private String arrayName;

    private String name;

    private String paramType;

    private String param;
    
    public OutputElement(String namespace) {
        super(namespace);
    }

    /**
     * Executes and writes the contents of this element, given the parameters.
     */
    public void execute(XMLStreamWriter xmlWriter, ExternalParamCollection params, int queryLevel)
            throws DataServiceFault {
        if (this.getArrayName() == null) {
            this.executeElement(xmlWriter, params, queryLevel);
        } else {
           ExternalParam exParam = this.getExternalParam(params);
            if (exParam == null) {
                throw new DataServiceFault("The array '" + this.getArrayName() +
                        "' does not exist");
            }
            ParamValue paramValue = exParam.getValue();
            String name = exParam.getName();
            String type = exParam.getType();

            if (!DBUtils.isSQLArray(paramValue)) {
                throw new DataServiceFault("Parameter does not corresponding to an array");
            }

            ExternalParamCollection tmpParams;
            for (ParamValue value : paramValue.getArrayValue()) {
                tmpParams = new ExternalParamCollection();
                tmpParams.addParam(new ExternalParam(name, value, type));
                this.executeElement(xmlWriter, tmpParams, queryLevel);
            }
        }
    }

    private ExternalParam getExternalParam(ExternalParamCollection params) {
        ExternalParam exParam = params.getParam(this.getParamName());
        if (exParam == null) {
            exParam = params.getParam(this.getParamType(), this.getParam());
        }
        return exParam;
    }

    private String getParamName() {
        String paramName = this.getArrayName();
        if (paramName.contains("[")) {
            paramName = paramName.substring(0, paramName.indexOf("["));
        }
        return paramName.toLowerCase();
    }

    protected abstract void executeElement(XMLStreamWriter xmlWriter,
                                           ExternalParamCollection params,
                                           int queryLevel) throws DataServiceFault;

    /**
     * Returns the requires roles to view this element.
     */
    public abstract Set<String> getRequiredRoles();

    /**
     * Checks if this element is optional,
     * if so, this has to be mentioned in the schema for WSDL generation.
     */
    public abstract boolean isOptional();

    public String getArrayName() {
        return arrayName;
    }

    public String getParam() {
        return param;
    }

    public String getName() {
        return name;
    }

    public String getParamType() {
        return paramType;
    }

}
