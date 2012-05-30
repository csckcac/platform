package org.wso2.carbon.statistics.persistance;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.wso2.carbon.statistics.StatisticsConstants;
import org.wso2.carbon.statistics.internal.ResponseTimeProcessor;
import org.wso2.carbon.statistics.services.util.OperationStatistics;
import org.wso2.carbon.statistics.services.util.ServiceStatistics;

import java.util.concurrent.atomic.AtomicInteger;

public class StatisticsPersistenceUtils {

    public static void attachStatisticsForService(AxisService axisService) {

    }

    public static void attachStatisticsForOperation(AxisOperation op) {
        // IN operation counter
        Parameter inOpCounter = new Parameter();
        inOpCounter.setName(StatisticsConstants.IN_OPERATION_COUNTER);
        inOpCounter.setValue(new AtomicInteger(0));
        try {
            op.addParameter(inOpCounter);
        } catch (AxisFault ignored) { // will not occur
        }

        // OUT operation counter
        Parameter outOpCounter = new Parameter();
        outOpCounter.setName(StatisticsConstants.OUT_OPERATION_COUNTER);
        outOpCounter.setValue(new AtomicInteger(0));
        try {
            op.addParameter(outOpCounter);
        } catch (AxisFault ignored) { // will not occur
        }

        // Operation response time processor
        Parameter responseTimeProcessor = new Parameter();
        responseTimeProcessor.setName(StatisticsConstants.OPERATION_RESPONSE_TIME_PROCESSOR);
        responseTimeProcessor.setValue(new ResponseTimeProcessor());
        try {
            op.addParameter(responseTimeProcessor);
        } catch (AxisFault axisFault) { // will not occur
        }

    }
}
