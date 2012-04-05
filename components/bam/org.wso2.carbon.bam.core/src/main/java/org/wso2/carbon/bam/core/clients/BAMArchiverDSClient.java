package org.wso2.carbon.bam.core.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.wso2.carbon.bam.common.ClientUtil;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.common.dataobjects.activity.MessageDataDO;

import org.wso2.carbon.bam.services.stub.bamarchiverds.BAMArchiverDSStub;
import org.wso2.carbon.bam.services.stub.bamarchiverds.types.ArchiveData;
import org.wso2.carbon.bam.services.stub.bamarchiverds.types.ArchiveTime;
import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.bam.util.BAMException;

import java.util.Calendar;

public class BAMArchiverDSClient {

    private static final Log log = LogFactory.getLog(BAMArchiverDSClient.class);

    private static final String BAM_ARCHIVER_DS = "BAMArchiverDS";
    private BAMArchiverDSStub stub;

    public BAMArchiverDSClient(String backendServerURL, ConfigurationContext configCtx)
            throws BAMException {

        try {
            String serviceURL = ClientUtil.getBackendEPR(backendServerURL, BAM_ARCHIVER_DS);
            stub = new BAMArchiverDSStub(configCtx, serviceURL);
        } catch (Exception e) {
            throw new BAMException(e.getMessage(), e);
        }
    }

    public BAMArchiverDSClient(String cookie, String backendServerURL,
                               ConfigurationContext configCtx) throws BAMException {
        try {
            String serviceURL = ClientUtil.getBackendEPR(backendServerURL, BAM_ARCHIVER_DS);
            stub = new BAMArchiverDSStub(configCtx, serviceURL);
        } catch (Exception e) {
            throw new BAMException(e.getMessage(), e);
        }
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public void cleanup() {
        try {
            stub._getServiceClient().cleanupTransport();
            stub._getServiceClient().cleanup();
            stub.cleanup();
        } catch (AxisFault axisFault) {
            if (log.isErrorEnabled()) {
                log.error("Stub cleanup failed: " + this.getClass().getName(), axisFault);
            }
        }
    }

    public Calendar getLatestArchiveTimeStamp() throws BAMException {
        Calendar timeStampCal = null;
        try {
            ArchiveTime timeStamp = stub.getLatestArchiveTimeStamp()[0];
            timeStampCal = timeStamp.getArchiveTime();

        } catch (Exception e) {
            throw new BAMException("Latest archive time stamp fetching failed", e);
        }

        return timeStampCal;
    }

    public MessageDataDO[] getMessageArchiveData(Calendar startTime, Calendar endTime)
            throws BAMException {
        MessageDataDO[] messages = null;
        try {
            ArchiveData[] datas = stub.getMessageArchiveData(startTime, endTime);

            if (datas == null) {
                return new MessageDataDO[0];
            }

            messages = new MessageDataDO[datas.length];

            for (int counter = 0; counter < datas.length; counter++) {
                MessageDataDO message = new MessageDataDO();
                ArchiveData data = datas[counter];

                message.setActivityKeyId(Integer.parseInt(data.getActivityId()));
                message.setIpAddress(data.getIpAddress());
                message.setMessageBody(data.getMessage());
                message.setMessageDataKeyId(Integer.parseInt(data.getMessageId()));
                message.setMessageKeyId(Integer.parseInt(data.getMessageId()));
                message.setMessageDirection(data.getDirection());
                message.setTimestamp(data.getTimestamp());
                if (data.getDirection().equals("Request")) {
                    message.setRequestMessageStatus(data.getStatus());
                } else {
                    message.setResponseMessageStatus(data.getStatus());
                }
                messages[counter] = message;
            }

        } catch (Exception e) {
            throw new BAMException("Achievable message data fetching failed..", e);
        }

        return messages;

    }

    public void removePrimaryMessageData(Calendar startTime, Calendar endTime) throws BAMException {
        try {
            stub.removePrimaryMessageData(startTime, endTime);
        } catch (Exception e) {
            throw new BAMException("Removing primary message data failed..", e);
        }
    }

    public void archiveMessageData(MessageDataDO data) throws BAMException {
        try {
            String status = "";
            if (data.getMessageDirection().equals("Request")) {
                status = data.getRequestMessageStatus();
            } else {
                status = data.getResponseMessageStatus();
            }
            stub.archiveMessageData(data.getMessageKeyId(), data.getActivityKeyId(),
                    BAMCalendar.parseTimestamp(data.getTimestamp()), data.getMessageDirection(), data.getMessageBody(),
                    data.getIpAddress(), status);
        } catch (Exception e) {
            throw new BAMException("Archiving message data failed..", e);
        }
    }

}
