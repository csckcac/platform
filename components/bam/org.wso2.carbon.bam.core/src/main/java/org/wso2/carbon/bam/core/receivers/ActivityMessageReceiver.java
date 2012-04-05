


package org.wso2.carbon.bam.core.receivers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.wso2.carbon.bam.common.dataobjects.activity.XPathConfigDO;
import org.wso2.carbon.bam.core.ActivityConstants;
import org.wso2.carbon.bam.core.admin.BAMDataServiceAdmin;
import org.wso2.carbon.bam.common.dataobjects.activity.ActivityDO;
import org.wso2.carbon.bam.common.dataobjects.activity.MessageDO;
import org.wso2.carbon.bam.common.dataobjects.activity.MessageDataDO;
import org.wso2.carbon.bam.common.dataobjects.activity.MessagePropertyDO;
import org.wso2.carbon.bam.common.dataobjects.service.OperationDO;
import org.wso2.carbon.bam.common.dataobjects.activity.PropertyFilterDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServiceDO;
import org.wso2.carbon.bam.core.internal.BAMServiceComponent;
import org.wso2.carbon.bam.core.util.SimpleLRUCache;
import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.bam.util.BAMException;

import java.util.concurrent.ConcurrentHashMap;

/* (1)<soapenv:Body xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelo pe/">
 * (2)  <activitydata:Event xmlns:activitydata="http://wso2.org/ns/2009/09/bam/service/activity/data">
 * (3)    <activitydata:ActivityData>
 * (4)    <activitydata:ServerName>https://10.10 0.1.143:9443</activitydata:ServerName>
 * (5)    <activitydata:ActivityID>prash491-a03a-426 0-b177-c493ab7a2dbb</activitydata:ActivityID>
 * (6)    <activitydata:MessageID />
 * (7)    <activity data:ServiceName>SampleHelloService</activitydata:ServiceName>
 * (8)    <activitydata:OperationName>sayHello</activitydata:OperationName>
 * (9)    <activitydata:ActivityName>hello2 </activitydata:ActivityName>
 * (10)   <activitydata:ActivityDescription>say hello2</activitydata:ActivityDescription>
 * (11)   <activitydata:RemoteIPAddress>127.0.0.1</activitydata:RemoteIPAddress>
 * (12)   <activitydata:UserAgent>%%%%%</activitydata:UserAgent>
 * (13)   </activitydata:ActivityData>
 * (14) </activitydata:Event>
 * (15)</soapenv:Body>
 */

public class ActivityMessageReceiver extends AbstractMessageReceiver {
    private String messageFaultReason = "";

    private Map<String, ServerDO> serverMap = new ConcurrentHashMap<String, ServerDO>();
    private Map<String, ServiceDO> serviceMap = new ConcurrentHashMap<String, ServiceDO>();
    private Map<String, OperationDO> operationMap = new ConcurrentHashMap<String, OperationDO>();
    private Map<String, ActivityDO> activityMap = new SimpleLRUCache<String, ActivityDO>(100);

    /**
     * When first time message comes to a system, service, operation will be
     * added.(bam persistance manager will take care about it, if not add to DB
     */
    protected void invokeBusinessLogic(MessageContext messageContext) throws AxisFault {

        BAMServiceComponent.getActivityEventQueue().enqueue(messageContext);

 /*       BAMDataServiceAdmin dsAdmin = new BAMDataServiceAdmin();
        SOAPBody body = messageContext.getEnvelope().getBody();
        Iterator itr = body.getChildren(); // Retrieving an empty body message
        if (itr.hasNext()) {
            QName aidEventQname = new QName(ActivityConstants.ACTIVITY_NS_URI, ActivityConstants.ACTIVITY_ELEMENT_EVENT);
            OMElement aidEventElement = messageContext.getEnvelope().getBody().getFirstChildWithName(aidEventQname);

            if (aidEventElement != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Activity Event has been received" + aidEventElement);
                }

                QName activityQname = new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                ActivityConstants.ACTIVITY_ELEMENT_ACTIVITY_DATA);

                Iterator iterator = aidEventElement.getChildrenWithName(activityQname);
                while (iterator.hasNext()) {
                    OMElement activityElement = (OMElement) iterator.next();
                    ActivityData data = processActivityElement(activityElement);
                    if (data != null) {

                        if ((data.getXPathEvaluations() != null && !data.getXPathEvaluations().isEmpty())) {
                            storeActivity(data, dsAdmin);
                            storeMessage(data, dsAdmin);
                            storeMessageData(data, dsAdmin);
                            storeProperties(data, dsAdmin);

                            for (PropertyFilterData filterData : data.getXPathEvaluations().keySet()) {
                                storeXPathData(filterData, dsAdmin);
                            }
                            storeXPathProperties(data, dsAdmin);
                        }
                        // For message status,event comes from observer
                        else if (data.getRequestMessageStatus() != null) {

                            if (!data.getRequestMessageStatus().equals("")) {
                                storeActivity(data, dsAdmin);
                                storeMessage(data, dsAdmin);
                                storeMessageStatusData(data, dsAdmin);
                                storeProperties(data, dsAdmin);
                            }

                        }// it is a normal event, fill activity,property,message, message_data tables
                        else {

                            storeActivity(data, dsAdmin);
                            storeMessage(data, dsAdmin);
                            storeMessageData(data, dsAdmin);
                            storeProperties(data, dsAdmin);

                        }

*//*                        if (data.getMessageBody() != null && (data.getXPathEvaluations() != null &&
                                                              !data.getXPathEvaluations().isEmpty())) {

                            storeActivity(data, dsAdmin);
                            storeMessage(data, dsAdmin);
                            storeMessageData(data, dsAdmin);
                            storeProperties(data, dsAdmin);

                            for (PropertyFilterData filterData : data.getXPathEvaluations().keySet()) {
                                storeXPathData(filterData, dsAdmin);
                            }
                            storeXPathProperties(data, dsAdmin);

                        } else if (data.getMessageBody() != null) {

                            storeActivity(data, dsAdmin);
                            storeMessage(data, dsAdmin);
                            storeMessageData(data, dsAdmin);
                            storeProperties(data, dsAdmin);

                        } else if (data.getXPathEvaluations() != null && !data.getXPathEvaluations().isEmpty()) {

                            for (PropertyFilterData filterData : data.getXPathEvaluations().keySet()) {
                                storeXPathData(filterData, dsAdmin);
                            }
                            storeActivity(data, dsAdmin);
                            storeMessage(data, dsAdmin);
                            storeProperties(data, dsAdmin);
                            storeXPathProperties(data, dsAdmin);

                        }
                        // For message status,event comes from observer
                        else if (data.getRequestMessageStatus() != null) {

                            if (!data.getRequestMessageStatus().equals("")) {
                                storeActivity(data, dsAdmin);
                                storeMessage(data, dsAdmin);
                                storeMessageStatusData(data, dsAdmin);
                                storeProperties(data, dsAdmin);
                            }

                        }// it is a normal event, fill activity,property,message table
                        else {

                            storeActivity(data, dsAdmin);
                            storeMessage(data, dsAdmin);
                            storeProperties(data, dsAdmin);

                        }*//*
                    }
                }

*//*                QName propertyFilterQname = new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                      ActivityConstants.PROPERTY_FILTER_DATA_ELEMENT);
                iterator = aidEventElement.getChildrenWithName(propertyFilterQname);

                while (iterator.hasNext()) {
                    OMElement propertyFilterElement = (OMElement) iterator.next();
                    PropertyFilterData data = processPropertyFilterElement(propertyFilterElement);

                    if (data != null) {
                        storeXPathData(data, dsAdmin);
                    }
                }*//*
            }


            if (!"".equals(messageFaultReason)) {
                log.error("BAM ActivityIDMessageReceiver invokeBusinessLogic " + messageFaultReason);
                log.error("BAM ActivityIDMessageReceiver invokeBusinessLogic SOAP Envelope causing the problem"
                          + messageContext.getEnvelope().toString());
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("BAM ActivityIDMessageReceiver invokeBusinessLogic SOAP Envelope "
                      + messageContext.getEnvelope().toString());
        }

        messageFaultReason = "";*/
    }

    /**
     * Stores xpath configuration data to the database. Two tables are affected. BAM_XPATH and BAM_NAMESPACE.
     * If the xpath configuration is already present updates it in BAM_XPATH. All the namespaces in
     * BAM_NAMESPACE relating to particular xpath configuration are deleted and new namespaces are
     * added during an update.
     *
     * @param data    New xpath configuration data to be added to the database
     * @param dsAdmin Dataservice client facade.
     */
    private void storeXPathData(PropertyFilterData data, BAMDataServiceAdmin dsAdmin) {
        try {
            ServerDO serverDO = dsAdmin.getServer(data.getServerName());
            if (serverDO != null && data != null) {
                PropertyFilterDO propertyFilterDO = dsAdmin.getXpathConfiguration(
                        data.getExpressionKey(), serverDO.getId());

                if (propertyFilterDO != null) { // If the xpath configuration is already present in the database we do an update instead of adding.
                    if (!isEqualConfigurations(propertyFilterDO, data)) {
                        try {
                            dsAdmin.updateXpathConfiguration(data.getAlias(), data.getExpressionKey(),
                                                             data.getExpression(), serverDO.getId(),
                                                             propertyFilterDO.getId());
                        } catch (BAMException e) {
                            log.error("Could not update xpath configuration in database..");
                            return;
                        }

                        try {
                            dsAdmin.deleteNamespaceData(propertyFilterDO.getId());
                        } catch (BAMException e) {
                            log.error("Failed to delete existing namespace data in database." +
                                      " New namespace changes won't be written..", e);
                            return;
                        }

                        storeNamespaceData(data, propertyFilterDO, dsAdmin);
                    }

                } else {
                    try {
                        dsAdmin.addXpathConfiguration(data.getAlias(), data.getExpressionKey(),
                                                      data.getExpression(), serverDO.getId());
                    } catch (BAMException e) {
                        log.error("Could not add xpath configuration to the database..");
                        return;
                    }

                    propertyFilterDO = dsAdmin.getXpathConfiguration(data.getExpressionKey(), serverDO.getId());

                    if (propertyFilterDO != null) { // Adds to namespace table.
                        storeNamespaceData(data, propertyFilterDO, dsAdmin);
                    } else {
                        log.error("Error adding xpath configuration to database..");
                    }
                }
            } else {
                log.error("Server is not in the Database " + data.getServerName());
            }
        } catch (BAMException e) {
            log.error("Could not retrieve server from DB ", e);
        }
    }

    /**
     * Adds namespace data relating to a particular xpath configuration to the BAM_NAMESPACE table.
     * Continues adding namespace until end even though some additions may fail.
     *
     * @param data             New xpath configuration data to be added to the database
     * @param propertyFilterDO Xpath configuration data already existing in the database.
     * @param dsAdmin          Dataservice client facade.
     */
    private void storeNamespaceData(PropertyFilterData data,
                                    PropertyFilterDO propertyFilterDO,
                                    BAMDataServiceAdmin dsAdmin) {
        for (String ns : data.getNamespaces()) {
            String[] tokens = ns.split("@");
            if (tokens != null && tokens.length == 2) {
                String prefix = tokens[0];
                String uri = tokens[1];

                try {
                    dsAdmin.addNamespaceData(propertyFilterDO.getId(), prefix, uri);
                } catch (BAMException e) {
                    log.error("Error adding xpath namespace " + ns + "to the database", e);
                }
            }
        }
    }

    private boolean isEqualConfigurations(PropertyFilterDO filterDO,
                                          PropertyFilterData filterData) {
        if (filterDO.getExpression() != null && filterData.getExpression() != null &&
            filterDO.getExpressionKey() != null && filterData.getExpressionKey() != null) {

            if (filterDO.getExpressionKey().trim().equals(filterData.getExpressionKey().trim())) {
                if (filterDO.getExpression().trim().equals(filterData.getExpression().trim())) {

                    if (filterDO.getAlias() != null && filterData.getAlias() != null) {
                        if (!filterDO.getAlias().trim().equals(filterData.getAlias().trim())) {
                            return false;
                        }
                    } else if (filterDO.getAlias() != null || filterData.getAlias() != null) {
                        return false;
                    }

                    if (filterDO.getNamespaces() != null && filterData.getNamespaces() != null) {
                        for (String ns : filterDO.getNamespaces()) {
                            boolean nsFound = false;
                            for (String innerNs : filterData.getNamespaces()) {
                                if (ns.trim().equals(innerNs.trim())) {
                                    nsFound = true;
                                }
                            }

                            if (!nsFound) {
                                return false;
                            }
                        }

                        return true;
                    } else if ((filterDO.getNamespaces() != null || filterData.getNamespaces() != null)) {
                        if (filterDO.getNamespaces() != null && filterDO.getNamespaces().length == 0) {
                            if (filterData.getNamespaces() == null || filterData.getNamespaces().length == 0) {
                                return true;
                            }
                        }

                        if (filterData.getNamespaces() != null && filterData.getNamespaces().length == 0) {
                            if (filterDO.getNamespaces() == null || filterData.getNamespaces().length == 0) {
                                return true;
                            }
                        }

                        return false;

                    } else {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Store Message
     *
     * @param data    Event data
     * @param dsAdmin Data service access
     */
    private void storeMessage(ActivityData data, BAMDataServiceAdmin dsAdmin) {
        if (data.getServiceName() != null && data.getOperationName() != null && data.getActivityId() != null) {
            if (!data.getOperationName().equals("") && !data.getServiceName().equals("")
                && !data.getActivityId().equals("")) {

                MessageDO messageDO;

                // TODO timestamp Fix
                if (data.getTimeStamp() != null) {
                    if (data.getTimeStamp().equals("")) {
                        messageDO = new MessageDO(data.getMessageId(), BAMCalendar.getInstance(Calendar.getInstance())
                                .getBAMTimestamp(), data.getRemoteIPAddress(), data.getUserAgent());
                    } else {
                        messageDO = new MessageDO(data.getMessageId(), data.getTimeStamp(), data.getRemoteIPAddress(),
                                                  data.getUserAgent());
                    }
                } else {
                    messageDO = new MessageDO(data.getMessageId(), BAMCalendar.getInstance(Calendar.getInstance())
                            .getBAMTimestamp(), data.getRemoteIPAddress(), data.getUserAgent());
                }

                // For the tenanting serverDO should be get passing server name
                // and tenant ID
                ServerDO serverDO = null;
                try {
                    if (serverMap.containsKey(data.getServerName())) {
                        serverDO = serverMap.get(data.getServerName());
                    } else {
                        serverDO = dsAdmin.getServer(data.getServerName());
                        if (serverDO == null) {
                            log.error("Server is not in the Database " + data.getServerName());
                            return;
                        } else {
                            serverMap.put(data.getServerName(), serverDO);
                        }
                    }
                    messageDO.setServerId(serverDO.getId());
                } catch (BAMException e) {
                    log.error("Could not retrieve server from DB ", e);
                }

                ServiceDO serviceDO = null;
                try {
                    String key = messageDO.getServerId() + "-" + data.getServiceName();
                    if (serviceMap.containsKey(key)) {
                        serviceDO = serviceMap.get(key);
                    } else {
                        serviceDO = dsAdmin.getService(messageDO.getServerId(), data.getServiceName());
                        if (serviceDO == null) {
                            serviceDO = new ServiceDO();
                            serviceDO.setServerID(serverDO.getId());
                            serviceDO.setName(data.getServiceName());
                            dsAdmin.addService(serviceDO);
                            serviceDO = dsAdmin.getService(messageDO.getServerId(), data.getServiceName());
                        }
                        serviceMap.put(key, serviceDO);
                    }
                    messageDO.setServiceId(serviceDO.getId());
                } catch (BAMException e) {
                    log.error("Could not retrieve service from DB ", e);
                }

                OperationDO operationDO;
                try {
                    String key = messageDO.getServiceId() + "-" + data.getOperationName();
                    if (operationMap.containsKey(key)) {
                        operationDO = operationMap.get(key);
                    } else {
                        operationDO = dsAdmin.getOperation(messageDO.getServiceId(), data.getOperationName());
                        if (operationDO == null) {
                            operationDO = new OperationDO();
                            operationDO.setServiceID(serviceDO.getId());
                            operationDO.setName(data.getOperationName());
                            dsAdmin.addOperation(operationDO);
                            operationDO = dsAdmin.getOperation(messageDO.getServiceId(), data.getOperationName());
                        }
                        operationMap.put(key, operationDO);
                    }
                    messageDO.setOperationId(operationDO.getOperationID());

                } catch (BAMException e) {
                    log.error("Could not retrieve operation from DB ", e);
                }

                try {
                    ActivityDO activity = null;
                    if (activityMap.containsKey(data.getActivityId())) {
                        activity = activityMap.get(data.getActivityId());
                    } else {
                        activity = dsAdmin.getActivityForActivityID(data.getActivityId());
                        if (activity == null) {
                            activity = new ActivityDO();
                            activity.setActivityId(data.getActivityId());
                            activity.setName(data.getActivityName());
                            activity.setDescription(data.getActivityDescription());
                            dsAdmin.addActivity(activity);
                            activity = dsAdmin.getActivityForActivityID(data.getActivityId());
                        }
                        activityMap.put(data.getActivityId(), activity);
                    }
                    messageDO.setActivityKeyId(activity.getActivityKeyId());
                } catch (BAMException e) {
                    log.error("Could not retrieve activity from DB ", e);
                }

                try {
                    MessageDO msg = dsAdmin.getMessage(data.getMessageId(), messageDO.getOperationId(), messageDO
                            .getActivityKeyId());
                    if (msg != null) {
                        messageDO.setMessageKeyId(msg.getMessageKeyId());
                    } else {
                        dsAdmin.addMessageData(messageDO);
                        if (log.isDebugEnabled()) {
                            log.debug("Message is saved");
                        }
                    }
                } catch (BAMException e) {
                    log.error("Error updating Message statistics data for server " + data.getServerName() + " service "
                              + data.getServiceName() + " operation " + data.getOperationName()
                              + " from eventing message receiver " + e.getLocalizedMessage());
                }
            } else {
                messageFaultReason = "Required element is empty ;\n Service :" + data.getServiceName()
                                     + "\n Operation:" + data.getOperationName() + "\n ActivityID:" + data.getActivityId();
            }
        } else {
            messageFaultReason = "Required element is null ;\n Service :" + data.getServiceName() + "\n Operation:"
                                 + data.getOperationName() + "\n ActivityID:" + data.getActivityId();
        }
    }

    /**
     * Store Activity
     *
     * @param data    Event data
     * @param dsAdmin Data service access
     */
    private void storeActivity(ActivityData data, BAMDataServiceAdmin dsAdmin) {

        ActivityDO activityDO;

        if (data.getActivityId() != null) {
            if (!data.getActivityId().equals("")) {
                activityDO = new ActivityDO(data.getActivityId(), data.getActivityName(), data.getActivityDescription());
                ActivityDO act;

                try {
                    act = dsAdmin.getActivityForActivityID(data.getActivityId());
                    if (act != null) {
                        //NOTE: Activity name & description never get changed. Hence no need to call update
                        //method
                        //TODO: if this update method is needed, implement dirty pattern first to check if the
                        //record in the database really needs to be updated.

                        //    dsAdmin.updateActivity(data.getActivityName(), data.getActivityDescription(),
                        //            act.getActivityKeyId());
                    } else {
                        try {
                            dsAdmin.addActivityData(activityDO);
                            if (log.isDebugEnabled()) {
                                log.debug("Activity is saved");
                            }
                            // After adding to the DB set the activityKeyID.
                            act = dsAdmin.getActivityForActivityID(data.getActivityId());
                        } catch (Exception ignore) {
                            log.error("Recovered successfully from race condition - trying to add the same"
                                      + " activity from two events.");
                        }
                    }
                    activityDO.setActivityKeyId(act.getActivityKeyId());
                } catch (Exception e) {
                    log.error("Couldn't add activity  : ", e);
                }
            }
        }
    }

    /**
     * Process activity event
     *
     * @param activityElement Activity data root element
     * @return Event data encapsulated in ActivityData bean
     */

    private ActivityData processActivityElement(OMElement activityElement) {
        String activityId;
        String messageId;
        String operationName;
        String serverName;
        String serviceName;
        String activityName;
        String activityDescription;
        String userAgent;
        String remoteIPAddress;
        String messageDirection;
        String messageBody;
        String timeStamp;
        String requestMessageStatus;
        String responseMessageStatus;
        String outMessageID;

        Map<PropertyFilterData, String> xpathEvaluations = new HashMap<PropertyFilterData, String>();
        Map<String, String> activityProperties = new HashMap<String, String>();

        ActivityData data = new ActivityData();
        if (activityElement != null) {

            serverName = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                               ActivityConstants.ACTIVITY_ELEMENT_SERVER_NAME), activityElement);

            messageId = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                              ActivityConstants.MESSAGE_ID), activityElement);

            activityId = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                               ActivityConstants.ACTIVITY_ID_STATISTICS_DATA), activityElement);

            activityName = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                                 ActivityConstants.ACTIVITY_ELEMENT_ACTIVITY_NAME), activityElement);

            activityDescription = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                                        ActivityConstants.ACTIVITY_ELEMENT_ACTIVITY_DESCRIPTION), activityElement);

            remoteIPAddress = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                                    ActivityConstants.ACTIVITY_ELEMENT_REMOTE_IP_ADDRESS), activityElement);

            userAgent = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                              ActivityConstants.ACTIVITY_ELEMENT_USER_AGENT), activityElement);

            serviceName = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                                ActivityConstants.ACTIVITY_ELEMENT_SERVICE_NAME), activityElement);

            operationName = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                                  ActivityConstants.ACTIVITY_ELEMENT_OPERATION_NAME), activityElement);

            messageDirection = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                                     ActivityConstants.ACTIVITY_ELEMENT_MESSAGE_DIRECTION), activityElement);

            messageBody = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                                ActivityConstants.ACTIVITY_ELEMENT_MESSAGE_BODY), activityElement);

            timeStamp = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                              ActivityConstants.ACTIVITY_ELEMENT_TIMESTAMP), activityElement);

            OMElement xpathExpressions = activityElement.getFirstChildWithName(new QName(
                    ActivityConstants.ACTIVITY_NS_URI, ActivityConstants.ACTIVITY_ELEMENT_XPATH_EXPRESSIONS));

            if (xpathExpressions != null) {
                Iterator xpathExpressionItr = xpathExpressions.getChildrenWithName(
                        new QName(ActivityConstants.ACTIVITY_NS_URI,
                                  ActivityConstants.ACTIVITY_ELEMENT_XPATH_EXPRESSION));

                while (xpathExpressionItr.hasNext()) {
                    OMElement xpathExpression = (OMElement) xpathExpressionItr.next();
                    String expressionKey = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                                                 ActivityConstants.XPATH_EXPRESSION_KEY), xpathExpression);

                    String xpath = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                                         ActivityConstants.ACTIVITY_XPATH_EXPRESSION), xpathExpression);

                    String alias = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                                         ActivityConstants.PROPERTY_FILTER_ALIAS), xpathExpression);

                    OMElement namespaces = activityElement.getFirstChildWithName(new QName(
                            ActivityConstants.ACTIVITY_NS_URI, ActivityConstants.PROPERTY_FILTER_NAMESPACES));

                    List<String> nsList = new ArrayList<String>();
                    if (namespaces != null) {
                        Iterator namespaceItr = xpathExpressions.getChildrenWithName(
                                new QName(ActivityConstants.ACTIVITY_NS_URI,
                                          ActivityConstants.PROPERTY_FILTER_NAMESPACE));


                        while (namespaceItr.hasNext()) {
                            OMElement namespace = (OMElement) namespaceItr.next();
                            String ns = namespace.getText();
                            nsList.add(ns);
                        }
                    }

                    PropertyFilterData propertyFilterData = new PropertyFilterData();
                    propertyFilterData.setServerName(serverName);
                    propertyFilterData.setExpressionKey(expressionKey);
                    propertyFilterData.setExpression(xpath);
                    propertyFilterData.setAlias(alias);
                    propertyFilterData.setNamespaces(nsList.toArray(new String[nsList.size()]));

                    String xpathValue = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                                              ActivityConstants.XPATH_VALUE), xpathExpression);
                    xpathEvaluations.put(propertyFilterData, xpathValue);

                }
            }

            OMElement properties = activityElement.getFirstChildWithName(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                                                   ActivityConstants.ACTIVITY_ELEMENT_PROPERTIES));
            if (properties != null) {
                Iterator<OMElement> propertiesItr = properties.getChildrenWithName(new QName(
                        ActivityConstants.ACTIVITY_NS_URI, ActivityConstants.ACTIVITY_ELEMENT_PROPERTY));

                while (propertiesItr.hasNext()) {
                    OMElement property = propertiesItr.next();
                    String propertyChild = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                                                 ActivityConstants.ACTIVITY_ELEMENT_PROPERTY_CHILD), property);

                    String propertyChildValue = getDataWithQNameFromElement(new QName(
                            ActivityConstants.ACTIVITY_NS_URI,
                            ActivityConstants.ACTIVITY_ELEMENT_PROPERTY_VALUE), property);

                    activityProperties.put(propertyChild, propertyChildValue);
                }
            }

            outMessageID = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                                 ActivityConstants.ACTIVITY_OUT_MESSAGE_ID), activityElement);

            requestMessageStatus = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                                         ActivityConstants.ACTIVITY_REQUEST_MESSAGE_STATUS), activityElement);

            responseMessageStatus = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                                          ActivityConstants.ACTIVITY_RESPONSE_MESSAGE_STATUS), activityElement);

            if (requestMessageStatus != null) {
                if (requestMessageStatus.equals("0")) {
                    requestMessageStatus = "SUCCESS MESSAGE";
                }
                if (requestMessageStatus.equals("1")) {
                    requestMessageStatus = "FAULTY MESSAGE";
                }
                if (requestMessageStatus.equals("-1")) {
                    requestMessageStatus = "UNDEFINED STATUS";
                }
            }

            if (responseMessageStatus != null) {
                if (responseMessageStatus.equals("0")) {
                    responseMessageStatus = "SUCCESS MESSAGE";
                }
                if (responseMessageStatus.equals("1")) {
                    responseMessageStatus = "FAULTY MESSAGE";
                }
                if (responseMessageStatus.equals("-1")) {
                    responseMessageStatus = "UNDEFINED STATUS";
                }
            }
            data.setServerName(serverName);
            data.setServiceName(serviceName);
            data.setOperationName(operationName);
            data.setMessageId(messageId);
            data.setActivityId(activityId);
            data.setActivityName(activityName);
            data.setActivityDescription(activityDescription);
            data.setUserAgent(userAgent);
            data.setRemoteIPAddress(remoteIPAddress);
            data.setTimeStamp(timeStamp);
            data.setMessageDirection(messageDirection);
            data.setMessageBody(messageBody);
            data.setXPathEvaluations(xpathEvaluations);
            data.setRequestMessageStatus(requestMessageStatus);
            data.setResponseMessageStatus(responseMessageStatus);
            data.setOutMessageID(outMessageID);
            data.setProperties(activityProperties);
        }

        return data;
    }

/*
    private PropertyFilterData processPropertyFilterElement(OMElement propertyFilterElement) {
        String serverName;
        String expressionKey = null;
        String alias = null;
        String expression = null;
        String[] namespaces = null;

        PropertyFilterData data = new PropertyFilterData();
        if (propertyFilterElement != null) {
            serverName = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                               ActivityConstants.ACTIVITY_ELEMENT_SERVER_NAME), propertyFilterElement);

            OMElement xpathExpressionsElement = propertyFilterElement.getFirstChildWithName(new QName(
                    ActivityConstants.ACTIVITY_NS_URI, ActivityConstants.PROPERTY_FILTER_XPATH_EXPRESSIONS));

            if (xpathExpressionsElement != null) {
                OMElement xpathExpressionElement = xpathExpressionsElement.getFirstChildWithName(new QName(
                        ActivityConstants.ACTIVITY_NS_URI, ActivityConstants.PROPERTY_FILTER_XPATH_EXPRESSION));

                if (xpathExpressionElement != null) {
                    expressionKey = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                                          ActivityConstants.PROPERTY_FILTER_EXPRESSION_KEY), xpathExpressionElement);

                    alias = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                                  ActivityConstants.PROPERTY_FILTER_ALIAS), xpathExpressionElement);

                    expression = getDataWithQNameFromElement(new QName(ActivityConstants.ACTIVITY_NS_URI,
                                                                       ActivityConstants.PROPERTY_FILTER_EXPRESSION), xpathExpressionElement);

                    OMElement namespacesElement = xpathExpressionElement.getFirstChildWithName(new QName(
                            ActivityConstants.ACTIVITY_NS_URI, ActivityConstants.PROPERTY_FILTER_NAMESPACES));

                    if (namespacesElement != null) {
                        Iterator iterator = namespacesElement.getChildrenWithName(new QName(
                                ActivityConstants.ACTIVITY_NS_URI, ActivityConstants.PROPERTY_FILTER_NAMESPACE));

                        ArrayList<String> namespacesList = new ArrayList<String>();
                        while (iterator != null && iterator.hasNext()) {
                            OMElement namespaceElement = (OMElement) iterator.next();

                            if (namespaceElement != null) {
                                namespacesList.add(namespaceElement.getText());
                            }
                        }

                        namespaces = namespacesList.toArray(new String[namespacesList.size()]);
                    }
                }
            }

            data.setServerName(serverName);
            data.setAlias(alias);
            data.setExpression(expression);
            data.setExpressionKey(expressionKey);
            data.setNamespaces(namespaces);

            return data;
        }

        return null;

    }
*/

    private String getDataWithQNameFromElement(QName qName, OMElement element) {
        OMElement dataElement = element.getFirstChildWithName(qName);

        String value = null;
        if (dataElement != null) {
            value = dataElement.getText();
        }
        return value;
    }

    /*
     * Store message Body+timestamp details @ DB
     */

    private void storeMessageData(ActivityData data, BAMDataServiceAdmin dsAdmin) {
        if (data.getServiceName() != null && data.getOperationName() != null && data.getActivityId() != null) {
            if (!data.getOperationName().equals("") && !data.getServiceName().equals("")
                && !data.getActivityId().equals("")) {

                ServiceDO service;
                OperationDO operation;
                ActivityDO activity;
                MessageDataDO messageDataDO;

                if (data.getTimeStamp() != null) {
                    messageDataDO = new MessageDataDO(data.getTimeStamp(), data.getRemoteIPAddress(), data
                            .getMessageDirection(), data.getMessageBody(), data.getRequestMessageStatus(), data
                            .getResponseMessageStatus());

                } else {
                    messageDataDO = new MessageDataDO(
                            BAMCalendar.getInstance(Calendar.getInstance()).getBAMTimestamp(),
                            data.getRemoteIPAddress(), data.getMessageDirection(), data.getMessageBody(),
                            data.getRequestMessageStatus(), data.getResponseMessageStatus());
                }

                // When first time message comes to a system, service, operation
                // need to be
                // added.
                try {
                    ServerDO serverDO = dsAdmin.getServer(data.getServerName());
                    if (serverDO != null) {
                        int serverId = serverDO.getId();
                        messageDataDO.setServerId(serverId);
                    } else {
                        log.error("Server is not in the Database" + data.getServerName());
                    }
                } catch (BAMException e) {
                    log.error("Could not retrieve server from DB ", e);
                }

                try {
                    if (dsAdmin.getService(messageDataDO.getServerId(), data.getServiceName()) == null) {
                        service = new ServiceDO();

                        ServerDO serverDO = dsAdmin.getServer(data.getServerName());
                        if (serverDO != null) {
                            int serverId = serverDO.getId();
                            service.setServerID(serverId);
                            service.setName(data.getServiceName());
                            dsAdmin.addService(service);
                            messageDataDO.setServiceId(dsAdmin.getService(messageDataDO.getServerId(),
                                                                          data.getServiceName()).getId());
                        }

                    } else {
                        messageDataDO.setServiceId(dsAdmin.getService(messageDataDO.getServerId(),
                                                                      data.getServiceName()).getId());
                    }
                } catch (BAMException e) {
                    log.error("Could not retrieve service from DB ", e);
                }

                try {
                    if (dsAdmin.getOperation(messageDataDO.getServiceId(), data.getOperationName()) == null) {
                        operation = new OperationDO();
                        ServiceDO serviceDO = dsAdmin.getService(messageDataDO.getServerId(), data.getServiceName());
                        if (serviceDO != null) {
                            operation.setServiceID(serviceDO.getId());
                            operation.setName(data.getOperationName());
                            dsAdmin.addOperation(operation);
                            messageDataDO.setOperationId(dsAdmin.getOperation(messageDataDO.getServiceId(),
                                                                              data.getOperationName()).getOperationID());
                        } else {
                            serviceDO = new ServiceDO();
                            serviceDO.setServerID(messageDataDO.getServerId());
                            serviceDO.setName(data.getServiceName());
                            dsAdmin.addService(serviceDO);

                            messageDataDO.setServiceId(dsAdmin.getService(messageDataDO.getServerId(),
                                                                          data.getServiceName()).getId());

                            operation.setServiceID(messageDataDO.getServiceId());
                            operation.setName(data.getOperationName());
                            dsAdmin.addOperation(operation);

                            messageDataDO.setOperationId(dsAdmin.getOperation(messageDataDO.getServiceId(),
                                                                              data.getOperationName()).getOperationID());

                        }
                    } else {
                        messageDataDO.setOperationId(dsAdmin.getOperation(messageDataDO.getServiceId(),
                                                                          data.getOperationName()).getOperationID());
                    }
                } catch (BAMException e) {
                    log.error("Could not retrieve operation from DB ", e);
                }

                checkForActivityExistense(data, dsAdmin, messageDataDO);

                try {
                    MessageDO msg = dsAdmin.getMessage(data.getMessageId(), messageDataDO.getOperationId(),
                                                       messageDataDO.getActivityKeyId());
                    if (msg != null) {
                        messageDataDO.setMessageKeyId(msg.getMessageKeyId());
                    } else {
                        MessageDO mesg = new MessageDO();
                        mesg.setMessageId(data.getMessageId());
                        mesg.setActivityKeyId(messageDataDO.getActivityKeyId());
                        mesg.setOperationId(messageDataDO.getOperationId());
                        mesg.setTimestamp(data.getTimeStamp());
                        mesg.setIPAddress(data.getRemoteIPAddress());
                        mesg.setUserAgent(data.getUserAgent());

                        //TODO: Data Services supports returning the ID
                        dsAdmin.addMessage(mesg);
                        messageDataDO.setMessageKeyId(dsAdmin.getMessage(data.getMessageId(),
                                                                         messageDataDO.getOperationId(),
                                                                         messageDataDO.getActivityKeyId())
                                .getMessageKeyId());
                    }
                } catch (BAMException e) {
                    log.error("Error updating Message statistics data for server " + data.getServerName() + " service "
                              + data.getServiceName() + " operation " + data.getOperationName()
                              + " from eventing message receiver " + e.getLocalizedMessage());
                }

                // store to the BAM MessageData table
                try {
                    MessageDataDO msgDat = dsAdmin.getMessageDataForActivityKeyIDandMessageKeyID(
                            messageDataDO.getMessageKeyId(), messageDataDO.getActivityKeyId());
                    if (msgDat != null) {
                        messageDataDO.setMessageDataKeyId(msgDat.getMessageDataKeyId());
//                        dsAdmin.updateMessageDump(data.getMessageBody(), data.getMessageDirection(),
//                                data.getRemoteIPAddress(), msgDat.getMessageDataKeyId());
                    } else {
                        if (data.getMessageDirection() != null) {
                            dsAdmin.addMessageDataDump(messageDataDO, data.getMessageDirection());
                            messageDataDO.setMessageDataKeyId(dsAdmin.getMessageDataForActivityKeyIDandMessageKeyID(
                                    messageDataDO.getMessageKeyId(), messageDataDO.getActivityKeyId())
                                                                      .getMessageDataKeyId());
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("Message Data is saved");
                        }
                    }
                } catch (BAMException e) {
                    log.error("Could not dump the full message to DB", e);
                }

            } else {
                messageFaultReason = "Required element is empty ;\n Service :" + data.getServiceName()
                                     + "\n Operation :" + data.getOperationName() + "\n ActivityID :" + data.getActivityId();
            }
        } else {
            messageFaultReason = "Required element is null ; \n Service :" + data.getServiceName() + "\n Operation :"
                                 + data.getOperationName() + "\n ActivityID :" + data.getActivityId();
        }
    }

    // store status

    private void storeMessageStatusData(ActivityData data,
                                        BAMDataServiceAdmin dsAdmin) {
        if (data.getServiceName() != null && data.getOperationName() != null && data.getActivityId() != null) {
            if (!data.getOperationName().equals("") && !data.getServiceName().equals("")
                && !data.getActivityId().equals("")) {

                ServiceDO service;
                OperationDO operation;
                ActivityDO activity;
                MessageDataDO messageDataDO;

                if (data.getTimeStamp() != null) {
                    if (data.getTimeStamp().equals("")) {
                        messageDataDO = new MessageDataDO(BAMCalendar.getInstance(Calendar.getInstance())
                                .getBAMTimestamp(), data.getRemoteIPAddress(), data.getMessageDirection(),
                                                          data.getMessageBody(), data.getRequestMessageStatus(),
                                                          data.getResponseMessageStatus());
                    } else {
                        messageDataDO = new MessageDataDO(data.getTimeStamp(), data.getRemoteIPAddress(),
                                                          data.getMessageDirection(), data.getMessageBody(),
                                                          data.getRequestMessageStatus(), data.getResponseMessageStatus());
                    }
                } else {
                    messageDataDO = new MessageDataDO(
                            BAMCalendar.getInstance(Calendar.getInstance()).getBAMTimestamp(),
                            data.getRemoteIPAddress(), data.getMessageDirection(), data.getMessageBody(),
                            data.getRequestMessageStatus(), data.getResponseMessageStatus());
                }

                // When first time message comes to a system, service, operation
                // need to be
                // added.
                try {
                    ServerDO serverDO = dsAdmin.getServer(data.getServerName());
                    if (serverDO != null) {
                        messageDataDO.setServerId(serverDO.getId());
                    } else {
                        log.error("Server is not in the Database" + data.getServerName());
                    }
                } catch (BAMException e) {
                    log.error("Could not retrieve server from DB ", e);
                }
                try {
                    if (dsAdmin.getService(messageDataDO.getServerId(), data.getServiceName()) == null) {
                        service = new ServiceDO();
                        ServerDO serverDO = dsAdmin.getServer(data.getServerName());
                        if (serverDO != null) {
                            service.setServerID(serverDO.getId());
                            service.setName(data.getServiceName());
                            dsAdmin.addService(service);
                            messageDataDO.setServiceId(dsAdmin.getService(messageDataDO.getServerId(),
                                                                          data.getServiceName()).getId());
                        }

                    } else {
                        messageDataDO.setServiceId(dsAdmin.getService(messageDataDO.getServerId(),
                                                                      data.getServiceName()).getId());
                    }
                } catch (BAMException e) {
                    log.error("Could not retrieve service from DB ", e);
                }
                try {
                    if (dsAdmin.getOperation(messageDataDO.getServiceId(), data.getOperationName()) == null) {
                        operation = new OperationDO();
                        ServiceDO serviceDO = dsAdmin.getService(messageDataDO.getServerId(), data.getServiceName());
                        if (serviceDO != null) {
                            operation.setServiceID(serviceDO.getId());
                            operation.setName(data.getOperationName());
                            dsAdmin.addOperation(operation);
                            messageDataDO.setOperationId(dsAdmin.getOperation(messageDataDO.getServiceId(),
                                                                              data.getOperationName()).getOperationID());
                        } else {
                            serviceDO = new ServiceDO();
                            serviceDO.setServerID(messageDataDO.getServerId());
                            serviceDO.setName(data.getServiceName());
                            dsAdmin.addService(serviceDO);

                            messageDataDO.setServiceId(dsAdmin.getService(messageDataDO.getServerId(),
                                                                          data.getServiceName()).getId());

                            operation.setServiceID(messageDataDO.getServiceId());
                            operation.setName(data.getOperationName());
                            dsAdmin.addOperation(operation);

                            messageDataDO.setOperationId(dsAdmin.getOperation(messageDataDO.getServiceId(),
                                                                              data.getOperationName()).getOperationID());
                        }
                    } else {
                        messageDataDO.setOperationId(dsAdmin.getOperation(messageDataDO.getServiceId(),
                                                                          data.getOperationName()).getOperationID());
                    }
                } catch (BAMException e) {
                    log.error("Could not retrieve operation from DB ", e);
                }
                checkForActivityExistense(data, dsAdmin, messageDataDO);
                // this is for mediation observer event(it publishes
                // req+response @ single
                // event)
                try {
                    MessageDO inMsg = dsAdmin.getMessage(data.getMessageId(), messageDataDO.getOperationId(),
                                                         messageDataDO.getActivityKeyId());
                    if (inMsg != null) {
                        messageDataDO.setMessageKeyId(inMsg.getMessageKeyId());
                    } else {
                        MessageDO mesg_in = new MessageDO();
                        mesg_in.setMessageId(data.getMessageId());
                        mesg_in.setActivityKeyId(messageDataDO.getActivityKeyId());
                        mesg_in.setOperationId(messageDataDO.getOperationId());
                        mesg_in.setTimestamp(data.getTimeStamp());
                        mesg_in.setIPAddress(data.getRemoteIPAddress());
                        mesg_in.setUserAgent(data.getUserAgent());

                        //TODO: Data Services supports returning the ID
                        dsAdmin.addMessage(mesg_in);
                        messageDataDO.setMessageKeyId(dsAdmin.getMessage(data.getMessageId(),
                                                                         messageDataDO.getOperationId(),
                                                                         messageDataDO.getActivityKeyId())
                                .getMessageKeyId());
                    }
                } catch (BAMException e) {
                    log.error("Error updating Message statistics data for server " + data.getServerName() + " service "
                              + data.getServiceName() + " operation " + data.getOperationName()
                              + " from eventing message receiver " + e.getLocalizedMessage());
                }
                // store to the BAM MessageData table
                try {
                    MessageDataDO msgDat_in = dsAdmin.getMessageDataForActivityKeyIDandMessageKeyID(
                            messageDataDO.getMessageKeyId(), messageDataDO.getActivityKeyId());
                    if (msgDat_in != null) {
                        messageDataDO.setMessageDataKeyId(msgDat_in.getMessageDataKeyId());
                        dsAdmin.updateMessageStatus(data.getRequestMessageStatus(), msgDat_in.getMessageDataKeyId());
                    } else {
                        String direction = "Request";
                        dsAdmin.addMessageDataDump(messageDataDO, direction);
                    }
                } catch (BAMException e) {
                    log.error("Could not dump the full message to DB", e);
                }
                try {
                    String out_msg_id = data.getOutMessageID();
                    if (out_msg_id != null && !"".equals(out_msg_id)) {
                        MessageDO outMsg = dsAdmin.getMessage(data.getOutMessageID(), messageDataDO.getOperationId(),
                                                              messageDataDO.getActivityKeyId());
                        if (outMsg != null) {
                            messageDataDO.setMessageKeyId(outMsg.getMessageKeyId());
                        } else {
                            MessageDO mesg_out = new MessageDO();
                            mesg_out.setMessageId(data.getOutMessageID());
                            mesg_out.setActivityKeyId(messageDataDO.getActivityKeyId());
                            mesg_out.setOperationId(messageDataDO.getOperationId());
                            mesg_out.setTimestamp(data.getTimeStamp());
                            mesg_out.setIPAddress(data.getRemoteIPAddress());
                            mesg_out.setUserAgent(data.getUserAgent());

                            //TODO: Data Services supports returning the ID
                            dsAdmin.addMessage(mesg_out);
                            messageDataDO.setMessageKeyId(dsAdmin.getMessage(data.getOutMessageID(),
                                                                             messageDataDO.getOperationId(),
                                                                             messageDataDO.getActivityKeyId())
                                    .getMessageKeyId());
                        }
                    }

                } catch (BAMException e) {
                    log.error("Error updating Message statistics data for server " + data.getServerName() + " service "
                              + data.getServiceName() + " operation " + data.getOperationName()
                              + " from eventing message receiver " + e.getLocalizedMessage());
                }
                // store to the BAM MessageData table
                try {
                    MessageDataDO msgDat_out = dsAdmin.getMessageDataForActivityKeyIDandMessageKeyID(
                            messageDataDO.getMessageKeyId(), messageDataDO.getActivityKeyId());
                    if (msgDat_out != null) {
                        messageDataDO.setMessageDataKeyId(msgDat_out.getMessageDataKeyId());
                        dsAdmin.updateMessageStatus(data.getResponseMessageStatus(), msgDat_out.getMessageDataKeyId());
                    } else {
                        String direction = "Response";
                        dsAdmin.addMessageDataDump(messageDataDO, direction);
                    }
                } catch (BAMException e) {
                    log.error("Could not dump the full message to DB", e);
                }
            } else {
                messageFaultReason = "Required element is empty ; \n Service :" + data.getServiceName()
                                     + "\n Operation:" + data.getOperationName() + "\n ActivityID:" + data.getActivityId();
            }
        } else {
            messageFaultReason = "Required element is null ; \n Service :" + data.getServiceName() + "\n Operation:"
                                 + data.getOperationName() + "\n ActivityID:" + data.getActivityId();
        }
    }

    /**
     * @param data
     * @param dsAdmin
     * @param messageDataDO
     */
    private void checkForActivityExistense(ActivityData data,
                                           BAMDataServiceAdmin dsAdmin,
                                           MessageDataDO messageDataDO) {
        ActivityDO activity = null;
        try {
            if (activityMap.containsKey(data.getActivityId())) {
                activity = activityMap.get(data.getActivityId());
            } else {
                if (dsAdmin.getActivityForActivityID(data.getActivityId()) == null) {
                    activity = new ActivityDO();
                    activity.setActivityId(data.getActivityId());
                    activity.setName(data.getActivityName());
                    activity.setDescription(data.getActivityDescription());
                    dsAdmin.addActivity(activity);
                }
                activity = dsAdmin.getActivityForActivityID(data.getActivityId());
                activityMap.put(activity.getActivityId(), activity);
            }
            messageDataDO.setActivityKeyId((activity.getActivityKeyId()));
        } catch (BAMException e) {
            log.error("Could not retrieve activity from DB ", e);
        }
    }

    /**
     * Store activity properties+xpath values
     *
     * @param data    Event data
     * @param dsAdmin Data service access
     */
    private void storeProperties(ActivityData data, BAMDataServiceAdmin dsAdmin) {
        if (data.getServiceName() != null && data.getOperationName() != null && data.getActivityId() != null) {
            if (!data.getOperationName().equals("") && !data.getServiceName().equals("")
                && !data.getActivityId().equals("")) {

                if (data.getProperties() != null && data.getProperties().size() > 0) {
                    MessagePropertyDO messagePropertyDO;
                    String dbtime = null; //need to check the timestamp of the different events

                    messagePropertyDO = new MessagePropertyDO();

                    ServerDO serverDO = null;
                    try {
                        if (serverMap.containsKey(data.getServerName())) {
                            serverDO = serverMap.get(data.getServerName());
                        } else {
                            serverDO = dsAdmin.getServer(data.getServerName());
                            if (serverDO == null) {
                                log.error("Server is not in the Database " + data.getServerName());
                                return;
                            } else {
                                serverMap.put(data.getServerName(), serverDO);
                            }
                        }
                        messagePropertyDO.setServerId(serverDO.getId());
                    } catch (BAMException e) {
                        log.error("Could not retrieve server from DB ", e);
                    }

                    ServiceDO serviceDO = null;
                    try {
                        String key = messagePropertyDO.getServerId() + "-" + data.getServiceName();
                        if (serviceMap.containsKey(key)) {
                            serviceDO = serviceMap.get(key);
                        } else {
                            serviceDO = dsAdmin.getService(messagePropertyDO.getServerId(), data.getServiceName());
                            if (serviceDO == null) {
                                serviceDO = new ServiceDO();
                                serviceDO.setServerID(serverDO.getId());
                                serviceDO.setName(data.getServiceName());
                                dsAdmin.addService(serviceDO);
                                serviceDO = dsAdmin.getService(messagePropertyDO.getServerId(), data.getServiceName());
                            }
                            serviceMap.put(key, serviceDO);
                        }
                        messagePropertyDO.setServiceId(serviceDO.getId());
                    } catch (BAMException e) {
                        log.error("Could not retrieve service from DB ", e);
                    }

                    OperationDO operationDO = null;
                    try {
                        String key = messagePropertyDO.getServiceId() + "-" + data.getOperationName();
                        if (operationMap.containsKey(key)) {
                            operationDO = operationMap.get(key);
                        } else {
                            operationDO = dsAdmin.getOperation(messagePropertyDO.getServiceId(), data.getOperationName());
                            if (operationDO == null) {
                                operationDO = new OperationDO();
                                operationDO.setServiceID(serviceDO.getId());
                                operationDO.setName(data.getOperationName());
                                dsAdmin.addOperation(operationDO);
                                operationDO = dsAdmin.getOperation(messagePropertyDO.getServiceId(), data.getOperationName());
                            }
                            operationMap.put(key, operationDO);
                        }
                        messagePropertyDO.setOperationId(operationDO.getOperationID());

                    } catch (BAMException e) {
                        log.error("Could not retrieve operation from DB ", e);
                    }

                    ActivityDO activity = null;
                    try {
                        if (activityMap.containsKey(data.getActivityId())) {
                            activity = activityMap.get(data.getActivityId());
                        } else {
                            activity = dsAdmin.getActivityForActivityID(data.getActivityId());
                            if (activity == null) {
                                activity = new ActivityDO();
                                activity.setActivityId(data.getActivityId());
                                activity.setName(data.getActivityName());
                                activity.setDescription(data.getActivityDescription());
                                dsAdmin.addActivity(activity);
                                activity = dsAdmin.getActivityForActivityID(data.getActivityId());
                            }
                            activityMap.put(data.getActivityId(), activity);
                        }
                        messagePropertyDO.setActivityKeyId(activity.getActivityKeyId());
                    } catch (BAMException e) {
                        log.error("Could not retrieve activity from DB ", e);
                    }

                    try {
                        MessageDO msg = dsAdmin.getMessage(data.getMessageId(), messagePropertyDO
                                .getOperationId(), messagePropertyDO.getActivityKeyId());
                        if (msg != null) {
                            messagePropertyDO.setMessageKeyId(msg.getMessageKeyId());
                            dbtime = msg.getTimestamp();
                        } else {
                            MessageDO mesg = new MessageDO();
                            if (activity != null) {
                                mesg.setMessageId(data.getMessageId());
                                mesg.setActivityKeyId(messagePropertyDO.getActivityKeyId());
                                mesg.setOperationId(messagePropertyDO.getOperationId());
                                mesg.setTimestamp(data.getTimeStamp());
                                mesg.setIPAddress(data.getRemoteIPAddress());
                                mesg.setUserAgent(data.getUserAgent());

                                //TODO: Data Services supports returning the ID
                                dsAdmin.addMessage(mesg);
                                messagePropertyDO.setMessageKeyId(dsAdmin.getMessage(
                                        data.getMessageId(),
                                        messagePropertyDO
                                                .getOperationId(),
                                        messagePropertyDO
                                                .getActivityKeyId())
                                        .getMessageKeyId());
                                dbtime = data.getTimeStamp();
                            }

                        }
                    } catch (BAMException e) {
                        log.error("Could not retrieve message from DB", e);
                    }

                    try {
                        // MessagePropertyDO property = dsAdmin.getPropertyofMessage(
                        // messagePropertyDO.getMessageKeyId(),
                        // messagePropertyDO.getActivityKeyId(), messagePropertyDO.getKey());
                        // if (property == null) {

                        // process properties and get arraylist out of it
                        Map<String, String> propertyList = new HashMap<String, String>();
                        for (String actProperty : data.getProperties().keySet()) {
                            propertyList.put(actProperty, data.getProperties().get(actProperty));
                        }
                        ArrayList<String> keyList = new ArrayList<String>();
                        ArrayList<String> valueList = new ArrayList<String>();
                        Set<String> keys = propertyList.keySet();
                        for (Iterator i = keys.iterator(); i.hasNext();) {
                            String key = (String) i.next();
                            String value = propertyList.get(key);
                            keyList.add(key);
                            valueList.add(value);
                        }
                        String valueArray[] = new String[keyList.size()];

                        String keyArray[] = new String[keyList.size()];
                        keyList.toArray(keyArray);
                        valueList.toArray(valueArray);
                        messagePropertyDO.setKeyArray(keyArray);
                        messagePropertyDO.setValueArray(valueArray);
                        dsAdmin.addMessageProperty(messagePropertyDO);

                        if (log.isDebugEnabled()) {
                            log.debug("Message Properties are saved");
                        }
                        // }
                    } catch (BAMException e) {
                        log.error("Could not add message property ", e);
                    }

                }
            }
        }
    }

    private void storeXPathProperties(ActivityData data, BAMDataServiceAdmin dsAdmin) {
        if (data.getServiceName() != null && data.getOperationName() != null && data.getActivityId() != null) {
            if (!data.getOperationName().equals("") && !data.getServiceName().equals("")
                && !data.getActivityId().equals("")) {

                if (data.getXPathEvaluations() != null) {
                    ServiceDO service;
                    OperationDO operation;
                    ActivityDO activity;


                    MessagePropertyDO messagePropertyDO = null;

                    messagePropertyDO = new MessagePropertyDO();
                    try {
                        ServerDO serverDO = dsAdmin.getServer(data.getServerName());
                        if (serverDO != null) {
                            messagePropertyDO.setServerId(serverDO.getId());
                        } else {
                            log.error("Server is not in the Database" + data.getServerName());
                        }
                    } catch (BAMException e) {
                        log.error("Could not retrieve server from DB ", e);
                    }

                    try {
                        if (dsAdmin.getService(messagePropertyDO.getServerId(), data.getServiceName()) == null) {
                            service = new ServiceDO();
                            ServerDO serverDO = dsAdmin.getServer(data.getServerName());
                            if (serverDO != null) {
                                service.setServerID(serverDO.getId());
                                service.setName(data.getServiceName());
                                dsAdmin.addService(service);
                                messagePropertyDO.setServiceId(dsAdmin.getService(messagePropertyDO.getServerId(),
                                                                                  data.getServiceName()).getId());
                            }

                        } else {
                            messagePropertyDO.setServiceId(dsAdmin.getService(messagePropertyDO.getServerId(),
                                                                              data.getServiceName()).getId());
                        }
                    } catch (BAMException e) {
                        log.error("Could not retrieve service from DB ", e);
                    }

                    try {
                        if (dsAdmin.getOperation(messagePropertyDO.getServiceId(), data.getOperationName()) == null) {
                            operation = new OperationDO();
                            ServiceDO serviceDO = dsAdmin.getService(messagePropertyDO.getServerId(),
                                                                     data.getServiceName());
                            if (serviceDO != null) {
                                operation.setServiceID(messagePropertyDO.getServiceId());
                                operation.setName(data.getOperationName());
                                dsAdmin.addOperation(operation);
                                messagePropertyDO.setOperationId(dsAdmin.getOperation(
                                        messagePropertyDO.getServiceId(), data.getOperationName())
                                        .getOperationID());
                            } else {
                                serviceDO = new ServiceDO();
                                serviceDO.setServerID(messagePropertyDO.getServerId());
                                serviceDO.setName(data.getServiceName());
                                dsAdmin.addService(serviceDO);

                                messagePropertyDO.setServiceId(dsAdmin.getService(messagePropertyDO.getServerId(),
                                                                                  data.getServiceName()).getId());

                                operation.setServiceID(messagePropertyDO.getServiceId());
                                operation.setName(data.getOperationName());
                                dsAdmin.addOperation(operation);

                                messagePropertyDO.setOperationId(dsAdmin.getOperation(
                                        messagePropertyDO.getServiceId(), data.getOperationName())
                                        .getOperationID());
                            }
                        } else {
                            messagePropertyDO.setOperationId(dsAdmin.getOperation(messagePropertyDO.getServiceId(),
                                                                                  data.getOperationName()).getOperationID());
                        }
                    } catch (BAMException e) {
                        log.error("Could not retrieve operation from DB ", e);
                    }

                    try {
                        if (dsAdmin.getActivityForActivityID(data.getActivityId()) == null) {
                            activity = new ActivityDO();
                            activity.setActivityId(data.getActivityId());
                            activity.setName(data.getActivityName());
                            activity.setDescription(data.getActivityDescription());
                            dsAdmin.addActivity(activity);
                            messagePropertyDO.setActivityKeyId(dsAdmin.getActivityForActivityID(
                                    data.getActivityId()).getActivityKeyId());
                        } else {
                            messagePropertyDO.setActivityKeyId(dsAdmin.getActivityForActivityID(
                                    data.getActivityId()).getActivityKeyId());
                        }
                    } catch (BAMException e) {
                        log.error("Could not retrieve activity from DB ", e);
                    }

                    try {
                        MessageDO msg = dsAdmin.getMessage(data.getMessageId(), messagePropertyDO.getOperationId(),
                                                           messagePropertyDO.getActivityKeyId());
                        if (msg != null) {
                            messagePropertyDO.setMessageKeyId(msg.getMessageKeyId());
                        } else {
                            MessageDO mesg = new MessageDO();
                            ActivityDO activityDO = dsAdmin.getActivityForActivityID(data.getActivityId());
                            if (activityDO != null) {
                                mesg.setMessageId(data.getMessageId());
                                mesg.setActivityKeyId(messagePropertyDO.getActivityKeyId());
                                mesg.setOperationId(messagePropertyDO.getOperationId());
                                // TODO timestamp fix
                                mesg.setTimestamp(BAMCalendar.getInstance(Calendar.getInstance()).getBAMTimestamp());
                                mesg.setIPAddress(data.getRemoteIPAddress());
                                mesg.setUserAgent(data.getUserAgent());

                                //TODO: Data Services supports returning the ID
                                dsAdmin.addMessage(mesg);
                                messagePropertyDO.setMessageKeyId(dsAdmin.getMessage(
                                        data.getMessageId(),
                                        messagePropertyDO
                                                .getOperationId(),
                                        messagePropertyDO
                                                .getActivityKeyId())
                                        .getMessageKeyId());
                            }

                        }
                    } catch (BAMException e) {
                        log.error("Could not retrieve message from DB", e);
                    }

                    try {
//                            MessagePropertyDO property = dsAdmin.getPropertyofMessage(
//                                    messagePropertyDO.getMessageKeyId(), messagePropertyDO.getActivityKeyId(),
//                                    messagePropertyDO.getKey());
//                            if (property == null) {
                        Map<String, String> propertyList = new HashMap<String, String>();
                        for (PropertyFilterData propertyFilterData : data.getXPathEvaluations().keySet()) {
                            propertyList.put(propertyFilterData.getExpressionKey(),
                                             data.getXPathEvaluations().get(propertyFilterData));
                        }
                        ArrayList<String> keyList = new ArrayList<String>();
                        ArrayList<String> valueList = new ArrayList<String>();
                        Set<String> keys = propertyList.keySet();
                        for (Iterator i = keys.iterator(); i.hasNext();) {
                            String key = (String) i.next();
                            String value = propertyList.get(key);
                            keyList.add(key);
                            valueList.add(value);
                        }
                        String valueArray[] = new String[keyList.size()];
                        String keyArray[] = new String[keyList.size()];
                        keyList.toArray(keyArray);
                        valueList.toArray(valueArray);
                        messagePropertyDO.setKeyArray(keyArray);
                        messagePropertyDO.setValueArray(valueArray);
                        dsAdmin.addMessageProperty(messagePropertyDO);
                        //   }
//                               else {
//                                messagePropertyDO.setMessagePropertyKeyId(property.getMessagePropertyKeyId());
//                            }
                    } catch (BAMException e) {
                        log.error("Could not add message property ", e);
                    }
                }

            }
        }
    }


    /**
     * Encapsulates event data relating to message tracing.
     */
    class ActivityData {

        private String activityId;
        private String messageId;
        private String operationName;
        private String serverName;
        private String serviceName;
        private String activityName;
        private String activityDescription;
        private String userAgent;
        private String remoteIPAddress;
        private String messageDirection;
        private String messageBody;
        private String timeStamp;
        private String requestMessageStatus;
        private String responseMessageStatus;
        private String outMessageID;

        private Map<String, String> properties;
        private Map<PropertyFilterData, String> xpathEvaluations;

        public String getActivityId() {
            return activityId;
        }

        public void setActivityId(String activityId) {
            this.activityId = activityId;
        }

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        public String getOperationName() {
            return operationName;
        }

        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }

        public String getServerName() {
            return serverName;
        }

        public void setServerName(String serverName) {
            this.serverName = serverName;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getActivityName() {
            return activityName;
        }

        public void setActivityName(String activityName) {
            this.activityName = activityName;
        }

        public String getActivityDescription() {
            return activityDescription;
        }

        public void setActivityDescription(String activityDescription) {
            this.activityDescription = activityDescription;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public void setUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }

        public String getRemoteIPAddress() {
            return remoteIPAddress;
        }

        public void setRemoteIPAddress(String remoteIPAddress) {
            this.remoteIPAddress = remoteIPAddress;
        }

        public String getMessageDirection() {
            return messageDirection;
        }

        public void setMessageDirection(String messageDirection) {
            this.messageDirection = messageDirection;
        }

        public String getMessageBody() {
            return messageBody;
        }

        public void setMessageBody(String messageBody) {
            this.messageBody = messageBody;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        public Map<PropertyFilterData, String> getXPathEvaluations() {
            return xpathEvaluations;
        }

        public void setXPathEvaluations(Map<PropertyFilterData, String> xpathEvaluations) {
            this.xpathEvaluations = xpathEvaluations;
        }

        public String getRequestMessageStatus() {
            return requestMessageStatus;
        }

        public void setRequestMessageStatus(String requestMessageStatus) {
            this.requestMessageStatus = requestMessageStatus;
        }

        public String getResponseMessageStatus() {
            return responseMessageStatus;
        }

        public void setResponseMessageStatus(String responseMessageStatus) {
            this.responseMessageStatus = responseMessageStatus;
        }

        public String getOutMessageID() {
            return outMessageID;
        }

        public void setOutMessageID(String outMessageID) {
            this.outMessageID = outMessageID;
        }

        public void setProperties(Map<String, String> properties) {
            this.properties = properties;
        }

        public Map<String, String> getProperties() {
            return properties;
        }
    }

    /**
     * Encapsulates event data relating to message tracing.
     */
    class PropertyFilterData {
        private String serverName;
        private String expressionKey;
        private String alias;
        private String expression;
        private String[] namespaces;


        public String getExpressionKey() {
            return expressionKey;
        }

        public void setExpressionKey(String expressionKey) {
            this.expressionKey = expressionKey;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public String getExpression() {
            return expression;
        }

        public void setExpression(String expression) {
            this.expression = expression;
        }

        public String[] getNamespaces() {
            return namespaces;
        }

        public void setNamespaces(String[] namespaces) {
            this.namespaces = namespaces;
        }

        public String getServerName() {
            return serverName;
        }

        public void setServerName(String serverName) {
            this.serverName = serverName;
        }
    }

}
