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

    /**
         * Retrieves the list of names of the in only dataservices operations specified in the dbs.
         *
         * @param dbsContent dbs content as a string
         * @return list of names of the in only dataservice operations.
         */
        public static List<String> getOutOnlyOperationsList(String dbsContent) {
            List<String> outOnlyOperations = new ArrayList<String>();
            Document doc = getDocument(dbsContent);
            Element root = doc.getDocumentElement();
            NodeList elements = root.getChildNodes();
            Map<String, Node> operations = new HashMap<String, Node>();
            Map<String, Node> queries = new HashMap<String, Node>();

            for (int i = 0; i < elements.getLength(); i++) {
                Node n = elements.item(i);
                if (DSTaskConstants.OPERATION.equals(n.getNodeName())) {
                    NamedNodeMap attributes = n.getAttributes();
                    if (attributes != null) {
                        String opName = attributes.getNamedItem(DSTaskConstants.NAME).
                                getFirstChild().getNodeValue();
                        operations.put(opName, n);
                    }
                }
                if (DSTaskConstants.QUERY.equals(n.getNodeName())) {
                    NamedNodeMap attributes = n.getAttributes();
                    if (attributes != null) {
                        String queryName = attributes.getNamedItem(DSTaskConstants.ID).
                                getFirstChild().getNodeValue();
                        queries.put(queryName, n);
                    }
                }
            }

            for (Map.Entry<String, Node> entry : operations.entrySet()) {
                NodeList children = entry.getValue().getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    if (DSTaskConstants.CALL_QUERY.equals(child.getNodeName())) {
                        String queryName = child.getAttributes().getNamedItem(DSTaskConstants.HREF).
                                getFirstChild().getNodeValue();
                        if (!hasInputParams(entry.getKey(), operations) && hasResult(queryName, queries)) {
                            outOnlyOperations.add(entry.getKey());
                        }
                    }
                }
            }
            return outOnlyOperations;
        }

    /**
     * Checks whether a particular data service query has a result.
     *
     * @param queryName name of the query associated with the processed dataservice operation.
     * @param queries   queries specified in the dbs file.
     * @return boolean representing whether the data service query has a result.
     */
    private static boolean hasResult(String queryName, Map<String, Node> queries) {
        Node query = queries.get(queryName);
        if (query != null) {
            NodeList subEls = query.getChildNodes();
            for (int i = 0; i < subEls.getLength(); i++) {
                Node n = subEls.item(i);
                if (DSTaskConstants.RESULT.equals(n.getNodeName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether a particular web service operation mapped into a data service query has input
     * parameters.
     *
     * @param operationName Name of the web service operation to be processed.
     * @param operations List of web service operations defined in the dbs.
     * @return A boolean representing the existence of input parameters inside the operation.
     */
    private static boolean hasInputParams(String operationName, Map<String, Node> operations) {
        Node operation = operations.get(operationName);
        if (operation != null) {
            NodeList subEls = operation.getChildNodes();
            for (int i = 0; i < subEls.getLength(); i++) {
                Node n = subEls.item(i);
                if (DSTaskConstants.WITH_PARAM.equals(n.getNodeName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves the DOM object corresponding to the content of the dbs.
     *
     * @param content dbs content as a string
     * @return DOM object
     */
    private static Document getDocument(String content) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc = null;
        try {
            builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(content));
            doc = builder.parse(is);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

    public static boolean isBoxcarringOp(String opName) {
        return "begin_boxcar".equals(opName) || "end_boxcar".equals(opName)
                || "abort_boxcar".equals(opName);
    }
	
}
