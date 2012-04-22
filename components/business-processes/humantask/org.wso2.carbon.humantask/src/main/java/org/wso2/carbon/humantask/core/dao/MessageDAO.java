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

    void setTask(TaskDAO task);

    QName getName();

    void setName(QName name);

    void setData(Element message);

    Element getBodyData();

    void setHeader(Element header);

    Element getHeader();

    Long getId();

    void setId(Long id);

    MessageType getMessageType();

    void setMessageType(MessageType messageType);

    void addBodyPart(String partName, Element part);

    Element getBodyPart(String partName);

    Map<String, Element> getBodyParts();

    void addHeaderPart(String partName, Element part);

    Element getHeaderPart(String partName);

    Map<String, Element> getHeaderParts();
}
