/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bam.util;

import java.util.regex.Pattern;

public class TimeRange {
    private int type = BAMCalendar.HOUR;
    private int value = 0;

    //TODO: needs documentation
    public TimeRange(int type, int value) {
        this.type = type;
        this.value = value;
    }

    /* approx */
    public int getRangeInHours() {
        int ret = value;
        switch (type) {
            case BAMCalendar.MONTH:
                ret *= 30;
                break;
            case BAMCalendar.DAY_OF_YEAR:
                ret *= 24;
                break;
        }
        return ret;
    }

    /* approx */
    public int getRangeInSeconds() {
        return getRangeInHours() * 60 * 60;
//        int ret = value;
//        switch (type) {
//            case BAMCalendar.MONTH:
//                ret *= 30;
//            case BAMCalendar.DATE:
//                ret *= 24;
//            case BAMCalendar.HOUR:
//                ret *= 60 * 60;
//        }
//        return ret;
    }


    public static TimeRange parseTimeRange(String timeRange) {
        String regexp = "[0-9]+[hdm]";

        if (Pattern.compile(regexp).matcher(timeRange).matches()) {
            int rangeValue = Integer.parseInt(timeRange.substring(0, timeRange.length()-1));
            int rangeType = BAMCalendar.HOUR;

            switch (timeRange.charAt(timeRange.length() - 1)) {
                case 'h': rangeType = BAMCalendar.HOUR; break;
                case 'd': rangeType = BAMCalendar.DAY_OF_YEAR; break;
                case 'm': rangeType = BAMCalendar.MONTH; break;
            }

            return new TimeRange(rangeType, rangeValue);
        } else {
            throw new IllegalArgumentException("Invalid time Range provided");
        }
    }

    public String toString() {
        String typeString = "INVALID";

        switch (type) {
            case BAMCalendar.HOUR: typeString = "h"; break;
            case BAMCalendar.DAY_OF_YEAR: typeString = "d"; break;
            case BAMCalendar.MONTH: typeString = "m"; break;
        }
        return value + typeString;
    }

    /* approx */
    public long getRangeInMilli() {
        return getRangeInSeconds() * 1000;
    }

    public int getType() {
        return type;
    }

    public int getValue() {
        return value;
    }
}
