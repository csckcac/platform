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
package org.wso2.carbon.mediation.library.connectors.core;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.mediation.library.connectors.core.util.ConfigHolder;
import org.wso2.carbon.mediation.library.connectors.core.util.ConnectorUtils;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public abstract class AbstractConnector extends AbstractMediator implements Connector {
    private ThreadLocal<MessageContext> currentContext = new ThreadLocal<MessageContext>();

    public abstract void connect() throws ConnectException;

    protected MessageContext getMessageContext() {
        return currentContext.get();
    }

    public boolean mediate(MessageContext messageContext) {
        currentContext.set(messageContext);
        try {
            connect();
        } catch (ConnectException e) {
            throw new SynapseException("Error occurred when connecting conenctor. Details :", e);
        }
        return true;
    }

    public UserRegistry getConfigRegistry() throws ConnectException {
        org.apache.axis2.context.MessageContext axis2Ctxt = ((Axis2MessageContext) currentContext.get()).getAxis2MessageContext();
        int tenantId = MultitenantUtils.getTenantId(axis2Ctxt.getConfigurationContext());
        UserRegistry configReg = null;
        try {
            configReg = ConfigHolder.getInstance().getRegistryService().
                    getConfigSystemRegistry(tenantId);
        } catch (RegistryException e) {
            throw new ConnectException(e);
        }
        return configReg;
    }

    public UserRegistry getGovernanceRegistry() throws ConnectException {
        org.apache.axis2.context.MessageContext axis2Ctxt = ((Axis2MessageContext) currentContext.get()).getAxis2MessageContext();
        int tenantId = MultitenantUtils.getTenantId(axis2Ctxt.getConfigurationContext());
        UserRegistry govReg = null;
        try {
            govReg = ConfigHolder.getInstance().getRegistryService().
                    getGovernanceSystemRegistry(tenantId);
        } catch (RegistryException e) {
            throw new ConnectException(e);
        }
        return govReg;
    }

    public UserRealm getUserRealm() throws ConnectException {
        org.apache.axis2.context.MessageContext axis2Ctxt = ((Axis2MessageContext) currentContext.get()).getAxis2MessageContext();
        int tenantId = MultitenantUtils.getTenantId(axis2Ctxt.getConfigurationContext());
        UserRealm userRealm = null;
        try {
            userRealm = ConfigHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);

        } catch (UserStoreException e) {
            throw new ConnectException(e);
        }
        return userRealm;
    }

    public SynapseEnvironment getSynapseEnvironment() throws ConnectException {
        org.apache.axis2.context.MessageContext axis2Ctxt = ((Axis2MessageContext) currentContext.get()).getAxis2MessageContext();
        int tenantId = MultitenantUtils.getTenantId(axis2Ctxt.getConfigurationContext());
        SynapseEnvironment synapseEnvironment = null;
        synapseEnvironment = ConfigHolder.getInstance().getSynapseEnvironmentService(tenantId).
                getSynapseEnvironment();

        return synapseEnvironment;
    }

    protected String getParameter(String paramName){
        return ConnectorUtils.lookupFunctionParam(getMessageContext(), paramName);
    }
}
