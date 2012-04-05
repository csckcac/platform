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

import junit.framework.Assert;
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

    public AdminServiceService(String backEndUrl) {
        String serviceName = "ServiceAdmin";
        String endPoint = backEndUrl + serviceName;
        log.debug("EndPoint :" + endPoint);
        try {
            serviceAdminStub = new ServiceAdminStub(endPoint);
        } catch (AxisFault axisFault) {
            log.error("ServiceAdminStub Initializing failed : " + axisFault.getMessage());
            Assert.fail("ServiceAdminStub Initializing failed : " + axisFault.getMessage());
        }
    }

    public void deleteService(String sessionCookie, String[] serviceGroup) {
        new AuthenticateStub().authenticateStub(sessionCookie, serviceAdminStub);
        try {
            serviceAdminStub.deleteServiceGroups(serviceGroup);

        } catch (RemoteException e) {
            log.error("Delete Service Fail :" + e.getMessage());
            Assert.fail("Delete Service Fail :" + e.getMessage());
        }

    }

    public void deleteFaultyService(String sessionCookie, String artifactPath) {
        new AuthenticateStub().authenticateStub(sessionCookie, serviceAdminStub);
        try {
            serviceAdminStub.deleteFaultyServiceGroup(artifactPath);
        } catch (RemoteException e) {
            log.error("Deleting Faulty Service Failed due to RemoteException :" + e.getMessage());
            Assert.fail("Deleting Faulty Service Failed due to RemoteException :" + e.getMessage());
        }

    }

    public void deleteAllNonAdminServiceGroups(String sessionCookie) {
        new AuthenticateStub().authenticateStub(sessionCookie, serviceAdminStub);
        try {
            serviceAdminStub.deleteAllNonAdminServiceGroups();
        } catch (RemoteException e) {
            log.error("Deleting All None admin services Failed due to RemoteException :" + e.getMessage());
            Assert.fail("Deleting All None admin services Failed due to RemoteException :" + e.getMessage());
        }

    }

    public ServiceMetaDataWrapper listServices(String sessionCookie, String serviceName) {
        new AuthenticateStub().authenticateStub(sessionCookie, serviceAdminStub);
        ServiceMetaDataWrapper serviceMetaDataWrapper = null;
        try {
            serviceMetaDataWrapper = serviceAdminStub.listServices("ALL", serviceName, 0);
        } catch (RemoteException e) {
            log.error("List Service Failed due to RemoteException :" + e.getMessage());
            Assert.fail("List Service Failed due to RemoteException :" + e.getMessage());
        }
        Assert.assertNotNull("No Service found. Service List null :", serviceMetaDataWrapper);
        return serviceMetaDataWrapper;
    }


    public FaultyServicesWrapper listFaultyServices(String sessionCookie) {
        new AuthenticateStub().authenticateStub(sessionCookie, serviceAdminStub);
        FaultyServicesWrapper faultyServicesWrapper = null;
        try {
            faultyServicesWrapper = serviceAdminStub.getFaultyServiceArchives(0);
        } catch (RemoteException e) {
            log.error("List Faulty Service Failed due to RemoteException :" + e.getMessage());
            Assert.fail("List Faulty Service Failed due to RemoteException :" + e.getMessage());
        }
        return faultyServicesWrapper;
    }


    public boolean isServiceExists(String sessionCookie, String serviceName) {
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

    public void deleteMatchingServiceByGroup(String sessionCookie, String serviceFileName) throws RemoteException {
        new AuthenticateStub().authenticateStub(sessionCookie, serviceAdminStub);
        String matchingServiceName = getMatchingServiceName(sessionCookie, serviceFileName);
        if (matchingServiceName != null) {
            String serviceGroup[] = {getServiceGroup(sessionCookie, matchingServiceName)};
            log.info("Service group name " + serviceGroup[0]);
            try {
                serviceAdminStub.deleteServiceGroups(serviceGroup);
            } catch (RemoteException e) {
                log.error("Delete ServiceGroup Fail :" + e.getMessage());
                Assert.fail("Delete ServiceGroup Fail :" + e.getMessage());
            }
        } else {
            log.error("Service group name cannot be null");
        }
    }

    public String deleteAllServicesByType(String sessionCookie,String type) throws RemoteException {
        new AuthenticateStub().authenticateStub(sessionCookie, serviceAdminStub);
        ServiceMetaDataWrapper serviceMetaDataWrapper;
        try {
            serviceMetaDataWrapper = serviceAdminStub.listServices("ALL", null, 0);
        } catch (RemoteException e) {
            log.error("List Service Fail :" + e.getMessage());
            throw new RemoteException("List Service Fail :" + e.getMessage());
        }

        ServiceMetaData[] serviceMetaDataList;
        assert serviceMetaDataWrapper != null;
        serviceMetaDataList = serviceMetaDataWrapper.getServices();
        String[] serviceGroup;
        if (serviceMetaDataList != null && serviceMetaDataList.length > 0) {

            for (ServiceMetaData serviceData : serviceMetaDataList) {
                if(serviceData.getServiceType().equalsIgnoreCase(type)){
                    serviceGroup = new String[]{serviceData.getServiceGroupName()};
                    deleteService(sessionCookie, serviceGroup );
                }
            }
        }
        return null;

    }

    public String getMatchingServiceName(String sessionCookie, String serviceFileName) throws RemoteException {

        new AuthenticateStub().authenticateStub(sessionCookie, serviceAdminStub);
        ServiceMetaDataWrapper serviceMetaDataWrapper;
        try {
            serviceMetaDataWrapper = serviceAdminStub.listServices("ALL", serviceFileName, 0);
        } catch (RemoteException e) {
            log.error("List Service Fail :" + e.getMessage());
            throw new RemoteException("List Service Fail :" + e.getMessage());
        }

        ServiceMetaData[] serviceMetaDataList;
        assert serviceMetaDataWrapper != null;
        serviceMetaDataList = serviceMetaDataWrapper.getServices();
        if (serviceMetaDataList != null && serviceMetaDataList.length > 0) {

            for (ServiceMetaData serviceData : serviceMetaDataList) {
                if (serviceData != null && serviceData.getName().contains(serviceFileName)) {
                    return serviceData.getName();
                }
            }
        }
        return null;
    }

    public String getServiceGroup(String sessionCookie, String serviceName) {
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

    public boolean isServiceFaulty(String sessionCookie, String serviceName) {
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

    public FaultyService getFaultyData(String sessionCookie, String serviceName) {
        FaultyService faultyService = null;
        FaultyServicesWrapper faultyServicesWrapper;
        FaultyService[] faultyServiceList;
        faultyServicesWrapper = listFaultyServices(sessionCookie);
        if (faultyServicesWrapper != null) {
            faultyServiceList = faultyServicesWrapper.getFaultyServices();
            if (faultyServiceList == null || faultyServiceList.length == 0) {
                Assert.fail("Service not found in faulty service list");
            } else {
                for (FaultyService faultyServiceData : faultyServiceList) {
                    if (faultyServiceData != null && faultyServiceData.getServiceName().equalsIgnoreCase(serviceName)) {
                        faultyService = faultyServiceData;
                    }
                }
            }
        }
        Assert.assertNotNull("Service not found in faulty service list", faultyService);
        return faultyService;
    }

    public ServiceMetaData getServicesData(String sessionCookie, String serviceName) {
        new AuthenticateStub().authenticateStub(sessionCookie, serviceAdminStub);
        try {
            return serviceAdminStub.getServiceData(serviceName);
        } catch (RemoteException e) {
            log.error("Service Not Found due to RemoteException :" + e.getMessage());
            Assert.fail("Service Not Found :" + e.getMessage());
        } catch (ServiceAdminException e) {
            log.error("Service Not Found due to ServiceAdminException :" + e.getMessage());
            Assert.fail("Service Not Found :" + e.getMessage());
        }
        Assert.assertNotNull("Service Not found. Service Data null :");
        return null;
    }

    public void startService(String sessionCookie, String serviceName) {
        new AuthenticateStub().authenticateStub(sessionCookie, serviceAdminStub);
        try {
            serviceAdminStub.startService(serviceName);
            log.info("Service Started");
        } catch (RemoteException e) {
            log.error("Service Starting failed due to RemoteException :" + e.getMessage());
            Assert.fail("Service Starting failed due to RemoteException :" + e.getMessage());
        } catch (ServiceAdminException e) {
            log.error("Service Starting failed due to Exception :" + e.getMessage());
            Assert.fail("Service Starting failed due to Exception :" + e.getMessage());
        }
    }

    public void stopService(String sessionCookie, String serviceName) {
        new AuthenticateStub().authenticateStub(sessionCookie, serviceAdminStub);

        try {
            serviceAdminStub.stopService(serviceName);
            log.info("Service Stopped");
        } catch (RemoteException e) {
            log.error("Service Stopping failed due to RemoteException :" + e.getMessage());
            Assert.fail("Service Stopping failed due to RemoteException :" + e.getMessage());
        } catch (ServiceAdminException e) {
            log.error("Service Stopping failed due to Exception :" + e.getMessage());
            Assert.fail("Service Stopping failed due to Exception :" + e.getMessage());
        }
    }
}
