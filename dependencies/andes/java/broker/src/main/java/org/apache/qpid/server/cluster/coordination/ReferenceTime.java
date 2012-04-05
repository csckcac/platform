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
package org.apache.qpid.server.cluster.coordination;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Class <code>ReferenceTime</code> Keeps the Reference time configured in the Configuration and
 * Given gives the time stamp which is needed to generated the message id.
 */
public class ReferenceTime {

    private long referenceTimeInMills;

    public ReferenceTime(String referenceTime) throws ParseException {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = df.parse(referenceTime);

        Calendar gc = new GregorianCalendar();
        gc.setTime(d);

        referenceTimeInMills = gc.getTimeInMillis();

    }


    /**
     * Returns the difference, measured in milliseconds, between the current time and reference Time
     * @return the difference, measured in milliseconds, between the current time and reference Time
     */
    public long getCurrentTime() {

        return (System.currentTimeMillis() -referenceTimeInMills);
    }


    public long getTime(long time) {
        return (time - referenceTimeInMills);
    }

}
