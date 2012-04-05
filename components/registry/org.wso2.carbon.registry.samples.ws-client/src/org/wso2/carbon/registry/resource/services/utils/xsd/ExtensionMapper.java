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
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5-wso2v1  Built on : May 20, 2009 (09:53:27 IST)
 */

            package org.wso2.carbon.registry.resource.services.utils.xsd;
            /**
            *  ExtensionMapper class
            */
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://beans.resource.registry.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "PermissionEntry".equals(typeName)){
                   
                            return  org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://beans.resource.registry.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "VersionsBean".equals(typeName)){
                   
                            return  org.wso2.carbon.registry.resource.beans.xsd.VersionsBean.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://common.registry.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "TagCount".equals(typeName)){
                   
                            return  org.wso2.carbon.registry.common.xsd.TagCount.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://common.registry.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "ResourceData".equals(typeName)){
                   
                            return  org.wso2.carbon.registry.common.xsd.ResourceData.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://beans.resource.registry.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "ContentDownloadBean".equals(typeName)){
                   
                            return  org.wso2.carbon.registry.resource.beans.xsd.ContentDownloadBean.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://beans.resource.registry.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "ContentBean".equals(typeName)){
                   
                            return  org.wso2.carbon.registry.resource.beans.xsd.ContentBean.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://beans.resource.registry.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "MetadataBean".equals(typeName)){
                   
                            return  org.wso2.carbon.registry.resource.beans.xsd.MetadataBean.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://beans.resource.registry.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "ResourceTreeEntryBean".equals(typeName)){
                   
                            return  org.wso2.carbon.registry.resource.beans.xsd.ResourceTreeEntryBean.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://beans.resource.registry.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "VersionPath".equals(typeName)){
                   
                            return  org.wso2.carbon.registry.resource.beans.xsd.VersionPath.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://beans.resource.registry.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "PermissionBean".equals(typeName)){
                   
                            return  org.wso2.carbon.registry.resource.beans.xsd.PermissionBean.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://services.resource.registry.carbon.wso2.org".equals(namespaceURI) &&
                  "Exception".equals(typeName)){
                   
                            return  org.wso2.carbon.registry.resource.services.Exception.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://common.registry.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "WebResourcePath".equals(typeName)){
                   
                            return  org.wso2.carbon.registry.common.xsd.WebResourcePath.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://beans.resource.registry.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "CollectionContentBean".equals(typeName)){
                   
                            return  org.wso2.carbon.registry.resource.beans.xsd.CollectionContentBean.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://utils.services.resource.registry.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "ResourceServiceException".equals(typeName)){
                   
                            return  org.wso2.carbon.registry.resource.services.utils.xsd.ResourceServiceException.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    