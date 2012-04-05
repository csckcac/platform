/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.bam.integration.test.monitoredservers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.stub.configadmin.types.carbon.ServerDO;

import javax.xml.stream.XMLStreamException;

public class BAMServerUtils {

    private static String sessionCookie = null;

    public static final int SERVICE = 1;

    private static final Log log = LogFactory.getLog(BAMServerUtils.class);


    public static OMElement getServiceStatSystemEvent(String fileName) throws XMLStreamException {
        return new StAXOMBuilder(BAMServerUtils.class.getResourceAsStream("/" + fileName)).
                getDocumentElement();
    }

    public static ServerDO createServerObject(String serverUrl, String serverType) {
        ServerDO server = null;
        server = new ServerDO();
        server.setServerURL(serverUrl);
        server.setUserName("admin");
        server.setPassword("admin");
        server.setActive(false);
        server.setCategory(SERVICE);
        server.setServerType(serverType);
        server.setSubscriptionEPR("");
        server.setDescription("");
        server.setSubscriptionID(null);
        return server;
    }
}
