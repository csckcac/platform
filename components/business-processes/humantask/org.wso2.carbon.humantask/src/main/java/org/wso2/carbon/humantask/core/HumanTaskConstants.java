/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core;

import java.io.File;

/**
 * A placeholder class for human task constants used throughout the module.
 */
public final class HumanTaskConstants {
    private HumanTaskConstants() {
    }

    /** HumanTask file extension */
    public static final String HUMANTASK_FILE_EXT = ".ht";

    /** HumanTask config file name */
    public static final String HUMANTASK_CONFIG_FILE = "humantask.xml";

    /** XPath 2  */
    public static final String WSHT_EXP_LANG_XPATH20 = "urn:wsht:sublang:xpath2.0";

    /** HumanTask Repo directory name */
    public static final String HUMANTASK_REPO_DIRECTORY = "humantasks";

    /** HumanTask package temporary location */
    public static final String HUMANTASK_PACKAGE_TEMP_DIRECTORY = "tmp" +
            File.separator + "humantaskuploads";

    /** HumanTask package file extension */
    public static final String HUMANTASK_PACKAGE_EXTENSION = "zip";

    /** Registry repository root location for storing human task deployment units */
    public static final String HT_DEP_UNITS_REPO_LOCATION = "/humantask/deploymentunits/";

    /** BPEL4People correlation header  */
    public static final String B4P_CORRELATION_HEADER = "correlation";

    /** Bpel4People correlation header attribute */
    public static final String B4P_CORRELATION_HEADER_ATTRIBUTE = "taskid";

    /** BPEL4People namespace */
    public static final String B4P_NAMESPACE = "http://docs.oasis-open.org/ns/bpel4people/bpel4people/200803";

    /** Default pagination size */
	public static final int ITEMS_PER_PAGE = 20;

    /** */
    public static final String HUMANTASK_TASK_TYPE =  "humantaskType";

    /** HumanTask Cleanup job name */
    public static final String HUMANTASK_CLEANUP_JOB = "humantaskCleanupJob";

    /** The port off set identifier */
    public static final String CARBON_CONFIG_PORT_OFFSET_NODE = "Ports.Offset";

    /** The specification defines the default task priority to be set as 5 */
    public static final int DEFAULT_TASK_PRIORITY = 5;

    /** The default access type value for an attachment*/
    public static final String DEFAULT_ATTACHMENT_ACCESS_TYPE = "AnonymousAccessType";

    /** The default content category value for an attachment*/
    public static final String DEFAULT_ATTACHMENT_CONTENT_CATEGORY = "AnonymousContentCategory";

    /** The default content type of the presentation desc. */
    public static final String PRESENTATION_DESC_CONTENT_TYPE = "text/plain";

    /** The log name to enable message tracing for humantask component */
    public static final String MESSAGE_TRACE = "org.wso2.carbon.humantask.messagetrace";

    public static final String SOAP_ENV_NS = "http://schemas.xmlsoap.org/soap/envelope/";

    public static final String JAVAX_WSDL_VERBOSE_MODE_KEY = "javax.wsdl.verbose";
}
