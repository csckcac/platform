/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bam.common.dataobjects.service;

import java.util.Calendar;

/*
 * Service statistics Data class
 */
public class ServiceStatisticsDO extends ServerStatisticsDO {

    private String serviceName;
    private int serviceID;

    public ServiceStatisticsDO(){
        this.serviceID = -1;
    }

      public ServiceStatisticsDO(String serverURL, Calendar timestamp,double avgResTime, double maxResTime, double minResTime,
    		int reqCount, int resCount, int faultCount, String serviceName) {

    	super(serverURL, timestamp, avgResTime, maxResTime, minResTime, reqCount, resCount, faultCount);
    	this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getServiceID() {
        return this.serviceID;
    }

     public void setServiceID(int serviceID) {
        this.serviceID = serviceID;
    }

//    public ServiceDO getService() throws BAMException {
//        ServiceDO service;
//        if (serviceID > 0) {
//            service = BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry()).getService(getServiceID());
//        } else {
//            service = BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry()).getService(getServerID(), getServiceName());
//        }
//
//        if (service != null) {
//            this.serviceID = service.getId(); // The ID is auto generated at DB level.
//                                              // Hence we need to pick it up for future use
//        }
//
//        return service;
//    }

}
