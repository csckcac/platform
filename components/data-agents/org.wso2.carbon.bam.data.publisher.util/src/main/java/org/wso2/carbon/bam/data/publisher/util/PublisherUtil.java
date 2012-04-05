/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.bam.data.publisher.util;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class PublisherUtil {

    private static Log log = LogFactory.getLog(PublisherUtil.class);



    public static PublisherConfiguration readConfigurationFromAgentConfig() {

        PublisherConfiguration publisherConfiguration = new PublisherConfiguration();

        String publisherConfigPath = CarbonUtils.getCarbonConfigDirPath() + "/" +
                                     BAMDataPublisherConstants.AGENT_CONFIG;
        try {
            OMElement publisherOMElement = new StAXOMBuilder(new FileInputStream(publisherConfigPath)).
                    getDocumentElement();

            OMElement threadPoolElement = publisherOMElement.getFirstChildWithName(new QName(
                    BAMDataPublisherConstants.PUBLISHER_CONFIG_THREAD_POOL_ELEMENT));
            OMElement taskQueueSize = threadPoolElement.getFirstChildWithName(new QName(
                    BAMDataPublisherConstants.PUBLISHER_CONFIG_TASK_QUEUE_SIZE_ELEMENT));
            OMElement corePoolSize = threadPoolElement.getFirstChildWithName(new QName(
                    BAMDataPublisherConstants.PUBLISHER_CONFIG_CORE_POOL_SIZE_ELEMENT));
            OMElement maxPoolSize = threadPoolElement.getFirstChildWithName(new QName(
                    BAMDataPublisherConstants.PUBLISHER_CONFIG_MAX_POOL_SIZE_ELEMENT));

            OMElement eventQueueSize = publisherOMElement.getFirstChildWithName(new QName(
                    BAMDataPublisherConstants.PUBLISHER_CONFIG_EVENT_QUEUE_SIZE_ELEMENT));

            OMElement connectionPoolElement =  publisherOMElement.getFirstChildWithName(new QName(
                    BAMDataPublisherConstants.PUBLISHER_CONFIG_CONNECTION_POOL_ELEMENT));
            OMElement maxIdleConnections = connectionPoolElement.getFirstChildWithName(new QName(
                    BAMDataPublisherConstants.PUBLISHER_CONFIG_MAX_IDLE_SIZE_ELEMENT));
            OMElement evictionTimePeriod = connectionPoolElement.getFirstChildWithName(new QName(
                    BAMDataPublisherConstants.PUBLISHER_CONFIG_TIME_GAP_EVICTION_RUN_ELEMENT));
            OMElement minIdleTimeInPool = connectionPoolElement.getFirstChildWithName(new QName(
                    BAMDataPublisherConstants.PUBLISHER_CONFIG_MIN_IDLE_TIME_ELEMENT));


            publisherConfiguration.setEventQueueSize(Integer.parseInt(eventQueueSize.getText()));
            publisherConfiguration.setTaskQueueSize(Integer.parseInt(taskQueueSize.getText()));
            publisherConfiguration.setCorePoolSize(Integer.parseInt(corePoolSize.getText()));
            publisherConfiguration.setMaxPoolSize(Integer.parseInt(maxPoolSize.getText()));

            publisherConfiguration.setMaxIdleConnections(Integer.parseInt(maxIdleConnections.getText()));
            publisherConfiguration.setEvictionTimePeriod(Long.parseLong(evictionTimePeriod.getText()));
            publisherConfiguration.setMinIdleTimeInPool(Long.parseLong(minIdleTimeInPool.getText()));

        } catch (XMLStreamException e) {
            log.error("Invalid configuration in publisher.xml", e);
        } catch (FileNotFoundException e) {
            log.error(publisherConfigPath + " file not found. Using default configurations");
        }
        return publisherConfiguration;
    }
}
