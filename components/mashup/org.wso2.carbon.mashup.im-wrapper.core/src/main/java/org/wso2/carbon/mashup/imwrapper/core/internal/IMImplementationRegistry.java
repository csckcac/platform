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
package org.wso2.carbon.mashup.imwrapper.core.internal;

import org.osgi.framework.Bundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class IMImplementationRegistry {
    Set<Bundle> activated = new HashSet<Bundle>();
    private Map<String, String> implementatiomMap = new HashMap<String, String>();
    private static final Log log = LogFactory.getLog(IMImplementationRegistry.class);
    private static final String IM_IMPLEMENTATION_HEADER = "IM-Wrapper-Implementation";
    private static final String PROTOCOL = "protocol";


    public void register(Bundle bundle) {
        synchronized (activated) {
            if (activated.contains(bundle)) return;
        }
        Map<String, String> map = processIMImplementations(bundle);

        try {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                registerIMImplementation(bundle, entry);
            }
        } catch (Exception e) {
            log.error("[extender] Activating Host Object from "
                    + bundle.getLocation(), e);
        }
        synchronized (activated) {
            activated.add(bundle);
        }
    }

    private void registerIMImplementation(Bundle bundle, Map.Entry<String, String> entry) throws Exception {
        String className = entry.getValue();
        Class clazz = bundle.loadClass(className);
      if (clazz != null) {
          implementatiomMap.put(entry.getKey(), entry.getValue());
      } else throw new IllegalArgumentException(
          "Can not find class " + className);
    }

    public void unregister(Bundle bundle) {
      synchronized (activated) {
        if (!activated.contains(bundle)) return;
        activated.remove(bundle);
      }

        Map<String, String> map = processIMImplementations(bundle);
        for (String key : map.keySet()) {
            implementatiomMap.remove(key);
        }
    }

    private Map<String, String> processIMImplementations(Bundle bundle) {
      Map<String, String> results = new HashMap<String, String>();
      String header = (String) bundle.getHeaders().get(IM_IMPLEMENTATION_HEADER);
      if (header != null) {
          String clauses[] = header.split(",");
          for (String clause : clauses) {
              String parts[] = clause.split(";");
              if (parts.length == 2) {
                  String innerParts[] = parts[1].trim().split("\\s*=\\s*");
                  if (innerParts.length == 2) {
                      if (PROTOCOL.equals(innerParts[0])) {
                          if (innerParts[1].indexOf(",") > 0) {
                              String[] protocols = innerParts[1].split(",");
                              for (String protocol : protocols) {
                                  results.put(protocol, parts[0]);
                              }
                          } else {
                            results.put(innerParts[1], parts[0]);
                          }
                      }
                  }
              }
          }
      }
        return results;
    }

    public void close() {
        for (Object anActivated : activated) {
            Bundle bundle = (Bundle) anActivated;
            unregister(bundle);
        }
    }

    public Map<String, String> getImplementatiomMap() {
        return implementatiomMap;
    }
}