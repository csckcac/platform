/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.dao;

import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Map;

public interface MessageDAO {

    public static enum MessageType {
        INPUT,
        OUTPUT,
        FAILURE
    }

    public void setTask(TaskDAO task);

    public QName getName();

    public void setName(QName name);

    public void setData(Element message);

    public Element getBodyData();

    public void setHeader(Element header);

    public Element getHeader();

    public Long getId();

    public void setId(Long id);

    public MessageType getMessageType();

    public void setMessageType(MessageType messageType);

    public void addBodyPart(String partName, Element part);

    public Element getBodyPart(String partName);

    public Map<String, Element> getBodyParts();

    public void addHeaderPart(String partName, Element part);

    public Element getHeaderPart(String partName);

    public Map<String, Element> getHeaderParts();
}
