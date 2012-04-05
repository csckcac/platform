package org.wso2.carbon.bam.core.archive;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.util.BAMCalendar;

import java.util.TimerTask;

/**
 * Periodic task used to run archival function (@link Archiver).
 */
public class ArchiverTask extends TimerTask {

    private static Log log = LogFactory.getLog(ArchiverTask.class);

    private boolean signalled = false;      // Sever is shutting down. So do not run this task again

    @Override
    public void run() {

        // If the this Timer has been signaled let Activator know the task is not running and return
        if (signalled) {
            return;
        } else {
        }

        try {
            Archiver archiver = new Archiver(BAMCalendar.DAY_OF_MONTH);
            archiver.archive();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Message data archiving failed...: ", e);
                return;
            }
        }

    }

}
