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

package org.wso2.carbon.registry.jcr.retention;

import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.retention.Hold;
import javax.jcr.retention.RetentionManager;
import javax.jcr.retention.RetentionPolicy;
import javax.jcr.version.VersionException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class RegistryRetentionManager implements RetentionManager {


    private Map<String, Set<Hold>> holdMap = new HashMap<String, Set<Hold>>();
    private Map<String, RetentionPolicy> retentionPolicies = new HashMap<String, RetentionPolicy>();


    public Hold[] getHolds(String s) throws PathNotFoundException, AccessDeniedException, RepositoryException {

        if (holdMap.size() != 0) {

            return holdMap.get(s).toArray(new Hold[holdMap.size()]);

        } else {

            return new Hold[0];

        }
    }

    public Hold addHold(String s, String s1, boolean b) throws PathNotFoundException, AccessDeniedException, LockException, VersionException, RepositoryException {

        Hold aHold = new RegistryHold(s1, b);

        if (holdMap.get(s) == null) {

            Set<Hold> tempHd = new HashSet<Hold>();
            tempHd.add(aHold);
            holdMap.put(s, tempHd);

        } else {

            holdMap.get(s).add(aHold);

        }

        return aHold;
    }

    public void removeHold(String s, Hold hold) throws PathNotFoundException, AccessDeniedException, LockException, VersionException, RepositoryException {

        ((Set) holdMap.get(s)).remove(hold);
    }

    public RetentionPolicy getRetentionPolicy(String s) throws PathNotFoundException, AccessDeniedException, RepositoryException {

        return retentionPolicies.get(s);
    }

    public void setRetentionPolicy(String s, RetentionPolicy retentionPolicy) throws PathNotFoundException, AccessDeniedException, LockException, VersionException, RepositoryException {

        retentionPolicies.put(s, retentionPolicy);
    }

    public void removeRetentionPolicy(String s) throws PathNotFoundException, AccessDeniedException, LockException, VersionException, RepositoryException {

        retentionPolicies.remove(s);
    }
}
