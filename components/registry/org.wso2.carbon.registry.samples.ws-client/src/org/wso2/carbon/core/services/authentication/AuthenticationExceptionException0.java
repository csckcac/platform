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
 * AuthenticationExceptionException0.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1-wso2v2  Built on : Feb 17, 2009 (11:16:43 PST)
 */

package org.wso2.carbon.core.services.authentication;

public class AuthenticationExceptionException0 extends java.lang.Exception{
    
    private org.wso2.carbon.core.services.authentication.AuthenticationExceptionE faultMessage;
    
    public AuthenticationExceptionException0() {
        super("AuthenticationExceptionException0");
    }
           
    public AuthenticationExceptionException0(java.lang.String s) {
       super(s);
    }
    
    public AuthenticationExceptionException0(java.lang.String s, java.lang.Throwable ex) {
      super(s, ex);
    }
    
    public void setFaultMessage(org.wso2.carbon.core.services.authentication.AuthenticationExceptionE msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.core.services.authentication.AuthenticationExceptionE getFaultMessage(){
       return faultMessage;
    }
}
    