/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.mashup.jsservices;

public class JSConstants {

    //public static final String AXIS2_MESSAGECONTEXT = "messageContext";

    //public static final String LOAD_JSSCRIPTS = "loadJSScripts";

    //public static final String JS_FUNCTION_NAME = "jsFunctionName";

    // Used to keep track of the presence of annotation of the types
    //public static final String ANNOTATED = "annotated";

    // Refers to username of the mashup Author
    public static final String ALLOW_HTTP_TRAFFIC_TO_MASHUPS ="allowHTTPAccess";

    // The value that is used by the javascript init annotation. e.g this.init = function (){}
    public static final String INIT_ANNOTATION = "init";

    // The value that is used by the javascript destroy annotation.
    // e.g this.destroy = function foo(){}
    public static final String DESTROY_ANNOTATION = "destroy";

    // The value that is used by the javascript undispatched annotation.
    // e.g this.undispatched = function foo(){}
    public static final String UNDISPATCHED_ANNOTATION = "undispatched";

    // The value that is used by the javascript scope annotation. e.g this.scope = "application";
    public static final String SCOPE_ANNOTATION = "scope";

    // The value that is used by the javascript documentation annotation.
    // e.g this.documentation = "documentation";
    public static final String DOCUMENTATION_ANNOTATION = "documentation";

    // The value that is used by the javascript targetNamespace annotation.
    // e.g this.targetNamespace = "http://foo.com";
    public static final String TARGET_NAMESPACE_ANNOTATION = "targetNamespace";

    // The value that is used by the javascript schemaTargetNamespace annotation.
    // e.g this.schemaTargetNamespace = "http://foo.com?xsd";
    public static final String SCHEMA_TARGET_NAMESPACE_ANNOTATION = "schemaTargetNamespace";

    // The value that is used by the javascript serviceName annotation.
    // e.g this.serviceName = "serviceName";
    public static final String SERVICE_NAME_ANNOTATION = "serviceName";

    // The value that is used by the javascript operationName annotation.
    // e.g foo.operationName = "operationName";
    // function foo () {}
    public static final String OPERATION_NAME_ANNOTATION = "operationName";

    // The value that is used by the javascript visible annotation.
    // e.g foo.visible = false
    // function foo () {}
    public static final String VISIBLE_ANNOTATION = "visible";

    public static final String IGNORE_UNCITED_ANNOTATION = "ignoreUncited";

    // The value that is used by the javascript safe annotation.
    // e.g foo.safe = true";
    // function foo () {}
    public static final String SAFE_ANNOTATION = "safe";

    // The value that is used by the javascript inputTypes annotation.
    // e.g foo.inputTypes = "string";
    // function foo (a) {}
    public static final String INPUT_TYPES_ANNOTATION = "inputTypes";

    // The value that is used by the javascript outputType annotation.
    // e.g foo.outputType = "string";
    // function foo () {}
    public static final String OUTPUT_TYPE_ANNOTATION = "outputType";

    // The value that is used by the javascript httpMethod annotation.
    // e.g foo.httpMethod = "GET";
    // function foo () {}
    public static final String HTTP_METHOD_ANNOTATION = "httpMethod";

    // The value that is used by the javascript httpLocation annotation.
    // e.g foo.httpLocation = "foo/{a}";
    // function foo (a) {}
    public static final String HTTP_LOCATION_ANNOTATION = "httpLocation";

    // The value that is used by the javascript serviceParameters annotation.
    // e.g this.serviceParameters = {"para1" : "value1"};
    public static final String SERVICE_PARAMETERS_ANNOTATION = "serviceParameters";

    // The value that is used by the javascript operationParameters annotation.
    // e.g op.operationParameters = {"para1" : "value1"};
    public static final String OPERATION_PARAMETERS_ANNOTATION = "operationParameters";

    public static final String JS_SERVICES_REPO = "jsservices";

    // The value that is used by the javascript deployer as the prefix for the default target
    // namespaces. The target namespace will be of the form
    // "http://services.mashup.wso2.org/" + serviceName and the schemaTargetNamespace will be of the
    // form http://services.mashup.wso2.org/" + serviceName + "?xsd";
    public static final String TARGET_NAMESPACE_PREFIX = "http://services.mashup.wso2.org/";

    public static final String NAME = "name";
    public static final String XSD = "xsd";
    public static final String QUESTION_MARK = "?";    
    public static final String EMPTY_STRING = "";

    // The extention of the resources file created for each masgup
    public static final String MASHUP_RESOURCES_FOLDER = ".resources";
    public static final String MASHUP_PRIVATE_FOLDER_NAME = "_private";
    public static final String MASHUP_DESTROY_FUNCTION = "org.wso2.carbon.mashup.destroyFunction";

    public static final String AXIS2_SERVICE_TYPE = "serviceType";
}
