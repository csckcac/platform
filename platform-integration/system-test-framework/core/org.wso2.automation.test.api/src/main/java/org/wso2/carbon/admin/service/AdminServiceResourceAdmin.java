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
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceResourceServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;
import org.wso2.carbon.registry.resource.stub.common.xsd.ResourceData;

import javax.activation.DataHandler;
import java.rmi.RemoteException;


public class AdminServiceResourceAdmin {
    private static final Log log = LogFactory.getLog(AdminServiceResourceAdmin.class);

    private final String serviceName = "ResourceAdminService";
    private ResourceAdminServiceStub resourceAdminServiceStub;
    private String endPoint;

    private static final String MEDIA_TYPE_WSDL = "application/wsdl+xml";
    private static final String MEDIA_TYPE_SCHEMA = "application/x-xsd+xml";
    private static final String MEDIA_TYPE_POLICY = "application/policy+xml";
    private static final String MEDIA_TYPE_GOVERNANCE_ARCHIVE = "application/vnd.wso2.governance-archive";

    public AdminServiceResourceAdmin(String backEndUrl) throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        log.debug("Endpoint :" + endPoint);

        resourceAdminServiceStub = new ResourceAdminServiceStub(endPoint);

    }

    public boolean addResource(String sessionCookie, String destinationPath, String mediaType,
                               String description, DataHandler dh)
            throws ResourceAdminServiceExceptionException, RemoteException {

        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        log.debug("Destination Path :" + destinationPath);
        log.debug("Media Type :" + mediaType);

        return resourceAdminServiceStub.addResource(destinationPath, mediaType, description, dh, null);

    }

    public ResourceData[] getResource(String sessionCookie, String destinationPath)
            throws ResourceAdminServiceExceptionException, RemoteException {
        ResourceData[] rs;
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        log.debug("Destination Path :" + destinationPath);

        rs = resourceAdminServiceStub.getResourceData(new String[]{destinationPath});

        return rs;
    }

    public CollectionContentBean getCollectionContent(String sessionCookie, String destinationPath)
            throws RemoteException, ResourceAdminServiceExceptionException {
        CollectionContentBean collectionContentBean;
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);

        try {
            collectionContentBean = resourceAdminServiceStub.getCollectionContent(destinationPath);
        } catch (RemoteException e) {
            log.error("Resource getting failed due to RemoteException : " + e.getMessage());
            throw new RemoteException("Resource getting failed due to RemoteException :" +
                                      e.getMessage());
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Resource getting failed due to ResourceAdminServiceExceptionException : " +
                      e.getMessage());
            throw new ResourceAdminServiceExceptionException(
                    "Resource getting failed due to ResourceAdminServiceExceptionException:" +
                    e.getMessage());
        }

        return collectionContentBean;
    }

    public boolean deleteResource(String sessionCookie, String destinationPath)
            throws ResourceAdminServiceExceptionException, RemoteException {

        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        log.debug("Destination Path :" + destinationPath);
        return resourceAdminServiceStub.delete(destinationPath);

    }

    public void addWSDL(String sessionCookie, String description, DataHandler dh) {
        String fileName;
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        fileName = dh.getName().substring(dh.getName().lastIndexOf('/') + 1);
        log.debug(fileName);
        try {
            Assert.assertTrue("WSDL Adding failed", resourceAdminServiceStub.addResource("/" + fileName, MEDIA_TYPE_WSDL, description, dh, null));
            log.info("WSDL Added");
        } catch (RemoteException e) {
            log.error("WSDL adding failed due to RemoteException : " + e.getMessage());
            Assert.fail("WSDL adding failed due to RemoteException : " + e.getMessage());
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("WSDL adding failed due to ResourceAdminServiceExceptionException : " + e.getMessage());
            Assert.fail("WSDL adding failed due to ResourceAdminServiceExceptionException : " + e.getMessage());
        }

    }

    public void addWSDL(String sessionCookie, String resourceName, String description,
                        String fetchURL) {

        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        log.debug("fetchUrl :" + fetchURL);
        try {
            Assert.assertTrue("Importing WSDL from URL failed", resourceAdminServiceStub.importResource("/", resourceName, MEDIA_TYPE_WSDL, description, fetchURL, null));
            log.info("WSDL imported");
        } catch (RemoteException e) {
            log.error("Importing WSDL from URL failed due to RemoteException : " + e.getMessage());
            Assert.fail("Importing WSDL from URL failed due to RemoteException : " + e.getMessage());
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Importing WSDL from URL failed due to ResourceAdminServiceExceptionException : " + e.getMessage());
            Assert.fail("Importing WSDL from URL failed due to ResourceAdminServiceExceptionException : " + e.getMessage());
        }

    }

    public void addSchema(String sessionCookie, String description, DataHandler dh) {
        String fileName;
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        fileName = dh.getName().substring(dh.getName().lastIndexOf('/') + 1);
        log.debug("File Name: " + fileName);
        try {
            Assert.assertTrue("Schema Adding failed", resourceAdminServiceStub.addResource("/" + fileName, MEDIA_TYPE_SCHEMA, description, dh, null));
            log.info("Schema Added");
        } catch (RemoteException e) {
            log.error("Schema adding failed due to RemoteException : " + e.getMessage());
            Assert.fail("Schema adding failed due to RemoteException : " + e.getMessage());
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Schema adding failed due to ResourceAdminServiceExceptionException : " + e.getMessage());
            Assert.fail("Schema adding failed due to ResourceAdminServiceExceptionException : " + e.getMessage());
        }


    }

    public void addSchema(String sessionCookie, String resourceName, String description,
                          String fetchURL) {

        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        log.debug("fetchURL :" + fetchURL);
        try {
            Assert.assertTrue("Importing Schema from URL failed", resourceAdminServiceStub.importResource("/", resourceName, MEDIA_TYPE_SCHEMA, description, fetchURL, null));
            log.info("Schema Imported");
        } catch (RemoteException e) {
            log.error("Importing Schema from URL failed due to RemoteException : " + e.getMessage());
            Assert.fail("Importing Schema from URL failed due to RemoteException : " + e.getMessage());
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Importing Schema from URL failed due to ResourceAdminServiceExceptionException : " + e.getMessage());
            Assert.fail("Importing Schema from URL failed due to ResourceAdminServiceExceptionException : " + e.getMessage());
        }

    }

    public void addPolicy(String sessionCookie, String description, DataHandler dh) {
        String fileName;
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        fileName = dh.getName().substring(dh.getName().lastIndexOf('/') + 1);
        log.debug("file name :" + fileName);
        try {
            Assert.assertTrue("Adding Policy failed", resourceAdminServiceStub.addResource("/" + fileName, MEDIA_TYPE_POLICY, description, dh, null));
            log.info("Policy Added");
        } catch (RemoteException e) {
            log.error("Policy adding failed due to RemoteException : " + e.getMessage());
            Assert.fail("Policy adding failed due to RemoteException : " + e.getMessage());
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Policy adding failed due to ResourceAdminServiceExceptionException : " + e.getMessage());
            Assert.fail("Policy adding failed due to ResourceAdminServiceExceptionException : " + e.getMessage());
        }

    }

    public void addPolicy(String sessionCookie, String resourceName, String description,
                          String fetchURL) {

        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        log.debug("fetchURL :" + fetchURL);
        try {
            Assert.assertTrue("Importing Policy failed", resourceAdminServiceStub.importResource("/", resourceName, MEDIA_TYPE_POLICY, description, fetchURL, null));
            log.info("Policy imported");
        } catch (RemoteException e) {
            log.error("Importing Policy from URL failed due to ExceptionException : " + e.getMessage());
            Assert.fail("Importing Policy from URL failed due to ExceptionException : " + e.getMessage());
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Importing Policy from URL failed due to ResourceAdminServiceExceptionException : " + e.getMessage());
            Assert.fail("Importing Policy from URL failed due to ResourceAdminServiceExceptionException : " + e.getMessage());
        }


    }

    public void uploadArtifact(String sessionCookie, String description, DataHandler dh) {
        String fileName;
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        fileName = dh.getName().substring(dh.getName().lastIndexOf('/') + 1);
        log.debug("file name :" + fileName);
        try {
            Assert.assertTrue("uploading vnd.wso2.governance-archive failed", resourceAdminServiceStub.addResource("/" + fileName, MEDIA_TYPE_GOVERNANCE_ARCHIVE, description, dh, null));
            log.info("Artifact Uploaded");
        } catch (RemoteException e) {
            log.error("Uploading artifact vnd.wso2.governance-archive failed due to RemoteException : " + e.getMessage());
            Assert.fail("Uploading artifact vnd.wso2.governance-archive failed due to RemoteException : " + e.getMessage());
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Uploading artifact vnd.wso2.governance-archive failed due to ResourceAdminServiceExceptionException : " + e.getMessage());
            Assert.fail("Uploading artifact vnd.wso2.governance-archive failed due to ResourceAdminServiceExceptionException : " + e.getMessage());
        }

    }

    public void addCollection(String sessionCookie, String parentPath, String collectionName,
                              String mediaType, String description) {
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        log.debug("collectionName :" + collectionName);
        log.debug("parentPath :" + parentPath);
        try {
            resourceAdminServiceStub.addCollection(parentPath, collectionName, mediaType, description);
            log.info("Collection Added");
        } catch (RemoteException e) {
            log.error("Collection adding failed due to RemoteException : " + e.getMessage());
            Assert.fail("Collection adding failed due to RemoteException : " + e.getMessage());
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Collection adding failed due to ResourceAdminServiceExceptionException : " + e.getMessage());
            Assert.fail("Collection adding failed due to ResourceAdminServiceExceptionException : " + e.getMessage());
        }

    }


    public void addSymbolink(String sessionCookie, String parentPath, String name,
                             String targetPath)
            throws RemoteException, ResourceAdminServiceExceptionException {
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        log.debug("symbolink name " + name);

        try {
            resourceAdminServiceStub.addSymbolicLink(parentPath, name, targetPath);
            log.info("Symbolink added :");
        } catch (RemoteException e) {
            log.error("Symbolink adding failed due to RemoteException : " + e.getMessage());
            throw new RemoteException("Symbolink adding failed due to RemoteException:" +
                                      e.getMessage());
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Symbolink adding failed due to ResourceAdminServiceExceptionException : " +
                      e.getMessage());
            throw new ResourceAdminServiceExceptionException(
                    "Symbolink adding failed due to ResourceAdminServiceExceptionException:" +
                    e.getMessage());

        }
    }

    public void addTextResource(String sessionCookie, String parentPath, String fileName,
                                String mediaType, String description, String content)
            throws RemoteException, ResourceAdminServiceExceptionException {

        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        log.debug("text resource name : " + fileName);

        try {
            resourceAdminServiceStub.addTextResource(parentPath, fileName, mediaType, description, content);
            log.info("Text resource added :");
        } catch (RemoteException e) {
            log.error("Text Resource adding failed due to RemoteException : " + e.getMessage());
            throw new RemoteException("Text Resource adding failed due to RemoteException:" +
                                      e.getMessage());
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Text Resource adding failed due to ResourceAdminServiceExceptionException : " +
                      e.getMessage());
            throw new ResourceAdminServiceExceptionException(
                    "Text Resource adding failed due to ResourceAdminServiceExceptionException:" +
                    e.getMessage());
        }

    }

    public void addResourcePermission(String sessionCookie, String pathToAuthorize,
                                      String roleToAuthorize,
                                      String actionToAuthorize, String permissionType)
            throws AxisFault {
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        try {
            resourceAdminServiceStub.addRolePermission(pathToAuthorize, roleToAuthorize, actionToAuthorize, permissionType);
        } catch (RemoteException e) {
            handleException("Fail to add registry permission", e);
        } catch (ResourceAdminServiceResourceServiceExceptionException e) {
            handleException("Fail to add registry permission", e);
        }
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
}
