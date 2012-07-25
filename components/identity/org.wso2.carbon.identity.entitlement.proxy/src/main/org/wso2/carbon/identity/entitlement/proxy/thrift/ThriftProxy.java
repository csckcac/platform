/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.identity.entitlement.proxy.thrift;

import java.util.List;

import org.wso2.carbon.identity.entitlement.proxy.AbstractPDPProxy;
import org.wso2.carbon.identity.entitlement.proxy.Attribute;
import org.wso2.carbon.identity.entitlement.proxy.PDPConfig;

public class ThriftProxy extends AbstractPDPProxy {

    @Override
    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
                                           String resourceId, String domainId, String appId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean subjectCanActOnResource(String subjectType, String alias, String actionId,
                                           String resourceId, Attribute[] attributes, String domainId, String appId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<String> getResourcesForAlias(String alias, String appId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getActionableResourcesForAlias(String alias, String appId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getActionsForResource(String alias, String resources, String appId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPDPConfig(PDPConfig config) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean getDecision(Attribute[] subjectAttrs, Attribute[] rescAttrs,
                               Attribute[] actionAttrs, Attribute[] envAttrs, String appId)
            throws Exception {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getActualDecision(Attribute[] subjectAttrs, Attribute[] rescAttrs,
                                    Attribute[] actionAttrs, Attribute[] envAttrs, String appId)
            throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getActionableChidResourcesForAlias(String alias, String parentResource,
                                                           String action, String appId) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean  getDecisionByAttributes(String subjectAttr, String rescAttrs,
                                            String actionAttrs, String [] envAttrs, String appId)
            throws Exception{
        return false;
    }

    @Override
    public String getActualDecisionByAttributes(String subjectAttr, String rescAttr,
                                                String actionAttr, String[] envAttrs, String appId)
            throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
