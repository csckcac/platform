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
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceResourceServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.beans.xsd.CollectionContentBean;
import org.wso2.carbon.registry.resource.stub.beans.xsd.ContentBean;
import org.wso2.carbon.registry.resource.stub.beans.xsd.MetadataBean;
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
        resourceAdminServiceStub = new ResourceAdminServiceStub(endPoint);
    }

    public boolean addResource(String sessionCookie, String destinationPath, String mediaType,
                               String description, DataHandler dh)
            throws ResourceAdminServiceExceptionException, RemoteException {

        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        if (log.isDebugEnabled()) {
            log.debug("Destination Path :" + destinationPath);
            log.debug("Media Type :" + mediaType);
        }

        return resourceAdminServiceStub.addResource(destinationPath, mediaType, description, dh, null);

    }

    public ResourceData[] getResource(String sessionCookie, String destinationPath)
            throws ResourceAdminServiceExceptionException, RemoteException {
        ResourceData[] rs;
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);

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
            log.error("Resource getting failed due to RemoteException : " + e);
            throw new RemoteException("Resource getting failed due to RemoteException :" +
                                      e);
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Resource getting failed due to ResourceAdminServiceExceptionException : " +
                      e);
            throw new ResourceAdminServiceExceptionException(
                    "Resource getting failed due to ResourceAdminServiceExceptionException:" +
                    e);
        }

        return collectionContentBean;
    }

    public boolean deleteResource(String sessionCookie, String destinationPath)
            throws ResourceAdminServiceExceptionException, RemoteException {

        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
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
            log.error("WSDL adding failed due to RemoteException : " + e);
            Assert.fail("WSDL adding failed due to RemoteException : " + e);
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("WSDL adding failed due to ResourceAdminServiceExceptionException : " + e);
            Assert.fail("WSDL adding failed due to ResourceAdminServiceExceptionException : " + e);
        }

    }

    public void addWSDL(String sessionCookie, String resourceName, String description,
                        String fetchURL) {

        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        try {
            Assert.assertTrue("Importing WSDL from URL failed", resourceAdminServiceStub.importResource("/", resourceName, MEDIA_TYPE_WSDL, description, fetchURL, null));
            log.info("WSDL imported");
        } catch (RemoteException e) {
            log.error("Importing WSDL from URL failed due to RemoteException : " + e);
            Assert.fail("Importing WSDL from URL failed due to RemoteException : " + e);
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Importing WSDL from URL failed due to ResourceAdminServiceExceptionException : " + e);
            Assert.fail("Importing WSDL from URL failed due to ResourceAdminServiceExceptionException : " + e);
        }

    }

    public void addSchema(String sessionCookie, String description, DataHandler dh) {
        String fileName;
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        fileName = dh.getName().substring(dh.getName().lastIndexOf('/') + 1);
        try {
            Assert.assertTrue("Schema Adding failed", resourceAdminServiceStub.addResource("/" + fileName, MEDIA_TYPE_SCHEMA, description, dh, null));
            log.info("Schema Added");
        } catch (RemoteException e) {
            log.error("Schema adding failed due to RemoteException : " + e);
            Assert.fail("Schema adding failed due to RemoteException : " + e);
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Schema adding failed due to ResourceAdminServiceExceptionException : " + e);
            Assert.fail("Schema adding failed due to ResourceAdminServiceExceptionException : " + e);
        }


    }

    public void addSchema(String sessionCookie, String resourceName, String description,
                          String fetchURL) {

        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        try {
            Assert.assertTrue("Importing Schema from URL failed", resourceAdminServiceStub.importResource("/", resourceName, MEDIA_TYPE_SCHEMA, description, fetchURL, null));
            log.info("Schema Imported");
        } catch (RemoteException e) {
            log.error("Importing Schema from URL failed due to RemoteException : " + e);
            Assert.fail("Importing Schema from URL failed due to RemoteException : " + e);
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Importing Schema from URL failed due to ResourceAdminServiceExceptionException : " + e);
            Assert.fail("Importing Schema from URL failed due to ResourceAdminServiceExceptionException : " + e);
        }

    }

    public void addPolicy(String sessionCookie, String description, DataHandler dh) {
        String fileName;
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        fileName = dh.getName().substring(dh.getName().lastIndexOf('/') + 1);
        try {
            Assert.assertTrue("Adding Policy failed", resourceAdminServiceStub.addResource("/" + fileName, MEDIA_TYPE_POLICY, description, dh, null));
            log.info("Policy Added");
        } catch (RemoteException e) {
            log.error("Policy adding failed due to RemoteException : " + e);
            Assert.fail("Policy adding failed due to RemoteException : " + e);
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Policy adding failed due to ResourceAdminServiceExceptionException : " + e);
            Assert.fail("Policy adding failed due to ResourceAdminServiceExceptionException : " + e);
        }

    }

    public void addPolicy(String sessionCookie, String resourceName, String description,
                          String fetchURL) {

        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        try {
            Assert.assertTrue("Importing Policy failed", resourceAdminServiceStub.importResource("/", resourceName, MEDIA_TYPE_POLICY, description, fetchURL, null));
            log.info("Policy imported");
        } catch (RemoteException e) {
            log.error("Importing Policy from URL failed due to ExceptionException : " + e);
            Assert.fail("Importing Policy from URL failed due to ExceptionException : " + e);
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Importing Policy from URL failed due to ResourceAdminServiceExceptionException : " + e);
            Assert.fail("Importing Policy from URL failed due to ResourceAdminServiceExceptionException : " + e);
        }


    }

    public void uploadArtifact(String sessionCookie, String description, DataHandler dh) {
        String fileName;
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        fileName = dh.getName().substring(dh.getName().lastIndexOf('/') + 1);
        try {
            Assert.assertTrue("uploading vnd.wso2.governance-archive failed", resourceAdminServiceStub.addResource("/" + fileName, MEDIA_TYPE_GOVERNANCE_ARCHIVE, description, dh, null));
            log.info("Artifact Uploaded");
        } catch (RemoteException e) {
            log.error("Uploading artifact vnd.wso2.governance-archive failed due to RemoteException : " + e);
            Assert.fail("Uploading artifact vnd.wso2.governance-archive failed due to RemoteException : " + e);
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Uploading artifact vnd.wso2.governance-archive failed due to ResourceAdminServiceExceptionException : " + e);
            Assert.fail("Uploading artifact vnd.wso2.governance-archive failed due to ResourceAdminServiceExceptionException : " + e);
        }

    }

    public void addCollection(String sessionCookie, String parentPath, String collectionName,
                              String mediaType, String description) {
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        try {
            resourceAdminServiceStub.addCollection(parentPath, collectionName, mediaType, description);
            log.info("Collection Added");
        } catch (RemoteException e) {
            log.error("Collection adding failed due to RemoteException : " + e);
            Assert.fail("Collection adding failed due to RemoteException : " + e);
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Collection adding failed due to ResourceAdminServiceExceptionException : " + e);
            Assert.fail("Collection adding failed due to ResourceAdminServiceExceptionException : " + e);
        }

    }


    public void addSymbolink(String sessionCookie, String parentPath, String name,
                             String targetPath)
            throws RemoteException, ResourceAdminServiceExceptionException {
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        try {
            resourceAdminServiceStub.addSymbolicLink(parentPath, name, targetPath);
            log.info("Symbolink added :");
        } catch (RemoteException e) {
            log.error("Symbolink adding failed due to RemoteException : " + e);
            throw new RemoteException("Symbolink adding failed due to RemoteException:" +
                                      e);
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Symbolink adding failed due to ResourceAdminServiceExceptionException : " +
                      e);
            throw new ResourceAdminServiceExceptionException(
                    "Symbolink adding failed due to ResourceAdminServiceExceptionException:" +
                    e);

        }
    }

    public void addTextResource(String sessionCookie, String parentPath, String fileName,
                                String mediaType, String description, String content)
            throws RemoteException, ResourceAdminServiceExceptionException {

        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        try {
            resourceAdminServiceStub.addTextResource(parentPath, fileName, mediaType, description, content);
            log.info("Text resource added :");
        } catch (RemoteException e) {
            log.error("Text Resource adding failed due to RemoteException : " + e);
            throw new RemoteException("Text Resource adding failed due to RemoteException:" +
                                      e);
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Text Resource adding failed due to ResourceAdminServiceExceptionException : " +
                      e);
            throw new ResourceAdminServiceExceptionException(
                    "Text Resource adding failed due to ResourceAdminServiceExceptionException:" +
                    e);
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

    public String getProperty(String sessionCookie, String resourcePath, String key)
            throws AxisFault {
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        try {
            return resourceAdminServiceStub.getProperty(resourcePath, key);
        } catch (RemoteException e) {
            handleException("Fail to get resource property", e);
        } catch (ResourceAdminServiceExceptionException e) {
            handleException("Fail to get resource property", e);
        }
        return null;
    }

    public MetadataBean getMetadata(String sessionCookie, String resourcePath)
            throws AxisFault {
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        try {
            return resourceAdminServiceStub.getMetadata(resourcePath);

        } catch (RemoteException e) {
            handleException("Fail to get resource property", e);
        } catch (ResourceAdminServiceExceptionException e) {
            handleException("Fail to get resource property", e);
        }
        return null;
    }

    public ContentBean getResourceContent(String sessionCookie, String resourcePath)
            throws AxisFault {
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        try {
            return resourceAdminServiceStub.getContentBean(resourcePath);


        } catch (RemoteException e) {
            handleException("Fail to get resource property", e);
        } catch (ResourceAdminServiceExceptionException e) {
            handleException("Fail to get resource property", e);
        }
        return null;
    }

    public ResourceData[] getResourceData(String sessionCookie, String resourcePath)
            throws AxisFault {
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        String[] resourceArray = {resourcePath};
        try {
            return resourceAdminServiceStub.getResourceData(resourceArray);
        } catch (RemoteException e) {
            handleException("Fail to get resource property", e);
        } catch (ResourceAdminServiceExceptionException e) {
            handleException("Fail to get resource property", e);
        }
        return null;
    }

    public String getHumanReadableMediaTypes(String sessionCookie, String resourcePath)
            throws AxisFault {
        AuthenticateStub.authenticateStub(sessionCookie, resourceAdminServiceStub);
        try {
            resourceAdminServiceStub.getHumanReadableMediaTypes();
        } catch (RemoteException e) {
            handleException("Fail to get resource property", e);
        } catch (ResourceAdminServiceExceptionException e) {
            handleException("Fail to get resource property", e);
        }
        return null;
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
}
