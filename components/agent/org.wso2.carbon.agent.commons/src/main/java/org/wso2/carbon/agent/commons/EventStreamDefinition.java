/**
 *
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.agent.commons;

import org.wso2.carbon.agent.commons.exception.MalformedStreamDefinitionException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventStreamDefinition {
    private String streamId;
    private String name;
    private String version = "1.0.0";
    private String nickName;
    private String description;
    private List<String> tags;

    private List<Attribute> metaData;
    private List<Attribute> correlationData;
    private List<Attribute> payloadData;

    public EventStreamDefinition(String name, String version)
            throws MalformedStreamDefinitionException {
        this.name = name;
        String versionPattern = "^\\d+\\.\\d+\\.\\d+$";
        if (!version.matches(versionPattern)) {
            throw new MalformedStreamDefinitionException("version " + version + " does not adhere to the format x.x.x ");
        }
        this.version = version;
        generateSteamId();
    }

    public EventStreamDefinition(String name) {
        this.name = name;
        generateSteamId();
    }

    private void generateSteamId() {
        if (streamId == null) {
            this.streamId = name + "-" + version + "-" + UUID.randomUUID();
        }
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setMetaData(List<Attribute> metaData) {
        this.metaData = metaData;
    }

    public void setCorrelationData(List<Attribute> correlationData) {
        this.correlationData = correlationData;
    }

    public void setPayloadData(List<Attribute> payloadData) {
        this.payloadData = payloadData;
    }

    public String getStreamId() {
        return streamId;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getNickName() {
        return nickName;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<Attribute> getMetaData() {
        return metaData;
    }

    public List<Attribute> getCorrelationData() {
        return correlationData;
    }

    public List<Attribute> getPayloadData() {
        return payloadData;
    }

    /**
     * Stream Id is not used for comparing definitions
     * This is because this method is used to identify duplicates
     *
     * @param o compared Object
     * @return true if equal else false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EventStreamDefinition that = (EventStreamDefinition) o;

        if (correlationData != null ? !correlationData.equals(that.correlationData) : that.correlationData != null) {
            return false;
        }
        if (metaData != null ? !metaData.equals(that.metaData) : that.metaData != null) {
            return false;
        }
        if (payloadData != null ? !payloadData.equals(that.payloadData) : that.payloadData != null) {
            return false;
        }
        if (!name.equals(that.name)) {
            return false;
        }
        if (!version.equals(that.version)) {
            return false;
        }

        return true;
    }

    /**
     * Stream Id is not used for comparing definitions
     * This is because this method is used to identify duplicates
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + version.hashCode();
        result = 31 * result + (metaData != null ? metaData.hashCode() : 0);
        result = 31 * result + (correlationData != null ? correlationData.hashCode() : 0);
        result = 31 * result + (payloadData != null ? payloadData.hashCode() : 0);
        return result;
    }

    public void addTag(String tag) {
        if (tags == null) {
            tags = new ArrayList<String>();
        }
        tags.add(tag);
    }

    public void addMetaData(String name, AttributeType type) {
        if (metaData == null) {
            metaData = new ArrayList<Attribute>();
        }
        metaData.add(new Attribute(name, type));
    }

    public void addCorrelationData(String name, AttributeType type) {
        if (correlationData == null) {
            correlationData = new ArrayList<Attribute>();
        }
        correlationData.add(new Attribute(name, type));
    }

    public void addPayloadData(String name, AttributeType type) {
        if (payloadData == null) {
            payloadData = new ArrayList<Attribute>();
        }
        payloadData.add(new Attribute(name, type));
    }


}
