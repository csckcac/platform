package org.wso2.carbon.logging.summarizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.TimerTask;

/*
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
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
public class SummaryGeneratorTask extends TimerTask {

    private static final Log log = LogFactory.getLog(SummaryGeneratorTask.class);

    @Override
    public void run() {
        System.out.println("run()");
        /*ScriptScheduler scriptScheduler = new ScriptScheduler();
        try {
            scriptScheduler.runSummarizer();
        } catch (Exception e) {
            String msg = "Error in summarizer invocation";
            log.error(msg, e);
        }*/


    }
}

