/**
 * QueueServiceSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: SNAPSHOT  Built on : Aug 07, 2010 (07:59:55 IST)
 */
package org.wso2.carbon.messagebox.sqs.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.messagebox.MessageBoxConstants;
import org.wso2.carbon.messagebox.MessageBoxException;
import org.wso2.carbon.messagebox.MessageBoxService;
import org.wso2.carbon.messagebox.sqs.internal.util.MessageBoxHolder;
import org.wso2.carbon.messagebox.sqs.internal.util.Utils;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.math.BigInteger;
import java.util.List;

/**
 * QueueServiceSkeleton java skeleton for the SQS QueueService
 */
public class QueueServiceSkeleton {

    private static final Log log = LogFactory.getLog(QueueServiceSkeleton.class);

    /**
     * Auto generated method signature
     * The ListQueues action returns a list of your queues.
     *
     * @param listQueues - contains queue name prefix
     */

    public ListQueuesResponse listQueues(ListQueues listQueues) throws AxisFault {
        String userName = UserCoreUtil.getTenantLessUsername(CarbonContext.getCurrentContext().getUsername());

        MessageBoxService messageBoxService = Utils.getMessageBoxService();
        String queueNamePrefix = listQueues.getQueueNamePrefix();
        if (queueNamePrefix == null) {
            queueNamePrefix = "";
        } else if (!Utils.validQueueName(queueNamePrefix)) {
            throw getInvalidParameterValueFault();
        }

        try {
            List<String> list = messageBoxService.listQueues(queueNamePrefix);
            int index = 0;
            URI[] uris = new URI[list.size()];
            for (String queueName : list) {
                uris[index] = Utils.constructResponseURL(queueName);
                index++;
            }
            ListQueuesResponse listQueuesResponse = new ListQueuesResponse();
            ListQueuesResult_type0 listQueuesResult_type0 = new ListQueuesResult_type0();
            ResponseMetadata_type0 responseMetadata_type0 = new ResponseMetadata_type0();
            responseMetadata_type0.setRequestId(Utils.getMessageRequestId());
            listQueuesResponse.setResponseMetadata(responseMetadata_type0);
            listQueuesResult_type0.setQueueUrl(uris);
            listQueuesResponse.setListQueuesResult(listQueuesResult_type0);

            if (log.isInfoEnabled()) {
                log.info("List of message boxes successfully retrieved with username :" + userName);
            }
            return listQueuesResponse;
        } catch (MessageBoxException e) {
            if (log.isWarnEnabled()) {
                log.warn("Failed to get list of message boxes.");
            }
            throw new FaultResponse(e, Utils.getMessageRequestId()).createAxisFault();
        }
    }

    private AxisFault getInvalidParameterValueFault() throws AxisFault {
        return new FaultResponse(Utils.getMessageRequestId(), "InvalidParameterValue ",
                                 "One or more parameters cannot be validated. ").
                createAxisFault();
    }


    /**
     * Auto generated method signature
     * The CreateQueue action creates a new queue, or returns the URL of an existing one.
     * When you request CreateQueue, you provide a name for the queue. To successfully create
     * a new queue, you must provide a name that is unique within the scope of your own queues.
     * If you provide the name of an existing queue, a new queue isn't created and an error
     * isn't returned. Instead, the request succeeds and the queue URL for the existing queue
     * is returned. Exception: if you provide a value for DefaultVisibilityTimeout that is different
     * from the value for the existing queue, you receive an error.
     *
     * @param createQueue - contains queue name to be created
     * @throws AxisFault if message box already exists and only if the new message box timeout value
     *                   is different from current one
     */

    public CreateQueueResponse createQueue(CreateQueue createQueue) throws AxisFault {

        String userName =UserCoreUtil.getTenantLessUsername(CarbonContext.getCurrentContext().getUsername());


        String messageBoxName = createQueue.getQueueName();
        if (!Utils.validQueueName(messageBoxName)) {
            throw getInvalidParameterValueFault();
        }

        long defaultVisibilityTimeout = MessageBoxConstants.DEFAULT_VISIBILITY_TIMEOUT;
        BigInteger visibilityTimeout = createQueue.getDefaultVisibilityTimeout();
        // if visibility timeout is not set, use the default value(30sec)
        if (visibilityTimeout != null) {
            defaultVisibilityTimeout = visibilityTimeout.longValue();
            if (defaultVisibilityTimeout > MessageBoxConstants.TWELVE_HOURS_IN_SECONDS ||
                defaultVisibilityTimeout < 0) {
                throw getInvalidParameterValueFault();
            }
        }

        try {
            MessageBoxService messageBoxService = MessageBoxHolder.getInstance().getMessageboxService();
            String queueName = messageBoxService.createMessageBox(messageBoxName,
                                                                  defaultVisibilityTimeout);

            CreateQueueResponse createQueueResponse = new CreateQueueResponse();
            CreateQueueResult createQueueResult = new CreateQueueResult();
            CreateQueueResult_type0 createQueueResult_type0 = new CreateQueueResult_type0();
            ResponseMetadata_type0 responseMetadata_type0 = new ResponseMetadata_type0();
            responseMetadata_type0.setRequestId(Utils.getMessageRequestId());
            createQueueResponse.setResponseMetadata(responseMetadata_type0);
            createQueueResult_type0.setQueueUrl(Utils.constructResponseURL(queueName));
            createQueueResult.setCreateQueueResult(createQueueResult_type0);
            createQueueResponse.setCreateQueueResult(createQueueResult_type0);

            if (log.isInfoEnabled()) {
                log.info("New Messagebox successfully created with username :" + userName +
                         " and messagebox name:" + messageBoxName);
            }
            return createQueueResponse;
        } catch (MessageBoxException e) {
            if (log.isWarnEnabled()) {
                log.warn(messageBoxName + " messagebox already exists with username :" + userName);
            }
            throw new FaultResponse(e, Utils.getMessageRequestId()).createAxisFault();
        }
    }
}
    