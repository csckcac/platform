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
 * AuthenticationAdminService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5-wso2v1  Built on : May 20, 2009 (09:53:23 IST)
 */

    package org.wso2.carbon.core.services.authentication;

    /*
     *  AuthenticationAdminService java interface
     */

    public interface AuthenticationAdminService {
          
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.core.services.authentication.AuthenticationExceptionException : 
         */
        public void  logout(
         

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.core.services.authentication.AuthenticationExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param login8
                
             * @throws org.wso2.carbon.core.services.authentication.AuthenticationExceptionException : 
         */

         
                     public boolean login(

                        java.lang.String username9,java.lang.String password10,java.lang.String remoteAddress11)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.core.services.authentication.AuthenticationExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param login8
            
          */
        public void startlogin(

            java.lang.String username9,java.lang.String password10,java.lang.String remoteAddress11,

            final org.wso2.carbon.core.services.authentication.AuthenticationAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getSystemPermissionOfUser14
                
             * @throws org.wso2.carbon.core.services.authentication.ExceptionException : 
         */

         
                     public java.lang.String[] getSystemPermissionOfUser(

                        java.lang.String username15)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.core.services.authentication.ExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getSystemPermissionOfUser14
            
          */
        public void startgetSystemPermissionOfUser(

            java.lang.String username15,

            final org.wso2.carbon.core.services.authentication.AuthenticationAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param loginWithDelegation18
                
             * @throws org.wso2.carbon.core.services.authentication.AuthenticationExceptionException : 
         */

         
                     public boolean loginWithDelegation(

                        java.lang.String username19,java.lang.String password20,java.lang.String authenticatedUser21,java.lang.String remoteAddress22)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.core.services.authentication.AuthenticationExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param loginWithDelegation18
            
          */
        public void startloginWithDelegation(

            java.lang.String username19,java.lang.String password20,java.lang.String authenticatedUser21,java.lang.String remoteAddress22,

            final org.wso2.carbon.core.services.authentication.AuthenticationAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    