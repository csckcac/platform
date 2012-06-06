package org.wso2.carbon.statistics.persistance;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.statistics.StatisticsConstants;
import org.wso2.carbon.statistics.services.SystemStatisticsUtil;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.statistics.services.util.OperationStatistics;
import org.wso2.carbon.statistics.services.util.ServiceStatistics;

import java.util.concurrent.atomic.AtomicInteger;

public class StatisticsPersistenceUtils {
    private static final Log log = LogFactory.getLog(StatisticsPersistenceUtils.class);

    public static void retrieveSystemStatistics(AxisConfiguration axisConfig) {
        Registry registry = getRegistry(axisConfig);
        String registryPath = getRegistryPath();
        Resource resource = getRegistryResource(registry, registryPath);

        setProperty(StatisticsConstants.GLOBAL_REQUEST_COUNTER, axisConfig, resource);
        setProperty(StatisticsConstants.GLOBAL_RESPONSE_COUNTER, axisConfig, resource);
        setProperty(StatisticsConstants.GLOBAL_FAULT_COUNTER, axisConfig, resource);
    }

    public static void attachStatisticsForService(AxisService axisService) {
        Registry registry = getRegistry(axisService.getAxisConfiguration());
        String registryPath = getRegistryPathForService(axisService);

        Resource resource = getRegistryResource(registry, registryPath);

        setProperty(StatisticsConstants.IN_OPERATION_COUNTER, axisService, resource);
        setProperty(StatisticsConstants.OUT_OPERATION_COUNTER, axisService, resource);
        setProperty(StatisticsConstants.OPERATION_FAULT_COUNTER, axisService, resource);
    }

    public static void attachStatisticsForOperation(AxisOperation axisOperation) {
        Registry registry = getRegistry(axisOperation.getAxisConfiguration());
        String regPath = getRegistryPathForOperation(axisOperation);

        Resource resource = getRegistryResource(registry, regPath);

        setProperty(StatisticsConstants.IN_OPERATION_COUNTER, axisOperation, resource);
        setProperty(StatisticsConstants.OUT_OPERATION_COUNTER, axisOperation, resource);
        setProperty(StatisticsConstants.OPERATION_FAULT_COUNTER, axisOperation, resource);
    }

    /**
     * Persist Global System Statistics
     *
     * @param axisConfig - AxisConfig for tenant
     * @throws Exception - On Error
     */
    public static void persistSystemStatistics(AxisConfiguration axisConfig) throws Exception {
        Registry registry = getRegistry(axisConfig);
        String registryPath = getRegistryPath();
        SystemStatisticsUtil statisticsUtil = new SystemStatisticsUtil();
        Resource resource;
        try {
            if (registry.resourceExists(registryPath)) {
                resource = registry.get(registryPath);
            } else {
                resource = registry.newResource();
            }
            resource.setProperty(StatisticsConstants.GLOBAL_REQUEST_COUNTER,
                                 String.valueOf(statisticsUtil.getTotalSystemRequestCount(axisConfig)));
            resource.setProperty(StatisticsConstants.GLOBAL_RESPONSE_COUNTER,
                                 String.valueOf(statisticsUtil.getSystemResponseCount(axisConfig)));
            resource.setProperty(StatisticsConstants.GLOBAL_FAULT_COUNTER,
                                 String.valueOf(statisticsUtil.getSystemFaultCount(axisConfig)));
/*
            System.out.println("YYYYYYYYYYYYYYYYYY persist stats for system -" +
                               retrievePropertyValue(StatisticsConstants.GLOBAL_REQUEST_COUNTER, resource) + ":" +
                               retrievePropertyValue(StatisticsConstants.GLOBAL_RESPONSE_COUNTER, resource) + ":" +
                               retrievePropertyValue(StatisticsConstants.GLOBAL_FAULT_COUNTER, resource));
*/
//            resource.setProperty(AVERAGE_SYSTEM_RESPONSE_TIME,
//                                 String.valueOf(statisticsUtil.getAvgSystemResponseTime(axisConfig)));
//            resource.setProperty(MAX_SYSTEM_RESPONSE_TIME,
//                                 String.valueOf(statisticsUtil.getMaxSystemResponseTime(axisConfig)));
//            resource.setProperty(MIN_SYSTEM_RESPONSE_TIME,
//                                 String.valueOf(statisticsUtil.getMinSystemResponseTime(axisConfig)));
//            resource.setProperty(StatisticsConstants.GLOBAL_CURRENT_INVOCATION_RESPONSE_TIME,
//                                 String.valueOf(statisticsUtil.getCurrentSystemResponseTime(axisConfig)));
//            resource.setProperty(StatisticsConstants.GLOBAL_CURRENT_RESPONSE_COUNTER,
//                                 String.valueOf(statisticsUtil.getCurrentSystemResponseCount(axisConfig)));
//            resource.setProperty(StatisticsConstants.GLOBAL_CURRENT_REQUEST_COUNTER,
//                                 String.valueOf(statisticsUtil.getCurrentSystemRequestCount(axisConfig)));
//            resource.setProperty(StatisticsConstants.GLOBAL_CURRENT_FAULT_COUNTER,
//                                 String.valueOf(statisticsUtil.getCurrentSystemFaultCount(axisConfig)));

            registry.put(registryPath, resource);

        } catch (Exception e) {
            int tenantId = getTenantId(axisConfig);
            log.error("Error persisting system statistics for tenant : " + tenantId, e);
            throw e;
        }
    }

    /**
     * Persist Stats for the given service
     *
     * @param axisService - service to persist stat
     * @throws Exception - On Error
     */
    public static void persistStatisticsForService(AxisService axisService) throws Exception {
        Registry registry = getRegistry(axisService.getAxisConfiguration());
        String registryPath = getRegistryPathForService(axisService);
        SystemStatisticsUtil statisticsUtil = new SystemStatisticsUtil();
        Resource resource;

        try {

            if (registry.resourceExists(registryPath)) {
                resource = registry.get(registryPath);
            } else {
                resource = registry.newResource();
            }
            ServiceStatistics serviceStatistics = statisticsUtil.getServiceStatistics
                    (axisService);
            resource.setProperty(
                    StatisticsConstants.IN_OPERATION_COUNTER,
                    String.valueOf(serviceStatistics.getTotalRequestCount()));
            resource.setProperty(
                    StatisticsConstants.OUT_OPERATION_COUNTER,
                    String.valueOf(serviceStatistics.getTotalResponseCount()));
            resource.setProperty(
                    StatisticsConstants.OPERATION_FAULT_COUNTER,
                    String.valueOf(serviceStatistics.getTotalFaultCount()));
/*
            System.out.println("YYYYYYYYYYYY persist stats for Service " +axisService.getName()+ " -" +
                               retrievePropertyValue(StatisticsConstants.IN_OPERATION_COUNTER, resource) + ":" +
                               retrievePropertyValue(StatisticsConstants.OUT_OPERATION_COUNTER, resource) + ":" +
                               retrievePropertyValue(StatisticsConstants.OPERATION_FAULT_COUNTER, resource));
*/
            /*resource.setProperty(
                    StatisticsConstants.SERVICE_RESPONSE_TIME_PROCESSOR,
                    String.valueOf(serviceStatistics.getAvgResponseTime()));
            resource.setProperty(
                    StatisticsConstants.SERVICE_RESPONSE_TIME,
                    String.valueOf(serviceStatistics.getCurrentInvocationResponseTime()));
            resource.setProperty(
                    StatisticsConstants.SERVICE_RESPONSE_TIME_PROCESSOR,
                    String.valueOf(serviceStatistics.getMaxResponseTime()));
            resource.setProperty(
                    StatisticsConstants.SERVICE_RESPONSE_TIME_PROCESSOR,
                    String.valueOf(serviceStatistics.getMinResponseTime()));
            resource.setProperty(
                    StatisticsConstants.CURRENT_IN_OPERATION_AND_SERVICE_COUNTER,
                    String.valueOf(serviceStatistics.getCurrentInvocationRequestCount()));
            resource.setProperty(
                    StatisticsConstants.CURRENT_OUT_OPERATION_AND_SERVICE_COUNTER,
                    String.valueOf(serviceStatistics.getCurrentInvocationResponseCount()));
            resource.setProperty(
                    StatisticsConstants.CURRENT_OPERATION_AND_SERVICE_FAULT_COUNTER,
                    String.valueOf(serviceStatistics.getCurrentInvocationFaultCount()));*/

            registry.put(registryPath, resource);
        } catch (Exception e) {
            int tenantId = getTenantId(axisService.getAxisConfiguration());
            log.error("Error persisting system statistics for tenant : " + tenantId, e);
            throw e;
        }
    }

    /**
     * persist stats for operation
     *
     * @param axisOperation  -operation to persist stats
     * @throws Exception - On Error
     */
    public static void persistStatisticsForOperation(AxisOperation axisOperation) throws Exception {
        Registry registry = getRegistry(axisOperation.getAxisConfiguration());
        String registryPath = getRegistryPathForOperation(axisOperation);
        SystemStatisticsUtil statisticsUtil = new SystemStatisticsUtil();
        Resource resource;

        try {
            if (registry.resourceExists(registryPath)) {
                resource = registry.get(registryPath);
            } else {
                resource = registry.newResource();
            }
            OperationStatistics operationStatistics = statisticsUtil.getOperationStatistics
                    (axisOperation);

            resource.setProperty(
                    StatisticsConstants.IN_OPERATION_COUNTER,
                    String.valueOf(operationStatistics.getTotalRequestCount()));
            resource.setProperty(
                    StatisticsConstants.OUT_OPERATION_COUNTER,
                    String.valueOf(operationStatistics.getTotalResponseCount()));
            resource.setProperty(
                    StatisticsConstants.OPERATION_FAULT_COUNTER,
                    String.valueOf(operationStatistics.getTotalFaultCount()));
/*
            System.out.println("YYYYYYY persist stats for Operation " +axisOperation.getName()+ " -" +
                               retrievePropertyValue(StatisticsConstants.IN_OPERATION_COUNTER, resource) + ":" +
                               retrievePropertyValue(StatisticsConstants.OUT_OPERATION_COUNTER, resource) + ":" +
                               retrievePropertyValue(StatisticsConstants.OPERATION_FAULT_COUNTER, resource));
*/
            /*resource.setProperty(
                    StatisticsConstants.OPERATION_RESPONSE_TIME_PROCESSOR,
                    String.valueOf(operationStatistics.getAvgResponseTime()));
            resource.setProperty(
                    StatisticsConstants.OPERATION_RESPONSE_TIME,
                    String.valueOf(operationStatistics.getCurrentInvocationResponseTime()));
            resource.setProperty(
                    StatisticsConstants.OPERATION_RESPONSE_TIME_PROCESSOR,
                    String.valueOf(operationStatistics.getMaxResponseTime()));
            resource.setProperty(StatisticsConstants.OPERATION_RESPONSE_TIME_PROCESSOR,
                                 String.valueOf(operationStatistics.getMinResponseTime()));
            resource.setProperty(
                    StatisticsConstants.CURRENT_IN_OPERATION_AND_SERVICE_COUNTER,
                    String.valueOf(operationStatistics.getCurrentInvocationRequestCount()));
            resource.setProperty(
                    StatisticsConstants.CURRENT_OUT_OPERATION_AND_SERVICE_COUNTER,
                    String.valueOf(operationStatistics.getCurrentInvocationResponseCount()));
            resource.setProperty(
                    StatisticsConstants.CURRENT_OPERATION_AND_SERVICE_FAULT_COUNTER,
                    String.valueOf(operationStatistics.getCurrentInvocationFaultCount()));*/

            registry.put(registryPath, resource);

        } catch (Exception e) {
            int tenantId = getTenantId(axisOperation.getAxisConfiguration());
            log.error("Error persisting system statistics for tenant : " + tenantId, e);
            throw e;
        }

    }

    public static void removePersistedStats(AxisService axisService) {
        Registry registry = getRegistry(axisService.getAxisConfiguration());
        String registryPath = getRegistryPathForService(axisService);
        try {
            if(registry.resourceExists(registryPath)){
                registry.delete(registryPath);
            }
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * @return system registry path
     */
    private static String getRegistryPath() {
        return StatisticsConstants.SYSTEM_REG_PATH;
    }

    private static String getRegistryPathForOperation(AxisOperation axisOperation) {
        return StatisticsConstants.SERVICES_REG_PATH.concat("/").
                concat(axisOperation.getAxisService().getName()).
                concat("/").concat(axisOperation.getName().getLocalPart());
    }

    private static String getRegistryPathForService(AxisService axisService) {
        return StatisticsConstants.SERVICES_REG_PATH.concat("/").
                concat(axisService.getName());
    }

    private static Registry getRegistry(AxisConfiguration axisConfiguration) {
        return SuperTenantCarbonContext.getCurrentContext
                (axisConfiguration).getRegistry(RegistryType.LOCAL_REPOSITORY);
    }

    private static int getTenantId(AxisConfiguration axisConfig) {
        return SuperTenantCarbonContext.getCurrentContext(axisConfig).getTenantId();
    }

    private static Resource getRegistryResource(Registry registry, String registryPath) {
        try {
            if(registry.resourceExists(registryPath)) {
                return registry.get(registryPath);
            } else {
                return null;
            }
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

/*    private static ResponseTimeProcessor getResponseTimeProcessor(AxisConfiguration axisConfig) {
        return (ResponseTimeProcessor) axisConfig.
                getParameter(StatisticsConstants.RESPONSE_TIME_PROCESSOR).getValue();
    }*/

    private static AtomicInteger retrievePropertyValue(String propName, Resource resource) {
        AtomicInteger counter = null;
        if (resource != null) {
            String statCount = resource.getProperty(propName);
            if (statCount != null && !"".equals(statCount)) {
                try {
                    counter = new AtomicInteger(Integer.parseInt(statCount));
                } catch (NumberFormatException e) {
                    //ignore error and set the counter to zero
                }
            }
        }
        if(counter == null){
            counter = new AtomicInteger(0);
        }
        return counter;
    }

    public static void setProperty(String propName, AxisConfiguration axisConfig,
                                   Resource resource){
        try {
            Parameter parameter = new Parameter();
            parameter.setName(propName);
            parameter.setValue(retrievePropertyValue(propName, resource));
/*
            System.out.println("XXXXXXXXXXXXXXXXXXX set property for system,"+ propName +":"+
                               retrievePropertyValue(propName, resource));
*/
            axisConfig.addParameter(parameter);
        } catch (AxisFault axisFault) {
            log.error("Error adding statistics property " + propName + " into axisConfig.",
                      axisFault);
        }
    }

    private static void setProperty(String propName, AxisService axisService,
                                    Resource resource) {
        try {
            Parameter parameter = new Parameter();
            parameter.setName(propName);
            parameter.setValue(retrievePropertyValue(propName, resource));
/*
            System.out.println("XXXXXXXXXX set property for service " + axisService.getName()+"- "+
                               propName +":"+  retrievePropertyValue(propName, resource));
*/
            axisService.addParameter(parameter);
        } catch (AxisFault axisFault) {
            log.error("Error adding statistics property " + propName + " into service " +
                      axisService.getName(), axisFault);
        }
    }

    private static void setProperty(String propName, AxisOperation axisOperation,
                                    Resource resource) {
        try {
            Parameter parameter = new Parameter();
            parameter.setName(propName);
            parameter.setValue(retrievePropertyValue(propName, resource));
/*
            System.out.println("XXXXX set property for operation " + axisOperation.getName()
                    .getLocalPart() + "- " + propName +":"+ retrievePropertyValue(propName, resource));
*/
            axisOperation.addParameter(parameter);
        } catch (AxisFault axisFault) {
            log.error("Error adding statistics property " + propName + " into axisOperation " +
                      axisOperation.getName().getLocalPart(), axisFault);
        }
    }
}
