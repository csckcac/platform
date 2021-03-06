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
import org.wso2.carbon.jarservices.stub.DuplicateServiceExceptionException;
import org.wso2.carbon.jarservices.stub.DuplicateServiceGroupExceptionException;
import org.wso2.carbon.jarservices.stub.JarServiceCreatorAdminStub;
import org.wso2.carbon.jarservices.stub.JarUploadExceptionException;
import org.wso2.carbon.jarservices.stub.types.Resource;
import org.wso2.carbon.jarservices.stub.types.Service;
import org.wso2.carbon.jarservices.stub.types.UploadArtifactsResponse;

import javax.activation.DataHandler;
import java.rmi.RemoteException;
import java.util.List;

public class AdminServiceJARServiceUploader {
    private static final Log log = LogFactory.getLog(AdminServiceJARServiceUploader.class);

    private JarServiceCreatorAdminStub jarServiceCreatorAdminStub;

    public AdminServiceJARServiceUploader(String backEndUrl) throws AxisFault {
        String serviceName = "JarServiceCreatorAdmin";
        String endPoint = backEndUrl + serviceName;
        jarServiceCreatorAdminStub = new JarServiceCreatorAdminStub(endPoint);
    }

    public void uploadJARServiceFile(String sessionCookie, String serviceGroup,
                                     List<DataHandler> dhJarList, DataHandler dhWsdl)
            throws JarUploadExceptionException, RemoteException,
                   DuplicateServiceGroupExceptionException,
                   DuplicateServiceExceptionException {

        Resource resourceData;
        Resource resourceDataWsdl;
        UploadArtifactsResponse uploadArtifactsResponse;

        AuthenticateStub.authenticateStub(sessionCookie, jarServiceCreatorAdminStub);
        Resource[] resourceDataArray = new Resource[dhJarList.size()];

        assert dhJarList.size() != 0;
        for (int i = 0; dhJarList.size() > i; i++) {
            resourceData = new Resource();
            resourceData.setFileName(dhJarList.get(i).getName().substring(dhJarList.get(i).getName().lastIndexOf('/') + 1));
            resourceData.setDataHandler(dhJarList.get(i));
            resourceDataArray[i] = resourceData;
        }

        if (dhWsdl != null) {
            resourceDataWsdl = new Resource();
            resourceDataWsdl.setFileName(dhWsdl.getName().substring(dhWsdl.getName().lastIndexOf('/') + 1));
            resourceDataWsdl.setDataHandler(dhWsdl);
        } else {
            resourceDataWsdl = null;
        }

        uploadArtifactsResponse = jarServiceCreatorAdminStub.upload(serviceGroup, resourceDataWsdl,
                                                                    resourceDataArray);

        Service[] service = uploadArtifactsResponse.getServices();
        for (Service temp : service) {
            temp.setUseOriginalWsdl(false);
            temp.setDeploymentScope("request");
        }

        jarServiceCreatorAdminStub.createAndDeployService(uploadArtifactsResponse.getResourcesDirPath(), "", serviceGroup, service);
        log.info("Artifact uploaded");

    }
}
