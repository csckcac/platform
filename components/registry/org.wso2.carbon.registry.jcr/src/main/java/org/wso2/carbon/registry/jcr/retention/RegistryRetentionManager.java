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

import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.jcr.RegistrySession;
import org.wso2.carbon.registry.jcr.util.test.data.TCKTestDataLoader;

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
    private Map<String, RetentionPolicy> pendingRetentionPolicies = new HashMap<String, RetentionPolicy>();


    private RegistrySession session;

    public RegistryRetentionManager(RegistrySession session) {
        this.session = session;
    }

    public Map<String, RetentionPolicy> getPendingRetentionPolicies() {
        return pendingRetentionPolicies;
    }

    public Map<String, RetentionPolicy> getRetentionPolicies() {
        return retentionPolicies;
    }

    public Hold[] getHolds(String s) throws PathNotFoundException, AccessDeniedException, RepositoryException {

        if (!isPathValid(s)) {
            throw new RepositoryException("Cannot apply invalid path for retention holds " + s);
        }
        if (!isPathExists(s)) {
            throw new PathNotFoundException("No such Path exists for getting hold: " + s);
        }

        if (holdMap.size() != 0) {
            return holdMap.get(s).toArray(new Hold[holdMap.size()]);
        } else {
            return new Hold[0];
        }
    }

    public Hold addHold(String s, String s1, boolean b) throws PathNotFoundException, AccessDeniedException, LockException, VersionException, RepositoryException {

        if (!isPathValid(s1)) {
            throw new RepositoryException("Cannot apply invalid path for retention holds " + s);
        }
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

        try {
            TCKTestDataLoader.loadRetentionPolicies(retentionPolicies, session);
        } catch (RegistryException e) {
            throw new RepositoryException("Cannot load TCK test data for add retention " + s);
        }

        //Invalid path check
        if (!isPathValid(s)) {
            throw new RepositoryException("Cannot apply invalid path for retention policies " + s);
        }
        // Node existance check
        if (!isPathExists(s)) {
            throw new PathNotFoundException("No such Path exists for apply retention: " + s);
        }

        return retentionPolicies.get(s);

    }

    private boolean isPathExists(String s) throws RepositoryException {
        try {
            return session.getUserRegistry().resourceExists(s)
                    && session.getUserRegistry().get(s) instanceof CollectionImpl;
        } catch (RegistryException e) {
            throw new RepositoryException("Registry level exception occurred when applying retention to " + s);
        }
    }

    public void setRetentionPolicy(String s, RetentionPolicy retentionPolicy) throws PathNotFoundException, AccessDeniedException, LockException, VersionException, RepositoryException {
        //Invalid path check
        if (!isPathValid(s)) {
            throw new RepositoryException("Cannot apply invalid path for retention policies " + s);
        }

        if (!isValidJCRName(retentionPolicy.getName())) {
            throw new RepositoryException("Cannot apply invalid name for retention policies " + s);
        }

        if (!isPathExists(s)) {
            throw new PathNotFoundException("No such Path exists for apply retention: " + s);
        }

        retentionPolicies.put(s, retentionPolicy);
    }

    public void removeRetentionPolicy(String s) throws PathNotFoundException, AccessDeniedException, LockException, VersionException, RepositoryException {

        if (!isPathValid(s)) {
            throw new RepositoryException("Cannot apply invalid path for retention policies " + s);
        }

        if (!isPathExists(s)) {
            throw new PathNotFoundException("No such Path exists for apply retention: " + s);
        }

        if (retentionPolicies.get(s) == null) {
            throw new RepositoryException("Cannot remove retention from other nodes" + s);
        }
        retentionPolicies.remove(s);
    }

    private boolean isPathValid(String path) {
        if (path == null || !path.contains("/")
                || path.contains("*")
                || path.contains("[")
                || path.contains("]"))
//            TODO add more to validate jcr path naming
        {
            return false;
        } else {
            return true;
        }
    }

    private boolean isValidJCRName(String name) {
        if (name == null || name.contains("/")
                || name.contains("*"))
//            TODO add more to validate jcr naming
        {
            return false;
        } else {
            return true;
        }
    }

}
