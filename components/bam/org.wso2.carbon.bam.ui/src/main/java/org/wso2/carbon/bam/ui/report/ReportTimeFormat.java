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

package org.wso2.carbon.bam.ui.report;

/**
 * used to format time stamp for the reports
 */
public class ReportTimeFormat {
   // String[] monthArray = new String[]{"Jan", "Feb", "Mar","Apr", "May", "Jun", "Jul", "Aug", "Sep","Oct", "Nov", "Dec"};

    /**
     *
     * @param time un-formated time stamp
     * @return  formatted time stamp for the reports
     */
    public static String formatTime(String time) {
        String year = time.split("T")[0].split("-")[0];
        String month = time.split("T")[0].split("-")[1];
        String day = time.split("T")[0].split("-")[2];
        String hour = time.split("T")[1].split(":")[0];
        return year+"/"+ month+ "/" + day + " " + hour +":00"+":00";
    }

//    public static Timestamp getTimeStamp(String time){
//           SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyy/MM/dd hh:mm:ss");
//            Date date = null;
//            try {
//                date = simpleDateFormat.parse(time);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//        return new Timestamp(date.getTime());
//    }

}
