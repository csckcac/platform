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
package org.wso2.carbon.bam.gauges.ui;

import org.wso2.carbon.bam.stub.listadmin.types.carbon.MonitoredServerDTO;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.ServiceDO;
import org.wso2.carbon.bam.stub.statquery.Data;

import java.text.DecimalFormat;
import java.util.Random;

/**
 * This class used to perform gauges common functions
 */
public class GaugesUtils {

    protected static String serverArrayToString(MonitoredServerDTO[] serverDTOs, String separator1, String separator2) {
        StringBuffer result = new StringBuffer();

        if (serverDTOs != null && serverDTOs.length > 0) {
            result.append(serverDTOs[0].getServerId() + separator1 + serverDTOs[0].getServerURL());
            for (int i = 1; i < serverDTOs.length; i++) {
                result.append( separator2 + serverDTOs[i].getServerId() + separator1 + serverDTOs[i].getServerURL());
            }
        }
        return result.toString();
    }

    protected static String serviceArrayToString(ServiceDO[] serviceDTOs, String separator1, String separator2) {
        StringBuffer result = new StringBuffer();
        if (serviceDTOs != null && serviceDTOs.length > 0) {
            result.append(serviceDTOs[0].getId() + separator1 + serviceDTOs[0].getName());
            for (int i = 1; i < serviceDTOs.length; i++) {
                 result.append(separator2 + serviceDTOs[i].getId() + separator1 + serviceDTOs[i].getName());
            }
        }
        return result.toString();
    }

    protected Data generateRandomData(int num) {
         Random generator = new Random();
         DecimalFormat df1 = new DecimalFormat("##.##");
         DecimalFormat df2 = new DecimalFormat("###");

         Data data = new Data();

         double min = generator.nextDouble() * 10.0;
         double max = generator.nextDouble() * (10.0 - min) + min;
         double avg = generator.nextDouble() * (max - min) + min;

         int fault = generator.nextInt(num);
         int response = generator.nextInt(num) + fault + 1;
         int request = fault + response;

         data.setAvgTime(df1.format(avg));
         data.setMinTime(df1.format(min));
         data.setMaxTime(df1.format(max));

         data.setReqCount(df2.format(request));
         data.setResCount(df2.format(response));
         data.setFaultCount(df2.format(fault));

         return data;
     }

}
