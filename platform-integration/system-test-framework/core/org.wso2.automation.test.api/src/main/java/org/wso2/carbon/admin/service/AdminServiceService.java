/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.admin.service;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.service.utils.AuthenticateStub;
import org.wso2.carbon.service.mgt.stub.*;
import org.wso2.carbon.service.mgt.stub.types.carbon.FaultyService;
import org.wso2.carbon.service.mgt.stub.types.carbon.FaultyServicesWrapper;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaDataWrapper;

import java.rmi.RemoteException;

public class AdminServiceService {
    private static final Log log = LogFactory.getLog(AdminServiceService.class);

    private ServiceAdminStub serviceAdminStub;

    public AdminServiceService(String backEndUrl) throws AxisFault {
        String serviceName = "ServiceAdmin";
        String endPoint = backEndUrl + serviceName;
        serviceAdminStub = new ServiceAdminStub(endPoint);
    }

    public void deleteService(String sessionCookie, String[] serviceGroup) throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, serviceAdminStub);

        serviceAdminStub.deleteServiceGroups(serviceGroup);

    }

    public void deleteFaultyService(String sessionCookie, String artifactPath)
            throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, serviceAdminStub);

        serviceAdminStub.deleteFaultyServiceGroup(artifactPath);


    }

    public void deleteAllNonAdminServiceGroups(String sessionCookie) throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, serviceAdminStub);

        serviceAdminStub.deleteAllNonAdminServiceGroups();


    }

    public ServiceMetaDataWrapper listServices(String sessionCookie, String serviceName)
            throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, serviceAdminStub);
        ServiceMetaDataWrapper serviceMetaDataWrapper;

        serviceMetaDataWrapper = serviceAdminStub.listServices("ALL", serviceName, 0);

        return serviceMetaDataWrapper;
    }


    public FaultyServicesWrapper listFaultyServices(String sessionCookie) throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, serviceAdminStub);
        FaultyServicesWrapper faultyServicesWrapper;

        faultyServicesWrapper = serviceAdminStub.getFaultyServiceArchives(0);

        return faultyServicesWrapper;
    }


    public boolean isServiceExists(String sessionCookie, String serviceName)
            throws RemoteException {
        boolean serviceState = false;
        ServiceMetaDataWrapper serviceMetaDataWrapper;
        ServiceMetaData[] serviceMetaDataList;
        serviceMetaDataWrapper = listServices(sessionCookie, serviceName);
        serviceMetaDataList = serviceMetaDataWrapper.getServices();
        if (serviceMetaDataList == null || serviceMetaDataList.length == 0) {
            serviceState = false;
        } else {
            for (ServiceMetaData serviceData : serviceMetaDataList) {
                if (serviceData != null && serviceData.getName().equalsIgnoreCase(serviceName)) {
                    return true;
                }
            }
        }
        return serviceState;
    }

    public void deleteMatchingServiceByGroup(String sessionCookie, String serviceFileName)
            throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, serviceAdminStub);
        String matchingServiceName = getMatchingServiceName(sessionCookie, serviceFileName);
        if (matchingServiceName != null) {
            String serviceGroup[] = {getServiceGroup(sessionCookie, matchingServiceName)};
            log.info("Service group name " + serviceGroup[0]);

            serviceAdminStub.deleteServiceGroups(serviceGroup);

        } else {
            log.error("Service group name cannot be null");
        }
    }

    public String deleteAllServicesByType(String sessionCookie, String type)
            throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, serviceAdminStub);
        ServiceMetaDataWrapper serviceMetaDataWrapper;

        serviceMetaDataWrapper = serviceAdminStub.listServices("ALL", null, 0);

        ServiceMetaData[] serviceMetaDataList;
        if (serviceMetaDataWrapper != null) {
            serviceMetaDataList = serviceMetaDataWrapper.getServices();

            String[] serviceGroup;
            if (serviceMetaDataList != null && serviceMetaDataList.length > 0) {

                for (ServiceMetaData serviceData : serviceMetaDataList) {
                    if (serviceData.getServiceType().equalsIgnoreCase(type)) {
                        serviceGroup = new String[]{serviceData.getServiceGroupName()};
                        deleteService(sessionCookie, serviceGroup);
                    }
                }
            }
        }
        return null;

    }

    public String getMatchingServiceName(String sessionCookie, String serviceFileName)
            throws RemoteException {

        AuthenticateStub.authenticateStub(sessionCookie, serviceAdminStub);
        ServiceMetaDataWrapper serviceMetaDataWrapper;
        serviceMetaDataWrapper = serviceAdminStub.listServices("ALL", serviceFileName, 0);


        ServiceMetaData[] serviceMetaDataList;
        if (serviceMetaDataWrapper != null) {
            serviceMetaDataList = serviceMetaDataWrapper.getServices();
            if (serviceMetaDataList != null && serviceMetaDataList.length > 0) {

                for (ServiceMetaData serviceData : serviceMetaDataList) {
                    if (serviceData != null && serviceData.getName().contains(serviceFileName)) {
                        return serviceData.getName();
                    }
                }
            }
        }
        return null;
    }

    public String getServiceGroup(String sessionCookie, String serviceName) throws RemoteException {
        ServiceMetaDataWrapper serviceMetaDataWrapper;
        ServiceMetaData[] serviceMetaDataList;
        serviceMetaDataWrapper = listServices(sessionCookie, serviceName);
        serviceMetaDataList = serviceMetaDataWrapper.getServices();
        if (serviceMetaDataList != null && serviceMetaDataList.length > 0) {

            for (ServiceMetaData serviceData : serviceMetaDataList) {
                if (serviceData != null && serviceData.getName().equalsIgnoreCase(serviceName)) {
                    return serviceData.getServiceGroupName();
                }
            }
        }
        return null;
    }

    public boolean isServiceFaulty(String sessionCookie, String serviceName)
            throws RemoteException {
        boolean serviceState = false;
        FaultyServicesWrapper faultyServicesWrapper;
        FaultyService[] faultyServiceList;
        faultyServicesWrapper = listFaultyServices(sessionCookie);
        if (faultyServicesWrapper != null) {
            faultyServiceList = faultyServicesWrapper.getFaultyServices();
            if (faultyServiceList == null || faultyServiceList.length == 0) {
                serviceState = false;
            } else {
                for (FaultyService faultyServiceData : faultyServiceList) {
                    if (faultyServiceData != null && faultyServiceData.getServiceName().equalsIgnoreCase(serviceName)) {
                        return true;
                    }
                }
            }
        }
        return serviceState;
    }

    public FaultyService getFaultyData(String sessionCookie, String serviceName)
            throws RemoteException {
        FaultyService faultyService = null;
        FaultyServicesWrapper faultyServicesWrapper;
        FaultyService[] faultyServiceList;
        faultyServicesWrapper = listFaultyServices(sessionCookie);
        if (faultyServicesWrapper != null) {
            faultyServiceList = faultyServicesWrapper.getFaultyServices();
            if (faultyServiceList == null || faultyServiceList.length == 0) {
                throw new RuntimeException("Service not found in faulty service list");
            } else {
                for (FaultyService faultyServiceData : faultyServiceList) {
                    if (faultyServiceData != null && faultyServiceData.getServiceName().equalsIgnoreCase(serviceName)) {
                        faultyService = faultyServiceData;
                    }
                }
            }
        }
        if (faultyService == null) {
            throw new RuntimeException("Service not found in faulty service list " + faultyService);
        }
        return faultyService;
    }

    public ServiceMetaData getServicesData(String sessionCookie, String serviceName)
            throws ServiceAdminException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, serviceAdminStub);

        return serviceAdminStub.getServiceData(serviceName);

    }

    public void startService(String sessionCookie, String serviceName)
            throws ServiceAdminException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, serviceAdminStub);

        serviceAdminStub.startService(serviceName);
        log.info("Service Started");

    }

    public void stopService(String sessionCookie, String serviceName)
            throws ServiceAdminException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, serviceAdminStub);

        serviceAdminStub.stopService(serviceName);
        log.info("Service Stopped");

    }
}
