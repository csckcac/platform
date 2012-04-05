package org.wso2.carbon.bam.core.archive;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.core.clients.BAMArchiverDSClient;
import org.wso2.carbon.bam.common.dataobjects.activity.MessageDataDO;
import org.wso2.carbon.bam.core.persistence.BAMPersistenceManager;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.bam.util.TimeRange;

import java.util.Calendar;

/**
 * Archives data from BAM_MESSAGE_DATA to BAM_MESSAGE_DATA_ARCHIVE for entries older than data
 * archival period global parameter.
 */
public class Archiver {

    private static final Log log = LogFactory.getLog(Archiver.class);

    private int timeInterval;

    public Archiver(int timeInterval) {
        this.timeInterval = timeInterval;
    }

    public void archive() throws BAMException {
        BAMCalendar nowTime = BAMCalendar.getInstance(Calendar.getInstance());

        if (log.isDebugEnabled()) {
            log.debug("[" + this.getClass().getSimpleName() + " | " + getTimeString() + "] " + "Time Now:"
                      + BAMCalendar.getInstance(nowTime).getBAMTimestamp());
        }

        BAMArchiverDSClient client = BAMUtil.getArchiverDSClient();

        Calendar lastTime = client.getLatestArchiveTimeStamp();
        if (lastTime == null) {
            lastTime = createEpochCalendar();
        }

        //Calendar zeroLastTime = getCalendarWithZeroTimeFields(lastTime);

        if (log.isDebugEnabled()) {
            log.debug("[" + this.getClass().getSimpleName() + " | " + getTimeString() + "] " + "Archive last ran at:"
                      + BAMCalendar.getInstance(lastTime).getBAMTimestamp());
        }

        TimeRange archival = BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry()).getDataArchivalPeriod();

        // do not archive anything if the archival period specified is 0.
        if (archival.getValue() != 0) {

            nowTime.add(archival.getType(), -1 * archival.getValue());
            lastTime.add(archival.getType(), -1 * archival.getValue());

            MessageDataDO[] datas = client.getMessageArchiveData(lastTime, nowTime);

            for (MessageDataDO data : datas) {
              //  client.archiveMessageData(data);
            }

            client.removePrimaryMessageData(lastTime, nowTime);

        }

    }

    private String getTimeString() {
        switch (getTimeInterval()) {
            case BAMCalendar.YEAR:
                return "Yearly";
            case BAMCalendar.QUATER:
                return "Quarterly";
            case BAMCalendar.MONTH:
                return "Monthly";
            case BAMCalendar.DAY_OF_MONTH:
                return "Daily";
            case BAMCalendar.HOUR_OF_DAY:
                return "Hourly";
            default:
                throw new IllegalArgumentException("Unexpected timeInterval");
        }
    }

    private int getTimeInterval() {
        return this.timeInterval;
    }

    private Calendar createEpochCalendar() {
        Calendar calendar = BAMCalendar.getInstance();
        calendar.setTimeInMillis(0);
        calendar.add(Calendar.YEAR, 1);

        return calendar;

    }

}
