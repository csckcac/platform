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

import org.apache.axis2.databinding.types.NCName;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.common.DBConstants.DBSFields;
import org.wso2.carbon.dataservices.common.DBConstants.FaultCodes;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DSSessionManager;
import org.wso2.carbon.dataservices.core.DataServiceFault;
import org.wso2.carbon.dataservices.core.boxcarring.TLParamStore;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.sql.SQLException;
import java.util.Set;

/**
 * Represents a static entry in a Result element.
 */
public class StaticOutputElement extends OutputElement {

    /**
     * name of element/attribute
     */
    private String name;

    /**
     * param value
     */
    private String param;

    /**
     * original param value, without any modifications: toLowerCase
     */
    private String originalParam;

    /**
     * i.e. column, query-param, value
     */
    private String paramType;

    /**
     * i.e. element, attribute
     */
    private String elementType;

    /**
     * i.e. xs:string, xs:decimal, etc..
     */
    private QName xsdType;

    /**
     * user roles required to access this element
     */
    private Set<String> requiredRoles;

    /**
     * i.e. XML, RDF etc..
     */
    private int resultType;

    /**
     * i.e. VALUE, REFERENCE etc..
     */
    private int dataCategory;

    /**
     * Exports the values in this element,
     * these will be saved in a thread local storage,
     * which can be re-used later by other queries
     */
    private String export;

    /**
     * The type of value to be exported, i.e. SCALAR, ARRAY
     */
    private int exportType;

    /**
     * A flag to keep if this output element's value is a constant,
     * i.e. paramType = 'value'
     */
    private boolean hasConstantValue;

    private String arrayName;

    public StaticOutputElement(DataService dataService, String name,
                               String param, String originalParam, String paramType,
                               String elementType, String namespace, QName xsdType,
                               Set<String> requiredRoles, int dataCategory, int resultType,
                               String export, int exportType, String arrayName) throws DataServiceFault {
        super(namespace);
        this.name = name;
        this.param = param;
        this.originalParam = originalParam;
        this.paramType = paramType;
        this.elementType = elementType;
        this.xsdType = xsdType;
        this.requiredRoles = requiredRoles;
        this.dataCategory = dataCategory;
        this.resultType = resultType;
        this.export = export;
        this.exportType = exportType;
        this.arrayName = arrayName;
        this.hasConstantValue = DBSFields.VALUE.equals(paramType);

        /* validate element/attribute name */
        if (!NCName.isValid(this.name)) {
            throw new DataServiceFault("Invalid output " + this.elementType + " name: '" +
                    this.name + "', must be an NCName.");
        }
    }

    public String getArrayName() {
        return arrayName;
    }

    public boolean hasConstantValue() {
        return hasConstantValue;
    }

    public String getExport() {
        return export;
    }

    public int getExportType() {
        return exportType;
    }

    public int getDataCategory() {
        return dataCategory;
    }

    public int getResultType() {
        return resultType;
    }

    public String getOriginalParam() {
        return originalParam;
    }

    public Set<String> getRequiredRoles() {
        return requiredRoles;
    }

    public boolean isOptional() {
        return this.getRequiredRoles() != null && this.getRequiredRoles().size() > 0;
    }

    public QName getXsdType() {
        return xsdType;
    }

    public String getName() {
        return name;
    }

    public String getParam() {
        return param;
    }

    public String getParamType() {
        return paramType;
    }

    public String getElementType() {
        return elementType;
    }

    private ParamValue getParamValue(ExternalParamCollection params) throws DataServiceFault {
        if (this.getParamType().equals(DBConstants.DBSFields.RDF_REF_URI)) {
            return new ParamValue(this.getParam());
        } else {
            ExternalParam paramObj = this.getParamObj(params);
            /* workaround for 'column', 'query-param' mix up */
            if (paramObj == null) {
                if (this.getParamType().equals(DBSFields.COLUMN)) {
                    paramObj = params.getParam(DBSFields.QUERY_PARAM, this.getParam());
                } else if (this.getParamType().equals(DBSFields.QUERY_PARAM)) {
                    paramObj = params.getParam(DBSFields.COLUMN, this.getParam());
                }
            }
            if (paramObj != null) {
                return paramObj.getValue();
            } else {
                throw new DataServiceFault(FaultCodes.INCOMPATIBLE_PARAMETERS_ERROR,
                        "Error in 'StaticOutputElement.execute', " +
                                "cannot find parameter with type:"
                                + this.getParamType() + " name:" + this.getOriginalParam());
            }
        }
    }

    /**
     * Exports the given parameter.
     *
     * @param exportName The name of the variable to store the exported value
     * @param value      The exported value
     */
    private void exportParam(String exportName, String value, int type) {
        ParamValue paramVal = TLParamStore.getParam(exportName);
        if (paramVal == null || paramVal.getValueType() != type) {
            paramVal = new ParamValue(type);
            TLParamStore.addParam(exportName, paramVal);
        }
        if (type == ParamValue.PARAM_VALUE_ARRAY) {
            paramVal.addToArrayValue(new ParamValue(value));
        } else if (type == ParamValue.PARAM_VALUE_SCALAR) {
            paramVal.setScalarValue(value);
        }
    }

    @Override
    public void executeElement(XMLStreamWriter xmlWriter, ExternalParamCollection params,
                               int queryLevel) throws DataServiceFault {
        ParamValue paramValue;
        if (this.hasConstantValue()) {
            paramValue = new ParamValue(this.getParam());
        } else {
            paramValue = this.getParamValue(params);
        }
        /* export it if told, and only if it's boxcarring */
        if (this.getExport() != null && DSSessionManager.isBoxcarring()) {
            this.exportParam(this.getExport(), paramValue.toString(), this.getExportType());
        }
        try {
            /* write element */
            if (this.getElementType().equals(DBSFields.ELEMENT)) {

                this.writeResultElement(xmlWriter, this.getName(), paramValue, this.getXsdType(),
                        this.getDataCategory(), this.getResultType(), params);

            } else if (this.getElementType().equals(DBSFields.ATTRIBUTE)) { /* write attribute */
                this.addAttribute(xmlWriter, this.getName(),
                        paramValue, this.getXsdType(), this.getResultType());
            }
        } catch (XMLStreamException e) {
            throw new DataServiceFault(e, "Error in XML generation at StaticOutputElement.execute");
        }
    }

    private ExternalParam getParamObj(ExternalParamCollection params) throws DataServiceFault {
        ExternalParam exParam;
        ParamValue processedParamValue;

        if (this.getParam().contains("[")) {
            exParam = DBUtils.getExParamObjectFromCollection(params, this.getParam(), this.getParamType(),
                    this.getParam().substring(0, this.getParam().indexOf("[")));
        } else {
            exParam = params.getParam(this.getParamType(), this.getParam());
        }
        if (exParam != null) {
            ParamValue paramValue = exParam.getValue();
            if (DBUtils.isUDT(paramValue)) {
                return this.getExParamFromUDT(exParam.getName(), paramValue);
            } else if (DBUtils.isSQLArray(paramValue)) {
                processedParamValue = new ParamValue(ParamValue.PARAM_VALUE_ARRAY);
                this.getExParamFromArray(processedParamValue, paramValue);

                return new ExternalParam(this.getParam(), processedParamValue, this.getParamType());
            }
        }

        return params.getParam(this.getParamType(), this.getParam());
    }

    /**
     * Extracts out an External parameter object representing an Array type ParamValue object.
     *
     * @param processedParamValue ExternalParam object produced after processing the original
     *                            ParamValue Object.
     * @param rawParamValue       ParamValue object without being converted.
     * @throws DataServiceFault Throws when the process is confronted with issues while processing
     *                          the UDT attributes.
     */
    private void getExParamFromArray(ParamValue processedParamValue,
                                     ParamValue rawParamValue) throws DataServiceFault {
        for (ParamValue value : rawParamValue.getArrayValue()) {
            if (DBUtils.isUDT(value)) {
                processedParamValue.getArrayValue().add(this.getExParamFromUDT(
                        this.getParam(), value).getValue());
            } else if (DBUtils.isSQLArray(value)) {
                this.getExParamFromArray(processedParamValue, value);
            } else {
                processedParamValue.getArrayValue().add(value);
            }
        }
    }

    /**
     * Extracts out an ExternalParam object after processing a User Defined Type.
     *
     * @param paramName  Name of the out parameter(mapping).
     * @param paramValue Value of the parameter referred by the aforementioned name.
     * @return External parameter object representing a given ParamValue object.
     * @throws DataServiceFault Throws when the process is confronted with SQL related issues while
     *                          processing UDT attributes.
     */
    private ExternalParam getExParamFromUDT(String paramName, ParamValue paramValue) throws
            DataServiceFault {
        ParamValue value;
        try {
            String indexString = this.getParam().substring(paramName.length(),
                    this.getParam().length());
            if (indexString != null && !"".equals(indexString)) {
                value = DBUtils.getUDTAttributeValue(DBUtils.getNestedIndices(indexString),
                        paramValue, 0);
            } else {
                indexString = this.getParam().substring(this.getParam().indexOf("["),
                        this.getParam().length());
                value = DBUtils.getUDTAttributeValue(DBUtils.getNestedIndices(indexString),
                        paramValue, 0);
            }

            return new ExternalParam(this.getName(), value, this.getParamType());
        } catch (SQLException e) {
            throw new DataServiceFault(e, "Unable to retrieve UDT attribute value referred by '" +
                    this.getParam() + "'");
        }
    }


    public boolean equals(Object o) {
        return (o instanceof StaticOutputElement) &&
                (((StaticOutputElement) o).getName().equals(this.getName()));
    }

}
