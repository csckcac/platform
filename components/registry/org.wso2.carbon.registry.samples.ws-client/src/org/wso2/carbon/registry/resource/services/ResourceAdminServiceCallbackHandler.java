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
 * ResourceAdminServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5-wso2v1  Built on : May 20, 2009 (09:53:23 IST)
 */

    package org.wso2.carbon.registry.resource.services;

    /**
     *  ResourceAdminServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class ResourceAdminServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public ResourceAdminServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public ResourceAdminServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for getMediatypeDefinitions method
            * override this method for handling normal response from getMediatypeDefinitions operation
            */
           public void receiveResultgetMediatypeDefinitions(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getMediatypeDefinitions operation
           */
            public void receiveErrorgetMediatypeDefinitions(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getResourceTreeEntry method
            * override this method for handling normal response from getResourceTreeEntry operation
            */
           public void receiveResultgetResourceTreeEntry(
                    org.wso2.carbon.registry.resource.beans.xsd.ResourceTreeEntryBean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getResourceTreeEntry operation
           */
            public void receiveErrorgetResourceTreeEntry(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for changeUserPermissions method
            * override this method for handling normal response from changeUserPermissions operation
            */
           public void receiveResultchangeUserPermissions(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from changeUserPermissions operation
           */
            public void receiveErrorchangeUserPermissions(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getResourceData method
            * override this method for handling normal response from getResourceData operation
            */
           public void receiveResultgetResourceData(
                    org.wso2.carbon.registry.common.xsd.ResourceData[] result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getResourceData operation
           */
            public void receiveErrorgetResourceData(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for addUserPermission method
            * override this method for handling normal response from addUserPermission operation
            */
           public void receiveResultaddUserPermission(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addUserPermission operation
           */
            public void receiveErroraddUserPermission(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for addRolePermission method
            * override this method for handling normal response from addRolePermission operation
            */
           public void receiveResultaddRolePermission(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addRolePermission operation
           */
            public void receiveErroraddRolePermission(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getSessionResourcePath method
            * override this method for handling normal response from getSessionResourcePath operation
            */
           public void receiveResultgetSessionResourcePath(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getSessionResourcePath operation
           */
            public void receiveErrorgetSessionResourcePath(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getTextContent method
            * override this method for handling normal response from getTextContent operation
            */
           public void receiveResultgetTextContent(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getTextContent operation
           */
            public void receiveErrorgetTextContent(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getCollectionMediatypeDefinitions method
            * override this method for handling normal response from getCollectionMediatypeDefinitions operation
            */
           public void receiveResultgetCollectionMediatypeDefinitions(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getCollectionMediatypeDefinitions operation
           */
            public void receiveErrorgetCollectionMediatypeDefinitions(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getCollectionContent method
            * override this method for handling normal response from getCollectionContent operation
            */
           public void receiveResultgetCollectionContent(
                    org.wso2.carbon.registry.resource.beans.xsd.CollectionContentBean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getCollectionContent operation
           */
            public void receiveErrorgetCollectionContent(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for changeRolePermissions method
            * override this method for handling normal response from changeRolePermissions operation
            */
           public void receiveResultchangeRolePermissions(
                    boolean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from changeRolePermissions operation
           */
            public void receiveErrorchangeRolePermissions(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getVersionsBean method
            * override this method for handling normal response from getVersionsBean operation
            */
           public void receiveResultgetVersionsBean(
                    org.wso2.carbon.registry.resource.beans.xsd.VersionsBean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getVersionsBean operation
           */
            public void receiveErrorgetVersionsBean(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getProperty method
            * override this method for handling normal response from getProperty operation
            */
           public void receiveResultgetProperty(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getProperty operation
           */
            public void receiveErrorgetProperty(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getContentDownloadBean method
            * override this method for handling normal response from getContentDownloadBean operation
            */
           public void receiveResultgetContentDownloadBean(
                    org.wso2.carbon.registry.resource.beans.xsd.ContentDownloadBean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getContentDownloadBean operation
           */
            public void receiveErrorgetContentDownloadBean(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getContentBean method
            * override this method for handling normal response from getContentBean operation
            */
           public void receiveResultgetContentBean(
                    org.wso2.carbon.registry.resource.beans.xsd.ContentBean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getContentBean operation
           */
            public void receiveErrorgetContentBean(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for addCollection method
            * override this method for handling normal response from addCollection operation
            */
           public void receiveResultaddCollection(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from addCollection operation
           */
            public void receiveErroraddCollection(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getMetadata method
            * override this method for handling normal response from getMetadata operation
            */
           public void receiveResultgetMetadata(
                    org.wso2.carbon.registry.resource.beans.xsd.MetadataBean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getMetadata operation
           */
            public void receiveErrorgetMetadata(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getPermissions method
            * override this method for handling normal response from getPermissions operation
            */
           public void receiveResultgetPermissions(
                    org.wso2.carbon.registry.resource.beans.xsd.PermissionBean result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getPermissions operation
           */
            public void receiveErrorgetPermissions(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getCustomUIMediatypeDefinitions method
            * override this method for handling normal response from getCustomUIMediatypeDefinitions operation
            */
           public void receiveResultgetCustomUIMediatypeDefinitions(
                    java.lang.String result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getCustomUIMediatypeDefinitions operation
           */
            public void receiveErrorgetCustomUIMediatypeDefinitions(java.lang.Exception e) {
            }
                


    }
    