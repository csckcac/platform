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
package org.wso2.carbon.cassandra.dataaccess.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cassandra.dataaccess.CassandraAxis2ConfigurationContextObserver;
import org.wso2.carbon.cassandra.dataaccess.DataAccessComponentManager;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.cassandra.dataaccess.DataAccessServiceImpl;
import org.wso2.carbon.identity.authentication.SharedKeyAccessService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;

/**
 * @scr.component name="org.wso2.carbon.cassandra.dataaccess.component" immediate="true"
 * @scr.reference name="org.wso2.carbon.identity.authentication.internal.AuthenticationServiceComponent"
 * interface="org.wso2.carbon.identity.authentication.SharedKeyAccessService"
 * cardinality="0..1" policy="dynamic" bind="setSharedKeyAccessService"  unbind="unsetSharedKeyAccessService"
 */
public class DataAccessServiceDSComponent {

    private static Log log = LogFactory.getLog(DataAccessServiceDSComponent.class);

    private ServiceRegistration serviceRegistration;
    private SharedKeyAccessService sharedKeyAccessService;
    private DataAccessService dataAccessService;
    private ServiceRegistration axisConfigContextObserverServiceReg;

    protected void activate(ComponentContext componentContext) {

        if (log.isDebugEnabled()) {
            log.debug("Starting the data access component for Cassandra");
        }

        DataAccessComponentManager.getInstance().init(sharedKeyAccessService);
        dataAccessService = new DataAccessServiceImpl();
        serviceRegistration = componentContext.getBundleContext().registerService(
                DataAccessService.class.getName(),
                dataAccessService,
                null);
        axisConfigContextObserverServiceReg = componentContext.getBundleContext().registerService(
                Axis2ConfigurationContextObserver.class.getName(),
                new CassandraAxis2ConfigurationContextObserver(dataAccessService),
                null);
    }

    protected void deactivate(ComponentContext componentContext) {

        if (log.isDebugEnabled()) {
            log.debug("Stopping the data access component for Cassandra");
        }
        dataAccessService.destroyAllClusters();
        componentContext.getBundleContext().ungetService(serviceRegistration.getReference());
        componentContext.getBundleContext().ungetService(axisConfigContextObserverServiceReg.getReference());
    }

    protected void setSharedKeyAccessService(SharedKeyAccessService sharedKeyAccessService) {
        this.sharedKeyAccessService = sharedKeyAccessService;
    }

    protected void unsetSharedKeyAccessService(SharedKeyAccessService sharedKeyAccessService) {
        this.sharedKeyAccessService = null;
    }
}
