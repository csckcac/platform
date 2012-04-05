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


/**
 * ResourceAdminService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5-wso2v1  Built on : May 20, 2009 (09:53:23 IST)
 */

    package org.wso2.carbon.registry.resource.services;

    /*
     *  ResourceAdminService java interface
     */

    public interface ResourceAdminService {
          

        /**
          * Auto generated method signature
          * 
             * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */

         
                     public java.lang.String getMediatypeDefinitions(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
          */
        public void startgetMediatypeDefinitions(

            

            final org.wso2.carbon.registry.resource.services.ResourceAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getResourceTreeEntry65
                
             * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */

         
                     public org.wso2.carbon.registry.resource.beans.xsd.ResourceTreeEntryBean getResourceTreeEntry(

                        java.lang.String resourcePath66)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getResourceTreeEntry65
            
          */
        public void startgetResourceTreeEntry(

            java.lang.String resourcePath66,

            final org.wso2.carbon.registry.resource.services.ResourceAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */
        public void  createVersion(
         java.lang.String resourcePath70

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */
        public void  addRemoteLink(
         java.lang.String parentPath72,java.lang.String name73,java.lang.String instance74,java.lang.String targetPath75

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.registry.resource.services.ExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param changeUserPermissions76
                
             * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */

         
                     public boolean changeUserPermissions(

                        java.lang.String resourcePath77,java.lang.String permissionInput78)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param changeUserPermissions76
            
          */
        public void startchangeUserPermissions(

            java.lang.String resourcePath77,java.lang.String permissionInput78,

            final org.wso2.carbon.registry.resource.services.ResourceAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */
        public void  delete(
         java.lang.String pathToDelete82

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */
        public void  updateTextContent(
         java.lang.String resourcePath84,java.lang.String contentText85

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */
        public void  renameResource(
         java.lang.String parentPath87,java.lang.String oldResourcePath88,java.lang.String newResourceName89

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */
        public void  moveResource(
         java.lang.String parentPath91,java.lang.String oldResourcePath92,java.lang.String destinationPath93,java.lang.String resourceName94

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.registry.resource.services.ExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getResourceData95
                
             * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */

         
                     public org.wso2.carbon.registry.common.xsd.ResourceData[] getResourceData(

                        java.lang.String[] paths96)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getResourceData95
            
          */
        public void startgetResourceData(

            java.lang.String[] paths96,

            final org.wso2.carbon.registry.resource.services.ResourceAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */
        public void  importResource(
         java.lang.String parentPath100,java.lang.String resourceName101,java.lang.String mediaType102,java.lang.String description103,java.lang.String fetchURL104,java.lang.String symlinkLocation105

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.registry.resource.services.ExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param addUserPermission106
                
             * @throws org.wso2.carbon.registry.resource.services.ResourceServiceExceptionException : 
         */

         
                     public boolean addUserPermission(

                        java.lang.String pathToAuthorize107,java.lang.String userToAuthorize108,java.lang.String actionToAuthorize109,java.lang.String permissionType110)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.registry.resource.services.ResourceServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param addUserPermission106
            
          */
        public void startaddUserPermission(

            java.lang.String pathToAuthorize107,java.lang.String userToAuthorize108,java.lang.String actionToAuthorize109,java.lang.String permissionType110,

            final org.wso2.carbon.registry.resource.services.ResourceAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */
        public void  restoreVersion(
         java.lang.String versionPath114

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.registry.resource.services.ExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param addRolePermission115
                
             * @throws org.wso2.carbon.registry.resource.services.ResourceServiceExceptionException : 
         */

         
                     public boolean addRolePermission(

                        java.lang.String pathToAuthorize116,java.lang.String roleToAuthorize117,java.lang.String actionToAuthorize118,java.lang.String permissionType119)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.registry.resource.services.ResourceServiceExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param addRolePermission115
            
          */
        public void startaddRolePermission(

            java.lang.String pathToAuthorize116,java.lang.String roleToAuthorize117,java.lang.String actionToAuthorize118,java.lang.String permissionType119,

            final org.wso2.carbon.registry.resource.services.ResourceAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
             * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */

         
                     public java.lang.String getSessionResourcePath(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
          */
        public void startgetSessionResourcePath(

            

            final org.wso2.carbon.registry.resource.services.ResourceAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */
        public void  addSymbolicLink(
         java.lang.String parentPath126,java.lang.String name127,java.lang.String targetPath128

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.registry.resource.services.ExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getTextContent129
                
             * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */

         
                     public java.lang.String getTextContent(

                        java.lang.String path130)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getTextContent129
            
          */
        public void startgetTextContent(

            java.lang.String path130,

            final org.wso2.carbon.registry.resource.services.ResourceAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
             * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */

         
                     public java.lang.String getCollectionMediatypeDefinitions(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
          */
        public void startgetCollectionMediatypeDefinitions(

            

            final org.wso2.carbon.registry.resource.services.ResourceAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getCollectionContent136
                
             * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */

         
                     public org.wso2.carbon.registry.resource.beans.xsd.CollectionContentBean getCollectionContent(

                        java.lang.String path137)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getCollectionContent136
            
          */
        public void startgetCollectionContent(

            java.lang.String path137,

            final org.wso2.carbon.registry.resource.services.ResourceAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */
        public void  setSessionResourcePath(
         java.lang.String resourcePath141

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.registry.resource.services.ExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param changeRolePermissions142
                
             * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */

         
                     public boolean changeRolePermissions(

                        java.lang.String resourcePath143,java.lang.String permissionsInput144)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param changeRolePermissions142
            
          */
        public void startchangeRolePermissions(

            java.lang.String resourcePath143,java.lang.String permissionsInput144,

            final org.wso2.carbon.registry.resource.services.ResourceAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getVersionsBean147
                
             * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */

         
                     public org.wso2.carbon.registry.resource.beans.xsd.VersionsBean getVersionsBean(

                        java.lang.String path148)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getVersionsBean147
            
          */
        public void startgetVersionsBean(

            java.lang.String path148,

            final org.wso2.carbon.registry.resource.services.ResourceAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */
        public void  setDescription(
         java.lang.String path152,java.lang.String description153

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */
        public void  addTextResource(
         java.lang.String parentPath155,java.lang.String fileName156,java.lang.String mediaType157,java.lang.String description158,java.lang.String content159

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.registry.resource.services.ExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getProperty160
                
             * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */

         
                     public java.lang.String getProperty(

                        java.lang.String resourcePath161,java.lang.String key162)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getProperty160
            
          */
        public void startgetProperty(

            java.lang.String resourcePath161,java.lang.String key162,

            final org.wso2.carbon.registry.resource.services.ResourceAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getContentDownloadBean165
                
             * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */

         
                     public org.wso2.carbon.registry.resource.beans.xsd.ContentDownloadBean getContentDownloadBean(

                        java.lang.String path166)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getContentDownloadBean165
            
          */
        public void startgetContentDownloadBean(

            java.lang.String path166,

            final org.wso2.carbon.registry.resource.services.ResourceAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getContentBean169
                
             * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */

         
                     public org.wso2.carbon.registry.resource.beans.xsd.ContentBean getContentBean(

                        java.lang.String path170)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getContentBean169
            
          */
        public void startgetContentBean(

            java.lang.String path170,

            final org.wso2.carbon.registry.resource.services.ResourceAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param addCollection173
                
             * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */

         
                     public java.lang.String addCollection(

                        java.lang.String parentPath174,java.lang.String collectionName175,java.lang.String mediaType176,java.lang.String description177)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param addCollection173
            
          */
        public void startaddCollection(

            java.lang.String parentPath174,java.lang.String collectionName175,java.lang.String mediaType176,java.lang.String description177,

            final org.wso2.carbon.registry.resource.services.ResourceAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getMetadata180
                
             * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */

         
                     public org.wso2.carbon.registry.resource.beans.xsd.MetadataBean getMetadata(

                        java.lang.String path181)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getMetadata180
            
          */
        public void startgetMetadata(

            java.lang.String path181,

            final org.wso2.carbon.registry.resource.services.ResourceAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getPermissions184
                
             * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */

         
                     public org.wso2.carbon.registry.resource.beans.xsd.PermissionBean getPermissions(

                        java.lang.String path185)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getPermissions184
            
          */
        public void startgetPermissions(

            java.lang.String path185,

            final org.wso2.carbon.registry.resource.services.ResourceAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */
        public void  copyResource(
         java.lang.String parentPath189,java.lang.String oldResourcePath190,java.lang.String destinationPath191,java.lang.String resourceName192

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */
        public void  addResource(
         java.lang.String path194,java.lang.String mediaType195,java.lang.String description196,javax.activation.DataHandler content197,java.lang.String symlinkLocation198

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.registry.resource.services.ExceptionException;

        

        /**
          * Auto generated method signature
          * 
             * @throws org.wso2.carbon.registry.resource.services.ExceptionException : 
         */

         
                     public java.lang.String getCustomUIMediatypeDefinitions(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.registry.resource.services.ExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
          */
        public void startgetCustomUIMediatypeDefinitions(

            

            final org.wso2.carbon.registry.resource.services.ResourceAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    