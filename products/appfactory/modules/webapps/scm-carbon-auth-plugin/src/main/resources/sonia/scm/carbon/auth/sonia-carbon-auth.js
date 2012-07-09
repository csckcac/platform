/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

registerGeneralConfigPanel({
                               xtype : 'configForm',
                               title : 'Carbon Authentication Settings',
                               items : [{
                                            xtype : 'textfield',
                                            fieldLabel : 'Carbon Server URL',
                                            name : 'backEndServerUrl',
                                            allowBlank : false
                               },{
                                   xtype : 'textfield',

                                   fieldLabel : 'Admin User Name',
                                   name : 'adminUserName',
                                   allowBlank : false
                               },{
                                   xtype : 'textfield',
                                   inputType: 'password',
                                   fieldLabel : 'DefaultTenant Password',
                                   name : 'defaultTenantPassword',
                                   allowBlank : false
                               },{
                                   xtype : 'textfield',

                                   fieldLabel : 'Role of SVN User',
                                   name : 'roleOfSVNRW',
                                   allowBlank : false
                               }],

                               onSubmit: function(values){
                                   this.el.mask('Submit ...');
                                   Ext.Ajax.request({
                                                        url: restUrl + 'config/auth/carbon.json',
                                                        method: 'POST',
                                                        jsonData: values,
                                                        scope: this,
                                                        disableCaching: true,
                                                        success: function(response){
                                                            this.el.unmask();
                                                        },
                                                        failure: function(){
                                                            this.el.unmask();
                                                        }
                                                    });
                               },

                               onLoad: function(el){
                                   var tid = setTimeout( function(){ el.mask('Loading ...'); }, 100);
                                   Ext.Ajax.request({
                                                        url: restUrl + 'config/auth/carbon.json',
                                                        method: 'GET',
                                                        scope: this,
                                                        disableCaching: true,
                                                        success: function(response){
                                                            var obj = Ext.decode(response.responseText);
                                                            this.load(obj);
                                                            clearTimeout(tid);
                                                            el.unmask();
                                                        },
                                                        failure: function(){
                                                            el.unmask();
                                                            clearTimeout(tid);
                                                            alert('failure');
                                                        }
                                                    });
                               }
                           });