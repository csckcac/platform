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

public class HumanTaskConstants {

    public static final String HUMANTASK_CONFIG_FILE = "humantask.xml";

    public static final String WSHT_EXP_LANG_XPATH20 = "urn:wsht:sublang:xpath2.0";

    public static final String HUMANTASK_REPO_DIRECTORY = "humantask";

    public static final String HUMANTASK_PACKAGE_TEMP_DIRECTORY = "tmp" +
            File.separator + "humantaskuploads";
    public static final String HUMANTASK_PACKAGE_EXTENSION = "zip";

    // Registry repository root location for storing human task deployment units
    public static final String HT_DEP_UNITS_REPO_LOCATION = "/humantask/deploymentunits/";

    @Deprecated
    public static final String HUMAN_INTERACTION_FEEDBACK_NS = "http://wso2.org/humantask/feedback";

    public static final String B4P_CORRELATION_HEADER = "correlation";
    public static final String B4P_CORRELATION_HEADER_ATTRIBUTE = "taskid";
	public static final String B4P_NAMESPACE = "http://docs.oasis-open.org/ns/bpel4people/bpel4people/200803";
    
	public static final int ITEMS_PER_PAGE = 20;
	
    public interface XPathConstants {




    }

    public static final Integer DEFAULT_TASK_PRIORITY = 5;

}
