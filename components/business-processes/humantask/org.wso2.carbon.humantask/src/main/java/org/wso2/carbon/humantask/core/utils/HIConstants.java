/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.utils;

import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

/**
 * Contains the Constants specific to Human Interactions
 */
public class HIConstants {


    // Registry repository root location for storing task configuration information
    public static final String HT_TASK_CONF_REPO_LOCATION = "/humantask/taskconfs/";

    public static final String PORT_TYPE_KEY = "portType";

    public static final String OPERATION_KEY = "operation";

    public static final String IS_RPC_KEY = "isRPC";

    public static final String HUMAN_TASK_ENGINE_PARAM = "humanTaskEngineInstance";

    public static final String HUMAN_INTERACTION_FEEDBACK_NS = "http://wso2.org/humantask/feedback";

    //Governance Registry location for wsdls in hi archives
    public static final String HUMAN_INTERACTION_WSDL_LOCATION = "/wsdls/hi/";

    //Governance Registry location for schemas in hi archives
    public static final String HUMAN_INTERACTION_SCHEMA_LOCATION = "/schemas/hi/";

    public static final String FUNCTION_GET_POTENTIAL_OWNERS = "getPotentialOwners";

    public static final String FUNCTION_GET_ACTUAL_OWNER = "getActualOwner";

    public static final String FUNCTION_GET_TASK_INITIATOR = "getTaskInitiator";

    public static final String FUNCTION_GET_TASK_STAKEHOLDERS = "getTaskStakeholders";

    public static final String FUNCTION_GET_BUSINESS_ADMINISTRATORS = "getBusinessAdministrators";

    public static final String FUNCTION_GET_EXCLUDED_OWNERS = "getExcludedOwners";

    public static final String FUNCTION_GET_TASK_PRIORITY = "getTaskPriority";

    public static final String FUNCTION_GET_INPUT = "getInput";

    public static final String FUNCTION_GET_LOGICAL_PEOPLE_GROUP = "getLogicalPeopleGroup";

    public static final String WSHT_EXP_LANG_XPATH10 = "urn:wsht:sublang:xpath1.0";

    public static final String WSHT_EXP_LANG_XPATH20 = "urn:wsht:sublang:xpath2.0";

    public static final String FUNCTION_UNION = "union";

    public static final String FUNCTION_INTERSECT = "intersect";

    public static final String FUNCTION_EXCEPT = "except";

    public static final String SOAP_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/soap/";

    public static final String WS_ADDRESSING_NS = "http://www.w3.org/2006/05/addressing/wsdl";

    public static final String FEEDBACK_HEADER_LOCALNAME = "feedback";
    public static final String FEEDBACK_HEADER_ROLE_URI = "http://schemas.xmlsoap.org/soap/envelope/actor/feedback";
    public static final String FEEDBACK_NAMESPACE = "http://wso2.org/humantask/feedback";
    public static final String FEEDBACK_ATTRIBUTE_NAME = "taskid";

    public static final int UNLIMITED = -1;
    public static final int HI_PACKAGES_PER_PAGE = 10;

    public static final int DEFAULT_TASK_PRIORITY = 5;

    public static final String SERVICE_TYPE = "serviceType";

    public static final String PRESERVE_SERVICE_HISTORY_PARAM = "preserveServiceHistory";

    public static final String HT_CUSTOMUI_LOCATION = CarbonUtils.getCarbonHome() + File.separator + "tmp" +
                                                      File.separator + "htcustomui" + File.separator;

    public static final String TASK_REQUEST_XSLT_LOCATION = "xslt/taskRequestTransformer.xsl";
}
