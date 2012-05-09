/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mediator.transform;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.mediators.AbstractMediator;
//import org.milyn.BAM;
//import org.milyn.container.ExecutionContext;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Transforms the current message payload using the given BAM configuration.
 * The current message context is replaced with the result as XML.
 */
public class BamMediator extends AbstractMediator {
    public enum TYPES {
        TEXT,
        XML
    }

    /** BAM engine */
    //private BAM bam = null;
    /** BAM configuration file */
    private String configKey = null;
    /** This lock is used to create the bam configuration synchronously */
    private volatile Lock lock = new ReentrantLock();

    private Input input = null;

    private Output output = null;

    public boolean mediate(MessageContext synCtx) {
        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : BAM mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        // check weather we need to create the bam configuration
        lock.lock();
        try {
            if (isCreationOrRecreationRequired(synCtx.getConfiguration())) {
                //bam = createBamConfig(synCtx);
            }
        } finally {
            lock.unlock();
        }

        // get the input as an stream
        ByteArrayInputStream byteArrayInputStream = input.process(synCtx, synLog);

        // create the execution context for bam. This is required for every message
        //ExecutionContext executionContext = bam.createExecutionContext();

        // create a output stream for store the result
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamResult streamResult = new StreamResult(outputStream);

        // filter the message through bam
        //bam.filterSource(executionContext, new StreamSource(byteArrayInputStream), streamResult);

        // add result
        output.process(outputStream, synCtx, synLog);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("End : BAM mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        return true;
    }

    /**
     * Create the smoooks configuration from the configuration key. BAM configuration can be
     * stored as a local entry or can be stored in the registry.
     * //@param synCtx synapse context
     * @return BAM configuration
     */
    /*private BAM createBamConfig(MessageContext synCtx) {

        return null;
    }*/

    private boolean isCreationOrRecreationRequired(SynapseConfiguration synCfg) {
        return true;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    /*public Input getInput() {
        return input;
    }*/

    public Output getOutput() {
        return output;
    }

    /*public void setInput(Input input) {
        this.input = input;
    }*/

    public void setOutput(Output output) {
        this.output = output;
    }
}