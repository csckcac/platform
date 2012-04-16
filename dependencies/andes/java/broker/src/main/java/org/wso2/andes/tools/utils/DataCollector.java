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
package org.wso2.andes.tools.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * <code>DataCollector</code> is a class that can be used inside broker to
 * Collect statistics.
 */
@SuppressWarnings( "unused" )
public class DataCollector {

    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd-HH:mm:ss";

	static{
		try {

            long currentTimeMills = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
            Calendar cal = Calendar.getInstance();

            OUT = new BufferedWriter(new FileWriter("perfdata_broker" +System.currentTimeMillis()+ "_" +
                    sdf.format(cal.getTime()) + ".log"));
        } catch (Exception e) {

	        e.printStackTrace();
        }
	}

	private static Writer OUT;


    /**
     * Write the give values as a new line to the file
     * @param value  value to write
     */
	public static void write(String value){

		try {
	        OUT.write(value);
	        OUT.write("\n");
        } catch (IOException e) {

	        e.printStackTrace();
        }
	}

    /**
     * Log the time of an event with a interested value
     * @param key  key to identify event
     * @param time time of the event
     * @param value values associated with the event
     */
	public static void write(Object key, long time, long value){
		DataCollector.write(new StringBuffer().append("(").append(key.toString()).append(",")
	     		.append(time).append(",").append(value).append(")").toString());
	}


    /**
     * Long an event with the time difference between current time and the given time
     * @param key event key
     * @param time  given time
     */
	public static void write(Object key, long time){
		DataCollector.write(new StringBuffer().append("(").append(key.toString()).append(",")
	     		.append(time).append(",").append(",").append((System.currentTimeMillis() - time)).append(")").toString());
	}

    /**
     * Flush data to the file
     */
	public static void flush(){
		try {
	        OUT.flush();
        } catch (IOException e) {

	        e.printStackTrace();
        }
	}


}
