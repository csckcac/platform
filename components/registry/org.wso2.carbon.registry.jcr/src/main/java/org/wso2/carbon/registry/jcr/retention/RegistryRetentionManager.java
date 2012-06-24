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

import org.apache.xalan.xsltc.dom.LoadDocument;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Resource;
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
import java.util.*;


public class RegistryRetentionManager implements RetentionManager {


    private Map<String, Set<Hold>> holdMap = new HashMap<String, Set<Hold>>();
    private Map<String, RetentionPolicy> retentionPolicies = new HashMap<String, RetentionPolicy>();
    private Map<String, RetentionPolicy> pendingRetentionPolicies = new HashMap<String, RetentionPolicy>();


    private RegistrySession session;

    public RegistryRetentionManager(RegistrySession session) {
        this.session = session;
//       loadTCKTestdata();
    }

//    TODO REMOVE from svn, as this just need for TCK running
    private void loadTCKTestdata(){
        try {
            TCKTestDataLoader.loadRetentionPolicies(session);
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
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

//        if (holdMap.size() != 0) {
//            return holdMap.get(s).toArray(new Hold[holdMap.size()]);
//        } else {
//            return new Hold[0];
//        }

       return getHoldsFromRegistry(s);
    }

    public Hold addHold(String s, String s1, boolean b) throws PathNotFoundException, AccessDeniedException, LockException, VersionException, RepositoryException {

        if (!isPathValid(s1)) {
            throw new RepositoryException("Cannot apply invalid path for retention holds " + s);
        }
//        Hold aHold = new RegistryHold(s1, b);
//        if (holdMap.get(s) == null) {
//            Set<Hold> tempHd = new HashSet<Hold>();
//            tempHd.add(aHold);
//            holdMap.put(s, tempHd);
//        } else {
//            holdMap.get(s).add(aHold);
//        }
//        return aHold;
    return addHoldsToRegistry(s,s1,b);
    }

    public void removeHold(String s, Hold hold) throws PathNotFoundException, AccessDeniedException, LockException, VersionException, RepositoryException {
           removeHoldFromRegistry(s, hold);
//        ((Set) holdMap.get(s)).remove(hold);
    }

    public RetentionPolicy getRetentionPolicy(String s) throws PathNotFoundException, AccessDeniedException, RepositoryException {

        //Invalid path check
        if (!isPathValid(s)) {
            throw new RepositoryException("Cannot apply invalid path for retention policies " + s);
        }
        // Node existance check
        if (!isPathExists(s)) {
            throw new PathNotFoundException("No such Path exists for apply retention: " + s);
        }

//        return retentionPolicies.get(s);
      return getRetentionPolicyFromRegistry(s);
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

        setRetentionPolicyToRegistry(s,retentionPolicy);
//        retentionPolicies.put(s, retentionPolicy);
    }

    public void removeRetentionPolicy(String s) throws PathNotFoundException, AccessDeniedException, LockException, VersionException, RepositoryException {

        if (!isPathValid(s)) {
            throw new RepositoryException("Cannot apply invalid path for retention policies " + s);
        }

        if (!isPathExists(s)) {
            throw new PathNotFoundException("No such Path exists for apply retention: " + s);
        }

//        if (retentionPolicies.get(s) == null) {
//            throw new RepositoryException("Cannot remove retention from other nodes" + s);
//        }
//        retentionPolicies.remove(s);
        if (getRetentionPolicy(s) == null) {
            throw new RepositoryException("Cannot remove retention from other nodes" + s);
        }
        removeRetentionPolicyFromRegistry(s);
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

    private void setRetentionPolicyToRegistry(String s, RetentionPolicy retentionPolicy) throws  RepositoryException {
        Resource resource= null;
        try {
            resource = session.getUserRegistry().get(s);
        resource.setProperty("org.wso2.carbon.registry.jcr.retention.policy",retentionPolicy.getName());
         session.getUserRegistry().put(s,resource);
        } catch (RegistryException e) {
            throw new RepositoryException("Registry level exception when setting retention policy at " + s);
        }
    }

     private void removeRetentionPolicyFromRegistry(String s) throws  RepositoryException {
        Resource resource= null;
        try {
            resource = session.getUserRegistry().get(s);
            resource.removeProperty("org.wso2.carbon.registry.jcr.retention.policy");
         session.getUserRegistry().put(s,resource);
        } catch (RegistryException e) {
            throw new RepositoryException("Registry level exception when setting retention policy at " + s);
        }
    }


     private RetentionPolicy getRetentionPolicyFromRegistry(String s) throws RepositoryException {
         Resource resource= null;
         try {
             resource = session.getUserRegistry().get(s);
         } catch (RegistryException e) {
             throw new RepositoryException("Registry level exception when setting retention policy at " + s);
         }
         String name = resource.getProperty("org.wso2.carbon.registry.jcr.retention.policy");
         if(name != null) {
             return new RegistryRetentionPolicy(name);
         } else {
            return null;
         }
     }



    private Hold addHoldsToRegistry(String s, String s1, boolean b) throws RepositoryException {
        Resource resource = null;
        try {
            resource = session.getUserRegistry().get(s);
        if(resource.getPropertyValues("org.wso2.carbon.registry.jcr.retention.holds") == null) {
            List list = new ArrayList();
            list.add(s1 + ";" + String.valueOf(b));
            resource.setProperty("org.wso2.carbon.registry.jcr.retention.holds",list);
        } else {
          resource.getPropertyValues("org.wso2.carbon.registry.jcr.retention.holds").
                   add(s1 + ";" + String.valueOf(b));
        }
        session.getUserRegistry().put(s,resource);
        } catch (RegistryException e) {
            throw new RepositoryException("Registry level exception when setting retention policy at " + s);
        }

       return  new RegistryHold(s1, b);

    }

    private Hold[] getHoldsFromRegistry(String s) throws RepositoryException {
        Resource resource = null;
        List<Hold> holdList = new ArrayList<Hold>();
        try {
            resource = session.getUserRegistry().get(s);
            List holds = resource.getPropertyValues("org.wso2.carbon.registry.jcr.retention.holds");
        if(holds != null ){
          for(Object hold:holds) {
             String[] vals = hold.toString().split(";");
              holdList.add(new RegistryHold(vals[0],Boolean.valueOf(vals[1])));
          }
        }
        } catch (RegistryException e) {
            throw new RepositoryException("Registry level exception when setting retention policy at " + s);
        }

        return holdList.toArray(new RegistryHold[0]);
    }

    private void removeHoldFromRegistry(String s, Hold hold) throws RepositoryException {
        Resource resource = null;
        try {
            resource = session.getUserRegistry().get(s);
        List holds = resource.getPropertyValues("org.wso2.carbon.registry.jcr.retention.holds");
        List<Hold> holdList = new ArrayList<Hold>();
        String refHold = hold.getName()+";"+hold.isDeep();
        if(holds != null ) {
          for(Object _hold:holds) {
              if(_hold.equals(refHold)) {
                  resource.getPropertyValues("org.wso2.carbon.registry.jcr.retention.holds").remove(_hold);
              }
          }
        }
         session.getUserRegistry().put(s,resource);

        } catch (RegistryException e) {
            throw new RepositoryException("Registry level exception when setting retention policy at " + s);
        }

    }
}
