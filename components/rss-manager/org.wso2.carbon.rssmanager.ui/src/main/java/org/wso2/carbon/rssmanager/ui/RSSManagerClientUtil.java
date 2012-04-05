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
import org.wso2.carbon.rssmanager.common.RSSManagerCommonUtil;
import org.wso2.carbon.rssmanager.ui.beans.DatabaseInstance;
import org.wso2.carbon.rssmanager.ui.beans.DatabasePermissions;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RSSManagerClientUtil {

    private static final OMFactory omFactory = (OMFactory) OMAbstractFactory.getOMFactory();

    private static final String NULL_NAMESPACE = "";

    private static final OMNamespace NULL_OMNS = omFactory.createOMNamespace(NULL_NAMESPACE, "");

    public static DatabaseInstance buildDatabaseInstance(OMElement dbEl) throws Exception {
        DatabaseInstance db = new DatabaseInstance();
        String name = dbEl.getAttributeValue(new QName(NULL_NAMESPACE, "name"));
        if (name != null) {
            db.setName(name);
        }
        String dbInsId = dbEl.getAttributeValue(new QName(NULL_NAMESPACE, "dbInsId"));
        if (dbInsId != null) {
            db.setDatabaseInstanceId(Integer.parseInt(dbInsId));
        }
        String rssInsId = dbEl.getAttributeValue(new QName(NULL_NAMESPACE, "rssInsId"));
        if (rssInsId != null) {
            db.setRssInstanceId(Integer.parseInt(rssInsId));
        }

        return db;
    }
    
    public static List<DatabaseInstance> buildDatabaseInstanceList(OMElement rootEl)
            throws Exception {
        List<DatabaseInstance> dbs = new ArrayList<DatabaseInstance>();
        OMElement dbsEl = (OMElement) rootEl.getChildElements().next();
        Iterator itr = dbsEl.getChildElements();
        while (itr.hasNext()) {
            OMElement dbEl = (OMElement) itr.next();
            DatabaseInstance db = buildDatabaseInstance(dbEl);
            if (db != null) {
                dbs.add(db);
            }
        }
        return dbs;
    }

    public static OMElement serializeDatabaseInstanceData(OMNamespace targetNamespace,
                                                          DatabaseInstance db) throws Exception {
        if (db == null) {
            throw new Exception("DatabaseInfo Object Cannot Be Null");
        }

        OMElement dbEl = omFactory.createOMElement("db", targetNamespace);

        String rssInstId = String.valueOf(db.getRssInstanceId());
        if (!"".equals(rssInstId) && rssInstId != null) {
            dbEl.addAttribute("rssInsId", rssInstId, NULL_OMNS);
        }
        String dbName = db.getName();
        if (!"".equals(dbName) && dbName != null) {
            dbEl.addAttribute("name", dbName, NULL_OMNS);
        }
        String dbInstId = String.valueOf(db.getDatabaseInstanceId());
        if (!"".equals(dbInstId) && dbInstId != null) {
            dbEl.addAttribute("dbInsId", dbInstId, NULL_OMNS);
        }

        return dbEl;
    }

    public static OMElement serializePermissionObject(
            OMNamespace targetNamespace, DatabasePermissions permissions) throws Exception {
        if (permissions == null) {
            throw new Exception("Permissions Object Cannot Be Null");
        }
        OMElement permissionsElement = omFactory.createOMElement("permissions", targetNamespace);
        for (Map.Entry entry : permissions.getPrivilegeMap().entrySet()) {
            Object attributeValue = entry.getValue();
            if (attributeValue != null) {
                permissionsElement.addAttribute(entry.getKey().toString(),
                        attributeValue.toString(), targetNamespace);
            } else {
                permissionsElement.addAttribute(entry.getKey().toString(), "",
                        targetNamespace);
            }
        }
        return permissionsElement;
    }

    public static DatabasePermissions getPermissionObject(OMElement privilegesElement) {
        DatabasePermissions permissions = new DatabasePermissions();
        Iterator attributeIterator = privilegesElement.getAllAttributes();
        while (attributeIterator.hasNext()) {
            OMAttribute attribute = (OMAttribute) attributeIterator.next();
            String attributeName = attribute.getLocalName();
            String value = attribute.getAttributeValue();
            for (String priv : RSSManagerCommonUtil.getDatabasePrivilegeList()) {
                if (priv.equals(attributeName)) {
                    if (value != null) {
                        if (RSSManagerCommonUtil.getBooleanResponsePrivilegeList().contains(priv)) {
                            permissions.setPermission(priv, value);
                        } else if (RSSManagerCommonUtil.getBlobResponsePrivilegeList().contains(priv)) {
                            permissions.setPermission(priv, value);
                        } else if (RSSManagerCommonUtil.getIntegerResponsePrivilegeList().contains(priv)) {
                            permissions.setPermission(priv, Integer.parseInt(value));
                        } else if (RSSManagerCommonUtil.getStringResponsePrivilegeList().contains(priv)) {
                            permissions.setPermission(priv, value);
                        }
                    } else {
                        if (RSSManagerCommonUtil.getBooleanResponsePrivilegeList().contains(priv)) {
                            permissions.setPermission(priv, "N");
                        } else if (RSSManagerCommonUtil.getBlobResponsePrivilegeList().contains(priv)) {
                            permissions.setPermission(priv, "");
                        } else if (RSSManagerCommonUtil.getIntegerResponsePrivilegeList().contains(priv)) {
                            permissions.setPermission(priv, 0);
                        } else if (RSSManagerCommonUtil.getStringResponsePrivilegeList().contains(priv)) {
                            permissions.setPermission(priv, "");
                        }
                    }
                }
            }
        }
        return permissions;
    }
}
