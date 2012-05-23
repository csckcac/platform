package org.wso2.carbon.statistics.persistance;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.core.deployment.CarbonDeploymentSchedulerExtender;
import org.wso2.carbon.core.util.SystemFilter;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.statistics.StatisticsConstants;
import org.wso2.carbon.statistics.services.SystemStatisticsUtil;
import org.wso2.carbon.statistics.services.util.OperationStatistics;
import org.wso2.carbon.statistics.services.util.ServiceStatistics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;


public class StatisticsPersistenceScheduler implements CarbonDeploymentSchedulerExtender{

    private UserRegistry localRegistry;
    SystemStatisticsUtil statisticsUtil;


    public StatisticsPersistenceScheduler(UserRegistry localRepository) {
        this.localRegistry = localRepository;
        statisticsUtil = new SystemStatisticsUtil();
    }

    public void invoke(AxisConfiguration axisConfiguration) {

        persistSystemStatistics(axisConfiguration);
        persistOperationStatistics(axisConfiguration);
        persistServiceStatistics(axisConfiguration);
    }

    public void persistSystemStatistics(AxisConfiguration axisConfig){

        //persist system stats
        String regPath = StatisticsConstants.SYSTEM_REG_PATH;
        Resource resource;


        try {
            if(localRegistry.resourceExists(regPath)){
                resource = localRegistry.get(regPath);
            } else {
                resource = localRegistry.newResource();
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

            localRegistry.put(regPath, resource);

        } catch (RegistryException e) {
            e.printStackTrace();
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
    }

    private void persistServiceStatistics(AxisConfiguration axisConfig) {
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

                if(localRegistry.resourceExists(serviceRegPath)){
                    resource = localRegistry.get(serviceRegPath);
                } else {
                    resource = localRegistry.newResource();
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

                localRegistry.put(serviceRegPath, resource);
            }

        } catch (RegistryException e) {
            e.printStackTrace();
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
    }

    private void persistOperationStatistics(AxisConfiguration axisConfig) {
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
                while (axisOperationIterator.hasNext()){
                    AxisOperation axisOperation = axisOperationIterator.next();
                    String operationRegPath = serviceRegPath.concat("/").concat(
                            axisOperation.getName().toString());

                    if(localRegistry.resourceExists(operationRegPath)){
                        resource = localRegistry.get(operationRegPath);
                    } else {
                        resource = localRegistry.newResource();
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

                    localRegistry.put(operationRegPath, resource);
                }
            }
        } catch (RegistryException e) {
            e.printStackTrace();
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }

    }
}
