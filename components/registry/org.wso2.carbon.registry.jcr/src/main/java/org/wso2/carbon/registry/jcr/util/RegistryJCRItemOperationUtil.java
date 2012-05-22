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

package org.wso2.carbon.registry.jcr.util;

import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.jcr.RegistrySession;

import javax.jcr.*;

public class RegistryJCRItemOperationUtil {

    public static String replaceNameSpacePrefixURIS(String path) {
        for (String uri : RegistryJCRSpecificStandardLoderUtil.getJCRSystemNameSpaceURIList()) {
            path = path.replace("{" + uri + "}", RegistryJCRSpecificStandardLoderUtil.
                    getJCRSystemNameSpacePrefxMap().get(uri) + ":");
        }
        return path;
    }

    public static String getAncestorPathAtGivenDepth(String path, int depth) throws ItemNotFoundException {
        String[] tmp = path.split("/");
        if ((depth > tmp.length - 1) || depth < 0) {
            throw new ItemNotFoundException("No such Ancestor exists. 0 < depth < n violated");
        }

        if (depth == 0) {
            return "/";   //TODO should use a given root whose path return from session.getRootNode();
        } else {
            int index = nthOccurrence(path, '/', depth);
            return path.substring(0, index);
        }
    }

    private static int nthOccurrence(String str, char c, int n) {
        str = str + "/";
        int pos = str.indexOf(c, 0);
        while (n-- > 0 && pos != -1)
            pos = str.indexOf(c, pos + 1);
        return pos;
    }

    public static void validateReadOnlyItemOpr(RegistrySession registrySession) throws AccessDeniedException {
           if(RegistryJCRSpecificStandardLoderUtil.isSessionReadOnly(registrySession.getUserID())) {
        throw new AccessDeniedException("A read only session must not be allowed to modify a property value");
     }
    }

    public static void validateSessionSaved(RegistrySession registrySession) throws InvalidItemStateException{
          if(!registrySession.isSessionSaved()) {
              throw new InvalidItemStateException("Tried to access unsaved operations : Invalid item state");
          }

    }

    public static void validateSystemConfigPath(String path) throws RepositoryException {
       if(path == null) {
         throw new RepositoryException("Null is an invalid path expression");
       }

       if(path.contains(RegistryJCRSpecificStandardLoderUtil.JCR_SYSTEM_CONFIG)) {
           throw new RepositoryException(RegistryJCRSpecificStandardLoderUtil.
                   JCR_SYSTEM_CONFIG+" cannot be included in a path,it is a system specific config node");
       }

    }

    public static boolean isSystemConfigNode(String path) throws RepositoryException {
       if(path == null) {
         throw new RepositoryException("Null is an invalid path expression");
       }

       if(path.contains(RegistryJCRSpecificStandardLoderUtil.JCR_SYSTEM_CONFIG)) {
        return true;
       } else {
         return false;
       }

    }

//    public static boolean isNodeVersionable(Session session, String path) throws RepositoryException {
//
//      if(  session.getNode(path).isNodeType("mix:versionable") ||
//           session.getNode(path).isNodeType("mix:simpleVersionable")){
//         return true;
//      } else {
//          return false;
//      }
//    }



}
