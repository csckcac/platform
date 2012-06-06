package org.wso2.carbon.statistics.persistance;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.deployment.CarbonDeploymentSchedulerExtender;
import org.wso2.carbon.core.deployment.SynchronizeRepositoryRequest;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.core.util.SystemFilter;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.statistics.StatisticsConstants;
import org.wso2.carbon.statistics.services.SystemStatisticsUtil;
import org.wso2.carbon.statistics.services.util.OperationStatistics;
import org.wso2.carbon.statistics.services.util.ServiceStatistics;
import org.wso2.carbon.statistics.synchronize.StatisticsSynchronizeRequest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;


public class StatisticsPersistenceScheduler implements CarbonDeploymentSchedulerExtender {

    private static final Log log = LogFactory.getLog(StatisticsPersistenceScheduler.class);

//    private RegistryService registryService;
    SystemStatisticsUtil statisticsUtil;

    public StatisticsPersistenceScheduler() {
//        this.registryService = registryService;
        statisticsUtil = new SystemStatisticsUtil();
    }

    public void invoke(AxisConfiguration axisConfiguration) {
        int tenantID = SuperTenantCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        if (log.isDebugEnabled()) {
            log.debug("Invoking StatisticsPersistenceScheduler.. for tenant : " + tenantID);
        }
        try {
//            UserRegistry registry = registryService.getLocalRepository(tenantID);
            persistSystemStatistics(axisConfiguration);
            persistOperationStatistics(axisConfiguration);
            persistServiceStatistics(axisConfiguration);
//        sendRepositorySyncMessage();
        } catch (Exception e) {
            log.error("Error invoking Statistics Persistence Scheduler..", e);
        }
    }

    private void sendRepositorySyncMessage() {
        // For sending clustering messages we need to use the super-tenant's AxisConfig (Main Server
        // AxisConfiguration) because we are using the clustering facility offered by the ST in the
        // tenants
        ClusteringAgent clusteringAgent =
                CarbonCoreDataHolder.getInstance().getMainServerConfigContext().
                        getAxisConfiguration().getClusteringAgent();
        int tenantId = SuperTenantCarbonContext.getCurrentContext().getTenantId();
        if (clusteringAgent != null) {
            try {
                clusteringAgent.sendMessage(new StatisticsSynchronizeRequest(tenantId), true);
            } catch (ClusteringFault e) {
                log.error("Error sending Statistics Synchronize Request.. " + e.getMessage(), e);
            }
        }
    }

    public void persistSystemStatistics(AxisConfiguration axisConfig) {
        //persist system stats
        try {
            StatisticsPersistenceUtils.persistSystemStatistics(axisConfig);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void persistServiceStatistics(AxisConfiguration axisConfig) {
        HashMap<String, AxisService> services = axisConfig.getServices();
        for (AxisService axisService : services.values()) {
            if (axisService == null ||
                SystemFilter.isFilteredOutService(axisService.getAxisServiceGroup()) ||
                axisService.isClientSide()) {
                continue;
            }
            try {
                //persist service stats
                StatisticsPersistenceUtils.persistStatisticsForService(axisService);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void persistOperationStatistics(AxisConfiguration axisConfig) {
        HashMap<String, AxisService> services = axisConfig.getServices();
        for (AxisService axisService : services.values()) {

            if (axisService == null ||
                SystemFilter.isFilteredOutService(axisService.getAxisServiceGroup()) ||
                axisService.isClientSide()) {
                continue;
            }
            for (Iterator iter = axisService.getOperations(); iter.hasNext(); ) {

                AxisOperation axisOperation = (AxisOperation) iter.next();
                try {
                    //persist operation stats
                    StatisticsPersistenceUtils.persistStatisticsForOperation(axisOperation);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}
