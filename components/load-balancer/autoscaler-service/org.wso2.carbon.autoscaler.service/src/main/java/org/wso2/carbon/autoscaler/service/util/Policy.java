/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.autoscaler.service.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This object keeps the policy configuration used by autoscaler.
 * Default values of attributes are in correspondents to defaul policy configuration.
 * </p>
 * 
 * <p>Default policy is,
 * <ul><li>when scaling up: first will try to spawn JVM instances, if it failed only
 *         tries to spawn other instances.</li>
 *     <li>when scaling down: will terminate instances in a random order.</li>
 * </p>
 * 
 */
public class Policy {
    
    /**
     * This list is populated after reading autoscaler-policy XML.
     * This specifies the scale up order of adapters.
     * Lower the index, higher the priority.
     */
    private List<String> scaleUpOrderList = new ArrayList<String>();
    
    /**
     * This Map is populated after reading autoscaler-policy XML.
     * This specifies the scale down order of adapters .
     * Lower the index, higher the priority.
     */
    private List<String> scaleDownOrderList = new ArrayList<String>();
    
    /**
     * Key: Index of the {@link #scaleDownOrderList} 
     * Value: Minimum number of instance count which is specified in the autoscaler-policy XML.
     */
    private Map<Integer, Integer> scaleDownOrderIdToMinInstanceCountMap = 
            new HashMap<Integer, Integer>();
    
    
    public Policy() {}

    public List<String> getScaleUpOrderList() {
        return scaleUpOrderList;
    }

    /**
     * Using this method will overwrite the default policy's scale up order.
     * @param scaleUpOrderList
     */
    public void setScaleUpOrderList(List<String> scaleUpOrderList) {
        this.scaleUpOrderList = scaleUpOrderList;
    }

    public List<String> getScaleDownOrderList() {
        return scaleDownOrderList;
    }

    /**
     * Using this method will overwrite the default policy's scale down order.
     * @param scaleDownOrderList
     */
    public void setScaleDownOrderList(List<String> scaleDownOrderList) {
        this.scaleDownOrderList = scaleDownOrderList;
    }

    public Map<Integer, Integer> getScaleDownOrderIdToMinInstanceCountMap() {
        return scaleDownOrderIdToMinInstanceCountMap;
    }

    /**
     * Using this method will overwrite the default policy's corresponding map.
     * @param scaleDownOrderIdToMinInstanceCountMap
     */
    public void setScaleDownOrderIdToMinInstanceCountMap(
                             Map<Integer, Integer> scaleDownOrderIdToMinInstanceCountMap) {
        this.scaleDownOrderIdToMinInstanceCountMap = scaleDownOrderIdToMinInstanceCountMap;
    }
    
    

}
