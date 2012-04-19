/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.lb.common.conf.util;

//import com.amazonaws.auth.AWSCredentials;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.services.ec2.AmazonEC2Client;
//import com.amazonaws.services.ec2.model.GroupIdentifier;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;

import sun.misc.BASE64Encoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for Autoscale mediator
 */
public final class LoadBalancerConfigUtil {

    private static final Log log = LogFactory.getLog(LoadBalancerConfigUtil.class);

    private LoadBalancerConfigUtil() {
    }

    /**
     * handles the exception
     *
     * @param msg exception message
     */
    public static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    /**
     * handles the exception
     *
     * @param msg exception message
     * @param e   exception
     */
    public static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

    /**
     * Replaces the variables
     *
     * @param text input string
     * @return output String
     */
    public static String replaceVariables(String text) {
        int indexOfStartingChars;
        int indexOfClosingBrace;

        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        if ((indexOfStartingChars = text.indexOf("${")) != -1 &&
            (indexOfClosingBrace = text.indexOf("}")) != -1) { // Is a property used?
            String var = text.substring(indexOfStartingChars + 2,
                                        indexOfClosingBrace);

            String propValue = System.getProperty(var);
            if (propValue == null) {
                propValue = System.getenv(var);
            }
            if (propValue != null) {
                text = text.substring(0, indexOfStartingChars) + propValue +
                       text.substring(indexOfClosingBrace + 1);
            }
        }
        return text;
    }


}