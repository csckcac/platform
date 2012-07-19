/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.rssmanager.ui;

import org.apache.axiom.om.*;

public class RSSManagerClientUtil {

    private static final OMFactory omFactory = (OMFactory) OMAbstractFactory.getOMFactory();

    private static final String NULL_NAMESPACE = "";

    private static final OMNamespace NULL_OMNS = omFactory.createOMNamespace(NULL_NAMESPACE, "");

//    public static OMElement serializePermissionObject(
//            OMNamespace targetNamespace, DatabasePermissions permissions) throws Exception {
//        if (permissions == null) {
//            throw new Exception("Permissions Object Cannot Be Null");
//        }
//        OMElement permissionsElement = omFactory.createOMElement("permissions", targetNamespace);
//        for (Map.Entry entry : permissions.getPrivilegeMap().entrySet()) {
//            Object attributeValue = entry.getValue();
//            if (attributeValue != null) {
//                permissionsElement.addAttribute(entry.getKey().toString(),
//                        attributeValue.toString(), targetNamespace);
//            } else {
//                permissionsElement.addAttribute(entry.getKey().toString(), "",
//                        targetNamespace);
//            }
//        }
//        return permissionsElement;
//    }

//    public static DatabasePermissions getPermissionObject(OMElement privilegesElement) {
//        DatabasePermissions permissions = new DatabasePermissions();
//        Iterator attributeIterator = privilegesElement.getAllAttributes();
//        while (attributeIterator.hasNext()) {
//            OMAttribute attribute = (OMAttribute) attributeIterator.next();
//            String attributeName = attribute.getLocalName();
//            String value = attribute.getAttributeValue();
//            for (String priv : RSSManagerCommonUtil.getDatabasePrivilegeList()) {
//                if (priv.equals(attributeName)) {
//                    if (value != null) {
//                        if (RSSManagerCommonUtil.getBooleanResponsePrivilegeList().contains(priv)) {
//                            permissions.setPermission(priv, value);
//                        } else if (RSSManagerCommonUtil.getBlobResponsePrivilegeList().contains(priv)) {
//                            permissions.setPermission(priv, value);
//                        } else if (RSSManagerCommonUtil.getIntegerResponsePrivilegeList().contains(priv)) {
//                            permissions.setPermission(priv, Integer.parseInt(value));
//                        } else if (RSSManagerCommonUtil.getStringResponsePrivilegeList().contains(priv)) {
//                            permissions.setPermission(priv, value);
//                        }
//                    } else {
//                        if (RSSManagerCommonUtil.getBooleanResponsePrivilegeList().contains(priv)) {
//                            permissions.setPermission(priv, "N");
//                        } else if (RSSManagerCommonUtil.getBlobResponsePrivilegeList().contains(priv)) {
//                            permissions.setPermission(priv, "");
//                        } else if (RSSManagerCommonUtil.getIntegerResponsePrivilegeList().contains(priv)) {
//                            permissions.setPermission(priv, 0);
//                        } else if (RSSManagerCommonUtil.getStringResponsePrivilegeList().contains(priv)) {
//                            permissions.setPermission(priv, "");
//                        }
//                    }
//                }
//            }
//        }
//        return permissions;
//    }

}
