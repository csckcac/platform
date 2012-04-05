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
package org.wso2.carbon.issue.tracker.mgt;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;
import org.wso2.carbon.logging.appenders.CircularBuffer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Create a jira when specific log event occurs, typically ERRORS or FAULT.
 */
public class JiraAppender extends AppenderSkeleton {

    private static ExecutorService threadExecutor;
    public static final int JIRA_THREAD_POOL_SIZE = 5;
     private CircularBuffer circularBuffer;


    public JiraAppender(){

    }


    public JiraAppender(CircularBuffer circularBuffer){
        this.circularBuffer=circularBuffer;
    }


    /**
     * @param loggingEvent specific to log level
     */
    protected void append(LoggingEvent loggingEvent) {

        if (!new DefaultEvaluator().isTriggeringEvent(loggingEvent)) {
            return;
        }
//        if (threadExecutor == null) {
//            init();
//        }
//        threadExecutor.submit(new JiraCreateTask(loggingEvent));

        if(null !=circularBuffer){
            circularBuffer.append(loggingEvent);
        }

    }

    /**
     * Initialize the thread pool for jira issue create
     */
    private void init() {
        threadExecutor = Executors.newFixedThreadPool(JIRA_THREAD_POOL_SIZE);

    }

    /**
     * If some error occurred of the JiraAppender , before terminate
     * current JiraCreateTask threads will be execute.
     */
    public void close() {
        threadExecutor.shutdown();

    }

    /**
     * default layout used to print the thowable information.
     *
     * @return true if we use the default layout
     */
    public boolean requiresLayout() {
        return true;
    }

}

/**
 * This inner class used to evaluate the triggered event level.
 */
class DefaultEvaluator implements TriggeringEventEvaluator {

    public boolean isTriggeringEvent(LoggingEvent loggingEvent) {

        return loggingEvent.getLevel().isGreaterOrEqual(Level.ERROR);
    }
}