/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.api.sla;

import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.sla.dataobjects.SLA;

/**
 * This interface represents a mechanism to filter that can be used to identify a sla from a
 * given set of sla.
 */
public interface SLAFilter {

    /**
     * Whether the given sla artifact matches the expected filter criteria.
     *
     * @param sla the sla artifact.
     *
     * @return true if a match was found or false otherwise.
     * @throws GovernanceException if the operation failed.
     */
    boolean matches(SLA sla) throws GovernanceException;
}
