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
package org.wso2.mashup.deployer;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Feb 12, 2009
 * Time: 1:59:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class RubyConstants {

        public static final String SERVICE_RUBY = "ServiceRUBY";

        public static final String SERVICE_RUBY_SCRIPT = "ServiceRUBYScript";

        public static final String RESOURCES_FOLDER = "ResourcesFolder";

        public static final String AXIS2_MESSAGECONTEXT = "messageContext";

        /**
         * AxisService is injected to the Rhino engine as a thread local variable
         * with the key set to this value. Values from the AxisService is needed by some host
         * objects at the deployment time as well as in the run time.. In the run
         * time we would have obtained the AxisService through the injected
         * MessageContext. But we simply don't have a MessageContext in
         * the deployment time.
         */
        public static final String AXIS2_SERVICE = "axisService";

        /**
         * ConfigurationContext is injected to the Rhino engine as a thread local variable
         * with the key set to this value. Reason for doing this is same as the injecting the AxisService.
         */
        public static final String AXIS2_CONFIGURATION_CONTEXT = "axisConfigurationContext";

        public static final String LOAD_JSSCRIPTS = "loadJSScripts";

        public static final String JS_FUNCTION_NAME = "jsFunctionName";

        // Used to keep track of the presence of annotation of the types
        public static final String ANNOTATED = "annotated";

        // Refers to username of the mashup Author
        public static final String ALLOW_HTTP_TRAFFIC_TO_MASHUPS ="allowHTTPAccess";
    
       //Ruby Annotation syntax Mapping

        public static final String OPERATION_NAME = "operationName";
        public static final String RETURN = "returns";
       




}
