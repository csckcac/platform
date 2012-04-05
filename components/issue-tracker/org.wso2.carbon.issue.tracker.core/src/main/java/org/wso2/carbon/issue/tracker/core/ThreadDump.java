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
package org.wso2.carbon.issue.tracker.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.issue.tracker.adapter.exceptions.IssueTrackerException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

/**
 * this class is responsible for generating thread dump ata a given time.
 */

public class ThreadDump {

     private static final Log log = LogFactory.getLog(ThreadDump.class);

    /**
     * method to obtain thread dump
     * @return thread dump as a string
     */
    public String getThreadDump() {

        StringBuffer threadDump = new StringBuffer();

        threadDump.append("Generating Thread-dump at:").append((new java.util.Date()).toString()).append("\n");
        threadDump.append("--------------------------------------------------------------------\n");
        Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
        Iterator<Thread> itr = map.keySet().iterator();
        while (itr.hasNext()) {
            Thread t = itr.next();
            StackTraceElement[] elem = map.get(t);
            threadDump.append("\"").append(t.getName()).append("\"");
            threadDump.append(" prio=").append(t.getPriority());
            threadDump.append(" tid=").append(t.getId());
            Thread.State state = t.getState();
            threadDump.append(" ").append(state);

            for (int i = 0; i < elem.length; i++) {
                threadDump.append("\n  at ");
                threadDump.append(elem[i].toString());
                threadDump.append("\n");
            }
            threadDump.append("--------------------------------------------------------------------\n");
        }


        return threadDump.toString();
    }

    /**
     * method to save thread dump in a temp file
     * @return  temp file
     * @throws IssueTrackerException
     */
    public File saveThreadDump() throws IssueTrackerException {

        File threadDumpFile = new File(System.getProperty(IssueTrackerConstants.JAVA_IO_TEMP_DIR) +
                "/" + IssueTrackerConstants.THREAD_DUMP_FILE_NAME);

        try {
            PrintWriter printWriter = new PrintWriter(threadDumpFile);
            printWriter.write(this.getThreadDump());
            printWriter.close();

        } catch (FileNotFoundException e) {

            String msg = "Error writing thread dump to "+ System.getProperty(IssueTrackerConstants.
                    JAVA_IO_TEMP_DIR) + "/" + IssueTrackerConstants.THREAD_DUMP_FILE_NAME;
           ExceptionHandler.handleException(msg,e,log);
        }

        return threadDumpFile;

    }


}


