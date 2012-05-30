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

    private RegistryService registryService;
    SystemStatisticsUtil statisticsUtil;


    public StatisticsPersistenceScheduler(RegistryService registryService) {
        this.registryService = registryService;
        statisticsUtil = new SystemStatisticsUtil();
    }

    public void invoke(AxisConfiguration axisConfiguration, int tenantID) {
        if(log.isDebugEnabled()){
            log.debug("Invoking StatisticsPersistenceScheduler.. for tenant : " + tenantID);
        }
        try {
            UserRegistry registry = registryService.getLocalRepository(tenantID);
            persistSystemStatistics(axisConfiguration, registry);
            persistOperationStatistics(axisConfiguration, registry);
            persistServiceStatistics(axisConfiguration, registry);
//        sendRepositorySyncMessage();
        } catch (RegistryException e) {
            log.error("Error getting tenant " + tenantID + "'s local registry instance.");
            e.printStackTrace();
        }
    }

    private void sendRepositorySyncMessage() {
        // For sending clustering messages we need to use the super-tenant's AxisConfig (Main Server
        // AxisConfiguration) because we are using the clustering facility offered by the ST in the
        // tenants
//        System.out.println("YYY StatisticsPersistenceScheduler.sendRepositorySyncMessage START");
        ClusteringAgent clusteringAgent =
                CarbonCoreDataHolder.getInstance().getMainServerConfigContext().
                        getAxisConfiguration().getClusteringAgent();
        int tenantId = SuperTenantCarbonContext.getCurrentContext().getTenantId();
        if (clusteringAgent != null) {
//            int numberOfRetries = 0;
//            while (numberOfRetries < 60) {
            try {
                clusteringAgent.sendMessage(new StatisticsSynchronizeRequest(tenantId), true);
//                    break;
            } catch (ClusteringFault e) {
                log.error("Error sending Statistics Synchronize Request.. " + e.getMessage());
                e.printStackTrace();
            }
//            }
        }
//        System.out.println("YYY StatisticsPersistenceScheduler.sendRepositorySyncMessage END");

    }

    public void persistSystemStatistics(AxisConfiguration axisConfig, UserRegistry registry) {

        //persist system stats
        String regPath = StatisticsConstants.SYSTEM_REG_PATH;
        Resource resource;


        try {
            if (registry.resourceExists(regPath)) {
                resource = registry.get(regPath);
            } else {
                resource = registry.newResource();
            }
            resource.setProperty(StatisticsConstants.GLOBAL_REQUEST_COUNTER,
                                 String.valueOf(statisticsUtil.getTotalSystemRequestCount(axisConfig)));
            resource.setProperty(StatisticsConstants.GLOBAL_RESPONSE_COUNTER,
                                 String.valueOf(statisticsUtil.getSystemResponseCount(axisConfig)));
            resource.setProperty(StatisticsConstants.GLOBAL_FAULT_COUNTER,
                                 String.valueOf(statisticsUtil.getSystemFaultCount(axisConfig)));
//            resource.setProperty(StatisticsConstants.RESPONSE_TIME_PROCESSOR,
//                                 String.valueOf(statisticsUtil.getAvgSystemResponseTime(axisConfig)));
//            resource.setProperty(StatisticsConstants.RESPONSE_TIME_PROCESSOR,
//                                 String.valueOf(statisticsUtil.getMaxSystemResponseTime(axisConfig)));
//            resource.setProperty(StatisticsConstants.RESPONSE_TIME_PROCESSOR,
//                                 String.valueOf(statisticsUtil.getMinSystemResponseTime(axisConfig)));
//            resource.setProperty(StatisticsConstants.GLOBAL_CURRENT_INVOCATION_RESPONSE_TIME,
//                                 String.valueOf(statisticsUtil.getCurrentSystemResponseTime(axisConfig)));
//            resource.setProperty(StatisticsConstants.GLOBAL_CURRENT_RESPONSE_COUNTER,
//                                 String.valueOf(statisticsUtil.getCurrentSystemResponseCount(axisConfig)));
//            resource.setProperty(StatisticsConstants.GLOBAL_CURRENT_REQUEST_COUNTER,
//                                 String.valueOf(statisticsUtil.getCurrentSystemRequestCount(axisConfig)));
//            resource.setProperty(StatisticsConstants.GLOBAL_CURRENT_FAULT_COUNTER,
//                                 String.valueOf(statisticsUtil.getCurrentSystemFaultCount(axisConfig)));

            registry.put(regPath, resource);

        } catch (RegistryException e) {
            e.printStackTrace();
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
    }

    private void persistServiceStatistics(AxisConfiguration axisConfig,
                                          UserRegistry registry) {
        //persist service stats
        String regPath = StatisticsConstants.SERVICES_REG_PATH;

        try {
            HashMap<String, AxisService> services = axisConfig.getServices();
            for (AxisService axisService : services.values()) {

                if (axisService == null ||
                    SystemFilter.isFilteredOutService(axisService.getAxisServiceGroup()) ||
                    axisService.isClientSide()) {
                    continue;
                }
                Resource resource;
                String serviceRegPath = regPath.concat("/").concat(axisService.getName());

                if (registry.resourceExists(serviceRegPath)) {
                    resource = registry.get(serviceRegPath);
                } else {
                    resource = registry.newResource();
                }
                ServiceStatistics serviceStatistics = statisticsUtil.getServiceStatistics
                        (axisService);
                resource.setProperty(
                        StatisticsConstants.SERVICE_RESPONSE_TIME_PROCESSOR,
                        String.valueOf(serviceStatistics.getAvgResponseTime()));
                resource.setProperty(
                        StatisticsConstants.SERVICE_RESPONSE_TIME,
                        String.valueOf(serviceStatistics.getCurrentInvocationResponseTime()));
                resource.setProperty(
                        StatisticsConstants.OPERATION_FAULT_COUNTER,
                        String.valueOf(serviceStatistics.getTotalFaultCount()));
                resource.setProperty(
                        StatisticsConstants.SERVICE_RESPONSE_TIME_PROCESSOR,
                        String.valueOf(serviceStatistics.getMaxResponseTime()));
                resource.setProperty(
                        StatisticsConstants.SERVICE_RESPONSE_TIME_PROCESSOR,
                        String.valueOf(serviceStatistics.getMinResponseTime()));
                resource.setProperty(
                        StatisticsConstants.IN_OPERATION_COUNTER,
                        String.valueOf(serviceStatistics.getTotalRequestCount()));
                resource.setProperty(
                        StatisticsConstants.OUT_OPERATION_COUNTER,
                        String.valueOf(serviceStatistics.getTotalResponseCount()));
                resource.setProperty(
                        StatisticsConstants.CURRENT_IN_OPERATION_AND_SERVICE_COUNTER,
                        String.valueOf(serviceStatistics.getCurrentInvocationRequestCount()));
                resource.setProperty(
                        StatisticsConstants.CURRENT_OUT_OPERATION_AND_SERVICE_COUNTER,
                        String.valueOf(serviceStatistics.getCurrentInvocationResponseCount()));
                resource.setProperty(
                        StatisticsConstants.CURRENT_OPERATION_AND_SERVICE_FAULT_COUNTER,
                        String.valueOf(serviceStatistics.getCurrentInvocationFaultCount()));

                registry.put(serviceRegPath, resource);
            }

        } catch (RegistryException e) {
            e.printStackTrace();
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
    }

    private void persistOperationStatistics(AxisConfiguration axisConfig,
                                            UserRegistry registry) {
        //persist operation stats
        String regPath = StatisticsConstants.SERVICES_REG_PATH;

        try {
            HashMap<String, AxisService> services = axisConfig.getServices();
            for (AxisService axisService : services.values()) {

                if (axisService == null ||
                    SystemFilter.isFilteredOutService(axisService.getAxisServiceGroup()) ||
                    axisService.isClientSide()) {
                    continue;
                }
                Resource resource;
                String serviceRegPath = regPath.concat("/").concat(axisService.getName());

                Iterator<AxisOperation> axisOperationIterator = axisService.getOperations();
                while (axisOperationIterator.hasNext()) {
                    AxisOperation axisOperation = axisOperationIterator.next();
                    String operationRegPath = serviceRegPath.concat("/").concat(
                            axisOperation.getName().getLocalPart());

                    if (registry.resourceExists(operationRegPath)) {
                        resource = registry.get(operationRegPath);
                    } else {
                        resource = registry.newResource();
                    }
                    OperationStatistics operationStatistics = statisticsUtil.getOperationStatistics
                            (axisOperation);

                    resource.setProperty(
                            StatisticsConstants.OPERATION_RESPONSE_TIME_PROCESSOR,
                            String.valueOf(operationStatistics.getAvgResponseTime()));
                    resource.setProperty(
                            StatisticsConstants.OPERATION_RESPONSE_TIME,
                            String.valueOf(operationStatistics.getCurrentInvocationResponseTime()));
                    resource.setProperty(
                            StatisticsConstants.OPERATION_FAULT_COUNTER,
                            String.valueOf(operationStatistics.getTotalFaultCount()));
                    resource.setProperty(
                            StatisticsConstants.OPERATION_RESPONSE_TIME_PROCESSOR,
                            String.valueOf(operationStatistics.getMaxResponseTime()));
                    resource.setProperty(StatisticsConstants.OPERATION_RESPONSE_TIME_PROCESSOR,
                                         String.valueOf(operationStatistics.getMinResponseTime()));
                    resource.setProperty(
                            StatisticsConstants.IN_OPERATION_COUNTER,
                            String.valueOf(operationStatistics.getTotalRequestCount()));
                    resource.setProperty(
                            StatisticsConstants.OUT_OPERATION_COUNTER,
                            String.valueOf(operationStatistics.getTotalResponseCount()));
                    resource.setProperty(
                            StatisticsConstants.CURRENT_IN_OPERATION_AND_SERVICE_COUNTER,
                            String.valueOf(operationStatistics.getCurrentInvocationRequestCount()));
                    resource.setProperty(
                            StatisticsConstants.CURRENT_OUT_OPERATION_AND_SERVICE_COUNTER,
                            String.valueOf(operationStatistics.getCurrentInvocationResponseCount()));
                    resource.setProperty(
                            StatisticsConstants.CURRENT_OPERATION_AND_SERVICE_FAULT_COUNTER,
                            String.valueOf(operationStatistics.getCurrentInvocationFaultCount()));

                    registry.put(operationRegPath, resource);
                }
            }
        } catch (RegistryException e) {
            e.printStackTrace();
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }

    }
}
