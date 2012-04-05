/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.dao.jpa.openjpa.util;


import org.h2.util.StringUtils;
import org.wso2.carbon.humantask.client.api.types.TGroup;
import org.wso2.carbon.humantask.client.api.types.TOrganizationalEntity;
import org.wso2.carbon.humantask.client.api.types.TOrganizationalEntityChoice;
import org.wso2.carbon.humantask.client.api.types.TSimpleQueryInput;
import org.wso2.carbon.humantask.client.api.types.TUser;
import org.wso2.carbon.humantask.core.dao.HumanTaskDAOConnection;
import org.wso2.carbon.humantask.core.dao.OrganizationalEntityDAO;
import org.wso2.carbon.humantask.core.dao.SimpleQueryCriteria;
import org.wso2.carbon.humantask.core.engine.HumanTaskEngine;
import org.wso2.carbon.humantask.core.engine.PeopleQueryEvaluator;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * The transformer from and to human task definition object types to jpa model objects.
 */
public class TransformationUtil {
    //TODO - REMOVE ME!

}
