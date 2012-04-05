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
 * PermissionBean.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5-wso2v1  Built on : May 20, 2009 (09:53:27 IST)
 */
            
                package org.wso2.carbon.registry.resource.beans.xsd;
            

            /**
            *  PermissionBean bean class
            */
        
        public  class PermissionBean
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = PermissionBean
                Namespace URI = http://beans.resource.registry.carbon.wso2.org/xsd
                Namespace Prefix = ns5
                */
            

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://beans.resource.registry.carbon.wso2.org/xsd")){
                return "ns5";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        

                        /**
                        * field for AuthorizeAllowed
                        */

                        
                                    protected boolean localAuthorizeAllowed ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAuthorizeAllowedTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getAuthorizeAllowed(){
                               return localAuthorizeAllowed;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AuthorizeAllowed
                               */
                               public void setAuthorizeAllowed(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       
                                               if (false) {
                                           localAuthorizeAllowedTracker = false;
                                              
                                       } else {
                                          localAuthorizeAllowedTracker = true;
                                       }
                                   
                                            this.localAuthorizeAllowed=param;
                                    

                               }
                            

                        /**
                        * field for DeleteAllowed
                        */

                        
                                    protected boolean localDeleteAllowed ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDeleteAllowedTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getDeleteAllowed(){
                               return localDeleteAllowed;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DeleteAllowed
                               */
                               public void setDeleteAllowed(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       
                                               if (false) {
                                           localDeleteAllowedTracker = false;
                                              
                                       } else {
                                          localDeleteAllowedTracker = true;
                                       }
                                   
                                            this.localDeleteAllowed=param;
                                    

                               }
                            

                        /**
                        * field for PathWithVersion
                        */

                        
                                    protected java.lang.String localPathWithVersion ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPathWithVersionTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPathWithVersion(){
                               return localPathWithVersion;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PathWithVersion
                               */
                               public void setPathWithVersion(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localPathWithVersionTracker = true;
                                       } else {
                                          localPathWithVersionTracker = true;
                                              
                                       }
                                   
                                            this.localPathWithVersion=param;
                                    

                               }
                            

                        /**
                        * field for PutAllowed
                        */

                        
                                    protected boolean localPutAllowed ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPutAllowedTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getPutAllowed(){
                               return localPutAllowed;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PutAllowed
                               */
                               public void setPutAllowed(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       
                                               if (false) {
                                           localPutAllowedTracker = false;
                                              
                                       } else {
                                          localPutAllowedTracker = true;
                                       }
                                   
                                            this.localPutAllowed=param;
                                    

                               }
                            

                        /**
                        * field for RoleNames
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localRoleNames ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRoleNamesTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getRoleNames(){
                               return localRoleNames;
                           }

                           
                        


                               
                              /**
                               * validate the array for RoleNames
                               */
                              protected void validateRoleNames(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param RoleNames
                              */
                              public void setRoleNames(java.lang.String[] param){
                              
                                   validateRoleNames(param);

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localRoleNamesTracker = true;
                                          } else {
                                             localRoleNamesTracker = true;
                                                 
                                          }
                                      
                                      this.localRoleNames=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addRoleNames(java.lang.String param){
                                   if (localRoleNames == null){
                                   localRoleNames = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localRoleNamesTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localRoleNames);
                               list.add(param);
                               this.localRoleNames =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

                        /**
                        * field for RolePermissions
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry[] localRolePermissions ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRolePermissionsTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry[]
                           */
                           public  org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry[] getRolePermissions(){
                               return localRolePermissions;
                           }

                           
                        


                               
                              /**
                               * validate the array for RolePermissions
                               */
                              protected void validateRolePermissions(org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param RolePermissions
                              */
                              public void setRolePermissions(org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry[] param){
                              
                                   validateRolePermissions(param);

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localRolePermissionsTracker = true;
                                          } else {
                                             localRolePermissionsTracker = true;
                                                 
                                          }
                                      
                                      this.localRolePermissions=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry
                             */
                             public void addRolePermissions(org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry param){
                                   if (localRolePermissions == null){
                                   localRolePermissions = new org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry[]{};
                                   }

                            
                                 //update the setting tracker
                                localRolePermissionsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localRolePermissions);
                               list.add(param);
                               this.localRolePermissions =
                             (org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry[])list.toArray(
                            new org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry[list.size()]);

                             }
                             

                        /**
                        * field for UserNames
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localUserNames ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUserNamesTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getUserNames(){
                               return localUserNames;
                           }

                           
                        


                               
                              /**
                               * validate the array for UserNames
                               */
                              protected void validateUserNames(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param UserNames
                              */
                              public void setUserNames(java.lang.String[] param){
                              
                                   validateUserNames(param);

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localUserNamesTracker = true;
                                          } else {
                                             localUserNamesTracker = true;
                                                 
                                          }
                                      
                                      this.localUserNames=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addUserNames(java.lang.String param){
                                   if (localUserNames == null){
                                   localUserNames = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localUserNamesTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localUserNames);
                               list.add(param);
                               this.localUserNames =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

                        /**
                        * field for UserPermissions
                        * This was an Array!
                        */

                        
                                    protected org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry[] localUserPermissions ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUserPermissionsTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry[]
                           */
                           public  org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry[] getUserPermissions(){
                               return localUserPermissions;
                           }

                           
                        


                               
                              /**
                               * validate the array for UserPermissions
                               */
                              protected void validateUserPermissions(org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param UserPermissions
                              */
                              public void setUserPermissions(org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry[] param){
                              
                                   validateUserPermissions(param);

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localUserPermissionsTracker = true;
                                          } else {
                                             localUserPermissionsTracker = true;
                                                 
                                          }
                                      
                                      this.localUserPermissions=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry
                             */
                             public void addUserPermissions(org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry param){
                                   if (localUserPermissions == null){
                                   localUserPermissions = new org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry[]{};
                                   }

                            
                                 //update the setting tracker
                                localUserPermissionsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localUserPermissions);
                               list.add(param);
                               this.localUserPermissions =
                             (org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry[])list.toArray(
                            new org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry[list.size()]);

                             }
                             

                        /**
                        * field for VersionView
                        */

                        
                                    protected boolean localVersionView ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localVersionViewTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getVersionView(){
                               return localVersionView;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param VersionView
                               */
                               public void setVersionView(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       
                                               if (false) {
                                           localVersionViewTracker = false;
                                              
                                       } else {
                                          localVersionViewTracker = true;
                                       }
                                   
                                            this.localVersionView=param;
                                    

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement (
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{


        
               org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

                 public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                       PermissionBean.this.serialize(parentQName,factory,xmlWriter);
                 }
               };
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               parentQName,factory,dataSource);
            
       }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       final org.apache.axiom.om.OMFactory factory,
                                       org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,factory,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               final org.apache.axiom.om.OMFactory factory,
                               org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
            
                


                java.lang.String prefix = null;
                java.lang.String namespace = null;
                

                    prefix = parentQName.getPrefix();
                    namespace = parentQName.getNamespaceURI();

                    if ((namespace != null) && (namespace.trim().length() > 0)) {
                        java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                        if (writerPrefix != null) {
                            xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                        } else {
                            if (prefix == null) {
                                prefix = generatePrefix(namespace);
                            }

                            xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                            xmlWriter.writeNamespace(prefix, namespace);
                            xmlWriter.setPrefix(prefix, namespace);
                        }
                    } else {
                        xmlWriter.writeStartElement(parentQName.getLocalPart());
                    }
                
                  if (serializeType){
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://beans.resource.registry.carbon.wso2.org/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":PermissionBean",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "PermissionBean",
                           xmlWriter);
                   }

               
                   }
                if (localAuthorizeAllowedTracker){
                                    namespace = "http://beans.resource.registry.carbon.wso2.org/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"authorizeAllowed", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"authorizeAllowed");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("authorizeAllowed");
                                    }
                                
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("authorizeAllowed cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAuthorizeAllowed));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDeleteAllowedTracker){
                                    namespace = "http://beans.resource.registry.carbon.wso2.org/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"deleteAllowed", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"deleteAllowed");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("deleteAllowed");
                                    }
                                
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("deleteAllowed cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDeleteAllowed));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPathWithVersionTracker){
                                    namespace = "http://beans.resource.registry.carbon.wso2.org/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"pathWithVersion", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"pathWithVersion");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("pathWithVersion");
                                    }
                                

                                          if (localPathWithVersion==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPathWithVersion);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPutAllowedTracker){
                                    namespace = "http://beans.resource.registry.carbon.wso2.org/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"putAllowed", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"putAllowed");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("putAllowed");
                                    }
                                
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("putAllowed cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPutAllowed));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localRoleNamesTracker){
                             if (localRoleNames!=null) {
                                   namespace = "http://beans.resource.registry.carbon.wso2.org/xsd";
                                   boolean emptyNamespace = namespace == null || namespace.length() == 0;
                                   prefix =  emptyNamespace ? null : xmlWriter.getPrefix(namespace);
                                   for (int i = 0;i < localRoleNames.length;i++){
                                        
                                            if (localRoleNames[i] != null){
                                        
                                                if (!emptyNamespace) {
                                                    if (prefix == null) {
                                                        java.lang.String prefix2 = generatePrefix(namespace);

                                                        xmlWriter.writeStartElement(prefix2,"roleNames", namespace);
                                                        xmlWriter.writeNamespace(prefix2, namespace);
                                                        xmlWriter.setPrefix(prefix2, namespace);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace,"roleNames");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("roleNames");
                                                }

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRoleNames[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://beans.resource.registry.carbon.wso2.org/xsd";
                                                            if (! namespace.equals("")) {
                                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace);

                                                                if (prefix2 == null) {
                                                                    prefix2 = generatePrefix(namespace);

                                                                    xmlWriter.writeStartElement(prefix2,"roleNames", namespace);
                                                                    xmlWriter.writeNamespace(prefix2, namespace);
                                                                    xmlWriter.setPrefix(prefix2, namespace);

                                                                } else {
                                                                    xmlWriter.writeStartElement(namespace,"roleNames");
                                                                }

                                                            } else {
                                                                xmlWriter.writeStartElement("roleNames");
                                                            }
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                            java.lang.String namespace2 = "http://beans.resource.registry.carbon.wso2.org/xsd";
                                            if (! namespace2.equals("")) {
                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                if (prefix2 == null) {
                                                    prefix2 = generatePrefix(namespace2);

                                                    xmlWriter.writeStartElement(prefix2,"roleNames", namespace2);
                                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                                    xmlWriter.setPrefix(prefix2, namespace2);

                                                } else {
                                                    xmlWriter.writeStartElement(namespace2,"roleNames");
                                                }

                                            } else {
                                                xmlWriter.writeStartElement("roleNames");
                                            }

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localRolePermissionsTracker){
                                       if (localRolePermissions!=null){
                                            for (int i = 0;i < localRolePermissions.length;i++){
                                                if (localRolePermissions[i] != null){
                                                 localRolePermissions[i].serialize(new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd","rolePermissions"),
                                                           factory,xmlWriter);
                                                } else {
                                                   
                                                            // write null attribute
                                                            java.lang.String namespace2 = "http://beans.resource.registry.carbon.wso2.org/xsd";
                                                            if (! namespace2.equals("")) {
                                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                                if (prefix2 == null) {
                                                                    prefix2 = generatePrefix(namespace2);

                                                                    xmlWriter.writeStartElement(prefix2,"rolePermissions", namespace2);
                                                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                                                    xmlWriter.setPrefix(prefix2, namespace2);

                                                                } else {
                                                                    xmlWriter.writeStartElement(namespace2,"rolePermissions");
                                                                }

                                                            } else {
                                                                xmlWriter.writeStartElement("rolePermissions");
                                                            }

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                // write null attribute
                                                java.lang.String namespace2 = "http://beans.resource.registry.carbon.wso2.org/xsd";
                                                if (! namespace2.equals("")) {
                                                    java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                    if (prefix2 == null) {
                                                        prefix2 = generatePrefix(namespace2);

                                                        xmlWriter.writeStartElement(prefix2,"rolePermissions", namespace2);
                                                        xmlWriter.writeNamespace(prefix2, namespace2);
                                                        xmlWriter.setPrefix(prefix2, namespace2);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace2,"rolePermissions");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("rolePermissions");
                                                }

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localUserNamesTracker){
                             if (localUserNames!=null) {
                                   namespace = "http://beans.resource.registry.carbon.wso2.org/xsd";
                                   boolean emptyNamespace = namespace == null || namespace.length() == 0;
                                   prefix =  emptyNamespace ? null : xmlWriter.getPrefix(namespace);
                                   for (int i = 0;i < localUserNames.length;i++){
                                        
                                            if (localUserNames[i] != null){
                                        
                                                if (!emptyNamespace) {
                                                    if (prefix == null) {
                                                        java.lang.String prefix2 = generatePrefix(namespace);

                                                        xmlWriter.writeStartElement(prefix2,"userNames", namespace);
                                                        xmlWriter.writeNamespace(prefix2, namespace);
                                                        xmlWriter.setPrefix(prefix2, namespace);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace,"userNames");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("userNames");
                                                }

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUserNames[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://beans.resource.registry.carbon.wso2.org/xsd";
                                                            if (! namespace.equals("")) {
                                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace);

                                                                if (prefix2 == null) {
                                                                    prefix2 = generatePrefix(namespace);

                                                                    xmlWriter.writeStartElement(prefix2,"userNames", namespace);
                                                                    xmlWriter.writeNamespace(prefix2, namespace);
                                                                    xmlWriter.setPrefix(prefix2, namespace);

                                                                } else {
                                                                    xmlWriter.writeStartElement(namespace,"userNames");
                                                                }

                                                            } else {
                                                                xmlWriter.writeStartElement("userNames");
                                                            }
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                            java.lang.String namespace2 = "http://beans.resource.registry.carbon.wso2.org/xsd";
                                            if (! namespace2.equals("")) {
                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                if (prefix2 == null) {
                                                    prefix2 = generatePrefix(namespace2);

                                                    xmlWriter.writeStartElement(prefix2,"userNames", namespace2);
                                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                                    xmlWriter.setPrefix(prefix2, namespace2);

                                                } else {
                                                    xmlWriter.writeStartElement(namespace2,"userNames");
                                                }

                                            } else {
                                                xmlWriter.writeStartElement("userNames");
                                            }

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localUserPermissionsTracker){
                                       if (localUserPermissions!=null){
                                            for (int i = 0;i < localUserPermissions.length;i++){
                                                if (localUserPermissions[i] != null){
                                                 localUserPermissions[i].serialize(new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd","userPermissions"),
                                                           factory,xmlWriter);
                                                } else {
                                                   
                                                            // write null attribute
                                                            java.lang.String namespace2 = "http://beans.resource.registry.carbon.wso2.org/xsd";
                                                            if (! namespace2.equals("")) {
                                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                                if (prefix2 == null) {
                                                                    prefix2 = generatePrefix(namespace2);

                                                                    xmlWriter.writeStartElement(prefix2,"userPermissions", namespace2);
                                                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                                                    xmlWriter.setPrefix(prefix2, namespace2);

                                                                } else {
                                                                    xmlWriter.writeStartElement(namespace2,"userPermissions");
                                                                }

                                                            } else {
                                                                xmlWriter.writeStartElement("userPermissions");
                                                            }

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                // write null attribute
                                                java.lang.String namespace2 = "http://beans.resource.registry.carbon.wso2.org/xsd";
                                                if (! namespace2.equals("")) {
                                                    java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                    if (prefix2 == null) {
                                                        prefix2 = generatePrefix(namespace2);

                                                        xmlWriter.writeStartElement(prefix2,"userPermissions", namespace2);
                                                        xmlWriter.writeNamespace(prefix2, namespace2);
                                                        xmlWriter.setPrefix(prefix2, namespace2);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace2,"userPermissions");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("userPermissions");
                                                }

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localVersionViewTracker){
                                    namespace = "http://beans.resource.registry.carbon.wso2.org/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"versionView", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"versionView");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("versionView");
                                    }
                                
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("versionView cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localVersionView));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             }
                    xmlWriter.writeEndElement();
               

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

        /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
                if (namespace.equals(""))
              {
                  xmlWriter.writeAttribute(attName,attValue);
              }
              else
              {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }


           /**
             * Util method to write an attribute without the ns prefix
             */
            private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                             javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

                java.lang.String attributeNamespace = qname.getNamespaceURI();
                java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                if (attributePrefix == null) {
                    attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                }
                java.lang.String attributeValue;
                if (attributePrefix.trim().length() > 0) {
                    attributeValue = attributePrefix + ":" + qname.getLocalPart();
                } else {
                    attributeValue = qname.getLocalPart();
                }

                if (namespace.equals("")) {
                    xmlWriter.writeAttribute(attName, attributeValue);
                } else {
                    registerPrefix(xmlWriter, namespace);
                    xmlWriter.writeAttribute(namespace, attName, attributeValue);
                }
            }
        /**
         *  method to handle Qnames
         */

        private void writeQName(javax.xml.namespace.QName qname,
                                javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix,namespaceURI);
                }

                if (prefix.trim().length() > 0){
                    xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                }

            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

            if (qnames != null) {
                // we have to store this data until last moment since it is not possible to write any
                // namespace data after writing the charactor data
                java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                java.lang.String namespaceURI = null;
                java.lang.String prefix = null;

                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix,namespaceURI);
                        }

                        if (prefix.trim().length() > 0){
                            stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }

        }


         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = generatePrefix(namespace);

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }


  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException{


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localAuthorizeAllowedTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd",
                                                                      "authorizeAllowed"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAuthorizeAllowed));
                            } if (localDeleteAllowedTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd",
                                                                      "deleteAllowed"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDeleteAllowed));
                            } if (localPathWithVersionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd",
                                                                      "pathWithVersion"));
                                 
                                         elementList.add(localPathWithVersion==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPathWithVersion));
                                    } if (localPutAllowedTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd",
                                                                      "putAllowed"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPutAllowed));
                            } if (localRoleNamesTracker){
                            if (localRoleNames!=null){
                                  for (int i = 0;i < localRoleNames.length;i++){
                                      
                                         if (localRoleNames[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd",
                                                                              "roleNames"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRoleNames[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd",
                                                                              "roleNames"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd",
                                                                              "roleNames"));
                                    elementList.add(null);
                                
                            }

                        } if (localRolePermissionsTracker){
                             if (localRolePermissions!=null) {
                                 for (int i = 0;i < localRolePermissions.length;i++){

                                    if (localRolePermissions[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd",
                                                                          "rolePermissions"));
                                         elementList.add(localRolePermissions[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd",
                                                                          "rolePermissions"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd",
                                                                          "rolePermissions"));
                                        elementList.add(localRolePermissions);
                                    
                             }

                        } if (localUserNamesTracker){
                            if (localUserNames!=null){
                                  for (int i = 0;i < localUserNames.length;i++){
                                      
                                         if (localUserNames[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd",
                                                                              "userNames"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUserNames[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd",
                                                                              "userNames"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd",
                                                                              "userNames"));
                                    elementList.add(null);
                                
                            }

                        } if (localUserPermissionsTracker){
                             if (localUserPermissions!=null) {
                                 for (int i = 0;i < localUserPermissions.length;i++){

                                    if (localUserPermissions[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd",
                                                                          "userPermissions"));
                                         elementList.add(localUserPermissions[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd",
                                                                          "userPermissions"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd",
                                                                          "userPermissions"));
                                        elementList.add(localUserPermissions);
                                    
                             }

                        } if (localVersionViewTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd",
                                                                      "versionView"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localVersionView));
                            }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{

        
        

        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static PermissionBean parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            PermissionBean object =
                new PermissionBean();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix ="";
            java.lang.String namespaceuri ="";
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = null;
                    if (fullTypeName.indexOf(":") > -1){
                        nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    }
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    
                            if (!"PermissionBean".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (PermissionBean)org.wso2.carbon.registry.resource.services.utils.xsd.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                 
                    
                    reader.next();
                
                        java.util.ArrayList list5 = new java.util.ArrayList();
                    
                        java.util.ArrayList list6 = new java.util.ArrayList();
                    
                        java.util.ArrayList list7 = new java.util.ArrayList();
                    
                        java.util.ArrayList list8 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd","authorizeAllowed").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAuthorizeAllowed(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd","deleteAllowed").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDeleteAllowed(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd","pathWithVersion").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPathWithVersion(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd","putAllowed").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPutAllowed(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd","roleNames").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list5.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list5.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone5 = false;
                                            while(!loopDone5){
                                                // Ensure we are at the EndElement
                                                while (!reader.isEndElement()){
                                                    reader.next();
                                                }
                                                // Step out of this element
                                                reader.next();
                                                // Step to next element event.
                                                while (!reader.isStartElement() && !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    loopDone5 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd","roleNames").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list5.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list5.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone5 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setRoleNames((java.lang.String[])
                                                        list5.toArray(new java.lang.String[list5.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd","rolePermissions").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list6.add(null);
                                                              reader.next();
                                                          } else {
                                                        list6.add(org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone6 = false;
                                                        while(!loopDone6){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone6 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd","rolePermissions").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list6.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list6.add(org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone6 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setRolePermissions((org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry.class,
                                                                list6));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd","userNames").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list7.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list7.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone7 = false;
                                            while(!loopDone7){
                                                // Ensure we are at the EndElement
                                                while (!reader.isEndElement()){
                                                    reader.next();
                                                }
                                                // Step out of this element
                                                reader.next();
                                                // Step to next element event.
                                                while (!reader.isStartElement() && !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    loopDone7 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd","userNames").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list7.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list7.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone7 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setUserNames((java.lang.String[])
                                                        list7.toArray(new java.lang.String[list7.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd","userPermissions").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list8.add(null);
                                                              reader.next();
                                                          } else {
                                                        list8.add(org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone8 = false;
                                                        while(!loopDone8){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone8 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd","userPermissions").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list8.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list8.add(org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone8 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setUserPermissions((org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                org.wso2.carbon.registry.resource.beans.xsd.PermissionEntry.class,
                                                                list8));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://beans.resource.registry.carbon.wso2.org/xsd","versionView").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setVersionView(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                  
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            
                                if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                            



            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          