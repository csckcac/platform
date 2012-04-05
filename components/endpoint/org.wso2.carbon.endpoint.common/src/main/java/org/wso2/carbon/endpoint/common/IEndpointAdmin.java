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

package org.wso2.carbon.endpoint.common;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.endpoint.common.to.AddressEndpointData;
import org.wso2.carbon.endpoint.common.to.EndpointMetaData;
import org.wso2.carbon.endpoint.common.to.LoadBalanceEndpointData;
import org.wso2.carbon.endpoint.common.to.WSDLEndpointData;
import org.wso2.carbon.endpoint.common.to.DefaultEndpointData;
import org.wso2.carbon.mediation.dependency.mgt.ConfigurationObject;

/**
 *
 */
public interface IEndpointAdmin {

    public static final int ADDRESS_EP = 0;
    public static final int FAILOVER_EP = 2;
    public static final int WSDL_EP = 1;
    public static final int LOADBALANCE_EP = 3;
    public static final int DEFAULT_EP=4;

    EndpointMetaData[] endpointData(int pageNumber, int endpointsPerPage) throws EndpointAdminException;

    int getEndpointCount() throws EndpointAdminException;

    void enableStatistics(String endpointName) throws EndpointAdminException;

    void disableStatistics(String endpointName) throws EndpointAdminException;

    void switchOn(String endpointName) throws EndpointAdminException;

    void switchOff(String endpointName) throws EndpointAdminException;

    String[] getEndPointsNames() throws EndpointAdminException;

    OMElement convertToEndpointData(OMElement epElement) throws EndpointAdminException;

    boolean addEndpoint(String epName) throws EndpointAdminException;

    boolean addDynamicEndpoint(String key, String epName) throws EndpointAdminException;

    boolean saveEndpoint(String epName) throws EndpointAdminException;

    boolean saveDynamicEndpoint(String key, String epName) throws EndpointAdminException;

    String getEndpoint(String endpointName) throws EndpointAdminException;

    AddressEndpointData getAddressEndpoint(String endpointName) throws EndpointAdminException;

    DefaultEndpointData getDefaultEndpoint(String endpointName) throws EndpointAdminException;

    WSDLEndpointData getdlEndpoint(String endpointName) throws EndpointAdminException;

    LoadBalanceEndpointData getLoadBalanceData(String endpointName) throws EndpointAdminException;

    boolean deleteEndpoint(String endpointName) throws EndpointAdminException;

    ConfigurationObject[] getDependents(String endpointName);

    public boolean updateDynamicEndpoint(String key, String epName) throws Exception;

    public String getDynamicEndpoint(String key) throws Exception;

    public String[] getDynamicEndpoints(int pageNumber, int endpointsPerPage) throws Exception;

    int getDynamicEndpointCount() throws EndpointAdminException;

}
