/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.endpoint.service;


import org.wso2.carbon.endpoint.common.to.EndpointDO;
import java.util.*;
import org.wso2.carbon.endpoint.EndpointConstants;

/**
 * The <code>EndpointService</code> class provides service methods to configure the
 * endpoint module for a given service.
 */
public class EndpointService {

    private static List<EndpointDO> addressEndpoints = new ArrayList<EndpointDO>();
    private static List<EndpointDO> wsdlEndpoints = new ArrayList<EndpointDO>();
    private static List<EndpointDO> failoverGroup = new ArrayList<EndpointDO>();
    private static List<EndpointDO> failoverGroupInfo = new ArrayList<EndpointDO>();
    private static Map<String, List<EndpointDO>> failoverInfoHolder = new HashMap<String, List<EndpointDO>>();

    /**
     * Returns endpoint Meta data
     * @return meta data metadata array
     */
    public EndpointDO[] getEndpointMetaData() {

        ArrayList<EndpointDO> mergedList = new ArrayList<EndpointDO>();
        mergedList.addAll(addressEndpoints);
        mergedList.addAll(wsdlEndpoints);
        mergedList.addAll(failoverGroup);
        EndpointDO[] epArray = new EndpointDO[mergedList.size()];

        for (int i = 0; i < mergedList.size(); i++) {
            epArray[i] = mergedList.get(i);
        }
        return epArray;
    }

    /**
     * Add a failover endpoint name
     * @param fgName faiover group name
     * @param endpoint endpoint name
     */
    public void addfailoverEp(String fgName, EndpointDO endpoint) {

        int mapSize = failoverInfoHolder.size();
        if (mapSize > 0) {
            List<EndpointDO> eps = failoverInfoHolder.get(fgName);
            if (eps != null) {
                if (!checkEndpointExistance(endpoint.getEndPointName(), EndpointConstants.FAILOVER_ENDPOINT, fgName)) {
                    eps.add(endpoint);
                }
            } else {
                eps = new ArrayList<EndpointDO>();
                if (!checkEndpointExistance(endpoint.getEndPointName(), EndpointConstants.FAILOVER_ENDPOINT, fgName)) {
                    eps.add(endpoint);
                    failoverInfoHolder.put(fgName, eps);
                }
            }
        } else {
            List<EndpointDO> eps = new ArrayList<EndpointDO>();
            eps.add(endpoint);
            failoverInfoHolder.put(fgName, eps);
        }
    }

    /**
     * Returns failover endpoint meta data
     * @param fgName failover endpoint name
     * @return failover endpoint group metadata array
     */
    public EndpointDO[] getFailoverEndpointData(String fgName) {

        List<EndpointDO> eps = failoverInfoHolder.get(fgName);
        if (eps == null) return null;
        EndpointDO[] epArray = new EndpointDO[eps.size()];

        for (int i = 0; i < eps.size(); i++) {
            epArray[i] = eps.get(i);
        }
        return epArray;
    }

    /**
     * Sets endpoint metadata
     * @param endpointDO endpoint data
     * @param epType endpoint tyep, adress, WSDL, loadbalanc and failover
     */
    public void setEndpointMetaData(EndpointDO endpointDO, String epType) {

        if (epType.equalsIgnoreCase(EndpointConstants.ADDRESS_ENDPOINT)) {
            if (!checkEndpointExistance(endpointDO.getEndPointName(), epType, null)) {
                addressEndpoints.add(endpointDO);
            }
        } else if (epType.equalsIgnoreCase(EndpointConstants.WSDL_ENDPOINT)) {
            if (!checkEndpointExistance(endpointDO.getEndPointName(), epType, null)) {
                wsdlEndpoints.add(endpointDO);
            }
        } else if (epType.equalsIgnoreCase(EndpointConstants.FAILOVER_ENDPOINT)) {
            if (!checkEndpointExistance(endpointDO.getEndPointName(), epType, null)) {
                failoverGroup.add(endpointDO);
            }
        }
    }

    /**
     * Returns the existance of an endpoint
     * @param epName endpoint name
     * @param epType endpoint tyep
     * @param fgName failover group
     * @return true if found, else false
     */
    private boolean checkEndpointExistance(String epName, String epType, String fgName) {

        if (epType.equalsIgnoreCase(EndpointConstants.ADDRESS_ENDPOINT)) {
            Iterator<EndpointDO> epItr = addressEndpoints.iterator();
            while (epItr.hasNext()) {
                if (epItr.next().getEndPointName().equalsIgnoreCase(epName)) return true;
            }
        } else if (epType.equalsIgnoreCase(EndpointConstants.WSDL_ENDPOINT)) {
            Iterator<EndpointDO> wsItr = wsdlEndpoints.iterator();
            while (wsItr.hasNext()) {
                if (wsItr.next().getEndPointName().equalsIgnoreCase(epName)) return true;
            }
        } else if (epType.equalsIgnoreCase(EndpointConstants.FAILOVER_ENDPOINT)) {

            if (fgName != null) {
                List<EndpointDO> epList = failoverInfoHolder.get(fgName);
                if (epList != null) {
                    Iterator<EndpointDO> fgItr = epList.iterator();
                    while (fgItr.hasNext()) {
                        EndpointDO checkEp = fgItr.next();
                        if ((checkEp.getEndPointName().equalsIgnoreCase(epName)) && (checkEp.getEndPointType().equalsIgnoreCase(epType)))
                            return true;
                    }
                }
            } else {
                Iterator<EndpointDO> fgItr = failoverGroup.iterator();
                while (fgItr.hasNext()) {
                    if (fgItr.next().getEndPointName().equalsIgnoreCase(epName)) return true;
                }
            }
        }
        return false;
    }

    /**
     * Deletes an endpoint
     * @param epName endpoint name
     * @param epType endpoint type
     */
    public void deleteEndpoints(String epName, String epType) {
        if (epType.equalsIgnoreCase("Add")) {
            Iterator<EndpointDO> epItr = addressEndpoints.iterator();
            EndpointDO ep = null;
            while (epItr.hasNext()) {
                ep = epItr.next();
                if (ep.getEndPointName().equalsIgnoreCase(epName)) {
                    break;
                }
            }
            if (ep != null) {
                addressEndpoints.remove(ep);
            }
        } else if (epType.equalsIgnoreCase("WSDL")) {
            Iterator<EndpointDO> wsItr = wsdlEndpoints.iterator();
            EndpointDO wsEp = new EndpointDO();
            while (wsItr.hasNext()) {
                wsEp = wsItr.next();
                if (wsEp.getEndPointName().equalsIgnoreCase(epName)) {
                    break;
                }
            }
            wsdlEndpoints.remove(wsEp);
        }
    }

    public String sayHello(String param) {
        return "Hello" + param;
    }
}
