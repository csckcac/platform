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
package org.wso2.carbon.issue.tracker.core;

import org.apache.commons.logging.Log;
import org.wso2.carbon.issue.tracker.adapter.exceptions.IssueTrackerException;

public class ExceptionHandler {

    public static void handleException(String msg, Exception e, Log log) throws IssueTrackerException {
        log.error(msg, e);
        throw new IssueTrackerException(msg, e);
    }

    public static void handleException(String msg, Log log) throws IssueTrackerException {
        log.error(msg);
        throw new IssueTrackerException(msg);
    }

}
