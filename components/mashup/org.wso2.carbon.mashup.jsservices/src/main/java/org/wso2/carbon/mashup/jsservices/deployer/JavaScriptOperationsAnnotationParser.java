/*
 * Copyright 2006,2007 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mashup.jsservices.deployer;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.http.HTTPConstants;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.UniqueTag;
import org.wso2.javascript.xmlimpl.XML;
import org.wso2.carbon.mashup.jsservices.JSUtils;
import org.wso2.carbon.mashup.jsservices.JSConstants;
import org.wso2.javascript.xmlimpl.XMLList;

import java.util.ArrayList;
import java.util.List;

/**
 * This extracts & processes the operation level annotations when deploying the
 * Mashup Services using the java script deployer. Operation level annotations
 * can be given as optional properties to a top-level Function Object.
 * <p/>
 * <pre>
 * eg:
 *      //operation level annotations
 *      reverse.visible = true;
 *      reverse.safe = true;
 *      reverse.operationName = "swap";
 *      reverse.inputTypes = {
 *      "first" : "string";,
 *      "second" : "string"
 *      }
 *      reverse.outputType = "any";
 *      reverse.documentation = "Simple string or html markup (xml node, xml nodelist)";
 *      reverse.httpMethod = "GET";
 *      reverse.httpLocation = "reverse/{first}/{second}";
 * <p/>
 *      function reverse(first, last) {
 *          return (last + " " + first);
 *      }
 * <p/>
 * For more details visit <a
 * href="http://www.wso2.org/wiki/display/mashup/Javascript+Web+Service+Annotations">JavaScript
 * Web Service Annotation</a>
 */
public class JavaScriptOperationsAnnotationParser {

    // Holds the value for visible annotation
    private boolean visible = true;

    // Holds the value for safe annotation
    private Boolean safe;

    // Holds the value for operationName annotation
    private String operationName = null;

    // Holds the value for inputTypes annotation
    private Object inputTypesNameObject = null;

    // Holds the value for outputType annotation
    private Object outputTypeNameObject = null;

    // Holds the value for documentation annotation
    private OMNode documentation;

    // Holds the value for httpMethod annotation
    private String httpMethod = null;

    // Holds the value for httpLocation annotation
    private String httpLocation = null;

    // Holds the value for ignoreUncited annotation
    private boolean ignoreUncited = false;

    private OMFactory omFactory = OMAbstractFactory.getOMFactory();

    // Holds the value for operationParameters annotation
    private List<Parameter> operationParameters = new ArrayList<Parameter>();

    /**
     * Given a JavaScript function object and functionName extract all the defined annotations and
     * populates its private variables
     *
     * @param function     The JavaScript function object
     * @param functionName The JavaScript function name
     * @throws DeploymentException Thrown in case any annotations are not in the expected form
     */
    public JavaScriptOperationsAnnotationParser(Function function, String functionName)
            throws DeploymentException {

        // Check weather the user marked this operation as visible or not
        Object visibleObject = function.get(JSConstants.VISIBLE_ANNOTATION, function);
        visible = JSUtils.isJSObjectTrue(visible, visibleObject);

        // Check weather the user provided a operationName
        Object operationNameObject = function.get(JSConstants.OPERATION_NAME_ANNOTATION,
                                                  function);
        if (operationNameObject instanceof String) {
            String operationNameString = ((String) operationNameObject).trim();
            if (operationNameString.indexOf(' ') > -1) {
                throw new DeploymentException("Value of the operationName annotation ('"
                        + operationNameString + "') should be a single word.");
            }
            operationName = (String) operationNameObject;
        } else {
            operationName = functionName;
        }

        // Check weather the user provided inputTypes
        inputTypesNameObject = function.get(JSConstants.INPUT_TYPES_ANNOTATION, function);

        // Check weather the user provided a outputType
        outputTypeNameObject = function.get(JSConstants.OUTPUT_TYPE_ANNOTATION, function);

        // Check weather the user marked this operation as been safe
        Object safeObject = function.get(JSConstants.SAFE_ANNOTATION, function);
        if (safeObject == null || safeObject instanceof Undefined ||
                safeObject instanceof UniqueTag) {
            safe = null;
        } else {
            safe = JSUtils.isJSObjectTrue(true, safeObject);
        }

        // Check weather the user provided some operation level documentation. If the documentation
        // is a String we wrap it as a OMText. Else if its an E4X XML object we get the axiom from
        // it and set that as the documentation.
        Object documentationObject = function.get(JSConstants.DOCUMENTATION_ANNOTATION,
                                                  function);
        if (documentationObject instanceof String) {
            String documentationString = (String) documentationObject;
            //todo
//            String s = JSUtils.sanitizeHtml(documentationString);
            this.documentation =
                    omFactory.createOMText(documentationString);
        } else if (documentationObject instanceof XML) {
            XML xml = (XML) documentationObject;
            OMNode axiom = xml.getAxiomFromXML();
            this.documentation = axiom;
            //todo
            /*try {
                OMNode node = JSUtils.getSanizedHTMLAsOMNode(axiom.toString());
                this.documentation = node;
            } catch (XMLStreamException e) {
                throw new DeploymentException("The documentation for the operation " + operationName +
                        " is not well formed. Please make sure that the documentation is a " +
                        "String or valid XML");
            }*/
        }

        // Check weather the user provided a httpMethod
        Object httpMethodObject = function.get(JSConstants.HTTP_METHOD_ANNOTATION, function);
        if (httpMethodObject instanceof String) {
            String httpMethodString = ((String) httpMethodObject).trim();
            if (!(HTTPConstants.HEADER_GET.equalsIgnoreCase(httpMethodString) ||
                    HTTPConstants.HEADER_POST.equalsIgnoreCase(httpMethodString)
                    || HTTPConstants.HEADER_PUT.equalsIgnoreCase(httpMethodString) ||
                    HTTPConstants.HEADER_DELETE.equalsIgnoreCase(httpMethodString))) {
                throw new DeploymentException(
                        "Invalid httpMethod annotation on operation " + operationName
                                + ". The httpMthod annotation can accept only the values " +
                                "GET, POST, PUT or DELETE");
            }
            httpMethod = httpMethodString;
        }

        // Check weather the user provided a httpLocation
        Object httpLocationObject = function.get(JSConstants.HTTP_LOCATION_ANNOTATION,
                                                 function);
        if (httpLocationObject instanceof String) {
            httpLocation = ((String) httpLocationObject).trim();
        }

        Object ignoreUncitedObject = function.get(JSConstants.IGNORE_UNCITED_ANNOTATION, function);
        ignoreUncited = JSUtils.isJSObjectTrue(ignoreUncited, ignoreUncitedObject);

        // Check weather the user provided serviceParameters
        Object serviceParametersObject = function.get(JSConstants.OPERATION_PARAMETERS_ANNOTATION, function);
        if (serviceParametersObject instanceof NativeObject) {
            NativeObject nativeObject = (NativeObject) serviceParametersObject;
            Object[] propertyNames = nativeObject.getIds();
            for (Object propertyNameObject : propertyNames) {
                if (propertyNameObject instanceof String) {
                    String propertyName = (String) propertyNameObject;
                    Object propertyValueObject = nativeObject.get(propertyName, nativeObject);
                    if (propertyValueObject instanceof String) {
                        OMFactory factory = OMAbstractFactory.getOMFactory();
                        OMElement parameterElement = factory.createOMElement("parameter", null);
                        parameterElement.addAttribute("name", propertyName, null);
                        parameterElement.setText((String) propertyValueObject);
                        Parameter param = new Parameter(propertyName, propertyValueObject);
                        param.setParameterElement(parameterElement);
                        operationParameters.add(param);
                    } else if (propertyValueObject instanceof XML) {
                        XML xml = (XML) propertyValueObject;
                        OMNode axiom = xml.getAxiomFromXML();
                        OMFactory factory = OMAbstractFactory.getOMFactory();
                        OMElement parameterElement = factory.createOMElement("parameter", null);
                        parameterElement.addAttribute("name", propertyName, null);
                        parameterElement.addChild(axiom);
                        Parameter param = new Parameter(propertyName, axiom);
                        param.setParameterElement(parameterElement);
                        operationParameters.add(param);
                    } else if (propertyValueObject instanceof XMLList) {
                        XMLList list = (XMLList) propertyValueObject;
                        OMNode[] omNodes = list.getAxiomFromXML();
                        OMFactory factory = OMAbstractFactory.getOMFactory();
                        OMElement parameterElement = factory.createOMElement("parameter", null);
                        parameterElement.addAttribute("name", propertyName, null);
                        for (OMNode node : omNodes) {
                            parameterElement.addChild(node);
                        }
                        Parameter param = new Parameter(propertyName, omNodes);
                        param.setParameterElement(parameterElement);
                        operationParameters.add(param);
                    } else {
                        throw new DeploymentException("Invalid property value specified for " +
                                "\"operationProperties\" annotation : " + propertyName +
                                ". You should provide a string for property value.");
                    }
                } else {
                    throw new DeploymentException("Invalid property name specified for " +
                            "\"operationProperties\" annotation : " + propertyNameObject);
                }
            }
        }
    }

    /**
     * Getter for operationName
     *
     * @return String representing the operationName
     */
    public String getOperationName() {
        return operationName;
    }

    /**
     * Getter for visible
     *
     * @return boolean representing visibility of the function
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Getter for outputType
     *
     * @return Object A Javascript object that conatins the outputType annotation value
     */
    public Object getOutputTypeNameObject() {
        return outputTypeNameObject;
    }

    /**
     * Getter for inputTypes
     *
     * @return Object A Javascript object that conatins the inputTypes annotation value
     */
    public Object getInputTypesNameObject() {
        return inputTypesNameObject;
    }

    /**
     * Getter for safe
     *
     * @return Boolean representing the safety of the function
     */
    public Boolean isSafe() {
        return safe;
    }

    /**
     * Getter for documentation
     *
     * @return OMNode representing the documentation of the operation
     */
    public OMNode getDocumentation() {
        return documentation;
    }

    /**
     * Getter for httpMethod
     *
     * @return String representing the httpMethod
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * Getter for httpLocation
     *
     * @return String representing the httpLocation
     */
    public String getHttpLocation() {
        return httpLocation;
    }

    public boolean isIgnoreUncited() {
        return ignoreUncited;
    }

     /**
     * Getter for serviceParameters
     *
     * @return List A list that contains the serviceParameters annotation value
     */
    public List<Parameter> getOperationParameters() {
        return operationParameters;
    }
}