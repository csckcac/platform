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
import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.description.Parameter;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.wso2.javascript.xmlimpl.XML;
import org.wso2.carbon.mashup.jsservices.JSConstants;
import org.wso2.javascript.xmlimpl.XMLList;

import java.util.ArrayList;
import java.util.List;

/**
 * This extracts & processes the service level annotations when deploying the
 * Mashup Services using the java script deployer. Service level annotations can
 * be given as optional properties to the Global object (referred to as "this"
 * outside any javascript function body).
 * <p/>
 * <pre>
 * eg:
 *      //service level annotations
 *       this.targetNamespace = "http://wso2.org/Mashup";
 *       this.schemaTargetNamespace = "http://wso2.org/schema";
 *       this.serviceName = "reverse";
 *       this.documentation = "Simple string or html markup";
 *       this.scope = "reverse";
 *       this.init=init;
 * <p/>
 *       function init() {
 *         // Do some stuff upon deployment
 *         print("init");
 *       }
 * <p/>
 *       OR
 * <p/>
 *       this.init= function init() {
 *         // Do some stuff upon deployment
 *         print("init");
 *       }
 * <p/>
 *       this.destroy=destroy;
 *       function destroy() {
 *         // Do some stuff upon undeployment
 *         print("destroy");
 *       }
 * <p/>
 *       OR
 * <p/>
 *       this.destroy= function destroy() {
 *         // Do some stuff upon undeployment
 *         print("destroy");
 *       }
 * <p/>
 *       this.undispatched= function bar(){
 *           // Do something here
 *       }
 * <p/>
 *       OR
 * <p/>
 *       this.undispatched= "bar";
 *       function bar(){
 *           // Do something here
 *       }
 * <p/>
 * </pre>
 * <p/>
 * For more details visit <a
 * href="http://www.wso2.org/wiki/display/mashup/Javascript+Web+Service+Annotations">JavaScript
 * Web Service Annotation</a>
 */
public class JavaScriptServiceAnnotationParser {

    // Holds the value for targetNamespace annotation
    private String targetNamespace = null;

    // Holds the value for schemaTargetNamespace annotation
    private String schemaTargetNamespace = null;

    // Holds the value for serviceName annotation
    private String serviceName;

    // Holds the value for serviceScope annotation
    private String serviceScope;

    // Holds the value for serviceDocumentation annotation
    private OMNode serviceDocumentation;

    // Holds the value for init annotation
    private Function init = null;

    // Holds the value for destroy annotation
    private Function destroy = null;

    // Holds the value for undispatched annotation
    private String undispatched;

    private OMFactory omFactory = OMAbstractFactory.getOMFactory();

    // Holds the value for serviceParameters annotation
    private List<Parameter> serviceParameters = new ArrayList<Parameter>();

    public JavaScriptServiceAnnotationParser(Scriptable service, String serviceName)
            throws DeploymentException {

        // Check weather the user provided a schemaTargetNamespace. If its not present use a default
        // namespace
        Object schemaTargetNamespaceObject =
                service.get(JSConstants.SCHEMA_TARGET_NAMESPACE_ANNOTATION, service);
        if (schemaTargetNamespaceObject instanceof String) {
            schemaTargetNamespace = (String) schemaTargetNamespaceObject;
        } else {
            schemaTargetNamespace = JSConstants.TARGET_NAMESPACE_PREFIX + serviceName +
                    JSConstants.QUESTION_MARK + JSConstants.XSD;
        }

        // Check weather the user provided a serviceName
        Object serviceNameObject = service.get(JSConstants.SERVICE_NAME_ANNOTATION, service);
        if (serviceNameObject instanceof String) {
            String serviceNameString = ((String) serviceNameObject).trim();
            if (serviceNameString.indexOf(' ') > -1) {
                throw new DeploymentException("Value of the serviceName annotation ('"
                        + serviceNameString + "') should be a single word.");
            }
            this.serviceName = serviceNameString;
        } else {
            this.serviceName = serviceName;
        }

        // Check weather the user provided a targetNamespace. If its not present use a default
        // namespace
        Object targetNamespaceObject = service.get(JSConstants.TARGET_NAMESPACE_ANNOTATION,
                service);
        if (targetNamespaceObject instanceof String) {
            targetNamespace = (String) targetNamespaceObject;
        } else {
            targetNamespace = JSConstants.TARGET_NAMESPACE_PREFIX + this.serviceName;
        }

        // Check weather the user provided a scope for the service
        Object serviceScopeObject = service.get(JSConstants.SCOPE_ANNOTATION, service);
        if (serviceScopeObject instanceof String) {
            this.serviceScope = (String) serviceScopeObject;
        }

        // Check weather the user provided some service level documentation. If the documentation is
        // a String we wrap it as a OMText. Else if its an E4X XML object we get the axiom from it
        // and set that as the documentation.
        Object serviceDocumentationObject = service.get(JSConstants.DOCUMENTATION_ANNOTATION,
                service);
        if (serviceDocumentationObject instanceof String) {
            String documentationString = (String) serviceDocumentationObject;
// todo
//            String s = MashupUtils.sanitizeHtml(documentationString);
            this.serviceDocumentation =
                    omFactory.createOMText(documentationString);
        } else if (serviceDocumentationObject instanceof XML) {
            XML xml = (XML) serviceDocumentationObject;
            OMNode axiom = xml.getAxiomFromXML();
            this.serviceDocumentation = axiom;
            //todo
            /*try {
                this.serviceDocumentation = MashupUtils.getSanizedHTMLAsOMNode(axiom.toString());
            } catch (XMLStreamException e) {
                throw new DeploymentException("The documentation for the service " + serviceName +
                        " is not well formed. Please make sure that the documentation is a " +
                        "String or valid XML");
            }*/
        }

        // Check weather the user provided a init annotation. If it was provided this function would
        // be called on service Deployment
        Object initObject = service.get(JSConstants.INIT_ANNOTATION, service);
        if (initObject instanceof Function) {
            this.init = (Function) initObject;
        }

        // Check weather the user provided a destroy annotation. If it was provided this function
        // would be called on service undeployment
        Object destroyObject = service.get(JSConstants.DESTROY_ANNOTATION, service);
        if (destroyObject instanceof Function) {
            this.destroy = (Function) destroyObject;
        }

        // Check weather the user provided a undispatched annotation. If it was provided any
        // undispatched operations will be dispatched to this special function
        Object undispatchedOperation =
                service.get(JSConstants.UNDISPATCHED_ANNOTATION, service);
        if (undispatchedOperation instanceof Function) {
            Scriptable scriptable = (Scriptable) undispatchedOperation;
            undispatched = (String) scriptable.get(JSConstants.NAME, scriptable);
        } else if (undispatchedOperation instanceof String) {
            undispatched = (String) undispatchedOperation;
        }

        // Check weather the user provided serviceParameters
        Object serviceParametersObject = service.get(JSConstants.SERVICE_PARAMETERS_ANNOTATION, service);
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
                        serviceParameters.add(param);
                    } else if (propertyValueObject instanceof XML) {
                        XML xml = (XML) propertyValueObject;
                        OMNode axiom = xml.getAxiomFromXML();
                        OMFactory factory = OMAbstractFactory.getOMFactory();
                        OMElement parameterElement = factory.createOMElement("parameter", null);
                        parameterElement.addAttribute("name", propertyName, null);
                        parameterElement.addChild(axiom);
                        Parameter param = new Parameter(propertyName, axiom);
                        param.setParameterElement(parameterElement);
                        serviceParameters.add(param);
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
                        serviceParameters.add(param);
                    } else {
                        throw new DeploymentException("Invalid property value specified for " +
                                "\"serviceProperties\" annotation : " + propertyName +
                                ". You should provide a string for property value.");
                    }
                } else {
                    throw new DeploymentException("Invalid property name specified for " +
                            "\"serviceProperties\" annotation : " + propertyNameObject);
                }
            }
        }
    }

    /**
     * Getter for the schemaTargetNamespace
     *
     * @return String representing the schemaTargetNamespace
     */
    public String getSchemaTargetNamespace() {
        return schemaTargetNamespace;
    }

    /**
     * Getter for the targetNamespace
     *
     * @return String representing the targetNamespace
     */
    public String getTargetNamespace() {
        return targetNamespace;
    }

    /**
     * Getter for the serviceName
     *
     * @return String representing the serviceName
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Getter for the serviceScope
     *
     * @return String representing the serviceScope
     */
    public String getServiceScope() {
        return serviceScope;
    }

    /**
     * Getter for the service Documentation
     *
     * @return OMNode representing the service Documentation
     */
    public OMNode getServiceDocumentation() {
        return serviceDocumentation;
    }

    /**
     * Getter for init
     *
     * @return Function representing the init operation
     */
    public Function getInit() {
        return init;
    }

    /**
     * Getter for destroy
     *
     * @return Function representing the destroy operation
     */
    public Function getDestroy() {
        return destroy;
    }

    /**
     * Getter for undispatched
     *
     * @return String representing the undispatched operation
     */
    public String getUndispatched() {
        return undispatched;
    }

    /**
     * Getter for serviceParameters
     *
     * @return List A list that contains the serviceParameters annotation value
     */
    public List<Parameter> getServiceParameters() {
        return serviceParameters;
    }
}