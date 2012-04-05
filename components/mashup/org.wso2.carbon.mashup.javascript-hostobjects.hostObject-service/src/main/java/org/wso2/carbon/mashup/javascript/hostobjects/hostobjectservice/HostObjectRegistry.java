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
package org.wso2.carbon.mashup.javascript.hostobjects.hostobjectservice;

import org.osgi.framework.Bundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mashup.javascript.hostobjects.hostobjectservice.service.HostObjectService;

import java.util.*;

public class HostObjectRegistry {

    Set<Bundle> activated = new HashSet<Bundle>();
    HostObjectService hostObjectService = HostObjectService.instance();
    private static final Log log = LogFactory.getLog(HostObjectRegistry.class);
    private static final String HOSTOBJECT_HEADER = "JavaScript-HostObject";
    private static final String HOSTOBJECT_SERVICE_NEEDED = "HostObjectServiceNeeded";

    public void register(Bundle bundle) {
      synchronized (activated) {
        if (activated.contains(bundle)) return;
      }
      List<HostObjectDetails> hostObjectList  = getHostObjectDetails(bundle);
        if (hostObjectList.size() > 0) {
            try {
                for (HostObjectDetails hostObjectDetails : hostObjectList) {
                    registerHostObject(bundle, hostObjectDetails);
                }
            } catch (Exception e) {
                log.error("[extender] Activating Host Object from "
                        + bundle.getLocation(), e);
            }
        }
        synchronized (activated) {
        activated.add(bundle);
      }
    }

    private void registerHostObject(Bundle bundle, HostObjectDetails hostObjectDetails) throws Exception {
        String className = hostObjectDetails.getClassName();
        Class clazz = bundle.loadClass(className);
      if (clazz != null) {
          hostObjectService.addHostObjectClass(className);
          String hostObjectName = hostObjectDetails.getHostObjectName();
          if (hostObjectName != null) {
            hostObjectService.addGlobalObject(hostObjectName, hostObjectDetails.getGlobalObjectName());
          }
          if (hostObjectDetails.isHostObjectServiceNeeded()) {
            hostObjectService.addHostObjectThatNeedServices(className); 
          }
      } else throw new IllegalArgumentException(
          "Can not find class " + className);
    }

    public void unregister(Bundle bundle) {
      synchronized (activated) {
        if (!activated.contains(bundle)) return;
        activated.remove(bundle);
      }

      List<HostObjectDetails> hostObjectList  = getHostObjectDetails(bundle);
        if (hostObjectList.size() > 0) {
            for (HostObjectDetails hostObjectDetails : hostObjectList) {
                hostObjectService.removeHostObject(hostObjectDetails.getClassName(), hostObjectDetails.getHostObjectName());
            }
        }
    }

    private List<HostObjectDetails> getHostObjectDetails(Bundle bundle) {
        List<HostObjectDetails> hostObjectList = new ArrayList<HostObjectDetails>();
      String header = (String) bundle.getHeaders().get(HOSTOBJECT_HEADER);
      if (header != null) {
          String clauses[] = header.split(",");
          for (String clause : clauses) {
              String parts[] = clause.split(";");
              HostObjectDetails hostObjectDetails = new HostObjectDetails();
              hostObjectDetails.setClassName(parts[0]);
              if (parts.length > 1) {
                  for (int i = 1; i < parts.length; i++) {
                      String innerParts[] = parts[i].trim().split("\\s*=\\s*");
                      if (innerParts.length == 2) {
                          if (HOSTOBJECT_SERVICE_NEEDED.equals(innerParts[0]) && Boolean.valueOf(innerParts[1])) {
                              hostObjectDetails.setHostObjectServiceNeeded(true);
                          } else {
                            hostObjectDetails.setHostObjectName(innerParts[0]);
                            hostObjectDetails.setGlobalObjectName(innerParts[1]);
                          }
                      }
                  }
              }
              hostObjectList.add(hostObjectDetails);
          }
      }
      return hostObjectList;
    }

    public void close() {
        for (Object anActivated : activated) {
            Bundle bundle = (Bundle) anActivated;
            unregister(bundle);
        }
    }

}
