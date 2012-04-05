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
package org.wso2.carbon.bam.data.publisher.activity.mediation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class BAMCalendar extends GregorianCalendar {

    public static final String SQL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /* quarter field can have a value in 0..3 */
    public static final int QUATER = 50;

    public BAMCalendar() {
    }

    public static BAMCalendar getInstance(Calendar cal) {
        return new BAMCalendar(cal);
    }

    public static BAMCalendar getInstance() {
        return new BAMCalendar(Calendar.getInstance());
    }


    /**
     * Constructs a <code>BAMCalendar</code> with the given date
     * and time set for the default time zone with the default locale.
     *
     * @param year       the value used to set the <code>YEAR</code> calendar field in the calendar.
     * @param month      the value used to set the <code>MONTH</code> calendar field in the calendar.
     *                   Month value is 0-based. e.g., 0 for January.
     * @param dayOfMonth the value used to set the <code>DAY_OF_MONTH</code> calendar field in the calendar.
     */
    public BAMCalendar(int year, int month, int dayOfMonth) {
        super(year, month, dayOfMonth);
    }


    /**
     * Constructs a <code>BAMCalendar</code> with the given date
     * and time set for the default time zone with the default locale.
     *
     * @param year       the value used to set the <code>YEAR</code> calendar field in the calendar.
     * @param month      the value used to set the <code>MONTH</code> calendar field in the calendar.
     *                   Month value is 0-based. e.g., 0 for January.
     * @param dayOfMonth the value used to set the <code>DAY_OF_MONTH</code> calendar field in the calendar.
     * @param hourOfDay  the value used to set the <code>HOUR_OF_DAY</code> calendar field
     *                   in the calendar.
     * @param minute     the value used to set the <code>MINUTE</code> calendar field
     *                   in the calendar.
     */
    public BAMCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
        super(year, month, dayOfMonth, hourOfDay, minute);
    }

    private BAMCalendar(Calendar cal) {
        this.setTime(cal.getTime());
    }

    public int get(int field) {
        complete();
        if (field == QUATER) {
            return super.get(Calendar.MONTH) / 3;
        } else {
            return super.get(field);
        }
    }

    /**
     * parse a timestamp of the from yyyy-MM-dd HH:mm:ss. Used in parsing timestamps retrieved from the DB.
     *
     * @param timestamp timestamp string of from yyyy-MM-dd HH:mm:ss
     * @return a Calendar instance with the time represented by the <code>timestamp</code>.
     * @throws java.text.ParseException
     */
    public static Calendar parseTimestamp(String timestamp) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(SQL_DATE_FORMAT, Locale.ENGLISH);
        Date date = dateFormat.parse(timestamp);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    /**
     * This method returns a string representation of the timestamp that is used when saving data to the db.
     *
     * @return string representation of the timestamp in the form yyyy-MM-dd HH:mm:ss
     */
    public String getBAMTimestamp() {
        DateFormat dateFormat = new SimpleDateFormat(SQL_DATE_FORMAT, Locale.ENGLISH);
        String dateString = dateFormat.format(this.getTime());
        return dateString;
    }

    /**
     * Creates a <code>BAMCalendar</code> at the start of the year of the given <code>BAMCalendar</code> instance
     *
     * @param cal the <code>BAMCalendar</code> instance used in selecting the year of the created calendar.
     * @return a <code>BAMCalendar</code> which represents the start of the year of <code>cal</code>
     */
    public static BAMCalendar getYear(BAMCalendar cal) {
        return new BAMCalendar(cal.get(Calendar.YEAR), BAMCalendar.JANUARY, 1, 0, 0);
    }

    /**
     * Creates a <code>BAMCalendar</code> at the start of the quarter of the given <code>BAMCalendar</code> instance
     *
     * @param cal the <code>BAMCalendar</code> instance used in selecting the quarter of the created calendar.
     * @return a <code>BAMCalendar</code> which represents the quarter of the year of <code>cal</code>
     */
    public static BAMCalendar getQuarter(BAMCalendar cal) {
        //find the month which starts this quarter.
        int startMonth = cal.get(QUATER) * 3;

        return new BAMCalendar(cal.get(Calendar.YEAR), startMonth, 1, 0, 0);
    }

    /**
     * Creates a <code>BAMCalendar</code> at the start of the month of the given <code>BAMCalendar</code> instance
     *
     * @param cal the <code>BAMCalendar</code> instance used in selecting the month of the created calendar.
     * @return a <code>BAMCalendar</code> which represents the quarter of the month of <code>cal</code>
     */
    public static BAMCalendar getMonth(BAMCalendar cal) {
        return new BAMCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1, 0, 0);
    }

    /**
     * Creates a <code>BAMCalendar</code> at the start of the day of the given <code>BAMCalendar</code> instance
     *
     * @param cal the <code>BAMCalendar</code> instance used in selecting the day of the created calendar.
     * @return a <code>BAMCalendar</code> which represents the quarter of the day of <code>cal</code>
     */
    public static BAMCalendar getDay(BAMCalendar cal) {
        return new BAMCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0);
    }

    /**
     * Creates a <code>BAMCalendar</code> at the start of the hour of the given <code>BAMCalendar</code> instance
     *
     * @param cal the <code>BAMCalendar</code> instance used in selecting the hour of the created calendar.
     * @return a <code>BAMCalendar</code> which represents the quarter of the hour of <code>cal</code>
     */
    public static BAMCalendar getHour(BAMCalendar cal) {
        return new BAMCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), 0);
    }


    public void add(int field, int amount) {
        if (field == QUATER) {
            super.add(Calendar.MONTH, amount * 3);
        } else {
            super.add(field, amount);
        }
    }
}