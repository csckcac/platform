/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.bam.core.summary.generators;

import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.bam.util.BAMException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Calendar;


public abstract class AbstractSummaryGenerator implements SummaryGenerator {

    private static final Log log = LogFactory.getLog(AbstractSummaryGenerator.class);

    private int timeInterval;
    //private MonitoredServer server;

    /**
     * @param timeInterval can be one of BAMCalendar.YEAR,BAMCalendar.QUARTER, BAMCalendar.MONTH,
     *                     BAMCalendar.DAY_OF_MONTH, or BAMCalendar.HOUR_OF_DAY
     */
    public AbstractSummaryGenerator(int timeInterval) {
        this.timeInterval = timeInterval;
        //this.server = server;
    }

    //All the calculations should happen using start of the timePeriod (i.e. use getCalendarWithZeroTimeFields()).
    public void generateSummary() throws BAMException {
        Calendar nowTime = getCalendarWithZeroTimeFields(Calendar.getInstance());

        if (log.isDebugEnabled()) {
            log.debug("[" + this.getClass().getSimpleName() + " | " + getTimeString() + "] " + "Time Now:"
                    + BAMCalendar.getInstance(nowTime).getBAMTimestamp());
        }

        // In case the data in the DB had non-zero fields below hour.
        // But this should not happen because we zero-out the fields below hour when writing to DB.
        Calendar lastTime = getLatestSummaryTime();
        if (lastTime == null) return; //TODO: debug...

        Calendar zeroLastTime = getCalendarWithZeroTimeFields(lastTime);

        if (log.isDebugEnabled()) {
            log.debug("[" + this.getClass().getSimpleName() + " | " + getTimeString() + "] " + "Query last ran at:"
                    + BAMCalendar.getInstance(zeroLastTime).getBAMTimestamp());
        }

        Calendar itrTime = (Calendar) zeroLastTime.clone();
        addTime(itrTime, 1);

        while (itrTime.compareTo(nowTime) < 0) {
            //this is the lo hour.
            //bounds for the query should be (itrTime ... itrTime +1)

            if (log.isDebugEnabled()) {
                log.debug("[" + this.getClass().getSimpleName() + " | " + getTimeString() + "] "
                        + "Generating summary for:" + BAMCalendar.getInstance(itrTime).getBAMTimestamp());
            }

            BAMCalendar loTime = BAMCalendar.getInstance(itrTime);
            BAMCalendar hiTime = (BAMCalendar) loTime.clone();
            addTime(hiTime, 1);

            summarize(loTime, hiTime);

            addTime(itrTime, 1);
        }

    }

    protected abstract void summarizeHourly(BAMCalendar loHour, BAMCalendar hiHour);

    protected abstract void summarizeDaily(BAMCalendar loDay, BAMCalendar hiDay);

    protected abstract void summarizeMonthly(BAMCalendar loMonth, BAMCalendar hiMonth);

    protected abstract void summarizeQuarterly(BAMCalendar loQuarter, BAMCalendar hiQuarter);

    protected abstract void summarizeYearly(BAMCalendar loYear, BAMCalendar hiYear);

    protected abstract Calendar getLatestYearlySummaryTime() throws BAMException;

    protected abstract Calendar getLatestQuarterlySummaryTime() throws BAMException;

    protected abstract Calendar getLatestMonthlySummaryTime() throws BAMException;

    protected abstract Calendar getLatestDailySummaryTime() throws BAMException;

    protected abstract Calendar getLatestHourlySummaryTime() throws BAMException;

    private void summarize(BAMCalendar loTime, BAMCalendar hiTime) {

        if (log.isDebugEnabled()) {
            log.debug("[" + this.getClass().getSimpleName() + " | " + getTimeString() + "] Summary time: "
                    + BAMCalendar.getInstance(loTime).getBAMTimestamp() + ", " + getInstanceInfo());
        }

        switch (getTimeInterval()) {
            case BAMCalendar.HOUR_OF_DAY:
                summarizeHourly(loTime, hiTime);
                break;
            case BAMCalendar.DAY_OF_MONTH:
                summarizeDaily(loTime, hiTime);
                break;
            case BAMCalendar.MONTH:
                summarizeMonthly(loTime, hiTime);
                break;
            case BAMCalendar.QUATER:
                summarizeQuarterly(loTime, hiTime);
                break;
            case BAMCalendar.YEAR:
                summarizeYearly(loTime, hiTime);
                break;
            default:
                throw new IllegalArgumentException("Unexpected timeInterval");
        }
    }

    private Calendar getLatestSummaryTime() throws BAMException {
        Calendar latestSummaryTime;
        switch (getTimeInterval()) {
            case BAMCalendar.YEAR:
                latestSummaryTime = getLatestYearlySummaryTime();
                break;
            case BAMCalendar.QUATER:
                latestSummaryTime = getLatestQuarterlySummaryTime();
                break;
            case BAMCalendar.MONTH:
                latestSummaryTime = getLatestMonthlySummaryTime();
                break;
            case BAMCalendar.DAY_OF_MONTH:
                latestSummaryTime = getLatestDailySummaryTime();
                break;
            case BAMCalendar.HOUR_OF_DAY:
                latestSummaryTime = getLatestHourlySummaryTime();
                break;
            default:
                throw new IllegalArgumentException("Unexpected timeInterval");
        }
        return latestSummaryTime;
    }


    protected Calendar getCalendarWithZeroTimeFields(Calendar cal) {
        BAMCalendar zeroCal;
        BAMCalendar tempCal = BAMCalendar.getInstance(cal);
        switch (getTimeInterval()) {
            case BAMCalendar.YEAR:
                zeroCal = BAMCalendar.getYear(tempCal);
                break;
            case BAMCalendar.QUATER:
                zeroCal = BAMCalendar.getQuarter(tempCal);
                break;
            case BAMCalendar.MONTH:
                zeroCal = BAMCalendar.getMonth(tempCal);
                break;
            case BAMCalendar.DAY_OF_MONTH:
                zeroCal = BAMCalendar.getDay(tempCal);
                break;
            case BAMCalendar.HOUR_OF_DAY:
                zeroCal = BAMCalendar.getHour(tempCal);
                break;
            default:
                throw new IllegalArgumentException("Unexpected timeInterval");
        }
        return zeroCal;
    }

    protected String getTimeString() {
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

    protected void addTime(Calendar cal, int amount) {
        switch (getTimeInterval()) {
            case BAMCalendar.QUATER:
                cal.add(Calendar.MONTH, amount * 3);
                break;
            case BAMCalendar.YEAR:
            case BAMCalendar.MONTH:
            case BAMCalendar.HOUR_OF_DAY:
                cal.add(getTimeInterval(), amount);
                break;
            case BAMCalendar.DAY_OF_MONTH:   //TODO:HACK is this the only place DAY_OF_MONTH is used in calculations?
                cal.add(BAMCalendar.DAY_OF_YEAR, amount);
                break;
            default:
                throw new IllegalArgumentException("Unexpected timeInterval");

        }
    }

    public int getTimeInterval() {
        return timeInterval;
    }

    protected abstract String getInstanceInfo();

//    public MonitoredServer getServer() {
//        return server;
//    }
}
