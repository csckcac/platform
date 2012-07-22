/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.dataservices.task;

import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL2Constants;
import org.w3c.dom.*;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskInfo.TriggerInfo;
import org.wso2.carbon.ntask.solutions.webservice.WebServiceCallTask;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * This class represents a utility class for scheduled tasks.  
 */
public class DSTaskUtils {

	public static DSTaskInfo convert(TaskInfo taskInfo) {
		DSTaskInfo dsTaskInfo = new DSTaskInfo();
		dsTaskInfo.setName(taskInfo.getName());
		Map<String, String> taskProps = taskInfo.getProperties();
		dsTaskInfo.setServiceName(taskProps.get(DSTaskConstants.DATA_SERVICE_NAME));
		dsTaskInfo.setOperationName(taskProps.get(DSTaskConstants.DATA_SERVICE_OPERATION_NAME));
		TriggerInfo triggerInfo = taskInfo.getTriggerInfo();
		dsTaskInfo.setCronExpression(triggerInfo.getCronExpression());
		dsTaskInfo.setStartTime(dateToCal(triggerInfo.getStartTime()));
		dsTaskInfo.setEndTime(dateToCal(triggerInfo.getEndTime()));
		dsTaskInfo.setTaskCount(triggerInfo.getRepeatCount());
		dsTaskInfo.setTaskInterval(triggerInfo.getIntervalMillis());
		return dsTaskInfo;
	}
	
	public static TaskInfo convert(DSTaskInfo dsTaskInfo) {
		TriggerInfo triggerInfo = new TriggerInfo();
		triggerInfo.setCronExpression(dsTaskInfo.getCronExpression());
		if (dsTaskInfo.getStartTime() != null) {
		    triggerInfo.setStartTime(dsTaskInfo.getStartTime().getTime());
		}
		if (dsTaskInfo.getEndTime() != null) {
		    triggerInfo.setEndTime(dsTaskInfo.getEndTime().getTime());
		}
		triggerInfo.setIntervalMillis(dsTaskInfo.getTaskInterval());
		triggerInfo.setRepeatCount(dsTaskInfo.getTaskCount());
		Map<String, String> props = new HashMap<String, String>();
		props.put(DSTaskConstants.DATA_SERVICE_NAME, dsTaskInfo.getServiceName());
		props.put(DSTaskConstants.DATA_SERVICE_OPERATION_NAME, dsTaskInfo.getOperationName());
		props.put(WebServiceCallTask.SERVICE_ACTION, "urn:" + dsTaskInfo.getOperationName());
		return new TaskInfo(dsTaskInfo.getName(), DSTask.class.getName(), props, triggerInfo);
	}
	
	private static Calendar dateToCal(Date date) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}
    
    public static String getTenantDomainFromId(int tid) {
        SuperTenantCarbonContext.startTenantFlow();
        SuperTenantCarbonContext.getCurrentContext().setTenantId(tid);
        String tenantDomain = SuperTenantCarbonContext.getCurrentContext().getTenantDomain();
        SuperTenantCarbonContext.endTenantFlow();
        return tenantDomain;
    }

    public static String extractHTTPEPR(AxisService axisService) {
        for (String epr : axisService.getEPRs()) {
            if (epr.startsWith("http:")) {
                return epr;
            }
        }
        return null;
    }

    public static boolean isInOutMEPInOperation(AxisService axisService, String opName) {
        Parameter param = axisService.getParameter(DSTaskConstants.DATA_SERVICE_OBJECT);
        if (param != null) {
            AxisOperation operation = axisService.getOperation(new QName(opName));
            if (WSDL2Constants.MEP_URI_IN_OUT.equals(operation.getMessageExchangePattern()) ||
                    WSDL2Constants.MEP_URI_OUT_ONLY.equals(operation.getMessageExchangePattern())) {
                return true;
            }
        }
        return false;
    }
	
}
