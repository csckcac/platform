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
import java.sql.SQLException;

/**
 * Abstract output element class
 */
public abstract class ArrayOutputElement extends XMLWriterHelper implements OutputElement {

    private String arrayName;

    private String name;

    private String paramType;

    private String param;

    public ArrayOutputElement(String namespace) {
        super(namespace);
    }

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

    @Override
    public void execute(XMLStreamWriter xmlWriter, ExternalParamCollection params,
                        int queryLevel) throws DataServiceFault {
        ParamValue paramValue;
        if (this.getArrayName() == null) {
            this.executeElement(xmlWriter, params, queryLevel);
        } else {
            ExternalParam exParam = this.getExternalParam(params);
            if (exParam != null) {
                paramValue = exParam.getValue();
                String name = exParam.getName();
                String type = exParam.getType();

                if (DBUtils.isSQLArray(paramValue)) {
                    ExternalParamCollection tmpParams;
                    for (ParamValue value : paramValue.getArrayValue()) {
                        tmpParams = new ExternalParamCollection();
                        tmpParams.addParam(new ExternalParam(name, value, type));
                        this.executeElement(xmlWriter, tmpParams, queryLevel);
                    }
                } else if (DBUtils.isUDT(paramValue)) {
                    String indexString = this.getArrayName().substring(
                            this.getArrayName().indexOf("["), this.getArrayName().length());
                    ParamValue value;
                    try {
                        value = DBUtils.getUDTAttributeValue(DBUtils.getNestedIndices(indexString),
                                paramValue, 0);
                        if (DBUtils.isSQLArray(value)) {
                            ExternalParamCollection tmpParams;
                            for (ParamValue param : value.getArrayValue()) {
                                tmpParams = new ExternalParamCollection();
                                tmpParams.addParam(new ExternalParam(this.getArrayName().toLowerCase(),
                                        param, type));
                                this.executeElement(xmlWriter, tmpParams, queryLevel);
                            }
                        } else {
                            this.executeElement(xmlWriter, params, queryLevel);
                        }
                    } catch (SQLException e) {
                        //Let the flow continue.
                    }
                } else {
                    this.execute(xmlWriter, params, queryLevel);
                }
            } else {
                throw new DataServiceFault("The array '" + this.getArrayName() + "' does not exist");
            }
        }
    }

    protected abstract void executeElement(XMLStreamWriter xmlWriter, ExternalParamCollection params,
                                           int queryLevel) throws DataServiceFault;

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

}
